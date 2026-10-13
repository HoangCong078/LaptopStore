
package com.laptopstore.view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class LaptopPanelDemo {

    public static void main(String[] args) {

        // Cai giao dien FlatLaf
        FlatLightLaf.setup();

        // Khoi tao cua so Swing
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                    "LaptopStore - Quan ly Laptop"
            );

            // Dong cua so khi nhan X
            frame.setDefaultCloseOperation(
                    JFrame.DISPOSE_ON_CLOSE
            );

            // Dua LaptopPanel vao cua so
            frame.setContentPane(new LaptopPanel());

            // Kich thuoc cua so
            frame.setSize(1100, 600);

            // Hien thi cua so o giua man hinh
            frame.setLocationRelativeTo(null);

            // Hien thi giao dien
            frame.setVisible(true);
        });
    }
}
