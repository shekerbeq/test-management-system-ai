const API = 'http://localhost:8080/api';

function toast(msg, ok = true) {
  const t = document.createElement('div');
  t.style.cssText = `position:fixed;bottom:20px;right:20px;padding:14px 24px;border-radius:12px;color:white;background:${ok?'#22c55e':'#ef4444'};z-index:9999`;
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 3000);
}

async function api(url, method = 'GET', body = null) {
  const token = localStorage.getItem('jwt');
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(API + url, { method, headers, body: body ? JSON.stringify(body) : null });
  if (res.status === 401 || res.status === 403) {
    localStorage.clear();
    location.href = '/login.html';
    throw new Error('Unauthorized');
  }
  if (!res.ok) {
    const txt = await res.text();
    throw new Error(txt || 'Server error');
  }
  const txt = await res.text();
  try { return JSON.parse(txt); } catch { return txt; }
}

// Хедер
document.addEventListener('DOMContentLoaded', () => {
  const header = document.getElementById('global-header');
  if (!header) return;
  const user = JSON.parse(localStorage.getItem('user'));
  header.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;padding:14px 30px;background:white;box-shadow:0 2px 8px rgba(0,0,0,0.04)">
      <a href="landing.html" style="font-weight:800;font-size:20px;color:var(--primary);text-decoration:none">Eduvibe</a>
      <div style="display:flex;gap:20px;align-items:center">
        ${user ? `
          ${user.role === 'TEACHER' ? '<a href="tests.html">Мои тесты</a> <a href="create-test.html">Создать тест</a>' : ''}
          ${user.role === 'STUDENT' ? '<a href="dashboard.html">Тесты</a> <a href="dashboard.html?tab=results">Результаты</a>' : ''}
          <span>${user.fullName || user.email}</span>
          <button onclick="localStorage.clear();location.href='/landing.html'" class="btn-outline" style="padding:6px 14px;font-size:13px">Выйти</button>
        ` : `
          <a href="login.html">Вход</a>
          <a href="register.html" class="btn-primary" style="padding:8px 16px;font-size:14px">Регистрация</a>
        `}
      </div>
    </div>
  `;
});