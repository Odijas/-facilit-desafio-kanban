package br.com.facilit.kanban.infrastructure.security;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.responsible.ResponsibleCredentialService;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.responsible.SaveResponsibleCommand;
import br.com.facilit.kanban.domain.responsible.Responsible;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResponsibleDemoBootstrap implements ApplicationRunner {

    private final SecurityUserJpaRepository securityUserRepository;
    private final ResponsibleService responsibleService;
    private final ResponsibleCredentialService credentialService;
    private final String name;
    private final String email;
    private final String position;
    private final String password;

    public ResponsibleDemoBootstrap(
            SecurityUserJpaRepository securityUserRepository,
            ResponsibleService responsibleService,
            ResponsibleCredentialService credentialService,
            @Value("${app.security.bootstrap-responsible-name:}") String name,
            @Value("${app.security.bootstrap-responsible-email:}") String email,
            @Value("${app.security.bootstrap-responsible-position:}") String position,
            @Value("${app.security.bootstrap-responsible-password:}") String password) {
        this.securityUserRepository = securityUserRepository;
        this.responsibleService = responsibleService;
        this.credentialService = credentialService;
        this.name = name;
        this.email = email;
        this.position = position;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        List<String> values = List.of(name, email, position, password);
        if (values.stream().allMatch(String::isBlank)) {
            return;
        }
        if (values.stream().anyMatch(String::isBlank)) {
            throw new IllegalStateException(
                    "Bootstrap responsible name, email, position and password must be configured together");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (securityUserRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            return;
        }

        Actor system = Actor.admin();
        Responsible responsible = responsibleService.create(
                new SaveResponsibleCommand(name.trim(), normalizedEmail, position.trim(), null),
                system);
        credentialService.setPassword(responsible.id(), password, system);
    }
}
