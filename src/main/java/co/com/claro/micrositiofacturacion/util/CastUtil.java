package co.com.claro.micrositiofacturacion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CastUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CastUtil() {
    }

    public static String serializeAuditPayload(Object payload) {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("No se pudo serializar el payload de auditoria a JSON: {}", ex.getMessage());
            return String.valueOf(payload);
        }
    }
}
