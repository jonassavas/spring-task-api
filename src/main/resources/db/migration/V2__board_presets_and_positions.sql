ALTER TABLE task_boards ADD COLUMN IF NOT EXISTS position INTEGER;
ALTER TABLE task_groups ADD COLUMN IF NOT EXISTS position INTEGER;
ALTER TABLE task_groups ADD COLUMN IF NOT EXISTS color VARCHAR(7);
ALTER TABLE task_groups ADD COLUMN IF NOT EXISTS strike_through_tasks BOOLEAN DEFAULT FALSE;
ALTER TABLE task_groups ADD COLUMN IF NOT EXISTS sort_mode VARCHAR(20) DEFAULT 'MANUAL';
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS position INTEGER;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS due_date DATE;

-- A previous issue-26 build may already have an immediate task-position constraint.
-- Remove position constraints before assigning dense ranks, then recreate named indexes.
DO $$
DECLARE constraint_row RECORD;
BEGIN
    FOR constraint_row IN
        SELECT table_name, constraint_name FROM (
            SELECT 'task_boards' AS table_name, conname AS constraint_name,
                   pg_get_constraintdef(oid) AS definition
            FROM pg_constraint WHERE conrelid = 'task_boards'::regclass AND contype = 'u'
            UNION ALL
            SELECT 'task_groups', conname, pg_get_constraintdef(oid)
            FROM pg_constraint WHERE conrelid = 'task_groups'::regclass AND contype = 'u'
            UNION ALL
            SELECT 'tasks', conname, pg_get_constraintdef(oid)
            FROM pg_constraint WHERE conrelid = 'tasks'::regclass AND contype = 'u'
        ) existing_constraints
        WHERE definition LIKE '%position%'
    LOOP
        EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I',
                       constraint_row.table_name, constraint_row.constraint_name);
    END LOOP;
END $$;

WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY owner_id ORDER BY position NULLS LAST, id) - 1 AS new_position
    FROM task_boards
)
UPDATE task_boards b SET position = r.new_position FROM ranked r WHERE b.id = r.id;

WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY task_board_id ORDER BY position NULLS LAST, id) - 1 AS new_position
    FROM task_groups
)
UPDATE task_groups g SET position = r.new_position FROM ranked r WHERE g.id = r.id;

WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY group_id ORDER BY position NULLS LAST, id) - 1 AS new_position
    FROM tasks
)
UPDATE tasks t SET position = r.new_position FROM ranked r WHERE t.id = r.id;

UPDATE task_groups SET strike_through_tasks = FALSE WHERE strike_through_tasks IS NULL;
UPDATE task_groups SET sort_mode = 'MANUAL' WHERE sort_mode IS NULL;
ALTER TABLE task_boards ALTER COLUMN position SET NOT NULL;
ALTER TABLE task_groups ALTER COLUMN position SET NOT NULL;
ALTER TABLE task_groups ALTER COLUMN strike_through_tasks SET NOT NULL;
ALTER TABLE task_groups ALTER COLUMN sort_mode SET NOT NULL;
ALTER TABLE tasks ALTER COLUMN position SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_task_boards_owner_position
    ON task_boards(owner_id, position);
CREATE UNIQUE INDEX IF NOT EXISTS uq_task_groups_board_position
    ON task_groups(task_board_id, position);
CREATE UNIQUE INDEX IF NOT EXISTS uq_tasks_group_position
    ON tasks(group_id, position);
