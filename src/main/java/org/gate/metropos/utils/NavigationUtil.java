package org.gate.metropos.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {





    public static void showBranchManagerDashboard(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/org/gate/metropos/BranchManagerScreens/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            currentStage.setTitle("Metro POS | Branch Manager");
            Scene dashboardScene = new Scene(dashboardRoot);
            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load Dashboard",
                    "An error occurred while loading the Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showCashierScreen(Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/org/gate/metropos/CashierScreens/cashierDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the current stage from loginButton

            currentStage.setTitle("Metro POS | Cashier Dashboard");

            // Create new scene with dashboard
            Scene dashboardScene = new Scene(dashboardRoot);

            // Set the new scene on current stage
            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            AlertUtils.showError("Navigation Error", "An error occurred while loading the Cashier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void showDataEntryDashboard(Stage currentStage) {
        try {
            // Load the Data Entry Dashboard FXML
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/org/gate/metropos/DataEntryScreens/DataEntryDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get the current stage from loginButton

            currentStage.setTitle("Metro POS | Data Entry Dashboard");

            // Create new scene with dashboard
            Scene dashboardScene = new Scene(dashboardRoot);

            // Set the new scene on current stage
            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load Data Entry Dashboard");
            alert.setContentText("An error occurred while loading the Dashboard: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }


}
