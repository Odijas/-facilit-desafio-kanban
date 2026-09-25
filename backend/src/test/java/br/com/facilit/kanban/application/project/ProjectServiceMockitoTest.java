package br.com.facilit.kanban.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.common.TransactionRunner;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Interações do {@link ProjectService} com as portas (repositórios e transação), com dublês Mockito: o que é
 * chamado, em que ordem e o que nunca deve ser chamado.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceMockitoTest {

    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);
    private static final UUID RESPONSIBLE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ResponsibleRepository responsibleRepository;

    @Mock
    private TransactionRunner transactions;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        lenient().when(transactions.execute(any())).thenAnswer(invocation -> {
            Supplier<?> work = invocation.getArgument(0);
            return work.get();
        });
        service = new ProjectService(
                projectRepository,
                responsibleRepository,
                new ProjectScheduleCalculator(),
                transactions,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void blockedTransitionIsNeverSaved() {
        Project inProgress = project(new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null), ProjectStatus.IN_PROGRESS);
        when(projectRepository.findStaleSchedules(TODAY, ProjectScheduleRefresher.BATCH_SIZE)).thenReturn(List.of());
        when(projectRepository.findById(inProgress.id())).thenReturn(Optional.of(inProgress));

        assertThatThrownBy(() -> service.transition(inProgress.id(), ProjectStatus.OVERDUE, false, Actor.admin()))
                .isInstanceOf(TransitionBlockedException.class);

        verify(transactions).execute(any());
        verify(projectRepository, never()).save(any(), any());
    }

    @Test
    void readsRecalculateStaleProjectsBeforeQuerying() {
        UUID staleId = UUID.randomUUID();
        ProjectDates dates = new ProjectDates(TODAY.minusDays(5), TODAY.minusDays(1), TODAY.minusDays(5), null);
        when(projectRepository.findStaleSchedules(TODAY, ProjectScheduleRefresher.BATCH_SIZE))
                .thenReturn(List.of(new ProjectScheduleSnapshot(staleId, dates)));
        when(projectRepository.updateSchedules(anyList(), eq(TODAY))).thenReturn(1);
        ProjectFilter filter = new ProjectFilter(ProjectStatus.OVERDUE, null, null, null, null, null);
        when(projectRepository.search(filter, new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        service.search(filter, new PageQuery(0, 20));

        InOrder order = inOrder(projectRepository);
        order.verify(projectRepository).findStaleSchedules(TODAY, ProjectScheduleRefresher.BATCH_SIZE);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProjectScheduleUpdate>> updates = ArgumentCaptor.forClass(List.class);
        order.verify(projectRepository).updateSchedules(updates.capture(), eq(TODAY));
        order.verify(projectRepository).search(filter, new PageQuery(0, 20));
        assertThat(updates.getValue()).singleElement().satisfies(update -> {
            assertThat(update.id()).isEqualTo(staleId);
            assertThat(update.schedule().status()).isEqualTo(ProjectStatus.OVERDUE);
            assertThat(update.schedule().delayDays()).isEqualTo(1);
        });
    }

    @Test
    void createValidatesResponsiblesInsideTheTransactionAndDoesNotSave() {
        when(responsibleRepository.allExist(Set.of(RESPONSIBLE_ID))).thenReturn(false);

        assertThatThrownBy(() -> service.create(command(), Actor.admin()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactions).execute(any());
        verify(projectRepository, never()).save(any(), any());
    }

    @Test
    void createSavesWithTheCalculationDateInsideTheTransaction() {
        when(responsibleRepository.allExist(Set.of(RESPONSIBLE_ID))).thenReturn(true);
        when(projectRepository.save(any(), eq(TODAY))).thenAnswer(invocation -> invocation.getArgument(0));

        Project created = service.create(command(), Actor.admin());

        assertThat(created.status()).isEqualTo(ProjectStatus.NOT_STARTED);
        InOrder order = inOrder(transactions, responsibleRepository, projectRepository);
        order.verify(transactions).execute(any());
        order.verify(responsibleRepository).allExist(Set.of(RESPONSIBLE_ID));
        order.verify(projectRepository).save(any(), eq(TODAY));
    }

    @Test
    void deadlinesRejectsWindowOutsideOneToNinetyDaysBeforeReadingRepositories() {
        assertThatThrownBy(() -> service.deadlines(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("withinDays deve estar entre 1 e 90.");

        verify(projectRepository, never()).findStaleSchedules(any(), anyInt());
        verify(projectRepository, never()).deadlines(any(), any());
    }

    @Test
    void deadlinesRefreshesSchedulesAndUsesTheBusinessDateWindow() {
        when(projectRepository.findStaleSchedules(TODAY, ProjectScheduleRefresher.BATCH_SIZE)).thenReturn(List.of());
        when(projectRepository.deadlines(TODAY, TODAY.plusDays(7))).thenReturn(List.of());

        ProjectDeadlineIndicators result = service.deadlines(7);

        InOrder order = inOrder(projectRepository);
        order.verify(projectRepository).findStaleSchedules(TODAY, ProjectScheduleRefresher.BATCH_SIZE);
        order.verify(projectRepository).deadlines(TODAY, TODAY.plusDays(7));
        assertThat(result.withinDays()).isEqualTo(7);
        assertThat(result.from()).isEqualTo(TODAY);
        assertThat(result.to()).isEqualTo(TODAY.plusDays(7));
    }

    @Test
    void deleteChecksOwnershipBeforeDeleting() {
        Project project = project(new ProjectDates(TODAY, TODAY.plusDays(10), null, null), ProjectStatus.NOT_STARTED);
        when(projectRepository.findStaleSchedules(TODAY, ProjectScheduleRefresher.BATCH_SIZE)).thenReturn(List.of());
        when(projectRepository.findById(project.id())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> service.delete(project.id(), Actor.responsible(UUID.randomUUID())))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(projectRepository, never()).deleteById(any());
    }

    private static SaveProjectCommand command() {
        return new SaveProjectCommand("Portal", Set.of(RESPONSIBLE_ID), TODAY, TODAY.plusDays(10), null, null);
    }

    private static Project project(ProjectDates dates, ProjectStatus expectedStatus) {
        var schedule = new ProjectScheduleCalculator().calculate(dates, TODAY);
        assertThat(schedule.status()).isEqualTo(expectedStatus);
        return new Project(
                UUID.randomUUID(),
                "Portal",
                Set.of(RESPONSIBLE_ID),
                dates,
                schedule,
                new AuditMetadata(NOW, NOW));
    }
}
