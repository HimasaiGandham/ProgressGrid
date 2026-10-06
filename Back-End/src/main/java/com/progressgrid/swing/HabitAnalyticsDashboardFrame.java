package com.progressgrid.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * HabitAnalyticsDashboardFrame - Advanced Visual Progress Visualization & Analytics Dashboard.
 * 
 * Demonstrates complex Java Swing concepts:
 * 1. Custom 2D Graphics Data Visualizations (Graphics2D, RenderingHints, GradientPaint, Arc2D, Path2D)
 * 2. Custom Radial Gauge (Circular Progress Indicator for Daily Progress)
 * 3. Custom Bar Chart with Hover Tooltips (Weekly Progress Bar Graph)
 * 4. Custom Polyline & Area Trend Chart (Monthly Consistency & Streak Graph)
 * 5. Custom Donut Chart (Category Distribution Breakdown)
 * 6. Smooth Animation Timers (javax.swing.Timer for 60fps easing progress fills)
 * 7. File Chooser Dialog (JFileChooser for CSV / Report Export)
 * 8. Compound Layout Management (BorderLayout, GridLayout, GridBagLayout, JTabbedPane)
 */
public class HabitAnalyticsDashboardFrame extends JFrame {

    // Themes & Colors
    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color SECONDARY = new Color(139, 92, 246);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color INFO = new Color(14, 165, 233);
    public static final Color BG_DARK = new Color(15, 23, 42);
    public static final Color CARD_BG = new Color(30, 41, 59);
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color BORDER_COLOR = new Color(51, 65, 85);

    private final List<HabitProgressGridFrame.HabitItem> habits;
    private CircularGaugePanel dailyGauge;
    private WeeklyBarChartPanel weeklyChart;
    private MonthlyTrendPanel trendChart;
    private CategoryDonutPanel donutChart;
    private JLabel statTotalHabits;
    private JLabel statTodayRate;
    private JLabel statBestStreak;
    private JLabel statWeeklyScore;

    public HabitAnalyticsDashboardFrame(List<HabitProgressGridFrame.HabitItem> habitList) {
        this.habits = (habitList != null && !habitList.isEmpty()) ? habitList : createDefaultHabitData();

        setTitle("ProgressGrid — Visual Progress & Habit Analytics Dashboard");
        setSize(1150, 820);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setupUI();
        triggerAnimations();
    }

    private List<HabitProgressGridFrame.HabitItem> createDefaultHabitData() {
        List<HabitProgressGridFrame.HabitItem> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        HabitProgressGridFrame.HabitItem h1 = new HabitProgressGridFrame.HabitItem(1, "Coding & DSA", "Development", "DAILY", 7, 14, 85.0);
        HabitProgressGridFrame.HabitItem h2 = new HabitProgressGridFrame.HabitItem(2, "Tech Reading", "Learning", "DAILY", 4, 10, 72.0);
        HabitProgressGridFrame.HabitItem h3 = new HabitProgressGridFrame.HabitItem(3, "Gym / Fitness", "Fitness", "DAILY", 12, 18, 90.0);
        HabitProgressGridFrame.HabitItem h4 = new HabitProgressGridFrame.HabitItem(4, "System Design", "Education", "WEEKLY", 3, 5, 60.0);
        HabitProgressGridFrame.HabitItem h5 = new HabitProgressGridFrame.HabitItem(5, "Drink 3L Water", "Health", "DAILY", 15, 15, 95.0);

        for (int i = 0; i < 30; i++) {
            LocalDate d = today.minusDays(i);
            if (i % 2 == 0) h1.getCompletionDates().add(d);
            if (i % 3 != 0) h2.getCompletionDates().add(d);
            if (i < 12 || i % 4 == 0) h3.getCompletionDates().add(d);
            if (i % 7 == 0) h4.getCompletionDates().add(d);
            if (i < 15) h5.getCompletionDates().add(d);
        }

        list.add(h1);
        list.add(h2);
        list.add(h3);
        list.add(h4);
        list.add(h5);
        return list;
    }

    private void setupUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);

        // 1. Header with Title & Action Controls
        JPanel topBar = createHeaderBar();
        root.add(topBar, BorderLayout.NORTH);

        // 2. Main Content Container
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setBackground(BG_DARK);
        mainContent.setBorder(new EmptyBorder(12, 18, 16, 18));

        // KPI Summary Cards
        JPanel kpiPanel = createKpiCardsPanel();
        mainContent.add(kpiPanel, BorderLayout.NORTH);

        // Charts Grid (2x2 layout for 4 custom data visualizers)
        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        chartsGrid.setBackground(BG_DARK);

        dailyGauge = new CircularGaugePanel(calculateDailyCompletionPercentage());
        weeklyChart = new WeeklyBarChartPanel(calculateWeeklyCompletion());
        trendChart = new MonthlyTrendPanel(calculateMonthlyTrend());
        donutChart = new CategoryDonutPanel(calculateCategoryDistribution());

        chartsGrid.add(createChartCard("Daily Progress (Circular Progress Indicator)", dailyGauge));
        chartsGrid.add(createChartCard("Weekly Progress (Day-by-Day Activity Graph)", weeklyChart));
        chartsGrid.add(createChartCard("30-Day Consistency Trend & Momentum", trendChart));
        chartsGrid.add(createChartCard("Category Distribution & Balance", donutChart));

        mainContent.add(chartsGrid, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        root.add(scrollPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(CARD_BG);

        JLabel icon = new JLabel("📈");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        JLabel title = new JLabel("Visual Progress & Habit Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel("— Performance metrics & consistency analysis");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        left.add(icon);
        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(CARD_BG);

        JButton replayBtn = new JButton("↻ Animate Charts");
        replayBtn.setBackground(new Color(51, 65, 85));
        replayBtn.setForeground(TEXT_LIGHT);
        replayBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        replayBtn.setFocusPainted(false);
        replayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        replayBtn.addActionListener(e -> triggerAnimations());

        JButton exportBtn = new JButton("📥 Export Report");
        exportBtn.setBackground(PRIMARY);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportReportToFile());

        JButton openGridBtn = new JButton("▦ Open Habit Grid");
        openGridBtn.setBackground(SUCCESS);
        openGridBtn.setForeground(Color.WHITE);
        openGridBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        openGridBtn.setFocusPainted(false);
        openGridBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openGridBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                HabitProgressGridFrame grid = new HabitProgressGridFrame();
                grid.setVisible(true);
            });
        });

        right.add(replayBtn);
        right.add(exportBtn);
        right.add(openGridBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createKpiCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setBackground(BG_DARK);

        int total = habits.size();
        int todayPercent = (int) Math.round(calculateDailyCompletionPercentage());
        int bestStreak = habits.stream().mapToInt(HabitProgressGridFrame.HabitItem::getBestStreak).max().orElse(0);
        int weeklyScore = (int) Math.round(calculateWeeklyAverageScore());

        statTotalHabits = new JLabel(String.valueOf(total));
        statTodayRate = new JLabel(todayPercent + "%");
        statBestStreak = new JLabel(bestStreak + " days");
        statWeeklyScore = new JLabel(weeklyScore + "%");

        panel.add(createKpiCard("TOTAL HABITS", statTotalHabits, "Tracked activities", PRIMARY));
        panel.add(createKpiCard("TODAY'S RATE", statTodayRate, "Activities completed", SUCCESS));
        panel.add(createKpiCard("LONGEST STREAK", statBestStreak, "Peak consistency record", WARNING));
        panel.add(createKpiCard("WEEKLY SCORE", statWeeklyScore, "7-day average pace", SECONDARY));

        return panel;
    }

    private JPanel createKpiCard(String label, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel titleLbl = new JLabel(label);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_LIGHT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createChartCard(String title, JComponent chart) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(TEXT_LIGHT);

        card.add(header, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private void triggerAnimations() {
        dailyGauge.startAnimation();
        weeklyChart.startAnimation();
        trendChart.startAnimation();
        donutChart.startAnimation();
    }

    private void exportReportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save ProgressGrid Habit Analytics Report");
        fileChooser.setSelectedFile(new File("ProgressGrid_Analytics_Report_" + LocalDate.now() + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Comma Delimited (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                writer.println("ProgressGrid Habit Performance & Analytics Report");
                writer.println("Generated Date," + LocalDate.now());
                writer.println("Total Tracked Habits," + habits.size());
                writer.println("Today Completion Rate," + String.format(Locale.US, "%.1f%%", calculateDailyCompletionPercentage()));
                writer.println();
                writer.println("Habit Name,Category,Frequency,Current Streak,Best Streak,Completion Rate %");

                for (HabitProgressGridFrame.HabitItem h : habits) {
                    writer.printf(Locale.US, "\"%s\",\"%s\",\"%s\",%d,%d,%.1f%%%n",
                            h.getTitle(), h.getCategory(), h.getFrequency(),
                            h.getCurrentStreak(), h.getBestStreak(), h.getCompletionRate());
                }

                JOptionPane.showMessageDialog(this,
                        "Report successfully exported to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export report: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Calculations
    private double calculateDailyCompletionPercentage() {
        if (habits.isEmpty()) return 0.0;
        LocalDate today = LocalDate.now();
        long completed = habits.stream().filter(h -> h.isCompletedOn(today)).count();
        return (completed * 100.0) / habits.size();
    }

    private double[] calculateWeeklyCompletion() {
        double[] days = new double[7]; // Monday to Sunday
        if (habits.isEmpty()) return days;

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            long done = habits.stream().filter(h -> h.isCompletedOn(date)).count();
            days[i] = (done * 100.0) / habits.size();
        }
        return days;
    }

    private double calculateWeeklyAverageScore() {
        double[] week = calculateWeeklyCompletion();
        double sum = 0;
        for (double v : week) sum += v;
        return sum / 7.0;
    }

    private double[] calculateMonthlyTrend() {
        double[] points = new double[30];
        if (habits.isEmpty()) return points;

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(29 - i);
            long done = habits.stream().filter(h -> h.isCompletedOn(date)).count();
            points[i] = (done * 100.0) / habits.size();
        }
        return points;
    }

    private Map<String, Integer> calculateCategoryDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (HabitProgressGridFrame.HabitItem h : habits) {
            map.put(h.getCategory(), map.getOrDefault(h.getCategory(), 0) + 1);
        }
        return map;
    }

    // =========================================================================
    // Visual Component 1: Circular Progress Gauge (Daily Progress)
    // =========================================================================
    public static class CircularGaugePanel extends JPanel {
        private final double targetPercentage;
        private double animatedPercentage = 0.0;
        private javax.swing.Timer timer;

        public CircularGaugePanel(double targetPercentage) {
            this.targetPercentage = targetPercentage;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(280, 220));
        }

        public void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            animatedPercentage = 0.0;
            timer = new javax.swing.Timer(16, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (animatedPercentage < targetPercentage) {
                        animatedPercentage += Math.max(0.5, (targetPercentage - animatedPercentage) * 0.12);
                        if (targetPercentage - animatedPercentage < 0.2) {
                            animatedPercentage = targetPercentage;
                            timer.stop();
                        }
                        repaint();
                    } else {
                        timer.stop();
                    }
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) - 40;
            int x = (width - size) / 2;
            int y = (height - size) / 2;

            int strokeWidth = 18;

            // Background Track
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(51, 65, 85, 120));
            g2.draw(new Arc2D.Double(x + strokeWidth / 2.0, y + strokeWidth / 2.0, size - strokeWidth, size - strokeWidth, 0, 360, Arc2D.OPEN));

            // Animated Foreground Arc
            double extent = -(animatedPercentage / 100.0) * 360.0;
            GradientPaint gradient = new GradientPaint(x, y, new Color(52, 211, 153), x + size, y + size, PRIMARY);
            g2.setPaint(gradient);
            g2.draw(new Arc2D.Double(x + strokeWidth / 2.0, y + strokeWidth / 2.0, size - strokeWidth, size - strokeWidth, 90, extent, Arc2D.OPEN));

            // Center Text
            g2.setColor(TEXT_LIGHT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            String text = String.format(Locale.US, "%.0f%%", animatedPercentage);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (width - fm.stringWidth(text)) / 2;
            int ty = (height / 2) + 2;
            g2.drawString(text, tx, ty);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(TEXT_MUTED);
            String sub = "Today's Target";
            fm = g2.getFontMetrics();
            g2.drawString(sub, (width - fm.stringWidth(sub)) / 2, ty + 20);

            g2.dispose();
        }
    }

    // =========================================================================
    // Visual Component 2: Weekly Bar Chart
    // =========================================================================
    public static class WeeklyBarChartPanel extends JPanel {
        private final double[] values;
        private double animProgress = 0.0;
        private javax.swing.Timer timer;
        private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        private int hoveredBar = -1;

        public WeeklyBarChartPanel(double[] values) {
            this.values = values;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(280, 220));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int old = hoveredBar;
                    hoveredBar = getBarAt(e.getX(), e.getY());
                    if (old != hoveredBar) {
                        repaint();
                        if (hoveredBar >= 0 && hoveredBar < values.length) {
                            setToolTipText(days[hoveredBar] + ": " + String.format(Locale.US, "%.1f%% completed", values[hoveredBar]));
                        } else {
                            setToolTipText(null);
                        }
                    }
                }
            });
        }

        private int getBarAt(int px, int py) {
            int width = getWidth() - 50;
            int startX = 35;
            int slotWidth = width / 7;
            int barWidth = Math.max(14, slotWidth - 14);

            for (int i = 0; i < 7; i++) {
                int bx = startX + i * slotWidth + (slotWidth - barWidth) / 2;
                if (px >= bx && px <= bx + barWidth && py >= 25 && py <= getHeight() - 35) {
                    return i;
                }
            }
            return -1;
        }

        public void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            animProgress = 0.0;
            timer = new javax.swing.Timer(16, e -> {
                animProgress += 0.05;
                if (animProgress >= 1.0) {
                    animProgress = 1.0;
                    timer.stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - 50;
            int height = getHeight() - 65;
            int startX = 35;
            int startY = 25;
            int baselineY = startY + height;

            // Grid lines (0%, 50%, 100%)
            g2.setColor(new Color(51, 65, 85, 100));
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
            g2.drawLine(startX, baselineY, startX + width, baselineY);
            g2.drawLine(startX, baselineY - height / 2, startX + width, baselineY - height / 2);
            g2.drawLine(startX, startY, startX + width, startY);

            int slotWidth = width / 7;
            int barWidth = Math.max(14, slotWidth - 14);

            for (int i = 0; i < 7; i++) {
                double targetH = (values[i] / 100.0) * height * animProgress;
                int currentH = (int) Math.round(targetH);
                int bx = startX + i * slotWidth + (slotWidth - barWidth) / 2;
                int by = baselineY - currentH;

                // Bar gradient
                boolean isHover = (i == hoveredBar);
                GradientPaint gp = new GradientPaint(bx, by, isHover ? new Color(129, 140, 248) : PRIMARY,
                        bx, baselineY, isHover ? PRIMARY : SECONDARY);
                g2.setPaint(gp);
                g2.fillRoundRect(bx, by, barWidth, currentH, 6, 6);

                // Bar Top Value Label
                if (currentH > 15) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.setColor(TEXT_LIGHT);
                    String valText = String.format(Locale.US, "%.0f", values[i]);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(valText, bx + (barWidth - fm.stringWidth(valText)) / 2, by - 4);
                }

                // Day Label
                g2.setFont(new Font("Segoe UI", isHover ? Font.BOLD : Font.PLAIN, 11));
                g2.setColor(isHover ? TEXT_LIGHT : TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(days[i], bx + (barWidth - fm.stringWidth(days[i])) / 2, baselineY + 16);
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // Visual Component 3: 30-Day Monthly Trend Chart
    // =========================================================================
    public static class MonthlyTrendPanel extends JPanel {
        private final double[] values;
        private double animProgress = 0.0;
        private javax.swing.Timer timer;

        public MonthlyTrendPanel(double[] values) {
            this.values = values;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(280, 220));
        }

        public void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            animProgress = 0.0;
            timer = new javax.swing.Timer(16, e -> {
                animProgress += 0.04;
                if (animProgress >= 1.0) {
                    animProgress = 1.0;
                    timer.stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - 40;
            int height = getHeight() - 50;
            int startX = 25;
            int startY = 20;
            int baselineY = startY + height;

            if (values.length < 2) {
                g2.dispose();
                return;
            }

            // Grid lines
            g2.setColor(new Color(51, 65, 85, 90));
            g2.drawLine(startX, baselineY, startX + width, baselineY);
            g2.drawLine(startX, startY + height / 2, startX + width, startY + height / 2);

            Path2D.Double path = new Path2D.Double();
            Path2D.Double areaPath = new Path2D.Double();

            double stepX = (double) width / (values.length - 1);

            for (int i = 0; i < values.length; i++) {
                double x = startX + i * stepX;
                double scaledY = (values[i] / 100.0) * height * animProgress;
                double y = baselineY - scaledY;

                if (i == 0) {
                    path.moveTo(x, y);
                    areaPath.moveTo(x, baselineY);
                    areaPath.lineTo(x, y);
                } else {
                    path.lineTo(x, y);
                    areaPath.lineTo(x, y);
                }
            }

            areaPath.lineTo(startX + width, baselineY);
            areaPath.closePath();

            // Draw Area Fill
            GradientPaint areaPaint = new GradientPaint(startX, startY, new Color(79, 70, 229, 90),
                    startX, baselineY, new Color(79, 70, 229, 5));
            g2.setPaint(areaPaint);
            g2.fill(areaPath);

            // Draw Stroke Line
            g2.setColor(new Color(129, 140, 248));
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);

            // Draw Point Knots
            for (int i = 0; i < values.length; i += 3) {
                double x = startX + i * stepX;
                double y = baselineY - (values[i] / 100.0) * height * animProgress;
                g2.setColor(CARD_BG);
                g2.fillOval((int) x - 4, (int) y - 4, 8, 8);
                g2.setColor(SUCCESS);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawOval((int) x - 4, (int) y - 4, 8, 8);
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // Visual Component 4: Category Donut Chart
    // =========================================================================
    public static class CategoryDonutPanel extends JPanel {
        private final Map<String, Integer> distribution;
        private double animAngle = 0.0;
        private javax.swing.Timer timer;
        private final Color[] sliceColors = {
                new Color(79, 70, 229),
                new Color(16, 185, 129),
                new Color(245, 158, 11),
                new Color(239, 68, 68),
                new Color(14, 165, 233),
                new Color(236, 72, 153)
        };

        public CategoryDonutPanel(Map<String, Integer> distribution) {
            this.distribution = distribution;
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(280, 220));
        }

        public void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            animAngle = 0.0;
            timer = new javax.swing.Timer(16, e -> {
                animAngle += 12.0;
                if (animAngle >= 360.0) {
                    animAngle = 360.0;
                    timer.stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width / 2, height - 30);
            int cx = 20 + size / 2;
            int cy = height / 2;

            int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) {
                g2.dispose();
                return;
            }

            double startAngle = 90.0;
            int colorIdx = 0;
            int legendX = cx + size / 2 + 25;
            int legendY = 35;

            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                double sliceAngle = (entry.getValue() / (double) total) * 360.0;
                double effectiveExtent = Math.min(sliceAngle, Math.max(0, animAngle - (90.0 - startAngle)));

                Color color = sliceColors[colorIdx % sliceColors.length];
                g2.setColor(color);
                g2.fill(new Arc2D.Double(cx - size / 2, cy - size / 2, size, size, -startAngle, -effectiveExtent, Arc2D.PIE));

                // Legend entry
                g2.fillRoundRect(legendX, legendY, 12, 12, 3, 3);
                g2.setColor(TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(entry.getKey() + " (" + entry.getValue() + ")", legendX + 18, legendY + 10);
                legendY += 22;

                startAngle += sliceAngle;
                colorIdx++;
            }

            // Hollow center (Donut hole)
            int innerSize = (int) (size * 0.58);
            g2.setColor(CARD_BG);
            g2.fillOval(cx - innerSize / 2, cy - innerSize / 2, innerSize, innerSize);

            // Center count
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(TEXT_LIGHT);
            String centerStr = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(centerStr, cx - fm.stringWidth(centerStr) / 2, cy + 4);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(TEXT_MUTED);
            String sub = "Habits";
            fm = g2.getFontMetrics();
            g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 18);

            g2.dispose();
        }
    }

    // =========================================================================
    // Independent Main Entry Point
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            HabitAnalyticsDashboardFrame frame = new HabitAnalyticsDashboardFrame(null);
            frame.setVisible(true);
        });
    }
}
