package lk.ijse.dep10.app.controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.db.DBConnection;
import lk.ijse.dep10.app.model.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class MainFormController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPicture;

    @FXML
    private TableView<Student> tblDetails;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearch;

    public void initialize() {
        btnNewStudent.fire();
        tblDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("imageView"));
        tblDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        loadAllStudents();
        Platform.runLater(btnNewStudent::fire);

        tblDetails.getSelectionModel().selectedItemProperty().addListener((observableValue, student, t1) -> {
            if (t1 != null) {
                btnDelete.setDisable(false);
                txtId.setText(t1.getId());
                txtName.setText(t1.getName());
                Blob picture = t1.getPicture();
                if (picture != null) {
                    try {
                        imgPicture.setImage(new Image(picture.getBinaryStream()));
                        btnClear.setDisable(false);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else btnClear.fire();

            } else btnDelete.setDisable(true);
        });
        txtSearch.textProperty().addListener((observableValue, s, t1) -> {
            if (t1.isEmpty()) {
                tblDetails.getItems().clear();
                loadAllStudents();
                btnNewStudent.fire();
            } else {
                tblDetails.getSelectionModel().clearSelection();
                Connection connection = DBConnection.getInstance().getConnection();
                try {
                    String sql = "SELECT * FROM  Student WHERE id LIKE ? or name LIKE ? ";
                    var preparedStatement = connection.prepareStatement("SELECT * FROM Picture WHERE student_id= ?");
                    PreparedStatement stm = connection.prepareStatement(sql);
                    stm.setString(1, t1);
                    stm.setString(2, t1);
                    var resultSet = stm.executeQuery();
                    while (resultSet.next()) {
                        var id = resultSet.getString("id");
                        var name = resultSet.getString("name");
                        Blob picture = getBlob(new Image("/images/avatar.png", 50, 50, true, true));
                        ImageView imageView = new ImageView(new Image(picture.getBinaryStream()));
                        preparedStatement.setString(1, id);
                        var rst = preparedStatement.executeQuery();
                        if (rst.next()) {
                            picture = rst.getBlob("picture");
                            imageView.setImage(new Image(picture.getBinaryStream(), 50, 50, true, true));
                        }


                        tblDetails.getItems().clear();
                        tblDetails.getItems().add((new Student(id, name, picture, imageView)));
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Blob getBlob(Image image) {
        var bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", bos);
            byte[] bytes = bos.toByteArray();
            Blob blob = new SerialBlob(bytes);
            return blob;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SerialException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadAllStudents() {
        try {
            var connection = DBConnection.getInstance().getConnection();
            var preparedStatement = connection.prepareStatement("SELECT * FROM Picture WHERE student_id= ?");
            Statement stm = connection.createStatement();
            var rst = stm.executeQuery("SELECT * FROM Student");
            while (rst.next()) {
                String id = rst.getString("id");
                String name = rst.getString("name");
                Blob picture = getBlob(new Image("/images/avatar.png", 50, 50, true, true));
                ImageView imageView = new ImageView(new Image(picture.getBinaryStream()));
                preparedStatement.setString(1, id);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    picture = resultSet.getBlob("picture");
                    imageView.setImage(new Image(picture.getBinaryStream(), 50, 50, true, true));
                }

                Student student = new Student(id, name, picture, imageView);
                tblDetails.getItems().add(student);


            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load students,Try again!");
            System.exit(1);
        }
    }

    @FXML
    void btlDetailsOnKeyReleased(KeyEvent event) {
        if (event.getCode().equals(KeyCode.DELETE)) btnDelete.fire();

    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the Student Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        File file = fileChooser.showOpenDialog(btnSave.getScene().getWindow());
        if (file != null) {
            Image image = new Image(file.toURI().toString(), 250, 250, true, true);
            imgPicture.setImage(image);
            btnClear.setDisable(false);
        }


    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        imgPicture.setImage(new Image("/images/avatar.png"));
        btnClear.setDisable(true);
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        var connection = DBConnection.getInstance().getConnection();
        try {
            if (!btnClear.isDisable()) {
                connection.setAutoCommit(false);
                PreparedStatement stm = connection.prepareStatement("DELETE  FROM Picture WHERE student_id=?");
                stm.setString(1, txtId.getText());
                stm.executeUpdate();
            }
            PreparedStatement stm2 = connection.prepareStatement("DELETE  FROM Student WHERE id=?");
            stm2.setString(1, txtId.getText());
            stm2.executeUpdate();

            connection.commit();
            tblDetails.getItems().remove(tblDetails.getSelectionModel().getSelectedItem());
            tblDetails.getSelectionModel().clearSelection();
            btnNewStudent.fire();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to Remove the Student, Try Again!");
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }


    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        txtName.getStyleClass().remove("invalid");
        txtId.clear();
        txtName.requestFocus();
        txtName.clear();
        btnClear.fire();
        tblDetails.getSelectionModel().clearSelection();
        generateId();


    }

    private void generateId() {
        if (tblDetails.getItems().size() == 0) txtId.setText("DEP-10/S-001");
        else {
            int nextId = Integer.parseInt(tblDetails.getItems().get(tblDetails.getItems().size() - 1).getId().substring(9)) + 1;
            String id = String.format("DEP-10/S-%03d", nextId);
            txtId.setText(id);
        }

    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        boolean isValid = true;
        if (txtName.getText().isEmpty() || !txtName.getText().matches("[A-Za-z ]{2,}")) {
            isValid = false;
            txtName.selectAll();
            txtName.requestFocus();
            txtName.getStyleClass().add("invalid");
        }
        if (!isValid) return;
        var connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement stm = connection.prepareStatement("INSERT INTO Student(id, name) VALUES (?,?)");
            stm.setString(1, txtId.getText());
            stm.setString(2, txtName.getText());
            stm.executeUpdate();
            Student student = new Student(txtId.getText(), txtName.getText().strip(), null, new ImageView(new Image("/images/avatar.png", 50, 50, true, true)));

            if (!btnClear.isDisable()) {
                String sql2 = "INSERT INTO Picture (student_id, picture) VALUES (?,?)";
                PreparedStatement stm2 = connection.prepareStatement(sql2);
                stm2.setString(1, txtId.getText());

                var image = imgPicture.getImage();
                var bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", bos);
                byte[] bytes = bos.toByteArray();
                Blob blob = new SerialBlob(bytes);


                stm2.setBlob(2, blob);
                stm2.executeUpdate();
                student.setPicture(blob);
                student.setImageView(new ImageView(new Image(blob.getBinaryStream(), 50, 50, true, true)));


            }
            connection.commit();
            tblDetails.getItems().add(student);
            btnNewStudent.fire();

        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to Save Student,Try Again");
        } finally {
            try {
                DBConnection.getInstance().getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
