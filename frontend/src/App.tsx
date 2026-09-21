import {
  Alert,
  Card,
  CardContent,
  Chip,
  Container,
  Stack,
  Typography,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getHealthStatus } from "./api/health";

export function App() {
  const healthQuery = useQuery({
    queryKey: ["health"],
    queryFn: getHealthStatus,
    retry: false,
  });

  return (
    <Container maxWidth="sm" sx={{ py: 8 }}>
      <Card variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Typography component="h1" variant="h4">
              Facilit Kanban
            </Typography>
            <Typography color="text.secondary">
              Fundação do desafio técnico
            </Typography>
            {healthQuery.isPending && <Chip label="Verificando backend" />}
            {healthQuery.isError && (
              <Alert severity="error">Backend indisponível</Alert>
            )}
            {healthQuery.isSuccess && (
              <Chip
                color="success"
                label={`Backend ${healthQuery.data.status}`}
              />
            )}
          </Stack>
        </CardContent>
      </Card>
    </Container>
  );
}
