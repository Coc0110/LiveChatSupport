package com.cmp180.livechat.client.staff;

import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.ui.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StaffApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ui/LCSLogin.fxml"));
        primaryStage.setTitle("LiveChat Support - Login");
        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(e -> {
            StaffContext.getInstance().getNetworkService().disconnect();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
