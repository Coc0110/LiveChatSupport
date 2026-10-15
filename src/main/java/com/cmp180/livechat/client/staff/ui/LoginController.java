package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.staff.context.StaffContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.application.Platform;
import com.cmp180.livechat.client.ui.ThemeManager;
import com.cmp180.livechat.client.ui.ServerConfig;
import com.cmp180.livechat.client.ui.SettingsDialog;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private Button btnSettings;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (btnLogin != null && btnLogin.getScene() != null) {
                ThemeManager.applyTheme(btnLogin.getScene());
                btnLogin.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });
        ThemeManager.addListener(() -> {
            Platform.runLater(() -> {
                if (btnLogin != null && btnLogin.getScene() != null) {
                    ThemeManager.applyTheme(btnLogin.getScene());
                    btnLogin.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
                }
            });
        });
    }

    @FXML
    private void handleOpenSettings(ActionEvent event) {
        if (btnLogin != null && btnLogin.getScene() != null) {
            SettingsDialog.show(btnLogin.getScene().getWindow());
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        
        if (username.isEmpty()) {
            lblError.setText("Vui lòng nhập mã nhân viên!");
            return;
        }

        String host = ServerConfig.getHost();
        int port = ServerConfig.getPort();

        // Logic kết nối Server
        StaffContext.getInstance().setStaffName(username);
        StaffContext.getInstance().getNetworkService().connectAndLogin(host, port, username);

        // Chuyển sang màn hình Chat
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            // Load giao diện Chat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LCSChat.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("LiveChat - " + username);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (e.getCause() != null) {
                errorMsg = e.getCause().toString();
            }
            lblError.setText("Lỗi tải giao diện: " + errorMsg);
        }
    }
}
