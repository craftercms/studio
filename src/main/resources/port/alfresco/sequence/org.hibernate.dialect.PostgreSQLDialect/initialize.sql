CREATE TABLE "cstudio_SEQUENCE"
(
  id bigserial NOT NULL,
  namespace varchar(20) NOT NULL,
  sql_generator bigint NOT NULL,
  step integer NOT NULL,
  CONSTRAINT "cstudio_SEQUENCE_pkey" PRIMARY KEY (id)
);

CREATE INDEX cstudio_sequence_namespace_idx
ON "cstudio_SEQUENCE" USING btree (namespace);

