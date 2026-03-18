INSERT INTO users (username, email, password_hash, created_at, updated_at)
SELECT 'seed.owner', 'seed.owner@test.local', 'seed-hash-owner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'seed.owner@test.local'
);

INSERT INTO users (username, email, password_hash, created_at, updated_at)
SELECT 'seed.member', 'seed.member@test.local', 'seed-hash-member', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'seed.member@test.local'
);

INSERT INTO users (username, email, password_hash, created_at, updated_at)
SELECT 'seed.viewer', 'seed.viewer@test.local', 'seed-hash-viewer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'seed.viewer@test.local'
);

INSERT INTO projects (name, description, start_date, owner_id, created_at, updated_at)
SELECT
    'Seed Project',
    'Preloaded test dataset for integration scenarios',
    DATE '2026-03-16',
    (SELECT id FROM users WHERE email = 'seed.owner@test.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE name = 'Seed Project'
);

INSERT INTO project_members (role, joined_at, project_id, user_id, created_at, updated_at)
SELECT
    'ADMIN',
    TIMESTAMP '2026-03-16 09:00:00',
    (SELECT id FROM projects WHERE name = 'Seed Project'),
    (SELECT id FROM users WHERE email = 'seed.owner@test.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_members
    WHERE project_id = (SELECT id FROM projects WHERE name = 'Seed Project')
      AND user_id = (SELECT id FROM users WHERE email = 'seed.owner@test.local')
);

INSERT INTO project_members (role, joined_at, project_id, user_id, created_at, updated_at)
SELECT
    'MEMBER',
    TIMESTAMP '2026-03-16 09:30:00',
    (SELECT id FROM projects WHERE name = 'Seed Project'),
    (SELECT id FROM users WHERE email = 'seed.member@test.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_members
    WHERE project_id = (SELECT id FROM projects WHERE name = 'Seed Project')
      AND user_id = (SELECT id FROM users WHERE email = 'seed.member@test.local')
);

INSERT INTO project_invitations (email, role, status, project_id, created_at, updated_at)
SELECT
    'future.member@test.local',
    'OBSERVER',
    'PENDING',
    (SELECT id FROM projects WHERE name = 'Seed Project'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_invitations
    WHERE email = 'future.member@test.local'
      AND project_id = (SELECT id FROM projects WHERE name = 'Seed Project')
);

INSERT INTO tasks (
    title,
    description,
    status,
    priority,
    due_date,
    end_date,
    project_id,
    created_by_id,
    assigned_to_id,
    created_at,
    updated_at
)
SELECT
    'Seed Task',
    'Task available immediately when the test context starts',
    'TODO',
    'HIGH',
    DATE '2026-03-20',
    NULL,
    (SELECT id FROM projects WHERE name = 'Seed Project'),
    (SELECT id FROM users WHERE email = 'seed.owner@test.local'),
    (SELECT id FROM users WHERE email = 'seed.member@test.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Seed Task'
);

INSERT INTO task_histories (
    action_type,
    field_name,
    old_value,
    new_value,
    task_id,
    changed_by_id,
    created_at,
    updated_at
)
SELECT
    'CREATED',
    'status',
    NULL,
    'TODO',
    (SELECT id FROM tasks WHERE title = 'Seed Task'),
    (SELECT id FROM users WHERE email = 'seed.owner@test.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM task_histories
    WHERE task_id = (SELECT id FROM tasks WHERE title = 'Seed Task')
      AND action_type = 'CREATED'
      AND field_name = 'status'
);

INSERT INTO notifications (
    type,
    status,
    message,
    sent_at,
    user_id,
    task_id,
    created_at,
    updated_at
)
SELECT
    'TASK_ASSIGNED',
    'SENT',
    'Seed task assigned to the member.',
    TIMESTAMP '2026-03-16 10:00:00',
    (SELECT id FROM users WHERE email = 'seed.member@test.local'),
    (SELECT id FROM tasks WHERE title = 'Seed Task'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM notifications
    WHERE message = 'Seed task assigned to the member.'
);
