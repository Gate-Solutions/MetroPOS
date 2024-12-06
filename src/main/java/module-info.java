module org.example.metropos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jooq;
    requires com.zaxxer.hikari;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires bcrypt;
    requires io.github.cdimascio.dotenv.java;


    opens org.gate.metropos to javafx.fxml;
    opens org.gate.metropos.Controllers to javafx.fxml;
    opens org.gate.metropos.Controllers.SuperAdminControllers to javafx.fxml;
    opens org.gate.metropos.Controllers.EmployeeControllers to javafx.fxml;
    opens org.gate.metropos.Controllers.BranchManagerControllers to javafx.fxml;
    opens org.gate.metropos.Controllers.Reports to javafx.fxml;

    opens org.gate.metropos.Controllers.DataEntryOperator to javafx.fxml;
    opens org.gate.metropos.models to org.jooq,javafx.base;
    opens org.gate.metropos.models.PurchaseInvoice to javafx.base;
    opens org.gate.metropos.enums to org.jooq;



    exports org.gate.metropos.Controllers to javafx.fxml;
    exports org.gate.metropos.models to javafx.base;
    exports org.gate.metropos.models.PurchaseInvoice to javafx.base;
    exports org.gate.metropos.Controllers.SuperAdminControllers to javafx.fxml;
    exports org.gate.metropos.Controllers.EmployeeControllers to javafx.fxml;
    exports org.gate.metropos.Controllers.BranchManagerControllers to javafx.fxml;
    exports org.gate.metropos.Controllers.DataEntryOperator to javafx.fxml;
    exports org.gate.metropos.Controllers.Reports to javafx.fxml;

    exports org.gate.metropos;
}