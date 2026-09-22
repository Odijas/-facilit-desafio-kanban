ALTER TABLE responsibles DROP CONSTRAINT uq_responsibles_email;
CREATE UNIQUE INDEX uq_responsibles_email_ci ON responsibles (lower(email));
