// ── Configuration ─────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

// ══════════════════════════════════════════════════════════
// AUTH HELPERS — Token storage and request headers
// ══════════════════════════════════════════════════════════

function getToken() {
  return localStorage.getItem('jwt_token');
}

function saveAuth(token, name, email) {
  localStorage.setItem('jwt_token', token);
  localStorage.setItem('user_name', name);
  localStorage.setItem('user_email', email);
}

function clearAuth() {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user_name');
  localStorage.removeItem('user_email');
}

function getUserName() {
  return localStorage.getItem('user_name');
}

// Every authenticated fetch call goes through this wrapper.
// It automatically attaches the JWT token to the Authorization header.
function authFetch(url, options = {}) {
  const token = getToken();
  return fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    }
  });
}

// Redirect to login if not authenticated
function requireAuth() {
  if (!getToken()) {
    window.location.href = 'login.html';
  }
}

// Logout — clear token and go to login page
function logout() {
  clearAuth();
  window.location.href = 'login.html';
}

// ── Theme Management ───────────────────────────────────────

function initTheme() {
  const saved = localStorage.getItem('theme') || 'dark';
  applyTheme(saved);
}

function applyTheme(theme) {
  const btn = document.getElementById('theme-toggle');
  if (theme === 'light') {
    document.body.classList.add('light');
    if (btn) btn.textContent = '☀️ Light';
  } else {
    document.body.classList.remove('light');
    if (btn) btn.textContent = '🌙 Dark';
  }
  localStorage.setItem('theme', theme);
}

function toggleTheme() {
  const isLight = document.body.classList.contains('light');
  applyTheme(isLight ? 'dark' : 'light');
}

// ── Utility: Toast Notification ───────────────────────────
function showToast(message, isError = false) {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.className = 'toast' + (isError ? ' error' : '');
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3000);
}

// ── Utility: Show Auth Error ───────────────────────────────
function showAuthError(message) {
  const el = document.getElementById('auth-error');
  if (el) {
    el.textContent = message;
    el.style.display = 'block';
  }
}

// ── Utility: Password Visibility Toggle ───────────────────
function togglePasswordVisibility(inputId, btn) {
  const input = document.getElementById(inputId);
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = '🙈';
  } else {
    input.type = 'password';
    btn.textContent = '👁';
  }
}

// ── Utility: Current Page Detection ───────────────────────
const isDashboard = window.location.pathname.endsWith('index.html')
  || window.location.pathname === '/'
  || window.location.pathname.endsWith('/');

const isProjectPage = window.location.pathname.endsWith('project.html');
const isLoginPage   = window.location.pathname.endsWith('login.html');
const isRegisterPage = window.location.pathname.endsWith('register.html');

// ── Utility: Get project ID from URL ──────────────────────
function getProjectIdFromURL() {
  const params = new URLSearchParams(window.location.search);
  return params.get('id');
}

// Initialise theme on every page
initTheme();
// ══════════════════════════════════════════════════════════
// DASHBOARD PAGE LOGIC
// ══════════════════════════════════════════════════════════

if (isDashboard) {
  requireAuth();
  document.addEventListener('DOMContentLoaded', () => {
    renderNavbarUser();
    loadDashboard();
  });
}
async function loadDashboard() {
  try {
    const response = await authFetch(`${API_BASE}/projects`);
    if (!response.ok) throw new Error('Failed to load projects');
    const projects = await response.json();
    renderProjectCards(projects);
  } catch (err) {
    showToast('Could not load projects. Is the backend running?', true);
  }
}

function renderProjectCards(projects) {
  const grid = document.getElementById('projects-grid');
  const emptyState = document.getElementById('empty-state');

  Array.from(grid.children).forEach(child => {
    if (child.id !== 'empty-state') child.remove();
  });

  if (projects.length === 0) {
    emptyState.style.display = 'block';
    return;
  }

  emptyState.style.display = 'none';

  projects.forEach(project => {
    const card = document.createElement('div');
    card.className = 'project-card';
    const progress = project.progress || 0;

    // ── Overdue milestone warning ──
    const overdueHtml = project.overdueMilestones > 0
      ? `<div class="overdue-badge">
           ⚠ ${project.overdueMilestones} overdue milestone${project.overdueMilestones > 1 ? 's' : ''}
         </div>`
      : '';

    // ── Task stats chips ──
    const taskHtml = project.totalTasks > 0
      ? `<div class="stat-chip highlight">
           ✓ ${project.completedTasks}/${project.totalTasks} tasks
         </div>`
      : `<div class="stat-chip">No tasks yet</div>`;

    const milestoneHtml = project.totalMilestones > 0
      ? `<div class="stat-chip">
           📅 ${project.totalMilestones} milestone${project.totalMilestones > 1 ? 's' : ''}
         </div>`
      : '';

    // ── Upcoming milestone ──
    let upcomingHtml = '';
    if (project.upcomingMilestoneTitle) {
      const days = project.upcomingMilestoneDaysLeft;
      let daysClass = 'milestone-days';
      let daysText = '';

      if (days < 0) {
        daysClass += ' overdue-text';
        daysText = `${Math.abs(days)}d overdue`;
      } else if (days <= 7) {
        daysClass += ' urgent';
        daysText = days === 0 ? 'Due today' : `${days}d left`;
      } else {
        daysText = `${days}d away`;
      }

      upcomingHtml = `
        <div class="card-milestone">
          <span>🎯 ${escapeHtml(project.upcomingMilestoneTitle)}</span>
          <span class="${daysClass}">${daysText}</span>
        </div>`;
    }

    card.innerHTML = `
      <div class="card-top-row">
        <h3>${escapeHtml(project.name)}</h3>
        <button class="btn-delete-project" title="Delete project"
          onclick="confirmDeleteProject(event, ${project.id},
          '${escapeHtml(project.name)}')">✕</button>
      </div>
      <p>${escapeHtml(project.description || 'No description provided.')}</p>
      ${overdueHtml}
      <div class="stage-badge stage-${project.currentStage}">
        ${project.currentStage}
      </div>
      <div class="card-stats">
        ${taskHtml}
        ${milestoneHtml}
      </div>
      <div class="progress-container">
        <div class="progress-label">
          <span>Progress</span>
          <span>${progress}%</span>
        </div>
        <div class="progress-bar-bg">
          <div class="progress-bar-fill" style="width:${progress}%"></div>
        </div>
      </div>
      ${upcomingHtml}
    `;

    card.addEventListener('click', (e) => {
      if (!e.target.classList.contains('btn-delete-project')) {
        window.location.href = `project.html?id=${project.id}`;
      }
    });

    grid.appendChild(card);
  });
}

// ── Create Project Modal ───────────────────────────────────

function openCreateModal() {
  document.getElementById('create-modal').classList.add('active');
  document.getElementById('project-name').focus();
}

function closeCreateModal() {
  document.getElementById('create-modal').classList.remove('active');
  document.getElementById('project-name').value = '';
  document.getElementById('project-description').value = '';
}

async function submitCreateProject() {
  const name = document.getElementById('project-name').value.trim();
  const description = document.getElementById('project-description').value.trim();

  if (!name) {
    showToast('Project name is required.', true);
    return;
  }

  try {
    const response = await authFetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, description })
    });

    if (!response.ok) throw new Error('Failed to create project');

    closeCreateModal();
    showToast('Project created successfully!');
    loadDashboard();

  } catch (err) {
    showToast('Failed to create project.', true);
  }
}

// Close modal when clicking outside
document.addEventListener('DOMContentLoaded', () => {
  const createOverlay = document.getElementById('create-modal');
  if (createOverlay) {
    createOverlay.addEventListener('click', (e) => {
      if (e.target === createOverlay) closeCreateModal();
    });
  }

  const deleteOverlay = document.getElementById('delete-modal');
  if (deleteOverlay) {
    deleteOverlay.addEventListener('click', (e) => {
      if (e.target === deleteOverlay) closeDeleteModal();
    });
  }
});

// ══════════════════════════════════════════════════════════
// PROJECT DETAIL PAGE LOGIC
// ══════════════════════════════════════════════════════════

if (isProjectPage) {
  document.addEventListener('DOMContentLoaded', loadProjectDetail);
}

async function loadProjectDetail() {
  const projectId = getProjectIdFromURL();
  if (!projectId) {
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await authFetch(`${API_BASE}/projects/${projectId}`);
    if (!response.ok) throw new Error('Project not found');
    const project = await response.json();
    renderProjectHeader(project);
    renderStages(project);
  } catch (err) {
    showToast('Could not load project.', true);
  }
}

function renderProjectHeader(project) {
  const header = document.getElementById('project-header');
  const progress = project.progress || 0;

  header.innerHTML = `
    <h2>${escapeHtml(project.name)}</h2>
    <p>${escapeHtml(project.description || 'No description provided.')}</p>

    <div class="project-meta">
      <div class="stage-badge stage-${project.currentStage}">
        ${project.currentStage}
      </div>

      <select class="stage-selector" onchange="updateProjectStage(${project.id}, this.value)">
        ${['IDEA','PLANNING','BUILDING','TESTING','LAUNCH','MAINTENANCE'].map(s => `
          <option value="${s}" ${s === project.currentStage ? 'selected' : ''}>${s}</option>
        `).join('')}
      </select>
    </div>

    <div class="progress-container" style="margin-top:16px;">
      <div class="progress-label">
        <span>Overall Progress</span>
        <span>${progress}%</span>
      </div>
      <div class="progress-bar-bg">
        <div class="progress-bar-fill" style="width: ${progress}%"></div>
      </div>
    </div>
  `;
}

function renderStages(project) {
  const stagesList = document.getElementById('stages-list');
  stagesList.innerHTML = '';

  if (!project.stages || project.stages.length === 0) {
    stagesList.innerHTML = '<p style="color:#555;">No stages found.</p>';
    return;
  }

  project.stages.forEach(stage => {
    const block = document.createElement('div');
    block.className = 'stage-block';
    block.id = `stage-block-${stage.id}`;

    const taskCount = stage.tasks ? stage.tasks.length : 0;
    const completedCount = stage.tasks
      ? stage.tasks.filter(t => t.completed).length
      : 0;

    block.innerHTML = `
      <div class="stage-block-header">
        <span class="stage-badge stage-${stage.name}" style="margin:0;">
          ${stage.name}
        </span>
        <span style="font-size:0.8rem; color:#888;">
          ${completedCount} / ${taskCount} tasks
        </span>
      </div>
      <div class="stage-block-body">
        <div id="tasks-${stage.id}">
          ${renderTasksList(stage.tasks, project.id)}
        </div>
        <div class="add-task-row">
          <input
            type="text"
            id="new-task-input-${stage.id}"
            placeholder="Add a task..."
            maxlength="200"
            onkeydown="if(event.key==='Enter') addTask(${project.id}, ${stage.id})"
          />
          <button class="btn btn-primary"
            onclick="addTask(${project.id}, ${stage.id})"
            style="padding: 8px 16px; font-size: 0.85rem;">
            Add
          </button>
        </div>

        <!-- Notes toggle button -->
        <button class="notes-toggle-btn"
          onclick="toggleNotesPanel(${project.id}, ${stage.id}, this)">
          📝 Notes
        </button>

        <!-- Notes panel (hidden by default) -->
        <div class="notes-panel" id="notes-panel-${stage.id}">
          <div id="notes-list-${stage.id}">
            <p class="notes-empty">Loading notes...</p>
          </div>
          <div class="note-input-area">
            <textarea
              id="note-textarea-${stage.id}"
              placeholder="Write a note, idea, or blocker for this stage..."
              maxlength="2000"
            ></textarea>
            <div class="note-input-actions">
              <button class="btn btn-secondary"
                onclick="clearNoteInput(${stage.id})">
                Clear
              </button>
              <button class="btn btn-primary"
                onclick="submitNote(${project.id}, ${stage.id})">
                Save Note
              </button>
            </div>
          </div>
        </div>

      </div>
    `;

    stagesList.appendChild(block);
  });
}

function renderTasksList(tasks, projectId) {
  if (!tasks || tasks.length === 0) {
    return '<p style="color:#555; font-size:0.85rem; padding: 8px 0;">No tasks yet.</p>';
  }

  return tasks.map(task => `
    <div class="task-item" id="task-${task.id}">
      <div class="task-left">
        <input
          type="checkbox"
          class="task-checkbox"
          ${task.completed ? 'checked' : ''}
          onchange="toggleTask(${projectId}, ${task.id}, this.checked)"
        />
        <span class="task-title ${task.completed ? 'done' : ''}">
          ${escapeHtml(task.title)}
        </span>
      </div>
      <div class="task-actions">
        <button class="btn btn-danger"
          onclick="deleteTask(${projectId}, ${task.id})">
          ✕
        </button>
      </div>
    </div>
  `).join('');
}

// ── Add Task ───────────────────────────────────────────────

async function addTask(projectId, stageId) {
  const input = document.getElementById(`new-task-input-${stageId}`);
  const title = input.value.trim();

  if (!title) {
    showToast('Task title cannot be empty.', true);
    return;
  }

  try {
    const response = await authFetch(`${API_BASE}/projects/${projectId}/tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, stageId })
    });

    if (!response.ok) throw new Error('Failed to add task');

    input.value = '';
    showToast('Task added!');
    loadProjectDetail();

  } catch (err) {
    showToast('Failed to add task.', true);
  }
}

// ── Toggle Task Complete / Incomplete ──────────────────────

async function toggleTask(projectId, taskId, isChecked) {
  const endpoint = isChecked ? 'complete' : 'incomplete';

  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/tasks/${taskId}/${endpoint}`,
      { method: 'PATCH' }
    );

    if (!response.ok) throw new Error('Failed to update task');

    showToast(isChecked ? 'Task completed!' : 'Task marked incomplete.');
    loadProjectDetail();

  } catch (err) {
    showToast('Failed to update task.', true);
  }
}

// ── Delete Task ────────────────────────────────────────────

async function deleteTask(projectId, taskId) {
  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/tasks/${taskId}`,
      { method: 'DELETE' }
    );

    if (!response.ok) throw new Error('Failed to delete task');

    showToast('Task deleted.');
    loadProjectDetail();

  } catch (err) {
    showToast('Failed to delete task.', true);
  }
}

// ── Update Project Stage ───────────────────────────────────

async function updateProjectStage(projectId, newStage) {
  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/stage?stage=${newStage}`,
      { method: 'PATCH' }
    );

    if (!response.ok) throw new Error('Failed to update stage');

    showToast(`Stage updated to ${newStage}`);
    loadProjectDetail();

  } catch (err) {
    showToast('Failed to update stage.', true);
  }
}

// ── Delete Project ─────────────────────────────────────────

// ── Delete Confirmation Modal ──────────────────────────────

let pendingDeleteProjectId = null;

function confirmDeleteProject(event, projectId, projectName) {
  event.stopPropagation();
  pendingDeleteProjectId = projectId;
  document.getElementById('delete-modal-message').textContent =
    `You are about to delete "${projectName}".`;
  document.getElementById('delete-modal').classList.add('active');
}

function closeDeleteModal() {
  document.getElementById('delete-modal').classList.remove('active');
  pendingDeleteProjectId = null;
}

async function executeDeleteProject() {
  if (!pendingDeleteProjectId) return;
  const id = pendingDeleteProjectId;
  closeDeleteModal();
  await deleteProject(id);
}

async function deleteProject(projectId) {
  try {
    const response = await authFetch(`${API_BASE}/projects/${projectId}`, {
      method: 'DELETE'
    });

    if (!response.ok) throw new Error('Failed to delete project');

    showToast('Project deleted.');
    loadDashboard();

  } catch (err) {
    showToast('Failed to delete project.', true);
  }
}

// ── Security: Escape HTML to prevent XSS ──────────────────

function escapeHtml(text) {
  if (!text) return '';
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// ══════════════════════════════════════════════════════════
// MILESTONE LOGIC
// ══════════════════════════════════════════════════════════

let currentProjectId = null;

// Override loadProjectDetail to also load milestones
const _originalLoadProjectDetail = loadProjectDetail;

async function loadProjectDetail() {
  const projectId = getProjectIdFromURL();
  if (!projectId) {
    window.location.href = 'index.html';
    return;
  }
  currentProjectId = projectId;

  try {
    const response = await authFetch(`${API_BASE}/projects/${projectId}`);
    if (!response.ok) throw new Error('Project not found');
    const project = await response.json();
    renderProjectHeader(project);
    renderStages(project);
    await loadMilestones(projectId);
  } catch (err) {
    showToast('Could not load project.', true);
  }
}

// ── Load and Render Milestones ─────────────────────────────

async function loadMilestones(projectId) {
  try {
    const response = await authFetch(`${API_BASE}/projects/${projectId}/milestones`);
    if (!response.ok) throw new Error('Failed to load milestones');
    const milestones = await response.json();
    renderMilestones(milestones, projectId);
  } catch (err) {
    showToast('Could not load milestones.', true);
  }
}

function renderMilestones(milestones, projectId) {
  const list = document.getElementById('milestone-list');
  if (!list) return;

  list.innerHTML = '';

  if (milestones.length === 0) {
    list.innerHTML = `
      <div class="milestone-empty">
        No milestones yet. Add your first milestone to track key deadlines.
      </div>
    `;
    return;
  }

  milestones.forEach(m => {
    const item = document.createElement('div');

    let statusClass = '';
    let tag = '';

    if (m.completed) {
      statusClass = 'completed';
      tag = '<span class="milestone-tag done">✓ Done</span>';
    } else if (m.overdue) {
      statusClass = 'overdue';
      tag = '<span class="milestone-tag overdue">⚠ Overdue</span>';
    } else if (m.daysUntilDue <= 7) {
      statusClass = '';
      tag = `<span class="milestone-tag soon">⏳ ${m.daysUntilDue}d left</span>`;
    } else {
      tag = `<span class="milestone-tag upcoming">📅 ${m.daysUntilDue}d away</span>`;
    }

    item.className = `milestone-item ${statusClass}`;
    item.innerHTML = `
      <div class="milestone-left">
        <input
          type="checkbox"
          class="milestone-checkbox"
          ${m.completed ? 'checked' : ''}
          onchange="toggleMilestone(${projectId}, ${m.id}, this.checked)"
        />
        <div class="milestone-info">
          <span class="milestone-title ${m.completed ? 'done' : ''}">
            ${escapeHtml(m.title)}
          </span>
          <div class="milestone-meta">
            <span class="milestone-date">
              Due: ${formatDate(m.dueDate)}
            </span>
            ${tag}
          </div>
        </div>
      </div>
      <div class="milestone-right">
        <button class="btn btn-danger"
          onclick="deleteMilestone(${projectId}, ${m.id})">
          ✕
        </button>
      </div>
    `;

    list.appendChild(item);
  });
}

// ── Toggle Add Milestone Form ──────────────────────────────

function toggleAddMilestoneForm() {
  const form = document.getElementById('add-milestone-form');
  form.classList.toggle('open');

  if (form.classList.contains('open')) {
    document.getElementById('milestone-title').focus();
    // Default due date to 2 weeks from today
    const twoWeeks = new Date();
    twoWeeks.setDate(twoWeeks.getDate() + 14);
    document.getElementById('milestone-date').value =
      twoWeeks.toISOString().split('T')[0];
  }
}

// ── Submit New Milestone ───────────────────────────────────

async function submitAddMilestone() {
  const title = document.getElementById('milestone-title').value.trim();
  const dueDate = document.getElementById('milestone-date').value;

  if (!title) {
    showToast('Milestone title is required.', true);
    return;
  }

  if (!dueDate) {
    showToast('Due date is required.', true);
    return;
  }

  try {
    const response = await authFetch(
      `${API_BASE}/projects/${currentProjectId}/milestones`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, dueDate })
      }
    );

    if (!response.ok) throw new Error('Failed to create milestone');

    document.getElementById('milestone-title').value = '';
    document.getElementById('add-milestone-form').classList.remove('open');
    showToast('Milestone added!');
    await loadMilestones(currentProjectId);

  } catch (err) {
    showToast('Failed to add milestone.', true);
  }
}

// ── Toggle Milestone Complete ──────────────────────────────

async function toggleMilestone(projectId, milestoneId, isChecked) {
  const endpoint = isChecked ? 'complete' : 'incomplete';

  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/milestones/${milestoneId}/${endpoint}`,
      { method: 'PATCH' }
    );

    if (!response.ok) throw new Error('Failed to update milestone');

    showToast(isChecked ? 'Milestone completed! 🎉' : 'Milestone reopened.');
    await loadMilestones(projectId);

  } catch (err) {
    showToast('Failed to update milestone.', true);
  }
}

// ── Delete Milestone ───────────────────────────────────────

async function deleteMilestone(projectId, milestoneId) {
  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/milestones/${milestoneId}`,
      { method: 'DELETE' }
    );

    if (!response.ok) throw new Error('Failed to delete milestone');

    showToast('Milestone deleted.');
    await loadMilestones(projectId);

  } catch (err) {
    showToast('Failed to delete milestone.', true);
  }
}

// ── Format Date for Display ────────────────────────────────

function formatDate(dateStr) {
  const date = new Date(dateStr + 'T00:00:00');
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

// ══════════════════════════════════════════════════════════
// NOTES LOGIC
// ══════════════════════════════════════════════════════════

// ── Toggle Notes Panel ─────────────────────────────────────

async function toggleNotesPanel(projectId, stageId, btn) {
  const panel = document.getElementById(`notes-panel-${stageId}`);
  const isOpen = panel.classList.contains('open');

  if (isOpen) {
    panel.classList.remove('open');
    btn.textContent = '📝 Notes';
  } else {
    panel.classList.add('open');
    btn.textContent = '📝 Hide Notes';
    await loadNotes(projectId, stageId);
  }
}

// ── Load and Render Notes ──────────────────────────────────

async function loadNotes(projectId, stageId) {
  const container = document.getElementById(`notes-list-${stageId}`);
  if (!container) return;

  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/stages/${stageId}/notes`
    );
    if (!response.ok) throw new Error('Failed to load notes');
    const notes = await response.json();
    renderNotes(notes, projectId, stageId, container);
  } catch (err) {
    container.innerHTML =
      '<p class="notes-empty">Could not load notes.</p>';
  }
}

function renderNotes(notes, projectId, stageId, container) {
  if (notes.length === 0) {
    container.innerHTML =
      '<p class="notes-empty">No notes yet. Write your first note below.</p>';
    return;
  }

  container.innerHTML = notes.map(note => `
    <div class="note-item" id="note-${note.id}">
      <div class="note-content">${escapeHtml(note.content)}</div>
      <div class="note-footer">
        <span class="note-timestamp">
          ${formatNoteDate(note.createdAt)}
        </span>
        <button class="btn-delete-note"
          onclick="deleteNote(${projectId}, ${note.id}, ${stageId})">
          🗑 Delete
        </button>
      </div>
    </div>
  `).join('');
}

// ── Submit Note ────────────────────────────────────────────

async function submitNote(projectId, stageId) {
  const textarea = document.getElementById(`note-textarea-${stageId}`);
  const content = textarea.value.trim();

  if (!content) {
    showToast('Note content cannot be empty.', true);
    return;
  }

  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/stages/${stageId}/notes`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content })
      }
    );

    if (!response.ok) throw new Error('Failed to save note');

    textarea.value = '';
    showToast('Note saved!');
    await loadNotes(projectId, stageId);

  } catch (err) {
    showToast('Failed to save note.', true);
  }
}

// ── Clear Note Input ───────────────────────────────────────

function clearNoteInput(stageId) {
  const textarea = document.getElementById(`note-textarea-${stageId}`);
  if (textarea) textarea.value = '';
}

// ── Delete Note ────────────────────────────────────────────

async function deleteNote(projectId, noteId, stageId) {
  try {
    const response = await authFetch(
      `${API_BASE}/projects/${projectId}/notes/${noteId}`,
      { method: 'DELETE' }
    );

    if (!response.ok) throw new Error('Failed to delete note');

    showToast('Note deleted.');
    await loadNotes(projectId, stageId);

  } catch (err) {
    showToast('Failed to delete note.', true);
  }
}

// ── Format Note Timestamp ──────────────────────────────────

function formatNoteDate(dateTimeStr) {
  const date = new Date(dateTimeStr);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// ══════════════════════════════════════════════════════════
// AUTH PAGE LOGIC — Login and Register
// ══════════════════════════════════════════════════════════

// ── Login Page ─────────────────────────────────────────────

if (isLoginPage) {
  // If already logged in, go straight to dashboard
  if (getToken()) window.location.href = 'index.html';
}

async function submitLogin() {
  const email    = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  if (!email || !password) {
    showAuthError('Please fill in all fields.');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    const data = await response.json();

    if (!response.ok) {
      showAuthError(data.message || 'Invalid email or password.');
      return;
    }

    saveAuth(data.token, data.name, data.email);
    window.location.href = 'index.html';

  } catch (err) {
    showAuthError('Could not connect to server. Is the backend running?');
  }
}

// ── Register Page ──────────────────────────────────────────

if (isRegisterPage) {
  if (getToken()) window.location.href = 'index.html';
}

async function submitRegister() {
  const name     = document.getElementById('name').value.trim();
  const email    = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  if (!name || !email || !password) {
    showAuthError('Please fill in all fields.');
    return;
  }

  if (password.length < 6) {
    showAuthError('Password must be at least 6 characters.');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password })
    });

    const data = await response.json();

    if (!response.ok) {
      showAuthError(data.message || 'Registration failed. Please try again.');
      return;
    }

    saveAuth(data.token, data.name, data.email);
    window.location.href = 'index.html';

  } catch (err) {
    showAuthError('Could not connect to server. Is the backend running?');
  }
}

// ══════════════════════════════════════════════════════════
// NAVBAR USER INFO — Show name and logout button
// ══════════════════════════════════════════════════════════

function renderNavbarUser() {
  const name = getUserName();
  if (!name) return;

  const navbarRight = document.querySelector('.navbar-right');
  if (!navbarRight) return;

  const userInfo = document.createElement('div');
  userInfo.className = 'navbar-user';
  userInfo.innerHTML = `
    <span>👤 ${escapeHtml(name)}</span>
    <button class="btn-logout" onclick="logout()">Sign Out</button>
  `;

  navbarRight.prepend(userInfo);
}