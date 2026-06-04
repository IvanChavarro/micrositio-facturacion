package co.com.claro.micrositiofacturacion.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class FacturacionJwtAuthFilter extends OncePerRequestFilter {

    private final JwtClaimsService jwtClaimsService;

    public FacturacionJwtAuthFilter(JwtClaimsService jwtClaimsService) {
        this.jwtClaimsService = jwtClaimsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!requiresJwtAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtClaimsService.extractClaims(request.getHeader(HttpHeaders.AUTHORIZATION));
            String subject = claims.getSubject();
            Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);
            authentication.setDetails(claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException exception) {
            writeUnauthorized(response, "Token expired");
            return;
        } catch (UnsupportedJwtException | MalformedJwtException exception) {
            writeUnauthorized(response, "Invalid JWT format");
            return;
        } catch (SecurityException exception) {
            writeUnauthorized(response, "Invalid JWT signature");
            return;
        } catch (JwtException exception) {
            writeUnauthorized(response, "Invalid JWT");
            return;
        } catch (IllegalArgumentException exception) {
            writeUnauthorized(response, exception.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected void doFilterNestedErrorDispatch(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    private boolean requiresJwtAuthentication(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String requestUri = request.getRequestURI();
        return matchesProtectedPath(servletPath) || matchesProtectedPath(requestUri);
    }

    private boolean matchesProtectedPath(String path) {
        return path != null && path.startsWith("/api/facturacion");
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (!(rolesObj instanceof List<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
