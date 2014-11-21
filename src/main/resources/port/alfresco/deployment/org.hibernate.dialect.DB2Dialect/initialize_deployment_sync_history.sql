CREATE  TABLE "cstudio_deploymentsynchistory" (
  "id"            	BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  "syncdate" TIMESTAMP NOT NULL ,
  "site" VARCHAR(50) NOT NULL ,
  "environment" VARCHAR(20) NOT NULL ,
  "path" VARCHAR(2048) NOT NULL ,
  "target" VARCHAR(50) NOT NULL ,
  "username" VARCHAR(25) NOT NULL ,
  "contenttypeclass" VARCHAR(25) NOT NULL ,
  CONSTRAINT "cstudio_deploymentsynchistory_pk" PRIMARY KEY("id")
);

CREATE INDEX "cs_depsynchist_site_idx" ON "cstudio_deploymentsynchistory" ("site");
CREATE INDEX "cs_depsynchist_env_idx" ON "cstudio_deploymentsynchistory" ("environment");
CREATE INDEX "cs_depsynchist_target_idx" ON "cstudio_deploymentsynchistory" ("target");
CREATE INDEX "cs_depsynchist_user_idx" ON "cstudio_deploymentsynchistory" ("username");
CREATE INDEX "cs_depsynchist_ctc_idx" ON "cstudio_deploymentsynchistory" ("contenttypeclass");
