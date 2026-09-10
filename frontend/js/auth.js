// auth.js - Authentication Logic

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginContainer = document.getElementById('login-form-container');
    const registerContainer = document.getElementById('register-form-container');
    const showRegisterLink = document.getElementById('show-register');
    const showLoginLink = document.getElementById('show-login');
    const loginError = document.getElementById('login-error');
    const regError = document.getElementById('reg-error');
    const authView = document.getElementById('auth-view');
    const appView = document.getElementById('app-view');
    const logoutBtn = document.getElementById('logout-btn');

    // Check if already logged in
    if (api.getToken()) {
        showApp();
    }

    // Toggle forms
    showRegisterLink.addEventListener('click', (e) => {
        e.preventDefault();
        loginContainer.classList.add('hidden');
        registerContainer.classList.remove('hidden');
        loginError.textContent = '';
    });

    showLoginLink.addEventListener('click', (e) => {
        e.preventDefault();
        registerContainer.classList.add('hidden');
        loginContainer.classList.remove('hidden');
        regError.textContent = '';
    });

    // Login Submission
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        try {
            loginError.textContent = 'Logging in...';
            const response = await api.auth.login({ email, password });
            api.setToken(response.accessToken);
            loginForm.reset();
            loginError.textContent = '';
            showApp();
        } catch (error) {
            loginError.textContent = 'Invalid email or password.';
        }
    });

    // Register Submission
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('reg-name').value;
        const email = document.getElementById('reg-email').value;
        const password = document.getElementById('reg-password').value;

        try {
            regError.textContent = 'Registering...';
            await api.auth.register({ name, email, password });
            regError.textContent = '';
            // Auto login or show login
            registerContainer.classList.add('hidden');
            loginContainer.classList.remove('hidden');
            document.getElementById('login-email').value = email;
            document.getElementById('login-password').value = password;
            loginError.textContent = 'Registration successful! Please login.';
        } catch (error) {
            regError.textContent = 'Registration failed. Email might be in use.';
        }
    });

    // Logout
    logoutBtn.addEventListener('click', () => {
        api.removeToken();
        showAuth();
    });

    // Handle token expiration
    window.addEventListener('auth-expired', showAuth);

    function showApp() {
        authView.classList.add('hidden');
        appView.classList.remove('hidden');
        window.dispatchEvent(new Event('app-ready'));
    }

    function showAuth() {
        appView.classList.add('hidden');
        authView.classList.remove('hidden');
    }
});
