import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { useMemo, useState } from "react";
import {
  createProject,
  createResponsible,
  deleteProject,
  KanbanApiError,
  listProjects,
  listResponsibles,
  listSecretariats,
  type Project,
  type ProjectFilters,
  type ProjectInput,
  type ProjectStatus,
  type ResponsibleInput,
  transitionProject,
  updateProject,
} from "../../api/kanban";
import { DeleteProjectDialog } from "./DeleteProjectDialog";
import { KanbanColumn } from "./KanbanColumn";
import { ProjectDialog } from "./ProjectDialog";
import { ProjectFiltersBar } from "./ProjectFiltersBar";
import {
  PROJECT_INDICATORS_QUERY_KEY,
  PROJECTS_QUERY_KEY,
  RESPONSIBLES_QUERY_KEY,
  SECRETARIATS_QUERY_KEY,
} from "./queryKeys";
import { ResponsibleDialog } from "./ResponsibleDialog";

const KANBAN_COLUMNS: ReadonlyArray<{
  status: ProjectStatus;
  label: string;
}> = [
  { status: "NOT_STARTED", label: "A iniciar" },
  { status: "IN_PROGRESS", label: "Em andamento" },
  { status: "OVERDUE", label: "Atrasado" },
  { status: "COMPLETED", label: "Concluído" },
];

type UpdateProjectVariables = {
  projectId: string;
  input: ProjectInput;
};

type TransitionProjectVariables = {
  projectId: string;
  status: ProjectStatus;
};

function operationErrorMessage(error: unknown): string {
  return error instanceof KanbanApiError
    ? error.message
    : "Não foi possível concluir a operação.";
}

export function KanbanBoard() {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<ProjectFilters>({});
  const [projectDialogOpen, setProjectDialogOpen] = useState(false);
  const [responsibleDialogOpen, setResponsibleDialogOpen] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [deleteCandidate, setDeleteCandidate] = useState<Project | null>(null);
  const [operationError, setOperationError] = useState<string | null>(null);

  const projectsQuery = useQuery({
    queryKey: [...PROJECTS_QUERY_KEY, filters],
    queryFn: () => listProjects(filters),
    placeholderData: keepPreviousData,
    retry: false,
  });
  const responsiblesQuery = useQuery({
    queryKey: RESPONSIBLES_QUERY_KEY,
    queryFn: () => listResponsibles(),
    retry: false,
  });
  const secretariatsQuery = useQuery({
    queryKey: SECRETARIATS_QUERY_KEY,
    queryFn: () => listSecretariats(),
    retry: false,
  });

  const responsibleNames = useMemo(() => {
    const names = new Map<string, string>();
    for (const responsible of responsiblesQuery.data ?? []) {
      names.set(responsible.id, responsible.name);
    }
    return names;
  }, [responsiblesQuery.data]);

  const refreshProjects = async (): Promise<void> => {
    await queryClient.invalidateQueries({ queryKey: PROJECTS_QUERY_KEY });
    await queryClient.invalidateQueries({
      queryKey: PROJECT_INDICATORS_QUERY_KEY,
    });
  };

  const createMutation = useMutation({
    mutationFn: (input: ProjectInput) => createProject(input),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setProjectDialogOpen(false);
      await refreshProjects();
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ projectId, input }: UpdateProjectVariables) =>
      updateProject(projectId, input),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setProjectDialogOpen(false);
      setEditingProject(null);
      await refreshProjects();
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  const transitionMutation = useMutation({
    mutationFn: ({ projectId, status }: TransitionProjectVariables) =>
      transitionProject(projectId, status),
    onMutate: () => setOperationError(null),
    onSuccess: refreshProjects,
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: (projectId: string) => deleteProject(projectId),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setDeleteCandidate(null);
      await refreshProjects();
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  const responsibleMutation = useMutation({
    mutationFn: (input: ResponsibleInput) => createResponsible(input),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setResponsibleDialogOpen(false);
      await queryClient.invalidateQueries({ queryKey: RESPONSIBLES_QUERY_KEY });
      await queryClient.invalidateQueries({ queryKey: PROJECTS_QUERY_KEY });
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  if (
    projectsQuery.isPending ||
    responsiblesQuery.isPending ||
    secretariatsQuery.isPending
  ) {
    return (
      <Stack spacing={2} sx={{ alignItems: "center", py: 6 }}>
        <CircularProgress aria-label="Carregando quadro Kanban" />
        <Typography color="text.secondary">Carregando quadro Kanban</Typography>
      </Stack>
    );
  }

  if (
    projectsQuery.isError ||
    responsiblesQuery.isError ||
    secretariatsQuery.isError
  ) {
    return (
      <Alert
        action={
          <Button
            onClick={() => {
              projectsQuery.refetch();
              responsiblesQuery.refetch();
              secretariatsQuery.refetch();
            }}
          >
            Tentar novamente
          </Button>
        }
        severity="error"
      >
        Não foi possível carregar o Kanban.
      </Alert>
    );
  }

  const projects = projectsQuery.data;
  const responsibles = responsiblesQuery.data;
  const secretariats = secretariatsQuery.data;
  const projectPending = createMutation.isPending || updateMutation.isPending;

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{ justifyContent: "space-between" }}
      >
        <Box>
          <Typography component="h2" variant="h5" sx={{ fontWeight: 700 }}>
            Quadro Kanban
          </Typography>
          <Typography color="text.secondary">
            Arraste os projetos entre as colunas para solicitar uma transição.
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button
            onClick={() => {
              setOperationError(null);
              setResponsibleDialogOpen(true);
            }}
            variant="outlined"
          >
            Novo responsável
          </Button>
          <Button
            onClick={() => {
              setEditingProject(null);
              setOperationError(null);
              setProjectDialogOpen(true);
            }}
            variant="contained"
          >
            Novo projeto
          </Button>
        </Stack>
      </Stack>

      <ProjectFiltersBar
        filters={filters}
        responsibles={responsibles}
        secretariats={secretariats}
        onChange={setFilters}
      />

      {operationError !== null && (
        <Alert onClose={() => setOperationError(null)} severity="error">
          {operationError}
        </Alert>
      )}

      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: "repeat(4, minmax(280px, 1fr))",
          gap: 2,
          overflowX: "auto",
          pb: 1,
        }}
      >
        {KANBAN_COLUMNS.map((column) => (
          <KanbanColumn
            key={column.status}
            label={column.label}
            status={column.status}
            projects={projects.filter(
              (project) => project.status === column.status,
            )}
            responsibleNames={responsibleNames}
            transitionPending={transitionMutation.isPending}
            onDeleteProject={setDeleteCandidate}
            onEditProject={(project) => {
              setEditingProject(project);
              setOperationError(null);
              setProjectDialogOpen(true);
            }}
            onDropProject={(projectId, status) => {
              const project = projects.find((item) => item.id === projectId);
              if (project !== undefined && project.status !== status) {
                transitionMutation.mutate({ projectId: project.id, status });
              }
            }}
          />
        ))}
      </Box>

      <ProjectDialog
        errorMessage={projectDialogOpen ? operationError : null}
        open={projectDialogOpen}
        pending={projectPending}
        project={editingProject}
        responsibles={responsibles}
        onClose={() => {
          setProjectDialogOpen(false);
          setEditingProject(null);
          setOperationError(null);
        }}
        onSubmit={(input) => {
          if (editingProject === null) {
            createMutation.mutate(input);
          } else {
            updateMutation.mutate({ projectId: editingProject.id, input });
          }
        }}
      />

      <ResponsibleDialog
        errorMessage={responsibleDialogOpen ? operationError : null}
        open={responsibleDialogOpen}
        pending={responsibleMutation.isPending}
        secretariats={secretariats}
        onClose={() => {
          setResponsibleDialogOpen(false);
          setOperationError(null);
        }}
        onSubmit={(input) => responsibleMutation.mutate(input)}
      />

      <DeleteProjectDialog
        pending={deleteMutation.isPending}
        project={deleteCandidate}
        onCancel={() => setDeleteCandidate(null)}
        onConfirm={(projectId) => deleteMutation.mutate(projectId)}
      />
    </Stack>
  );
}
