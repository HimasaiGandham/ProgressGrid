package com.progressgrid.swing;

import com.progressgrid.dao.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * SwingLoginDemo - Connected Login & Registration window for the ProgressGrid Swing Suite.
 * Directly integrates with MySQL (progressgrid_db) and Spring Boot REST API.
 */
public class SwingLoginDemo {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_SIGNUP = "SIGNUP";

    // Modern color palette
    private static final Color BG_WINDOW = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_TITLE = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    private static final Color SUCCESS_GREEN = new Color(16, 185, 129);
    private static final Color ERROR_RED = new Color(239, 68, 68);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    public static void main(String[] args) {
        showLogin();
    }

    public static void showLogin() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ProgressGrid — Secure Authentication");
            frame.setSize(460, 620);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.getContentPane().setBackground(BG_WINDOW);

            CardLayout cardLayout = new CardLayout();
            JPanel container = new JPanel(cardLayout);
            container.setOpaque(false);

            // Create both views
            JPanel loginCard = createLoginPanel(frame, cardLayout, container);
            JPanel signupCard = createSignupPanel(frame, cardLayout, container);

            container.add(loginCard, CARD_LOGIN);
            container.add(signupCard, CARD_SIGNUP);

            frame.add(container);
            cardLayout.show(container, CARD_LOGIN);
            frame.setVisible(true);
        });
    }

    /**
     * View 1: Login Page (Shown First)
     */
    private static JPanel createLoginPanel(JFrame frame, CardLayout cardLayout, JPanel container) {
        JPanel card = createCardWrapper();

        // 1. Title & Header
        JLabel pageTitle = new JLabel("Login Page", SwingConstants.CENTER);
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(TEXT_TITLE);
        pageTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Sign in to your ProgressGrid account", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_MUTED);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Database status indicator
        JPanel dbBadge = createDatabaseBadge();
        dbBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Form Fields
        JLabel userLabel = createFieldLabel("Username or Email:");
        JTextField userField = createTextField();

        JLabel passLabel = createFieldLabel("Password:");
        JPasswordField passField = createPasswordField();

        // Status Feedback Label
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login Button
        JButton loginBtn = createPrimaryButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bottom Switch to Signup Option
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noAccountLabel.setForeground(TEXT_MUTED);

        JLabel createAccountLink = new JLabel("<html><u>Create an Account</u></html>");
        createAccountLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createAccountLink.setForeground(PRIMARY_BLUE);
        createAccountLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createAccountLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                statusLabel.setText(" ");
                cardLayout.show(container, CARD_SIGNUP);
            }
        });

        bottomPanel.add(noAccountLabel);
        bottomPanel.add(createAccountLink);

        // "View Registered Users" helper button
        JButton viewUsersBtn = new JButton("View Users in Database");
        viewUsersBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        viewUsersBtn.setForeground(TEXT_MUTED);
        viewUsersBtn.setFocusPainted(false);
        viewUsersBtn.setContentAreaFilled(false);
        viewUsersBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        viewUsersBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewUsersBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewUsersBtn.addActionListener(e -> showUsersDialog(frame));

        // Action logic
        Runnable doLogin = () -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setForeground(ERROR_RED);
                statusLabel.setText("Please enter both username and password.");
                return;
            }

            statusLabel.setForeground(PRIMARY_BLUE);
            statusLabel.setText("Authenticating with database...");
            loginBtn.setEnabled(false);

            SwingWorker<AuthClient.AuthResult, Void> worker = new SwingWorker<>() {
                @Override
                protected AuthClient.AuthResult doInBackground() {
                    return AuthClient.login(username, password);
                }

                @Override
                protected void done() {
                    loginBtn.setEnabled(true);
                    try {
                        AuthClient.AuthResult result = get();
                        if (result.isSuccess()) {
                            statusLabel.setForeground(SUCCESS_GREEN);
                            statusLabel.setText("✓ " + result.getMessage());

                            // Short delay to show success before launching main app
                            Timer timer = new Timer(600, evt -> {
                                frame.dispose();
                                ProgressGridSwingLauncher.showLauncher();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            statusLabel.setForeground(ERROR_RED);
                            statusLabel.setText("✗ " + result.getMessage());
                        }
                    } catch (Exception ex) {
                        statusLabel.setForeground(ERROR_RED);
                        statusLabel.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        };

        loginBtn.addActionListener(e -> doLogin.run());
        passField.addActionListener(e -> doLogin.run());
        userField.addActionListener(e -> passField.requestFocusInWindow());

        // Assembly
        card.add(Box.createVerticalStrut(10));
        card.add(pageTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(subTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(dbBadge);
        card.add(Box.createVerticalStrut(20));

        card.add(userLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(userField);
        card.add(Box.createVerticalStrut(14));

        card.add(passLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passField);
        card.add(Box.createVerticalStrut(16));

        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(14));

        card.add(bottomPanel);
        card.add(Box.createVerticalStrut(12));
        card.add(viewUsersBtn);

        return wrapInCenterPanel(card);
    }

    /**
     * View 2: Create Account Page (Reached by clicking "Create an Account")
     */
    private static JPanel createSignupPanel(JFrame frame, CardLayout cardLayout, JPanel container) {
        JPanel card = createCardWrapper();

        // 1. Title & Header
        JLabel pageTitle = new JLabel("Create Account", SwingConstants.CENTER);
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(TEXT_TITLE);
        pageTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Register new account in MySQL database", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_MUTED);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Form Fields
        JLabel nameLabel = createFieldLabel("Full Name (Optional):");
        JTextField nameField = createTextField();

        JLabel userLabel = createFieldLabel("Username (Required):");
        JTextField userField = createTextField();

        JLabel emailLabel = createFieldLabel("Email Address (Required):");
        JTextField emailField = createTextField();

        JLabel passLabel = createFieldLabel("Password (Min 6 characters):");
        JPasswordField passField = createPasswordField();

        JLabel confirmLabel = createFieldLabel("Confirm Password:");
        JPasswordField confirmField = createPasswordField();

        // Status Feedback Label
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register Button
        JButton registerBtn = createPrimaryButton("Create Account & Store in DB");
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bottom Switch back to Login
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel haveAccountLabel = new JLabel("Already have an account?");
        haveAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        haveAccountLabel.setForeground(TEXT_MUTED);

        JLabel backToLoginLink = new JLabel("<html><u>Back to Login</u></html>");
        backToLoginLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backToLoginLink.setForeground(PRIMARY_BLUE);
        backToLoginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLoginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                statusLabel.setText(" ");
                cardLayout.show(container, CARD_LOGIN);
            }
        });

        bottomPanel.add(haveAccountLabel);
        bottomPanel.add(backToLoginLink);

        // Registration logic
        Runnable doRegister = () -> {
            String name = nameField.getText().trim();
            String username = userField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword());
            String confirmPass = new String(confirmField.getPassword());

            if (username.isEmpty()) {
                statusLabel.setText("Please enter a username.");
                return;
            }
            if (email.isEmpty() || !email.contains("@")) {
                statusLabel.setText("Please enter a valid email address.");
                return;
            }
            if (password.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters.");
                return;
            }
            if (!password.equals(confirmPass)) {
                statusLabel.setText("Passwords do not match.");
                return;
            }

            statusLabel.setForeground(PRIMARY_BLUE);
            statusLabel.setText("Saving user to MySQL database...");
            registerBtn.setEnabled(false);

            SwingWorker<AuthClient.AuthResult, Void> worker = new SwingWorker<>() {
                @Override
                protected AuthClient.AuthResult doInBackground() {
                    return AuthClient.register(username, email, password, name);
                }

                @Override
                protected void done() {
                    registerBtn.setEnabled(true);
                    try {
                        AuthClient.AuthResult result = get();
                        if (result.isSuccess()) {
                            statusLabel.setForeground(SUCCESS_GREEN);
                            statusLabel.setText("✓ " + result.getMessage());

                            // Pop up confirmation dialog so user clearly sees it
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Account created successfully!\n\n"
                                            + "• Username: " + username + "\n"
                                            + "• Email: " + email + "\n"
                                            + "• User ID in DB: #" + result.getUser().getId() + "\n"
                                            + "• Stored in: MySQL database (progressgrid_db.users)\n\n"
                                            + "You can now log in with your credentials.",
                                    "Registration Successful",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            // Switch to login card
                            cardLayout.show(container, CARD_LOGIN);
                        } else {
                            statusLabel.setForeground(ERROR_RED);
                            statusLabel.setText("✗ " + result.getMessage());
                        }
                    } catch (Exception ex) {
                        statusLabel.setForeground(ERROR_RED);
                        statusLabel.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        };

        registerBtn.addActionListener(e -> doRegister.run());

        // Assembly
        card.add(Box.createVerticalStrut(6));
        card.add(pageTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(subTitle);
        card.add(Box.createVerticalStrut(14));

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(nameField);
        card.add(Box.createVerticalStrut(8));

        card.add(userLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(userField);
        card.add(Box.createVerticalStrut(8));

        card.add(emailLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(emailField);
        card.add(Box.createVerticalStrut(8));

        card.add(passLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));

        card.add(confirmLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(14));

        card.add(registerBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(10));

        card.add(bottomPanel);

        return wrapInCenterPanel(card);
    }

    /**
     * Dialog to display all rows currently stored in MySQL users table.
     */
    private static void showUsersDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "MySQL Database — 'users' Table Contents", true);
        dialog.setSize(620, 380);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Users currently stored in progressgrid_db.users:");
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(TEXT_TITLE);
        panel.add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Username", "Email", "Name", "Created At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        try {
            List<DatabaseConnection.UserRecord> list = DatabaseConnection.getAllUsers();
            for (DatabaseConnection.UserRecord u : list) {
                model.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getName(),
                        u.getCreatedAt() != null ? u.getCreatedAt().toString() : "—"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Could not read users from database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Database status badge showing connection state.
     */
    private static JPanel createDatabaseBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        badge.setBackground(new Color(241, 245, 249));
        badge.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        badge.setMaximumSize(new Dimension(320, 30));

        boolean dbOk = DatabaseConnection.isConnected();

        JLabel dot = new JLabel(dbOk ? "●" : "○");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(dbOk ? SUCCESS_GREEN : ERROR_RED);

        JLabel text = new JLabel(dbOk ? "MySQL: progressgrid_db (Connected)" : "MySQL: Offline (Check port 3306)");
        text.setFont(new Font("Segoe UI", Font.BOLD, 11));
        text.setForeground(dbOk ? new Color(22, 101, 52) : ERROR_RED);

        badge.add(dot);
        badge.add(text);
        return badge;
    }

    // --- UI Component Factory Helpers ---

    private static JPanel createCardWrapper() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));
        card.setPreferredSize(new Dimension(390, 560));
        card.setMaximumSize(new Dimension(390, 560));
        return card;
    }

    private static JPanel wrapInCenterPanel(JPanel card) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        return wrapper;
    }

    private static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_TITLE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(330, 20));
        return label;
    }

    private static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(330, 34));
        field.setMaximumSize(new Dimension(330, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(330, 34));
        field.setMaximumSize(new Dimension(330, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_BLUE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(330, 38));
        btn.setMaximumSize(new Dimension(330, 38));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_BLUE);
            }
        });
        return btn;
    }
}
