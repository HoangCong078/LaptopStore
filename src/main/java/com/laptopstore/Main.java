package com.laptopstore;

import com.formdev.flatlaf.FlatLightLaf;
import com.laptopstore.view.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup(); // giao diện phẳng dùng chung cho cả ứng dụng
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
