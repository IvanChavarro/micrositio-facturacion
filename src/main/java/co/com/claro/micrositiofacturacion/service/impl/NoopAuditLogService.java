package co.com.claro.micrositiofacturacion.service.impl;

import co.com.claro.micrositiofacturacion.entity.AuditLog;
import co.com.claro.micrositiofacturacion.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "multi-db.audit", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopAuditLogService implements AuditLogService {

    @Override
    public AuditLog saveAuditLog(AuditLog auditLog) {
        log.debug("Auditoria deshabilitada. Se omite persistencia del audit log.");
        return auditLog;
    }
}
