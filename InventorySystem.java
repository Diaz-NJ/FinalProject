import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.Timer;

public class InventorySystem extends JFrame {
    private List<InventoryItem> inventory = new ArrayList<>();
    private JTable table;
    private InventoryTableModel tableModel;
    private TableRowSorter<InventoryTableModel> sorter;

    // GUI Components
    private JTextField idField, nameField, searchField;
    private JFormattedTextField quantityField, priceField;
    private JButton addButton, updateButton, deleteButton, clearButton, exportButton, importButton, showAllButton;
    private JLabel dateLabel;
    private Timer searchTimer = new Timer(300, e -> filterItems());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            
            if (login.isAuthenticated()) {
                new InventorySystem().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }

    public InventorySystem() {
        setTitle("Inventory Management System (₱ PHP)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search Panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        mainPanel.add(searchPanel, gbc);

        // Item Information Panel
        gbc.gridy = 1;
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Item Information"));
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(2, 2, 2, 2);
        gbcInfo.fill = GridBagConstraints.HORIZONTAL;

        // ID Field
        gbcInfo.gridx = 0; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("ID:"));
        gbcInfo.gridx = 1;
        idField = new JTextField(10);
        infoPanel.add(idField, gbcInfo);

        // Name Field
        gbcInfo.gridx = 2; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Name:"));
        gbcInfo.gridx = 3;
        nameField = new JTextField(15);
        infoPanel.add(nameField, gbcInfo);

        // Quantity Field
        gbcInfo.gridx = 4; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Quantity:"));
        gbcInfo.gridx = 5;
        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        intFormat.setGroupingUsed(false);
        NumberFormatter intFormatter = new NumberFormatter(intFormat);
        intFormatter.setMinimum(0);
        quantityField = new JFormattedTextField(intFormatter);
        quantityField.setColumns(5);
        infoPanel.add(quantityField, gbcInfo);

        // Price Field (PHP)
        gbcInfo.gridx = 6; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Price (₱):"));
        gbcInfo.gridx = 7;
        NumberFormat decimalFormat = NumberFormat.getNumberInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        NumberFormatter decimalFormatter = new NumberFormatter(decimalFormat);
        decimalFormatter.setMinimum(0.0);
        priceField = new JFormattedTextField(decimalFormatter);
        priceField.setColumns(8);
        infoPanel.add(priceField, gbcInfo);

        // Date Label
        gbcInfo.gridx = 3; gbcInfo.gridy = 1; gbcInfo.gridwidth = 8;
        dateLabel = new JLabel("Date: " + new java.util.Date());
        infoPanel.add(dateLabel, gbcInfo);

        mainPanel.add(infoPanel, gbc);

        // Table (Center)
        gbc.gridy = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        tableModel = new InventoryTableModel(inventory);
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        // Custom comparator for Total Value
        sorter.setComparator(4, (String s1, String s2) -> {
            double v1 = Double.parseDouble(s1.replace("₱", ""));
            double v2 = Double.parseDouble(s2.replace("₱", ""));
            return Double.compare(v1, v2);
        });

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, gbc);

        // Buttons Panel
        gbc.gridy = 3; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");
        exportButton = new JButton("Export");
        importButton = new JButton("Import");
        showAllButton = new JButton("Show All");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);
        buttonPanel.add(showAllButton);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);

        // Tooltips
        addButton.setToolTipText("Add new item (Alt+A)");
        updateButton.setToolTipText("Update selected item (Alt+U)");
        deleteButton.setToolTipText("Delete selected item (Alt+D)");
        clearButton.setToolTipText("Clear form (Alt+C)");
        exportButton.setToolTipText("Export to CSV (Alt+E)");
        importButton.setToolTipText("Import from CSV (Alt+I)");
        showAllButton.setToolTipText("Show all inventory items (Alt+S)");
        searchField.setToolTipText("Search by ID or name");

        // Action Listeners
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        clearButton.addActionListener(e -> clearForm());
        exportButton.addActionListener(e -> exportToCsv());
        importButton.addActionListener(e -> importFromCsv());
        showAllButton.addActionListener(e -> showAllItems());

        // Search Debounce Timer
        searchTimer.setRepeats(false);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchTimer.restart();
            }
        });

        // Keyboard Shortcuts
        addButton.setMnemonic(KeyEvent.VK_A);
        updateButton.setMnemonic(KeyEvent.VK_U);
        deleteButton.setMnemonic(KeyEvent.VK_D);
        clearButton.setMnemonic(KeyEvent.VK_C);
        exportButton.setMnemonic(KeyEvent.VK_E);
        importButton.setMnemonic(KeyEvent.VK_I);
        showAllButton.setMnemonic(KeyEvent.VK_S);

        // Table Selection Listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                InventoryItem item = inventory.get(modelRow);
                idField.setText(item.getId());
                nameField.setText(item.getName());
                quantityField.setValue(item.getQuantity());
                priceField.setValue(item.getPrice());
            }
        });

        loadFromFile();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveToFile();
            }
        });
    }

    private void filterItems() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            String escapedQuery = Pattern.quote(query);
            sorter.setRowFilter(RowFilter.orFilter(
                List.of(
                    RowFilter.regexFilter("(?i)" + escapedQuery, 0),
                    RowFilter.regexFilter("(?i)" + escapedQuery, 1)
                )
            ));
        }
        table.repaint();
    }

    private void showAllItems() {
        searchField.setText("");
        sorter.setRowFilter(null);
        table.repaint();
        clearForm();
    }

    private boolean isUniqueId(String id, int excludeIndex) {
        for (int i = 0; i < inventory.size(); i++) {
            if (i != excludeIndex && inventory.get(i).getId().equalsIgnoreCase(id)) {
                return false;
            }
        }
        return true;
    }

    private void addItem() {
        String id = idField.getText().trim();
        if (validateInput() && isUniqueId(id, -1)) {
            InventoryItem item = new InventoryItem(
                id, nameField.getText().trim(),
                ((Number) quantityField.getValue()).intValue(),
                ((Number) priceField.getValue()).doubleValue()
            );
            inventory.add(item);
            tableModel.fireTableDataChanged();
            clearForm();
            saveToFile();
            checkLowStock(item);
            dateLabel.setText("Date: " + new java.util.Date());
        } else if (!isUniqueId(id, -1)) {
            JOptionPane.showMessageDialog(this, "ID already exists", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && validateInput()) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            String id = idField.getText().trim();
            if (isUniqueId(id, modelRow)) {
                InventoryItem item = inventory.get(modelRow);
                item.setId(id);
                item.setName(nameField.getText().trim());
                item.setQuantity(((Number) quantityField.getValue()).intValue());
                item.setPrice(((Number) priceField.getValue()).doubleValue());
                tableModel.fireTableDataChanged();
                saveToFile();
                checkLowStock(item);
                dateLabel.setText("Date: " + new java.util.Date());
            } else {
                JOptionPane.showMessageDialog(this, "ID already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to update", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                inventory.remove(table.convertRowIndexToModel(selectedRow));
                tableModel.fireTableDataChanged();
                clearForm();
                saveToFile();
                dateLabel.setText("Date: " + new java.util.Date());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        quantityField.setValue(null);
        priceField.setValue(null);
        table.clearSelection();
    }

    private boolean validateInput() {
        try {
            if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                throw new Exception("ID and Name are required");
            }
            Number quantityObj = (Number) quantityField.getValue();
            Number priceObj = (Number) priceField.getValue();
            if (quantityObj == null || priceObj == null) {
                throw new Exception("Invalid quantity or price");
            }
            int quantity = quantityObj.intValue();
            double price = priceObj.doubleValue();
            if (quantity < 0) throw new Exception("Quantity cannot be negative");
            if (price < 0) throw new Exception("Price cannot be negative");
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter("inventory.csv")) {
            for (InventoryItem item : inventory) {
                writer.println(String.format("%s,%s,%d,%.2f",
                    item.getId(), item.getName().replace(",", ""),
                    item.getQuantity(), item.getPrice()));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFromFile() {
        File file = new File("inventory.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length == 4) {
                    try {
                        inventory.add(new InventoryItem(
                            parts[0], parts[1],
                            Integer.parseInt(parts[2]), Double.parseDouble(parts[3])
                        ));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid line: " + line);
                    }
                }
            }
            tableModel.fireTableDataChanged();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("inventory_export.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("ID,Name,Quantity,Price,Total Value");
                for (InventoryItem item : inventory) {
                    writer.println(String.format("%s,%s,%d,%.2f,%.2f",
                        item.getId(), item.getName().replace(",", ""),
                        item.getQuantity(), item.getPrice(), item.getQuantity() * item.getPrice()));
                }
                JOptionPane.showMessageDialog(this, "Export successful!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importFromCsv() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                inventory.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 4) {
                        try {
                            inventory.add(new InventoryItem(
                                parts[0], parts[1],
                                Integer.parseInt(parts[2]),
                                Double.parseDouble(parts[3])
                            ));
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping invalid line: " + line);
                        }
                    }
                }
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Import successful!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Import failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkLowStock(InventoryItem item) {
        if (item.getQuantity() <= 5) {
            JOptionPane.showMessageDialog(this,
                "Warning: Low stock for " + item.getName() + " (Quantity: " + item.getQuantity() + ")",
                "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        }
    }
}

// ======================== SECURE LOGIN DIALOG ========================
class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean authenticated = false;
    private String currentUser;

    // =============================================
    // ADMIN CONFIGURATION SECTION - EDIT HERE ONLY
    // =============================================
    private static final Map<String, String> HARDCODED_ADMINS = new HashMap<>() {{
        put("admin", "admin123");  // Default admin account
        // Add more permanent admins here:
        // put("admin2", "securePassword123");
    }};
    
    private static final Map<String, String> users = new HashMap<>() {{
        putAll(HARDCODED_ADMINS);  // Includes all admins
        put("user", "user123");    // Regular user account
        // Add regular users here:
        // put("user2", "password123");
    }};
    
    private static final Map<String, String> userRoles = new HashMap<>() {{
        HARDCODED_ADMINS.keySet().forEach(name -> put(name, "admin")); // All admins
        put("user", "user");  // Regular user
        // Add roles for additional users here:
        // put("user2", "user");
    }};
    // =============================================
    // END ADMIN CONFIGURATION SECTION
    // =============================================

    public LoginDialog(JFrame parent) {
        super(parent, "Login", true);
        setSize(400, 250);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));

        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::attemptLogin);
        loginPanel.add(loginButton);

        JButton manageButton = new JButton("Manage Users");
        manageButton.addActionListener(e -> showUserManagement());
        loginPanel.add(manageButton);

        gbc.gridx = 0; gbc.gridy = 0;
        add(loginPanel, gbc);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> System.exit(0));
        gbc.gridy = 1;
        add(closeButton, gbc);
    }

    private void attemptLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            authenticated = true;
            currentUser = username;
            JOptionPane.showMessageDialog(this, "Logged in as " + username);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserManagement() {
        if (!userRoles.getOrDefault(currentUser, "").equals("admin")) {
            JOptionPane.showMessageDialog(this, "Only admins can manage users", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog manageDialog = new JDialog(this, "User Management", true);
        manageDialog.setSize(400, 300);
        manageDialog.setLayout(new BorderLayout());

        // User Table
        String[] columnNames = {"Username", "Role"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String username = (String) getValueAt(row, 0);
                return column == 1 && !HARDCODED_ADMINS.containsKey(username);
            }
        };

        users.keySet().forEach(user -> {
            model.addRow(new Object[]{user, userRoles.get(user)});
        });

        JTable userTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(userTable);
        manageDialog.add(scrollPane, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 5, 5));

        JButton addButton = new JButton("Add User");
        addButton.addActionListener(e -> {
            String newUser = JOptionPane.showInputDialog("Enter username:");
            if (newUser != null && !newUser.trim().isEmpty()) {
                if (users.containsKey(newUser)) {
                    JOptionPane.showMessageDialog(manageDialog, "User already exists");
                    return;
                }
                String newPass = JOptionPane.showInputDialog("Enter password:");
                if (newPass != null && !newPass.trim().isEmpty()) {
                    users.put(newUser, newPass);
                    userRoles.put(newUser, "user"); // New users are always regular users
                    model.addRow(new Object[]{newUser, "user"});
                }
            }
        });

        JButton removeButton = new JButton("Remove User");
        removeButton.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                String username = (String) userTable.getValueAt(row, 0);
                if (HARDCODED_ADMINS.containsKey(username)) {
                    JOptionPane.showMessageDialog(manageDialog, "Cannot remove admin accounts");
                    return;
                }
                users.remove(username);
                userRoles.remove(username);
                model.removeRow(row);
            }
        });

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            // Update roles for editable users
            for (int i = 0; i < model.getRowCount(); i++) {
                String username = (String) model.getValueAt(i, 0);
                if (!HARDCODED_ADMINS.containsKey(username)) {
                    userRoles.put(username, (String) model.getValueAt(i, 1));
                }
            }
            manageDialog.dispose();
        });

        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        controlPanel.add(saveButton);
        manageDialog.add(controlPanel, BorderLayout.SOUTH);

        manageDialog.setVisible(true);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}

class InventoryItem {
    private String id, name;
    private int quantity;
    private double price;

    public InventoryItem(String id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

class InventoryTableModel extends AbstractTableModel {
    private final List<InventoryItem> inventory;
    private final String[] columns = {"ID", "Name", "Quantity", "Price", "Total Value"};

    public InventoryTableModel(List<InventoryItem> inventory) {
        this.inventory = inventory;
    }

    @Override public int getRowCount() { return inventory.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int column) {
        InventoryItem item = inventory.get(row);
        return switch (column) {
            case 0 -> item.getId();
            case 1 -> item.getName();
            case 2 -> item.getQuantity();
            case 3 -> String.format("₱%.2f", item.getPrice());
            case 4 -> String.format("₱%.2f", item.getQuantity() * item.getPrice());
            default -> null;
        };
    }
}