package com.progressgrid.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ProgressGridSwingLauncher - Unified Desktop Launcher for the ProgressGrid Swing Suite.
 * Allows running the Interactive Habit Progress Grid or the Visual Analytics Dashboard.
 */
// ============================================================================================
// [SWING - JFRAME LAUNCHER]: Standalone Entry Point & Module Selector
// ProgressGridSwingLauncher demonstrates instantiating a top-level JFrame directly,
// managing window closing behaviors, and launching other JFrames on the Event Dispatch Thread (EDT).
// ============================================================================================
public class ProgressGridSwingLauncher {

    // [CONFIG]: Set to true to show the login screen first; set to false to bypass login.
    public static boolean REQUIRE_LOGIN = true;

    // ========================================================================================
    // [SWING - APPLICATION ENTRY POINT & EDT]: main() Launch Method
    // ========================================================================================
    public static void main(String[] args) {
        if (REQUIRE_LOGIN) {
            SwingLoginDemo.showLogin();
        } else {
            showLauncher();
        }
    }

    public static void showLauncher() {
        // [SWING LOOK AND FEEL]: Set System Look and Feel for native operating system styling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // [SWING THREAD SAFETY]: Initialize and display GUI components on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // [SWING - JFRAME INSTANTIATION]: Direct instantiation of JFrame with title
            JFrame frame = new JFrame("ProgressGrid — Desktop Swing Suite Launcher");

            // [JFRAME CONFIGURATION]: Configure window dimensions, center positioning, and close policy
            frame.setSize(520, 360);                                 // Window width & height
            frame.setLocationRelativeTo(null);                       // Center on screen
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // Terminate entire JVM on close
            frame.setResizable(false);                               // Disable window resizing

            // [SWING CONTAINER]: Root JPanel with BorderLayout and 16px vertical gap
            JPanel root = new JPanel(new BorderLayout(0, 16));
            root.setBackground(HabitProgressGridFrame.BG_DARK);
            root.setBorder(new EmptyBorder(24, 28, 24, 28));

            // ---------------- Header Panel ----------------
            // [SWING CONTAINER & LAYOUT]: Header panel with 2-row GridLayout
            JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
            header.setBackground(HabitProgressGridFrame.BG_DARK);

            // [SWING COMPONENT]: JLabels for title and instructions
            JLabel title = new JLabel("ProgressGrid Desktop Suite", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(HabitProgressGridFrame.TEXT_LIGHT);

            JLabel subtitle = new JLabel("Select an application module to launch:", SwingConstants.CENTER);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(HabitProgressGridFrame.TEXT_MUTED);

            header.add(title);
            header.add(subtitle);
            root.add(header, BorderLayout.NORTH);

            // ---------------- Button Options Panel ----------------
            // [SWING CONTAINER & LAYOUT]: 2-row GridLayout for launcher action buttons
            JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 14));
            buttons.setBackground(HabitProgressGridFrame.BG_DARK);
            buttons.setBorder(new EmptyBorder(10, 10, 10, 10));

            // [SWING COMPONENT]: JButton to launch HabitProgressGridFrame
            JButton btnGrid = new JButton("Launch Habit Tracking Matrix & Progress Grid");
            btnGrid.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnGrid.setBackground(HabitProgressGridFrame.PRIMARY);
            btnGrid.setForeground(Color.BLACK);
            btnGrid.setFocusPainted(false);
            btnGrid.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnGrid.addActionListener(e -> {
                // [JFRAME METHOD]: Dispose launcher window
                frame.dispose();

                // [SWING - JFRAME INSTANTIATION]: Create and display HabitProgressGridFrame
                HabitProgressGridFrame grid = new HabitProgressGridFrame();
                grid.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit JVM when this window closes
                grid.setVisible(true);                               // Show JFrame
            });

            // [SWING COMPONENT]: JButton to launch HabitAnalyticsDashboardFrame
            JButton btnAnalytics = new JButton("Launch Visual Analytics & Progress Dashboard");
            btnAnalytics.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnAnalytics.setBackground(HabitProgressGridFrame.SUCCESS);
            btnAnalytics.setForeground(Color.BLACK);
            btnAnalytics.setFocusPainted(false);
            btnAnalytics.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAnalytics.addActionListener(e -> {
                // [JFRAME METHOD]: Dispose launcher window
                frame.dispose();

                // [SWING - JFRAME INSTANTIATION]: Create and display HabitAnalyticsDashboardFrame
                HabitAnalyticsDashboardFrame analytics = new HabitAnalyticsDashboardFrame(null);
                analytics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit JVM when this window closes
                analytics.setVisible(true);                               // Show JFrame
            });

            buttons.add(btnGrid);
            buttons.add(btnAnalytics);
            root.add(buttons, BorderLayout.CENTER);

            // ---------------- Footer ----------------
            // [SWING COMPONENT]: JLabel footer text
            JLabel footer = new JLabel("Built with Core Java Swing & Java 2D Graphics", SwingConstants.CENTER);
            footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            footer.setForeground(HabitProgressGridFrame.TEXT_MUTED);
            root.add(footer, BorderLayout.SOUTH);

            // [JFRAME METHOD]: Attach assembled JPanel as the content pane of this JFrame
            frame.setContentPane(root);

            // [JFRAME METHOD]: Make the launcher window visible on screen
            frame.setVisible(true);
        });
    }
}
