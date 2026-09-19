const API_URL = `${window.location.origin}/api/habits`;

document.addEventListener('DOMContentLoaded', () => {
    
    // Auth Check
    const rawUserId = localStorage.getItem('userId');
    const username = localStorage.getItem('username') || 'User';
    if (!rawUserId) {
        window.location.href = 'login.html';
        return;
    }
    // A missing or expired session: sign in again instead of silently showing cached data.
    // The backend identifies the user from the session token, so no user id is sent any more.
    function handleUnauthorized(res) {
        if (res.status !== 401) return false;
        localStorage.clear();
        window.location.href = 'login.html';
        return true;
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
    const profileView = document.getElementById('profileView');
    const navDashboard = document.getElementById('navDashboard');
    const navSettings = document.getElementById('navSettings');
    const userProfileBtn = document.getElementById('userProfileBtn');
    const backToDashboardBtn = document.getElementById('backToDashboardBtn');
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
    
    // Chart
    let weeklyChartInstance = null;

    // Init
    init();

    function init() {
        initProfileData();
        attachEventListeners();
        fetchHabits();
    }

    function initProfileData() {
        // Fall back to the signed-in account, never to a hardcoded person. login.js saves the
        // email as 'email'; 'userEmail' only exists once the profile form has been saved.
        const storedUsername = localStorage.getItem('username') || 'User';
        const storedFullName = localStorage.getItem('userFullName') || storedUsername;
        const storedEmail = localStorage.getItem('userEmail') || localStorage.getItem('email') || '';
        const storedMobile = localStorage.getItem('userMobile') || '';
        const storedAvatar = localStorage.getItem('userAvatar');

        if (profileNameInput) profileNameInput.value = storedFullName;
        if (profileUsernameInput) profileUsernameInput.value = storedUsername;
        if (profileEmailInput) profileEmailInput.value = storedEmail;
        if (profileMobileInput) profileMobileInput.value = storedMobile;

        if (sidebarUsername) sidebarUsername.innerText = storedUsername || storedFullName;
        renderAvatar(storedAvatar, storedFullName || storedUsername);
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

    // Local storage helpers for offline resilience. They live at this level rather than inside
    // attachEventListeners() because renderGrid() and fetchHabits() call them too; nested there,
    // ticking a habit threw a ReferenceError before the change ever reached the server.
    function getStoredHabits() {
        const stored = localStorage.getItem('pg_local_habits');
        if (stored) {
            try { return JSON.parse(stored); } catch(e) {}
        }
        const today = new Date();
        const d0 = formatDateIso(today);
        const d1 = formatDateIso(new Date(Date.now() - 86400000));
        const d2 = formatDateIso(new Date(Date.now() - 86400000 * 2));
        const d3 = formatDateIso(new Date(Date.now() - 86400000 * 3));
        const defaults = [
            {
                id: 101,
                name: 'Morning Workout & Stretch',
                category: 'Health',
                frequency: 'DAILY',
                currentStreak: 3,
                bestStreak: 7,
                completionPercentage: 85,
                completions: [d0, d1, d2]
            },
            {
                id: 102,
                name: 'Read 20 Pages',
                category: 'Productivity',
                frequency: 'DAILY',
                currentStreak: 4,
                bestStreak: 12,
                completionPercentage: 90,
                completions: [d0, d1, d2, d3]
            },
            {
                id: 103,
                name: 'Drink 2.5L Water',
                category: 'Health',
                frequency: 'DAILY',
                currentStreak: 2,
                bestStreak: 6,
                completionPercentage: 70,
                completions: [d0, d1]
            },
            {
                id: 104,
                name: 'Weekly Planning & Review',
                category: 'Work',
                frequency: 'WEEKLY',
                currentStreak: 1,
                bestStreak: 4,
                completionPercentage: 100,
                completions: [d0]
            }
        ];
        saveStoredHabits(defaults);
        return defaults;
    }

    function saveStoredHabits(data) {
        localStorage.setItem('pg_local_habits', JSON.stringify(data));
    }

    function attachEventListeners() {
        // Tab Navigation
        function showDashboard() {
            if (dashboardView) dashboardView.style.display = 'block';
            if (profileView) profileView.style.display = 'none';
            if (navDashboard) navDashboard.classList.add('active');
            if (navSettings) navSettings.classList.remove('active');
        }

        function showProfile() {
            if (dashboardView) dashboardView.style.display = 'none';
            if (profileView) profileView.style.display = 'block';
            if (navDashboard) navDashboard.classList.remove('active');
            if (navSettings) navSettings.classList.add('active');
            initProfileData();
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
        if (navDashboard) {
            navDashboard.addEventListener('click', (e) => {
                e.preventDefault();
                showDashboard();
            });
        }
        if (backToDashboardBtn) {
            backToDashboardBtn.addEventListener('click', (e) => {
                e.preventDefault();
                showDashboard();
            });
        }

        // Other Nav links
        ['navHabits', 'navStats', 'navCalendar'].forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener('click', (e) => {
                    e.preventDefault();
                    document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
                    el.classList.add('active');
                    showDashboard();
                });
            }
        });

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

                if (sidebarUsername) sidebarUsername.innerText = newUsername || newName;
                renderAvatar(localStorage.getItem('userAvatar'), newName || newUsername);

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
                localStorage.clear();
                window.location.href = 'login.html?logout=true';
            });
        }

        // Modal
        addHabitBtn.addEventListener('click', () => {
            const startDateInput = document.getElementById('habitStartDate');
            if (startDateInput) {
                startDateInput.value = formatDateIso(new Date());
            }
            addHabitModal.classList.add('active');
        });
        closeHabitModal.addEventListener('click', () => addHabitModal.classList.remove('active'));
        cancelHabitBtn.addEventListener('click', () => addHabitModal.classList.remove('active'));
        
        // Form
        addHabitForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const habitName = document.getElementById('habitName').value.trim();
            const habitCat = document.getElementById('habitCategory').value;
            const habitFreq = document.getElementById('habitFrequency').value;
            const startDateInput = document.getElementById('habitStartDate');
            const habitStartDate = (startDateInput && startDateInput.value) ? startDateInput.value : formatDateIso(new Date());

            if (!habitName) return;

            const newHabit = {
                name: habitName,
                category: habitCat,
                frequency: habitFreq,
                startDate: habitStartDate
            };

            let savedOnBackend = false;
            try {
                const controller = new AbortController();
                const timeoutId = setTimeout(() => controller.abort(), 6000);
                const token = localStorage.getItem('progressgrid_token');
                const headers = { 
                    'Content-Type': 'application/json'
                };
                if (token) headers['Authorization'] = 'Bearer ' + token;

                const res = await fetch(API_URL, {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify(newHabit),
                    signal: controller.signal
                });
                clearTimeout(timeoutId);
                if (handleUnauthorized(res)) return;
                if(res.ok) {
                    savedOnBackend = true;
                    const serverHabit = await res.json();
                    if (serverHabit && serverHabit.id) {
                        newHabit.id = serverHabit.id;
                        if (serverHabit.startDate) newHabit.startDate = serverHabit.startDate;
                    }
                }
            } catch(e) {
                // Backend offline - gracefully fallback to local
            }

            // Always ensure habit is added and rendered immediately
            const currentList = getStoredHabits();
            const created = {
                id: newHabit.id || Date.now(),
                name: newHabit.name,
                category: newHabit.category,
                frequency: newHabit.frequency,
                startDate: newHabit.startDate || habitStartDate,
                currentStreak: 0,
                bestStreak: 0,
                completionPercentage: 0,
                completions: []
            };
            currentList.push(created);
            saveStoredHabits(currentList);
            
            addHabitForm.reset();
            if (startDateInput) startDateInput.value = formatDateIso(new Date());
            addHabitModal.classList.remove('active');
            fetchHabits();
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
        let loadedFromServer = false;
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 1200);
            const token = localStorage.getItem('progressgrid_token');
            const headers = {};
            if (token) headers['Authorization'] = 'Bearer ' + token;

            const res = await fetch(API_URL, {
                headers: headers,
                signal: controller.signal
            });
            clearTimeout(timeoutId);
            if (handleUnauthorized(res)) return;
            if (res.ok) {
                habits = await res.json();
                loadedFromServer = true;
                saveStoredHabits(habits);
            }
        } catch(e) {
            // Backend offline - seamlessly use local storage
        }

        if (!loadedFromServer) {
            habits = getStoredHabits();
        }

        // Format dates and ensure startDate is present
        habits.forEach(h => {
            h.completionsSet = new Set(h.completions || []);
            if (!h.startDate) {
                h.startDate = formatDateIso(new Date());
            }
        });
        renderDashboard();
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
        // Headers
        const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        let headerHtml = `<th>Habit</th>`;
        
        let loopDate = new Date(currentWeekStart);
        for(let i=0; i<7; i++) {
            headerHtml += `<th>${dayNames[i]}<br><small>${loopDate.getDate()}</small></th>`;
            loopDate.setDate(loopDate.getDate() + 1);
        }
        dayHeaderRow.innerHTML = headerHtml;

        // Body
        let tbodyHtml = '';
        if(habits.length === 0) {
            tbodyHtml = `<tr><td colspan="8" style="text-align:center; padding: 20px;">No habits yet. Add one above!</td></tr>`;
        }

        habits.forEach(habit => {
            let habitStartDate = '';
            if (habit.startDate) {
                habitStartDate = typeof habit.startDate === 'string' ? habit.startDate.split('T')[0] : formatDateIso(new Date(habit.startDate));
            } else if (habit.createdAt) {
                habitStartDate = typeof habit.createdAt === 'string' ? habit.createdAt.split('T')[0] : formatDateIso(new Date(habit.createdAt));
            } else {
                habitStartDate = formatDateIso(new Date());
            }

            let rowHtml = `<tr class="habit-row">
                <td class="habit-name">
                    <strong>${habit.name}</strong><br>
                    <small style="color:var(--text-muted)">${habit.category} • From ${formatShortDate(new Date(habitStartDate + 'T00:00:00'))}</small>
                </td>`;
            
            let d = new Date(currentWeekStart);
            for (let i = 0; i < 7; i++) {
                const dateStr = formatDateIso(d);
                const isBeforeStart = dateStr < habitStartDate;

                if (isBeforeStart) {
                    // Habit has not started yet before this date: show subtle inactive indicator, no box
                    rowHtml += `
                        <td class="check-cell not-started" title="Habit starts on ${formatShortDate(new Date(habitStartDate + 'T00:00:00'))}">
                            <span class="not-started-dash" aria-label="Not started yet">—</span>
                        </td>`;
                } else {
                    // Habit is active starting from this date: show interactive check box
                    const isChecked = habit.completionsSet.has(dateStr);
                    rowHtml += `
                        <td class="check-cell ${isChecked ? 'checked' : ''}" data-habit="${habit.id}" data-date="${dateStr}" title="${dateStr}">
                            <div class="check-box-inner"></div>
                        </td>`;
                }
                d.setDate(d.getDate() + 1);
            }
            rowHtml += `</tr>`;
            tbodyHtml += rowHtml;
        });

        habitTableBody.innerHTML = tbodyHtml;

        // Attach clicks only to active habit cells (excluding not-started days)
        document.querySelectorAll('.check-cell:not(.not-started)').forEach(cell => {
            cell.addEventListener('click', async (e) => {
                const td = e.target.closest('.check-cell');
                if (!td || td.classList.contains('not-started')) return;
                const habitId = parseInt(td.dataset.habit);
                const dateStr = td.dataset.date;
                if (!habitId || !dateStr) return;
                const habit = habits.find(h => h.id === habitId);
                if (!habit) return;
                
                const isCompleted = !habit.completionsSet.has(dateStr);
                
                // Optimistic UI
                if(isCompleted) habit.completionsSet.add(dateStr);
                else habit.completionsSet.delete(dateStr);
                
                renderDashboard();

                // Persist locally
                habit.completions = Array.from(habit.completionsSet);
                const storedList = getStoredHabits();
                const matched = storedList.find(item => item.id === habitId);
                if (matched) {
                    matched.completions = habit.completions;
                    saveStoredHabits(storedList);
                }

                // API sync if available
                try {
                    const controller = new AbortController();
                    const timeoutId = setTimeout(() => controller.abort(), 1200);
                    const token = localStorage.getItem('progressgrid_token');
                    const headers = { 
                        'Content-Type': 'application/json'
                    };
                    if (token) headers['Authorization'] = 'Bearer ' + token;

                    const res = await fetch(`${API_URL}/${habitId}/complete`, {
                        method: 'POST',
                        headers: headers,
                        body: JSON.stringify({ date: dateStr, completed: isCompleted }),
                        signal: controller.signal
                    });
                    clearTimeout(timeoutId);
                    handleUnauthorized(res);
                } catch(err) {
                    // Offline - state already persisted locally
                }
            });
        });
    }

    function renderSummaries() {
        document.getElementById('sumTotalHabits').innerText = habits.length;
        
        let totalCurrentStreak = habits.reduce((acc, h) => acc + (h.currentStreak || 0), 0);
        let bestOverallStreak = Math.max(0, ...habits.map(h => h.bestStreak || 0));
        
        document.getElementById('sumCurrentStreak').innerText = `${totalCurrentStreak} days`;
        document.getElementById('sumBestStreak').innerText = `${bestOverallStreak} days`;

        // Calculate Weekly Progress only for active days (from start date onwards)
        let totalChecksThisWeek = 0;
        let possibleChecks = 0;
        
        habits.forEach(h => {
            const hStart = h.startDate ? (typeof h.startDate === 'string' ? h.startDate.split('T')[0] : formatDateIso(new Date(h.startDate))) : '';
            let d = new Date(currentWeekStart);
            for(let i=0; i<7; i++) {
                const dateStr = formatDateIso(d);
                if (!hStart || dateStr >= hStart) {
                    possibleChecks++;
                    if(h.completionsSet.has(dateStr)) totalChecksThisWeek++;
                }
                d.setDate(d.getDate() + 1);
            }
        });

        let weeklyPercent = possibleChecks === 0 ? 0 : Math.round((totalChecksThisWeek / possibleChecks) * 100);
        document.getElementById('sumWeeklyProgress').innerText = `${weeklyPercent}%`;
        
        // Monthly simply average of all habits completion percentage from backend
        let totalMonthly = habits.reduce((acc, h) => acc + (h.completionPercentage || 0), 0);
        let monthlyAvg = habits.length === 0 ? 0 : Math.round(totalMonthly / habits.length);
        document.getElementById('sumMonthlyProgress').innerText = `${monthlyAvg}%`;
    }

    function renderTodayHabits() {
        const todayIso = formatDateIso(new Date());
        let html = '';
        
        if(habits.length === 0) html = `<p class="text-muted">No habits scheduled.</p>`;
        
        habits.forEach(h => {
            const isDone = h.completionsSet.has(todayIso);
            html += `
                <div class="today-habit-item ${isDone ? 'completed' : ''}">
                    <div class="habit-title">
                        <span style="display:inline-block; width:10px; height:10px; background:var(--primary-color); border-radius:50%; margin-right:8px;"></span>
                        ${h.name}
                    </div>
                    <div class="habit-status">${isDone ? '✓ Done' : 'Pending'}</div>
                </div>
            `;
        });
        document.getElementById('todayHabitsList').innerHTML = html;
    }

    function renderPerformance() {
        let sorted = [...habits].sort((a,b) => (b.completionPercentage || 0) - (a.completionPercentage || 0));
        let html = '';
        sorted.slice(0, 5).forEach(h => {
            html += `
                <div class="stat-item">
                    <div class="stat-item-info">
                        <h4>${h.name}</h4>
                        <p>Streak: ${h.currentStreak || 0} | Best: ${h.bestStreak || 0}</p>
                    </div>
                    <div class="stat-value">${h.completionPercentage || 0}%</div>
                </div>
            `;
        });
        document.getElementById('topHabitsList').innerHTML = html || '<p>No data yet.</p>';
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
        const dataPoints = [0, 0, 0, 0, 0, 0, 0];
        const dayCounts = [0, 0, 0, 0, 0, 0, 0];
        const totalHabits = habits.length;
        
        if (habits.length > 0) {
            let d = new Date(currentWeekStart);
            for (let i = 0; i < 7; i++) {
                let dayDone = 0;
                const iso = formatDateIso(d);
                habits.forEach(h => {
                    if (h.completionsSet && h.completionsSet.has(iso)) dayDone++;
                });
                dayCounts[i] = dayDone;
                dataPoints[i] = Math.round((dayDone / habits.length) * 100);
                d.setDate(d.getDate() + 1);
            }
        }

        const bgColors = dataPoints.map(p => getWeeklyProgressColor(p).bg);
        const borderColors = dataPoints.map(p => getWeeklyProgressColor(p).border);
        const hoverColors = dataPoints.map(p => getWeeklyProgressColor(p).hover);

        // Update Average Badge
        const avgPct = dataPoints.length > 0 
            ? Math.round(dataPoints.reduce((a, b) => a + b, 0) / dataPoints.length) 
            : 0;
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
            weeklyChartInstance.totalHabits = totalHabits;
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
                                    const counts = (weeklyChartInstance && weeklyChartInstance.dayCounts) || dayCounts;
                                    const total = (weeklyChartInstance && weeklyChartInstance.totalHabits) || totalHabits;
                                    const done = counts[index] || 0;
                                    
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
            weeklyChartInstance.totalHabits = totalHabits;
        }
    }

    // Utils
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
