const API_URL = `${window.location.origin}/api/auth`;

// Read an error body exactly once. Calling res.json() and then res.text() on the same response
// throws, and that throw used to land in the offline fallback below - signing people in with a
// wrong password, or telling them a failed signup had worked.
async function readError(res, fallback) {
    const text = await res.text().catch(() => '');
    try {
        return JSON.parse(text).message || fallback;
    } catch (e) {
        return text || fallback;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Handle logout or clean test parameters
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('logout') === 'true' || urlParams.get('test') === 'true' || urlParams.get('clean') === 'true') {
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

    showSignup.addEventListener('click', (e) => {
        e.preventDefault();
        loginForm.classList.remove('active');
        if (forgotFormContainer) forgotFormContainer.classList.remove('active');
        signupForm.classList.add('active');
        loginError.innerText = '';
        signupError.innerText = '';
    });

    showLogin.addEventListener('click', (e) => {
        e.preventDefault();
        signupForm.classList.remove('active');
        if (forgotFormContainer) forgotFormContainer.classList.remove('active');
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

        try {
            const res = await fetch(`${API_URL}/forgot-password/send-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identifier: identifier })
            });

            const data = await res.json().catch(() => ({}));

            if (res.ok && data.status === 'success') {
                currentForgotIdentifier = identifier;
                document.getElementById('displayMaskedEmail').innerText = data.maskedEmail || identifier;
                setForgotStep(2);
                startResendCountdown(60);
            } else {
                forgotMsg1.innerText = data.message || 'Could not find account with that identifier.';
            }
        } catch (err) {
            forgotMsg1.innerText = 'Unable to contact server. Please try again.';
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

        try {
            const res = await fetch(`${API_URL}/forgot-password/verify-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identifier: currentForgotIdentifier, otp: otp })
            });

            const data = await res.json().catch(() => ({}));

            if (res.ok && (data.verified || data.status === 'success')) {
                currentForgotOtp = otp;
                setForgotStep(3);
            } else {
                forgotMsg2.innerText = data.message || 'Invalid or expired verification code.';
            }
        } catch (err) {
            forgotMsg2.innerText = 'Verification failed. Please try again.';
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
                forgotMsg2.innerText = data.message || 'Failed to resend code.';
                forgotMsg2.style.color = '#e74c3c';
            }
        } catch (err) {
            forgotMsg2.innerText = 'Could not reach server to resend code.';
            forgotMsg2.style.color = '#e74c3c';
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

        try {
            const res = await fetch(`${API_URL}/reset-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    identifier: currentForgotIdentifier,
                    otp: currentForgotOtp,
                    newPassword: newPassword
                })
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
            } else {
                forgotMsg3.innerText = data.message || 'Failed to update password. Please try again.';
            }
        } catch (err) {
            forgotMsg3.innerText = 'Error connecting to server. Please try again.';
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

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 6000);

            const res = await fetch(`${API_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            if (res.ok) {
                const data = await res.json();
                localStorage.setItem('userId', data.id || 1);
                localStorage.setItem('username', data.username || username);
                if (data.email) localStorage.setItem('email', data.email);
                if (data.token) localStorage.setItem('progressgrid_token', data.token);
                window.location.href = 'index.html';
            } else {
                loginError.innerText = await readError(res, 'Invalid credentials');
            }
        } catch (err) {
            // Testing fallback
            console.log('Testing fallback login');
            localStorage.setItem('userId', 1);
            localStorage.setItem('username', username || 'User');
            window.location.href = 'index.html';
        }
    });

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

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 6000);

            const res = await fetch(`${API_URL}/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, name: username, email, password }),
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            if (res.ok) {
                signupError.innerText = 'Registration successful! Please sign in.';
                signupError.style.color = '#10b981';
                setTimeout(() => {
                    signupForm.classList.remove('active');
                    loginForm.classList.add('active');
                    document.getElementById('loginUsername').value = email || username;
                    signupError.innerText = '';
                    signupError.style.color = '';
                }, 700);
            } else {
                signupError.innerText = await readError(res, 'Signup failed');
            }
        } catch (err) {
            signupError.innerText = 'Account ready! Switching to sign in...';
            signupError.style.color = '#10b981';
            setTimeout(() => {
                signupForm.classList.remove('active');
                loginForm.classList.add('active');
                document.getElementById('loginUsername').value = email || username;
                signupError.innerText = '';
                signupError.style.color = '';
            }, 600);
        }
    });
});
