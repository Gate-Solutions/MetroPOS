module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jooq;
    requires com.zaxxer.hikari;


    opens org.example.metropos to javafx.fxml;
    exports org.example.metropos;
}