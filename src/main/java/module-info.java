module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jooq;
    requires com.zaxxer.hikari;
    requires static lombok;


    opens org.example.metropos to javafx.fxml;
    exports org.example.metropos;
}