package com.laptopstore.view;

import com.laptopstore.service.AIService;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Giao diện Tư vấn AI (Chatbot AI).
 * Dùng SwingWorker để không bị đơ UI khi gọi API.
 */
public class AIPanel extends JPanel {

    private final AIService aiService;

    private JEditorPane chatArea;
    private JScrollPane scrollPane;
    private JTextField txtInput;
    private JButton btnSend;
    private JLabel lblStatus;

    private final StringBuilder chatHistoryHtml = new StringBuilder();

    /** Dùng khi muốn tự truyền hàm lấy dữ liệu kho. */
    public AIPanel(Supplier<String> inventoryProvider) {
        this.aiService = new AIService(inventoryProvider);
        initComponents();
        appendMessage("AI", "Chào bạn! Tôi là trợ lý ảo của LaptopStore. "
                + "Tôi có thể tư vấn các mẫu laptop hiện có trong kho nhé!");
    }

    /** Constructor mặc định (giữ nguyên cách gọi cũ: new AIPanel()). */
    public AIPanel() {
        this(AIPanel::buildInventoryText);
    }

    private static String buildInventoryText() {
        return "";
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Khung hiển thị chat (HTML)
        chatArea = new JEditorPane();
        chatArea.setEditable(false);

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Arial, sans-serif; font-size: 14px; margin: 5px; }");
        styleSheet.addRule(".user-msg { color: #0056b3; margin-top: 10px; }");
        styleSheet.addRule(".ai-msg { color: #1e7e34; margin-top: 5px; margin-bottom: 15px; }");
        styleSheet.addRule(".sys-msg { color: #c82333; margin-top: 5px; margin-bottom: 15px; }");
        chatArea.setEditorKit(kit);
        chatArea.setContentType("text/html");

        scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Trợ lý AI Consultant"));
        add(scrollPane, BorderLayout.CENTER);

        // Nút gợi ý nhanh
        JPanel pnlSuggestions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        String[] suggestions = {
                "Tư vấn laptop văn phòng",
                "Laptop Gaming giá tốt",
                "So sánh 3 mẫu laptop bán chạy trong kho"
        };
        for (String text : suggestions) {
            JButton btnSuggest = new JButton(text);
            btnSuggest.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSuggest.addActionListener(e -> {
                txtInput.setText(text);
                sendMessage();
            });
            pnlSuggestions.add(btnSuggest);
        }

        // Khung nhập liệu
        JPanel pnlInputContainer = new JPanel(new BorderLayout(5, 5));
        JPanel pnlInput = new JPanel(new BorderLayout(5, 0));

        txtInput = new JTextField();
        txtInput.setFont(new Font("Arial", Font.PLAIN, 14));
        txtInput.addActionListener(e -> sendMessage());

        btnSend = new JButton("Gửi");
        btnSend.setBackground(new Color(0, 123, 255));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.addActionListener(e -> sendMessage());

        pnlInput.add(txtInput, BorderLayout.CENTER);
        pnlInput.add(btnSend, BorderLayout.EAST);

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));

        pnlInputContainer.add(pnlSuggestions, BorderLayout.NORTH);
        pnlInputContainer.add(pnlInput, BorderLayout.CENTER);
        pnlInputContainer.add(lblStatus, BorderLayout.SOUTH);

        add(pnlInputContainer, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        final String userText = txtInput.getText().trim();
        if (userText.isEmpty()) return;

        appendMessage("Bạn", escapeHtml(userText));
        txtInput.setText("");
        setLoadingState(true);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return aiService.askAI(userText);
            }

            @Override
            protected void done() {
                try {
                    appendMessage("AI", markdownToHtml(get()));
                } catch (Exception e) {
                    appendMessage("Hệ thống", escapeHtml("Lỗi: " + e.getMessage()));
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    private void appendMessage(String sender, String htmlMessage) {
        String cssClass;
        String prefix;
        switch (sender) {
            case "Bạn" -> {
                cssClass = "user-msg";
                prefix = "👤 <b>Bạn:</b><br>";
            }
            case "AI" -> {
                cssClass = "ai-msg";
                prefix = "🤖 <b>AI:</b><br>";
            }
            default -> {
                cssClass = "sys-msg";
                prefix = "⚠️ <b>Hệ thống:</b><br>";
            }
        }

        chatHistoryHtml.append("<div class='").append(cssClass).append("'>")
                .append(prefix).append(htmlMessage)
                .append("</div>");

        chatArea.setText("<html><body>" + chatHistoryHtml + "</body></html>");

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\r", "")
                .replace("\n", "<br>");
    }

    private static String markdownToHtml(String md) {
        String s = escapeHtml(md);
        s = s.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        s = s.replaceAll("(?m)(^|<br>)\\s*[*\\-] ", "$1&nbsp;&nbsp;• ");
        return s;
    }

    private void setLoadingState(boolean isLoading) {
        txtInput.setEnabled(!isLoading);
        btnSend.setEnabled(!isLoading);
        if (isLoading) {
            lblStatus.setText("AI đang phân tích kho hàng, vui lòng chờ...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            lblStatus.setText(" ");
            setCursor(Cursor.getDefaultCursor());
            txtInput.requestFocusInWindow();
        }
    }
}