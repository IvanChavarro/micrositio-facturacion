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
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcifServiceImpl implements AcifService {

    private static final int CSV_WRITER_BUFFER_SIZE = 64 * 1024;
    private static final int CSV_FLUSH_INTERVAL = 1000;


    private final AcifBaseActasRepo acifBaseActasRepo;
    private final AcifSerialesRepo acifSerialesRepo;

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
        List<AcifBaseActasEntity> results = acifBaseActasRepo.findByIdCargueFk(idCargueFk);

        if (results.isEmpty()) {
            log.warn("No se encontraron actas ACIF para exportar a CSV, idCargueFk={}", idCargueFk);
            throw new AcifResultNotFoundException(idCargueFk);
        }

        return generateCsv(results, idCargueFk, "actas");
    }

    @Override
    public StreamingResponseBody generateSerialesCsv(Long idCargueFk) {
        List<AcifSerialesEntity> results = acifSerialesRepo.findByIdCargueFk(idCargueFk);

        if (results.isEmpty()) {
            log.warn("No se encontraron seriales ACIF para exportar a CSV, idCargueFk={}", idCargueFk);
            throw new AcifResultNotFoundException(idCargueFk);
        }

        return generateCsv(results, idCargueFk, "seriales");
    }

    private <T> StreamingResponseBody generateCsv(List<T> results, Long idCargueFk, String exportType) {
        List<CsvColumn> columns = getCsvColumns(results.get(0).getClass());

        log.info("Iniciando exportacion CSV de {} ACIF para idCargueFk={}", exportType, idCargueFk);
        return outputStream -> {
            try {
                long exportedRows = writeCsv(results, columns, outputStream);
                log.info("CSV de {} ACIF transmitido. idCargueFk={}, registros={}",
                        exportType, idCargueFk, exportedRows);
            } catch (IOException | RuntimeException exception) {
                log.error("Error generando CSV de {} ACIF para idCargueFk={}",
                        exportType, idCargueFk, exception);
                throw exception;
            }
        };
    }

    private <T> long writeCsv(List<T> rows, List<CsvColumn> columns, OutputStream outputStream)
            throws IOException {
        long exportedRows = 0L;

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                CSV_WRITER_BUFFER_SIZE);

        writer.write('\uFEFF');
        writeCsvHeader(writer, columns);
        writer.flush();

        for (T row : rows) {
            writeCsvRow(writer, row, columns);
            exportedRows++;
            if (exportedRows % CSV_FLUSH_INTERVAL == 0) {
                writer.flush();
            }
        }
        writer.flush();

        return exportedRows;
    }

    private List<CsvColumn> getCsvColumns(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(field -> {
                    field.setAccessible(true);
                    return new CsvColumn(field.getAnnotation(Column.class).name(), field);
                })
                .toList();
    }

    private void writeCsvHeader(BufferedWriter writer, List<CsvColumn> columns)
            throws IOException {
        for (int index = 0; index < columns.size(); index++) {
            writeCsvSeparator(writer, index);
            writer.write(escapeCsv(columns.get(index).header()));
        }
        writer.write("\r\n");
    }

    private void writeCsvRow(BufferedWriter writer, Object row, List<CsvColumn> columns)
            throws IOException {
        for (int index = 0; index < columns.size(); index++) {
            writeCsvSeparator(writer, index);
            writer.write(escapeCsv(toCsvValue(columns.get(index).value(row))));
        }
        writer.write("\r\n");
    }

    private void writeCsvSeparator(BufferedWriter writer, int columnIndex) throws IOException {
        if (columnIndex > 0) {
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

    private record CsvColumn(String header, Field field) {
        private Object value(Object row) {
            try {
                return field.get(row);
            } catch (IllegalAccessException exception) {
                throw new AcifConversionException("No se pudo obtener el valor de la columna CSV: " + header);
            }
        }
    }
}
