// Shared by every page: where the API lives. Load before app.js / login.js.
const isGitHubPages = window.location.hostname.endsWith('github.io');
const BACKEND_BASE = (window.location.port === '3000' || window.location.port === '8080')
    ? window.location.origin
    : (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' ? 'http://localhost:8080' : window.location.origin);
