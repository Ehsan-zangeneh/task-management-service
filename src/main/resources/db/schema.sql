DROP TABLE IF EXISTS task;
CREATE TABLE task (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          title TEXT,
          description TEXT,
          creation_date TIMESTAMPTZ,
          modification_date TIMESTAMPTZ,
          status TEXT,
          owner_id VARCHAR(36),
          assignee_id VARCHAR(36)
);
