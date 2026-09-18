// dashboard.js - Handles data fetching, UI updates, charts

window.DashboardManager = (function() {
    let dailyChartInstance = null;
    let weeklyChartInstance = null;
    let modalsWired = false;

    // Dates for the grid
    const getDatesForWeek = () => {
        const dates = [];
        const today = new Date();
        const dayOfWeek = today.getDay(); // 0 is Sunday
        const distanceToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

        const monday = new Date(today);
        monday.setDate(today.getDate() + distanceToMonday);

        for(let i=0; i<7; i++) {
            const d = new Date(monday);
            d.setDate(monday.getDate() + i);
            dates.push(d);
        }
        return dates;
    };

    const weekDates = getDatesForWeek();

    const formatDateString = (date) => {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();

        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;

        return [year, month, day].join('-');
    };

    const init = async () => {
        setupModals();
        await refreshAllData();
    };

    const refreshAllData = async () => {
        try {
            await Promise.all([
                loadActivitiesAndGrid(),
                loadDailyProgress(),
                loadWeeklyProgress(),
                loadMonthlyProgress(),
                loadNotifications()
            ]);
        } catch (error) {
            console.error("Failed to load data", error);
        }
    };

    // Everything that changes when a box is ticked, minus the grid itself - the
    // checkbox already shows its own new state.
    const refreshProgress = () => Promise.all([
        loadDailyProgress(),
        loadWeeklyProgress(),
        loadMonthlyProgress(),
        loadNotifications()
    ]);

    // --- Activities & Grid ---
    const loadActivitiesAndGrid = async () => {
        // Fetch the week's stored ticks alongside the activities, otherwise the grid
        // renders blank and every past completion looks lost.
        const [activities, completions] = await Promise.all([
            api.activities.getAll(),
            api.activities.completions(formatDateString(weekDates[0]), formatDateString(weekDates[6]))
        ]);

        const ticked = new Set(
            (completions || []).filter(c => c.completed).map(c => `${c.activityId}|${c.date}`)
        );

        const gridBody = document.querySelector('#activity-grid-table tbody');
        const emptyDash = document.getElementById('empty-activities-dash');
        const manageList = document.getElementById('manage-activity-list');

        gridBody.innerHTML = '';
        manageList.innerHTML = '';

        if (activities.length === 0) {
            document.getElementById('activity-grid-table').classList.add('hidden');
            emptyDash.classList.remove('hidden');
            const empty = placeholder('No activities added yet.');
            empty.className = 'empty-state';
            manageList.appendChild(empty);
            return;
        }

        document.getElementById('activity-grid-table').classList.remove('hidden');
        emptyDash.classList.add('hidden');

        for (const act of activities) {
            gridBody.appendChild(buildGridRow(act, ticked));
            manageList.appendChild(buildManageRow(act));
        }
    };

    const buildGridRow = (act, ticked) => {
        const tr = document.createElement('tr');

        const tdName = document.createElement('td');
        tdName.textContent = act.activityName;
        tr.appendChild(tdName);

        for (let i = 0; i < 7; i++) {
            const dateStr = formatDateString(weekDates[i]);
            const td = document.createElement('td');
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.className = 'grid-checkbox';
            checkbox.checked = ticked.has(`${act.id}|${dateStr}`);
            checkbox.setAttribute('aria-label', `${act.activityName} on ${dateStr}`);

            // Future days stay editable on purpose - plan ahead if you want to.

            checkbox.addEventListener('change', async (e) => {
                const isChecked = e.target.checked;
                try {
                    if (isChecked) {
                        await api.activities.complete(act.id, dateStr);
                    } else {
                        await api.activities.uncomplete(act.id, dateStr);
                    }
                    await refreshProgress();
                } catch(err) {
                    e.target.checked = !isChecked; // revert
                    alert("Failed to update status");
                }
            });

            td.appendChild(checkbox);
            tr.appendChild(td);
        }
        return tr;
    };

    // Built with textContent rather than innerHTML: an activity name is user input
    // and would otherwise be parsed as markup.
    const buildManageRow = (act) => {
        const li = document.createElement('li');

        const info = document.createElement('div');
        info.className = 'activity-info';
        const title = document.createElement('h4');
        title.textContent = act.activityName;
        const meta = document.createElement('p');
        meta.textContent = act.description ? `${act.frequency} - ${act.description}` : act.frequency;
        info.append(title, meta);

        const actions = document.createElement('div');
        actions.className = 'activity-actions';

        const editBtn = document.createElement('button');
        editBtn.className = 'btn btn-sm btn-outline';
        editBtn.textContent = 'Edit';
        editBtn.addEventListener('click', () => openModal(act));

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'btn btn-sm btn-danger';
        deleteBtn.textContent = 'Delete';
        deleteBtn.addEventListener('click', async () => {
            if (!confirm(`Delete "${act.activityName}"? Its completion history goes with it.`)) return;
            try {
                await api.activities.delete(act.id);
                await refreshAllData();
            } catch (err) {
                alert("Failed to delete activity");
            }
        });

        actions.append(editBtn, deleteBtn);
        li.append(info, actions);
        return li;
    };

    // --- Progress ---
    const loadDailyProgress = async () => {
        const data = await api.progress.getDaily();
        const ctx = document.getElementById('dailyChart').getContext('2d');

        document.getElementById('daily-percentage-text').textContent = `${data.dailyPercentage}%`;
        document.getElementById('prog-daily-comp').textContent = data.completedActivities;
        document.getElementById('prog-daily-plan').textContent = data.plannedActivities;

        if (dailyChartInstance) {
            dailyChartInstance.destroy();
        }

        const remaining = Math.max(0, data.plannedActivities - data.completedActivities);

        dailyChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Completed', 'Pending'],
                datasets: [{
                    data: [data.completedActivities, remaining],
                    backgroundColor: ['#10B981', '#E5E7EB'],
                    borderWidth: 0,
                    cutout: '75%'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            }
        });
    };

    const loadWeeklyProgress = async () => {
        const data = await api.progress.getWeekly();
        const ctx = document.getElementById('weeklyChart').getContext('2d');

        document.getElementById('prog-weekly-avg').textContent = `${data.weeklyAveragePercentage}%`;

        if (weeklyChartInstance) {
            weeklyChartInstance.destroy();
        }

        const labels = Object.keys(data.weeklyData || {});
        const values = Object.values(data.weeklyData || {});

        weeklyChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Completion %',
                    data: values,
                    backgroundColor: '#4F46E5',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        ticks: { stepSize: 20 }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    };

    const loadMonthlyProgress = async () => {
        const data = await api.progress.getMonthly();

        document.getElementById('monthly-percentage-text').textContent = `${data.monthlyPercentage}%`;
        document.getElementById('monthly-progress-fill').style.width = `${data.monthlyPercentage}%`;

        document.getElementById('prog-monthly-comp').textContent = data.completedActivities;
    };

    // --- Notifications ---
    const loadNotifications = async () => {
        const notifications = await api.notifications.getAll();
        const dashList = document.getElementById('dash-notifications');
        const fullList = document.getElementById('full-notifications-list');

        dashList.innerHTML = '';
        fullList.innerHTML = '';

        if (notifications.length === 0) {
            dashList.appendChild(placeholder('No recent notifications.'));
            fullList.appendChild(placeholder('You have no notifications.'));
            return;
        }

        notifications.slice(0, 3).forEach(n => dashList.appendChild(buildNotification(n)));
        notifications.forEach(n => fullList.appendChild(buildNotification(n)));
    };

    const placeholder = (text) => {
        const li = document.createElement('li');
        li.textContent = text;
        return li;
    };

    const buildNotification = (n) => {
        const li = document.createElement('li');
        li.textContent = n.message;

        if (!n.isRead) {
            li.classList.add('unread');
            li.title = 'Click to mark as read';
            li.addEventListener('click', async () => {
                try {
                    await api.notifications.markRead(n.id);
                    await loadNotifications();
                } catch (err) {
                    console.error("Failed to mark notification read", err);
                }
            });
        }
        return li;
    };

    // --- Modals & Forms ---
    // Null activity means "create"; passing one switches the same modal into edit mode.
    const openModal = (activity) => {
        document.getElementById('modal-title').textContent = activity ? 'Edit Activity' : 'Add New Activity';
        document.getElementById('act-id').value = activity ? activity.id : '';
        document.getElementById('act-name').value = activity ? activity.activityName : '';
        document.getElementById('act-desc').value = (activity && activity.description) || '';
        document.getElementById('act-freq').value = (activity && activity.frequency) || 'DAILY';
        document.getElementById('activity-modal').classList.remove('hidden');
    };

    const closeModal = () => {
        document.getElementById('activity-modal').classList.add('hidden');
        document.getElementById('activity-form').reset();
        document.getElementById('act-id').value = '';
    };

    const setupModals = () => {
        // init() runs again whenever showApp() fires app-ready, e.g. logging out and back
        // in without a reload. Binding twice would submit every save twice.
        if (modalsWired) return;
        modalsWired = true;

        const form = document.getElementById('activity-form');

        document.getElementById('btn-add-activity-dash').addEventListener('click', () => openModal(null));
        document.getElementById('btn-new-activity').addEventListener('click', () => openModal(null));
        document.querySelector('.close-modal').addEventListener('click', closeModal);

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('act-id').value;
            const data = {
                activityName: document.getElementById('act-name').value,
                description: document.getElementById('act-desc').value,
                frequency: document.getElementById('act-freq').value
            };

            try {
                if (id) {
                    await api.activities.update(id, data);
                } else {
                    await api.activities.create(data);
                }
                closeModal();
                await refreshAllData();
            } catch(err) {
                alert("Failed to save activity");
            }
        });
    };

    return {
        init
    };
})();
