package co.com.claro.micrositiofacturacion.service.impl;

import co.com.claro.micrositiofacturacion.entity.AuditLog;
import co.com.claro.micrositiofacturacion.security.JwtClaimsService;
import co.com.claro.micrositiofacturacion.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Pattern;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "multi-db.audit", name = "enabled", havingValue = "true")
public class AuditLogServiceImpl implements AuditLogService {

    private static final Pattern SCHEMA_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String INSERT_AUDIT_LOG_TEMPLATE = """
            INSERT INTO %s.audit_log
                (name, email, execution_time, execution_command, payload, response_service)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate auditJdbcTemplate;
    private final JwtClaimsService jwtClaimsService;
    private final String insertAuditLogSql;

    public AuditLogServiceImpl(
            @Qualifier("auditJdbcTemplate") JdbcTemplate auditJdbcTemplate,
            JwtClaimsService jwtClaimsService,
            @Value("${multi-db.audit.schema:audit}") String auditSchema) {
        this.auditJdbcTemplate = auditJdbcTemplate;
        this.jwtClaimsService = jwtClaimsService;
        this.insertAuditLogSql = INSERT_AUDIT_LOG_TEMPLATE.formatted(validateSchemaName(auditSchema));
    }

    @Override
    public AuditLog saveAuditLog(AuditLog auditLog) {
        Date executionTime = new Date();
        String authorization = resolveAuthorizationHeader();
        auditLog.setName(resolveCurrentUserName(authorization));
        auditLog.setEmail(resolveCurrentUserEmail(authorization));
        auditLog.setExecutionTime(executionTime);

        try {
            auditJdbcTemplate.update(insertAuditLogSql,
                    auditLog.getName(),
                    auditLog.getEmail(),
                    new Timestamp(executionTime.getTime()),
                    auditLog.getExecutionCommand(),
                    auditLog.getPayload(),
                    auditLog.getResponseService());
        } catch (Exception exception) {
            log.warn("No se pudo guardar el log de auditoria: {}", exception.getMessage());
        }

        return auditLog;
    }

    private String resolveCurrentUserEmail(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            log.warn("Authorization header ausente. No se puede resolver el email del usuario.");
            return null;
        }
        try {
            return jwtClaimsService.extractSubject(authorization);
        } catch (Exception exception) {
            log.warn("No se pudo resolver el email del usuario autenticado: {}", exception.getMessage());
            return null;
        }
    }

    private String resolveCurrentUserName(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            log.warn("Authorization header ausente. No se puede resolver el nombre del usuario.");
            return null;
        }
        try {
            return jwtClaimsService.extractName(authorization);
        } catch (Exception exception) {
            log.warn("No se pudo resolver el nombre del usuario autenticado: {}", exception.getMessage());
            return null;
        }
    }

    private String resolveAuthorizationHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

    private String validateSchemaName(String schema) {
        if (schema == null || !SCHEMA_NAME_PATTERN.matcher(schema).matches()) {
            throw new IllegalArgumentException("Invalid audit schema name: " + schema);
        }
        return schema;
    }
}
