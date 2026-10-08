package com.cmp180.livechat.client.customer.ui;

import com.cmp180.livechat.client.customer.context.CustomerContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.scene.control.ComboBox;

public class CustomerLoginController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbIssue;
    @FXML private Label lblError;
    @FXML private Button btnConnect;

    @FXML
    public void initialize() {
        cbIssue.getItems().addAll(
            "Tư vấn Mua hàng",
            "Hỗ trợ Kỹ thuật",
            "Bảo hành & Đổi trả",
            "Thắc mắc Chung"
        );
    }

    @FXML
    private void handleConnect(ActionEvent event) {
        String name = txtName.getText().trim();
        String issue = cbIssue.getValue();
        
        if (name.isEmpty()) {
            lblError.setText("Vui lòng nhập tên của bạn!");
            return;
        }
        if (issue == null) {
            lblError.setText("Vui lòng chọn vấn đề cần tư vấn!");
            return;
        }

        String combinedName = name + " (" + issue + ")";
        CustomerContext.getInstance().setCustomerName(combinedName);
        CustomerContext.getInstance().getNetworkService().connectAndLogin(combinedName);

        try {
            Stage stage = (Stage) btnConnect.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerChat.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("LiveChat Hỗ Trợ - " + name);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Lỗi tải giao diện: " + e.getMessage());
        }
    }
}
