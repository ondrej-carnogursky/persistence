package sk.tuke.mp.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sqlite.SQLiteConnection;
import sk.tuke.mp.persistence.PersistenceManager;
import sk.tuke.mp.persistence.ReflectivePersistenceManager;
import sk.tuke.mp.test.fixtures.Category;
import sk.tuke.mp.test.fixtures.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class ReflectivePersistenceManagerTest {

    private PersistenceManager manager;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        connection.setAutoCommit(true);
        manager = new ReflectivePersistenceManager(connection);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public  void Tests() throws Exception {
        createTables();
        save();
        getAll();
        get();
        getBy();
    }

    //@Test
    public void createTables() { // throws SQLException {
        try {
            manager.createTables(Product.class, Category.class);
            // Change in schema happened
            boolean productCreated = false;
            boolean categoryCreated = false;
            ResultSet rs = connection.getMetaData().getTables(null, null, "%", null);
            while(rs.next()) {
                if(!rs.getString(4).equalsIgnoreCase("TABLE")) {
                    continue;
                }
                String tableName = rs.getString(3);
                if(tableName.equals("Product")) {
                    productCreated = true;
                }

                if(tableName.equals("Category")) {
                    categoryCreated = true;
                }
            }
            assertTrue(productCreated);
            assertTrue(categoryCreated);
        } catch (Exception e) {
            // Without errors
            assertNull(e);
        }
    }

    //@Test
    public void getAll() {
        assertEquals(2, manager.getAll(Category.class).size());
        assertEquals(1, manager.getAll(Product.class).size());
    }

    //@Test
    public void get() {
        assertNotNull(manager.get(Category.class, 1));
        assertEquals("Second Category", manager.get(Product.class, 0).getCategory().getTitle());
    }

    //@Test
    public void getBy() {
        List<Category> cats = manager.getBy(Category.class, "title", "New First Category");
        assertEquals(1, cats.size());
        assertEquals(0, cats.get(0).getId());
        List<Product> prods =  manager.getBy(Product.class, "name", "Product 1");
        assertEquals(1, prods.size());
        assertEquals("Product 1", prods.get(0).getName());
        assertEquals("Second Category", prods.get(0).getCategory().getTitle());
    }

    //@Test
    public void save() {
        try {
            Category cat = new Category();
            cat.setId(0);
            cat.setTitle("First Category");
            assertEquals(1, manager.save(cat)); // INSERT
            cat.setTitle("New First Category");
            assertEquals(1, manager.save(cat)); // UPDATE
            Product prod = new Product();
            prod.setId(0);
            prod.setName("Product 1");
            prod.setCategory(cat);
            assertEquals(1, manager.save(prod)); // INSERT with FOREIGN KEY referencing existing FOREIGN TABLE entry
            cat = new Category();
            cat.setId(1);
            cat.setTitle("Second Category");
            // assertEquals(1, manager.save(cat));
            prod.setCategory(cat);
            assertEquals(1, manager.save(prod)); // UPDATE with FOREIGN KEY referencing new FOREIGN TABLE entry
        } catch (Exception e) {
            // Without errors
            assertNull(e);
        }
    }
}