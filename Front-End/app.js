const API_URL = `${window.location.origin}/api/habits`;

document.addEventListener('DOMContentLoaded', () => {

    // Signed in means holding a session token. Without one, go to the sign-in page.
    if (!localStorage.getItem('progressgrid_token')) {
        window.location.href = 'login.html';
        return;
    }

    // State
    let habits = [];
    let loadFailed = false;
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

    // One place for the session token, the request timeout and what a 401 means.
    async function api(path, options = {}) {
        const res = await fetch(API_URL + path, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('progressgrid_token')
            },
            signal: AbortSignal.timeout(8000)
        });
        if (res.status === 401) {
            // Missing or expired session: sign in again.
            localStorage.clear();
            window.location.href = 'login.html';
        }
        return res;
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

        // Other Nav links (their pages don't exist yet, so they show the dashboard)
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

            const res = await api('', { method: 'POST', body: JSON.stringify(habit) }).catch(() => null);
            if (!res || !res.ok) {
                alert("Couldn't save the habit. Check that the backend is running and try again.");
                return;
            }
            addHabitForm.reset();
            addHabitModal.classList.remove('active');
            fetchHabits();
        });

        // Ticking a day: one listener on the table instead of one per cell on every render.
        habitTableBody.addEventListener('click', async (e) => {
            const cell = e.target.closest('td[data-habit]');
            if (!cell) return;
            const habit = habits.find(h => h.id === Number(cell.dataset.habit));
            const date = cell.dataset.date;
            const completed = !habit.completionsSet.has(date);

            // Show the tick straight away, then reload: streaks and percentages are worked out on
            // the server, and reloading also puts the box back if saving failed.
            if (completed) habit.completionsSet.add(date);
            else habit.completionsSet.delete(date);
            renderDashboard();
            await api(`/${habit.id}/complete`, { method: 'POST', body: JSON.stringify({ date, completed }) }).catch(() => {});
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
        try {
            const res = await api('');
            if (!res.ok) throw new Error('HTTP ' + res.status);
            habits = await res.json();
            loadFailed = false;
        } catch (e) {
            habits = [];
            loadFailed = true;
        }

        habits.forEach(h => {
            h.completionsSet = new Set(h.completions || []);
            h.start = String(h.startDate || formatDateIso(new Date())).split('T')[0];
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
        const days = weekDates(currentWeekStart);
        const today = formatDateIso(new Date());
        const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        dayHeaderRow.innerHTML = '<th>Habit</th>' + days.map((iso, i) => `<th>${dayNames[i]}<br><small>${Number(iso.slice(8))}</small></th>`).join('');

        if (habits.length === 0) {
            const message = loadFailed
                ? "Couldn't load your habits. Check that the backend is running, then refresh."
                : 'No habits yet. Add one above!';
            habitTableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 20px;">${message}</td></tr>`;
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
                    <strong>${esc(habit.name)}</strong><br>
                    <small style="color:var(--text-muted)">${esc(habit.category)} • ${isWeekly(habit) ? 'Weekly' : 'Daily'} • From ${startLabel}</small>
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
