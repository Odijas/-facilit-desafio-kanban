# F0-L2 — VERIFICAÇÃO DO USUÁRIO

Execute na raiz do projeto. O `set -euo pipefail` fica isolado em um Bash filho e não altera o shell interativo.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

echo "=== BACKEND BUILD + TESTES EXISTENTES ==="
cd backend
mvn -B -ntp clean verify
cd ..

echo "=== PROBE DO DOMÍNIO ==="
cat > /tmp/F0L2DomainProbe.java <<'JAVA'
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.responsible.Responsible;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class F0L2DomainProbe {
    public static void main(String[] args) {
        Instant createdAt = Instant.parse("2026-09-21T21:00:00Z");
        AuditMetadata audit = new AuditMetadata(createdAt, createdAt);
        Secretariat secretariat = new Secretariat(UUID.randomUUID(), "Tecnologia", audit);
        Responsible responsible = new Responsible(
                UUID.randomUUID(), "Ana", "ana@example.com", "Analista", secretariat.id(), audit);
        Set<UUID> sourceIds = new HashSet<>();
        sourceIds.add(responsible.id());
        Project project = new Project(
                UUID.randomUUID(), "Portal", ProjectStatus.NOT_STARTED, sourceIds,
                new ProjectDates(LocalDate.parse("2026-09-22"), LocalDate.parse("2026-09-30"), null, null), audit);

        sourceIds.clear();
        if (!project.responsibleIds().contains(responsible.id())) throw new AssertionError();
        expectUnsupported(project.responsibleIds()::clear);
        expectIllegal(() -> new Project(project.id(), " ", project.status(), project.responsibleIds(), project.dates(), audit));
        expectIllegal(() -> new Project(project.id(), project.name(), project.status(), Set.of(), project.dates(), audit));
        expectIllegal(() -> new Responsible(responsible.id(), responsible.name(), " ", responsible.position(), secretariat.id(), audit));
        expectIllegal(() -> new Secretariat(secretariat.id(), " ", audit));
        expectIllegal(() -> new AuditMetadata(createdAt, createdAt.minusSeconds(1)));
        System.out.println("F0_L2_DOMAIN_PROBE_GREEN");
    }

    private static void expectIllegal(Runnable action) {
        try { action.run(); throw new AssertionError(); } catch (IllegalArgumentException expected) { }
    }

    private static void expectUnsupported(Runnable action) {
        try { action.run(); throw new AssertionError(); } catch (UnsupportedOperationException expected) { }
    }
}
JAVA
javac -Xlint:all -Werror -cp backend/target/classes -d /tmp /tmp/F0L2DomainProbe.java
java -cp backend/target/classes:/tmp F0L2DomainProbe
rm -f /tmp/F0L2DomainProbe.java /tmp/F0L2DomainProbe.class

echo "=== DOCKER REGRESSION ==="
docker compose down --remove-orphans
docker compose up --build -d
for tentativa in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/kanban-health.json 2>/dev/null; then
    cat /tmp/kanban-health.json
    echo
    break
  fi
  if [ "$tentativa" -eq 30 ]; then
    docker compose logs --no-color backend
    exit 1
  fi
  sleep 2
done
curl -fsS -H 'Content-Type: application/json' \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql
echo

echo "=== GIT ==="
git diff --check

echo "=== F0-L2 GREEN ==="
VERIFY
STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige: Maven `BUILD SUCCESS`, testes existentes sem falha, `F0_L2_DOMAIN_PROBE_GREEN`, REST `UP`, GraphQL `UP`, `git diff --check` limpo e exit code 0.
