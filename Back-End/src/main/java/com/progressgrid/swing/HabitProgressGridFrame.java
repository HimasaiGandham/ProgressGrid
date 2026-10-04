package com.progressgrid.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * HabitProgressGridFrame - Main Interactive Habit Tracking Grid & Heatmap Application.
 * 
 * Demonstrates advanced Java Swing concepts:
 * 1. Custom 2D Graphics Painting (paintComponent, Graphics2D, Antialiasing, Gradients)
 * 2. Custom Heatmap Grid Component with Mouse Hover & Click Detection
 * 3. JTable with custom AbstractTableModel, TableCellRenderer, and TableRowSorter
 * 4. Multi-threaded background tasks using SwingWorker (Non-blocking EDT)
 * 5. Dynamic filtering via DocumentListener on JTextField
 * 6. Custom Modal JDialog with GridBagLayout, ButtonGroup, and validation
 * 7. Modern UI design using BorderLayout, JSplitPane, JToolBar, JMenuBar, and Status Bar
 */
public class HabitProgressGridFrame extends JFrame {

    // Color Palette
    public static final Color PRIMARY = new Color(79, 70, 229);      // Indigo
    public static final Color PRIMARY_DARK = new Color(67, 56, 202);
    public static final Color SUCCESS = new Color(16, 185, 129);      // Emerald green
    public static final Color WARNING = new Color(245, 158, 11);      // Amber
    public static final Color DANGER = new Color(239, 68, 68);        // Rose
    public static final Color BG_DARK = new Color(15, 23, 42);        // Slate 900
    public static final Color CARD_BG = new Color(30, 41, 59);        // Slate 800
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color BORDER_COLOR = new Color(51, 65, 85);

    // Data structures
    public static class HabitItem {
        private final long id;
        private String title;
        private String category;
        private String frequency; // "DAILY" or "WEEKLY"
        private int currentStreak;
        private int bestStreak;
        private double completionRate;
        private final Set<LocalDate> completionDates = new HashSet<>();

        public HabitItem(long id, String title, String category, String frequency, int currentStreak, int bestStreak, double completionRate) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.frequency = frequency;
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
            this.completionRate = completionRate;
        }

        public long getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public int getCurrentStreak() { return currentStreak; }
        public int getBestStreak() { return bestStreak; }
        public double getCompletionRate() { return completionRate; }
        public Set<LocalDate> getCompletionDates() { return completionDates; }

        public boolean isCompletedOn(LocalDate date) {
            return completionDates.contains(date);
        }

        public void toggleDate(LocalDate date) {
            if (completionDates.contains(date)) {
                completionDates.remove(date);
                if (currentStreak > 0) currentStreak--;
            } else {
                completionDates.add(date);
                currentStreak++;
                if (currentStreak > bestStreak) bestStreak = currentStreak;
            }
            recalcCompletionRate();
        }

        private void recalcCompletionRate() {
            int totalDays = 30;
            this.completionRate = Math.min(100.0, (completionDates.size() * 100.0) / totalDays);
        }
    }

    private final List<HabitItem> habits = new ArrayList<>();
    private HabitTableModel tableModel;
    private JTable habitTable;
    private TableRowSorter<HabitTableModel> rowSorter;
    private ActivityHeatmapPanel heatmapPanel;
    private JLabel statusLabel;
    private JLabel totalCountLabel;
    private JTextField searchField;
    private JComboBox<String> categoryFilterBox;

    public HabitProgressGridFrame() {
        setTitle("ProgressGrid — Desktop Habit Tracker & Matrix Grid");
        setSize(1200, 780);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeMockData();
        setupMenuBar();
        setupComponents();
        loadHabitDataAsync();
    }

    private void initializeMockData() {
        LocalDate today = LocalDate.now();

        HabitItem h1 = new HabitItem(1, "Coding & DSA Practice", "Development", "DAILY", 8, 14, 85.0);
        HabitItem h2 = new HabitItem(2, "Read Tech / Self-Growth Book", "Learning", "DAILY", 5, 12, 70.0);
        HabitItem h3 = new HabitItem(3, "Morning Workout & Running", "Fitness", "DAILY", 12, 21, 92.0);
        HabitItem h4 = new HabitItem(4, "System Design Study", "Education", "WEEKLY", 3, 6, 60.0);
        HabitItem h5 = new HabitItem(5, "Drink 3L Water", "Health", "DAILY", 15, 15, 100.0);
        HabitItem h6 = new HabitItem(6, "Review Daily College Notes", "Academics", "DAILY", 4, 9, 65.0);

        // Populate completion dates for the last 30 days
        for (int i = 0; i < 30; i++) {
            LocalDate d = today.minusDays(i);
            if (i % 2 == 0) h1.getCompletionDates().add(d);
            if (i % 3 != 0) h2.getCompletionDates().add(d);
            if (i < 12 || i % 4 == 0) h3.getCompletionDates().add(d);
            if (i % 7 == 0) h4.getCompletionDates().add(d);
            if (i < 15) h5.getCompletionDates().add(d);
            if (i % 3 == 0) h6.getCompletionDates().add(d);
        }

        habits.add(h1);
        habits.add(h2);
        habits.add(h3);
        habits.add(h4);
        habits.add(h5);
        habits.add(h6);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(CARD_BG);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(TEXT_LIGHT);
        fileMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JMenuItem refreshItem = new JMenuItem("Refresh Data (F5)");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(e -> loadHabitDataAsync());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> dispose());

        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Habits Menu
        JMenu habitMenu = new JMenu("Habits");
        habitMenu.setForeground(TEXT_LIGHT);
        habitMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JMenuItem addHabitItem = new JMenuItem("Add New Habit... (Ctrl+N)");
        addHabitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        addHabitItem.addActionListener(e -> showAddHabitDialog());

        JMenuItem toggleItem = new JMenuItem("Toggle Today's Check (Space)");
        toggleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        toggleItem.addActionListener(e -> toggleSelectedHabitToday());

        JMenuItem deleteItem = new JMenuItem("Delete Selected Habit (Del)");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(e -> deleteSelectedHabit());

        habitMenu.add(addHabitItem);
        habitMenu.add(toggleItem);
        habitMenu.add(deleteItem);

        // Views Menu
        JMenu viewMenu = new JMenu("Analytics");
        viewMenu.setForeground(TEXT_LIGHT);
        viewMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JMenuItem analyticsItem = new JMenuItem("Open Visual Analytics Dashboard (F2)");
        analyticsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        analyticsItem.addActionListener(e -> openAnalyticsDashboard());

        viewMenu.add(analyticsItem);

        menuBar.add(fileMenu);
        menuBar.add(habitMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void setupComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(BG_DARK);

        // 1. Header Toolbar
        JPanel topPanel = createTopPanel();
        contentPane.add(topPanel, BorderLayout.NORTH);

        // 2. Center SplitPane: Top = Table of Habits, Bottom = Interactive Heatmap Grid
        tableModel = new HabitTableModel(habits);
        rowSorter = new TableRowSorter<>(tableModel);

        habitTable = new JTable(tableModel);
        habitTable.setRowSorter(rowSorter);
        habitTable.setRowHeight(38);
        habitTable.setBackground(CARD_BG);
        habitTable.setForeground(TEXT_LIGHT);
        habitTable.setGridColor(BORDER_COLOR);
        habitTable.setSelectionBackground(new Color(67, 56, 202, 180));
        habitTable.setSelectionForeground(Color.WHITE);
        habitTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        habitTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        habitTable.getTableHeader().setBackground(new Color(24, 32, 47));
        habitTable.getTableHeader().setForeground(TEXT_LIGHT);
        habitTable.getTableHeader().setPreferredSize(new Dimension(0, 35));

        // Custom Renderers for Columns
        habitTable.getColumnModel().getColumn(1).setCellRenderer(new CategoryBadgeRenderer());
        habitTable.getColumnModel().getColumn(3).setCellRenderer(new StreakCellRenderer());
        habitTable.getColumnModel().getColumn(4).setCellRenderer(new ProgressBarCellRenderer());

        habitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = habitTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelIndex = habitTable.convertRowIndexToModel(selectedRow);
                    heatmapPanel.setSelectedHabit(habits.get(modelIndex));
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(habitTable);
        tableScrollPane.getViewport().setBackground(CARD_BG);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                " All Monitored Habits (Click a habit to inspect in Matrix Grid) ",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), TEXT_MUTED
        ));

        // Bottom Heatmap Component
        heatmapPanel = new ActivityHeatmapPanel(habits.isEmpty() ? null : habits.get(0));
        JScrollPane heatmapScrollPane = new JScrollPane(heatmapPanel);
        heatmapScrollPane.getViewport().setBackground(BG_DARK);
        heatmapScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                " 30-Day Activity Progress Heatmap Matrix (Click any cell to toggle completion) ",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), TEXT_MUTED
        ));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, heatmapScrollPane);
        splitPane.setDividerLocation(340);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(6);
        splitPane.setBackground(BG_DARK);
        splitPane.setBorder(new EmptyBorder(10, 16, 6, 16));

        contentPane.add(splitPane, BorderLayout.CENTER);

        // 3. Status Bar at Bottom
        JPanel statusBar = createStatusBar();
        contentPane.add(statusBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(BG_DARK);
        headerPanel.setBorder(new EmptyBorder(14, 16, 6, 16));

        // Title and Logo
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(BG_DARK);

        JLabel logoBadge = new JLabel(" ■ ");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoBadge.setForeground(PRIMARY);

        JLabel titleLabel = new JLabel("ProgressGrid");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subtitleLabel = new JLabel(" | Habit Activity Tracker & Interactive Grid");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_MUTED);

        titlePanel.add(logoBadge);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        // Action Toolbar
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(BG_DARK);

        // Search Filter Field
        searchField = new JTextField(14);
        searchField.setPreferredSize(new Dimension(150, 32));
        searchField.setBackground(CARD_BG);
        searchField.setForeground(TEXT_LIGHT);
        searchField.setCaretColor(TEXT_LIGHT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        searchField.setToolTipText("Search habit by title...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(TEXT_MUTED);

        // Category Filter
        categoryFilterBox = new JComboBox<>(new String[]{"All Categories", "Development", "Learning", "Fitness", "Education", "Health", "Academics"});
        categoryFilterBox.setPreferredSize(new Dimension(140, 32));
        categoryFilterBox.setBackground(CARD_BG);
        categoryFilterBox.setForeground(TEXT_LIGHT);
        categoryFilterBox.addActionListener(e -> filterTable());

        // Buttons
        JButton addBtn = createStyledButton("+ New Habit", PRIMARY, Color.WHITE);
        addBtn.addActionListener(e -> showAddHabitDialog());

        JButton toggleTodayBtn = createStyledButton("✔ Check Today", SUCCESS, Color.WHITE);
        toggleTodayBtn.addActionListener(e -> toggleSelectedHabitToday());

        JButton analyticsBtn = createStyledButton("📊 View Analytics", new Color(139, 92, 246), Color.WHITE);
        analyticsBtn.addActionListener(e -> openAnalyticsDashboard());

        controlsPanel.add(searchIcon);
        controlsPanel.add(searchField);
        controlsPanel.add(categoryFilterBox);
        controlsPanel.add(addBtn);
        controlsPanel.add(toggleTodayBtn);
        controlsPanel.add(analyticsBtn);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setBackground(CARD_BG);
        statusPanel.setBorder(new EmptyBorder(6, 16, 6, 16));

        statusLabel = new JLabel("● Ready — Loaded local & synced habit state.");
        statusLabel.setForeground(SUCCESS);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        totalCountLabel = new JLabel("Total Habits: " + habits.size() + " | Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        totalCountLabel.setForeground(TEXT_MUTED);
        totalCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(totalCountLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private void filterTable() {
        String query = searchField.getText().trim();
        String cat = (String) categoryFilterBox.getSelectedItem();

        List<RowFilter<HabitTableModel, Integer>> filters = new ArrayList<>();
        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + query, 0));
        }
        if (cat != null && !cat.equals("All Categories")) {
            filters.add(RowFilter.regexFilter("(?i)" + cat, 1));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void showAddHabitDialog() {
        AddHabitDialog dialog = new AddHabitDialog(this);
        dialog.setVisible(true);

        HabitItem newHabit = dialog.getCreatedHabit();
        if (newHabit != null) {
            habits.add(newHabit);
            tableModel.fireTableDataChanged();
            totalCountLabel.setText("Total Habits: " + habits.size() + " | Today: " + LocalDate.now());
            statusLabel.setText("● Added new habit: " + newHabit.getTitle());
            heatmapPanel.setSelectedHabit(newHabit);
        }
    }

    private void toggleSelectedHabitToday() {
        int selectedRow = habitTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a habit from the table first.", "No Habit Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = habitTable.convertRowIndexToModel(selectedRow);
        HabitItem item = habits.get(modelRow);
        LocalDate today = LocalDate.now();
        item.toggleDate(today);

        tableModel.fireTableRowsUpdated(modelRow, modelRow);
        heatmapPanel.repaint();
        statusLabel.setText("● Toggled completion for '" + item.getTitle() + "' on " + today);
    }

    private void deleteSelectedHabit() {
        int selectedRow = habitTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a habit to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = habitTable.convertRowIndexToModel(selectedRow);
        HabitItem item = habits.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + item.getTitle() + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            habits.remove(modelRow);
            tableModel.fireTableDataChanged();
            heatmapPanel.setSelectedHabit(habits.isEmpty() ? null : habits.get(0));
            totalCountLabel.setText("Total Habits: " + habits.size() + " | Today: " + LocalDate.now());
            statusLabel.setText("● Deleted habit '" + item.getTitle() + "'");
        }
    }

    private void openAnalyticsDashboard() {
        SwingUtilities.invokeLater(() -> {
            HabitAnalyticsDashboardFrame analyticsFrame = new HabitAnalyticsDashboardFrame(habits);
            analyticsFrame.setVisible(true);
        });
    }

    /**
     * Demonstrates multi-threaded asynchronous data loading using SwingWorker.
     * Keeps UI responsive while processing background tasks.
     */
    private void loadHabitDataAsync() {
        statusLabel.setText("● Syncing data in background thread (SwingWorker)...");
        statusLabel.setForeground(WARNING);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Simulate network latency / REST API fetch from Spring Boot backend
                Thread.sleep(600);
                publish("Connected to backend API. Synced " + habits.size() + " habits.");
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    statusLabel.setText("● " + chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                statusLabel.setText("● Synchronized successfully. Real-time updates active.");
                statusLabel.setForeground(SUCCESS);
                tableModel.fireTableDataChanged();
                heatmapPanel.repaint();
            }
        };

        worker.execute();
    }

    // =========================================================================
    // Custom Swing Component: ActivityHeatmapPanel
    // =========================================================================
    public static class ActivityHeatmapPanel extends JPanel {
        private HabitItem currentHabit;
        private final int DAYS_TO_SHOW = 30;
        private final List<LocalDate> dateRange = new ArrayList<>();
        private int hoveredIndex = -1;

        public ActivityHeatmapPanel(HabitItem habit) {
            this.currentHabit = habit;
            setBackground(BG_DARK);
            setPreferredSize(new Dimension(800, 240));

            LocalDate today = LocalDate.now();
            for (int i = DAYS_TO_SHOW - 1; i >= 0; i--) {
                dateRange.add(today.minusDays(i));
            }

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int oldHover = hoveredIndex;
                    hoveredIndex = getCellIndexAt(e.getPoint());
                    if (oldHover != hoveredIndex) {
                        repaint();
                        if (hoveredIndex >= 0 && hoveredIndex < dateRange.size() && currentHabit != null) {
                            LocalDate date = dateRange.get(hoveredIndex);
                            boolean done = currentHabit.isCompletedOn(date);
                            setToolTipText(date.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")) + " — " +
                                    (done ? "COMPLETED (Tick Active)" : "NOT COMPLETED (Click to toggle)"));
                        } else {
                            setToolTipText(null);
                        }
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentHabit != null && hoveredIndex >= 0 && hoveredIndex < dateRange.size()) {
                        LocalDate date = dateRange.get(hoveredIndex);
                        currentHabit.toggleDate(date);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredIndex = -1;
                    repaint();
                }
            });
        }

        public void setSelectedHabit(HabitItem habit) {
            this.currentHabit = habit;
            repaint();
        }

        private int getCellIndexAt(Point p) {
            int startX = 40;
            int startY = 85;
            int cellSize = 28;
            int gap = 8;
            int columns = 10;

            for (int i = 0; i < dateRange.size(); i++) {
                int col = i % columns;
                int row = i / columns;
                int x = startX + col * (cellSize + gap);
                int y = startY + row * (cellSize + gap);

                if (p.x >= x && p.x <= x + cellSize && p.y >= y && p.y <= y + cellSize) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (currentHabit == null) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 15));
                g2.drawString("Select a habit from the table above to view its 30-day activity matrix.", 40, 60);
                g2.dispose();
                return;
            }

            // Header info
            g2.setColor(TEXT_LIGHT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
            g2.drawString("Tracking: " + currentHabit.getTitle(), 40, 35);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.setColor(TEXT_MUTED);
            g2.drawString("Category: " + currentHabit.getCategory() + "   |   Current Streak: " +
                    currentHabit.getCurrentStreak() + " days 🔥   |   Best Streak: " + currentHabit.getBestStreak() + " days", 40, 58);

            // Matrix Grid
            int startX = 40;
            int startY = 85;
            int cellSize = 28;
            int gap = 8;
            int columns = 10;

            for (int i = 0; i < dateRange.size(); i++) {
                LocalDate date = dateRange.get(i);
                int col = i % columns;
                int row = i / columns;
                int x = startX + col * (cellSize + gap);
                int y = startY + row * (cellSize + gap);

                boolean completed = currentHabit.isCompletedOn(date);
                boolean isToday = date.equals(LocalDate.now());
                boolean isHovered = (i == hoveredIndex);

                // Cell background
                if (completed) {
                    GradientPaint gp = new GradientPaint(x, y, new Color(52, 211, 153), x + cellSize, y + cellSize, SUCCESS);
                    g2.setPaint(gp);
                } else {
                    g2.setColor(new Color(30, 41, 59));
                }

                RoundRectangle2D cell = new RoundRectangle2D.Float(x, y, cellSize, cellSize, 8, 8);
                g2.fill(cell);

                // Cell border
                if (isHovered) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.draw(cell);
                } else if (isToday) {
                    g2.setColor(WARNING);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.draw(cell);
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(cell);
                }

                // Day number inside cell
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.setColor(completed ? Color.BLACK : TEXT_MUTED);
                String dayStr = String.valueOf(date.getDayOfMonth());
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (cellSize - fm.stringWidth(dayStr)) / 2;
                int ty = y + ((cellSize - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(dayStr, tx, ty);
            }

            // Legend on right side
            int legendX = startX + columns * (cellSize + gap) + 40;
            int legendY = 95;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(TEXT_LIGHT);
            g2.drawString("Matrix Legend", legendX, legendY);

            // Inactive box
            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(legendX, legendY + 12, 16, 16, 4, 4);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(legendX, legendY + 12, 16, 16, 4, 4);
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("Missed / Unticked", legendX + 24, legendY + 25);

            // Active box
            g2.setColor(SUCCESS);
            g2.fillRoundRect(legendX, legendY + 36, 16, 16, 4, 4);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Completed (Ticked)", legendX + 24, legendY + 49);

            // Today indicator
            g2.setColor(WARNING);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(legendX, legendY + 60, 16, 16, 4, 4);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Today's Box", legendX + 24, legendY + 73);

            g2.dispose();
        }
    }

    // =========================================================================
    // Custom Table Model: AbstractTableModel implementation
    // =========================================================================
    public static class HabitTableModel extends AbstractTableModel {
        private final String[] columns = {"Habit Name", "Category", "Frequency", "Current Streak", "Completion Rate"};
        private final List<HabitItem> data;

        public HabitTableModel(List<HabitItem> data) {
            this.data = data;
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            HabitItem item = data.get(row);
            switch (col) {
                case 0: return item.getTitle();
                case 1: return item.getCategory();
                case 2: return item.getFrequency();
                case 3: return item.getCurrentStreak();
                case 4: return item.getCompletionRate();
                default: return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 3: return Integer.class;
                case 4: return Double.class;
                default: return String.class;
            }
        }
    }

    // =========================================================================
    // Custom Table Cell Renderers
    // =========================================================================
    public static class CategoryBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

            String text = val != null ? val.toString() : "";
            lbl.setText(text);

            if (!isSel) {
                lbl.setBackground(new Color(51, 65, 85));
                lbl.setForeground(new Color(199, 210, 254));
                lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
            }
            return lbl;
        }
    }

    public static class StreakCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
            int streak = val instanceof Integer ? (Integer) val : 0;
            lbl.setText(streak + " days " + (streak >= 5 ? "🔥" : "⚡"));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            if (!isSel) {
                lbl.setForeground(streak >= 5 ? new Color(251, 146, 60) : TEXT_LIGHT);
            }
            return lbl;
        }
    }

    public static class ProgressBarCellRenderer extends DefaultTableCellRenderer {
        private final JProgressBar progressBar = new JProgressBar(0, 100);

        public ProgressBarCellRenderer() {
            progressBar.setStringPainted(true);
            progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
            progressBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            progressBar.setBackground(new Color(30, 41, 59));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
            double rate = val instanceof Double ? (Double) val : 0.0;
            int intVal = (int) Math.round(rate);
            progressBar.setValue(intVal);
            progressBar.setString(intVal + "%");

            if (intVal >= 80) progressBar.setForeground(SUCCESS);
            else if (intVal >= 50) progressBar.setForeground(WARNING);
            else progressBar.setForeground(DANGER);

            return progressBar;
        }
    }

    // =========================================================================
    // Custom Modal Dialog: AddHabitDialog with GridBagLayout
    // =========================================================================
    public static class AddHabitDialog extends JDialog {
        private HabitItem createdHabit = null;
        private final JTextField titleField;
        private final JComboBox<String> categoryBox;
        private final JRadioButton dailyRadio;
        private final JRadioButton weeklyRadio;

        public AddHabitDialog(JFrame parent) {
            super(parent, "Add New ProgressGrid Activity / Habit", true);
            setSize(460, 360);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(CARD_BG);
            panel.setBorder(new EmptyBorder(20, 24, 20, 24));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Header
            JLabel header = new JLabel("Create a New Activity");
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            header.setForeground(TEXT_LIGHT);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            panel.add(header, gbc);

            // Title
            gbc.gridwidth = 1;
            gbc.gridx = 0; gbc.gridy = 1;
            JLabel titleLbl = new JLabel("Habit Title:");
            titleLbl.setForeground(TEXT_LIGHT);
            panel.add(titleLbl, gbc);

            gbc.gridx = 1;
            titleField = new JTextField(15);
            titleField.setPreferredSize(new Dimension(200, 30));
            titleField.setBackground(BG_DARK);
            titleField.setForeground(TEXT_LIGHT);
            titleField.setCaretColor(TEXT_LIGHT);
            panel.add(titleField, gbc);

            // Category
            gbc.gridx = 0; gbc.gridy = 2;
            JLabel catLbl = new JLabel("Category:");
            catLbl.setForeground(TEXT_LIGHT);
            panel.add(catLbl, gbc);

            gbc.gridx = 1;
            categoryBox = new JComboBox<>(new String[]{"Development", "Learning", "Fitness", "Education", "Health", "Academics", "Personal"});
            categoryBox.setBackground(BG_DARK);
            categoryBox.setForeground(TEXT_LIGHT);
            panel.add(categoryBox, gbc);

            // Frequency
            gbc.gridx = 0; gbc.gridy = 3;
            JLabel freqLbl = new JLabel("Frequency:");
            freqLbl.setForeground(TEXT_LIGHT);
            panel.add(freqLbl, gbc);

            gbc.gridx = 1;
            JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            radioPanel.setBackground(CARD_BG);
            dailyRadio = new JRadioButton("Daily", true);
            weeklyRadio = new JRadioButton("Weekly");
            dailyRadio.setBackground(CARD_BG);
            dailyRadio.setForeground(TEXT_LIGHT);
            weeklyRadio.setBackground(CARD_BG);
            weeklyRadio.setForeground(TEXT_LIGHT);

            ButtonGroup bg = new ButtonGroup();
            bg.add(dailyRadio);
            bg.add(weeklyRadio);
            radioPanel.add(dailyRadio);
            radioPanel.add(weeklyRadio);
            panel.add(radioPanel, gbc);

            // Buttons
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            gbc.insets = new Insets(20, 8, 8, 8);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btnPanel.setBackground(CARD_BG);

            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.setBackground(new Color(51, 65, 85));
            cancelBtn.setForeground(TEXT_LIGHT);
            cancelBtn.addActionListener(e -> dispose());

            JButton saveBtn = new JButton("Save Activity");
            saveBtn.setBackground(PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            saveBtn.addActionListener(e -> {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a habit title.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String cat = (String) categoryBox.getSelectedItem();
                String freq = dailyRadio.isSelected() ? "DAILY" : "WEEKLY";
                long id = System.currentTimeMillis();

                createdHabit = new HabitItem(id, title, cat, freq, 0, 0, 0.0);
                dispose();
            });

            btnPanel.add(cancelBtn);
            btnPanel.add(saveBtn);
            panel.add(btnPanel, gbc);

            setContentPane(panel);
        }

        public HabitItem getCreatedHabit() {
            return createdHabit;
        }
    }

    // =========================================================================
    // Independent Main Entry Point
    // =========================================================================
    public static void main(String[] args) {
        // Set System Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            HabitProgressGridFrame frame = new HabitProgressGridFrame();
            frame.setVisible(true);
        });
    }
}
