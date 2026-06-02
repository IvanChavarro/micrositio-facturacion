package co.com.claro.micrositiofacturacion.dto;

import jakarta.persistence.Column;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcifSerialesDTO {
    private Long idSerial;
    private String serial;
    private Long material;
    private String descripcion;
    private String centro;
    private String almacen;
    private String tps;
    private String lote;
    private String aliado;
    private String estadoInicial;
    private String tipoDiferencia;
    private BigDecimal saldoSap;
    private String tomaFisica;
    private String diferencia;
    private Date fechaInsert;
    private String usuarioInsert;
    private String estado;
    private Long idActaFk;
    private Long idCargueFk;
    private Long idBaseActas;
}
