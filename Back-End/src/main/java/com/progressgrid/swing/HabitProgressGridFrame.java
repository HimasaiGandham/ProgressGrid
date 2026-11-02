package com.progressgrid.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

/**
 * HabitProgressGridFrame - Pixel-perfect desktop implementation matching
 * the ProgressGrid modern light web dashboard (himasaigandham.github.io/ProgressGrid).
 */
public class HabitProgressGridFrame extends JFrame {

    // Theme Color Palette matching the web application
    public static final Color BG_PAGE = new Color(248, 250, 252);        // #f8fafc Light Gray
    public static final Color CARD_BG = Color.WHITE;                      // #ffffff Pure White
    public static final Color BRAND_GREEN = new Color(16, 185, 129);     // #10b981 Emerald Green
    public static final Color BRAND_GREEN_DARK = new Color(5, 150, 105); // #059669
    public static final Color BRAND_GREEN_LIGHT = new Color(236, 253, 245);// #ecfdf5 Mint Pill
    public static final Color TEXT_DARK = new Color(17, 24, 39);          // #111827 Dark Slate
    public static final Color TEXT_MUTED = new Color(107, 114, 128);      // #6b7280 Cool Gray
    public static final Color BORDER_COLOR = new Color(229, 231, 235);    // #e5e7eb Divider Gray
    public static final Color DANGER_RED = new Color(239, 68, 68);        // #ef4444 Soft Red

    // Legacy color constants for compatibility with launcher/sub-frames
    public static final Color PRIMARY = BRAND_GREEN;
    public static final Color PRIMARY_DARK = BRAND_GREEN_DARK;
    public static final Color SUCCESS = BRAND_GREEN;
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = DANGER_RED;
    public static final Color BG_DARK = BG_PAGE;
    public static final Color TEXT_LIGHT = TEXT_DARK;

    // Data structures
    public static class HabitItem {
        private final long id;
        private String title;
        private String category;
        private String frequency; // "Daily" or "Weekly"
        private LocalDate startDate;
        private int currentStreak;
        private int bestStreak;
        private double completionRate;
        private final Set<LocalDate> completionDates = new HashSet<>();

        public HabitItem(long id, String title, String category, String frequency, LocalDate startDate, int currentStreak, int bestStreak, double completionRate) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.frequency = frequency;
            this.startDate = startDate != null ? startDate : LocalDate.now().minusDays(20);
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
            this.completionRate = completionRate;
        }

        public HabitItem(long id, String title, String category, String frequency, int currentStreak, int bestStreak, double completionRate) {
            this(id, title, category, frequency, LocalDate.now().minusDays(20), currentStreak, bestStreak, completionRate);
        }

        public long getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public LocalDate getStartDate() { return startDate; }
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
            } else {
                completionDates.add(date);
            }
            recalculateMetrics();
        }

        public void recalculateMetrics() {
            LocalDate today = LocalDate.now();
            int streak = 0;
            LocalDate checkDate = today;

            // Calculate streak backward
            if (!completionDates.contains(checkDate)) {
                checkDate = checkDate.minusDays(1);
            }
            while (completionDates.contains(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            }

            this.currentStreak = streak;
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }

            int totalPeriod = 30;
            this.completionRate = Math.min(100.0, (completionDates.size() * 100.0) / totalPeriod);
        }
    }

    private final List<HabitItem> habits = new ArrayList<>();
    private LocalDate currentWeekStart;

    // Stat metric labels
    private JLabel totalHabitsValue;
    private JLabel currentStreakValue;
    private JLabel bestStreakValue;
    private JLabel weeklyCompletionValue;
    private JLabel overallCompletionValue;
    private JPanel gridRowsPanel;
    private JLabel weekRangeLabel;

    public HabitProgressGridFrame() {
        setTitle("ProgressGrid — Habit Tracker");
        setSize(1280, 820);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Align current week to Monday
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        initializeDefaultHabits();
        setupLayout();
        refreshAllStats();
    }

    private void initializeDefaultHabits() {
        // Matches exact items from user screenshot
        LocalDate startDate = LocalDate.of(2026, 10, 6);

        HabitItem h1 = new HabitItem(1, "Swiming", "Health", "Daily", startDate, 0, 3, 14.0);
        HabitItem h2 = new HabitItem(2, "Leetcode", "Health", "Daily", startDate, 0, 2, 10.0);
        HabitItem h3 = new HabitItem(3, "3 Liters of water", "Health", "Daily", startDate, 0, 3, 18.0);

        habits.add(h1);
        habits.add(h2);
        habits.add(h3);
    }

    private void setupLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PAGE);

        // 1. Left Sidebar Navigation
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Main Content Dashboard
        JPanel mainContent = createMainContent();
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BG_PAGE);

        root.add(scrollPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ----------------------------------------------------------------------------------------
    // LEFT SIDEBAR (Matching Sidebar in Screenshot)
    // ----------------------------------------------------------------------------------------
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(CARD_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Top Brand Logo
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 24));
        brandPanel.setBackground(CARD_BG);

        // 4-Squares Icon
        JLabel logoIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BRAND_GREEN);
                int s = 7;
                int r = 2;
                g2.fillRoundRect(0, 0, s, s, r, r);
                g2.fillRoundRect(10, 0, s, s, r, r);
                g2.fillRoundRect(0, 10, s, s, r, r);
                g2.fillRoundRect(10, 10, s, s, r, r);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };

        JLabel brandText = new JLabel("ProgressGrid");
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandText.setForeground(BRAND_GREEN);

        brandPanel.add(logoIcon);
        brandPanel.add(brandText);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Sidebar Navigation Links
        JPanel navMenu = new JPanel();
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));
        navMenu.setBackground(CARD_BG);
        navMenu.setBorder(new EmptyBorder(10, 14, 10, 14));

        navMenu.add(createNavItem("Dashboard", "▦", true, null));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavItem("My Habits", "✔", false, null));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavItem("Statistics", "📊", false, e -> openAnalyticsDashboard()));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavItem("Calendar", "📅", false, null));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavItem("Profile Settings", "⚙", false, null));

        sidebar.add(navMenu, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel createNavItem(String text, String icon, boolean active, ActionListener action) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (active) {
            item.setBackground(BRAND_GREEN_LIGHT);
        } else {
            item.setBackground(CARD_BG);
        }

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
        lblIcon.setForeground(active ? BRAND_GREEN : TEXT_MUTED);

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
        lblText.setForeground(active ? BRAND_GREEN : new Color(75, 85, 99));

        item.add(lblIcon);
        item.add(lblText);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) action.actionPerformed(null);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) item.setBackground(new Color(243, 244, 246));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) item.setBackground(CARD_BG);
            }
        });

        return item;
    }

    // ----------------------------------------------------------------------------------------
    // MAIN DASHBOARD CONTENT
    // ----------------------------------------------------------------------------------------
    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_PAGE);
        content.setBorder(new EmptyBorder(26, 36, 36, 36));

        // 1. Top Header Row (Title on Left, User & Controls on Right)
        content.add(createHeaderBar());
        content.add(Box.createVerticalStrut(24));

        // 2. Five Metric KPI Cards Row
        content.add(createMetricCardsRow());
        content.add(Box.createVerticalStrut(26));

        // 3. Habit Grid Card
        content.add(createHabitGridCard());

        return content;
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PAGE);

        // Left Titles
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setBackground(BG_PAGE);

        JLabel title = new JLabel("Habit Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        JLabel subtitle = new JLabel(monthYear);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        // Right Controls: Profile Badge + Date Navigator + "+ Add Habit" Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setBackground(BG_PAGE);

        // User Avatar Badge
        JPanel userBadge = createUserBadge();
        rightPanel.add(userBadge);

        // Date Range Selector
        JPanel dateNav = createDateNavigator();
        rightPanel.add(dateNav);

        // "+ Add Habit" Button
        JButton btnAddHabit = new JButton("+ Add Habit") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(BRAND_GREEN_DARK);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(13, 148, 136));
                } else {
                    g2.setColor(BRAND_GREEN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAddHabit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddHabit.setForeground(Color.WHITE);
        btnAddHabit.setContentAreaFilled(false);
        btnAddHabit.setBorderPainted(false);
        btnAddHabit.setFocusPainted(false);
        btnAddHabit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddHabit.setPreferredSize(new Dimension(115, 36));
        btnAddHabit.addActionListener(e -> showAddHabitDialog());

        rightPanel.add(btnAddHabit);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createUserBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        badge.setBackground(CARD_BG);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(2, 6, 2, 10)
        ));

        String displayName = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getDisplayName() : "Himasai Gandham";
        String initial = displayName.isEmpty() ? "U" : displayName.substring(0, 1).toUpperCase();
        String subText = UserSession.isLoggedIn() ? UserSession.getCurrentUser().getEmail() : "View Profile  ⌄";

        // Circular Green Avatar
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BRAND_GREEN);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(26, 26));

        JPanel nameBox = new JPanel(new GridLayout(2, 1, 0, 0));
        nameBox.setBackground(CARD_BG);

        JLabel userName = new JLabel(displayName);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userName.setForeground(TEXT_DARK);

        JLabel userRole = new JLabel(subText);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        userRole.setForeground(TEXT_MUTED);

        nameBox.add(userName);
        nameBox.add(userRole);

        badge.add(avatar);
        badge.add(nameBox);
        return badge;
    }

    private JPanel createDateNavigator() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        nav.setBackground(CARD_BG);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        JButton btnPrev = new JButton("<");
        styleNavButton(btnPrev);
        btnPrev.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekDisplay();
        });

        weekRangeLabel = new JLabel(formatWeekRange(currentWeekStart));
        weekRangeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        weekRangeLabel.setForeground(TEXT_DARK);

        JButton btnNext = new JButton(">");
        styleNavButton(btnNext);
        btnNext.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekDisplay();
        });

        nav.add(btnPrev);
        nav.add(weekRangeLabel);
        nav.add(btnNext);
        return nav;
    }

    private void styleNavButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private String formatWeekRange(LocalDate monday) {
        LocalDate sunday = monday.plusDays(6);
        DateTimeFormatter mFmt = DateTimeFormatter.ofPattern("MMM d");
        return monday.format(mFmt) + " - " + sunday.format(mFmt);
    }

    private void updateWeekDisplay() {
        weekRangeLabel.setText(formatWeekRange(currentWeekStart));
        refreshHabitGridRows();
    }

    // ----------------------------------------------------------------------------------------
    // FIVE STAT METRIC CARDS ROW (Exact match to screenshot)
    // ----------------------------------------------------------------------------------------
    private JPanel createMetricCardsRow() {
        JPanel cardsRow = new JPanel(new GridLayout(1, 5, 16, 0));
        cardsRow.setBackground(BG_PAGE);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        totalHabitsValue = new JLabel("3");
        currentStreakValue = new JLabel("0 days");
        bestStreakValue = new JLabel("3 days");
        weeklyCompletionValue = new JLabel("0%");
        overallCompletionValue = new JLabel("14%");

        cardsRow.add(buildStatCard("TOTAL HABITS", totalHabitsValue));
        cardsRow.add(buildStatCard("CURRENT STREAK", currentStreakValue));
        cardsRow.add(buildStatCard("BEST STREAK", bestStreakValue));
        cardsRow.add(buildStatCard("WEEKLY COMPLETION", weeklyCompletionValue));
        cardsRow.add(buildStatCard("OVERALL COMPLETION", overallCompletionValue));

        return cardsRow;
    }

    private JPanel buildStatCard(String labelText, JLabel valLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridLayout(2, 1, 0, 4));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel(labelText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLabel.setForeground(BRAND_GREEN);

        card.add(title);
        card.add(valLabel);
        return card;
    }

    // ----------------------------------------------------------------------------------------
    // HABIT GRID CARD (Interactive 7-Day Checklist Matrix)
    // ----------------------------------------------------------------------------------------
    private JPanel createHabitGridCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 26, 24, 26));

        // Card Header Title
        JLabel cardTitle = new JLabel("Habit Grid");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        cardTitle.setForeground(TEXT_DARK);
        cardTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(cardTitle, BorderLayout.NORTH);

        // Container for Column Headers + Habit Rows
        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));
        tableContainer.setBackground(CARD_BG);

        // Header Row: "Habit" on left, Mon-Sun dates on right
        tableContainer.add(createDaysHeaderRow());
        tableContainer.add(createDividerLine());

        // Dynamic Rows Panel
        gridRowsPanel = new JPanel();
        gridRowsPanel.setLayout(new BoxLayout(gridRowsPanel, BoxLayout.Y_AXIS));
        gridRowsPanel.setBackground(CARD_BG);

        tableContainer.add(gridRowsPanel);
        card.add(tableContainer, BorderLayout.CENTER);

        refreshHabitGridRows();
        return card;
    }

    private JPanel createDaysHeaderRow() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(new EmptyBorder(6, 4, 10, 4));

        JLabel habitCol = new JLabel("Habit");
        habitCol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        habitCol.setForeground(TEXT_MUTED);
        header.add(habitCol, BorderLayout.WEST);

        // 7 Day Headers Panel
        JPanel daysBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        daysBox.setBackground(CARD_BG);

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            LocalDate d = currentWeekStart.plusDays(i);
            JPanel dayCell = new JPanel(new GridLayout(2, 1, 0, 1));
            dayCell.setPreferredSize(new Dimension(32, 34));
            dayCell.setBackground(CARD_BG);

            JLabel nameLbl = new JLabel(dayNames[i], SwingConstants.CENTER);
            nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            nameLbl.setForeground(TEXT_MUTED);

            JLabel dateLbl = new JLabel(String.valueOf(d.getDayOfMonth()), SwingConstants.CENTER);
            dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLbl.setForeground(TEXT_MUTED);

            dayCell.add(nameLbl);
            dayCell.add(dateLbl);
            daysBox.add(dayCell);
        }

        header.add(daysBox, BorderLayout.EAST);
        return header;
    }

    private JComponent createDividerLine() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void refreshHabitGridRows() {
        gridRowsPanel.removeAll();

        for (int i = 0; i < habits.size(); i++) {
            HabitItem habit = habits.get(i);
            gridRowsPanel.add(createHabitRow(habit));
            if (i < habits.size() - 1) {
                gridRowsPanel.add(createDividerLine());
            }
        }

        gridRowsPanel.revalidate();
        gridRowsPanel.repaint();
    }

    private JPanel createHabitRow(HabitItem habit) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD_BG);
        row.setBorder(new EmptyBorder(14, 4, 14, 4));

        // Left Side: Habit Title & Metadata (e.g. "Health • Daily • From Oct 6")
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        infoPanel.setBackground(CARD_BG);

        JLabel titleLbl = new JLabel(habit.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(TEXT_DARK);

        String fromDateStr = habit.getStartDate().format(DateTimeFormatter.ofPattern("MMM d"));
        JLabel metaLbl = new JLabel(habit.getCategory() + " • " + habit.getFrequency() + " • From " + fromDateStr);
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLbl.setForeground(TEXT_MUTED);

        infoPanel.add(titleLbl);
        infoPanel.add(metaLbl);
        row.add(infoPanel, BorderLayout.WEST);

        // Right Side: Delete Button "×" + 7 Interactive Rounded Checkboxes
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        rightBox.setBackground(CARD_BG);

        // Delete "×" button
        JButton btnDelete = new JButton("×");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnDelete.setForeground(new Color(209, 213, 219));
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorder(null);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.setToolTipText("Delete habit");
        btnDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btnDelete.setForeground(DANGER_RED); }
            @Override
            public void mouseExited(MouseEvent e) { btnDelete.setForeground(new Color(209, 213, 219)); }
        });
        btnDelete.addActionListener(e -> {
            habits.remove(habit);
            refreshAllStats();
            refreshHabitGridRows();
        });

        rightBox.add(btnDelete);

        // 7 Checkbox Buttons (Mon - Sun)
        for (int d = 0; d < 7; d++) {
            LocalDate date = currentWeekStart.plusDays(d);
            JComponent checkBtn = createCheckboxButton(habit, date);
            rightBox.add(checkBtn);
        }

        row.add(rightBox, BorderLayout.EAST);
        return row;
    }

    private JComponent createCheckboxButton(HabitItem habit, LocalDate date) {
        JComponent btn = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean checked = habit.isCompletedOn(date);
                int w = getWidth() - 2;
                int h = getHeight() - 2;
                int arc = 10;

                if (checked) {
                    // Solid Green Fill
                    g2.setColor(BRAND_GREEN);
                    g2.fillRoundRect(1, 1, w, h, arc, arc);

                    // White Checkmark
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(w / 4 + 1, h / 2, w / 2, (int) (h * 0.72));
                    g2.drawLine(w / 2, (int) (h * 0.72), (int) (w * 0.78), h / 4 + 1);
                } else {
                    // Empty rounded box with soft border
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(1, 1, w, h, arc, arc);
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, w, h, arc, arc);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(28, 28);
            }
        };

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                habit.toggleDate(date);
                btn.repaint();
                refreshAllStats();
            }
        });

        return btn;
    }

    private void refreshAllStats() {
        int total = habits.size();
        totalHabitsValue.setText(String.valueOf(total));

        int currentStreak = 0;
        int bestStreak = 0;
        double sumCompletion = 0;

        for (HabitItem h : habits) {
            if (h.getCurrentStreak() > currentStreak) currentStreak = h.getCurrentStreak();
            if (h.getBestStreak() > bestStreak) bestStreak = h.getBestStreak();
            sumCompletion += h.getCompletionRate();
        }

        currentStreakValue.setText(currentStreak + " days");
        bestStreakValue.setText(bestStreak + " days");

        int weeklyCount = 0;
        for (HabitItem h : habits) {
            for (int i = 0; i < 7; i++) {
                if (h.isCompletedOn(currentWeekStart.plusDays(i))) {
                    weeklyCount++;
                }
            }
        }
        int totalWeeklySlots = Math.max(1, total * 7);
        int weeklyPct = (weeklyCount * 100) / totalWeeklySlots;
        weeklyCompletionValue.setText(weeklyPct + "%");

        int overallPct = total > 0 ? (int) Math.round(sumCompletion / total) : 0;
        overallCompletionValue.setText(overallPct + "%");
    }

    // ----------------------------------------------------------------------------------------
    // ADD HABIT MODAL DIALOG
    // ----------------------------------------------------------------------------------------
    private void showAddHabitDialog() {
        JDialog dialog = new JDialog(this, "Add New Habit", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 2, 12, 14));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        panel.add(new JLabel("Habit Name:"));
        JTextField nameField = new JTextField("Morning Meditation");
        panel.add(nameField);

        panel.add(new JLabel("Category:"));
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Health", "Fitness", "Study", "Productivity"});
        panel.add(categoryBox);

        panel.add(new JLabel("Frequency:"));
        JComboBox<String> freqBox = new JComboBox<>(new String[]{"Daily", "Weekly"});
        panel.add(freqBox);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton("Save Habit");
        btnSave.setBackground(BRAND_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                HabitItem item = new HabitItem(
                        System.currentTimeMillis(),
                        name,
                        (String) categoryBox.getSelectedItem(),
                        (String) freqBox.getSelectedItem(),
                        LocalDate.now(),
                        0, 0, 0.0
                );
                habits.add(item);
                refreshAllStats();
                refreshHabitGridRows();
                dialog.dispose();
            }
        });

        panel.add(btnCancel);
        panel.add(btnSave);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void openAnalyticsDashboard() {
        HabitAnalyticsDashboardFrame analytics = new HabitAnalyticsDashboardFrame(habits);
        analytics.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HabitProgressGridFrame frame = new HabitProgressGridFrame();
            frame.setVisible(true);
        });
    }
}
