package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.responsible.ResponsibleCredentialRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class InMemoryResponsibleCredentialRepository implements ResponsibleCredentialRepository {

    private final Map<UUID, StoredCredential> credentials = new LinkedHashMap<>();
    private final Set<String> otherLoginEmails = new HashSet<>();

    public void registerOtherLogin(String loginEmail) {
        otherLoginEmails.add(loginEmail.toLowerCase(Locale.ROOT));
    }

    public Optional<StoredCredential> find(UUID responsibleId) {
        return Optional.ofNullable(credentials.get(responsibleId));
    }

    @Override
    public void save(UUID responsibleId, String loginEmail, String rawPassword, Instant timestamp) {
        credentials.put(responsibleId, new StoredCredential(loginEmail, rawPassword, timestamp));
    }

    @Override
    public boolean deleteByResponsibleId(UUID responsibleId) {
        return credentials.remove(responsibleId) != null;
    }

    @Override
    public boolean existsForResponsible(UUID responsibleId) {
        return credentials.containsKey(responsibleId);
    }

    @Override
    public boolean loginEmailTakenByAnotherUser(String loginEmail, UUID responsibleId) {
        String normalized = loginEmail.toLowerCase(Locale.ROOT);
        return otherLoginEmails.contains(normalized)
                || credentials.entrySet().stream()
                        .anyMatch(entry -> !entry.getKey().equals(responsibleId)
                                && entry.getValue().loginEmail().equalsIgnoreCase(normalized));
    }

    public record StoredCredential(String loginEmail, String rawPassword, Instant timestamp) {
    }
}
