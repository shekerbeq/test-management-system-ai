const API = '/api';

function currentUser() {
    try {
        return JSON.parse(localStorage.getItem('user'));
    } catch {
        return null;
    }
}

function showToast(message, ok = true) {
    const toast = document.createElement('div');
    toast.className = `toast ${ok ? 'toast-ok' : 'toast-error'}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3200);
}

const toast = showToast;

async function request(url, method = 'GET', body = null) {
    const token = localStorage.getItem('jwt');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers.Authorization = `Bearer ${token}`;

    const response = await fetch(API + url, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (response.status === 401 || response.status === 403) {
        localStorage.clear();
        location.href = '/login.html';
        throw new Error('Қайта кіріңіз');
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Сервер қатесі');
    }

    const text = await response.text();
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

const api = request;

document.addEventListener('DOMContentLoaded', () => {
    const header = document.getElementById('global-header');
    if (!header) return;

    const user = currentUser();
    header.innerHTML = `
        <div class="topbar">
            <a class="brand" href="landing.html">Eduvibe</a>
            <nav class="nav">
                ${user ? `
                    ${user.role === 'TEACHER' ? '<a href="tests.html">Тесттер</a><a href="create-test.html">Жасау</a>' : ''}
                    ${user.role === 'STUDENT' ? '<a href="dashboard.html">Қолжетімді тесттер</a>' : ''}
                    ${user.role === 'ADMIN' ? '<a href="admin-users.html">Әкімшілік</a>' : ''}
                    <span class="user-pill">${user.fullName || user.email}</span>
                    <button class="btn btn-outline btn-sm" onclick="localStorage.clear();location.href='/landing.html'">Шығу</button>
                ` : `
                    <a href="login.html">Кіру</a>
                    <a class="btn btn-primary btn-sm" href="register.html">Тіркелу</a>
                `}
            </nav>
        </div>
    `;
});
