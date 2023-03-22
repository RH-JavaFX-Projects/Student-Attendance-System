package lk.ijse.dep10.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lk.ijse.dep10.app.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (DBConnection.getInstance().getConnection() != null &&
                        !DBConnection.getInstance().getConnection().isClosed()) {
                    DBConnection.getInstance().getConnection().close();
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        generateTablesIfNotExist();
        try {
            primaryStage.setScene(new Scene(new FXMLLoader(getClass().getResource("/view/MainForm.fxml")).load()));
            primaryStage.show();
//            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
            primaryStage.setTitle("Student Attendance System");
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to Load the App").showAndWait();
            System.exit(1);
        }

    }

    private void generateTablesIfNotExist() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES");

            HashSet<String> tableNameSet = new HashSet<>();
            while (rst.next()) {
                tableNameSet.add(rst.getString(1));
            }

            boolean tableExists = tableNameSet.
                    containsAll(Set.of("Attendance", "Picture", "Student", "User"));

            if (!tableExists) {
                InputStream resourceAsStream = getClass().getResourceAsStream("/schema.sql");
                if (resourceAsStream != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));

                    String line;
                    StringBuilder dbScript = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        dbScript.append(line).append("\n");
                    }
                    br.close();
                    stm.execute(dbScript.toString());
                }
            }


        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to read schema script").showAndWait();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to generate tables").showAndWait();
            throw new RuntimeException(e);
        }
    }
}
