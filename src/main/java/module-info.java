module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jooq;
    requires com.zaxxer.hikari;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires bcrypt;


    opens org.gate.metropos to javafx.fxml;
    opens org.gate.metropos.Controllers to javafx.fxml;
    exports org.gate.metropos.Controllers to javafx.fxml;
    exports org.gate.metropos;
}