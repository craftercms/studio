CREATE TABLE "cstudio_DEPENDENCY" (
  "id"            	BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  "site" varchar(35) NOT NULL,
  "source_path" VARCHAR(2048) NOT NULL,
  "target_path" VARCHAR(2048) NOT NULL,
  "type" varchar(15) NOT NULL,
  CONSTRAINT "cstudio_dependency_pk" PRIMARY KEY("id")
);

CREATE INDEX "cstudio_dependency_site_idx" ON "cstudio_DEPENDENCY" ("site");
