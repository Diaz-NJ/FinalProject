import javax.swing.*;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
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
    private JButton addButton, updateButton, deleteButton, clearButton, exportButton, showAllButton;
    private JLabel dateLabel;
    private Timer searchTimer; // For debouncing search

    public InventorySystem() {
        setTitle("Inventory Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with GridBagLayout for proper scaling
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Search Panel (top)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        mainPanel.add(searchPanel, gbc);

        // 2. Item Information Panel
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Item Information"));
        
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(2, 2, 2, 2);
        gbcInfo.fill = GridBagConstraints.HORIZONTAL;

        // ID Field
        gbcInfo.gridx = 0; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("ID:"), gbcInfo);
        gbcInfo.gridx = 1;
        idField = new JTextField(10);
        infoPanel.add(idField, gbcInfo);

        // Name
        gbcInfo.gridx = 2; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Name:"), gbcInfo);
        gbcInfo.gridx = 3;
        nameField = new JTextField(15);
        infoPanel.add(nameField, gbcInfo);

        // Quantity
        gbcInfo.gridx = 4; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Quantity:"), gbcInfo);
        gbcInfo.gridx = 5;
        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        intFormat.setGroupingUsed(false);
        NumberFormatter intFormatter = new NumberFormatter(intFormat);
        intFormatter.setMinimum(0);
        quantityField = new JFormattedTextField(intFormatter);
        quantityField.setColumns(5);
        infoPanel.add(quantityField, gbcInfo);

        // Price
        gbcInfo.gridx = 6; gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Price:"), gbcInfo);
        gbcInfo.gridx = 7;
        NumberFormat decimalFormat = NumberFormat.getNumberInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        NumberFormatter decimalFormatter = new NumberFormatter(decimalFormat);
        decimalFormatter.setMinimum(0.0);
        priceField = new JFormattedTextField(decimalFormatter);
        priceField.setColumns(8);
        infoPanel.add(priceField, gbcInfo);

        // Date
        gbcInfo.gridx = 3; gbcInfo.gridy = 1;
        gbcInfo.gridwidth = 8;
        dateLabel = new JLabel("Date: " + new java.util.Date());
        infoPanel.add(dateLabel, gbcInfo);

        mainPanel.add(infoPanel, gbc);

        // 3. Table (center - takes most space)
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        tableModel = new InventoryTableModel(inventory);
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer); // Quantity
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Price
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Total Value
        // Custom comparator for Total Value
        sorter.setComparator(4, (String s1, String s2) -> {
            double v1 = Double.parseDouble(s1.replace("$", ""));
            double v2 = Double.parseDouble(s2.replace("$", ""));
            return Double.compare(v1, v2);
        });
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, gbc);

        // 4. Buttons Panel (bottom)
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");
        exportButton = new JButton("Export");
        showAllButton = new JButton("Show All");
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(showAllButton);
        mainPanel.add(buttonPanel, gbc);

        // Add components to frame
        add(mainPanel);

        // Set tooltips
        addButton.setToolTipText("Add new item (Alt+A)");
        updateButton.setToolTipText("Update selected item (Alt+U)");
        deleteButton.setToolTipText("Delete selected item (Alt+D)");
        clearButton.setToolTipText("Clear form (Alt+C)");
        exportButton.setToolTipText("Export to CSV (Alt+E)");
        showAllButton.setToolTipText("Show all inventory items (Alt+S)");
        searchField.setToolTipText("Search by ID or name");

        // Add action listeners
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        clearButton.addActionListener(e -> clearForm());
        exportButton.addActionListener(e -> exportToCsv());
        showAllButton.addActionListener(e -> showAllItems());

        // Initialize search debounce timer
        searchTimer = new Timer(300, e -> filterItems());
        searchTimer.setRepeats(false);

        // Search listener with debounce
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchTimer.restart();
            }
        });

        // Consume Enter key in search field to prevent triggering addButton
        searchField.addActionListener(e -> {
            filterItems(); // Apply filter immediately on Enter
        });

        // Keyboard shortcuts
        addButton.setMnemonic(KeyEvent.VK_A);
        updateButton.setMnemonic(KeyEvent.VK_U);
        deleteButton.setMnemonic(KeyEvent.VK_D);
        clearButton.setMnemonic(KeyEvent.VK_C);
        exportButton.setMnemonic(KeyEvent.VK_E);
        showAllButton.setMnemonic(KeyEvent.VK_S);
        // Removed default button to prevent accidental addItem on Enter
        // getRootPane().setDefaultButton(addButton);

        // Table selection listener
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

        // Load inventory from file
        loadFromFile();

        // Save on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveToFile();
            }
        });
    }

    private void filterItems() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            if (query.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                String escapedQuery = Pattern.quote(query);
                sorter.setRowFilter(RowFilter.orFilter(
                    List.of(
                        RowFilter.regexFilter("(?i)" + escapedQuery, 0), // ID
                        RowFilter.regexFilter("(?i)" + escapedQuery, 1)  // Name
                    )
                ));
            }
        } catch (Exception e) {
            System.err.println("Filter error: " + e.getMessage());
            sorter.setRowFilter(null);
        }
        table.repaint();
    }

    private void showAllItems() {
        searchField.setText(""); // Clear search field
        sorter.setRowFilter(null); // Reset filter
        table.repaint();
        clearForm(); // Clear form to avoid confusion
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
            JOptionPane.showMessageDialog(this, "ID already exists");
        }
        // No error message here; validateInput handles it
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
                JOptionPane.showMessageDialog(this, "ID already exists");
            }
        } else if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to update");
        }
        // No error message here; validateInput handles it
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
            JOptionPane.showMessageDialog(this, "Please select an item to delete");
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
            JOptionPane.showMessageDialog(this, e.getMessage());
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
            System.err.println("Save error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
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
                        System.err.println("Skipping invalid line: " + line + ", error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
            tableModel.fireTableDataChanged();
        } catch (IOException e) {
            System.err.println("Load error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
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
                JOptionPane.showMessageDialog(this, "Export successful");
            } catch (IOException e) {
                System.err.println("Export error: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage());
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventorySystem().setVisible(true));
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

class InventoryTableModel extends javax.swing.table.AbstractTableModel {
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
            case 3 -> String.format("$%.2f", item.getPrice());
            case 4 -> String.format("$%.2f", item.getQuantity() * item.getPrice());
            default -> null;
        };
    }
}