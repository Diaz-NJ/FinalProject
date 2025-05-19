import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class InventorySystem extends JFrame {
    private List<InventoryItem> inventory = new ArrayList<>();
    private JTable table;
    private InventoryTableModel tableModel;

    // GUI Components
    private JTextField idField, nameField, quantityField, priceField;
    private JButton addButton, updateButton, deleteButton, clearButton;

    public InventorySystem() {
        setTitle("Inventory Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the table model
        tableModel = new InventoryTableModel(inventory);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));

        // ID Field
        formPanel.add(new JLabel("Item ID:"));
        idField = new JTextField();
        formPanel.add(idField);

        // Name Field
        formPanel.add(new JLabel("Item Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        // Quantity Field
        formPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        formPanel.add(quantityField);

        // Price Field
        formPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        formPanel.add(priceField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Add action listeners
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addItem();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateItem();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteItem();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    InventoryItem item = inventory.get(selectedRow);
                    idField.setText(item.getId());
                    nameField.setText(item.getName());
                    quantityField.setText(String.valueOf(item.getQuantity()));
                    priceField.setText(String.valueOf(item.getPrice()));
                }
            }
        });

        // Layout the main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addItem() {
        try {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            InventoryItem newItem = new InventoryItem(id, name, quantity, price);
            inventory.add(newItem);
            tableModel.fireTableDataChanged();
            clearForm();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and price", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());

                if (id.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "ID and Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                InventoryItem item = inventory.get(selectedRow);
                item.setId(id);
                item.setName(name);
                item.setQuantity(quantity);
                item.setPrice(price);

                tableModel.fireTableDataChanged();
                clearForm();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and price", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to update", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            inventory.remove(selectedRow);
            tableModel.fireTableDataChanged();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        quantityField.setText("");
        priceField.setText("");
        table.clearSelection();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InventorySystem system = new InventorySystem();
            system.setVisible(true);
        });
    }
}

class InventoryItem {
    private String id;
    private String name;
    private int quantity;
    private double price;

    public InventoryItem(String id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
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
    private final String[] columnNames = {"ID", "Name", "Quantity", "Price", "Total Value"};

    public InventoryTableModel(List<InventoryItem> inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getRowCount() {
        return inventory.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InventoryItem item = inventory.get(rowIndex);
        switch (columnIndex) {
            case 0: return item.getId();
            case 1: return item.getName();
            case 2: return item.getQuantity();
            case 3: return String.format("$%.2f", item.getPrice());
            case 4: return String.format("$%.2f", item.getQuantity() * item.getPrice());
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Make all cells non-editable
    }
}

