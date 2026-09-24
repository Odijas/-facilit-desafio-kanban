package br.com.facilit.kanban.infrastructure.security;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthenticatedActorResolver {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    private static final String RESPONSIBLE_AUTHORITY = "ROLE_RESPONSIBLE";

    private final SecurityUserJpaRepository repository;

    public AuthenticatedActorResolver(SecurityUserJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Actor resolve(Principal principal) {
        if (!(principal instanceof Authentication authentication) || !authentication.isAuthenticated()) {
            throw new ForbiddenOperationException("Authentication required");
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        if (authorities.contains(ADMIN_AUTHORITY)) {
            return Actor.admin();
        }
        if (authorities.contains(RESPONSIBLE_AUTHORITY)) {
            return repository.findByEmailIgnoreCase(authentication.getName())
                    .map(SecurityUserJpaEntity::getResponsibleId)
                    .map(Actor::responsible)
                    .orElseThrow(() -> new ForbiddenOperationException(
                            "Authenticated user is not linked to a responsible"));
        }
        throw new ForbiddenOperationException("Authenticated user has no supported role");
    }
}
