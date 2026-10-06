const API_URL = `${BACKEND_BASE}/api/habits`;

document.addEventListener('DOMContentLoaded', () => {

    // Signed in means holding a session token. Without one, go to the sign-in page.
    if (!localStorage.getItem('progressgrid_token')) {
        window.location.href = 'login.html';
        return;
    }

    // State
    let habits = [];
    let currentWeekStart = getMonday(new Date());

    // DOM Elements
    const dayHeaderRow = document.getElementById('dayHeaderRow');
    const habitTableBody = document.getElementById('habitTableBody');
    const addHabitBtn = document.getElementById('addHabitBtn');
    const addHabitModal = document.getElementById('addHabitModal');
    const closeHabitModal = document.getElementById('closeHabitModal');
    const cancelHabitBtn = document.getElementById('cancelHabitBtn');
    const addHabitForm = document.getElementById('addHabitForm');
    const prevWeekBtn = document.getElementById('prevWeekBtn');
    const nextWeekBtn = document.getElementById('nextWeekBtn');

    // Tabs & Profile Elements
    const dashboardView = document.getElementById('dashboardView');
    const statsView = document.getElementById('statsView');
    const profileView = document.getElementById('profileView');
    const calendarView = document.getElementById('calendarView');
    const navDashboard = document.getElementById('navDashboard');
    const navHabits = document.getElementById('navHabits');
    const navStats = document.getElementById('navStats');
    const navCalendar = document.getElementById('navCalendar');
    const navSettings = document.getElementById('navSettings');
    const userProfileBtn = document.getElementById('userProfileBtn');
    const backToDashboardBtn = document.getElementById('backToDashboardBtn');
    const statsBackToDashboardBtn = document.getElementById('statsBackToDashboardBtn');
    const goToStatsBtn = document.getElementById('goToStatsBtn');
    const sidebarUsername = document.getElementById('sidebarUsername');
    const sidebarAvatarText = document.getElementById('sidebarAvatarText');
    const sidebarAvatarImg = document.getElementById('sidebarAvatarImg');

    // Profile Form & Photo Upload
    const profileAvatarLarge = document.getElementById('profileAvatarLarge');
    const largeAvatarInitial = document.getElementById('largeAvatarInitial');
    const largeAvatarImg = document.getElementById('largeAvatarImg');
    const photoFileInput = document.getElementById('photoFileInput');
    const triggerBrowseBtn = document.getElementById('triggerBrowseBtn');
    const triggerRemoveBtn = document.getElementById('triggerRemoveBtn');
    const profileDetailsForm = document.getElementById('profileDetailsForm');
    const profileNameInput = document.getElementById('profileNameInput');
    const profileUsernameInput = document.getElementById('profileUsernameInput');
    const profileEmailInput = document.getElementById('profileEmailInput');
    const profileMobileInput = document.getElementById('profileMobileInput');
    const profileAlertMsg = document.getElementById('profileAlertMsg');
    const clearFieldsBtn = document.getElementById('clearFieldsBtn');

    // Charts & Analytics Instances
    let weeklyChartInstance = null;
    let statsTrendChartInstance = null;
    let statsCategoryChartInstance = null;
    let statsDayOfWeekChartInstance = null;
    let currentStatsTimeframe = '30days';

    // Init
    init();

    function init() {
        initProfileData();
        attachEventListeners();
        fetchHabits();
    }

    // Local storage helpers for static hosting & client-side demo mode
    function getDefaultHabits() {
        const today = formatDateIso(new Date());
        const d1 = formatDateIso(new Date(Date.now() - 86400000));
        const d2 = formatDateIso(new Date(Date.now() - 86400000 * 2));
        const d3 = formatDateIso(new Date(Date.now() - 86400000 * 3));
        const defaultList = [
            {
                id: 101,
                name: 'Morning Workout & Stretch',
                category: 'Health',
                frequency: 'DAILY',
                startDate: d3,
                completions: [today, d1, d2]
            },
            {
                id: 102,
                name: 'Read 20 Pages',
                category: 'Productivity',
                frequency: 'DAILY',
                startDate: d3,
                completions: [today, d1, d2, d3]
            },
            {
                id: 103,
                name: 'Drink 2.5L Water',
                category: 'Health',
                frequency: 'DAILY',
                startDate: d2,
                completions: [today, d1]
            },
            {
                id: 104,
                name: 'Weekly Planning & Review',
                category: 'Work',
                frequency: 'WEEKLY',
                startDate: d3,
                completions: [today]
            }
        ];
        defaultList.forEach(recalculateHabitStats);
        return defaultList;
    }

    function getStoredHabits() {
        const raw = localStorage.getItem('pg_demo_habits');
        if (raw) {
            try { return JSON.parse(raw); } catch(e) {}
        }
        const defaults = getDefaultHabits();
        saveStoredHabits(defaults);
        return defaults;
    }

    function saveStoredHabits(list) {
        localStorage.setItem('pg_demo_habits', JSON.stringify(list));
    }

    // False on GitHub Pages and for offline demo sign-ins: those only use localStorage.
    function usesServer() {
        return !isGitHubPages && !localStorage.getItem('progressgrid_token')?.startsWith('demo-');
    }

    // One place for the session token, the request timeout and what a 401 means.
    // Resolves to the response, or null when there's no server to ask or it can't be reached.
    async function api(path, options = {}) {
        if (!usesServer()) {
            return null;
        }
        try {
            const res = await fetch(API_URL + path, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('progressgrid_token'),
                    // So the server checks ticks against the user's today, not its own.
                    'X-Timezone': Intl.DateTimeFormat().resolvedOptions().timeZone || ''
                },
                signal: AbortSignal.timeout(3500)
            });
            if (res.status === 401) {
                // Missing or expired session: sign in again.
                localStorage.removeItem('progressgrid_token');
                window.location.href = 'login.html';
            }
            return res;
        } catch (err) {
            return null;
        }
    }

    function resolveUserDisplayName(username, fullName, email) {
        // 1. If explicit fullName exists and isn't just an email address
        if (fullName && typeof fullName === 'string' && !fullName.includes('@') && fullName.trim().toLowerCase() !== 'user') {
            return fullName.trim();
        }

        // 2. Derive from username if it's not an email
        if (username && typeof username === 'string' && !username.includes('@') && username.trim().toLowerCase() !== 'user') {
            const clean = username.trim();
            return clean.replace(/[._\-]/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
        }

        // 3. If username or email is an email address (e.g. jane.doe42@example.com)
        const emailToInspect = (username && username.includes('@')) ? username : (email || '');
        if (emailToInspect) {
            const localPart = emailToInspect.split('@')[0];
            const cleaned = localPart.replace(/\d+$/, '').replace(/[._\-]/g, ' ').trim();
            if (cleaned) {
                return cleaned.replace(/\b\w/g, c => c.toUpperCase());
            }
        }

        return 'User';
    }

    function initProfileData() {
        const storedRawUsername = localStorage.getItem('username') || '';
        const storedRawFullName = localStorage.getItem('userFullName') || '';
        const storedEmail = localStorage.getItem('userEmail') || localStorage.getItem('email') || (storedRawUsername.includes('@') ? storedRawUsername : '');
        const storedMobile = localStorage.getItem('userMobile') || '';
        const storedAvatar = localStorage.getItem('userAvatar');

        const cleanDisplayName = resolveUserDisplayName(storedRawUsername, storedRawFullName, storedEmail);
        
        let cleanUsername = storedRawUsername;
        if (!cleanUsername || cleanUsername.includes('@') || cleanUsername.toLowerCase() === 'user') {
            cleanUsername = cleanDisplayName.toLowerCase().replace(/\s+/g, '');
        }

        // Clean up stored values in localStorage so on refresh everything stays clean
        if (!storedRawFullName || storedRawFullName.includes('@') || storedRawFullName.toLowerCase() === 'user') {
            localStorage.setItem('userFullName', cleanDisplayName);
        }
        if (!storedRawUsername || storedRawUsername.includes('@') || storedRawUsername.toLowerCase() === 'user') {
            localStorage.setItem('username', cleanUsername);
        }
        if (storedEmail && !localStorage.getItem('userEmail')) {
            localStorage.setItem('userEmail', storedEmail);
        }

        if (profileNameInput) profileNameInput.value = cleanDisplayName;
        if (profileUsernameInput) profileUsernameInput.value = cleanUsername;
        if (profileEmailInput) profileEmailInput.value = storedEmail;
        if (profileMobileInput) profileMobileInput.value = storedMobile;

        if (sidebarUsername) sidebarUsername.innerText = cleanDisplayName;
        renderAvatar(storedAvatar, cleanDisplayName);
    }

    function renderAvatar(photoDataUrl, fallbackName) {
        const nameToUse = fallbackName || (profileNameInput ? profileNameInput.value : '') || localStorage.getItem('userFullName') || localStorage.getItem('username') || 'User';
        const initial = (nameToUse.trim().charAt(0) || 'U').toUpperCase();

        if (photoDataUrl) {
            // Large Avatar in Profile View
            if (largeAvatarImg) {
                largeAvatarImg.src = photoDataUrl;
                largeAvatarImg.style.display = 'block';
            }
            if (largeAvatarInitial) largeAvatarInitial.style.display = 'none';

            // Sidebar Avatar in Left Corner
            if (sidebarAvatarImg) {
                sidebarAvatarImg.src = photoDataUrl;
                sidebarAvatarImg.style.display = 'block';
            }
            if (sidebarAvatarText) sidebarAvatarText.style.display = 'none';

            if (triggerRemoveBtn) triggerRemoveBtn.style.display = 'inline-flex';
        } else {
            // No photo, use initial
            if (largeAvatarImg) {
                largeAvatarImg.src = '';
                largeAvatarImg.style.display = 'none';
            }
            if (largeAvatarInitial) {
                largeAvatarInitial.innerText = initial;
                largeAvatarInitial.style.display = 'block';
            }

            if (sidebarAvatarImg) {
                sidebarAvatarImg.src = '';
                sidebarAvatarImg.style.display = 'none';
            }
            if (sidebarAvatarText) {
                sidebarAvatarText.innerText = initial;
                sidebarAvatarText.style.display = 'block';
            }

            if (triggerRemoveBtn) triggerRemoveBtn.style.display = 'none';
        }
    }

    function attachEventListeners() {
        // Tab Navigation
        function showDashboard() {
            if (dashboardView) dashboardView.style.display = 'block';
            if (statsView) statsView.style.display = 'none';
            if (profileView) profileView.style.display = 'none';
            if (calendarView) calendarView.style.display = 'none';
            document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
            if (navDashboard) navDashboard.classList.add('active');
        }

        function showProfile() {
            if (dashboardView) dashboardView.style.display = 'none';
            if (statsView) statsView.style.display = 'none';
            if (calendarView) calendarView.style.display = 'none';
            if (profileView) profileView.style.display = 'block';
            document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
            if (navSettings) navSettings.classList.add('active');
            initProfileData();
        }

        function showStats() {
            if (dashboardView) dashboardView.style.display = 'none';
            if (profileView) profileView.style.display = 'none';
            if (calendarView) calendarView.style.display = 'none';
            if (statsView) statsView.style.display = 'block';
            document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
            if (navStats) navStats.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
            renderStatsView(currentStatsTimeframe);
        }

        function showCalendar() {
            if (dashboardView) dashboardView.style.display = 'none';
            if (profileView) profileView.style.display = 'none';
            if (statsView) statsView.style.display = 'none';
            if (calendarView) calendarView.style.display = 'block';
            document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
            if (navCalendar) navCalendar.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
            initCalendar();
        }

        // When pressing on their name or profile details in the left corner
        if (userProfileBtn) {
            userProfileBtn.addEventListener('click', showProfile);
        }
        if (navSettings) {
            navSettings.addEventListener('click', (e) => {
                e.preventDefault();
                showProfile();
            });
        }
        if (backToDashboardBtn) {
            backToDashboardBtn.addEventListener('click', () => {
                showDashboard();
            });
        }
        if (statsBackToDashboardBtn) {
            statsBackToDashboardBtn.addEventListener('click', () => {
                showDashboard();
            });
        }
        if (goToStatsBtn) {
            goToStatsBtn.addEventListener('click', () => {
                showStats();
            });
        }

        // Sidebar Nav links: switch views and smoothly scroll to active sections
        if (navDashboard) {
            navDashboard.addEventListener('click', (e) => {
                e.preventDefault();
                showDashboard();
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        }

        if (navHabits) {
            navHabits.addEventListener('click', (e) => {
                e.preventDefault();
                showDashboard();
                const gridSection = document.querySelector('.habit-grid-section');
                if (gridSection) gridSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
            });
        }

        if (navStats) {
            navStats.addEventListener('click', (e) => {
                e.preventDefault();
                showStats();
            });
        }

        if (navCalendar) {
            navCalendar.addEventListener('click', (e) => {
                e.preventDefault();
                showCalendar();
            });
        }

        const statsTimeframeSelector = document.getElementById('statsTimeframeSelector');
        if (statsTimeframeSelector) {
            statsTimeframeSelector.addEventListener('click', (e) => {
                const btn = e.target.closest('.pill-btn');
                if (!btn) return;
                statsTimeframeSelector.querySelectorAll('.pill-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentStatsTimeframe = btn.dataset.timeframe;
                renderStatsView(currentStatsTimeframe);
            });
        }

        const statsHabitSearch = document.getElementById('statsHabitSearch');
        if (statsHabitSearch) {
            statsHabitSearch.addEventListener('input', (e) => {
                filterLeaderboardTable(e.target.value.trim().toLowerCase());
            });
        }

        // Photo Upload via File Explorer
        if (profileAvatarLarge) {
            profileAvatarLarge.addEventListener('click', () => {
                if (photoFileInput) photoFileInput.click();
            });
        }
        if (triggerBrowseBtn) {
            triggerBrowseBtn.addEventListener('click', () => {
                if (photoFileInput) photoFileInput.click();
            });
        }

        // File selection from File Explorer
        if (photoFileInput) {
            photoFileInput.addEventListener('change', (e) => {
                const file = e.target.files && e.target.files[0];
                if (file) {
                    if (!file.type.startsWith('image/')) {
                        showProfileToast('Please select a valid image file (PNG, JPG, JPEG, WEBP)', 'error');
                        return;
                    }
                    const reader = new FileReader();
                    reader.onload = function(event) {
                        const dataUrl = event.target.result;
                        localStorage.setItem('userAvatar', dataUrl);
                        renderAvatar(dataUrl);
                        showProfileToast('Profile picture updated successfully!', 'success');
                    };
                    reader.readAsDataURL(file);
                }
            });
        }

        // Remove Photo
        if (triggerRemoveBtn) {
            triggerRemoveBtn.addEventListener('click', () => {
                localStorage.removeItem('userAvatar');
                if (photoFileInput) photoFileInput.value = '';
                renderAvatar(null);
                showProfileToast('Profile picture removed.', 'info');
            });
        }

        // Save Profile Details
        if (profileDetailsForm) {
            profileDetailsForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const newName = profileNameInput.value.trim();
                const newUsername = profileUsernameInput.value.trim();
                const newEmail = profileEmailInput.value.trim();
                const newMobile = profileMobileInput.value.trim();

                localStorage.setItem('userFullName', newName);
                localStorage.setItem('username', newUsername);
                localStorage.setItem('userEmail', newEmail);
                localStorage.setItem('userMobile', newMobile);

                const displayName = newName || newUsername;
                if (sidebarUsername) sidebarUsername.innerText = displayName;
                renderAvatar(localStorage.getItem('userAvatar'), displayName);

                showProfileToast('Profile details saved successfully!', 'success');
            });
        }

        // Clear Fields
        if (clearFieldsBtn) {
            clearFieldsBtn.addEventListener('click', () => {
                if (profileNameInput) profileNameInput.value = '';
                if (profileUsernameInput) profileUsernameInput.value = '';
                if (profileEmailInput) profileEmailInput.value = '';
                if (profileMobileInput) profileMobileInput.value = '';
                showProfileToast('Fields cleared. Click "Save Changes" if you wish to persist.', 'info');
            });
        }

        function showProfileToast(message, type) {
            if (!profileAlertMsg) return;
            profileAlertMsg.innerText = message;
            profileAlertMsg.className = `profile-alert ${type || 'success'}`;
            profileAlertMsg.style.display = 'block';
            setTimeout(() => {
                if (profileAlertMsg) profileAlertMsg.style.display = 'none';
            }, 3500);
        }

        // Safe Logout handler if element exists
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                localStorage.removeItem('progressgrid_token');
                localStorage.removeItem('username');
                localStorage.removeItem('email');
                window.location.href = 'login.html?logout=true';
            });
        }

        // Modal
        addHabitBtn.addEventListener('click', () => {
            document.getElementById('habitStartDate').value = formatDateIso(new Date());
            addHabitModal.classList.add('active');
        });
        closeHabitModal.addEventListener('click', () => addHabitModal.classList.remove('active'));
        cancelHabitBtn.addEventListener('click', () => addHabitModal.classList.remove('active'));

        // Form
        addHabitForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const habit = {
                name: document.getElementById('habitName').value.trim(),
                category: document.getElementById('habitCategory').value,
                frequency: document.getElementById('habitFrequency').value,
                startDate: document.getElementById('habitStartDate').value || formatDateIso(new Date())
            };
            if (!habit.name) return;

            const res = await api('', { method: 'POST', body: JSON.stringify(habit) });
            if (!res || !res.ok) {
                const current = getStoredHabits();
                const newHabit = {
                    id: Date.now(),
                    name: habit.name,
                    category: habit.category,
                    frequency: habit.frequency,
                    startDate: habit.startDate,
                    completions: []
                };
                recalculateHabitStats(newHabit);
                current.push(newHabit);
                saveStoredHabits(current);
            }

            addHabitForm.reset();
            addHabitModal.classList.remove('active');
            fetchHabits();
        });

        // Ticking a day or deleting a habit
        habitTableBody.addEventListener('click', async (e) => {
            const deleteBtn = e.target.closest('.btn-delete-habit');
            if (deleteBtn) {
                const habitId = Number(deleteBtn.dataset.habitId);
                const habit = habits.find(h => h.id === habitId);
                const habitName = habit ? habit.name : 'this habit';
                if (!confirm(`Are you sure you want to delete "${habitName}"?`)) return;

                habits = habits.filter(h => h.id !== habitId);
                const currentList = getStoredHabits().filter(h => h.id !== habitId);
                saveStoredHabits(currentList);
                renderDashboard();

                await api(`/${habitId}`, { method: 'DELETE' });
                return;
            }

            const cell = e.target.closest('td[data-habit]');
            if (!cell) return;
            const habit = habits.find(h => h.id === Number(cell.dataset.habit));
            if (!habit) return;
            const date = cell.dataset.date;
            const completed = !habit.completionsSet.has(date);

            // Show the tick straight away, then update storage & server
            if (completed) habit.completionsSet.add(date);
            else habit.completionsSet.delete(date);
            habit.completions = Array.from(habit.completionsSet);

            // Update in stored list for client-side demo resilience
            const currentList = getStoredHabits();
            const storedIndex = currentList.findIndex(h => h.id === habit.id);
            if (storedIndex !== -1) {
                currentList[storedIndex].completions = habit.completions;
                recalculateHabitStats(currentList[storedIndex]);
                habit.currentStreak = currentList[storedIndex].currentStreak;
                habit.bestStreak = currentList[storedIndex].bestStreak;
                habit.completionPercentage = currentList[storedIndex].completionPercentage;
                habit.completedDays = currentList[storedIndex].completedDays;
                saveStoredHabits(currentList);
            } else {
                recalculateHabitStats(habit);
            }

            renderDashboard();
            await api(`/${habit.id}/complete`, { method: 'POST', body: JSON.stringify({ date, completed }) });
        });

        // Navigation
        prevWeekBtn.addEventListener('click', () => {
            currentWeekStart.setDate(currentWeekStart.getDate() - 7);
            renderDashboard();
        });
        nextWeekBtn.addEventListener('click', () => {
            currentWeekStart.setDate(currentWeekStart.getDate() + 7);
            renderDashboard();
        });
    }

    async function fetchHabits() {
        const res = await api('');
        const serverHabits = res && res.ok ? await res.json().catch(() => null) : null;
        if (Array.isArray(serverHabits)) {
            habits = serverHabits;
            saveStoredHabits(habits);
        } else {
            // No server (GitHub Pages / demo sign-in) or it couldn't be reached.
            habits = getStoredHabits();
        }

        habits.forEach(h => {
            h.completionsSet = new Set(h.completions || []);
            h.start = String(h.startDate || formatDateIso(new Date())).split('T')[0];
            recalculateHabitStats(h);
        });
        if (!loadedFromServer) {
            saveStoredHabits(habits);
        }
        renderDashboard();
        if (statsView && statsView.style.display !== 'none') {
            renderStatsView(currentStatsTimeframe);
        }
    }

    function renderDashboard() {
        updateDateHeaders();
        renderGrid();
        renderSummaries();
        renderTodayHabits();
        renderPerformance();
        renderChart();
    }

    function updateDateHeaders() {
        const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        document.getElementById('currentDateDisplay').innerText = `${monthNames[currentWeekStart.getMonth()]} ${currentWeekStart.getFullYear()}`;

        const endOfWeek = new Date(currentWeekStart);
        endOfWeek.setDate(endOfWeek.getDate() + 6);
        document.getElementById('currentWeekLabel').innerText = `${formatShortDate(currentWeekStart)} - ${formatShortDate(endOfWeek)}`;
    }

    function renderGrid() {
        const days = weekDates(currentWeekStart);
        const today = formatDateIso(new Date());
        const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        dayHeaderRow.innerHTML = '<th>Habit</th>' + days.map((iso, i) => `<th>${dayNames[i]}<br><small>${Number(iso.slice(8))}</small></th>`).join('');

        if (habits.length === 0) {
            habitTableBody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding: 20px;">No habits yet. Add one above!</td></tr>';
            return;
        }

        habitTableBody.innerHTML = habits.map(habit => {
            const startLabel = formatShortDate(new Date(habit.start + 'T00:00:00'));
            const cells = days.map(iso => {
                if (iso < habit.start) {
                    return `<td class="check-cell not-started" title="Habit starts on ${startLabel}"><span class="not-started-dash" aria-label="Not started yet">—</span></td>`;
                }
                if (iso > today) {
                    return `<td class="check-cell not-started" title="Can't tick a day that hasn't happened yet"></td>`;
                }
                const checked = habit.completionsSet.has(iso) ? 'checked' : '';
                return `<td class="check-cell ${checked}" data-habit="${habit.id}" data-date="${iso}" title="${iso}"><div class="check-box-inner"></div></td>`;
            }).join('');

            return `<tr class="habit-row">
                <td class="habit-name">
                    <div class="habit-name-wrapper">
                        <div class="habit-name-text">
                            <strong>${esc(habit.name)}</strong><br>
                            <small style="color:var(--text-muted)">${esc(habit.category)} • ${isWeekly(habit) ? 'Weekly' : 'Daily'} • From ${startLabel}</small>
                        </div>
                        <button class="btn-delete-habit" data-habit-id="${habit.id}" title="Delete ${esc(habit.name)}">×</button>
                    </div>
                </td>${cells}</tr>`;
        }).join('');
    }

    function renderSummaries() {
        const days = weekDates(currentWeekStart);
        const today = formatDateIso(new Date());

        document.getElementById('sumTotalHabits').innerText = habits.length;
        document.getElementById('sumCurrentStreak').innerText = longestStreak('currentStreak');
        document.getElementById('sumBestStreak').innerText = longestStreak('bestStreak');

        // This week so far: a daily habit can be done once for each day since it started,
        // a weekly habit once for the whole week.
        let done = 0;
        let possible = 0;
        habits.forEach(h => {
            if (isWeekly(h)) {
                if (h.start > days[6] || days[0] > today) return;
                possible++;
                if (days.some(d => h.completionsSet.has(d))) done++;
            } else {
                days.filter(d => d >= h.start && d <= today).forEach(d => {
                    possible++;
                    if (h.completionsSet.has(d)) done++;
                });
            }
        });
        document.getElementById('sumWeeklyProgress').innerText = `${possible ? Math.round(done * 100 / possible) : 0}%`;

        // Average of each habit's completion since its start date, worked out on the server.
        const overall = habits.length
            ? Math.round(habits.reduce((sum, h) => sum + (h.completionPercentage || 0), 0) / habits.length)
            : 0;
        document.getElementById('sumMonthlyProgress').innerText = `${overall}%`;
    }

    // The longest current or best streak across all habits, in that habit's own unit.
    function longestStreak(field) {
        const top = habits.reduce((best, h) => ((h[field] || 0) > (best ? best[field] || 0 : 0) ? h : best), null);
        return top ? streakText(top, top[field]) : '0 days';
    }

    function streakText(habit, n) {
        const unit = isWeekly(habit) ? 'week' : 'day';
        return `${n} ${unit}${n === 1 ? '' : 's'}`;
    }

    function renderTodayHabits() {
        const today = formatDateIso(new Date());
        const thisWeek = weekDates(getMonday(new Date()));

        const html = habits.filter(h => h.start <= today).map(h => {
            const weekly = isWeekly(h);
            const done = weekly ? thisWeek.some(d => h.completionsSet.has(d)) : h.completionsSet.has(today);
            const status = done ? (weekly ? '✓ Done this week' : '✓ Done') : (weekly ? 'This week' : 'Pending');
            return `
                <div class="today-habit-item ${done ? 'completed' : ''}">
                    <div class="habit-title">
                        <span style="display:inline-block; width:10px; height:10px; background:var(--primary-color); border-radius:50%; margin-right:8px;"></span>
                        ${esc(h.name)}
                    </div>
                    <div class="habit-status">${status}</div>
                </div>`;
        }).join('');
        document.getElementById('todayHabitsList').innerHTML = html || '<p class="text-muted">No habits scheduled.</p>';
    }

    function renderPerformance() {
        const top = [...habits].sort((a, b) => (b.completionPercentage || 0) - (a.completionPercentage || 0)).slice(0, 5);
        document.getElementById('topHabitsList').innerHTML = top.map(h => `
            <div class="stat-item">
                <div class="stat-item-info">
                    <h4>${esc(h.name)}</h4>
                    <p>Streak: ${streakText(h, h.currentStreak || 0)} | Best: ${streakText(h, h.bestStreak || 0)}</p>
                </div>
                <div class="stat-value">${h.completionPercentage || 0}%</div>
            </div>`).join('') || '<p>No data yet.</p>';
    }

    // Color mapping for weekly progress graph based on completion percentage tiers
    function getWeeklyProgressColor(pct) {
        if (pct >= 100) return { bg: 'rgba(16, 185, 129, 0.85)', border: '#059669', hover: '#047857' }; // 100% Done: Vibrant Emerald Green
        if (pct >= 75)  return { bg: 'rgba(132, 204, 22, 0.85)',  border: '#65a30d', hover: '#4d7c0f' }; // 75%+: Fresh Lime Green
        if (pct >= 50)  return { bg: 'rgba(245, 158, 11, 0.85)',  border: '#d97706', hover: '#b45309' }; // 50%+: Warm Amber Gold
        if (pct >= 25)  return { bg: 'rgba(249, 115, 22, 0.85)',  border: '#ea580c', hover: '#c2410c' }; // 25%+: Vibrant Orange
        if (pct > 0)    return { bg: 'rgba(239, 68, 68, 0.85)',   border: '#dc2626', hover: '#b91c1c' }; // <25%: Soft Crimson / Red
        return { bg: 'rgba(203, 213, 225, 0.35)', border: 'rgba(148, 163, 184, 0.5)', hover: 'rgba(148, 163, 184, 0.6)' }; // 0%: Subtle Slate
    }

    function renderChart() {
        const canvas = document.getElementById('weeklyChart');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');

        const labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        const days = weekDates(currentWeekStart);
        const today = formatDateIso(new Date());

        // Each bar is the share of daily habits (already started) ticked that day. Weekly habits
        // aren't due on any particular day, so they stay out of the daily bars.
        const due = days.map(iso => habits.filter(h => !isWeekly(h) && h.start <= iso));
        const dayCounts = days.map((iso, i) => due[i].filter(h => h.completionsSet.has(iso)).length);
        const dayTotals = due.map(list => list.length);
        const dataPoints = dayCounts.map((done, i) => (dayTotals[i] ? Math.round(done * 100 / dayTotals[i]) : 0));

        const bgColors = dataPoints.map(p => getWeeklyProgressColor(p).bg);
        const borderColors = dataPoints.map(p => getWeeklyProgressColor(p).border);
        const hoverColors = dataPoints.map(p => getWeeklyProgressColor(p).hover);

        // Average Badge: only days that have happened and had something due.
        const counted = dataPoints.filter((_, i) => days[i] <= today && dayTotals[i] > 0);
        const avgPct = counted.length ? Math.round(counted.reduce((a, b) => a + b, 0) / counted.length) : 0;
        const avgBadge = document.getElementById('weeklyAvgBadge');
        if (avgBadge) {
            const avgColor = getWeeklyProgressColor(avgPct);
            avgBadge.innerText = `${avgPct}% Avg`;
            avgBadge.style.backgroundColor = avgColor.bg.replace('0.85', '0.15');
            avgBadge.style.color = avgColor.border;
            avgBadge.style.borderColor = avgColor.border;
        }

        if (weeklyChartInstance) {
            weeklyChartInstance.dayCounts = dayCounts;
            weeklyChartInstance.dayTotals = dayTotals;
            weeklyChartInstance.data.labels = labels;
            weeklyChartInstance.data.datasets[0].data = dataPoints;
            weeklyChartInstance.data.datasets[0].backgroundColor = bgColors;
            weeklyChartInstance.data.datasets[0].borderColor = borderColors;
            weeklyChartInstance.data.datasets[0].hoverBackgroundColor = hoverColors;
            weeklyChartInstance.update();
        } else {
            weeklyChartInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Completion %',
                        data: dataPoints,
                        backgroundColor: bgColors,
                        borderColor: borderColors,
                        hoverBackgroundColor: hoverColors,
                        borderWidth: 2,
                        borderRadius: 6,
                        borderSkipped: false
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: {
                        duration: 400
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: 'rgba(15, 23, 42, 0.92)',
                            titleColor: '#f8fafc',
                            titleFont: { family: "'Inter', sans-serif", weight: '600', size: 13 },
                            bodyColor: '#e2e8f0',
                            bodyFont: { family: "'Inter', sans-serif", size: 12 },
                            padding: 12,
                            boxPadding: 6,
                            cornerRadius: 8,
                            borderColor: 'rgba(255, 255, 255, 0.1)',
                            borderWidth: 1,
                            callbacks: {
                                label: function(context) {
                                    const index = context.dataIndex;
                                    const pct = context.parsed.y;
                                    const done = weeklyChartInstance.dayCounts[index] || 0;
                                    const total = weeklyChartInstance.dayTotals[index] || 0;

                                    if (pct >= 100) return ` ${pct}% Complete (${done}/${total}) — 100% Done! 🎉`;
                                    if (pct >= 75)  return ` ${pct}% Complete (${done}/${total}) — Almost done! 🌟`;
                                    if (pct >= 50)  return ` ${pct}% Complete (${done}/${total}) — 50%+ Halfway! ⚡`;
                                    if (pct >= 25)  return ` ${pct}% Complete (${done}/${total}) — 25%+ Progress! 💪`;
                                    if (pct > 0)    return ` ${pct}% Complete (${done}/${total}) — Started 🔥`;
                                    return ` 0% Complete (${done}/${total}) — No habits done`;
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            min: 0,
                            max: 100,
                            ticks: {
                                stepSize: 25,
                                callback: function(val) { return val + '%'; },
                                font: {
                                    family: "'Inter', sans-serif",
                                    size: 11
                                },
                                color: '#94a3b8'
                            },
                            grid: {
                                color: 'rgba(226, 232, 240, 0.6)',
                                drawBorder: false
                            }
                        },
                        x: {
                            ticks: {
                                font: {
                                    family: "'Inter', sans-serif",
                                    size: 12,
                                    weight: '600'
                                },
                                color: '#475569'
                            },
                            grid: {
                                display: false
                            }
                        }
                    }
                }
            });
            weeklyChartInstance.dayCounts = dayCounts;
            weeklyChartInstance.dayTotals = dayTotals;
        }
    }

    // ==========================================================================
    // STATISTICS & ANALYTICS VIEW LOGIC
    // ==========================================================================

    function renderStatsView(timeframe = '30days') {
        if (!statsView) return;

        const todayObj = new Date();
        const todayIso = formatDateIso(todayObj);

        // Determine timeframe date range
        let startDateObj = new Date(todayObj);
        if (timeframe === 'week') {
            startDateObj.setDate(todayObj.getDate() - 6);
        } else if (timeframe === 'month') {
            startDateObj = new Date(todayObj.getFullYear(), todayObj.getMonth(), 1);
        } else if (timeframe === '30days') {
            startDateObj.setDate(todayObj.getDate() - 29);
        } else if (timeframe === 'all') {
            let earliest = todayIso;
            habits.forEach(h => {
                if (h.start && h.start < earliest) earliest = h.start;
                if (h.completions) {
                    h.completions.forEach(c => {
                        if (c < earliest) earliest = c;
                    });
                }
            });
            startDateObj = new Date(earliest + 'T00:00:00');
            const minDaysAgo = new Date(todayObj);
            minDaysAgo.setDate(minDaysAgo.getDate() - 14);
            if (startDateObj > minDaysAgo) startDateObj = minDaysAgo;
        }

        // Generate date list for range
        const dateList = [];
        const iter = new Date(startDateObj);
        while (formatDateIso(iter) <= todayIso) {
            dateList.push(formatDateIso(iter));
            iter.setDate(iter.getDate() + 1);
        }

        // 1. KPI Calculations
        let totalCheckIns = 0;
        let currentStreakMax = 0;
        let currentStreakHabit = 'None';
        let bestStreakMax = 0;
        let bestStreakHabit = 'None';

        habits.forEach(h => {
            const count = h.completions ? h.completions.length : 0;
            totalCheckIns += count;

            if ((h.currentStreak || 0) > currentStreakMax) {
                currentStreakMax = h.currentStreak || 0;
                currentStreakHabit = h.name;
            }
            if ((h.bestStreak || 0) > bestStreakMax) {
                bestStreakMax = h.bestStreak || 0;
                bestStreakHabit = h.name;
            }
        });

        let totalDue = 0;
        let totalDone = 0;
        const dailyRateMap = {};

        dateList.forEach(iso => {
            const dueHabits = habits.filter(h => !isWeekly(h) && h.start <= iso);
            const doneHabits = dueHabits.filter(h => h.completionsSet.has(iso));
            const dueCount = dueHabits.length;
            const doneCount = doneHabits.length;
            const pct = dueCount > 0 ? Math.round((doneCount * 100) / dueCount) : 0;

            dailyRateMap[iso] = { due: dueCount, done: doneCount, pct };
            totalDue += dueCount;
            totalDone += doneCount;
        });

        // Perfect Days
        let perfectDays = 0;
        dateList.forEach(iso => {
            const d = dailyRateMap[iso];
            if (d && d.due > 0 && d.done === d.due) {
                perfectDays++;
            }
        });

        // Overall consistency percentage
        let overallConsistency = totalDue > 0
            ? Math.round((totalDone * 100) / totalDue)
            : (habits.length ? Math.round(habits.reduce((sum, h) => sum + (h.completionPercentage || 0), 0) / habits.length) : 0);

        // Update KPI DOM
        const rateEl = document.getElementById('statsOverallRate');
        if (rateEl) rateEl.innerText = `${overallConsistency}%`;

        const subtextEl = document.getElementById('statsOverallSubtext');
        if (subtextEl) {
            if (overallConsistency >= 80) subtextEl.innerText = 'Exceptional discipline! 🌟';
            else if (overallConsistency >= 60) subtextEl.innerText = 'Solid consistency! Keep it up ⚡';
            else if (overallConsistency >= 40) subtextEl.innerText = 'Building momentum! 🌱';
            else subtextEl.innerText = 'Ready to build new momentum 🎯';
        }

        const currStreakEl = document.getElementById('statsCurrentStreak');
        if (currStreakEl) currStreakEl.innerText = `${currentStreakMax} day${currentStreakMax === 1 ? '' : 's'}`;
        const currStreakHabitEl = document.getElementById('statsCurrentStreakHabit');
        if (currStreakHabitEl) currStreakHabitEl.innerText = currentStreakMax > 0 ? `🔥 ${esc(currentStreakHabit)}` : 'No active streak';

        const bestStreakEl = document.getElementById('statsBestStreak');
        if (bestStreakEl) bestStreakEl.innerText = `${bestStreakMax} day${bestStreakMax === 1 ? '' : 's'}`;
        const bestStreakHabitEl = document.getElementById('statsBestStreakHabit');
        if (bestStreakHabitEl) bestStreakHabitEl.innerText = bestStreakMax > 0 ? `🏆 ${esc(bestStreakHabit)}` : 'Personal record';

        const totalTicksEl = document.getElementById('statsTotalCompletions');
        if (totalTicksEl) totalTicksEl.innerText = totalCheckIns;

        const perfectDaysEl = document.getElementById('statsPerfectDays');
        if (perfectDaysEl) perfectDaysEl.innerText = perfectDays;

        // 2. Render Smart Insights
        renderSmartInsights(dateList, dailyRateMap, habits, overallConsistency, currentStreakMax, currentStreakHabit);

        // 3. Render Consistency Trajectory Chart
        renderStatsTrendChart(dateList, dailyRateMap);

        // 4. Render Category Donut Chart
        renderStatsCategoryChart();

        // 5. Render Day of Week Discipline Chart
        renderStatsDayOfWeekChart(dateList, dailyRateMap);

        // 6. Render Heatmap Matrix
        renderStatsHeatmapMatrix(todayObj);

        // 7. Render Leaderboard Table
        renderLeaderboardTable();
    }

    function renderSmartInsights(dateList, dailyRateMap, habits, overallRate, currentStreak, currentStreakHabit) {
        const grid = document.getElementById('statsInsightsGrid');
        if (!grid) return;

        if (habits.length === 0) {
            grid.innerHTML = `
                <div class="insight-pill">
                    <span class="insight-icon" style="color: #6366f1;">info</span>
                    <div class="insight-content">
                        <strong>Get Started</strong>
                        <p>Create your first habit on the dashboard to start tracking analytics and visual trends.</p>
                    </div>
                </div>`;
            return;
        }

        // Calculate day-of-week stats
        const dowTotals = [0, 0, 0, 0, 0, 0, 0];
        const dowDones = [0, 0, 0, 0, 0, 0, 0];

        dateList.forEach(iso => {
            const d = new Date(iso + 'T00:00:00');
            const dayIdx = d.getDay();
            const r = dailyRateMap[iso];
            if (r) {
                dowTotals[dayIdx] += r.due;
                dowDones[dayIdx] += r.done;
            }
        });

        let bestDowIdx = 1;
        let bestDowRate = -1;
        let worstDowIdx = 0;
        let worstDowRate = 999;

        for (let i = 0; i < 7; i++) {
            if (dowTotals[i] > 0) {
                const rate = Math.round((dowDones[i] * 100) / dowTotals[i]);
                if (rate > bestDowRate) {
                    bestDowRate = rate;
                    bestDowIdx = i;
                }
                if (rate < worstDowRate) {
                    worstDowRate = rate;
                    worstDowIdx = i;
                }
            }
        }

        const fullDayNames = ['Sundays', 'Mondays', 'Tuesdays', 'Wednesdays', 'Thursdays', 'Fridays', 'Saturdays'];
        const peakDayText = bestDowRate >= 0
            ? `Your peak consistency occurs on <strong>${fullDayNames[bestDowIdx]}</strong> with a <strong>${bestDowRate}%</strong> completion rate.`
            : 'Track habits over multiple days to reveal your peak performance patterns.';

        // Top habit
        const topHabit = [...habits].sort((a, b) => (b.completionPercentage || 0) - (a.completionPercentage || 0))[0];
        const championText = topHabit
            ? `<strong>${esc(topHabit.name)}</strong> is leading with <strong>${topHabit.completionPercentage || 0}%</strong> consistency and a ${topHabit.currentStreak || 0}-day streak.`
            : 'Keep ticking habits daily to see your top performing routine.';

        // Actionable optimization tip
        let tipText = 'Consistency compounds! Even completing a single 2-minute habit daily strengthens habit loops.';
        if (worstDowRate < 50 && worstDowRate < bestDowRate) {
            tipText = `Discipline dips slightly on <strong>${fullDayNames[worstDowIdx]}</strong> (${worstDowRate}%). Try setting weekend reminders earlier!`;
        } else if (overallRate >= 75) {
            tipText = 'You are performing in the top tier! Consider adding an incremental habit to challenge yourself.';
        }

        grid.innerHTML = `
            <div class="insight-pill">
                <span class="insight-icon" style="color: #10b981;">trending_up</span>
                <div class="insight-content">
                    <strong>Peak Discipline Day</strong>
                    <p>${peakDayText}</p>
                </div>
            </div>
            <div class="insight-pill">
                <span class="insight-icon" style="color: #f59e0b;">emoji_events</span>
                <div class="insight-content">
                    <strong>Discipline Champion</strong>
                    <p>${championText}</p>
                </div>
            </div>
            <div class="insight-pill">
                <span class="insight-icon" style="color: #6366f1;">psychology</span>
                <div class="insight-content">
                    <strong>Discipline Optimization</strong>
                    <p>${tipText}</p>
                </div>
            </div>`;
    }

    function renderStatsTrendChart(dateList, dailyRateMap) {
        const canvas = document.getElementById('statsTrendChart');
        if (!canvas || typeof Chart === 'undefined') return;

        const ctx = canvas.getContext('2d');
        const labels = dateList.map(iso => {
            const d = new Date(iso + 'T00:00:00');
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
        });
        const dataPoints = dateList.map(iso => (dailyRateMap[iso] ? dailyRateMap[iso].pct : 0));

        // Average badge
        const activeDays = dateList.filter(iso => dailyRateMap[iso] && dailyRateMap[iso].due > 0);
        const avg = activeDays.length
            ? Math.round(activeDays.reduce((sum, iso) => sum + dailyRateMap[iso].pct, 0) / activeDays.length)
            : 0;

        const avgBadge = document.getElementById('statsTrendAvgBadge');
        if (avgBadge) {
            avgBadge.innerText = `${avg}% Avg`;
            const color = getWeeklyProgressColor(avg);
            avgBadge.style.backgroundColor = color.bg.replace('0.85', '0.15');
            avgBadge.style.color = color.border;
            avgBadge.style.borderColor = color.border;
        }

        // Gradient for line fill
        const gradient = ctx.createLinearGradient(0, 0, 0, 240);
        gradient.addColorStop(0, 'rgba(16, 185, 129, 0.35)');
        gradient.addColorStop(1, 'rgba(16, 185, 129, 0.00)');

        if (statsTrendChartInstance) {
            statsTrendChartInstance.destroy();
            statsTrendChartInstance = null;
        }

        statsTrendChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Completion %',
                    data: dataPoints,
                    borderColor: '#10b981',
                    borderWidth: 2.5,
                    backgroundColor: gradient,
                    fill: true,
                    tension: 0.35,
                    pointBackgroundColor: '#10b981',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 1.5,
                    pointRadius: dateList.length > 20 ? 2 : 4,
                    pointHoverRadius: 6,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 400 },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(15, 23, 42, 0.92)',
                        titleColor: '#f8fafc',
                        bodyColor: '#e2e8f0',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: function(context) {
                                const idx = context.dataIndex;
                                const iso = dateList[idx];
                                const item = dailyRateMap[iso];
                                const done = item ? item.done : 0;
                                const due = item ? item.due : 0;
                                return ` ${context.parsed.y}% (${done}/${due} habits completed)`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: {
                            color: '#94a3b8',
                            font: { family: "'Inter', sans-serif", size: 11 },
                            maxTicksLimit: 10
                        }
                    },
                    y: {
                        beginAtZero: true,
                        max: 100,
                        grid: { color: '#f1f5f9' },
                        ticks: {
                            callback: val => `${val}%`,
                            color: '#94a3b8',
                            font: { family: "'Inter', sans-serif", size: 11 },
                            stepSize: 25
                        }
                    }
                }
            }
        });
    }

    function renderStatsCategoryChart() {
        const canvas = document.getElementById('statsCategoryChart');
        if (!canvas || typeof Chart === 'undefined') return;

        const ctx = canvas.getContext('2d');
        const catMap = {};
        habits.forEach(h => {
            const cat = h.category || 'General';
            catMap[cat] = (catMap[cat] || 0) + (h.completions ? h.completions.length : 1);
        });

        const categories = Object.keys(catMap);
        const dataValues = categories.map(c => catMap[c]);
        const colors = ['#10b981', '#6366f1', '#f59e0b', '#ec4899', '#06b6d4', '#8b5cf6', '#f97316', '#14b8a6'];

        const legendContainer = document.getElementById('statsCategoryLegend');
        if (legendContainer) {
            legendContainer.innerHTML = categories.map((cat, idx) => `
                <span class="cat-legend-item">
                    <span class="cat-legend-dot" style="background: ${colors[idx % colors.length]};"></span>
                    <strong>${esc(cat)}</strong>: ${catMap[cat]} ticks
                </span>
            `).join('') || '<span class="text-muted">No categories available</span>';
        }

        if (statsCategoryChartInstance) {
            statsCategoryChartInstance.destroy();
            statsCategoryChartInstance = null;
        }

        if (categories.length === 0) return;

        statsCategoryChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: categories,
                datasets: [{
                    data: dataValues,
                    backgroundColor: categories.map((_, i) => colors[i % colors.length]),
                    borderWidth: 2,
                    borderColor: '#ffffff',
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(15, 23, 42, 0.92)',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: function(context) {
                                return ` ${context.label}: ${context.parsed} completions`;
                            }
                        }
                    }
                }
            }
        });
    }

    function renderStatsDayOfWeekChart(dateList, dailyRateMap) {
        const canvas = document.getElementById('statsDayOfWeekChart');
        if (!canvas || typeof Chart === 'undefined') return;

        const ctx = canvas.getContext('2d');
        const dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        const dueTotals = [0, 0, 0, 0, 0, 0, 0];
        const doneTotals = [0, 0, 0, 0, 0, 0, 0];

        dateList.forEach(iso => {
            const d = new Date(iso + 'T00:00:00');
            const dayNum = d.getDay();
            const idx = dayNum === 0 ? 6 : dayNum - 1;
            const r = dailyRateMap[iso];
            if (r) {
                dueTotals[idx] += r.due;
                doneTotals[idx] += r.done;
            }
        });

        const dayPcts = dueTotals.map((due, i) => (due > 0 ? Math.round((doneTotals[i] * 100) / due) : 0));
        const bgColors = dayPcts.map(p => getWeeklyProgressColor(p).bg);
        const borderColors = dayPcts.map(p => getWeeklyProgressColor(p).border);

        if (statsDayOfWeekChartInstance) {
            statsDayOfWeekChartInstance.destroy();
            statsDayOfWeekChartInstance = null;
        }

        statsDayOfWeekChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: dayLabels,
                datasets: [{
                    label: 'Discipline %',
                    data: dayPcts,
                    backgroundColor: bgColors,
                    borderColor: borderColors,
                    borderWidth: 1.5,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(15, 23, 42, 0.92)',
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                            label: function(context) {
                                const idx = context.dataIndex;
                                return ` ${context.parsed.y}% completed (${doneTotals[idx]}/${dueTotals[idx]} habits)`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: '#94a3b8', font: { family: "'Inter', sans-serif", size: 11 } }
                    },
                    y: {
                        beginAtZero: true,
                        max: 100,
                        grid: { color: '#f1f5f9' },
                        ticks: {
                            callback: val => `${val}%`,
                            color: '#94a3b8',
                            font: { family: "'Inter', sans-serif", size: 11 },
                            stepSize: 25
                        }
                    }
                }
            }
        });
    }

    function renderStatsHeatmapMatrix(todayObj) {
        const container = document.getElementById('statsHeatmapGrid');
        if (!container) return;

        const currentMonday = getMonday(todayObj);
        const startMonday = new Date(currentMonday);
        startMonday.setDate(startMonday.getDate() - 28);

        const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        const rows = days.map((dayName, dayIndex) => {
            let cellsHtml = '';
            for (let week = 0; week < 5; week++) {
                const cellDate = new Date(startMonday);
                cellDate.setDate(cellDate.getDate() + (week * 7) + dayIndex);
                const iso = formatDateIso(cellDate);

                if (cellDate > todayObj) {
                    cellsHtml += `<div class="heatmap-cell lvl-0" title="${iso} (Future day)" style="opacity: 0.35;"></div>`;
                    continue;
                }

                const dueHabits = habits.filter(h => !isWeekly(h) && h.start <= iso);
                const doneHabits = dueHabits.filter(h => h.completionsSet.has(iso));
                const total = dueHabits.length;
                const done = doneHabits.length;
                const pct = total > 0 ? Math.round((done * 100) / total) : 0;

                let lvl = 'lvl-0';
                if (pct >= 85) lvl = 'lvl-4';
                else if (pct >= 60) lvl = 'lvl-3';
                else if (pct >= 35) lvl = 'lvl-2';
                else if (pct > 0) lvl = 'lvl-1';

                const tip = `${formatShortDate(cellDate)}: ${done}/${total} habits completed (${pct}%)`;
                cellsHtml += `<div class="heatmap-cell ${lvl}" title="${tip}"></div>`;
            }

            return `
                <div class="heatmap-row">
                    <span class="heatmap-label">${dayName}</span>
                    <div class="heatmap-cells">${cellsHtml}</div>
                </div>`;
        }).join('');

        container.innerHTML = rows || '<p class="text-muted">No activity data available.</p>';
    }

    function renderLeaderboardTable() {
        const tbody = document.getElementById('statsLeaderboardBody');
        if (!tbody) return;

        if (habits.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 25px; color: var(--text-muted);">No habits recorded yet. Add habits to view consistency matrix.</td></tr>';
            return;
        }

        const sorted = [...habits].sort((a, b) => {
            const pctA = a.completionPercentage || 0;
            const pctB = b.completionPercentage || 0;
            if (pctB !== pctA) return pctB - pctA;
            return (b.currentStreak || 0) - (a.currentStreak || 0);
        });

        tbody.innerHTML = sorted.map(h => {
            const pct = h.completionPercentage || 0;
            const streak = h.currentStreak || 0;
            const best = h.bestStreak || 0;
            const checkIns = h.completions ? h.completions.length : 0;

            let badgeHtml = '';
            if (pct >= 80) {
                badgeHtml = '<span class="discipline-badge badge-fire">🔥 On Fire</span>';
            } else if (pct >= 55) {
                badgeHtml = '<span class="discipline-badge badge-steady">⚡ Steady</span>';
            } else if (pct >= 30) {
                badgeHtml = '<span class="discipline-badge badge-emerging">🌱 Emerging</span>';
            } else {
                badgeHtml = '<span class="discipline-badge badge-focus">⚠️ Needs Focus</span>';
            }

            const tierColor = getWeeklyProgressColor(pct).border;

            return `
                <tr class="stats-habit-row" data-habit-name="${esc(h.name).toLowerCase()}" data-category="${esc(h.category || '').toLowerCase()}">
                    <td><strong>${esc(h.name)}</strong></td>
                    <td><span class="cat-legend-item" style="padding: 2px 8px; font-size: 11.5px;">${esc(h.category || 'General')}</span></td>
                    <td>${isWeekly(h) ? 'Weekly' : 'Daily'}</td>
                    <td><span style="font-weight: 600; color: #ea580c;">${streakText(h, streak)}</span></td>
                    <td><span style="font-weight: 600; color: #ca8a04;">${streakText(h, best)}</span></td>
                    <td><strong>${checkIns}</strong></td>
                    <td>
                        <div class="progress-bar-wrap">
                            <div class="mini-progress-track">
                                <div class="mini-progress-fill" style="width: ${pct}%; background: ${tierColor};"></div>
                            </div>
                            <span class="progress-pct-num">${pct}%</span>
                        </div>
                    </td>
                    <td>${badgeHtml}</td>
                </tr>`;
        }).join('');
    }

    function filterLeaderboardTable(query) {
        const rows = document.querySelectorAll('.stats-habit-row');
        rows.forEach(row => {
            const name = row.getAttribute('data-habit-name') || '';
            const cat = row.getAttribute('data-category') || '';
            if (!query || name.includes(query) || cat.includes(query)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    }

    // ==========================================
    // STATS CALCULATION LOGIC
    // ==========================================
    function parseDateParts(d) {
        if (!d) return null;
        if (d instanceof Date) {
            return {
                year: d.getFullYear(),
                month: d.getMonth() + 1,
                day: d.getDate()
            };
        }
        const str = String(d).split('T')[0];
        const parts = str.split('-');
        if (parts.length !== 3) return null;
        const y = parseInt(parts[0], 10);
        const m = parseInt(parts[1], 10);
        const day = parseInt(parts[2], 10);
        if (isNaN(y) || isNaN(m) || isNaN(day)) return null;
        return { year: y, month: m, day };
    }

    // Convert date string or Date object to integer epoch days (UTC midnight based).
    // Pure calendar day integer: immune to local browser timezone or DST shifts.
    function toEpochDays(d) {
        const p = parseDateParts(d);
        if (!p) return null;
        return Math.floor(Date.UTC(p.year, p.month - 1, p.day) / 86400000);
    }

    // For weekly habits, period is Monday of that week
    function periodOfDays(epochDays, weekly) {
        if (!weekly) return epochDays;
        const d = new Date(epochDays * 86400000);
        const dayOfWeek = d.getUTCDay(); // 0 = Sun, 1 = Mon, ..., 6 = Sat
        const diffToMonday = dayOfWeek === 0 ? 6 : (dayOfWeek - 1);
        return epochDays - diffToMonday;
    }

    function recalculateHabitStats(habit) {
        const weekly = isWeekly(habit);
        const step = weekly ? 7 : 1;
        const todayDays = toEpochDays(new Date());
        const startDays = toEpochDays(habit.startDate) ?? todayDays;

        const now = periodOfDays(todayDays, weekly);
        const first = periodOfDays(startDays, weekly);

        const doneSet = new Set();
        (habit.completions || []).forEach(dStr => {
            const ed = toEpochDays(dStr);
            if (ed === null) return;
            const p = periodOfDays(ed, weekly);
            if (p >= first && p <= now) {
                doneSet.add(p);
            }
        });

        const done = Array.from(doneSet).sort((a, b) => a - b);

        let best = 0;
        let run = 0;
        let previous = null;
        for (const period of done) {
            run = (previous !== null && period - step === previous) ? run + 1 : 1;
            best = Math.max(best, run);
            previous = period;
        }

        // Today (or this week) may simply not be done yet, so a run ending one period back still counts.
        let current = 0;
        let p = doneSet.has(now) ? now : now - step;
        while (doneSet.has(p)) {
            current++;
            p -= step;
        }

        const daysBetween = now - first;
        const periods = Math.max(1, Math.floor(daysBetween / step) + 1);
        const completionPercentage = Math.min(100, Math.round((100.0 * done.length) / periods));

        habit.currentStreak = current;
        habit.bestStreak = best;
        habit.completionPercentage = completionPercentage;
        habit.completedDays = (habit.completions || []).length;
    }

    // ==========================================
    // CALENDAR LOGIC
    // ==========================================
    let currentCalDate = new Date();
    let selectedCalDate = new Date();

    function initCalendar() {
        renderCalendar();
        renderDayHabits(selectedCalDate);

        const calPrevMonth = document.getElementById('calPrevMonth');
        const calNextMonth = document.getElementById('calNextMonth');

        if (calPrevMonth && !calPrevMonth.dataset.bound) {
            calPrevMonth.addEventListener('click', () => {
                currentCalDate.setMonth(currentCalDate.getMonth() - 1);
                renderCalendar();
            });
            calPrevMonth.dataset.bound = 'true';
        }

        if (calNextMonth && !calNextMonth.dataset.bound) {
            calNextMonth.addEventListener('click', () => {
                currentCalDate.setMonth(currentCalDate.getMonth() + 1);
                renderCalendar();
            });
            calNextMonth.dataset.bound = 'true';
        }
    }

    function renderCalendar() {
        const calMonthYear = document.getElementById('calMonthYear');
        const calendarGrid = document.querySelector('.calendar-grid');
        if (!calMonthYear || !calendarGrid) return;

        const year = currentCalDate.getFullYear();
        const month = currentCalDate.getMonth();
        
        calMonthYear.innerText = new Date(year, month, 1).toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

        // Remove old days
        const oldDays = calendarGrid.querySelectorAll('.cal-day');
        oldDays.forEach(d => d.remove());

        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const prevMonthDays = new Date(year, month, 0).getDate();

        // Previous month days
        for (let i = firstDay; i > 0; i--) {
            const dayDiv = document.createElement('div');
            dayDiv.className = 'cal-day other-month';
            dayDiv.innerText = prevMonthDays - i + 1;
            calendarGrid.appendChild(dayDiv);
        }

        // Current month days
        const today = new Date();
        for (let i = 1; i <= daysInMonth; i++) {
            const dayDiv = document.createElement('div');
            dayDiv.className = 'cal-day';
            dayDiv.innerText = i;
            
            const cellDate = new Date(year, month, i);
            
            if (cellDate.toDateString() === today.toDateString()) {
                dayDiv.classList.add('today');
            }
            
            if (cellDate.toDateString() === selectedCalDate.toDateString()) {
                dayDiv.classList.add('active');
            }

            dayDiv.addEventListener('click', () => {
                selectedCalDate = cellDate;
                renderCalendar();
                renderDayHabits(selectedCalDate);
            });

            calendarGrid.appendChild(dayDiv);
        }

        // Next month days
        const totalCells = firstDay + daysInMonth;
        const remainingCells = 42 - totalCells; // 6 rows max
        for (let i = 1; i <= remainingCells; i++) {
            const dayDiv = document.createElement('div');
            dayDiv.className = 'cal-day other-month';
            dayDiv.innerText = i;
            calendarGrid.appendChild(dayDiv);
        }
    }

    function renderDayHabits(date) {
        const listDiv = document.getElementById('calDayHabitsList');
        const selectedDateText = document.getElementById('calSelectedDateText');
        if (!listDiv || !selectedDateText) return;

        const dateStr = date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
        const isoDate = formatDateIso(date);
        selectedDateText.innerText = 'Habits for ' + dateStr;

        // Filter habits that started before or on this date
        const activeHabits = habits.filter(h => {
            const startDate = h.startDate || '2000-01-01';
            return startDate <= isoDate;
        });

        if (activeHabits.length === 0) {
            listDiv.innerHTML = '<p class="text-muted">No habits active on this day.</p>';
            return;
        }

        let html = '';
        activeHabits.forEach(h => {
            const isCompleted = h.completions && h.completions.includes(isoDate);
            
            // For weekly habits, maybe they completed it on another day of the week, but for this specific date view we just check if the isoDate is in completions
            html += `
                <div class="cal-habit-item">
                    <span class="cal-habit-name">${esc(h.name)} <small class="text-muted">(${esc(h.category)})</small></span>
                    <span class="cal-habit-status ${isCompleted ? 'completed' : 'missed'}">
                        <span class="icon">${isCompleted ? 'check_circle' : 'cancel'}</span>
                    </span>
                </div>
            `;
        });
        
        listDiv.innerHTML = html;
    }


    // Utils
    // Habit names and categories are user input, so escape them before they go into innerHTML.
    function esc(text) {
        return String(text ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    }

    function isWeekly(habit) {
        return /^weekly$/i.test(habit.frequency || '');
    }

    // The seven ISO dates of the week starting on the given Monday.
    function weekDates(monday) {
        return Array.from({ length: 7 }, (_, i) => {
            const d = new Date(monday);
            d.setDate(d.getDate() + i);
            return formatDateIso(d);
        });
    }

    function getMonday(d) {
        d = new Date(d);
        var day = d.getDay(),
            diff = d.getDate() - day + (day == 0 ? -6:1); // adjust when day is sunday
        return new Date(d.setDate(diff));
    }

    function formatDateIso(d) {
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    function formatShortDate(d) {
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }
});
