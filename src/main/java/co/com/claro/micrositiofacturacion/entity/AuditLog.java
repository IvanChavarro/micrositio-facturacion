package co.com.claro.micrositiofacturacion.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    private Long id;

    private String name;

    private String email;

    private Date executionTime;

    private String executionCommand;

    private String payload;

    private String responseService;
}
