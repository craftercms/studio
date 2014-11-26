CREATE TABLE "cstudio_DEPENDENCY"
(
  id bigserial NOT NULL,
  site varchar(35) NOT NULL,
  source_path text NOT NULL,
  target_path text NOT NULL,
  type varchar(15) NOT NULL,
  CONSTRAINT "cstudio_DEPENDENCY_pkey" PRIMARY KEY (id)
);

CREATE INDEX cstudio_dependency_site_idx
ON "cstudio_DEPENDENCY" USING btree (site);

CREATE INDEX cstudio_dependency_sourcepath_idx
ON "cstudio_DEPENDENCY" USING btree (source_path);

