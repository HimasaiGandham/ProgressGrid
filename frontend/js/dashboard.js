// dashboard.js - Handles data fetching, UI updates, charts

window.DashboardManager = (function() {
    let dailyChartInstance = null;
    let weeklyChartInstance = null;

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

    // --- Activities & Grid ---
    const loadActivitiesAndGrid = async () => {
        const activities = await api.activities.getAll();
        
        const gridBody = document.querySelector('#activity-grid-table tbody');
        const emptyDash = document.getElementById('empty-activities-dash');
        const manageList = document.getElementById('manage-activity-list');
        
        gridBody.innerHTML = '';
        manageList.innerHTML = '';

        if (activities.length === 0) {
            document.getElementById('activity-grid-table').classList.add('hidden');
            emptyDash.classList.remove('hidden');
            manageList.innerHTML = '<li class="empty-state">No activities added yet.</li>';
            return;
        }

        document.getElementById('activity-grid-table').classList.remove('hidden');
        emptyDash.classList.add('hidden');

        // Note: For a real app, we need to fetch which days are completed.
        // To do this simply without a massive query, we can assume we check daily status 
        // OR we just send completions for the week from the backend.
        // Since our backend doesn't have a specific endpoint for the week's grid,
        // we'll fetch the daily progress or just keep it simple: 
        // A full app would have a dedicated endpoint for the grid state.
        // For now, let's just render the grid with checkboxes and handle clicks.
        
        for (const act of activities) {
            // Dashboard Grid Row
            const tr = document.createElement('tr');
            
            const tdName = document.createElement('td');
            tdName.textContent = act.activityName;
            tr.appendChild(tdName);

            for (let i = 0; i < 7; i++) {
                const td = document.createElement('td');
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.className = 'grid-checkbox';
                checkbox.dataset.actId = act.id;
                checkbox.dataset.date = formatDateString(weekDates[i]);
                
                // Allow access to future days as requested
                // if (weekDates[i] > new Date()) {
                //     checkbox.disabled = true;
                // }

                checkbox.addEventListener('change', async (e) => {
                    const id = e.target.dataset.actId;
                    const date = e.target.dataset.date;
                    const isChecked = e.target.checked;
                    
                    try {
                        if (isChecked) {
                            await api.activities.complete(id, date);
                        } else {
                            await api.activities.uncomplete(id, date);
                        }
                        // Refresh progress
                        loadDailyProgress();
                        loadWeeklyProgress();
                        loadMonthlyProgress();
                    } catch(err) {
                        e.target.checked = !isChecked; // revert
                        alert("Failed to update status");
                    }
                });

                td.appendChild(checkbox);
                tr.appendChild(td);
            }
            gridBody.appendChild(tr);

            // Manage Activities List Row
            const li = document.createElement('li');
            li.innerHTML = `
                <div class="activity-info">
                    <h4>${act.activityName}</h4>
                    <p>${act.frequency}</p>
                </div>
                <div class="activity-actions">
                    <button class="btn btn-sm btn-danger delete-act-btn" data-id="${act.id}">Delete</button>
                </div>
            `;
            manageList.appendChild(li);
        }

        // Attach delete listeners
        document.querySelectorAll('.delete-act-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                if(confirm('Are you sure you want to delete this activity?')) {
                    await api.activities.delete(e.target.dataset.id);
                    refreshAllData();
                }
            });
        });
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

        dailyChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Completed', 'Pending'],
                datasets: [{
                    data: [data.completedActivities, data.plannedActivities - data.completedActivities],
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
            dashList.innerHTML = '<li>No recent notifications.</li>';
            fullList.innerHTML = '<li>You have no notifications.</li>';
            return;
        }

        notifications.slice(0, 3).forEach(n => {
            const li = document.createElement('li');
            li.textContent = n.message;
            if(!n.isRead) li.classList.add('unread');
            dashList.appendChild(li);
        });

        notifications.forEach(n => {
            const li = document.createElement('li');
            li.textContent = n.message;
            if(!n.isRead) li.classList.add('unread');
            fullList.appendChild(li);
        });
    };

    // --- Modals & Forms ---
    const setupModals = () => {
        const modal = document.getElementById('activity-modal');
        const btnDash = document.getElementById('btn-add-activity-dash');
        const btnManage = document.getElementById('btn-new-activity');
        const closeBtn = document.querySelector('.close-modal');
        const form = document.getElementById('activity-form');

        const openModal = () => modal.classList.remove('hidden');
        const closeModal = () => {
            modal.classList.add('hidden');
            form.reset();
        };

        btnDash.addEventListener('click', openModal);
        btnManage.addEventListener('click', openModal);
        closeBtn.addEventListener('click', closeModal);

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                activityName: document.getElementById('act-name').value,
                description: document.getElementById('act-desc').value,
                frequency: document.getElementById('act-freq').value
            };

            try {
                await api.activities.create(data);
                closeModal();
                refreshAllData();
            } catch(err) {
                alert("Failed to save activity");
            }
        });
    };

    return {
        init
    };
})();
