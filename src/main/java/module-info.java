module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires com.zaxxer.hikari;
    requires org.jooq;


    opens org.example.metropos to javafx.fxml;
    exports org.example.metropos;
    exports org.example.metropos.Controllers;
    opens org.example.metropos.Controllers to javafx.fxml;
    exports org.example.metropos.Controllers.SuperAdminControllers;
    opens org.example.metropos.Controllers.SuperAdminControllers to javafx.fxml;
}