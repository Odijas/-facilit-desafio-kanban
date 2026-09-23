import { CircularProgress, Container, Stack, Typography } from "@mui/material";

type LoadingStateProps = {
  label: string;
};

export function LoadingState({ label }: LoadingStateProps) {
  return (
    <Container maxWidth="sm" sx={{ py: 10 }}>
      <Stack spacing={2} sx={{ alignItems: "center" }}>
        <CircularProgress aria-label={label} />
        <Typography color="text.secondary">{label}</Typography>
      </Stack>
    </Container>
  );
}
