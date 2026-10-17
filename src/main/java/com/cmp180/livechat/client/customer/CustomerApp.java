package com.cmp180.livechat.client.customer;

import com.cmp180.livechat.client.customer.context.CustomerContext;
import com.cmp180.livechat.client.ui.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CustomerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ui/CustomerLogin.fxml"));
        primaryStage.setTitle("LiveChat - Khách Hàng");
        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(e -> {
            CustomerContext.getInstance().getNetworkService().disconnect();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }
}
