package br.com.facilit.kanban.delivery.auth;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.delivery.common.ApiExamples;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AuthenticatedActorResolver actorResolver;

    public AuthRestController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository,
            AuthenticatedActorResolver actorResolver) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.actorResolver = actorResolver;
    }

    @GetMapping("/csrf")
    // O token vem do filtro de CSRF, não do cliente: fica fora do OpenAPI.
    public CsrfResponse csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
        csrfToken.getToken();
        return new CsrfResponse(csrfToken.getHeaderName(), "XSRF-TOKEN");
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody @Valid LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.password()));

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        csrfTokenRepository.saveToken(null, request, response);

        return AuthResponse.from(authentication, actorResolver.resolve(authentication));
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return AuthResponse.from(authentication, actorResolver.resolve(authentication));
    }

    public record LoginRequest(
            @NotBlank @Email @Schema(example = ApiExamples.RESPONSIBLE_EMAIL) String email,
            @NotBlank @Schema(example = ApiExamples.PASSWORD, accessMode = Schema.AccessMode.WRITE_ONLY) String password) {
    }

    public record CsrfResponse(
            @Schema(example = "X-XSRF-TOKEN") String headerName,
            @Schema(example = "XSRF-TOKEN") String cookieName) {
    }

    public record AuthResponse(
            @Schema(example = ApiExamples.RESPONSIBLE_EMAIL) String email,
            List<String> authorities,
            @Schema(example = ApiExamples.RESPONSIBLE_ID, description = "Nulo para o administrador") String responsibleId) {

        static AuthResponse from(Authentication authentication, Actor actor) {
            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .sorted()
                    .toList();
            String responsibleId = actor.responsibleId() == null ? null : actor.responsibleId().toString();
            return new AuthResponse(authentication.getName(), authorities, responsibleId);
        }
    }
}
