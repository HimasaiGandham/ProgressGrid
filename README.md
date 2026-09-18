# ProgressGrid - Email OTP Verification & Secure Password Reset

This branch (`feature/otp-verification`) implements multi-step, production-ready Email OTP verification and secure password reset workflows.

## Features Included
- **Cryptographic 6-Digit OTP**: Secure random numeric token generation with a 10-minute expiry window.
- **Transactional Email Delivery**:
  - **Resend REST API**: Direct HTTP/2 integration with `https://api.resend.com/emails` for lightning-fast inbox delivery.
  - **SMTP Fallback**: Automatic failover to configured SMTP mail servers.
  - **Console Logging Fallback**: Local developer fallback logging OTPs when no API key or SMTP is configured.
- **3-Step Security Modal Flow**:
  1. *Request Code*: User enters username, email, or User ID. The system resolves the account and responds with masked email confirmation (e.g., `te***@domain.com`).
  2. *Verify OTP*: 6 individual PIN digits with auto-advance, backspace navigation, paste support, and a 60-second cooldown resend timer.
  3. *Set New Password*: Validates minimum 6-character length and updates user credentials securely.
- **Rate-Limiting & Security Guards**: Enforces OTP verification prior to allowing password changes, preventing unauthorized credential tampering.

## Architecture
- **Front-End**:
  - `Front-End/login.html`: Multi-step OTP modal with interactive state transitions.
  - `Front-End/login.css`: Styled OTP digit inputs, glowing active states, countdown timer, and alert badges.
  - `Front-End/login.js`: Real-time OTP dispatch, verification, countdown timer interval, and reset password API communication.
- **Back-End (Spring Boot 3 / Java 17)**:
  - `OtpService.java`: Thread-safe concurrent cache for OTP generation, expiration tracking, attempt verification, and invalidation.
  - `EmailService.java`: Resend REST client and JavaMailSender SMTP integration.
  - `AuthController.java`: Exposes `/api/auth/forgot-password/send-otp`, `/verify-otp`, and `/reset-password`.
  - `AuthService.java`: Orchestrates user lookup, masking, OTP challenge verification, and password updates.
  - DTOs: `SendOtpDTO.java`, `VerifyOtpDTO.java`, `ResetPasswordDTO.java`.
