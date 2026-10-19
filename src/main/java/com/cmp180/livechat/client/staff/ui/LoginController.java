package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.staff.model.CustomerItem;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import com.cmp180.livechat.client.ui.ThemeManager;
import com.cmp180.livechat.client.ui.ServerConfig;
import com.cmp180.livechat.client.ui.SettingsDialog;

public class LoginController implements StaffNetworkListener {

    @FXML private AnchorPane rootPane;
    @FXML private VBox cardPane;
    @FXML private Label lblTitle;
    @FXML private Label lblForgot;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private Button btnSettings;
    @FXML private SVGPath svgSettings;
    private PauseTransition loginTimeout;

    @FXML
    public void initialize() {
        if (txtUsername != null) {
            txtUsername.textProperty().addListener((obs, oldV, newV) -> {
                if (lblError != null) lblError.setText("");
            });
            txtUsername.setOnAction(e -> {
                if (txtPassword != null) txtPassword.requestFocus();
            });
        }
        if (txtPassword != null) {
            txtPassword.textProperty().addListener((obs, oldV, newV) -> {
                if (lblError != null) lblError.setText("");
            });
            txtPassword.setOnAction(this::handleLogin);
        }

        Platform.runLater(this::applyCurrentTheme);

        ThemeManager.addListener(() -> {
            Platform.runLater(this::applyCurrentTheme);
        });
    }

    private void applyCurrentTheme() {
        boolean dark = ThemeManager.isDarkMode();
        String brandColor = ThemeManager.getPrimaryColor();
        String bgTint = dark ? "#141414" : ThemeManager.getBgTint(brandColor);
        String cardBg = dark ? "#1e1e1e" : "#ffffff";
        String cardBorder = dark ? "-fx-border-color: #333538; -fx-border-radius: 15;" : "-fx-border-color: transparent;";
        String cardShadow = dark ? "rgba(0,0,0,0.5), 18, 0, 0, 6" : "rgba(0,0,0,0.1), 20, 0, 0, 10";
        String textMain = dark ? "#e4e6eb" : "#222222";
        String textSub = dark ? "#a0a3a8" : "#888888";
        String inputBg = dark ? "#2b2c2e" : "#ffffff";
        String inputBorder = dark ? "#3e4042" : "#e0e0e0";
        Color settingsColor = dark ? Color.web("#e4e6eb") : Color.web("#555555");

        if (rootPane != null) {
            if (rootPane.getScene() != null) {
                ThemeManager.applyTheme(rootPane.getScene());
            }
            rootPane.setStyle("-fx-background-color: " + bgTint + ";");
        }

        if (cardPane != null) {
            cardPane.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, " + cardShadow + "); -fx-padding: 25; " + cardBorder);
        }

        if (lblTitle != null) {
            lblTitle.setStyle("-fx-text-fill: " + brandColor + ";");
        }

        if (lblForgot != null) {
            lblForgot.setStyle("-fx-text-fill: " + textSub + "; -fx-cursor: hand;");
        }

        if (btnLogin != null) {
            btnLogin.setStyle("-fx-background-color: " + brandColor + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        }

        if (svgSettings != null) {
            svgSettings.setStroke(settingsColor);
        }

        TextField[] fields = { txtUsername, txtPassword };
        for (TextField f : fields) {
            if (f != null) {
                f.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textMain + "; -fx-border-color: " + inputBorder + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 15; -fx-font-size: 14;");
            }
        }
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
        String password = txtPassword.getText().trim();
        
        if (username.isEmpty()) {
            lblError.setText("Vui lòng nhập tên đăng nhập!");
            return;
        }

        if (password.isEmpty()) {
            lblError.setText("Vui lòng nhập mật khẩu!");
            return;
        }

        lblError.setText("Đang xác thực tài khoản...");
        btnLogin.setDisable(true);

        if (loginTimeout != null) {
            loginTimeout.stop();
        }
        loginTimeout = new PauseTransition(Duration.seconds(8));
        loginTimeout.setOnFinished(e -> {
            if (btnLogin != null && btnLogin.isDisabled()) {
                StaffContext.getInstance().getNetworkService().removeListener(this);
                lblError.setText("Không nhận được phản hồi từ máy chủ (Timeout). Vui lòng thử lại!");
                btnLogin.setDisable(false);
            }
        });
        loginTimeout.play();

        String host = ServerConfig.getHost();
        int port = ServerConfig.getPort();

        // Lắng nghe phản hồi từ máy chủ
        StaffContext.getInstance().getNetworkService().addListener(this);
        StaffContext.getInstance().setStaffName(username);
        StaffContext.getInstance().getNetworkService().connectAndLogin(host, port, username, password);
    }

    @Override
    public void onLoginSuccess() {
        if (loginTimeout != null) {
            loginTimeout.stop();
        }
        StaffContext.getInstance().getNetworkService().removeListener(this);
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("LCSChat.fxml"));
                Parent root = loader.load();
                
                boolean wasMaximized = stage.isMaximized();
                boolean wasFullScreen = stage.isFullScreen();
                Scene scene = new Scene(root);
                ThemeManager.applyTheme(scene);
                stage.setScene(scene);
                stage.setTitle("LiveChat - " + StaffContext.getInstance().getStaffName());
                if (wasFullScreen) {
                    stage.setFullScreen(true);
                } else if (wasMaximized) {
                    stage.setMaximized(true);
                    Platform.runLater(() -> stage.setMaximized(true));
                } else {
                    stage.centerOnScreen();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                lblError.setText("Lỗi tải giao diện: " + e.getMessage());
                btnLogin.setDisable(false);
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        if (loginTimeout != null) {
            loginTimeout.stop();
        }
        StaffContext.getInstance().getNetworkService().removeListener(this);
        Platform.runLater(() -> {
            lblError.setText(errorMessage);
            btnLogin.setDisable(false);
        });
    }

    @Override public void onQueueUpdated(List<CustomerItem> waitingCustomers) {}
    @Override public void onPairedWithCustomer(String customerName, String customerId) {}
    @Override public void onMessageReceived(String senderName, String senderId, String message) {}
    @Override public void onSessionEnded(String reason) {}
}
