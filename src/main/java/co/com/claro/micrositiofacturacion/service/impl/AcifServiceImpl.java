package co.com.claro.micrositiofacturacion.service.impl;

import co.com.claro.micrositiofacturacion.dto.AcifBaseActasDTO;
import co.com.claro.micrositiofacturacion.dto.AcifSerialesDTO;
import co.com.claro.micrositiofacturacion.entity.AcifBaseActasEntity;
import co.com.claro.micrositiofacturacion.entity.AcifSerialesEntity;
import co.com.claro.micrositiofacturacion.exception.AcifConversionException;
import co.com.claro.micrositiofacturacion.exception.AcifResultNotFoundException;
import co.com.claro.micrositiofacturacion.repository.AcifBaseActasRepo;
import co.com.claro.micrositiofacturacion.repository.AcifSerialesRepo;
import co.com.claro.micrositiofacturacion.service.AcifService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcifServiceImpl implements AcifService {

    private static final int CSV_FETCH_SIZE = 500;
    private static final int CSV_WRITER_BUFFER_SIZE = 64 * 1024;
    private static final int CSV_FLUSH_INTERVAL = 1000;
    private static final String BASE_ACTAS_CSV_QUERY = """
            SELECT ID_BASE_ACTAS, ESTADO_INICIAL, TPMT, AREA_INTEG, MATERIAL, DESCRIPCION,
                   CENTRO, REGIONAL, ALMACEN, LOTE, CL_VALORACION, UMB, CANT, VALOR,
                   VALOR_UNIT, FAMILIA, CAT_CONSUMO, PERFIL_SERIE, SEGMENTO, ALIADO,
                   BODEGA, TIPO_BODEGA, ATIP, QTY_FISICO, VR_TOMA_FISICA, QTY_FINAL,
                   VR_FINAL, RESULTADO, USUARIO_INSERT, FECHA_INSERT, ID_CARGUE_FK, ID_REGIONAL
              FROM GESTIONNEW.ACIF_BASE_ACTAS
             WHERE ID_CARGUE_FK = ?
            """;
    private static final String SERIALES_CSV_QUERY = """
            SELECT ID_SERIAL, SERIAL, MATERIAL, DESCRIPCION, CENTRO, ALMACEN, TPS, LOTE,
                   ALIADO, ESTADO_INICIAL, TIPO_DIFERENCIA, SALDO_SAP, TOMA_FISICA,
                   DIFERENCIA, FECHA_INSERT, USUARIO_INSERT, ESTADO, ID_ACTA_FK,
                   ID_CARGUE_FK, ID_BASE_ACTAS
              FROM GESTIONNEW.ACIF_SERIALES
             WHERE ID_CARGUE_FK = ?
            """;

    private final AcifBaseActasRepo acifBaseActasRepo;
    private final AcifSerialesRepo acifSerialesRepo;
    private final DataSource dataSource;

    @Override
    public List<AcifBaseActasDTO> findBaseActasByIdCargue(Long idCargueFk) {
        log.debug("Consultando actas ACIF para idCargueFk={}", idCargueFk);
        List<AcifBaseActasEntity> results = acifBaseActasRepo.findByIdCargueFk(idCargueFk);

        if (results.isEmpty()) {
            log.warn("No se encontraron actas ACIF para idCargueFk={}", idCargueFk);
            throw new AcifResultNotFoundException(idCargueFk);
        }

        log.info("Se encontraron {} actas ACIF para idCargueFk={}", results.size(), idCargueFk);
        return results.stream()
                .map(this::toDto).map(dto -> (AcifBaseActasDTO) dto)
                .toList();
    }

    @Override
    public List<AcifSerialesDTO> findSerialesByIdCargue(Long idCargueFk) {
        List<AcifSerialesEntity> results = acifSerialesRepo.findByIdCargueFk(idCargueFk);

        if (results.isEmpty()) {
            log.warn("No se encontraron seriales ACIF para idCargueFk={}", idCargueFk);
            throw new AcifResultNotFoundException(idCargueFk);
        }

        log.info("Se encontraron {} seriales ACIF para idCargueFk={}", results.size(), idCargueFk);
        return results.stream()
                .map(this::toDto).map(dto -> (AcifSerialesDTO) dto)
                .toList();
    }

    @Override
    public StreamingResponseBody generateBaseActasCsv(Long idCargueFk) {
        if (!acifBaseActasRepo.existsByIdCargueFk(idCargueFk)) {
            log.warn("No se encontraron actas ACIF para exportar a CSV, idCargueFk={}", idCargueFk);
            throw new AcifResultNotFoundException(idCargueFk);
        }

        return generateCsv(BASE_ACTAS_CSV_QUERY, idCargueFk, "actas");
    }

    @Override
    public StreamingResponseBody generateSerialesCsv(Long idCargueFk) {
        if (!acifSerialesRepo.existsByIdCargueFk(idCargueFk)) {
            log.warn("No se encontraron seriales ACIF para exportar a CSV, idCargueFk={}", idCargueFk);
            throw new AcifResultNotFoundException(idCargueFk);
        }

        return generateCsv(SERIALES_CSV_QUERY, idCargueFk, "seriales");
    }

    private StreamingResponseBody generateCsv(String query, Long idCargueFk, String exportType) {
        log.info("Iniciando exportacion CSV de {} ACIF para idCargueFk={}", exportType, idCargueFk);
        return outputStream -> {
            try {
                long exportedRows = writeCsv(query, idCargueFk, outputStream);
                log.info("CSV de {} ACIF transmitido. idCargueFk={}, registros={}",
                        exportType, idCargueFk, exportedRows);
            } catch (SQLException exception) {
                log.error("Error generando CSV de {} ACIF para idCargueFk={}",
                        exportType, idCargueFk, exception);
                throw new IOException("Error generando CSV de ACIF", exception);
            }
        };
    }

    private long writeCsv(String query, Long idCargueFk, OutputStream outputStream)
            throws SQLException, IOException {
        long exportedRows = 0L;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     query,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {
            statement.setLong(1, idCargueFk);
            statement.setFetchSize(CSV_FETCH_SIZE);

            try (ResultSet resultSet = statement.executeQuery()) {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                        CSV_WRITER_BUFFER_SIZE);
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                writer.write('\uFEFF');
                writeCsvHeader(writer, metaData, columnCount);
                writer.flush();

                while (resultSet.next()) {
                    writeCsvRow(writer, resultSet, columnCount);
                    exportedRows++;
                    if (exportedRows % CSV_FLUSH_INTERVAL == 0) {
                        writer.flush();
                    }
                }
                writer.flush();
            }
        }

        return exportedRows;
    }

    private void writeCsvHeader(BufferedWriter writer, ResultSetMetaData metaData, int columnCount)
            throws SQLException, IOException {
        for (int index = 1; index <= columnCount; index++) {
            writeCsvSeparator(writer, index);
            writer.write(escapeCsv(metaData.getColumnLabel(index)));
        }
        writer.write("\r\n");
    }

    private void writeCsvRow(BufferedWriter writer, ResultSet resultSet, int columnCount)
            throws SQLException, IOException {
        for (int index = 1; index <= columnCount; index++) {
            writeCsvSeparator(writer, index);
            writer.write(escapeCsv(toCsvValue(resultSet.getObject(index))));
        }
        writer.write("\r\n");
    }

    private void writeCsvSeparator(BufferedWriter writer, int columnIndex) throws IOException {
        if (columnIndex > 1) {
            writer.write(',');
        }
    }

    private String toCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("\r\n", "\\n")
                .replace('\r', '\n')
                .replace("\n", "\\n")
                .replace('\t', ' ');
    }

    private String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private Object toDto(Object entity) {
        if (entity instanceof AcifBaseActasEntity) {
            AcifBaseActasDTO dto = new AcifBaseActasDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        } else if (entity instanceof AcifSerialesEntity) {
            AcifSerialesDTO dto = new AcifSerialesDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }
        String entityType = entity == null ? "null" : entity.getClass().getName();
        log.error("No se pudo convertir la entidad ACIF a DTO: tipo no reconocido={}", entityType);
        throw new AcifConversionException("No se pudo convertir la entidad a DTO: tipo no reconocido");
    }
}
