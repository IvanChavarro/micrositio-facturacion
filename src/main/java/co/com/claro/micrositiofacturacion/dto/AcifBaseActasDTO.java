package co.com.claro.micrositiofacturacion.dto;

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
public class AcifBaseActasDTO {
    private Long idBaseActas;
    private String estadoInicial;
    private String tpmt;
    private String areaInteg;
    private Long material;
    private String descripcion;
    private String centro;
    private String regional;
    private String almacen;
    private String lote;
    private String clValoracion;
    private String umb;
    private BigDecimal cant;
    private BigDecimal valor;
    private BigDecimal valorUnit;
    private String familia;
    private String catConsumo;
    private String perfilSerie;
    private String segmento;
    private String aliado;
    private String bodega;
    private String tipoBodega;
    private String atip;
    private BigDecimal qtyFisico;
    private BigDecimal vrTomaFisica;
    private BigDecimal qtyFinal;
    private BigDecimal vrFinal;
    private String resultado;
    private String usuarioInsert;
    private Date fechaInsert;
    private Long idCargueFk;
    private Long idRegional;
}
