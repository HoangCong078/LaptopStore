package com.laptopstore.view;

import com.laptopstore.dao.JdbcLaptopDAO;
import com.laptopstore.dao.LaptopDAO;
import com.laptopstore.model.Laptop;
import com.laptopstore.service.StockService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.laptopstore.dao.StockMovementDAO;
import com.laptopstore.dao.JdbcStockMovementDAO;
import com.laptopstore.model.StockMovement;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

public class LaptopPanel extends JPanel {

    private final LaptopDAO laptopDAO;
    private final JTable laptopTable;
    private final DefaultTableModel tableModel;

    private final JTextField searchField;
    private final JButton searchButton;
    private final JButton refreshButton;
    private final JButton addButton;
    private final JButton editButton;
    private final StockService stockService;
    private final JButton importButton;
    private final JButton exportButton;
    private final StockMovementDAO movementDAO;
    private final JButton historyButton;
    private final JButton deleteButton;


    public LaptopPanel() {

        laptopDAO = new JdbcLaptopDAO();
        stockService = new StockService();
        movementDAO = new JdbcStockMovementDAO();


        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15
        ));

        // ===== 1. TIÊU ĐỀ =====

        JLabel titleLabel =
                new JLabel("QUẢN LÝ LAPTOP");

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        // ===== 2. Ô TÌM KIẾM =====

        searchField = new JTextField(22);

        searchField.setToolTipText(
                "Nhập tên hoặc hãng laptop"
        );

        searchButton = new JButton("Tìm kiếm");
        refreshButton = new JButton("Làm mới");
        addButton = new JButton("Thêm Laptop");
        editButton = new JButton("Sửa Laptop");
        importButton = new JButton("Nhập kho");
        exportButton = new JButton("Xuất kho");
        historyButton = new JButton("Lịch sử kho");
        deleteButton = new JButton("Xóa Laptop");

        // ===== THANH CONG CU =====

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(addButton);
        searchPanel.add(editButton);
        searchPanel.add(importButton);
        searchPanel.add(exportButton);
        searchPanel.add(historyButton);
        searchPanel.add(deleteButton);

        // ===== KHU VUC TIEU DE =====

        JPanel headerPanel = new JPanel(
                new BorderLayout(0, 12)
        );

        // Tieu de nam o hang tren
        headerPanel.add(
                titleLabel,
                BorderLayout.NORTH
        );

        // Cac nut va o tim kiem nam o hang duoi
        headerPanel.add(
                searchPanel,
                BorderLayout.CENTER
        );

        add(headerPanel, BorderLayout.NORTH);


        // ===== 3. BẢNG LAPTOP =====

        String[] columns = {
                "ID",
                "Tên Laptop",
                "Hãng",
                "Giá",
                "Số lượng",
                "CPU",
                "RAM",
                "Trạng thái"
        };

        tableModel = new DefaultTableModel(
                columns, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column
            ) {
                return false;
            }
        };

        laptopTable = new JTable(tableModel);

        laptopTable.setRowHeight(30);

        laptopTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        laptopTable.setAutoCreateRowSorter(true);

        // Định dạng giá tiền Việt Nam
        NumberFormat priceFormat = NumberFormat.getNumberInstance(
                Locale.forLanguageTag("vi-VN")
        );

        priceFormat.setMaximumFractionDigits(0);

        // Tăng độ rộng cột Giá
        laptopTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(170);

        laptopTable.getColumnModel()
                .getColumn(3)
                .setMinWidth(150);

        // Căn trái và tạo khoảng cách
        laptopTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(new DefaultTableCellRenderer() {

                    @Override
                    protected void setValue(Object value) {

                        setHorizontalAlignment(SwingConstants.LEFT);

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        0, 10, 0, 10
                                )
                        );

                        if (value instanceof Number) {
                            setText(priceFormat.format(value) + " ₫");
                        } else {
                            setText("");
                        }
                    }
                });

        add(
                new JScrollPane(laptopTable),
                BorderLayout.CENTER
        );

        // ===== 4. SỰ KIỆN =====

        searchButton.addActionListener(
                e -> loadLaptopData(searchField.getText())
        );
        addButton.addActionListener(
                e -> showAddLaptopDialog()
        );
        editButton.addActionListener(
                e -> showEditLaptopDialog()
        );
        importButton.addActionListener(
                e -> showImportStockDialog()
        );
        exportButton.addActionListener(
                e -> showExportStockDialog()
        );
        historyButton.addActionListener(
                e -> showStockHistoryDialog()
        );
        deleteButton.addActionListener(
                e -> showDeleteLaptopDialog()
        );


        // Nhấn Enter trong ô tìm kiếm
        searchField.addActionListener(
                e -> loadLaptopData(searchField.getText())
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadLaptopData("");
        });

        // ===== 5. TẢI DỮ LIỆU LẦN ĐẦU =====

        loadLaptopData("");
    }

    // Lấy dữ liệu từ MySQL và đưa lên JTable
    private void loadLaptopData(String keyword) {

        searchButton.setEnabled(false);
        refreshButton.setEnabled(false);

        String text = keyword == null
                ? ""
                : keyword.trim();

        SwingWorker<List<Laptop>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Laptop> doInBackground()
                            throws SQLException {

                        if (text.isEmpty()) {
                            return laptopDAO.findAll();
                        }

                        return laptopDAO.search(text);
                    }

                    @Override
                    protected void done() {

                        try {
                            List<Laptop> laptops = get();

                            tableModel.setRowCount(0);

                            for (Laptop laptop : laptops) {

                                Object[] row = {
                                        laptop.getLaptopId(),
                                        laptop.getLaptopName(),
                                        laptop.getBrand(),
                                        laptop.getPrice(),
                                        laptop.getQuantity(),
                                        laptop.getCpu(),
                                        laptop.getRam(),
                                        laptop.getQuantity() > 0
                                                ? "Còn hàng"
                                                : "Hết hàng"
                                };

                                tableModel.addRow(row);
                            }

                        } catch (Exception e) {

                            JOptionPane.showMessageDialog(
                                    LaptopPanel.this,
                                    "Không thể tải laptop:\n"
                                            + e.getMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } finally {
                            searchButton.setEnabled(true);
                            refreshButton.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private void showAddLaptopDialog() {

        // 1. Tạo các ô nhập dữ liệu
        JTextField nameField = new JTextField(20);
        JTextField brandField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField cpuField = new JTextField(20);
        JTextField ramField = new JTextField(20);
        JTextField storageField = new JTextField(20);
        JTextField screenField = new JTextField(20);
        JTextField tagsField = new JTextField(20);

        // 2. Tạo form nhập
        JPanel form = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        form.add(new JLabel("Tên Laptop:"));
        form.add(nameField);

        form.add(new JLabel("Hãng:"));
        form.add(brandField);

        form.add(new JLabel("Giá bán (VND):"));
        form.add(priceField);

        form.add(new JLabel("CPU:"));
        form.add(cpuField);

        form.add(new JLabel("RAM:"));
        form.add(ramField);

        form.add(new JLabel("Ổ cứng:"));
        form.add(storageField);

        form.add(new JLabel("Màn hình:"));
        form.add(screenField);

        form.add(new JLabel("Tags:"));
        form.add(tagsField);

        // 3. Mở hộp thoại
        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Thêm Laptop mới",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // Nhấn Cancel thì không làm gì
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // 4. Đọc và kiểm tra dữ liệu
        final Laptop newLaptop;

        try {
            BigDecimal price = new BigDecimal(
                    priceField.getText().trim()
            );

            newLaptop = new Laptop(
                    nameField.getText(),
                    brandField.getText(),
                    null,                 // Chưa chọn danh mục
                    price,
                    0,                    // Tồn kho ban đầu
                    cpuField.getText(),
                    ramField.getText(),
                    storageField.getText(),
                    screenField.getText(),
                    tagsField.getText()
            );

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Dữ liệu không hợp lệ:\n" + e.getMessage(),
                    "Lỗi nhập liệu",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 5. Lưu vào MySQL ở luồng nền
        addButton.setEnabled(false);

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            @Override
            protected Integer doInBackground()
                    throws SQLException {

                return laptopDAO.insert(newLaptop);
            }

            @Override
            protected void done() {
                try {
                    int newId = get();

                    JOptionPane.showMessageDialog(
                            LaptopPanel.this,
                            "Thêm Laptop thành công!\n"
                                    + "Mã Laptop: " + newId,
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Xóa từ khóa và tải lại bảng
                    searchField.setText("");
                    loadLaptopData("");

                } catch (Exception e) {

                    Throwable cause = e.getCause() != null
                            ? e.getCause()
                            : e;

                    JOptionPane.showMessageDialog(
                            LaptopPanel.this,
                            "Không thể thêm Laptop:\n"
                                    + cause.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    addButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void showEditLaptopDialog() {

        // 1. Kiểm tra người dùng đã chọn laptop chưa
        int selectedRow = laptopTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn Laptop cần sửa!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Chuyển dòng đang hiển thị về dòng trong Model
        // Vì JTable đang bật chức năng sắp xếp
        int modelRow = laptopTable.convertRowIndexToModel(
                selectedRow
        );

        int laptopId = (Integer) tableModel.getValueAt(
                modelRow, 0
        );

        editButton.setEnabled(false);

        // 3. Lấy dữ liệu laptop từ MySQL
        SwingWorker<Laptop, Void> loadWorker =
                new SwingWorker<>() {

                    @Override
                    protected Laptop doInBackground()
                            throws SQLException {

                        return laptopDAO.findById(laptopId);
                    }

                    @Override
                    protected void done() {

                        try {
                            Laptop laptop = get();

                            if (laptop == null) {
                                JOptionPane.showMessageDialog(
                                        LaptopPanel.this,
                                        "Laptop không tồn tại!"
                                );
                                editButton.setEnabled(true);
                                return;
                            }

                            // 4. Tạo form chỉnh sửa
                            JTextField nameField = new JTextField(
                                    laptop.getLaptopName()
                            );

                            JTextField brandField = new JTextField(
                                    laptop.getBrand()
                            );

                            JTextField priceField = new JTextField(
                                    laptop.getPrice().toPlainString()
                            );

                            JTextField cpuField = new JTextField(
                                    laptop.getCpu() == null
                                            ? "" : laptop.getCpu()
                            );

                            JTextField ramField = new JTextField(
                                    laptop.getRam() == null
                                            ? "" : laptop.getRam()
                            );

                            JTextField storageField = new JTextField(
                                    laptop.getStorage() == null
                                            ? "" : laptop.getStorage()
                            );

                            JTextField screenField = new JTextField(
                                    laptop.getScreen() == null
                                            ? "" : laptop.getScreen()
                            );

                            JTextField tagsField = new JTextField(
                                    laptop.getTags() == null
                                            ? "" : laptop.getTags()
                            );

                            JPanel form = new JPanel(
                                    new GridLayout(0, 2, 10, 10)
                            );

                            form.add(new JLabel("Tên Laptop:"));
                            form.add(nameField);

                            form.add(new JLabel("Hãng:"));
                            form.add(brandField);

                            form.add(new JLabel("Giá bán:"));
                            form.add(priceField);

                            form.add(new JLabel("CPU:"));
                            form.add(cpuField);

                            form.add(new JLabel("RAM:"));
                            form.add(ramField);

                            form.add(new JLabel("Ổ cứng:"));
                            form.add(storageField);

                            form.add(new JLabel("Màn hình:"));
                            form.add(screenField);

                            form.add(new JLabel("Tags:"));
                            form.add(tagsField);

                            // 5. Hiển thị hộp thoại
                            int result = JOptionPane.showConfirmDialog(
                                    LaptopPanel.this,
                                    form,
                                    "Sửa Laptop - ID: " + laptopId,
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE
                            );
                            if (result != JOptionPane.OK_OPTION) {
                                editButton.setEnabled(true);
                                return;
                            }

                            // 6. Kiểm tra và cập nhật dữ liệu đối tượng
                            try {
                                BigDecimal newPrice = new BigDecimal(
                                        priceField.getText().trim()
                                );

                                laptop.setLaptopName(nameField.getText());
                                laptop.setBrand(brandField.getText());
                                laptop.setPrice(newPrice);
                                laptop.setCpu(cpuField.getText());
                                laptop.setRam(ramField.getText());
                                laptop.setStorage(storageField.getText());
                                laptop.setScreen(screenField.getText());
                                laptop.setTags(tagsField.getText());

                            } catch (IllegalArgumentException ex) {

                                JOptionPane.showMessageDialog(
                                        LaptopPanel.this,
                                        "Dữ liệu không hợp lệ:\n"
                                                + ex.getMessage(),
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                editButton.setEnabled(true);
                                return;
                            }

                            // 7. Lưu vào MySQL bằng luồng nền
                            SwingWorker<Boolean, Void> updateWorker =
                                    new SwingWorker<>() {

                                        @Override
                                        protected Boolean doInBackground()
                                                throws SQLException {

                                            return laptopDAO.update(laptop);
                                        }

                                        @Override
                                        protected void done() {

                                            try {
                                                boolean updated = get();

                                                if (updated) {
                                                    JOptionPane.showMessageDialog(
                                                            LaptopPanel.this,
                                                            "Cập nhật Laptop thành công!"
                                                    );

                                                    loadLaptopData(
                                                            searchField.getText()
                                                    );

                                                } else {
                                                    JOptionPane.showMessageDialog(
                                                            LaptopPanel.this,
                                                            "Không có Laptop được cập nhật!"
                                                    );
                                                }

                                            } catch (Exception ex) {

                                                JOptionPane.showMessageDialog(
                                                        LaptopPanel.this,
                                                        "Lỗi cập nhật:\n"
                                                                + ex.getMessage(),
                                                        "Lỗi",
                                                        JOptionPane.ERROR_MESSAGE
                                                );

                                            } finally {
                                                editButton.setEnabled(true);
                                            }
                                        }
                                    };

                            updateWorker.execute();

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    LaptopPanel.this,
                                    "Không thể đọc thông tin Laptop:\n"
                                            + ex.getMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } finally {
                            // Chỉ bật lại nút nếu không có worker cập nhật
                            // đang chạy
                            if (editButton.isEnabled() == false) {
                                // Việc bật lại nút khi update xong
                                // được xử lý ở updateWorker
                            }
                        }
                    }
                };

        loadWorker.execute();
    }

    private void showImportStockDialog() {

        // 1. Kiểm tra dòng được chọn
        int selectedRow = laptopTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn laptop cần nhập kho!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Lấy ID laptop chính xác khi bảng có sắp xếp
        int modelRow =
                laptopTable.convertRowIndexToModel(selectedRow);

        int laptopId = (Integer) tableModel.getValueAt(
                modelRow, 0
        );

        String laptopName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        int currentQuantity = (Integer) tableModel.getValueAt(
                modelRow, 4
        );

        // 3. Mở hộp thoại nhập số lượng
        String input = JOptionPane.showInputDialog(
                this,
                "Laptop: " + laptopName
                        + "\nTồn kho hiện tại: " + currentQuantity
                        + "\nNhập số lượng cần thêm:",
                "Nhập kho",
                JOptionPane.QUESTION_MESSAGE
        );

        // Người dùng nhấn Cancel
        if (input == null) {
            return;
        }

        int quantity;

        // 4. Kiểm tra số lượng
        try {
            quantity = Integer.parseInt(input.trim());

            if (quantity <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Số lượng phải là số nguyên dương hợp lệ!",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 5. Xác nhận giao dịch
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn nhập thêm "
                        + quantity + " chiếc\n"
                        + laptopName + " vào kho?",
                "Xác nhận nhập kho",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 6. Khóa nút trong khi thực hiện
        importButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                // null là accountId tạm thời khi kiểm thử
                return stockService.importStock(
                        laptopId,
                        null,
                        quantity
                );
            }

            @Override
            protected void done() {

                try {
                    boolean success = get();

                    if (success) {

                        JOptionPane.showMessageDialog(
                                LaptopPanel.this,
                                "Nhập kho thành công!\n"
                                        + "Laptop: " + laptopName
                                        + "\nSố lượng nhập: " + quantity,
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        // Tải lại dữ liệu sau khi cập nhật
                        loadLaptopData(searchField.getText());

                    } else {

                        JOptionPane.showMessageDialog(
                                LaptopPanel.this,
                                "Nhập kho thất bại. "
                                        + "Laptop không tồn tại hoặc "
                                        + "không thể cập nhật số lượng.",
                                "Thông báo",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    JOptionPane.showMessageDialog(
                            LaptopPanel.this,
                            "Quá trình nhập kho bị gián đoạn.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException e) {

                    Throwable cause = e.getCause() != null
                            ? e.getCause()
                            : e;

                    JOptionPane.showMessageDialog(
                            LaptopPanel.this,
                            "Lỗi nhập kho:\n" + cause.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    importButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void showExportStockDialog() {

        // 1. Kiểm tra đã chọn laptop chưa
        int selectedRow = laptopTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn laptop cần xuất kho!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Lấy đúng ID khi bảng đang được sắp xếp
        int modelRow = laptopTable.convertRowIndexToModel(
                selectedRow
        );

        int laptopId = (Integer) tableModel.getValueAt(
                modelRow, 0
        );

        String laptopName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        int currentQuantity = (Integer) tableModel.getValueAt(
                modelRow, 4
        );

        // 3. Kiểm tra còn hàng hay không
        if (currentQuantity <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Laptop này đã hết hàng!\n"
                            + "Không thể thực hiện xuất kho.",
                    "Hết hàng",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 4. Yêu cầu nhập số lượng cần xuất
        String input = JOptionPane.showInputDialog(
                this,
                "Laptop: " + laptopName
                        + "\nTồn kho hiện tại: " + currentQuantity
                        + "\nNhập số lượng cần xuất:",
                "Xuất kho",
                JOptionPane.QUESTION_MESSAGE
        );

        // Nhấn Hủy
        if (input == null) {
            return;
        }

        int quantity;

        // 5. Kiểm tra dữ liệu nhập
        try {
            quantity = Integer.parseInt(input.trim());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Số lượng xuất phải lớn hơn 0!",
                        "Dữ liệu không hợp lệ",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập một số nguyên dương hợp lệ!",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 6. Kiểm tra số lượng tồn kho trên giao diện
        if (quantity > currentQuantity) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không đủ hàng để xuất!\n"
                            + "Tồn kho hiện tại: " + currentQuantity
                            + "\nSố lượng muốn xuất: " + quantity,
                    "Không đủ hàng",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // 7. Xác nhận xuất kho
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xuất " + quantity
                        + " chiếc\n"
                        + laptopName + " khỏi kho?",
                "Xác nhận xuất kho",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 8. Khóa nút khi đang xử lý
        exportButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                // accountId = null chỉ dùng khi kiểm thử
                return stockService.exportStock(
                        laptopId,
                        null,
                        quantity
                );
            }

            @Override
            protected void done() {

                try {
                    boolean success = get();

                    if (success) {

                        JOptionPane.showMessageDialog(
                                LaptopPanel.this,
                                "Xuất kho thành công!\n"
                                        + "Laptop: " + laptopName
                                        + "\nSố lượng xuất: " + quantity,
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        // Tải lại số lượng mới từ MySQL
                        loadLaptopData(searchField.getText());

                    } else {

                        JOptionPane.showMessageDialog(
                                LaptopPanel.this,
                                "Xuất kho thất bại!\n"
                                        + "Laptop không tồn tại hoặc "
                                        + "số lượng tồn kho không đủ.",
                                "Thông báo",
                                JOptionPane.WARNING_MESSAGE
                        );

                        // Tải lại vì tồn kho có thể đã thay đổi
                        loadLaptopData(searchField.getText());
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    JOptionPane.showMessageDialog(
                            LaptopPanel.this,
                            "Quá trình xuất kho bị gián đoạn.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (java.util.concurrent.ExecutionException e) {

                    Throwable cause = e.getCause() != null
                            ? e.getCause()
                            : e;

                    JOptionPane.showMessageDialog(
                            LaptopPanel.this,
                            "Lỗi xuất kho:\n" + cause.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    exportButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void showStockHistoryDialog() {

        // 1. Kiểm tra laptop được chọn
        int selectedRow = laptopTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn laptop để xem lịch sử kho!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Lấy ID chính xác khi JTable có sắp xếp
        int modelRow = laptopTable.convertRowIndexToModel(
                selectedRow
        );

        int laptopId = (Integer) tableModel.getValueAt(
                modelRow, 0
        );

        String laptopName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        // 3. Khóa nút trong khi tải dữ liệu
        historyButton.setEnabled(false);

        SwingWorker<List<StockMovement>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<StockMovement> doInBackground()
                            throws Exception {

                        return movementDAO.findByLaptopId(laptopId);
                    }

                    @Override
                    protected void done() {

                        // Bật lại nút trước khi mở hộp thoại
                        historyButton.setEnabled(true);

                        try {
                            List<StockMovement> movements = get();

                            // 4. Tạo bảng lịch sử
                            String[] columns = {
                                    "Mã giao dịch",
                                    "Laptop",
                                    "Loại giao dịch",
                                    "Số lượng",
                                    "Nhân viên ID",
                                    "Thời gian"
                            };

                            DefaultTableModel historyModel =
                                    new DefaultTableModel(columns, 0) {

                                        @Override
                                        public boolean isCellEditable(
                                                int row, int column
                                        ) {
                                            return false;
                                        }
                                    };

                            // 5. Định dạng thời gian
                            DateTimeFormatter formatter =
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy HH:mm:ss"
                                    );

                            // 6. Đưa dữ liệu MySQL vào JTable
                            for (StockMovement movement : movements) {

                                String type;

                                if ("IMPORT".equals(
                                        movement.getMovementType())) {

                                    type = "Nhập kho";

                                } else if ("EXPORT".equals(
                                        movement.getMovementType())) {

                                    type = "Xuất kho";

                                } else {

                                    type = movement.getMovementType();
                                }

                                String quantityText =
                                        "IMPORT".equals(
                                                movement.getMovementType())
                                                ? "+" + movement.getQuantity()
                                                : "-" + movement.getQuantity();

                                String accountText =
                                        movement.getAccountId() == null
                                                ? "Chưa xác định"
                                                : movement.getAccountId().toString();

                                String timeText =
                                        movement.getCreatedAt() == null
                                                ? "Chưa xác định"
                                                : movement.getCreatedAt().format(
                                                formatter
                                        );

                                Object[] row = {
                                        movement.getMovementId(),
                                        laptopName,
                                        type,
                                        quantityText,
                                        accountText,
                                        timeText
                                };

                                historyModel.addRow(row);
                            }

                            // 7. Tạo JTable
                            JTable historyTable = new JTable(historyModel);

                            historyTable.setRowHeight(30);
                            historyTable.setAutoCreateRowSorter(true);

                            historyTable.setDefaultEditor(
                                    Object.class, null
                            );

                            JScrollPane scrollPane =
                                    new JScrollPane(historyTable);

                            scrollPane.setPreferredSize(
                                    new Dimension(850, 350)
                            );

                            // 8. Thông báo nếu chưa có lịch sử
                            if (movements.isEmpty()) {

                                JOptionPane.showMessageDialog(
                                        LaptopPanel.this,
                                        "Laptop \"" + laptopName
                                                + "\" chưa có lịch sử "
                                                + "nhập/xuất kho.",
                                        "Lịch sử kho",
                                        JOptionPane.INFORMATION_MESSAGE
                                );

                                return;
                            }

                            // 9. Hiển thị lịch sử
                            JOptionPane.showMessageDialog(
                                    LaptopPanel.this,
                                    scrollPane,
                                    "Lịch sử kho - " + laptopName,
                                    JOptionPane.PLAIN_MESSAGE
                            );

                        } catch (Exception e) {

                            Throwable cause = e.getCause() != null
                                    ? e.getCause()
                                    : e;

                            JOptionPane.showMessageDialog(
                                    LaptopPanel.this,
                                    "Không thể tải lịch sử kho:\n"
                                            + cause.getMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void showDeleteLaptopDialog() {

        // 1. Kiểm tra đã chọn laptop chưa
        int selectedRow = laptopTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn laptop cần xóa!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Lấy ID khi JTable có sắp xếp
        int modelRow = laptopTable.convertRowIndexToModel(
                selectedRow
        );

        int laptopId = (Integer) tableModel.getValueAt(
                modelRow, 0
        );

        String laptopName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        int currentQuantity = (Integer) tableModel.getValueAt(
                modelRow, 4
        );

        // 3. Không cho xóa laptop còn tồn kho
        if (currentQuantity > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa laptop đang còn hàng!\n"
                            + "Số lượng tồn: " + currentQuantity,
                    "Không đủ điều kiện",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 4. Xác nhận trước khi xóa
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa laptop này?\n\n"
                        + "ID: " + laptopId
                        + "\nTên: " + laptopName
                        + "\n\nThao tác này không thể hoàn tác!",
                "Xác nhận xóa Laptop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 5. Khóa nút trong lúc xử lý
        deleteButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        // DAO kiểm tra lại điều kiện ngay trong SQL
                        return laptopDAO.delete(laptopId);
                    }

                    @Override
                    protected void done() {

                        try {
                            boolean deleted = get();

                            if (deleted) {

                                JOptionPane.showMessageDialog(
                                        LaptopPanel.this,
                                        "Đã xóa laptop thành công!",
                                        "Thành công",
                                        JOptionPane.INFORMATION_MESSAGE
                                );

                            } else {

                                JOptionPane.showMessageDialog(
                                        LaptopPanel.this,
                                        "Không thể xóa laptop!\n"
                                                + "Sản phẩm có thể đã có "
                                                + "lịch sử kho, hóa đơn, "
                                                + "bảo hành hoặc không tồn tại.",
                                        "Không đủ điều kiện",
                                        JOptionPane.WARNING_MESSAGE
                                );
                            }

                            // Luôn cập nhật danh sách sau thao tác
                            loadLaptopData(searchField.getText());

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            JOptionPane.showMessageDialog(
                                    LaptopPanel.this,
                                    "Thao tác xóa bị gián đoạn.",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } catch (java.util.concurrent.ExecutionException e) {

                            Throwable cause = e.getCause() != null
                                    ? e.getCause()
                                    : e;

                            JOptionPane.showMessageDialog(
                                    LaptopPanel.this,
                                    "Lỗi khi xóa laptop:\n"
                                            + cause.getMessage(),
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            loadLaptopData(searchField.getText());

                        } finally {
                            deleteButton.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

}
