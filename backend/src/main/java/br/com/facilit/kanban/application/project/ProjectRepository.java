package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    Optional<Project> findById(UUID id);

    PageResult<Project> findAll(PageQuery pageQuery);

    PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery);

    PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery);

    ProjectIndicators indicators();

    /**
     * Grava o projeto com status e métricas calculados para {@code scheduleCalculatedOn}.
     */
    Project save(Project project, LocalDate scheduleCalculatedOn);

    /**
     * Projetos não concluídos cujo status e métricas foram calculados antes de {@code today},
     * ordenados por id, no máximo {@code limit}. Projeto concluído não muda com o tempo.
     */
    List<ProjectScheduleSnapshot> findStaleSchedules(LocalDate today, int limit);

    /**
     * Grava status e métricas recalculados para {@code calculatedOn}. Só altera projetos ainda
     * calculados antes dessa data, então uma gravação concorrente mais nova prevalece. Não altera
     * {@code updatedAt}, porque o recálculo do sistema não é edição do usuário.
     *
     * @return quantidade de projetos alterados
     */
    int updateSchedules(List<ProjectScheduleUpdate> updates, LocalDate calculatedOn);

    void deleteById(UUID id);

    boolean existsByResponsibleId(UUID responsibleId);
}
