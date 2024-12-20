import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.sql.*;

public class MusicShopTests {
    
    private Connection connection;
    
    // Этот метод выполняется до каждого теста, создавая новое подключение к базе данных
    @Before
    public void setUp() throws SQLException {
        // Используем in-memory базу данных для тестирования, так каждый тест будет изолирован
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        Statement statement = connection.createStatement();
        
        // Создание таблиц для тестирования
        statement.execute("CREATE TABLE IF NOT EXISTS Albums (id INTEGER PRIMARY KEY, name TEXT, artist TEXT, expected_sales INTEGER, available TEXT)");
        statement.execute("CREATE TABLE IF NOT EXISTS Inventory (id INTEGER PRIMARY KEY, album_id INTEGER, quantity INTEGER, FOREIGN KEY(album_id) REFERENCES Albums(id))");
    }
    
    // Закрытие подключения после каждого теста
    @After
    public void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
    
    @Test
    public void testAddAlbum() throws SQLException {
        // Добавляем альбом
        String name = "Album 1";
        String artist = "Artist 1";
        int expectedSales = 1000;
        
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, ?)");
        ps.setString(1, name);
        ps.setString(2, artist);
        ps.setInt(3, expectedSales);
        ps.setString(4, "Нет");
        ps.executeUpdate();
        
        // Проверяем, что альбом добавлен
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Albums WHERE name = 'Album 1' AND artist = 'Artist 1'");
        
        if (rs.next()) {
            assertEquals(1, rs.getInt(1));  // Должна быть одна запись
        }
    }
    
    @Test
    public void testUpdateAlbum() throws SQLException {
        // Добавляем альбом в базу данных
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "Album 2");
        ps.setString(2, "Artist 2");
        ps.setInt(3, 1500);
        ps.setString(4, "Нет");
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        int albumId = -1;
        if (rs.next()) {
            albumId = rs.getInt(1);
        }
        
        // Обновляем название альбома
        String newName = "Updated Album 2";
        PreparedStatement updatePs = connection.prepareStatement("UPDATE Albums SET name = ? WHERE id = ?");
        updatePs.setString(1, newName);
        updatePs.setInt(2, albumId);
        updatePs.executeUpdate();
        
        // Проверяем, что альбом был обновлен
        PreparedStatement selectPs = connection.prepareStatement("SELECT name FROM Albums WHERE id = ?");
        selectPs.setInt(1, albumId);
        ResultSet updatedRs = selectPs.executeQuery();
        
        if (updatedRs.next()) {
            assertEquals(newName, updatedRs.getString("name"));  // Проверка, что имя изменилось
        }
    }
    
    @Test
    public void testDeleteAlbum() throws SQLException {
        // Добавляем альбом в базу
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "Album 3");
        ps.setString(2, "Artist 3");
        ps.setInt(3, 2000);
        ps.setString(4, "Нет");
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        int albumId = -1;
        if (rs.next()) {
            albumId = rs.getInt(1);
        }
        
        // Удаляем альбом
        PreparedStatement deletePs = connection.prepareStatement("DELETE FROM Albums WHERE id = ?");
        deletePs.setInt(1, albumId);
        deletePs.executeUpdate();
        
        // Проверяем, что альбом удалён
        PreparedStatement checkPs = connection.prepareStatement("SELECT COUNT(*) FROM Albums WHERE id = ?");
        checkPs.setInt(1, albumId);
        ResultSet checkRs = checkPs.executeQuery();
        
        if (checkRs.next()) {
            assertEquals(0, checkRs.getInt(1));  // Альбом должен быть удален
        }
    }
    
    @Test
    public void testAddInventory() throws SQLException {
        // Добавление альбома
        String albumName = "Album 4";
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, albumName);
        ps.setString(2, "Artist 4");
        ps.setInt(3, 500);
        ps.setString(4, "Нет");
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        int albumId = -1;
        if (rs.next()) {
            albumId = rs.getInt(1);
        }
        
        // Добавление записи в инвентарь
        PreparedStatement inventoryPs = connection.prepareStatement("INSERT INTO Inventory (album_id, quantity) VALUES (?, ?)");
        inventoryPs.setInt(1, albumId);
        inventoryPs.setInt(2, 50);  // 50 экземпляров
        inventoryPs.executeUpdate();
        
        // Проверяем, что количество инвентаря обновилось
        PreparedStatement inventoryCheckPs = connection.prepareStatement("SELECT quantity FROM Inventory WHERE album_id = ?");
        inventoryCheckPs.setInt(1, albumId);
        ResultSet inventoryRs = inventoryCheckPs.executeQuery();
        
        if (inventoryRs.next()) {
            assertEquals(50, inventoryRs.getInt("quantity"));
        }
    }
    
    @Test
    public void testDeleteInventory() throws SQLException {
        // Добавление альбома и инвентаря
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Albums (name, artist, expected_sales, available) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "Album 5");
        ps.setString(2, "Artist 5");
        ps.setInt(3, 500);
        ps.setString(4, "Нет");
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        int albumId = -1;
        if (rs.next()) {
            albumId = rs.getInt(1);
        }
        
        PreparedStatement inventoryPs = connection.prepareStatement("INSERT INTO Inventory (album_id, quantity) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        inventoryPs.setInt(1, albumId);
        inventoryPs.setInt(2, 20);
        inventoryPs.executeUpdate();
        
        // Удаляем запись из инвентаря
        PreparedStatement deletePs = connection.prepareStatement("DELETE FROM Inventory WHERE album_id = ?");
        deletePs.setInt(1, albumId);
        deletePs.executeUpdate();
        
        // Проверяем, что инвентарь пуст
        PreparedStatement inventoryCheckPs = connection.prepareStatement("SELECT COUNT(*) FROM Inventory WHERE album_id = ?");
        inventoryCheckPs.setInt(1, albumId);
        ResultSet inventoryCheckRs = inventoryCheckPs.executeQuery();
        
        if (inventoryCheckRs.next()) {
            assertEquals(0, inventoryCheckRs.getInt(1));  // Инвентарь должен быть удален
        }
    }
}
