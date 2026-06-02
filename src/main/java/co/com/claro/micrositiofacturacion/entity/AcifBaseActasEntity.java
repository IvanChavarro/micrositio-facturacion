package co.com.claro.micrositiofacturacion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ACIF_BASE_ACTAS", schema = "GESTIONNEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcifBaseActasEntity {
    @Id
    @Column(name = "ID_BASE_ACTAS")
    private Long idBaseActas;

    @Column(name = "ESTADO_INICIAL")
    private String estadoInicial;

    @Column(name = "TPMT")
    private String tpmt;

    @Column(name = "AREA_INTEG")
    private String areaInteg;

    @Column(name = "MATERIAL")
    private Long material;

    @Column(name = "DESCRIPCION")
    private String descripcion;

    @Column(name = "CENTRO")
    private String centro;

    @Column(name = "REGIONAL")
    private String regional;

    @Column(name = "ALMACEN")
    private String almacen;

    @Column(name = "LOTE")
    private String lote;

    @Column(name = "CL_VALORACION")
    private String clValoracion;

    @Column(name = "UMB")
    private String umb;

    @Column(name = "CANT")
    private BigDecimal cant;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "VALOR_UNIT")
    private BigDecimal valorUnit;

    @Column(name = "FAMILIA")
    private String familia;

    @Column(name = "CAT_CONSUMO")
    private String catConsumo;

    @Column(name = "PERFIL_SERIE")
    private String perfilSerie;

    @Column(name = "SEGMENTO")
    private String segmento;

    @Column(name = "ALIADO")
    private String aliado;

    @Column(name = "BODEGA")
    private String bodega;

    @Column(name = "TIPO_BODEGA")
    private String tipoBodega;

    @Column(name = "ATIP")
    private String atip;

    @Column(name = "QTY_FISICO")
    private BigDecimal qtyFisico;

    @Column(name = "VR_TOMA_FISICA")
    private BigDecimal vrTomaFisica;

    @Column(name = "QTY_FINAL")
    private BigDecimal qtyFinal;

    @Column(name = "VR_FINAL")
    private BigDecimal vrFinal;

    @Column(name = "RESULTADO")
    private String resultado;

    @Column(name = "USUARIO_INSERT")
    private String usuarioInsert;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INSERT")
    private Date fechaInsert;

    @Column(name = "ID_CARGUE_FK")
    private Long idCargueFk;

    @Column(name = "ID_REGIONAL")
    private Long idRegional;
}
