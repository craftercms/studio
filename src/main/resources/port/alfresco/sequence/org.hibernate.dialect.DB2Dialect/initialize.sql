CREATE TABLE "cstudio_SEQUENCE" (
  "id"            	BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  "namespace" varchar(20) NOT NULL,
  "sql_generator" bigint NOT NULL,
  "step" int NOT NULL,
  CONSTRAINT "cstudio_SEQUENCE_pk" PRIMARY KEY("id")
);

CREATE INDEX "cstudio_sequence_namespace_idx" ON "cstudio_SEQUENCE" ("namespace");