document.addEventListener('DOMContentLoaded', () => {
    const daysInMonth = 31;
    const dayNames = ['Thu', 'Fri', 'Sat', 'Sun', 'Mon', 'Tue', 'Wed']; // Starting Jan 1 2026

    const habits = [
        { id: 1, name: 'Book Reading', completions: new Set([1,2,3,4,5,6,7, 9,10,11,12, 14, 15, 20, 22, 24,25,26,27]) },
        { id: 2, name: '1 Hour Gym', completions: new Set([1,2,3, 8,9,10, 14, 19, 21, 23,24,25]) },
        { id: 3, name: 'Meditation', completions: new Set([3, 7, 13, 17,18,19, 20, 23, 26,27]) },
        { id: 4, name: 'Drink Water', completions: new Set([2,3,4, 7,8,9,10, 13,14,15, 19, 22, 27]) },
        { id: 5, name: 'No Sugar', completions: new Set([1, 3,4,5,6,7,8,9,10, 14,15,16,17, 21,22,23,24,25,26, 27]) }
    ];

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
    
    // Initialize heatmap array
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

            const percent = Math.round((habit.completions.size / daysInMonth) * 100);
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

        // Overall Monthly Progress
        const overallPercent = Math.round((totalCompletedAll / totalPossibleAll) * 100) || 0;
        document.getElementById('overallProgressFill').style.width = overallPercent + '%';
        document.getElementById('overallPercentage').innerText = overallPercent + '%';
        document.getElementById('totalCompleted').innerText = totalCompletedAll;

        // Render Heatmap
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
                
                if (habit.completions.has(day)) {
                    habit.completions.delete(day);
                } else {
                    habit.completions.add(day);
                }
                renderGrid();
            });
        });
    }

    renderGrid();
});
