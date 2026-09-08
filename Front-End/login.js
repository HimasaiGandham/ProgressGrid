const API_URL = 'http://localhost:8080/api/auth';

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, go to dashboard
    if (localStorage.getItem('userId')) {
        window.location.href = 'index.html';
        return;
    }

    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const showSignup = document.getElementById('showSignup');
    const showLogin = document.getElementById('showLogin');
    
    const loginError = document.getElementById('loginError');
    const signupError = document.getElementById('signupError');

    showSignup.addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.remove('active');
        signupForm.classList.add('active');
        loginError.innerText = '';
    });

    showLogin.addEventListener('click', (e) => {
        e.preventDefault();
        signupForm.classList.remove('active');
        loginForm.classList.add('active');
        signupError.innerText = '';
    });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;
        
        try {
            const res = await fetch(`${API_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            
            if (res.ok) {
                const data = await res.json();
                localStorage.setItem('userId', data.id);
                localStorage.setItem('username', data.username);
                window.location.href = 'index.html';
            } else {
                const errorText = await res.text();
                loginError.innerText = errorText || 'Invalid credentials';
            }
        } catch (err) {
            loginError.innerText = 'Failed to connect to server';
        }
    });

    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('signupUsername').value;
        const email = document.getElementById('signupEmail').value;
        const password = document.getElementById('signupPassword').value;
        
        try {
            const res = await fetch(`${API_URL}/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });
            
            if (res.ok) {
                const data = await res.json();
                localStorage.setItem('userId', data.id);
                localStorage.setItem('username', data.username);
                window.location.href = 'index.html';
            } else {
                const errorText = await res.text();
                signupError.innerText = errorText || 'Signup failed';
            }
        } catch (err) {
            signupError.innerText = 'Failed to connect to server';
        }
    });
});
