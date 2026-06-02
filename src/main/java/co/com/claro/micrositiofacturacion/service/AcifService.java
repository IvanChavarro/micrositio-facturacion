package co.com.claro.micrositiofacturacion.service;

import co.com.claro.micrositiofacturacion.dto.AcifBaseActasDTO;
import co.com.claro.micrositiofacturacion.dto.AcifSerialesDTO;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface AcifService {
    List<AcifBaseActasDTO> findBaseActasByIdCargue(Long idCargueFk);
    List<AcifSerialesDTO> findSerialesByIdCargue(Long idCargueFk);
    StreamingResponseBody generateBaseActasCsv(Long idCargueFk);
    StreamingResponseBody generateSerialesCsv(Long idCargueFk);
}
