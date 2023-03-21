package lk.ijse.dep10.app.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final DBConnection dbconnection = new DBConnection();
    private final Connection connection;

    private DBConnection() {
        Properties configurations = new Properties();
        File file = new File("application.properties");
        try {
            FileReader fileReader = new FileReader(file);
            configurations.load(fileReader);
            fileReader.close();

            String host = configurations.getProperty("mysql.host", "localhost");
            String port = configurations.getProperty("mysql.port", "3306");
            String database = configurations.getProperty("mysql.database", "dep10_jdbc2");
            String username = configurations.getProperty("mysql.username", "root");
            String password = configurations.getProperty("mysql.password", "rasiya");


            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&allowMultiQueries=true";
            connection = DriverManager.getConnection(url, username, password);

        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Configuration file doesn't exist").showAndWait();
            throw new RuntimeException(e);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to read Configurations").showAndWait();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to the database connection, try again.If the problem persists please contact the technical team").showAndWait();
            throw new RuntimeException(e);
        }


    }

    public static DBConnection getInstance() {
        return dbconnection;
    }

    public Connection getConnection() {
        return connection;
    }
}
