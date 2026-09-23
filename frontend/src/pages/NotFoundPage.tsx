import { Button, Container, Stack, Typography } from "@mui/material";
import { navigate } from "../app/navigation";

export function NotFoundPage() {
  return (
    <Container maxWidth="sm" sx={{ py: 10 }}>
      <Stack spacing={2}>
        <Typography component="h1" variant="h4">
          Página não encontrada
        </Typography>
        <Button onClick={() => navigate("/", true)} variant="contained">
          Voltar ao início
        </Button>
      </Stack>
    </Container>
  );
}
