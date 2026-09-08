const API_URL = 'http://localhost:8080/api/habits';

document.addEventListener('DOMContentLoaded', () => {
    
    // Auth Check
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('username');
    if (!userId) {
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
    
    // Chart
    let weeklyChartInstance = null;

    // Init
    init();

    function init() {
        document.querySelector('.user-info h4').innerText = username;
        attachEventListeners();
        fetchHabits();
    }

    function attachEventListeners() {
        // Logout
        document.getElementById('logoutBtn').addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.clear();
            window.location.href = 'login.html';
        });
        // Modal
        addHabitBtn.addEventListener('click', () => addHabitModal.classList.add('active'));
        closeHabitModal.addEventListener('click', () => addHabitModal.classList.remove('active'));
        cancelHabitBtn.addEventListener('click', () => addHabitModal.classList.remove('active'));
        
        // Form
        addHabitForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const newHabit = {
                name: document.getElementById('habitName').value,
                category: document.getElementById('habitCategory').value,
                frequency: document.getElementById('habitFrequency').value
            };
            try {
                const res = await fetch(API_URL, {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/json',
                        'X-User-Id': userId
                    },
                    body: JSON.stringify(newHabit)
                });
                if(res.ok) {
                    addHabitForm.reset();
                    addHabitModal.classList.remove('active');
                    fetchHabits();
                }
            } catch(e) {
                console.error("Failed to create habit", e);
                alert("Failed to create habit. Make sure backend is running.");
            }
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
            const res = await fetch(API_URL, {
                headers: { 'X-User-Id': userId }
            });
            habits = await res.json();
            
            // Format dates
            habits.forEach(h => {
                h.completionsSet = new Set(h.completions);
            });
            renderDashboard();
        } catch(e) {
            console.error("Failed to fetch habits", e);
            // Fallback for visual testing without backend
            habits = [];
            renderDashboard();
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
            let rowHtml = `<tr class="habit-row">
                <td class="habit-name">
                    <strong>${habit.name}</strong><br>
                    <small style="color:var(--text-muted)">${habit.category}</small>
                </td>`;
            
            let d = new Date(currentWeekStart);
            for (let i = 0; i < 7; i++) {
                const dateStr = formatDateIso(d);
                const isChecked = habit.completionsSet.has(dateStr);
                
                rowHtml += `
                    <td class="check-cell ${isChecked ? 'checked' : ''}" data-habit="${habit.id}" data-date="${dateStr}">
                        <div class="check-box-inner"></div>
                    </td>`;
                d.setDate(d.getDate() + 1);
            }
            rowHtml += `</tr>`;
            tbodyHtml += rowHtml;
        });

        habitTableBody.innerHTML = tbodyHtml;

        // Attach clicks
        document.querySelectorAll('.check-cell').forEach(cell => {
            cell.addEventListener('click', async (e) => {
                const td = e.target.closest('.check-cell');
                const habitId = parseInt(td.dataset.habit);
                const dateStr = td.dataset.date;
                const habit = habits.find(h => h.id === habitId);
                
                const isCompleted = !habit.completionsSet.has(dateStr);
                
                // Optimistic UI
                if(isCompleted) habit.completionsSet.add(dateStr);
                else habit.completionsSet.delete(dateStr);
                
                renderDashboard();

                // API
                try {
                    await fetch(`${API_URL}/${habitId}/complete`, {
                        method: 'POST',
                        headers: { 
                            'Content-Type': 'application/json',
                            'X-User-Id': userId
                        },
                        body: JSON.stringify({ date: dateStr, completed: isCompleted })
                    });
                    fetchHabits(); // sync stats from server
                } catch(err) {
                    console.error(err);
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

        // Calculate Weekly Progress
        let totalChecksThisWeek = 0;
        let possibleChecks = habits.length * 7;
        
        habits.forEach(h => {
            let d = new Date(currentWeekStart);
            for(let i=0; i<7; i++) {
                if(h.completionsSet.has(formatDateIso(d))) totalChecksThisWeek++;
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

    function renderChart() {
        const ctx = document.getElementById('weeklyChart').getContext('2d');
        
        const labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        const dataPoints = [0,0,0,0,0,0,0];
        
        if(habits.length > 0) {
            let d = new Date(currentWeekStart);
            for(let i=0; i<7; i++) {
                let dayDone = 0;
                habits.forEach(h => {
                    if(h.completionsSet.has(formatDateIso(d))) dayDone++;
                });
                dataPoints[i] = Math.round((dayDone / habits.length) * 100);
                d.setDate(d.getDate() + 1);
            }
        }

        if(weeklyChartInstance) {
            weeklyChartInstance.data.datasets[0].data = dataPoints;
            weeklyChartInstance.update();
        } else {
            weeklyChartInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Completion %',
                        data: dataPoints,
                        backgroundColor: '#4CAF50',
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: { beginAtZero: true, max: 100 }
                    }
                }
            });
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
        return d.toISOString().split('T')[0];
    }
    
    function formatShortDate(d) {
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }
});
