package co.com.claro.micrositiofacturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ACIF_SERIALES", schema = "GESTIONNEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcifSerialesEntity {
    @Id
    @Column(name = "ID_SERIAL")
    private Long idSerial;

    @Column(name = "SERIAL")
    private String serial;

    @Column(name = "MATERIAL")
    private Long material;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "CENTRO")
    private String centro;

    @Column(name = "ALMACEN")
    private String almacen;

    @Column(name = "TPS")
    private String tps;

    @Column(name = "LOTE")
    private String lote;

    @Column(name = "ALIADO")
    private String aliado;

    @Column(name = "ESTADO_INICIAL")
    private String estadoInicial;

    @Column(name = "TIPO_DIFERENCIA")
    private String tipoDiferencia;

    @Column(name = "SALDO_SAP")
    private BigDecimal saldoSap;

    @Column(name = "TOMA_FISICA")
    private String tomaFisica;

    @Column(name = "DIFERENCIA")
    private String diferencia;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INSERT")
    private Date fechaInsert;

    @Column(name = "USUARIO_INSERT")
    private String usuarioInsert;

    @Column(name = "ESTADO")
    private String estado;

    @Column(name = "ID_ACTA_FK")
    private Long idActaFk;

    @Column(name = "ID_CARGUE_FK")
    private Long idCargueFk;

    @Column(name = "ID_BASE_ACTAS")
    private Long idBaseActas;
}
