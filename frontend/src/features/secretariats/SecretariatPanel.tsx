import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import {
  createSecretariat,
  deleteSecretariat,
  KanbanApiError,
  listSecretariats,
  type Secretariat,
  type SecretariatInput,
  updateSecretariat,
} from "../../api/kanban";
import {
  RESPONSIBLES_QUERY_KEY,
  SECRETARIATS_QUERY_KEY,
} from "../kanban/queryKeys";
import { DeleteSecretariatDialog } from "./DeleteSecretariatDialog";
import { SecretariatDialog } from "./SecretariatDialog";

type UpdateVariables = {
  secretariatId: string;
  input: SecretariatInput;
};

function operationErrorMessage(error: unknown): string {
  return error instanceof KanbanApiError
    ? error.message
    : "Não foi possível concluir a operação.";
}

export function SecretariatPanel() {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Secretariat | null>(null);
  const [deleteCandidate, setDeleteCandidate] = useState<Secretariat | null>(
    null,
  );
  const [operationError, setOperationError] = useState<string | null>(null);

  const secretariatsQuery = useQuery({
    queryKey: SECRETARIATS_QUERY_KEY,
    queryFn: () => listSecretariats(),
    retry: false,
  });

  const refresh = async (): Promise<void> => {
    await queryClient.invalidateQueries({ queryKey: SECRETARIATS_QUERY_KEY });
    await queryClient.invalidateQueries({ queryKey: RESPONSIBLES_QUERY_KEY });
  };

  const createMutation = useMutation({
    mutationFn: (input: SecretariatInput) => createSecretariat(input),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setDialogOpen(false);
      await refresh();
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ secretariatId, input }: UpdateVariables) =>
      updateSecretariat(secretariatId, input),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setDialogOpen(false);
      setEditing(null);
      await refresh();
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: (secretariatId: string) => deleteSecretariat(secretariatId),
    onMutate: () => setOperationError(null),
    onSuccess: async () => {
      setDeleteCandidate(null);
      await refresh();
    },
    onError: (error) => setOperationError(operationErrorMessage(error)),
  });

  if (secretariatsQuery.isPending) {
    return (
      <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
        <CircularProgress aria-label="Carregando secretarias" size={20} />
        <Typography color="text.secondary">Carregando secretarias</Typography>
      </Stack>
    );
  }

  if (secretariatsQuery.isError) {
    return (
      <Alert severity="error">Não foi possível carregar as secretarias.</Alert>
    );
  }

  const pending = createMutation.isPending || updateMutation.isPending;

  return (
    <Stack spacing={2}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{ justifyContent: "space-between" }}
      >
        <Box>
          <Typography component="h2" variant="h5" sx={{ fontWeight: 700 }}>
            Secretarias
          </Typography>
          <Typography color="text.secondary">
            Organize os responsáveis por secretaria.
          </Typography>
        </Box>
        <Button
          onClick={() => {
            setEditing(null);
            setOperationError(null);
            setDialogOpen(true);
          }}
          variant="outlined"
        >
          Nova secretaria
        </Button>
      </Stack>

      {operationError !== null && !dialogOpen && (
        <Alert onClose={() => setOperationError(null)} severity="error">
          {operationError}
        </Alert>
      )}

      {secretariatsQuery.data.length === 0 ? (
        <Alert severity="info">Nenhuma secretaria cadastrada.</Alert>
      ) : (
        <Box
          sx={{
            display: "grid",
            gap: 2,
            gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
          }}
        >
          {secretariatsQuery.data.map((secretariat) => (
            <Card key={secretariat.id} variant="outlined">
              <CardContent>
                <Typography sx={{ fontWeight: 600 }}>
                  {secretariat.name}
                </Typography>
              </CardContent>
              <CardActions>
                <Button
                  size="small"
                  onClick={() => {
                    setEditing(secretariat);
                    setOperationError(null);
                    setDialogOpen(true);
                  }}
                >
                  Editar
                </Button>
                <Button
                  color="error"
                  disabled={deleteMutation.isPending}
                  size="small"
                  onClick={() => {
                    setOperationError(null);
                    setDeleteCandidate(secretariat);
                  }}
                >
                  Excluir
                </Button>
              </CardActions>
            </Card>
          ))}
        </Box>
      )}

      <DeleteSecretariatDialog
        pending={deleteMutation.isPending}
        secretariat={deleteCandidate}
        onCancel={() => setDeleteCandidate(null)}
        onConfirm={(secretariatId) => deleteMutation.mutate(secretariatId)}
      />

      <SecretariatDialog
        errorMessage={dialogOpen ? operationError : null}
        open={dialogOpen}
        pending={pending}
        secretariat={editing}
        onClose={() => {
          setDialogOpen(false);
          setEditing(null);
          setOperationError(null);
        }}
        onSubmit={(input) => {
          if (editing === null) {
            createMutation.mutate(input);
          } else {
            updateMutation.mutate({ secretariatId: editing.id, input });
          }
        }}
      />
    </Stack>
  );
}
