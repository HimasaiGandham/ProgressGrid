// api.js - Core API wrapper

const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    getToken: () => localStorage.getItem('progressgrid_token'),
    setToken: (token) => localStorage.setItem('progressgrid_token', token),
    removeToken: () => localStorage.removeItem('progressgrid_token'),

    request: async (endpoint, options = {}) => {
        const token = api.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                ...options,
                headers
            });

            if (response.status === 401 || response.status === 403) {
                // Token expired or invalid
                api.removeToken();
                window.dispatchEvent(new Event('auth-expired'));
                throw new Error('Unauthorized');
            }

            // Some endpoints return empty body on success (like DELETE or complete)
            if (response.status === 200 || response.status === 201) {
                const text = await response.text();
                if (!text) return null;
                try {
                    return JSON.parse(text);
                } catch (e) {
                    return text; // Return as plain text if it's not valid JSON
                }
            }

            const errorText = await response.text();
            let errorMessage = errorText;
            try {
                const parsedError = JSON.parse(errorText);
                errorMessage = parsedError.message || errorText;
            } catch (e) {
                // Not JSON, use plain text
            }
            throw new Error(errorMessage || 'Request failed');

        } catch (error) {
            console.error(`API Error on ${endpoint}:`, error);
            throw error;
        }
    },

    auth: {
        login: (data) => api.request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
        register: (data) => api.request('/auth/register', { method: 'POST', body: JSON.stringify(data) })
    },

    activities: {
        getAll: () => api.request('/activities'),
        create: (data) => api.request('/activities', { method: 'POST', body: JSON.stringify(data) }),
        update: (id, data) => api.request(`/activities/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
        delete: (id) => api.request(`/activities/${id}`, { method: 'DELETE' }),
        complete: (id, date) => api.request(`/activities/${id}/complete?date=${date}`, { method: 'POST' }),
        uncomplete: (id, date) => api.request(`/activities/${id}/uncomplete?date=${date}`, { method: 'POST' })
    },

    progress: {
        getDaily: () => api.request('/progress/daily'),
        getWeekly: () => api.request('/progress/weekly'),
        getMonthly: () => api.request('/progress/monthly')
    },

    notifications: {
        getAll: () => api.request('/notifications'),
        markRead: (id) => api.request(`/notifications/${id}/read`, { method: 'PUT' })
    }
};
