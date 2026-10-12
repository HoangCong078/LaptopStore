package com.laptopstore.view;

import com.laptopstore.model.Warranty;
import com.laptopstore.model.WarrantyStatus;
import com.laptopstore.service.WarrantyService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Quản lý Bảo hành & Hậu mãi (dành cho Thành viên 5 nhúng vào MainFrame).
 * - Tra cứu theo SĐT, Mã hóa đơn hoặc Số serial.
 * - Hiển thị danh sách bảo hành trên bảng với màu trạng thái trực quan.
 * - Cập nhật trạng thái bảo hành tuân theo quy tắc nghiệp vụ.
 * - Sử dụng SwingWorker để tránh giật lag, treo giao diện (EDT).
 */
public class WarrantyPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final WarrantyService warrantyService;
    private final List<Warranty> currentList = new ArrayList<>();

    // Các thành phần giao diện
    private JComboBox<String> cbSearchType;
    private JTextField txtKeyword;
    private JButton btnSearch;

    private JTable table;
    private DefaultTableModel tableModel;

    private JLabel lblResultCount;
    private JComboBox<WarrantyStatus> cbNewStatus;
    private JButton btnUpdateStatus;

    /**
     * Constructor mặc định.
     */
    public WarrantyPanel() {
        this(new WarrantyService());
    }

    /**
     * Constructor nhận Service (hỗ trợ kiểm thử hoặc tùy biến).
     *
     * @param warrantyService tầng service nghiệp vụ bảo hành
     */
    public WarrantyPanel(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Panel phía trên: Thanh tìm kiếm
        add(buildSearchPanel(), BorderLayout.NORTH);

        // 2. Panel ở giữa: Bảng kết quả
        add(buildTablePanel(), BorderLayout.CENTER);

        // 3. Panel phía dưới: Thao tác cập nhật trạng thái & thống kê kết quả
        add(buildActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Tra cứu thông tin bảo hành"));

        JLabel lblType = new JLabel("Hình thức tra cứu:");
        cbSearchType = new JComboBox<>(new String[]{"Số điện thoại", "Mã hóa đơn", "Số serial"});

        JLabel lblKeyword = new JLabel("Từ khóa:");
        txtKeyword = new JTextField(20);
        btnSearch = new JButton("Tra cứu");

        // Nhấn phím Enter trong ô nhập cũng kích hoạt tra cứu
        txtKeyword.addActionListener(e -> performSearch());
        btnSearch.addActionListener(e -> performSearch());

        panel.add(lblType);
        panel.add(cbSearchType);
        panel.add(lblKeyword);
        panel.add(txtKeyword);
        panel.add(btnSearch);

        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                "Mã BH", "Mã HĐ", "Khách hàng", "SĐT",
                "Sản phẩm", "Số serial", "Ngày bắt đầu", "Ngày hết hạn", "Trạng thái"
        };

        // Bảng chỉ đọc dữ liệu
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        // Canh giữa cho các cột ngắn
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Mã BH
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Mã HĐ
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // SĐT
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Ngày BĐ
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Ngày HH

        // Renderer màu cho cột Trạng thái (đảm bảo hiển thị tốt trên cả FlatLaf Light và Dark)
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());

        // Lắng nghe sự kiện chọn dòng trên bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = table.getSelectedRow() >= 0;
                btnUpdateStatus.setEnabled(hasSelection);
                if (hasSelection) {
                    Warranty selected = currentList.get(table.getSelectedRow());
                    cbNewStatus.setSelectedItem(selected.getStatus());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Phía trái: Nhãn đếm số lượng
        lblResultCount = new JLabel("Tìm thấy: 0 kết quả");
        lblResultCount.setFont(lblResultCount.getFont().deriveFont(Font.BOLD));

        // Phía phải: Cập nhật trạng thái
        JPanel actionRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel lblStatus = new JLabel("Trạng thái mới:");
        cbNewStatus = new JComboBox<>(WarrantyStatus.values());
        btnUpdateStatus = new JButton("Cập nhật trạng thái");
        btnUpdateStatus.setEnabled(false); // Chỉ bật khi đã chọn 1 dòng

        btnUpdateStatus.addActionListener(e -> performUpdateStatus());

        actionRight.add(lblStatus);
        actionRight.add(cbNewStatus);
        actionRight.add(btnUpdateStatus);

        panel.add(lblResultCount, BorderLayout.WEST);
        panel.add(actionRight, BorderLayout.EAST);
        return panel;
    }

    /**
     * Thực hiện tìm kiếm bất đồng bộ thông qua SwingWorker.
     */
    private void performSearch() {
        String keyword = txtKeyword.getText().trim();
        int typeIndex = cbSearchType.getSelectedIndex();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập từ khóa tra cứu!",
                    "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            txtKeyword.requestFocus();
            return;
        }

        long startTime = System.currentTimeMillis();
        setLoading(true);

        SwingWorker<List<Warranty>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Warranty> doInBackground() {
                return switch (typeIndex) {
                    case 0 -> warrantyService.searchByPhone(keyword);
                    case 1 -> {
                        int invId;
                        try {
                            invId = Integer.parseInt(keyword);
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("Mã hóa đơn phải là số nguyên!");
                        }
                        yield warrantyService.searchByInvoiceId(invId);
                    }
                    case 2 -> warrantyService.searchBySerialNumber(keyword);
                    default -> new ArrayList<>();
                };
            }

            @Override
            protected void done() {
                setLoading(false);
                long duration = System.currentTimeMillis() - startTime;
                try {
                    List<Warranty> result = get();
                    displayResults(result, duration);
                    if (result.isEmpty()) {
                        JOptionPane.showMessageDialog(WarrantyPanel.this,
                                "Không tìm thấy dữ liệu bảo hành phù hợp với từ khóa: " + keyword,
                                "Kết quả tra cứu", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(WarrantyPanel.this,
                            message,
                            "Lỗi tra cứu", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Đổ dữ liệu tìm kiếm được lên JTable kèm thời gian xử lý.
     */
    private void displayResults(List<Warranty> list, long durationMs) {
        currentList.clear();
        currentList.addAll(list);
        tableModel.setRowCount(0);

        for (Warranty w : list) {
            String startStr = w.getStartDate() != null ? w.getStartDate().format(DATE_FORMATTER) : "";
            String endStr = w.getEndDate() != null ? w.getEndDate().format(DATE_FORMATTER) : "";
            String serialStr = (w.getSerialNumber() != null && !w.getSerialNumber().trim().isEmpty())
                    ? w.getSerialNumber() : "(Chưa có)";
            String customerNameStr = (w.getCustomerName() != null && !w.getCustomerName().trim().isEmpty())
                    ? w.getCustomerName() : "(Chưa có)";
            String customerPhoneStr = (w.getCustomerPhone() != null && !w.getCustomerPhone().trim().isEmpty())
                    ? w.getCustomerPhone() : "(Chưa có)";
            String productStr = (w.getProductName() != null && !w.getProductName().trim().isEmpty())
                    ? w.getProductName() : "(Không xác định)";

            tableModel.addRow(new Object[]{
                    w.getId(),
                    w.getInvoiceId(),
                    customerNameStr,
                    customerPhoneStr,
                    productStr,
                    serialStr,
                    startStr,
                    endStr,
                    w.getStatus()
            });
        }

        lblResultCount.setText("Tìm thấy: " + list.size() + " kết quả (" + durationMs + " ms)");
        btnUpdateStatus.setEnabled(false);
    }

    /**
     * Thực hiện cập nhật trạng thái thẻ bảo hành.
     */
    private void performUpdateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một thẻ bảo hành trên bảng để cập nhật!",
                    "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= currentList.size()) {
            return;
        }

        Warranty selectedWarranty = currentList.get(modelRow);
        WarrantyStatus newStatus = (WarrantyStatus) cbNewStatus.getSelectedItem();

        if (newStatus == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn chuyển trạng thái thẻ #" + selectedWarranty.getId()
                        + " từ [" + selectedWarranty.getStatus().getLabel() + "] sang [" + newStatus.getLabel() + "]?",
                "Xác nhận thay đổi", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setLoading(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return warrantyService.updateStatus(selectedWarranty, newStatus);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    boolean success = get();
                    if (success) {
                        tableModel.setValueAt(newStatus, modelRow, 8);
                        JOptionPane.showMessageDialog(WarrantyPanel.this,
                                "Cập nhật trạng thái thành công!",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(WarrantyPanel.this,
                                "Không tìm thấy thẻ bảo hành cần cập nhật!",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(WarrantyPanel.this,
                            message,
                            "Lỗi cập nhật trạng thái", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void setLoading(boolean loading) {
        btnSearch.setEnabled(!loading);
        txtKeyword.setEnabled(!loading);
        cbSearchType.setEnabled(!loading);
        btnUpdateStatus.setEnabled(!loading && table.getSelectedRow() >= 0);
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    /**
     * CellRenderer hiển thị trạng thái với màu sắc trực quan,
     * tương thích với cả giao diện Sáng và Tối (FlatLaf Light/Dark).
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        private static final Color COLOR_ACTIVE = new Color(40, 167, 69);      // Xanh lá
        private static final Color COLOR_PROCESSING = new Color(230, 126, 34);  // Cam
        private static final Color COLOR_COMPLETED = new Color(0, 123, 255);    // Xanh dương
        private static final Color COLOR_EXPIRED = new Color(220, 53, 69);      // Đỏ
        private Font cachedBoldFont = null;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected) {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            if (value instanceof WarrantyStatus status) {
                setText(status.getLabel());
                if (!isSelected) {
                    if (cachedBoldFont == null || cachedBoldFont.getSize() != getFont().getSize()) {
                        cachedBoldFont = getFont().deriveFont(Font.BOLD);
                    }
                    setFont(cachedBoldFont);
                    switch (status) {
                        case ACTIVE -> setForeground(COLOR_ACTIVE);
                        case PROCESSING -> setForeground(COLOR_PROCESSING);
                        case COMPLETED -> setForeground(COLOR_COMPLETED);
                        case EXPIRED -> setForeground(COLOR_EXPIRED);
                    }
                }
            }
            return this;
        }
    }
}
