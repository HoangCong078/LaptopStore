package com.laptopstore.view;

import com.laptopstore.model.Account;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Khung giao diện chính: thanh điều hướng + CardLayout chứa panel của 4 module.
 *
 * CÁCH GẮN MODULE CỦA BẠN (mỗi module là 1 class kế thừa JPanel):
 *   Thành viên 2: LaptopPanel   -> thay PlaceholderPanel ở KEY_PRODUCTS
 *   Thành viên 3: SalePanel     -> KEY_SALES
 *   Thành viên 4: WarrantyPanel -> KEY_WARRANTY
 *   Thành viên 1: AIPanel       -> KEY_AI
 * Chỉ cần gửi Pull Request đổi đúng 1 dòng trong buildModules() hoặc báo Thành viên 5 sửa.
 */
public class MainFrame extends JFrame {
    public static final String KEY_HOME = "home";
    public static final String KEY_PRODUCTS = "products";
    public static final String KEY_SALES = "sales";
    public static final String KEY_WARRANTY = "warranty";
    public static final String KEY_AI = "ai";

    private final Account currentAccount;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final JToolBar navBar = new JToolBar();
    private final ButtonGroup navGroup = new ButtonGroup();
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();
    private HomePanel homePanel;

    public MainFrame(Account account) {
        super("Cửa hàng Laptop");
        this.currentAccount = account;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        navBar.setFloatable(false);
        navBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setJMenuBar(buildMenuBar());
        add(navBar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        buildModules();
        showModule(KEY_HOME);

        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    /** Nơi đăng ký các module. adminOnly = true thì STAFF không thấy mục này. */
    private void buildModules() {
        homePanel = new HomePanel(currentAccount);
        addModule(KEY_HOME, "Trang chủ", homePanel, false);
        addModule(KEY_PRODUCTS, "Quản lý Laptop & Kho", new LaptopPanel(currentAccount), false);
        addModule(KEY_SALES, "Bán hàng", new PlaceholderPanel("Bán hàng"), false);
        addModule(KEY_WARRANTY, "Bảo hành", new PlaceholderPanel("Bảo hành"), false);
        addModule(KEY_AI, "Tư vấn AI", new PlaceholderPanel("Tư vấn AI"), false);
    }

    /** Thêm 1 module vào menu điều hướng. */
    public void addModule(String key, String title, JPanel panel, boolean adminOnly) {
        if (adminOnly && !currentAccount.isAdmin()) {
            return; // phân quyền: STAFF không thấy module dành cho ADMIN
        }
        content.add(panel, key);
        JToggleButton button = new JToggleButton(title);
        button.setFocusable(false);
        button.addActionListener(e -> showModule(key));
        navGroup.add(button);
        navBar.add(button);
        navButtons.put(key, button);
    }

    public void showModule(String key) {
        cardLayout.show(content, key);
        JToggleButton button = navButtons.get(key);
        if (button != null) button.setSelected(true);
        if (KEY_HOME.equals(key) && homePanel != null) {
            homePanel.refresh(); // thống kê luôn mới khi quay về trang chủ
        }
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Tài khoản");
        JMenuItem logout = new JMenuItem("Đăng xuất");
        JMenuItem exit = new JMenuItem("Thoát");
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất?",
                    "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        exit.addActionListener(e -> System.exit(0));
        menu.add(logout);
        menu.addSeparator();
        menu.add(exit);
        bar.add(menu);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.add(new JLabel("Người dùng: " + currentAccount.getFullName()
                + " (" + currentAccount.getRole() + ")"), BorderLayout.WEST);
        return status;
    }
}
