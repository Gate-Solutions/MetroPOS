module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires com.zaxxer.hikari;
    requires org.jooq;


    opens org.gate.metropos to javafx.fxml;
    exports org.gate.metropos;
}