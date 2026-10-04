package com.progressgrid.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ProgressGridSwingLauncher - Unified Desktop Launcher for the ProgressGrid Swing Suite.
 * Allows running the Interactive Habit Progress Grid or the Visual Analytics Dashboard.
 */
public class ProgressGridSwingLauncher {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ProgressGrid — Desktop Swing Suite Launcher");
            frame.setSize(520, 360);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            JPanel root = new JPanel(new BorderLayout(0, 16));
            root.setBackground(HabitProgressGridFrame.BG_DARK);
            root.setBorder(new EmptyBorder(24, 28, 24, 28));

            // Header
            JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
            header.setBackground(HabitProgressGridFrame.BG_DARK);

            JLabel title = new JLabel("ProgressGrid Desktop Suite", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(HabitProgressGridFrame.TEXT_LIGHT);

            JLabel subtitle = new JLabel("Select an application module to launch:", SwingConstants.CENTER);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(HabitProgressGridFrame.TEXT_MUTED);

            header.add(title);
            header.add(subtitle);
            root.add(header, BorderLayout.NORTH);

            // Button Options
            JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 14));
            buttons.setBackground(HabitProgressGridFrame.BG_DARK);
            buttons.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnGrid = new JButton("▦ Launch Habit Tracking Matrix & Progress Grid");
            btnGrid.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnGrid.setBackground(HabitProgressGridFrame.PRIMARY);
            btnGrid.setForeground(Color.WHITE);
            btnGrid.setFocusPainted(false);
            btnGrid.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnGrid.addActionListener(e -> {
                frame.dispose();
                HabitProgressGridFrame grid = new HabitProgressGridFrame();
                grid.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                grid.setVisible(true);
            });

            JButton btnAnalytics = new JButton("📈 Launch Visual Analytics & Progress Dashboard");
            btnAnalytics.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnAnalytics.setBackground(HabitProgressGridFrame.SUCCESS);
            btnAnalytics.setForeground(Color.WHITE);
            btnAnalytics.setFocusPainted(false);
            btnAnalytics.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAnalytics.addActionListener(e -> {
                frame.dispose();
                HabitAnalyticsDashboardFrame analytics = new HabitAnalyticsDashboardFrame(null);
                analytics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                analytics.setVisible(true);
            });

            buttons.add(btnGrid);
            buttons.add(btnAnalytics);
            root.add(buttons, BorderLayout.CENTER);

            // Footer
            JLabel footer = new JLabel("Built with Core Java Swing & Java 2D Graphics", SwingConstants.CENTER);
            footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            footer.setForeground(HabitProgressGridFrame.TEXT_MUTED);
            root.add(footer, BorderLayout.SOUTH);

            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }
}
