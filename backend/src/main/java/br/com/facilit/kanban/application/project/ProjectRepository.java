package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    Optional<Project> findById(UUID id);

    PageResult<Project> findAll(PageQuery pageQuery);

    PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery);

    PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery);

    ProjectIndicators indicators();

    Project save(Project project);

    void deleteById(UUID id);

    boolean existsByResponsibleId(UUID responsibleId);
}
