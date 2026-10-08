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

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    @FXML
    private Button btnLogin;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        
        if (username.isEmpty()) {
            lblError.setText("Vui lòng nhập mã nhân viên!");
            return;
        }

        // Logic kết nối Server
        StaffContext.getInstance().setStaffName(username);
        StaffContext.getInstance().getNetworkService().connectAndLogin(username);

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
