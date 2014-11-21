CREATE  TABLE "cstudio_copytoenvironment" (
  "id"            	BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  "site" VARCHAR(50) NOT NULL ,
  "environment" VARCHAR(20) NOT NULL ,
  "path" VARCHAR(2048) NOT NULL ,
  "oldpath" VARCHAR(2048) NULL ,
  "username" VARCHAR(25) NULL ,
  "scheduleddate" TIMESTAMP NOT NULL ,
  "state" VARCHAR(50) NOT NULL ,
  "action" VARCHAR(20) NOT NULL ,
  "contenttypeclass" VARCHAR(20) NULL,
  CONSTRAINT "cstudio_copytoenvironment_pk" PRIMARY KEY("id")
);

CREATE INDEX "cstudio_cte_site_idx" ON "cstudio_copytoenvironment" ("site");
CREATE INDEX "cstudio_cte_environment_idx" ON "cstudio_copytoenvironment" ("environment");
CREATE INDEX "cstudio_cte_state_idx" ON "cstudio_copytoenvironment" ("state");

CREATE  TABLE "cstudio_publishtotarget" (
  "id"            	BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  "site" VARCHAR(50) NOT NULL ,
  "environment" VARCHAR(20) NOT NULL ,
  "path" VARCHAR(2048) NOT NULL ,
  "oldpath" VARCHAR(2048) NULL ,
  "username" VARCHAR(25) NOT NULL ,
  "version" BIGINT NOT NULL ,
  "action" VARCHAR(20) NOT NULL ,
  "contenttypeclass" VARCHAR(20) NULL,
  CONSTRAINT "cstudio_publishtotarget_pk" PRIMARY KEY("id")
);

CREATE INDEX "cstudio_ptt_site_idx" ON "cstudio_publishtotarget" ("site");
CREATE INDEX "cstudio_ptt_environment_idx" ON "cstudio_publishtotarget" ("environment");