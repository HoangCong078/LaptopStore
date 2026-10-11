package com.laptopstore.view;

import com.laptopstore.dao.StatisticsDAO;
import com.laptopstore.model.Account;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/** Trang chủ: lời chào và 2 thống kê cơ bản (tổng máy trong kho, doanh thu trong ngày). */
public class HomePanel extends JPanel {
    private final StatisticsDAO statisticsDAO = new StatisticsDAO();
    private final JLabel lblTotalStock = new JLabel("...");
    private final JLabel lblRevenue = new JLabel("...");
    private final JLabel lblError = new JLabel(" ");

    public HomePanel(Account account) {
        super(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel welcome = new JLabel("Xin chào, " + account.getFullName() + "!");
        welcome.setFont(welcome.getFont().deriveFont(22f));
        add(welcome, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.add(createCard("Tổng số máy trong kho", lblTotalStock));
        cards.add(createCard("Doanh thu hôm nay", lblRevenue));
        add(cards, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refresh());
        lblError.setForeground(java.awt.Color.RED.darker());
        bottom.add(lblError, BorderLayout.CENTER);
        bottom.add(btnRefresh, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(java.awt.Color.GRAY),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(lblTitle.getFont().deriveFont(14f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(java.awt.Font.BOLD, 30f));
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Tải số liệu ở luồng nền để không làm đơ giao diện. Gọi lại mỗi khi mở trang chủ. */
    public void refresh() {
        lblError.setText(" ");
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{statisticsDAO.getTotalStock(), statisticsDAO.getTodayRevenue()};
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    lblTotalStock.setText(result[0] + " máy");
                    NumberFormat vnd = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
                    lblRevenue.setText(vnd.format((BigDecimal) result[1]) + " đ");
                } catch (Exception e) {
                    lblTotalStock.setText("--");
                    lblRevenue.setText("--");
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblError.setText("Không tải được thống kê: " + cause.getMessage());
                }
            }
        }.execute();
    }
}
