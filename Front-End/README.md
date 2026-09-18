# 🎨 ProgressGrid - Frontend

<p align="left">
  <img src="https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" alt="JavaScript" />
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML5" />
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" alt="CSS3" />
  <img src="https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white" alt="Chart.js" />
</p>

This directory contains the user interface and presentation logic for the ProgressGrid dashboard. It is built using **Vanilla HTML5, CSS3, and JavaScript** without heavy framework overhead, ensuring it is lightweight, responsive, lightning-fast, and easy to customize.

---

## 📂 File Structure & Purpose

- **`index.html`**
  The main dashboard view. Contains the structural layout for sidebar navigation, summary overview cards, the dynamic habit/activity grid, right-side analytics panels, and the modal dialogs for adding habits.

- **`style.css`**
  The dashboard stylesheet. Implements the dark glassmorphic design system, responsive layouts, hover animations, and custom CSS variables for completion intensity.

- **`app.js`**
  The core dashboard application logic:
  - **API Communication**: Fetches habits and records completions with the Spring Boot backend (`/api/habits`).
  - **Dynamic Rendering**: Renders the 7-day completion grid and updates dates dynamically based on the active week.
  - **Optimistic UI**: Instantly updates checkbox states and synchronizes with server state.
  - **Analytics**: Calculates completion rates, streaks, and renders weekly progress graphs using **Chart.js**.

- **`login.html`**
  The authentication interface providing user login, signup, and 3-step security modal for password recovery with email OTP verification.

- **`login.css`**
  Styles for the authentication views, glassmorphic auth cards, form inputs, and verification modals.

- **`login.js`**
  Handles authentication state, credentials validation, OTP request/verification timers, and password reset flows.

- **`logo.png`**
  Official brand identity icon and logo.

---

## 🚀 How to Run

1. Ensure the Spring Boot backend is running on `http://localhost:8080`.
2. Start the local development server from the repository root:
   ```bash
   python dev_server.py
   ```
3. Open `http://localhost:3000` in your web browser.
