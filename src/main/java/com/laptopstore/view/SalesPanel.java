package com.laptopstore.view;

import com.laptopstore.dao.JdbcLaptopDAO;
import com.laptopstore.dao.LaptopDAO;
import com.laptopstore.model.Laptop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SalesPanel extends JPanel {
    private LaptopDAO laptopDAO;
    private JTable laptopTable;
    private JTable cartTable;
    private DefaultTableModel laptopModel;
    private DefaultTableModel cartModel;
    private JLabel lblTotalAmount;
    private JButton btnAddToCart;
    private JButton btnRemoveCart;
    private JButton btnCheckout;
    private JTextField txtSearch;

    // Sửa dòng khai báo currencyFormat ở đầu class thành:
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    public SalesPanel() {
        this.laptopDAO = new JdbcLaptopDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        loadLaptopData();
    }

    private void initUI() {
        // --- BÊN TRÁI: Danh sách Laptop kho ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh sách Laptop khả dụng"));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> searchLaptop());
        searchPanel.add(new JLabel("Tìm tên/mã: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        String[] laptopCols = {"Mã ID", "Tên Laptop", "Đơn giá", "Tồn kho"};
        laptopModel = new DefaultTableModel(laptopCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        laptopTable = new JTable(laptopModel);
        leftPanel.add(new JScrollPane(laptopTable), BorderLayout.CENTER);

        btnAddToCart = new JButton(" Thêm vào giỏ hàng");
        btnAddToCart.setPreferredSize(new Dimension(0, 40));
        btnAddToCart.addActionListener(e -> addToCart());
        leftPanel.add(btnAddToCart, BorderLayout.SOUTH);

        // --- BÊN PHẢI: Giỏ hàng & Thanh toán ---
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Giỏ hàng của khách"));

        String[] cartCols = {"ID", "Tên Laptop", "Số lượng", "Đơn giá", "Thành tiền"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartModel);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("Tổng tiền: "));
        lblTotalAmount = new JLabel("0 VNĐ");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalAmount.setForeground(Color.RED);
        totalPanel.add(lblTotalAmount);

        btnRemoveCart = new JButton("Xóa khỏi giỏ");
        btnRemoveCart.addActionListener(e -> removeFromCart());

        btnCheckout = new JButton("Thanh toán & Xuất kho");
        btnCheckout.setPreferredSize(new Dimension(0, 45));
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckout.addActionListener(e -> processCheckout());

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cartActions.add(btnRemoveCart);

        checkoutPanel.add(totalPanel);
        checkoutPanel.add(cartActions);
        checkoutPanel.add(btnCheckout);
        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadLaptopData() {
        laptopModel.setRowCount(0);
        try {
            List<Laptop> list = laptopDAO.findAll(); 
            for (Laptop l : list) {
                if (l.getQuantity() > 0) { 
                    laptopModel.addRow(new Object[]{
                        l.getLaptopId(), l.getLaptopName(), 
                        currencyFormat.format(l.getPrice()), l.getQuantity()
                    });
                }
            }
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu kho: " + e.getMessage());
        }
    }

    private void searchLaptop() {
        String keyword = txtSearch.getText().trim();
        laptopModel.setRowCount(0);
        try {
            // Dùng luôn hàm search(keyword) siêu tiện lợi của ông
            List<Laptop> list = laptopDAO.search(keyword); 
            for (Laptop l : list) {
                if (l.getQuantity() > 0) {
                    laptopModel.addRow(new Object[]{
                        l.getLaptopId(), l.getLaptopName(), 
                        currencyFormat.format(l.getPrice()), l.getQuantity()
                    });
                }
            }
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void addToCart() {
        int selectedRow = laptopTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một laptop trong bảng để thêm!");
            return;
        }

        int laptopId = (int) laptopModel.getValueAt(selectedRow, 0);
        String name = (String) laptopModel.getValueAt(selectedRow, 1);
        int stock = (int) laptopModel.getValueAt(selectedRow, 3);

        try {
            // Dùng findById() theo DAO
            Laptop laptop = laptopDAO.findById(laptopId); 
            if (laptop == null) return;

            String inputQty = JOptionPane.showInputDialog(this, "Nhập số lượng mua (Tồn kho: " + stock + "):", "1");
            if (inputQty == null || inputQty.trim().isEmpty()) return;

            int qty = Integer.parseInt(inputQty.trim());
            if (qty <= 0 || qty > stock) {
                JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ hoặc vượt quá tồn kho!");
                return;
            }

            // Kiểm tra xem đã có trong giỏ chưa
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                if ((int) cartModel.getValueAt(i, 0) == laptopId) {
                    int currentQty = (int) cartModel.getValueAt(i, 2);
                    if (currentQty + qty > stock) {
                        JOptionPane.showMessageDialog(this, "Tổng số lượng vượt quá tồn kho hiện có!");
                        return;
                    }
                    int newQty = currentQty + qty;
                    java.math.BigDecimal newTotal = laptop.getPrice().multiply(java.math.BigDecimal.valueOf(newQty));
                    cartModel.setValueAt(newQty, i, 2);
                    cartModel.setValueAt(currencyFormat.format(newTotal), i, 4);
                    updateCartTotal();
                    return;
                }
            }

            // Thêm dòng mới vào giỏ
            java.math.BigDecimal total = laptop.getPrice().multiply(java.math.BigDecimal.valueOf(qty));
            cartModel.addRow(new Object[]{
                    laptopId, name, qty, 
                    currencyFormat.format(laptop.getPrice()), 
                    currencyFormat.format(total)
            });
            updateCartTotal();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên!");
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi truy xuất dữ liệu máy tính: " + e.getMessage());
        }
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow != -1) {
            cartModel.removeRow(selectedRow);
            updateCartTotal();
        }
    }

    private void updateCartTotal() {
        java.math.BigDecimal totalSum = java.math.BigDecimal.ZERO;
        try {
            List<Laptop> allLaptops = laptopDAO.findAll(); 
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int laptopId = (int) cartModel.getValueAt(i, 0);
                int qty = (int) cartModel.getValueAt(i, 2);
                for (Laptop l : allLaptops) {
                    if (l.getLaptopId() == laptopId) {
                        // Dùng .multiply() thay cho dấu * vì là BigDecimal
                        java.math.BigDecimal lineTotal = l.getPrice().multiply(java.math.BigDecimal.valueOf(qty));
                        totalSum = totalSum.add(lineTotal);
                        break;
                    }
                }
            }
            lblTotalAmount.setText(currencyFormat.format(totalSum));
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Thanh toán tổng: " + lblTotalAmount.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 1. Khởi tạo StockService của Khoa
                com.laptopstore.service.StockService stockService = new com.laptopstore.service.StockService();
                
                // 2. Duyệt qua giỏ hàng và gọi hàm xuất kho
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int laptopId = (int) cartModel.getValueAt(i, 0);
                    int qty = (int) cartModel.getValueAt(i, 2);
                    
                    // Gọi hàm exportStock (tham số accountId truyền tạm là null hoặc 1 nếu chưa có user login)
                    stockService.exportStock(laptopId, null, qty); 
                }
                
                JOptionPane.showMessageDialog(this, "Thanh toán thành công! Đã cập nhật lại tồn kho.");
                
                // 3. Xóa giỏ hàng và tải lại danh sách
                cartModel.setRowCount(0);
                updateCartTotal();
                loadLaptopData(); 
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xử lý kho: " + ex.getMessage());
                ex.printStackTrace(); // In lỗi ra terminal để dễ debug nếu sập
            }
        }
    }
}