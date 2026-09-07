document.addEventListener('DOMContentLoaded', () => {
    const daysInMonth = 31;
    const dayNames = ['Thu', 'Fri', 'Sat', 'Sun', 'Mon', 'Tue', 'Wed'];

    let habits = [];

    // Fetch habits from backend
    fetch('http://localhost:8080/tracker-backend-1.0-SNAPSHOT/api/habits')
        .then(response => response.json())
        .then(data => {
            // Convert array of completions to Set for easier lookup
            habits = data.map(h => ({
                id: h.id,
                name: h.name,
                completions: new Set(h.completions)
            }));
            renderGrid();
        })
        .catch(err => {
            console.error('Error fetching habits:', err);
            // Fallback mock data if server is down
            habits = [
                { id: 1, name: 'Book Reading', completions: new Set([1,2,3]) },
                { id: 2, name: '1 Hour Gym', completions: new Set([1,2]) }
            ];
            renderGrid();
        });

    // Generate Day Headers
    const dayHeaderRow = document.getElementById('dayHeaderRow');
    let dayHeaderHtml = '';
    for (let i = 1; i <= daysInMonth; i++) {
        const dayName = dayNames[(i - 1) % 7];
        dayHeaderHtml += `<th><span class="day-name">${dayName}</span><span class="day-num">${i}</span></th>`;
    }
    dayHeaderRow.innerHTML = dayHeaderHtml;

    const habitTableBody = document.getElementById('habitTableBody');
    const heatmapGrid = document.getElementById('heatmapGrid');
    const heatmapCounts = new Array(daysInMonth + 1).fill(0);

    function renderGrid() {
        let tbodyHtml = '';
        heatmapCounts.fill(0);
        let totalCompletedAll = 0;
        let totalPossibleAll = habits.length * daysInMonth;

        habits.forEach(habit => {
            let rowHtml = `<tr class="habit-row"><td class="habit-name">${habit.name}</td>`;
            
            for (let i = 1; i <= daysInMonth; i++) {
                const isChecked = habit.completions.has(i);
                if (isChecked) {
                    heatmapCounts[i]++;
                    totalCompletedAll++;
                }
                rowHtml += `<td class="check-cell ${isChecked ? 'checked' : ''}" data-habit="${habit.id}" data-day="${i}">${isChecked ? '✓' : ''}</td>`;
            }

            const percent = Math.round((habit.completions.size / daysInMonth) * 100) || 0;
            rowHtml += `
                <td class="row-progress-container">
                    <span class="row-percent">${percent}%</span>
                    <div class="row-bar-wrap">
                        <div class="row-bar-fill" style="width: ${percent}%"></div>
                    </div>
                </td>
            </tr>`;
            tbodyHtml += rowHtml;
        });

        habitTableBody.innerHTML = tbodyHtml;

        const overallPercent = Math.round((totalCompletedAll / totalPossibleAll) * 100) || 0;
        document.getElementById('overallProgressFill').style.width = overallPercent + '%';
        document.getElementById('overallPercentage').innerText = overallPercent + '%';
        document.getElementById('totalCompleted').innerText = totalCompletedAll;

        let heatmapHtml = '';
        for (let i = 1; i <= daysInMonth; i++) {
            const count = heatmapCounts[i];
            let cellClass = '';
            if (count > 0 && count <= 1) cellClass = 'heatmap-1';
            else if (count === 2) cellClass = 'heatmap-2';
            else if (count === 3) cellClass = 'heatmap-3';
            else if (count >= 4) cellClass = 'heatmap-4';
            heatmapHtml += `<div class="heatmap-cell ${cellClass}" title="Day ${i}: ${count} habits"></div>`;
        }
        heatmapGrid.innerHTML = heatmapHtml;
        attachClickListeners();
    }

    function attachClickListeners() {
        document.querySelectorAll('.check-cell').forEach(cell => {
            cell.addEventListener('click', (e) => {
                const habitId = parseInt(e.target.dataset.habit);
                const day = parseInt(e.target.dataset.day);
                const habit = habits.find(h => h.id === habitId);
                const isCompleted = !habit.completions.has(day);

                // Optimistic UI update
                if (isCompleted) {
                    habit.completions.add(day);
                } else {
                    habit.completions.delete(day);
                }
                renderGrid();

                // Send to backend
                fetch('http://localhost:8080/tracker-backend-1.0-SNAPSHOT/api/habits', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ habitId, day, isCompleted })
                }).catch(err => console.error('Failed to save to database', err));
            });
        });
    }
});
