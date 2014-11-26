CREATE TABLE "cstudio_objectstate" (
        "object_id" varchar(255) NOT NULL,
        "site" varchar(50) NOT NULL,
        "path" varchar(2000) NOT NULL,
        "state" varchar(255) NOT NULL,
        "system_processing" smallint NOT NULL,
        CONSTRAINT "cstudio_objectstate_pk" PRIMARY KEY("object_id")
);

CREATE INDEX "cstudio_objectstate_object_idx" ON "cstudio_objectstate" ("object_id");

