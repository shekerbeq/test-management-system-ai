const API = '/api';
const LANG_KEY = 'ui_lang';
const I18N = {
    kz: {
        tests: 'Тесттер', create: 'Жасау', availableTests: 'Қолжетімді тесттер', admin: 'Әкімшілік',
        profile: 'Профиль', logout: 'Шығу', login: 'Кіру', register: 'Тіркелу', language: 'Тіл',
        loginTitle: 'Жүйеге кіру', loginText: 'Тест жасау немесе тапсыру үшін аккаунтыңызға кіріңіз.',
        loginField: 'Логин немесе email', password: 'Құпия сөз', loginButton: 'Кіру',
        registerTitle: 'Тіркелу', fullName: 'Аты-жөні', role: 'Рөл', student: 'Студент',
        teacher: 'Оқытушы', openAccount: 'Аккаунт ашу', inviteCode: 'Шақыру коды',
        invitePlaceholder: 'Тест кодын енгізіңіз', open: 'Ашу', history: 'Өткен тесттер тарихы',
        noHistory: 'Әзірге аяқталған әрекеттер жоқ.', review: 'Талдау', score: 'Балл', finished: 'Аяқталды',
        teacherPanel: 'Оқытушы панелі', myTests: 'Менің тесттерім', newTest: 'Жаңа тест жасау',
        statistics: 'Статистика', edit: 'Өңдеу', copy: 'Көшіру', invite: 'Шақыру',
        publish: 'Жариялау', close: 'Жабу', delete: 'Жою', adminPanel: 'Әкімшілік панель',
        users: 'Пайдаланушылар', allTests: 'Барлық тесттер', status: 'Статус', actions: 'Әрекет',
        blocked: 'Бұғатталған', active: 'Белсенді', block: 'Бұғаттау', unblock: 'Бұғаттан шығару',
        attempts: 'Әрекеттер', completed: 'Аяқталған', average: 'Орташа', passRate: 'Сәтті өту',
        completionRate: 'Аяқтау', copiedInvite: 'Шақыру сілтемесі көшірілді'
    },
    ru: {
        tests: 'Тесты', create: 'Создать', availableTests: 'Доступные тесты', admin: 'Админка',
        profile: 'Профиль', logout: 'Выйти', login: 'Вход', register: 'Регистрация', language: 'Язык',
        loginTitle: 'Вход в систему', loginText: 'Войдите в аккаунт, чтобы создавать или проходить тесты.',
        loginField: 'Логин или email', password: 'Пароль', loginButton: 'Войти',
        registerTitle: 'Регистрация', fullName: 'ФИО', role: 'Роль', student: 'Студент',
        teacher: 'Учитель', openAccount: 'Создать аккаунт', inviteCode: 'Код приглашения',
        invitePlaceholder: 'Введите код теста', open: 'Открыть', history: 'История пройденных тестов',
        noHistory: 'Пока нет завершенных попыток.', review: 'Разбор', score: 'Балл', finished: 'Завершен',
        teacherPanel: 'Панель учителя', myTests: 'Мои тесты', newTest: 'Создать новый тест',
        statistics: 'Статистика', edit: 'Редактировать', copy: 'Копировать', invite: 'Приглашение',
        publish: 'Опубликовать', close: 'Закрыть', delete: 'Удалить', adminPanel: 'Админ-панель',
        users: 'Пользователи', allTests: 'Все тесты', status: 'Статус', actions: 'Действия',
        blocked: 'Заблокирован', active: 'Активен', block: 'Блокировать', unblock: 'Разблокировать',
        attempts: 'Попытки', completed: 'Завершено', average: 'Средний балл', passRate: 'Сдали',
        completionRate: 'Завершили', copiedInvite: 'Ссылка приглашения скопирована'
    },
    en: {
        tests: 'Tests', create: 'Create', availableTests: 'Available tests', admin: 'Admin',
        profile: 'Profile', logout: 'Logout', login: 'Sign in', register: 'Register', language: 'Language',
        loginTitle: 'Sign in', loginText: 'Sign in to create or take tests.',
        loginField: 'Login or email', password: 'Password', loginButton: 'Sign in',
        registerTitle: 'Register', fullName: 'Full name', role: 'Role', student: 'Student',
        teacher: 'Teacher', openAccount: 'Create account', inviteCode: 'Invitation code',
        invitePlaceholder: 'Enter test code', open: 'Open', history: 'Completed test history',
        noHistory: 'No completed attempts yet.', review: 'Review', score: 'Score', finished: 'Finished',
        teacherPanel: 'Teacher panel', myTests: 'My tests', newTest: 'Create new test',
        statistics: 'Statistics', edit: 'Edit', copy: 'Copy', invite: 'Invite',
        publish: 'Publish', close: 'Close', delete: 'Delete', adminPanel: 'Admin panel',
        users: 'Users', allTests: 'All tests', status: 'Status', actions: 'Actions',
        blocked: 'Blocked', active: 'Active', block: 'Block', unblock: 'Unblock',
        attempts: 'Attempts', completed: 'Completed', average: 'Average', passRate: 'Passed',
        completionRate: 'Completed', copiedInvite: 'Invitation link copied'
    }
};

function lang() {
    return localStorage.getItem(LANG_KEY) || 'ru';
}

function t(key) {
    return (I18N[lang()] && I18N[lang()][key]) || I18N.ru[key] || key;
}

function setLanguage(value) {
    localStorage.setItem(LANG_KEY, value);
    applyI18n();
    document.dispatchEvent(new CustomEvent('languagechange'));
}

function applyI18n(root = document) {
    root.querySelectorAll('[data-i18n]').forEach(element => element.textContent = t(element.dataset.i18n));
    root.querySelectorAll('[data-i18n-placeholder]').forEach(element => element.placeholder = t(element.dataset.i18nPlaceholder));
    document.documentElement.lang = lang();
}

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

    let response = await fetch(API + url, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (response.status === 401 && localStorage.getItem('refreshToken')) {
        const refreshed = await refreshAccessToken();
        if (refreshed) {
            headers.Authorization = `Bearer ${localStorage.getItem('jwt')}`;
            response = await fetch(API + url, {
                method,
                headers,
                body: body ? JSON.stringify(body) : null
            });
        }
    }

    if ((response.status === 401 || response.status === 403) && !url.startsWith('/auth/login')) {
        localStorage.clear();
        location.href = '/login.html';
        throw new Error('Қайта кіріңіз');
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(errorMessage(text) || 'Сервер қатесі');
    }

    const text = await response.text();
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

const api = request;

function errorMessage(text) {
    if (!text) return '';
    try {
        const data = JSON.parse(text);
        if (data.errors) {
            return Object.entries(data.errors)
                .map(([field, message]) => `${field}: ${message}`)
                .join('; ');
        }
        return data.message || data.error || text;
    } catch {
        return text;
    }
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

async function logout() {
    try {
        await fetch(API + '/auth/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: localStorage.getItem('refreshToken') })
        });
    } finally {
        const savedLang = lang();
        localStorage.clear();
        localStorage.setItem(LANG_KEY, savedLang);
        location.href = '/landing.html';
    }
}

async function refreshAccessToken() {
    try {
        const response = await fetch(API + '/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: localStorage.getItem('refreshToken') })
        });
        if (!response.ok) return false;
        const data = await response.json();
        localStorage.setItem('jwt', data.token);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, role: data.role, fullName: data.fullName }));
        return true;
    } catch {
        return false;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initExperienceEffects();
    const code = new URLSearchParams(location.search).get('code');
    if (code) localStorage.setItem('pending_invite_code', code);

    const header = document.getElementById('global-header');
    const user = currentUser();
    if (header) {
        header.innerHTML = `
        <div class="topbar">
            <a class="brand" href="landing.html">Eduvibe</a>
            <nav class="nav">
                ${user ? `
                    ${user.role === 'TEACHER' ? `<a href="tests.html" data-i18n="tests">${t('tests')}</a><a href="create-test.html" data-i18n="create">${t('create')}</a><a href="prompt-templates.html">Prompt</a>` : ''}
                    ${user.role === 'STUDENT' ? `<a href="dashboard.html" data-i18n="availableTests">${t('availableTests')}</a>` : ''}
                    ${user.role === 'ADMIN' ? `<a href="admin-users.html" data-i18n="admin">${t('admin')}</a>` : ''}
                    <a class="user-pill" href="profile.html">${user.fullName || user.email}</a>
                    <button class="btn btn-outline btn-sm" onclick="logout()" data-i18n="logout">${t('logout')}</button>
                ` : `
                    <a href="login.html" data-i18n="login">${t('login')}</a>
                    <a class="btn btn-primary btn-sm" href="register.html" data-i18n="register">${t('register')}</a>
                `}
                <label class="lang-switch" title="${t('language')}">
                    <span data-i18n="language">${t('language')}</span>
                    <select id="langSelect" onchange="setLanguage(this.value)">
                        <option value="kz" ${lang() === 'kz' ? 'selected' : ''}>KZ</option>
                        <option value="ru" ${lang() === 'ru' ? 'selected' : ''}>RUS</option>
                        <option value="en" ${lang() === 'en' ? 'selected' : ''}>EN</option>
                    </select>
                </label>
            </nav>
        </div>
    `;
    }
    applyI18n();
});

function initExperienceEffects() {
    const revealObserver = 'IntersectionObserver' in window
        ? new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    revealObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 })
        : null;

    const registerReveal = (element, index = 0) => {
        if (element.classList.contains('reveal')) return;
        element.classList.add('reveal');
        element.style.transitionDelay = `${Math.min(index * 45, 260)}ms`;
        if (revealObserver) {
            revealObserver.observe(element);
        } else {
            element.classList.add('is-visible');
        }
    };

    document.querySelectorAll('.panel, .card, .question-card, .feature-card, .hero-copy, .hero-board')
        .forEach(registerReveal);

    const mutationObserver = new MutationObserver((mutations) => {
        mutations.forEach(mutation => {
            mutation.addedNodes.forEach(node => {
                if (!(node instanceof HTMLElement)) return;
                if (node.matches('.panel, .card, .question-card, .feature-card, .hero-board')) {
                    registerReveal(node);
                }
                node.querySelectorAll?.('.panel, .card, .question-card, .feature-card, .hero-board')
                    .forEach(registerReveal);
            });
        });
    });
    mutationObserver.observe(document.body, { childList: true, subtree: true });

}
