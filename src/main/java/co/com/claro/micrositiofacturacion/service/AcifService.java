package co.com.claro.micrositiofacturacion.service;

import co.com.claro.micrositiofacturacion.dto.AcifBaseActasDTO;
import co.com.claro.micrositiofacturacion.dto.AcifSerialesDTO;
import co.com.claro.micrositiofacturacion.dto.PageResponseDTO;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface AcifService {
    PageResponseDTO<AcifBaseActasDTO> findBaseActasByIdCargue(Long idCargueFk, Integer page, Integer size);
    PageResponseDTO<AcifSerialesDTO> findSerialesByIdCargue(Long idCargueFk, Integer page, Integer size);
    StreamingResponseBody generateBaseActasCsv(Long idCargueFk);
    StreamingResponseBody generateSerialesCsv(Long idCargueFk);
}
