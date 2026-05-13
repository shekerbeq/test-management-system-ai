
const API_BASE = 'http://localhost:8080/api';

function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

async function request(endpoint, method = 'GET', body = null) {
    const token = localStorage.getItem('jwt');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(`${API_BASE}${endpoint}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('jwt');
        localStorage.removeItem('user');
        window.location.href = '/login.html';
        throw new Error('Unauthorized');
    }

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Ошибка сервера');
    }

    const text = await response.text();
    try {
        return JSON.parse(text);
    } catch (e) {
        return text;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const headerElement = document.getElementById('global-header');
    if (!headerElement) return;

    const user = JSON.parse(localStorage.getItem('user'));
    let navContent = `
        <div class="nav-container">
            <a href="landing.html" class="logo"><span class="logo-dot"></span>Eduvibe</a>
            <div class="nav-menu">
    `;

    if (user) {
        if (user.role === 'ROLE_TEACHER') {
            navContent += `
                <a href="tests.html" class="nav-link">Мои тесты</a>
                <a href="create-test.html" class="nav-link">Создать тест</a>
            `;
        } else if (user.role === 'ROLE_STUDENT') {
            navContent += `
                <a href="dashboard.html" class="nav-link">Доступные тесты</a>
                <a href="dashboard.html?tab=results" class="nav-link">Мои результаты</a>
            `;
        } else if (user.role === 'ROLE_ADMIN') {
            navContent += `<a href="admin-users.html" class="nav-link">Админ-панель</a>`;
        }
        navContent += `
            <a href="profile.html" class="nav-link">👤 ${user.fullName || user.email}</a>
            <button onclick="logout()" class="btn btn-outline" style="padding: 8px 16px; font-size: 13px;">Выйти</button>
        `;
    } else {
        navContent += `
            <a href="login.html" class="nav-link">Вход</a>
            <a href="register.html" class="btn btn-primary" style="padding: 10px 20px; font-size: 14px;">Регистрация</a>
        `;
    }

    navContent += `</div></div>`;
    headerElement.innerHTML = navContent;
});

function logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('user');
    window.location.href = 'landing.html';
}