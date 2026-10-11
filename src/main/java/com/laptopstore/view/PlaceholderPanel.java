package com.laptopstore.view;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Panel tạm cho module chưa hoàn thành. Thay bằng panel thật khi module của bạn xong. */
public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(String moduleName) {
        super(new BorderLayout());
        JLabel label = new JLabel("Module \"" + moduleName + "\" đang được phát triển", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(16f));
        add(label, BorderLayout.CENTER);
    }
}
