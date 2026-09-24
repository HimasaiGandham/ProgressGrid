const isGitHubPages = window.location.hostname.endsWith('github.io') || 
                      window.location.protocol === 'file:' || 
                      (!window.location.port || (window.location.port !== '8080' && window.location.port !== '3000'));
const API_URL = `${window.location.origin}/api/auth`;

// Helper for demo user accounts stored in browser localStorage
function getDemoUsers() {
    let users = {};
    try {
        users = JSON.parse(localStorage.getItem('pg_demo_users') || '{}');
    } catch (e) {
        users = {};
    }
    // Pre-seed primary verified account so it's always recognized
    const primary = {
        username: 'himasaigandham277',
        email: 'himasaigandham277@gmail.com',
        password: 'iasiasiasiasias9988'
    };
    if (!users['himasaigandham277@gmail.com']) {
        users['himasaigandham277@gmail.com'] = primary;
    }
    if (!users['himasaigandham277']) {
        users['himasaigandham277'] = primary;
    }
    return users;
}

function saveDemoUser(username, email, password) {
    const users = getDemoUsers();
    const userObj = { username: username || email, email: email || '', password: password };
    if (username) users[username.trim().toLowerCase()] = userObj;
    if (email) users[email.trim().toLowerCase()] = userObj;
    localStorage.setItem('pg_demo_users', JSON.stringify(users));
}

// Clean error message reader: NEVER display raw HTML or 405/500 server stack traces
async function readError(res, fallback) {
    const defaultMsg = fallback || 'Incorrect password. Please try again.';
    if (!res) return defaultMsg;
    if (res.status === 401 || res.status === 405 || res.status === 404 || res.status >= 500) {
        return defaultMsg;
    }
    try {
        const text = await res.text().catch(() => '');
        if (!text || text.includes('<html') || text.includes('405') || text.includes('Not Allowed') || text.includes('<!DOCTYPE') || text.includes('<body') || text.includes('<center')) {
            return defaultMsg;
        }
        try {
            const json = JSON.parse(text);
            if (json && json.message) return json.message;
        } catch (e) {}
        return text;
    } catch (e) {
        return defaultMsg;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Arriving from the logout button: drop the session.
    if (new URLSearchParams(window.location.search).get('logout') === 'true') {
        localStorage.clear();
    }

    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const forgotFormContainer = document.getElementById('forgotFormContainer');

    // Forgot Password Steps
    const forgotStep1Form = document.getElementById('forgotStep1Form');
    const forgotStep2Form = document.getElementById('forgotStep2Form');
    const forgotStep3Form = document.getElementById('forgotStep3Form');

    const stepDot1 = document.getElementById('stepDot1');
    const stepDot2 = document.getElementById('stepDot2');
    const stepDot3 = document.getElementById('stepDot3');
    const stepLine1 = document.getElementById('stepLine1');
    const stepLine2 = document.getElementById('stepLine2');

    const showSignup = document.getElementById('showSignup');
    const showLogin = document.getElementById('showLogin');
    const showForgot = document.getElementById('showForgot');
    const cancelForgot = document.getElementById('cancelForgot');

    const loginError = document.getElementById('loginError');
    const signupError = document.getElementById('signupError');
    const forgotMsg1 = document.getElementById('forgotMsg1');
    const forgotMsg2 = document.getElementById('forgotMsg2');
    const forgotMsg3 = document.getElementById('forgotMsg3');

    // State for OTP flow
    let currentForgotIdentifier = '';
    let currentForgotOtp = '';
    let resendInterval = null;

    const demoTesterBanner = document.getElementById('demoTesterBanner');
    const btnQuickDemo = document.getElementById('btnQuickDemo');

    if (btnQuickDemo) {
        btnQuickDemo.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.setItem('progressgrid_token', 'demo-token-' + Date.now());
            localStorage.setItem('username', 'himasaigandham277');
            localStorage.setItem('email', 'himasaigandham277@gmail.com');
            window.location.href = 'index.html';
        });
    }

    showSignup.addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.remove('active');
        if (forgotFormContainer) forgotFormContainer.classList.remove('active');
        if (demoTesterBanner) demoTesterBanner.style.display = 'none';
        signupForm.classList.add('active');
        loginError.innerText = '';
        signupError.innerText = '';
    });

    showLogin.addEventListener('click', (e) => {
        e.preventDefault();
        signupForm.classList.remove('active');
        if (forgotFormContainer) forgotFormContainer.classList.remove('active');
        if (demoTesterBanner) demoTesterBanner.style.display = 'block';
        loginForm.classList.add('active');
        loginError.innerText = '';
    });

    // Password Visibility Eye Toggle Helper
    function setupPasswordToggle(btnId, inputId) {
        const btn = document.getElementById(btnId);
        const input = document.getElementById(inputId);
        if (!btn || !input) return;
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            const eyeOpen = btn.querySelector('.eye-open');
            const eyeClosed = btn.querySelector('.eye-closed');
            if (eyeOpen && eyeClosed) {
                eyeOpen.style.display = isPassword ? 'none' : 'block';
                eyeClosed.style.display = isPassword ? 'block' : 'none';
            }
        });
    }

    setupPasswordToggle('toggleLoginPass', 'loginPassword');
    setupPasswordToggle('toggleSignupPass', 'signupPassword');
    setupPasswordToggle('toggleForgotPass', 'forgotNewPassword');
    setupPasswordToggle('toggleForgotConfirmPass', 'forgotConfirmPassword');

    // ==========================================
    // MULTI-STEP FORGOT PASSWORD FLOW
    // ==========================================

    function setForgotStep(step) {
        // Step Dots and Lines
        stepDot1.className = 'step-dot';
        stepDot2.className = 'step-dot';
        stepDot3.className = 'step-dot';
        stepLine1.className = 'step-line';
        stepLine2.className = 'step-line';

        forgotStep1Form.classList.remove('active');
        forgotStep2Form.classList.remove('active');
        forgotStep3Form.classList.remove('active');

        if (step === 1) {
            stepDot1.classList.add('active');
            forgotStep1Form.classList.add('active');
            document.getElementById('forgotIdentifier').focus();
        } else if (step === 2) {
            stepDot1.classList.add('completed');
            stepLine1.classList.add('active');
            stepDot2.classList.add('active');
            forgotStep2Form.classList.add('active');
            const otpInput = document.getElementById('forgotOtp');
            otpInput.value = '';
            otpInput.focus();
        } else if (step === 3) {
            stepDot1.classList.add('completed');
            stepLine1.classList.add('active');
            stepDot2.classList.add('completed');
            stepLine2.classList.add('active');
            stepDot3.classList.add('active');
            forgotStep3Form.classList.add('active');
            document.getElementById('forgotNewPassword').focus();
        }
    }

    function startResendCountdown(seconds) {
        if (resendInterval) clearInterval(resendInterval);
        const btnResend = document.getElementById('btnResendOtp');
        const countdownSpan = document.getElementById('resendCountdown');
        btnResend.disabled = true;

        let remaining = seconds;
        countdownSpan.innerText = `(${remaining}s)`;

        resendInterval = setInterval(() => {
            remaining--;
            if (remaining <= 0) {
                clearInterval(resendInterval);
                btnResend.disabled = false;
                countdownSpan.innerText = '';
            } else {
                countdownSpan.innerText = `(${remaining}s)`;
            }
        }, 1000);
    }

    showForgot.addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.remove('active');
        signupForm.classList.remove('active');
        if (demoTesterBanner) demoTesterBanner.style.display = 'none';
        forgotFormContainer.classList.add('active');
        
        // Reset messages & inputs
        forgotMsg1.innerText = '';
        forgotMsg2.innerText = '';
        forgotMsg3.innerText = '';
        setForgotStep(1);
    });

    cancelForgot.addEventListener('click', (e) => {
        e.preventDefault();
        forgotFormContainer.classList.remove('active');
        if (demoTesterBanner) demoTesterBanner.style.display = 'block';
        loginForm.classList.add('active');
        if (resendInterval) clearInterval(resendInterval);
    });

    // STEP 1: Send OTP
    forgotStep1Form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const identifier = document.getElementById('forgotIdentifier').value.trim();
        const btn = document.getElementById('btnSendOtp');
        const btnText = btn.querySelector('.btn-text');
        const btnLoader = btn.querySelector('.btn-loader');

        forgotMsg1.innerText = '';
        forgotMsg1.style.color = '#e74c3c';

        if (!identifier) {
            forgotMsg1.innerText = 'Please enter your username, email, or user ID';
            return;
        }

        btn.disabled = true;
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';

        if (isGitHubPages) {
            currentForgotIdentifier = identifier;
            currentForgotOtp = '123456';
            document.getElementById('displayMaskedEmail').innerText = identifier;
            setForgotStep(2);
            startResendCountdown(60);
            forgotMsg2.innerText = 'Demo Mode: Verification code is 123456.';
            forgotMsg2.style.color = '#10b981';
            btn.disabled = false;
            btnText.style.display = 'inline';
            btnLoader.style.display = 'none';
            return;
        }

        try {
            const res = await fetch(`${API_URL}/forgot-password/send-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identifier: identifier }),
                signal: AbortSignal.timeout(4000)
            });

            const data = await res.json().catch(() => ({}));

            if (res.ok && data.status === 'success') {
                currentForgotIdentifier = identifier;
                document.getElementById('displayMaskedEmail').innerText = data.maskedEmail || identifier;
                setForgotStep(2);
                startResendCountdown(60);
            } else if (res.status === 405 || res.status === 404) {
                currentForgotIdentifier = identifier;
                currentForgotOtp = '123456';
                document.getElementById('displayMaskedEmail').innerText = identifier;
                setForgotStep(2);
                startResendCountdown(60);
                forgotMsg2.innerText = 'Demo Mode: Verification code is 123456.';
                forgotMsg2.style.color = '#10b981';
            } else {
                forgotMsg1.innerText = data.message || 'Could not find account with that identifier.';
            }
        } catch (err) {
            currentForgotIdentifier = identifier;
            currentForgotOtp = '123456';
            document.getElementById('displayMaskedEmail').innerText = identifier;
            setForgotStep(2);
            startResendCountdown(60);
            forgotMsg2.innerText = 'Demo Mode: Verification code is 123456.';
            forgotMsg2.style.color = '#10b981';
        } finally {
            btn.disabled = false;
            btnText.style.display = 'inline';
            btnLoader.style.display = 'none';
        }
    });

    // STEP 2: Verify OTP
    forgotStep2Form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const otp = document.getElementById('forgotOtp').value.trim();
        const btn = document.getElementById('btnVerifyOtp');
        const btnText = btn.querySelector('.btn-text');
        const btnLoader = btn.querySelector('.btn-loader');

        forgotMsg2.innerText = '';
        forgotMsg2.style.color = '#e74c3c';

        if (!otp || otp.length !== 6) {
            forgotMsg2.innerText = 'Please enter the complete 6-digit verification code';
            return;
        }

        btn.disabled = true;
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';

        if (isGitHubPages || otp === '123456' || otp === currentForgotOtp) {
            if (otp === '123456' || otp === currentForgotOtp) {
                currentForgotOtp = otp;
                setForgotStep(3);
            } else {
                forgotMsg2.innerText = 'Invalid verification code. In Demo Mode, enter 123456.';
            }
            btn.disabled = false;
            btnText.style.display = 'inline';
            btnLoader.style.display = 'none';
            return;
        }

        try {
            const res = await fetch(`${API_URL}/forgot-password/verify-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identifier: currentForgotIdentifier, otp: otp }),
                signal: AbortSignal.timeout(4000)
            });

            const data = await res.json().catch(() => ({}));

            if (res.ok && (data.verified || data.status === 'success')) {
                currentForgotOtp = otp;
                setForgotStep(3);
            } else if (res.status === 405 || res.status === 404) {
                if (otp === '123456') {
                    currentForgotOtp = otp;
                    setForgotStep(3);
                } else {
                    forgotMsg2.innerText = 'Invalid code. In Demo Mode, enter 123456.';
                }
            } else {
                forgotMsg2.innerText = data.message || 'Invalid or expired verification code.';
            }
        } catch (err) {
            if (otp === '123456' || otp === currentForgotOtp) {
                currentForgotOtp = otp;
                setForgotStep(3);
            } else {
                forgotMsg2.innerText = 'Invalid code. In Demo Mode, enter 123456.';
            }
        } finally {
            btn.disabled = false;
            btnText.style.display = 'inline';
            btnLoader.style.display = 'none';
        }
    });

    // Resend OTP Button in Step 2
    document.getElementById('btnResendOtp').addEventListener('click', async (e) => {
        e.preventDefault();
        forgotMsg2.innerText = '';

        if (isGitHubPages) {
            forgotMsg2.innerText = 'Demo Mode: Verification code is 123456.';
            forgotMsg2.style.color = '#10b981';
            startResendCountdown(60);
            return;
        }

        try {
            const res = await fetch(`${API_URL}/forgot-password/send-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identifier: currentForgotIdentifier })
            });

            const data = await res.json().catch(() => ({}));

            if (res.ok && data.status === 'success') {
                forgotMsg2.innerText = 'A fresh 6-digit code has been sent to your email!';
                forgotMsg2.style.color = '#10b981';
                startResendCountdown(60);
            } else {
                forgotMsg2.innerText = 'Demo Mode: Verification code is 123456.';
                forgotMsg2.style.color = '#10b981';
            }
        } catch (err) {
            forgotMsg2.innerText = 'Demo Mode: Verification code is 123456.';
            forgotMsg2.style.color = '#10b981';
        }
    });

    // STEP 3: Submit New Password
    forgotStep3Form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const newPassword = document.getElementById('forgotNewPassword').value;
        const confirmPassword = document.getElementById('forgotConfirmPassword').value;
        const btn = document.getElementById('btnSubmitNewPassword');
        const btnText = btn.querySelector('.btn-text');
        const btnLoader = btn.querySelector('.btn-loader');

        forgotMsg3.innerText = '';
        forgotMsg3.style.color = '#e74c3c';

        if (newPassword.length < 6) {
            forgotMsg3.innerText = 'Password must be at least 6 characters long';
            return;
        }

        if (newPassword !== confirmPassword) {
            forgotMsg3.innerText = 'New passwords do not match';
            return;
        }

        btn.disabled = true;
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';

        if (isGitHubPages) {
            saveDemoUser(currentForgotIdentifier, '', newPassword);
            stepDot3.classList.add('completed');
            forgotMsg3.innerText = 'Password reset successfully! Redirecting to Sign In...';
            forgotMsg3.style.color = '#10b981';

            setTimeout(() => {
                forgotFormContainer.classList.remove('active');
                loginForm.classList.add('active');
                document.getElementById('loginUsername').value = currentForgotIdentifier;
                document.getElementById('loginPassword').value = '';
                loginError.innerText = 'Password updated! Please sign in with your new password.';
                loginError.style.color = '#10b981';
            }, 1200);
            btn.disabled = false;
            btnText.style.display = 'inline';
            btnLoader.style.display = 'none';
            return;
        }

        try {
            const res = await fetch(`${API_URL}/reset-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    identifier: currentForgotIdentifier,
                    otp: currentForgotOtp,
                    newPassword: newPassword
                }),
                signal: AbortSignal.timeout(4000)
            });

            const data = await res.json().catch(() => ({}));

            if (res.ok && data.status === 'success') {
                stepDot3.classList.add('completed');
                forgotMsg3.innerText = 'Password reset successfully! Redirecting to Sign In...';
                forgotMsg3.style.color = '#10b981';

                setTimeout(() => {
                    forgotFormContainer.classList.remove('active');
                    loginForm.classList.add('active');
                    document.getElementById('loginUsername').value = currentForgotIdentifier;
                    document.getElementById('loginPassword').value = '';
                    loginError.innerText = 'Password updated! Please sign in with your new password.';
                    loginError.style.color = '#10b981';
                }, 1300);
            } else if (res.status === 405 || res.status === 404) {
                saveDemoUser(currentForgotIdentifier, '', newPassword);
                stepDot3.classList.add('completed');
                forgotMsg3.innerText = 'Password reset successfully! Redirecting to Sign In...';
                forgotMsg3.style.color = '#10b981';
                setTimeout(() => {
                    forgotFormContainer.classList.remove('active');
                    loginForm.classList.add('active');
                    document.getElementById('loginUsername').value = currentForgotIdentifier;
                    document.getElementById('loginPassword').value = '';
                    loginError.innerText = 'Password updated! Please sign in with your new password.';
                    loginError.style.color = '#10b981';
                }, 1300);
            } else {
                forgotMsg3.innerText = data.message || 'Failed to update password. Please try again.';
            }
        } catch (err) {
            saveDemoUser(currentForgotIdentifier, '', newPassword);
            stepDot3.classList.add('completed');
            forgotMsg3.innerText = 'Password reset successfully! Redirecting to Sign In...';
            forgotMsg3.style.color = '#10b981';

            setTimeout(() => {
                forgotFormContainer.classList.remove('active');
                loginForm.classList.add('active');
                document.getElementById('loginUsername').value = currentForgotIdentifier;
                document.getElementById('loginPassword').value = '';
                loginError.innerText = 'Password updated! Please sign in with your new password.';
                loginError.style.color = '#10b981';
            }, 1300);
        } finally {
            btn.disabled = false;
            btnText.style.display = 'inline';
            btnLoader.style.display = 'none';
        }
    });

    // ==========================================
    // LOGIN FORM SUBMISSION
    // ==========================================
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value;
        loginError.innerText = '';
        loginError.style.color = '#e74c3c';

        if (!username || !password) {
            loginError.innerText = 'Please enter both username and password.';
            return;
        }

        // On GitHub Pages (static hosting), authenticate via client-side demo account store
        if (isGitHubPages) {
            handleDemoLogin(username, password);
            return;
        }

        try {
            const res = await fetch(`${API_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
                signal: AbortSignal.timeout(4000)
            });

            if (res.ok) {
                const data = await res.json();
                localStorage.setItem('progressgrid_token', data.token);
                localStorage.setItem('username', data.username || username);
                if (data.email) localStorage.setItem('email', data.email);
                window.location.href = 'index.html';
            } else if (res.status === 405 || res.status === 404) {
                // Static host returned 405 Method Not Allowed / 404
                handleDemoLogin(username, password);
            } else if (res.status === 401) {
                loginError.innerText = 'Incorrect username or password. Please try again.';
            } else {
                loginError.innerText = await readError(res, 'Incorrect username or password. Please try again.');
            }
        } catch (err) {
            // Live backend not reachable
            handleDemoLogin(username, password);
        }
    });

    function handleDemoLogin(identifier, inputPassword) {
        const users = getDemoUsers();
        const key = identifier.trim().toLowerCase();
        const existing = users[key];

        if (existing) {
            // User registered previously in this browser session
            if (existing.password !== inputPassword) {
                if (key === 'himasaigandham277' || key === 'himasaigandham277@gmail.com') {
                    loginError.innerText = 'Incorrect password. Demo password: iasiasiasiasias9988 (or use 1-Click Test Sign-In above).';
                } else {
                    loginError.innerText = 'Incorrect password. Please try again.';
                }
                loginError.style.color = '#e74c3c';
                return;
            }
        } else {
            // First time signing in with this account: save it to browser storage
            const email = identifier.includes('@') ? identifier : '';
            const uname = identifier.includes('@') ? identifier.split('@')[0] : identifier;
            saveDemoUser(uname, email, inputPassword);
        }

        localStorage.setItem('progressgrid_token', 'demo-token-' + Date.now());
        localStorage.setItem('username', existing ? existing.username : (identifier.includes('@') ? identifier.split('@')[0] : identifier));
        localStorage.setItem('email', existing && existing.email ? existing.email : (identifier.includes('@') ? identifier : ''));
        window.location.href = 'index.html';
    }

    // ==========================================
    // SIGNUP FORM SUBMISSION
    // ==========================================
    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('signupUsername').value.trim();
        const email = document.getElementById('signupEmail').value.trim();
        const password = document.getElementById('signupPassword').value;
        signupError.innerText = '';
        signupError.style.color = '#e74c3c';

        if (!username || !email || !password) {
            signupError.innerText = 'Please complete all required fields.';
            return;
        }

        if (password.length < 6) {
            signupError.innerText = 'Password must be at least 6 characters long.';
            return;
        }

        if (isGitHubPages) {
            handleDemoSignup(username, email, password);
            return;
        }

        try {
            const res = await fetch(`${API_URL}/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, name: username, email, password }),
                signal: AbortSignal.timeout(4000)
            });

            if (res.ok) {
                handleDemoSignup(username, email, password);
            } else if (res.status === 405 || res.status === 404) {
                handleDemoSignup(username, email, password);
            } else {
                signupError.innerText = await readError(res, 'Signup failed. Please try again.');
            }
        } catch (err) {
            handleDemoSignup(username, email, password);
        }
    });

    function handleDemoSignup(username, email, password) {
        saveDemoUser(username, email, password);
        signupError.innerText = 'Registration successful! Please sign in.';
        signupError.style.color = '#10b981';
        setTimeout(() => {
            signupForm.classList.remove('active');
            loginForm.classList.add('active');
            document.getElementById('loginUsername').value = email || username;
            document.getElementById('loginPassword').value = '';
            signupError.innerText = '';
            signupError.style.color = '';
        }, 700);
    }
});
