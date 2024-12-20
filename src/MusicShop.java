import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.net.URL;

public class MusicShop {

    private JFrame frame;
    private JTable albumsTable;
    private JTable inventoryTable;
    private DefaultTableModel albumsModel;
    private DefaultTableModel inventoryModel;
    private Connection connection;

    public MusicShop() {
        try {
            connectToDatabase();
            initializeGUI();
        } catch (Exception e) {
            e.printStackTrace(); // Печать стека исключений
            JOptionPane.showMessageDialog(null, "Произошла ошибка при запуске приложения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Завершаем приложение, если ошибка
        }
    }

private void connectToDatabase() {
    try {
        String dbPath = "music_shop.db";
        File dbFile = new File(dbPath);
        System.out.println("Путь к базе данных: " + dbFile.getAbsolutePath());

        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS Albums (id INTEGER PRIMARY KEY, name TEXT UNIQUE, artist TEXT, expected_sales INTEGER, available TEXT)");
        statement.execute("CREATE TABLE IF NOT EXISTS Inventory (id INTEGER PRIMARY KEY, album_id INTEGER, quantity INTEGER, FOREIGN KEY(album_id) REFERENCES Albums(id))");

        System.out.println("Успешное подключение к базе данных.");
    } catch (SQLException e) {
        e.printStackTrace(); // Печать стека ошибок подключения
        JOptionPane.showMessageDialog(null, "Ошибка подключения к базе данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        System.exit(1); // Завершаем программу при ошибке
    }
}
    private ImageIcon loadIcon(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("Не удалось найти иконку: " + path);
            return null;
        }
        ImageIcon icon = new ImageIcon(resource);
        // Задаем целевой размер иконки (например, 32x32 пикселя)
        Image scaledImage = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
    
    private void initializeGUI() {
        frame = new JFrame("Музыкальный магазин");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
    
        JTabbedPane tabbedPane = new JTabbedPane();
    
        // Панель альбомов
        JPanel albumsPanel = new JPanel(new BorderLayout());
        albumsModel = new DefaultTableModel(new Object[]{"ID", "Название альбома", "Исполнитель", "Ожидаемые продажи", "В наличии"}, 0);
        albumsTable = new JTable(albumsModel);
        albumsTable.setDefaultEditor(Object.class, null); // Отключение редактирования
        loadAlbums();
    
        JPanel albumsButtonPanel = new JPanel();
    
        JButton addAlbumButton = new JButton(loadIcon("/icons/add.png"));
        //JButton editAlbumsButton = new JButton(loadIcon("/icons/edit.png"));
        JButton deleteAlbumsButton = new JButton(loadIcon("/icons/delete.png"));
        JButton runTestsButton = new JButton(loadIcon("/icons/tests.png"));
        JButton addRandomAlbumButton = new JButton(loadIcon("/icons/random.png"));
        JButton addFromTXTButton = new JButton(loadIcon("/icons/import_txt.png"));
        JButton addFromXmlButton = new JButton(loadIcon("/icons/import_xml.png"));
        JButton saveToTXTButton = new JButton(loadIcon("/icons/export_txt.png"));
        JButton saveToXmlButton = new JButton(loadIcon("/icons/export_xml.png"));

        JPanel editAlbumsPanel = new JPanel();
        JButton editAlbumsButton = new JButton("Изменить"); 
    
        // Установка подсказок для кнопок
        addAlbumButton.setToolTipText("Добавить альбом");
        editAlbumsButton.setToolTipText("Изменить альбом");
        deleteAlbumsButton.setToolTipText("Удалить альбом");
        runTestsButton.setToolTipText("Запуск тестов");
        addRandomAlbumButton.setToolTipText("Добавить случайный альбом");
        addFromTXTButton.setToolTipText("Добавить альбомы из TXT");
        addFromXmlButton.setToolTipText("Добавить альбомы из XML");
        saveToTXTButton.setToolTipText("Сохранить альбомы в TXT");
        saveToXmlButton.setToolTipText("Сохранить альбомы в XML");
    
        // Добавляем кнопки в панель
        albumsButtonPanel.add(addAlbumButton);
        albumsButtonPanel.add(deleteAlbumsButton);
        albumsButtonPanel.add(runTestsButton);
        albumsButtonPanel.add(addRandomAlbumButton);
        albumsButtonPanel.add(addFromTXTButton);
        albumsButtonPanel.add(addFromXmlButton);
        albumsButtonPanel.add(saveToTXTButton);
        albumsButtonPanel.add(saveToXmlButton);

        editAlbumsPanel.add(editAlbumsButton);
    
        albumsPanel.add(new JScrollPane(albumsTable), BorderLayout.CENTER);
        albumsPanel.add(albumsButtonPanel, BorderLayout.NORTH);
        albumsPanel.add(editAlbumsPanel, BorderLayout.SOUTH);
    
        // Привязываем действия
        addAlbumButton.addActionListener(e -> addAlbum());
        editAlbumsButton.addActionListener(e -> toggleTableEditing(albumsTable, editAlbumsButton));
        deleteAlbumsButton.addActionListener(e -> deleteAlbums());
        runTestsButton.addActionListener(e -> runTests());
        addRandomAlbumButton.addActionListener(e -> addRandomAlbum());
        addFromTXTButton.addActionListener(e -> loadDataFromTXT());
        addFromXmlButton.addActionListener(e -> loadDataFromXml());
        saveToTXTButton.addActionListener(e -> saveDataToTXT());
        saveToXmlButton.addActionListener(e -> saveDataToXml());
    
        tabbedPane.add("Альбомы", albumsPanel);

    // Панель инвентаря
    JPanel inventoryPanel = new JPanel(new BorderLayout());
    inventoryModel = new DefaultTableModel(new Object[]{"ID", "Название альбома", "Количество"}, 0);
    inventoryTable = new JTable(inventoryModel);
    inventoryTable.setDefaultEditor(Object.class, null); // Отключение редактирования
    loadInventory();

    JPanel inventoryButtonPanel = new JPanel();

    JButton addInventoryButton = new JButton(loadIcon("/icons/add.png"));
    //JButton editInventoryButton = new JButton(loadIcon("/icons/edit.png"));
    JButton deleteInventoryButton = new JButton(loadIcon("/icons/delete.png"));

    addInventoryButton.setToolTipText("Добавить в инвентарь");
    //editInventoryButton.setToolTipText("Изменить");
    deleteInventoryButton.setToolTipText("Удалить из инвентаря");

    
    JPanel editInventoryPanel = new JPanel();
    JButton editInventoryButton = new JButton("Изменить"); 
    
    inventoryButtonPanel.add(addInventoryButton);
    inventoryButtonPanel.add(deleteInventoryButton);

    editInventoryPanel.add(editInventoryButton);

    inventoryPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
    inventoryPanel.add(inventoryButtonPanel, BorderLayout.NORTH);
    inventoryPanel.add(editInventoryPanel, BorderLayout.SOUTH);

    // Добавление действия для кнопки "Добавить в инвентарь"
    addInventoryButton.addActionListener(e -> addInventory());

    // Добавление действия для кнопки "Изменить"
    editInventoryButton.addActionListener(e -> toggleTableEditing(inventoryTable, editInventoryButton));

    // Привязываем действие
    deleteInventoryButton.addActionListener(e -> deleteInventory());

    tabbedPane.add("Инвентарь", inventoryPanel);

    frame.add(tabbedPane);
    frame.setVisible(true);

    albumsModel.addTableModelListener(e -> {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            Object newValue = albumsModel.getValueAt(row, column);
    
            // Проверяем, какой альбом был изменён
            if (column > 0) { // Колонка ID не редактируется
                int albumId = (int) albumsModel.getValueAt(row, 0); // ID из первой колонки
                updateAlbumInDatabase(albumId, column, newValue);
    
                // Запуск обновления "Есть в наличии" после изменения
                updateAvailability();
            }
        }
    });
    
    inventoryModel.addTableModelListener(e -> {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            Object newValue = inventoryModel.getValueAt(row, column);
    
            // Проверяем, какой элемент инвентаря был изменён
            if (column > 0) { // Колонка ID не редактируется
                int inventoryId = (int) inventoryModel.getValueAt(row, 0); // ID из первой колонки
                updateInventoryInDatabase(inventoryId, column, newValue);
    
                // Обновляем наличие после изменения количества
                updateAvailability();
            }
        }
    });


    /* 

    saveToXmlButton.addActionListener(e -> {
        SwingWorker<Void, Void> saveXmlWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                System.out.println("Сохранение данных в XML...");
                saveDataToXml(); // Сохранение данных в XML-файл
                System.out.println("Данные успешно сохранены в XML.");
                return null;
            }
        };
        saveXmlWorker.execute();
    });

    addFromXmlButton.addActionListener(e -> {
        SwingWorker<Void, Void> loadXmlWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                System.out.println("Загрузка данных из XML...");
                loadDataFromXml(); // Загрузка данных из XML-файла
                System.out.println("Данные успешно загружены из Xml.");
                return null;
            }
        };
        loadXmlWorker.execute();
    });

    saveToTXTButton.addActionListener(e -> {
        SwingWorker<Void, Void> saveTXTWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                System.out.println("Сохранение данных в TXT...");
                saveDataToTXT(); // Сохранение данных в TXT-файл
                System.out.println("Данные успешно сохранены в TXT.");
                return null;
            }
        };
        saveTXTWorker.execute();
    });

    addFromTXTButton.addActionListener(e -> {
        SwingWorker<Void, Void> loadTXTWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                System.out.println("Загрузка данных из TXT...");
                loadDataFromTXT(); // Загрузка данных из TXT-файла
                System.out.println("Данные успешно загружены из TXT.");
                return null;
            }
        };
        loadTXTWorker.execute();
    });
*/
}
    private void toggleTableEditing(JTable table, JButton button) {
        if ("Изменить".equals(button.getText())) {
            table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));
            table.setCellSelectionEnabled(true);
            button.setText("Завершить редактирование");
        } else {
            table.setDefaultEditor(Object.class, null);
            table.setCellSelectionEnabled(false);
            button.setText("Изменить");
    
            // Запуск обновления наличия после завершения редактирования
            if (table == albumsTable || table == inventoryTable) {
                updateAvailability();
            }
        }
    }    

    private void updateAlbumInDatabase(int albumId, int column, Object newValue) {
        //new Thread(() -> { // Фоновый поток
            String columnName = "";
            switch (column) {
                case 1: columnName = "name"; break;           // Название альбома
                case 2: columnName = "artist"; break;         // Исполнитель
                case 3: columnName = "expected_sales"; break; // Продажи
                case 4: columnName = "available"; break;      // Наличие
            }
    
            if (!columnName.isEmpty()) {
                try {
                    String updateSQL = "UPDATE Albums SET " + columnName + " = ? WHERE id = ?";
                    PreparedStatement ps = connection.prepareStatement(updateSQL);
                    ps.setObject(1, newValue);
                    ps.setInt(2, albumId);
                    ps.executeUpdate();
    
                    System.out.println("Запись обновлена в базе данных.");
    
                    // Обновление интерфейса по завершении
                    SwingUtilities.invokeLater(this::loadAlbums);
    
                } catch (SQLException e) {
                    System.err.println("Ошибка обновления записи: " + e.getMessage());
                }
            }
        //}).start(); // Запуск потока
    }

    private void updateInventoryInDatabase(int inventoryId, int column, Object newValue) {
        //new Thread(() -> { // Фоновый поток
            String columnName = "";
            switch (column) {
                case 1: columnName = "name"; break;     // Название альбома
                case 2: columnName = "quantity"; break; // Количество
            }
    
            if (!columnName.isEmpty()) {
                try {
                    String updateSQL = "UPDATE Inventory SET " + columnName + " = ? WHERE id = ?";
                    PreparedStatement ps = connection.prepareStatement(updateSQL);
                    ps.setObject(1, newValue);
                    ps.setInt(2, inventoryId);
                    ps.executeUpdate();
    
                    System.out.println("Запись инвентаря обновлена в базе данных.");
    
                    // Обновление интерфейса по завершении
                    SwingUtilities.invokeLater(this::loadInventory);
    
                } catch (SQLException e) {
                    System.err.println("Ошибка обновления инвентаря: " + e.getMessage());
                }
            }
        //}).start(); // Запуск потока
    }
    

    private void loadAlbums() {
        try {
            albumsModel.setRowCount(0);  // Очищаем модель перед загрузкой новых данных
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Albums");

            while (rs.next()) {
                albumsModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("artist"),
                        rs.getInt("expected_sales"),
                        rs.getString("available")
                });
            }

            albumsModel.fireTableDataChanged(); // Уведомляем JTable, что данные обновились
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки альбомов: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Ошибка загрузки альбомов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInventory() {
        try {
            inventoryModel.setRowCount(0); // Очищаем модель перед загрузкой данных
    
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(
                "SELECT Inventory.id, Albums.name AS album_name, Inventory.quantity " +
                "FROM Inventory INNER JOIN Albums ON Inventory.album_id = Albums.id"
            );
    
            while (rs.next()) {
                inventoryModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("album_name"),
                    rs.getInt("quantity")
                });
            }
    
            inventoryModel.fireTableDataChanged(); // Обновляем отображение модели
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки данных инвентаря: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Ошибка загрузки данных инвентаря: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAvailability() {
        //new Thread(() -> { // Фоновый поток
            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                    "SELECT Albums.id, IFNULL(SUM(Inventory.quantity), 0) AS total_quantity " +
                    "FROM Albums LEFT JOIN Inventory ON Albums.id = Inventory.album_id GROUP BY Albums.id"
                );
    
                while (rs.next()) {
                    int albumId = rs.getInt("id");
                    int totalQuantity = rs.getInt("total_quantity");
                    String availability = totalQuantity > 0 ? "Есть" : "Нет";
    
                    PreparedStatement ps = connection.prepareStatement("UPDATE Albums SET available = ? WHERE id = ?");
                    ps.setString(1, availability);
                    ps.setInt(2, albumId);
                    ps.executeUpdate();
                }
    
                System.out.println("Статус наличия альбомов обновлён.");
                
                // Обновление интерфейса по завершении
                SwingUtilities.invokeLater(this::loadAlbums);
    
            } catch (SQLException e) {
                System.err.println("Ошибка обновления наличия альбомов: " + e.getMessage());
            }
        //}).start(); // Запуск потока
    }
    

    private void addAlbum() {
        JTextField nameField = new JTextField();
        JTextField artistField = new JTextField();
        JTextField salesField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название альбома:"));
        panel.add(nameField);
        panel.add(new JLabel("Имя исполнителя:"));
        panel.add(artistField);
        panel.add(new JLabel("Ожидаемые продажи:"));
        panel.add(salesField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Добавить альбом", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String artist = artistField.getText().trim();
                int expectedSales = Integer.parseInt(salesField.getText().trim());

                PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, 'Нет')");
                ps.setString(1, name);
                ps.setString(2, artist);
                ps.setInt(3, expectedSales);
                ps.executeUpdate();

                loadAlbums();  // Загружаем данные заново в таблицу

            } catch (SQLException | NumberFormatException e) {
                System.err.println("Ошибка добавления альбома: " + e.getMessage());
                JOptionPane.showMessageDialog(frame, "Ошибка добавления альбома. Убедитесь, что все поля заполнены корректно.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void addInventory() {
        try {
            JComboBox<String> albumsComboBox = new JComboBox<>();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Albums");

            while (rs.next()) {
                albumsComboBox.addItem(rs.getInt("id") + ": " + rs.getString("name"));
            }

            JTextField quantityField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Выберите альбом:"));
            panel.add(albumsComboBox);
            panel.add(new JLabel("Количество:"));
            panel.add(quantityField);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Добавить в инвентарь", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String selectedAlbum = (String) albumsComboBox.getSelectedItem();
                if (selectedAlbum != null) {
                    int albumId = Integer.parseInt(selectedAlbum.split(":")[0]);
                    int quantity = Integer.parseInt(quantityField.getText().trim());

                    PreparedStatement ps = connection.prepareStatement("INSERT INTO Inventory (album_id, quantity) VALUES (?, ?)");
                    ps.setInt(1, albumId);
                    ps.setInt(2, quantity);
                    ps.executeUpdate();

                    updateAvailability();
                    loadInventory();
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Ошибка добавления в инвентарь: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Ошибка добавления в инвентарь. Убедитесь, что введены корректные данные.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAlbums() {
        if (albumsTable.getRowCount() > 0) {
            if (albumsTable.getSelectedRow() != -1) {
                try {
                    // Получаем ID альбома, выбранного пользователем
                    int albumId = (int) albumsModel.getValueAt(albumsTable.convertRowIndexToModel(albumsTable.getSelectedRow()), 0);
    
                    // Проверяем, есть ли записи в Inventory, связанные с этим альбомом
                    String checkSQL = "SELECT COUNT(*) AS count FROM Inventory WHERE album_id = ?";
                    PreparedStatement checkPs = connection.prepareStatement(checkSQL);
                    checkPs.setInt(1, albumId);
                    ResultSet rs = checkPs.executeQuery();
    
                    if (rs.next() && rs.getInt("count") > 0) {
                        JOptionPane.showMessageDialog(frame, "Невозможно удалить альбом. Сначала удалите связанные записи из инвентаря.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    } else {
                        // Удаляем альбом из базы данных
                        String deleteSQL = "DELETE FROM Albums WHERE id = ?";
                        PreparedStatement deletePs = connection.prepareStatement(deleteSQL);
                        deletePs.setInt(1, albumId);
                        deletePs.executeUpdate();
    
                        // Удаляем строку из таблицы
                        albumsModel.removeRow(albumsTable.convertRowIndexToModel(albumsTable.getSelectedRow()));
                        JOptionPane.showMessageDialog(frame, "Альбом успешно удалён.");
                    }
                } catch (SQLException e) {
                    System.err.println("Ошибка удаления альбома: " + e.getMessage());
                    JOptionPane.showMessageDialog(frame, "Ошибка удаления альбома.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Вы не выбрали строку для удаления.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "В таблице альбомов нет записей.");
        }
    }
    

    private void deleteInventory() {
        // Проверяем, что в таблице есть строки
        if (inventoryTable.getRowCount() > 0) {
            // Проверяем, что пользователь выбрал строку
            if (inventoryTable.getSelectedRow() != -1) {
                try {
                    // Получаем ID записи инвентаря, выбранной пользователем
                    int inventoryId = (int) inventoryModel.getValueAt(inventoryTable.convertRowIndexToModel(inventoryTable.getSelectedRow()), 0);
    
                    // Удаляем из базы данных
                    String deleteSQL = "DELETE FROM Inventory WHERE id = ?";
                    PreparedStatement ps = connection.prepareStatement(deleteSQL);
                    ps.setInt(1, inventoryId);
                    ps.executeUpdate();
    
                    // Удаляем строку из таблицы
                    inventoryModel.removeRow(inventoryTable.convertRowIndexToModel(inventoryTable.getSelectedRow()));
                    JOptionPane.showMessageDialog(frame, "Запись из инвентаря успешно удалена.");
                    
                    // Обновление наличия после удаления
                    updateAvailability();
                } catch (SQLException e) {
                    System.err.println("Ошибка удаления из инвентаря: " + e.getMessage());
                    JOptionPane.showMessageDialog(frame, "Ошибка удаления из инвентаря.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Вы не выбрали строку для удаления из инвентаря.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "В таблице инвентаря нет записей.");
        }
    }    

    private void runTests() {
        SwingWorker<Void, Void> testWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    org.junit.runner.JUnitCore.main("MusicShopTests");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Ошибка при запуске тестов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
    
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(frame, "Тесты завершены. Проверьте консоль для результатов.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        testWorker.execute();
    }

    private void addRandomAlbum() {
        // Массивы с возможными значениями
        String[] albumNames = {"The Best of Jazz", "Rock Legends", "Pop Hits", "Classical Symphony", "Indie Vibes"};
        String[] artists = {"John Doe", "The Beatles", "Queen", "Beethoven", "Radiohead"};
    
        // Генерация случайных значений для альбома
        String randomAlbumName = albumNames[(int) (Math.random() * albumNames.length)];
        String randomArtist = artists[(int) (Math.random() * artists.length)];
        int randomExpectedSales = (int) (Math.random() * 10000) + 500; // От 500 до 10000
    
        try {
            // Проверка на уникальность названия альбома
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM Albums WHERE name = ?"
            );
            checkStmt.setString(1, randomAlbumName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Альбом с таким названием уже существует.");
            }
    
            // Вставка нового альбома в таблицу "Альбомы"
            PreparedStatement albumStmt = connection.prepareStatement(
                "INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, 'Нет')", 
                Statement.RETURN_GENERATED_KEYS
            );
            albumStmt.setString(1, randomAlbumName);
            albumStmt.setString(2, randomArtist);
            albumStmt.setInt(3, randomExpectedSales);
            albumStmt.executeUpdate();
    
            // Получение ID нового альбома
            ResultSet generatedKeys = albumStmt.getGeneratedKeys();
            int albumId = -1;
            if (generatedKeys.next()) {
                albumId = generatedKeys.getInt(1);
            }
    
            if (albumId != -1) {
                // Генерация случайного количества для инвентаря (включая 0)
                int randomQuantity = (int) (Math.random() * 10); // От 0 до 9
    
                // Вставка данных в таблицу "Инвентарь"
                PreparedStatement inventoryStmt = connection.prepareStatement(
                    "INSERT INTO Inventory (album_id, quantity) VALUES (?, ?)"
                );
                inventoryStmt.setInt(1, albumId);
                inventoryStmt.setInt(2, randomQuantity);
                inventoryStmt.executeUpdate();
    
                // Обновление статуса наличия для альбома
                PreparedStatement updateAvailability = connection.prepareStatement(
                    "UPDATE Albums SET available = ? WHERE id = ?"
                );
                updateAvailability.setString(1, randomQuantity > 0 ? "Есть" : "Нет");
                updateAvailability.setInt(2, albumId);
                updateAvailability.executeUpdate();
            }
    
            // Обновление данных в интерфейсе
            SwingUtilities.invokeLater(this::loadInventory);
            loadAlbums();

            JOptionPane.showMessageDialog(frame, "Случайный альбом добавлен в базу данных.", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            System.err.println("Ошибка добавления случайного альбома: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Ошибка добавления случайного альбома: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    } 
    
    private void saveDataToTXT() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("# Albums\n");
                for (int i = 0; i < albumsModel.getRowCount(); i++) {
                    bw.write(albumsModel.getValueAt(i, 1) + "," + albumsModel.getValueAt(i, 2) + "," + 
                             albumsModel.getValueAt(i, 3) + "," + albumsModel.getValueAt(i, 4));
                    bw.newLine();
                }
                
                bw.write("# Inventory\n");
                for (int i = 0; i < inventoryModel.getRowCount(); i++) {
                    bw.write(inventoryModel.getValueAt(i, 1) + "," + inventoryModel.getValueAt(i, 2));
                    bw.newLine();
                }
                
                JOptionPane.showMessageDialog(frame, "Данные успешно сохранены в TXT.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения данных в TXT: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveDataToXml() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML Files", "xml"));
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xml")) {
                file = new File(file.getAbsolutePath() + ".xml");
            }
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.newDocument();
    
                Element rootElement = doc.createElement("MusicShop");
                doc.appendChild(rootElement);
    
                Element albumsElement = doc.createElement("Albums");
                rootElement.appendChild(albumsElement);
    
                for (int i = 0; i < albumsModel.getRowCount(); i++) {
                    Element albumElement = doc.createElement("Album");
    
                    Element nameElement = doc.createElement("Name");
                    nameElement.appendChild(doc.createTextNode((String) albumsModel.getValueAt(i, 1)));
                    albumElement.appendChild(nameElement);
    
                    Element artistElement = doc.createElement("Artist");
                    artistElement.appendChild(doc.createTextNode((String) albumsModel.getValueAt(i, 2)));
                    albumElement.appendChild(artistElement);
    
                    Element salesElement = doc.createElement("ExpectedSales");
                    salesElement.appendChild(doc.createTextNode(String.valueOf(albumsModel.getValueAt(i, 3))));
                    albumElement.appendChild(salesElement);
    
                    Element availableElement = doc.createElement("Available");
                    availableElement.appendChild(doc.createTextNode((String) albumsModel.getValueAt(i, 4)));
                    albumElement.appendChild(availableElement);
    
                    albumsElement.appendChild(albumElement);
                }
    
                Element inventoryElement = doc.createElement("Inventory");
                rootElement.appendChild(inventoryElement);
    
                for (int i = 0; i < inventoryModel.getRowCount(); i++) {
                    Element inventoryItemElement = doc.createElement("InventoryItem");
    
                    Element albumNameElement = doc.createElement("AlbumName");
                    albumNameElement.appendChild(doc.createTextNode((String) inventoryModel.getValueAt(i, 1)));
                    inventoryItemElement.appendChild(albumNameElement);
    
                    Element quantityElement = doc.createElement("Quantity");
                    quantityElement.appendChild(doc.createTextNode(String.valueOf(inventoryModel.getValueAt(i, 2))));
                    inventoryItemElement.appendChild(quantityElement);
    
                    inventoryElement.appendChild(inventoryItemElement);
                }
    
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
    
                JOptionPane.showMessageDialog(frame, "Данные успешно сохранены в XML.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения данных в XML: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadDataFromTXT() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isInventory = false;
    
                albumsModel.setRowCount(0);
                inventoryModel.setRowCount(0);
                clearDatabase();
    
                while ((line = br.readLine()) != null) {
                    if (line.equals("# Albums")) {
                        isInventory = false;
                        continue;
                    } else if (line.equals("# Inventory")) {
                        isInventory = true;
                        continue;
                    }
    
                    String[] data = line.split(",");
                    if (!isInventory && data.length == 4) {
                        String name = data[0].trim();
                        String artist = data[1].trim();
                        int sales = Integer.parseInt(data[2].trim());
                        String available = data[3].trim();
    
                        albumsModel.addRow(new Object[]{null, name, artist, sales, available});
                        saveAlbumToDatabase(name, artist, sales, available);
                    } else if (isInventory && data.length == 2) {
                        String albumName = data[0].trim();
                        int quantity = Integer.parseInt(data[1].trim());
    
                        inventoryModel.addRow(new Object[]{null, albumName, quantity});
                        saveInventoryToDatabase(albumName, quantity);
                    }
                }
                JOptionPane.showMessageDialog(frame, "Данные успешно загружены из TXT.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка загрузки данных из TXT: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadDataFromXml() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML Files", "xml"));
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
    
                albumsModel.setRowCount(0);
                inventoryModel.setRowCount(0);
                clearDatabase();
    
                NodeList albumsList = doc.getElementsByTagName("Album");
                for (int i = 0; i < albumsList.getLength(); i++) {
                    Element albumElement = (Element) albumsList.item(i);
                    String name = albumElement.getElementsByTagName("Name").item(0).getTextContent();
                    String artist = albumElement.getElementsByTagName("Artist").item(0).getTextContent();
                    int sales = Integer.parseInt(albumElement.getElementsByTagName("ExpectedSales").item(0).getTextContent());
                    String available = albumElement.getElementsByTagName("Available").item(0).getTextContent();
    
                    albumsModel.addRow(new Object[]{null, name, artist, sales, available});
                    saveAlbumToDatabase(name, artist, sales, available);
                }
    
                NodeList inventoryList = doc.getElementsByTagName("InventoryItem");
                for (int i = 0; i < inventoryList.getLength(); i++) {
                    Element inventoryItemElement = (Element) inventoryList.item(i);
                    String albumName = inventoryItemElement.getElementsByTagName("AlbumName").item(0).getTextContent();
                    int quantity = Integer.parseInt(inventoryItemElement.getElementsByTagName("Quantity").item(0).getTextContent());
    
                    inventoryModel.addRow(new Object[]{null, albumName, quantity});
                    saveInventoryToDatabase(albumName, quantity);
                }
    
                JOptionPane.showMessageDialog(frame, "Данные успешно загружены из XML.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка загрузки данных из XML: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM Inventory");
            stmt.executeUpdate("DELETE FROM Albums");
        } catch (SQLException e) {
            System.err.println("Ошибка очистки базы данных: " + e.getMessage());
        }
    }
    
    private void saveAlbumToDatabase(String name, String artist, int sales, String available) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, ?)");) {
            ps.setString(1, name);
            ps.setString(2, artist);
            ps.setInt(3, sales);
            ps.setString(4, available);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения альбома в базу данных: " + e.getMessage());
        }
    }
    
    private void saveInventoryToDatabase(String albumName, int quantity) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Inventory (album_id, quantity) VALUES ((SELECT id FROM Albums WHERE name = ?), ?)");) {
            ps.setString(1, albumName);
            ps.setInt(2, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения записи инвентаря в базу данных: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicShop::new);
    }
}