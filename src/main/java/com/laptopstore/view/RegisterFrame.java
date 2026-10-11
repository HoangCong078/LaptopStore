package com.laptopstore.view;

import com.laptopstore.service.AuthException;
import com.laptopstore.service.AuthService;
import com.laptopstore.service.AuthServiceImpl;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Form đăng ký tài khoản STAFF (dialog đi kèm LoginFrame). */
public class RegisterFrame extends JDialog {
    private final AuthService authService = new AuthServiceImpl();
    private final LoginFrame owner;
    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JPasswordField txtConfirm = new JPasswordField(20);
    private final JButton btnRegister = new JButton("Đăng ký");
    private final JButton btnCancel = new JButton("Hủy");

    public RegisterFrame(LoginFrame owner) {
        super(owner, "Đăng ký tài khoản", true);
        this.owner = owner;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Họ và tên:", txtFullName);
        addRow(form, c, 1, "Tên đăng nhập:", txtUsername);
        addRow(form, c, 2, "Mật khẩu:", txtPassword);
        addRow(form, c, 3, "Nhập lại mật khẩu:", txtConfirm);

        JPanel buttons = new JPanel();
        buttons.add(btnRegister);
        buttons.add(btnCancel);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        form.add(buttons, c);

        setContentPane(form);
        getRootPane().setDefaultButton(btnRegister);
        btnRegister.addActionListener(e -> doRegister());
        btnCancel.addActionListener(e -> dispose());

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridwidth = 1; c.gridy = row; c.gridx = 0; c.weightx = 0;
        form.add(new javax.swing.JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        form.add(field, c);
    }

    private void doRegister() {
        char[] password = txtPassword.getPassword();
        char[] confirm = txtConfirm.getPassword();
        if (!Arrays.equals(password, confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp", "Lỗi", JOptionPane.ERROR_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }
        Arrays.fill(confirm, '\0');
        String username = txtUsername.getText();
        String fullName = txtFullName.getText();
        btnRegister.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.register(username, password, fullName);
                return null;
            }

            @Override
            protected void done() {
                btnRegister.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(RegisterFrame.this, "Đăng ký thành công! Hãy đăng nhập.");
                    owner.prefillUsername(username.trim());
                    dispose();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    boolean userError = cause instanceof AuthException;
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            userError ? cause.getMessage() : "Lỗi hệ thống: " + cause.getMessage(),
                            "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
