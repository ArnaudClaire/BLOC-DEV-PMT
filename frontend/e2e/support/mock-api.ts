import { Page, Route } from '@playwright/test';

const SESSION_KEY = 'mpmt.current-user';

type MemberRole = 'ADMIN' | 'MEMBER' | 'OBSERVER';
type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED' | 'CANCELED';

export type MockUser = {
  id: number;
  username: string;
  email: string;
  password: string;
};

type MockProject = {
  id: number;
  name: string;
  description: string;
  startDate: string;
  ownerId: number;
};

type MockTask = {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
  endDate: string;
  projectId: number;
  createdById: number;
  assignedToId?: number;
};

type MockProjectMember = {
  id: number;
  role: MemberRole;
  joinedAt: string;
  projectId: number;
  userId: number;
};

type MockProjectInvitation = {
  id: number;
  email: string;
  token: string;
  role: MemberRole;
  status: InvitationStatus;
  createdAt: string;
  expiresAt: string;
  acceptedAt?: string;
  projectId: number;
  invitedById: number;
  acceptedById?: number;
};

type MockNotification = {
  id: number;
  type: 'TASK_ASSIGNED' | 'INVITATION_SENT';
  status: 'SENT' | 'READ';
  message: string;
  sentAt: string;
  userId: number;
  taskId?: number;
};

type MockTaskHistory = {
  id: number;
  actionType: 'CREATED' | 'UPDATED' | 'ASSIGNED' | 'STATUS_CHANGED' | 'COMPLETED';
  fieldName: string;
  oldValue: string | null;
  newValue: string | null;
  createdAt: string;
  taskId: number;
  changedById: number;
};

export type MockState = {
  users: MockUser[];
  projects: MockProject[];
  tasks: MockTask[];
  projectMembers: MockProjectMember[];
  projectInvitations: MockProjectInvitation[];
  notifications: MockNotification[];
  taskHistories: MockTaskHistory[];
  nextIds: {
    user: number;
    project: number;
    task: number;
    projectInvitation: number;
    notification: number;
    taskHistory: number;
  };
};

export function createMockState(): MockState {
  return {
    users: [
      { id: 1, username: 'alice.admin', email: 'alice@example.com', password: 'secret123' },
      { id: 2, username: 'bob.member', email: 'bob@example.com', password: 'secret123' },
      { id: 3, username: 'claire.observer', email: 'claire@example.com', password: 'secret123' },
      { id: 4, username: 'david.outsider', email: 'david@example.com', password: 'secret123' },
    ],
    projects: [
      {
        id: 1,
        name: 'PMT Launch',
        description: 'Projet principal de collaboration produit.',
        startDate: '2026-03-01',
        ownerId: 1,
      },
      {
        id: 2,
        name: 'Mobile Refresh',
        description: 'Refonte du tableau de bord mobile.',
        startDate: '2026-03-05',
        ownerId: 1,
      },
    ],
    tasks: [
      {
        id: 10,
        title: 'Configurer auth',
        description: 'Brancher la session locale et les roles.',
        status: 'TODO',
        priority: 'HIGH',
        dueDate: '2026-03-20',
        endDate: '2026-03-20',
        projectId: 1,
        createdById: 1,
        assignedToId: 2,
      },
      {
        id: 11,
        title: 'Revoir dashboard mobile',
        description: 'Verifier la consultation en lecture seule.',
        status: 'DONE',
        priority: 'LOW',
        dueDate: '2026-03-18',
        endDate: '2026-03-19',
        projectId: 2,
        createdById: 1,
        assignedToId: 1,
      },
    ],
    projectMembers: [
      { id: 101, role: 'MEMBER', joinedAt: '2026-03-02T09:00:00.000Z', projectId: 1, userId: 2 },
      { id: 102, role: 'OBSERVER', joinedAt: '2026-03-03T09:00:00.000Z', projectId: 1, userId: 3 },
    ],
    projectInvitations: [],
    notifications: [
      {
        id: 301,
        type: 'TASK_ASSIGNED',
        status: 'SENT',
        message: 'La tache "Configurer auth" vous a ete assignee.',
        sentAt: '2026-03-19T10:00:00.000Z',
        userId: 2,
        taskId: 10,
      },
      {
        id: 302,
        type: 'TASK_ASSIGNED',
        status: 'READ',
        message: 'La tache "Revoir dashboard mobile" vous a ete assignee.',
        sentAt: '2026-03-19T08:30:00.000Z',
        userId: 1,
        taskId: 11,
      },
    ],
    taskHistories: [
      {
        id: 401,
        actionType: 'STATUS_CHANGED',
        fieldName: 'status',
        oldValue: 'TODO',
        newValue: 'IN_PROGRESS',
        createdAt: '2026-03-19T11:00:00.000Z',
        taskId: 10,
        changedById: 1,
      },
      {
        id: 402,
        actionType: 'COMPLETED',
        fieldName: 'status',
        oldValue: 'IN_PROGRESS',
        newValue: 'DONE',
        createdAt: '2026-03-18T11:00:00.000Z',
        taskId: 11,
        changedById: 1,
      },
    ],
    nextIds: {
      user: 10,
      project: 10,
      task: 20,
      projectInvitation: 30,
      notification: 40,
      taskHistory: 50,
    },
  };
}

export async function installMockApi(page: Page, state: MockState): Promise<void> {
  await page.route('**/api/**', async (route, request) => {
    const url = new URL(request.url());
    const { pathname } = url;
    const method = request.method();

    if (pathname === '/api/users' && method === 'GET') {
      return fulfillJson(route, 200, state.users.map(serializeUser));
    }

    if (pathname === '/api/users' && method === 'POST') {
      const payload = request.postDataJSON() as { username: string; email: string; password: string };
      const user: MockUser = {
        id: state.nextIds.user++,
        username: payload.username,
        email: payload.email,
        password: payload.password,
      };
      state.users.push(user);
      return fulfillJson(route, 201, serializeUser(user));
    }

    if (pathname === '/api/auth/login' && method === 'POST') {
      const payload = request.postDataJSON() as { email: string; password: string };
      const user = state.users.find(
        (candidate) => candidate.email.toLowerCase() === payload.email.toLowerCase() && candidate.password === payload.password,
      );

      if (!user) {
        return fulfillJson(route, 401, { message: 'Identifiant ou mot de passe incorrect.' });
      }

      return fulfillJson(route, 200, serializeUser(user));
    }

    if (pathname === '/api/projects' && method === 'GET') {
      return fulfillJson(route, 200, state.projects.map(serializeProject));
    }

    if (pathname === '/api/projects' && method === 'POST') {
      const payload = request.postDataJSON() as {
        name: string;
        description: string;
        startDate: string;
        ownerId: number;
      };
      const project: MockProject = {
        id: state.nextIds.project++,
        name: payload.name,
        description: payload.description,
        startDate: payload.startDate,
        ownerId: payload.ownerId,
      };
      state.projects.push(project);
      return fulfillJson(route, 201, project.id);
    }

    if (pathname === '/api/tasks' && method === 'GET') {
      return fulfillJson(route, 200, state.tasks.map(serializeTask));
    }

    if (pathname === '/api/tasks' && method === 'POST') {
      const payload = request.postDataJSON() as Omit<MockTask, 'id'>;
      const task: MockTask = {
        id: state.nextIds.task++,
        ...payload,
      };
      state.tasks.push(task);
      return fulfillJson(route, 201, task.id);
    }

    if (pathname.startsWith('/api/tasks/') && method === 'PUT') {
      const taskId = Number(pathname.split('/').pop());
      const payload = request.postDataJSON() as Partial<MockTask>;
      const task = state.tasks.find((candidate) => candidate.id === taskId);

      if (!task) {
        return fulfillJson(route, 404, { message: 'Task not found.' });
      }

      Object.assign(task, payload);
      return fulfillJson(route, 200, {});
    }

    if (pathname === '/api/project-members' && method === 'GET') {
      return fulfillJson(route, 200, state.projectMembers.map(serializeProjectMember));
    }

    if (pathname.startsWith('/api/project-members/') && method === 'PUT') {
      const memberId = Number(pathname.split('/').pop());
      const payload = request.postDataJSON() as { role: MemberRole };
      const member = state.projectMembers.find((candidate) => candidate.id === memberId);

      if (!member) {
        return fulfillJson(route, 404, { message: 'Project member not found.' });
      }

      member.role = payload.role;
      return fulfillJson(route, 200, {});
    }

    if (pathname === '/api/project-invitations' && method === 'GET') {
      return fulfillJson(route, 200, state.projectInvitations.map(serializeProjectInvitation));
    }

    if (pathname === '/api/project-invitations' && method === 'POST') {
      const payload = request.postDataJSON() as {
        email: string;
        role: MemberRole;
        projectId: number;
        invitedById: number;
      };
      const invitation: MockProjectInvitation = {
        id: state.nextIds.projectInvitation++,
        email: payload.email,
        token: `invite-${state.nextIds.projectInvitation}`,
        role: payload.role,
        status: 'PENDING',
        createdAt: '2026-03-20T09:00:00.000Z',
        expiresAt: '2026-03-27T09:00:00.000Z',
        projectId: payload.projectId,
        invitedById: payload.invitedById,
      };
      state.projectInvitations.push(invitation);
      return fulfillJson(route, 201, invitation.id);
    }

    if (pathname.includes('/api/project-invitations/token/') && method === 'GET') {
      const token = pathname.split('/').pop()!;
      const invitation = state.projectInvitations.find((candidate) => candidate.token === token);

      if (!invitation) {
        return fulfillJson(route, 404, { message: 'Invitation introuvable.' });
      }

      const project = state.projects.find((candidate) => candidate.id === invitation.projectId);
      return fulfillJson(route, 200, {
        id: invitation.id,
        email: invitation.email,
        role: invitation.role,
        status: invitation.status,
        projectId: invitation.projectId,
        projectName: project?.name ?? 'Projet inconnu',
        expiresAt: invitation.expiresAt,
      });
    }

    if (pathname.includes('/api/project-invitations/token/') && pathname.endsWith('/accept') && method === 'POST') {
      const segments = pathname.split('/');
      const token = segments[segments.length - 2];
      const payload = request.postDataJSON() as { userId: number };
      const invitation = state.projectInvitations.find((candidate) => candidate.token === token);

      if (!invitation) {
        return fulfillJson(route, 404, { message: 'Invitation introuvable.' });
      }

      invitation.status = 'ACCEPTED';
      invitation.acceptedAt = '2026-03-20T10:00:00.000Z';
      invitation.acceptedById = payload.userId;

      const existingMember = state.projectMembers.find(
        (candidate) => candidate.projectId === invitation.projectId && candidate.userId === payload.userId,
      );

      if (!existingMember) {
        state.projectMembers.push({
          id: Math.max(0, ...state.projectMembers.map((candidate) => candidate.id)) + 1,
          role: invitation.role,
          joinedAt: invitation.acceptedAt,
          projectId: invitation.projectId,
          userId: payload.userId,
        });
      }

      return fulfillJson(route, 200, { projectId: invitation.projectId });
    }

    if (pathname.match(/^\/api\/project-invitations\/\d+\/cancel$/) && method === 'POST') {
      const invitationId = Number(pathname.split('/')[3]);
      const invitation = state.projectInvitations.find((candidate) => candidate.id === invitationId);
      if (invitation) {
        invitation.status = 'CANCELED';
      }
      return fulfillJson(route, 200, {});
    }

    if (pathname.match(/^\/api\/project-invitations\/\d+\/resend$/) && method === 'POST') {
      const invitationId = Number(pathname.split('/')[3]);
      const invitation = state.projectInvitations.find((candidate) => candidate.id === invitationId);
      if (invitation) {
        invitation.status = 'PENDING';
        invitation.expiresAt = '2026-03-30T09:00:00.000Z';
      }
      return fulfillJson(route, 200, {});
    }

    if (pathname === '/api/notifications' && method === 'GET') {
      return fulfillJson(route, 200, state.notifications.map(serializeNotification));
    }

    if (pathname === '/api/notifications' && method === 'POST') {
      const payload = request.postDataJSON() as Omit<MockNotification, 'id'>;
      const notification: MockNotification = {
        id: state.nextIds.notification++,
        ...payload,
      };
      state.notifications.push(notification);
      return fulfillJson(route, 201, notification.id);
    }

    if (pathname === '/api/task-histories' && method === 'GET') {
      return fulfillJson(route, 200, state.taskHistories.map(serializeTaskHistory));
    }

    if (pathname === '/api/task-histories' && method === 'POST') {
      const payload = request.postDataJSON() as Omit<MockTaskHistory, 'id' | 'createdAt'>;
      const history: MockTaskHistory = {
        id: state.nextIds.taskHistory++,
        createdAt: '2026-03-20T10:15:00.000Z',
        ...payload,
      };
      state.taskHistories.push(history);
      return fulfillJson(route, 201, history.id);
    }

    return fulfillJson(route, 404, { message: `Unhandled route in test: ${pathname}` });
  });
}

export async function setSession(page: Page, user: Pick<MockUser, 'id' | 'username' | 'email'>): Promise<void> {
  await page.addInitScript(
    ([storageKey, storageValue]) => {
      window.localStorage.setItem(storageKey, JSON.stringify(storageValue));
    },
    [SESSION_KEY, user] as const,
  );
}

async function fulfillJson(route: Route, status: number, body: unknown): Promise<void> {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body),
  });
}

function serializeUser(user: MockUser) {
  return {
    id: user.id,
    username: user.username,
    email: user.email,
  };
}

function serializeProject(project: MockProject) {
  return {
    id: project.id,
    name: project.name,
    description: project.description,
    startDate: project.startDate,
    owner: { id: project.ownerId },
  };
}

function serializeTask(task: MockTask) {
  return {
    id: task.id,
    title: task.title,
    description: task.description,
    status: task.status,
    priority: task.priority,
    dueDate: task.dueDate,
    endDate: task.endDate,
    project: { id: task.projectId },
    createdBy: { id: task.createdById },
    assignedTo: task.assignedToId ? { id: task.assignedToId } : null,
  };
}

function serializeProjectMember(member: MockProjectMember) {
  return {
    id: member.id,
    role: member.role,
    joinedAt: member.joinedAt,
    project: { id: member.projectId },
    user: { id: member.userId },
  };
}

function serializeProjectInvitation(invitation: MockProjectInvitation) {
  return {
    id: invitation.id,
    email: invitation.email,
    token: invitation.token,
    role: invitation.role,
    status: invitation.status,
    createdAt: invitation.createdAt,
    expiresAt: invitation.expiresAt,
    acceptedAt: invitation.acceptedAt,
    project: { id: invitation.projectId },
    invitedBy: { id: invitation.invitedById },
    acceptedBy: invitation.acceptedById ? { id: invitation.acceptedById } : null,
  };
}

function serializeNotification(notification: MockNotification) {
  return {
    id: notification.id,
    type: notification.type,
    status: notification.status,
    message: notification.message,
    sentAt: notification.sentAt,
    user: { id: notification.userId },
    task: notification.taskId ? { id: notification.taskId } : null,
  };
}

function serializeTaskHistory(history: MockTaskHistory) {
  return {
    id: history.id,
    actionType: history.actionType,
    fieldName: history.fieldName,
    oldValue: history.oldValue,
    newValue: history.newValue,
    createdAt: history.createdAt,
    task: { id: history.taskId },
    changedBy: { id: history.changedById },
  };
}
