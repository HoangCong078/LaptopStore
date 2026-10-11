package com.laptopstore.view;

import com.laptopstore.model.Account;
import com.laptopstore.service.AuthException;
import com.laptopstore.service.AuthService;
import com.laptopstore.service.AuthServiceImpl;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Form đăng nhập. Đăng nhập thành công -> mở MainFrame. */
public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthServiceImpl();
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnRegister = new JButton("Đăng ký tài khoản mới");

    public LoginFrame() {
        super("Cửa hàng Laptop - Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("QUẢN LÝ CỬA HÀNG LAPTOP", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(title, c);

        c.gridwidth = 1; c.gridy = 1; c.gridx = 0; c.weightx = 0;
        form.add(new JLabel("Tên đăng nhập:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(txtUsername, c);

        c.gridy = 2; c.gridx = 0; c.weightx = 0;
        form.add(new JLabel("Mật khẩu:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(txtPassword, c);

        c.gridy = 3; c.gridx = 0; c.gridwidth = 2;
        form.add(btnLogin, c);
        c.gridy = 4;
        form.add(btnRegister, c);

        setContentPane(form);
        getRootPane().setDefaultButton(btnLogin); // Enter = đăng nhập

        btnLogin.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> new RegisterFrame(this).setVisible(true));

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String username = txtUsername.getText();
        char[] password = txtPassword.getPassword();
        btnLogin.setEnabled(false);

        new SwingWorker<Account, Void>() {
            @Override
            protected Account doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    Account account = get();
                    txtPassword.setText("");
                    new MainFrame(account).setVisible(true);
                    dispose();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    boolean userError = cause instanceof AuthException;
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            userError ? cause.getMessage() : "Lỗi hệ thống: " + cause.getMessage(),
                            userError ? "Đăng nhập thất bại" : "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                }
            }
        }.execute();
    }

    /** Dùng sau khi đăng ký thành công để điền sẵn tên đăng nhập. */
    public void prefillUsername(String username) {
        txtUsername.setText(username);
        txtPassword.requestFocus();
    }
}
