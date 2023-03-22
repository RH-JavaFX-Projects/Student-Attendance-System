package lk.ijse.dep10.app.model;

import javafx.scene.image.ImageView;

import java.io.Serializable;
import java.sql.Blob;

public class Student implements Serializable {
    private String id;
    private String name;
    private Blob picture;
    private ImageView imageView;

    public Student(String id, String name, Blob picture, ImageView imageView) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.imageView = imageView;
    }

    public Student() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getPicture() {
        return picture;
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
