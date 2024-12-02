module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jooq;
    requires com.zaxxer.hikari;
    requires static lombok;
    requires com.fasterxml.jackson.databind;


    opens org.example.metropos to javafx.fxml;
    opens org.example.metropos.Controllers to javafx.fxml;
    exports org.example.metropos.Controllers to javafx.fxml;
    exports org.example.metropos;
}