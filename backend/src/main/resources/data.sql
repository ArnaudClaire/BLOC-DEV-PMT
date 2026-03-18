UPDATE users
SET username = 'alice.admin',
    password_hash = '$2a$10$n12lXryy9uKNIyxGuW7m4OZbbtwpReq.uURHfrZBJ.58WAWsliX8e',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'alice.admin@pmt.local';

UPDATE users
SET username = 'bob.member',
    password_hash = '$2a$10$n12lXryy9uKNIyxGuW7m4OZbbtwpReq.uURHfrZBJ.58WAWsliX8e',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'bob.member@pmt.local';

UPDATE users
SET username = 'claire.observer',
    password_hash = '$2a$10$n12lXryy9uKNIyxGuW7m4OZbbtwpReq.uURHfrZBJ.58WAWsliX8e',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'claire.observer@pmt.local';

INSERT INTO users (username, email, password_hash, created_at, updated_at)
SELECT 'alice.admin', 'alice.admin@pmt.local', '$2a$10$n12lXryy9uKNIyxGuW7m4OZbbtwpReq.uURHfrZBJ.58WAWsliX8e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'alice.admin@pmt.local'
);

INSERT INTO users (username, email, password_hash, created_at, updated_at)
SELECT 'bob.member', 'bob.member@pmt.local', '$2a$10$n12lXryy9uKNIyxGuW7m4OZbbtwpReq.uURHfrZBJ.58WAWsliX8e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'bob.member@pmt.local'
);

INSERT INTO users (username, email, password_hash, created_at, updated_at)
SELECT 'claire.observer', 'claire.observer@pmt.local', '$2a$10$n12lXryy9uKNIyxGuW7m4OZbbtwpReq.uURHfrZBJ.58WAWsliX8e', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'claire.observer@pmt.local'
);

INSERT INTO projects (name, description, start_date, owner_id, created_at, updated_at)
SELECT
    'PMT Launch',
    'Seed project for onboarding and API demos',
    DATE '2026-03-01',
    (SELECT id FROM users WHERE email = 'alice.admin@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE name = 'PMT Launch'
);

INSERT INTO projects (name, description, start_date, owner_id, created_at, updated_at)
SELECT
    'Mobile Refresh',
    'Second seeded project with active collaboration data',
    DATE '2026-03-05',
    (SELECT id FROM users WHERE email = 'bob.member@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE name = 'Mobile Refresh'
);

INSERT INTO project_members (role, joined_at, project_id, user_id, created_at, updated_at)
SELECT
    'ADMIN',
    TIMESTAMP '2026-03-01 09:00:00',
    (SELECT id FROM projects WHERE name = 'PMT Launch'),
    (SELECT id FROM users WHERE email = 'alice.admin@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_members
    WHERE project_id = (SELECT id FROM projects WHERE name = 'PMT Launch')
      AND user_id = (SELECT id FROM users WHERE email = 'alice.admin@pmt.local')
);

INSERT INTO project_members (role, joined_at, project_id, user_id, created_at, updated_at)
SELECT
    'MEMBER',
    TIMESTAMP '2026-03-02 10:30:00',
    (SELECT id FROM projects WHERE name = 'PMT Launch'),
    (SELECT id FROM users WHERE email = 'bob.member@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_members
    WHERE project_id = (SELECT id FROM projects WHERE name = 'PMT Launch')
      AND user_id = (SELECT id FROM users WHERE email = 'bob.member@pmt.local')
);

INSERT INTO project_members (role, joined_at, project_id, user_id, created_at, updated_at)
SELECT
    'OBSERVER',
    TIMESTAMP '2026-03-06 08:45:00',
    (SELECT id FROM projects WHERE name = 'Mobile Refresh'),
    (SELECT id FROM users WHERE email = 'claire.observer@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_members
    WHERE project_id = (SELECT id FROM projects WHERE name = 'Mobile Refresh')
      AND user_id = (SELECT id FROM users WHERE email = 'claire.observer@pmt.local')
);

INSERT INTO project_invitations (email, role, status, project_id, created_at, updated_at)
SELECT
    'new.joiner@pmt.local',
    'MEMBER',
    'PENDING',
    (SELECT id FROM projects WHERE name = 'PMT Launch'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM project_invitations
    WHERE email = 'new.joiner@pmt.local'
      AND project_id = (SELECT id FROM projects WHERE name = 'PMT Launch')
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
    'Prepare kickoff',
    'Create the project kickoff checklist and share it with the team',
    'IN_PROGRESS',
    'HIGH',
    DATE '2026-03-20',
    NULL,
    (SELECT id FROM projects WHERE name = 'PMT Launch'),
    (SELECT id FROM users WHERE email = 'alice.admin@pmt.local'),
    (SELECT id FROM users WHERE email = 'bob.member@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Prepare kickoff'
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
    'Review mobile backlog',
    'Prioritize tickets for the mobile refresh sprint',
    'TODO',
    'MEDIUM',
    DATE '2026-03-25',
    NULL,
    (SELECT id FROM projects WHERE name = 'Mobile Refresh'),
    (SELECT id FROM users WHERE email = 'bob.member@pmt.local'),
    (SELECT id FROM users WHERE email = 'claire.observer@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Review mobile backlog'
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
    'STATUS_CHANGED',
    'status',
    'TODO',
    'IN_PROGRESS',
    (SELECT id FROM tasks WHERE title = 'Prepare kickoff'),
    (SELECT id FROM users WHERE email = 'alice.admin@pmt.local'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM task_histories
    WHERE task_id = (SELECT id FROM tasks WHERE title = 'Prepare kickoff')
      AND action_type = 'STATUS_CHANGED'
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
    'Kickoff preparation has been assigned to you.',
    TIMESTAMP '2026-03-10 14:15:00',
    (SELECT id FROM users WHERE email = 'bob.member@pmt.local'),
    (SELECT id FROM tasks WHERE title = 'Prepare kickoff'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM notifications
    WHERE message = 'Kickoff preparation has been assigned to you.'
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
    'INVITATION_SENT',
    'READ',
    'You have been invited to join PMT Launch.',
    TIMESTAMP '2026-03-11 09:00:00',
    (SELECT id FROM users WHERE email = 'claire.observer@pmt.local'),
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM notifications
    WHERE message = 'You have been invited to join PMT Launch.'
);
