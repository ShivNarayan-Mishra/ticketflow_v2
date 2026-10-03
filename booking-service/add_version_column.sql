-- Run this once against the ticketflow database (pgAdmin Query Tool or psql -f)
-- before starting the Spring Boot service. Needed because @Version requires
-- an actual column to track — Hibernate reads it, increments it on every
-- successful update, and uses it in the WHERE clause of every UPDATE it issues.

ALTER TABLE resources ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
