module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.metropos to javafx.fxml;
    exports org.example.metropos;
}