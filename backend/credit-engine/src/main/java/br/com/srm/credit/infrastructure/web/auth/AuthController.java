package br.com.srm.credit.infrastructure.web.auth;

import br.com.srm.credit.domain.shared.StructuredLog;
import br.com.srm.credit.infrastructure.security.JwtTokenService;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthController {

    private static final Set<String> SUPPORTED_ROLES = Set.of("ADMIN", "OPERATOR");

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;
    private final Duration expiration;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            Clock clock,
            @org.springframework.beans.factory.annotation.Value("${credit.security.jwt.expiration-minutes:480}")
                    long expirationMinutes) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.clock = clock;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            var roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replaceFirst("^ROLE_", ""))
                    .filter(SUPPORTED_ROLES::contains)
                    .distinct()
                    .collect(Collectors.toList());
            var token = jwtTokenService.createToken(authentication.getName(), roles);
            StructuredLog.info()
                    .step("auth-login-success")
                    .append("user", authentication.getName())
                    .append("roles", roles)
                    .log();
            return new LoginResponse(
                    "Bearer",
                    token,
                    authentication.getName(),
                    List.copyOf(roles),
                    OffsetDateTime.now(clock).plus(expiration));
        } catch (AuthenticationException exception) {
            StructuredLog.warn()
                    .step("auth-login-failure")
                    .append("user", request.username())
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        }
    }
}
