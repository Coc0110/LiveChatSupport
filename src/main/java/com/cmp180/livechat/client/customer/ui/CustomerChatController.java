package com.cmp180.livechat.client.customer.ui;

import com.cmp180.livechat.client.customer.context.CustomerContext;
import com.cmp180.livechat.client.customer.network.CustomerNetworkListener;
import com.cmp180.livechat.client.customer.model.ChatMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;


public class CustomerChatController implements CustomerNetworkListener {

    @FXML private ListView<ChatMessage> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Label lblTitle;
    @FXML private Label lblStatus;

    private String currentStaffId = null;

    @FXML private Button btnAccount;
    @FXML private Button btnSettings;
    private ContextMenu accountMenu;
    private ContextMenu settingsMenu;


    @FXML
    public void initialize() {
        CustomerContext.getInstance().getNetworkService().addListener(this);

        // --- Account Menu Setup ---
        accountMenu = new ContextMenu();
        MenuItem itemProfile = new MenuItem("Hồ sơ của bạn");
        MenuItem itemAccSettings = new MenuItem("Cài đặt");
        MenuItem itemLogout = new MenuItem("Đăng xuất");
        itemLogout.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
        accountMenu.getItems().addAll(itemProfile, itemAccSettings, new SeparatorMenuItem(), itemLogout);

        // --- Settings Menu Setup ---
        settingsMenu = new ContextMenu();
        MenuItem itemExit = new MenuItem("Thoát ứng dụng");
        itemExit.setOnAction(e -> System.exit(0));

        CustomMenuItem themeItem = new CustomMenuItem();
        themeItem.setHideOnClick(false);
        themeItem.getStyleClass().add("menu-item");
        HBox themeBox = new HBox(10);
        themeBox.setPrefWidth(180);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTheme = new Label("Giao diện sáng/tối");
        lblTheme.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane themeSwitch = new StackPane();
        themeSwitch.setCursor(Cursor.HAND);
        themeSwitch.setScaleX(1.3);
        themeSwitch.setScaleY(1.3);
        SVGPath switchBg = new SVGPath();
        switchBg.setContent("M16 18H8C4.68629 18 2 15.3137 2 12C2 8.68629 4.68629 6 8 6H16C19.3137 6 22 8.68629 22 12C22 15.3137 19.3137 18 16 18Z");
        switchBg.setStroke(Color.GRAY);
        switchBg.setStrokeWidth(2);
        switchBg.setFill(Color.TRANSPARENT);

        SVGPath switchIcon = new SVGPath();
        switchIcon.setContent("M12 4V2M12 20V22M6.41421 6.41421L5 5M17.728 17.728L19.1422 19.1422M4 12H2M20 12H22M17.7285 6.41421L19.1427 5M6.4147 17.728L5.00049 19.1422M12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12C17 14.7614 14.7614 17 12 17Z");
        switchIcon.setStroke(Color.GRAY);
        switchIcon.setStrokeWidth(2);
        switchIcon.setFill(Color.TRANSPARENT);
        switchIcon.setScaleX(0.5);
        switchIcon.setScaleY(0.5);
        switchIcon.setTranslateX(-4);

        themeSwitch.getChildren().addAll(switchBg, switchIcon);
        themeSwitch.setOnMouseClicked(e -> {
            boolean isDark = switchIcon.getTranslateX() > 0;
            if (isDark) {
                switchIcon.setTranslateX(-4);
                switchIcon.setContent("M12 4V2M12 20V22M6.41421 6.41421L5 5M17.728 17.728L19.1422 19.1422M4 12H2M20 12H22M17.7285 6.41421L19.1427 5M6.4147 17.728L5.00049 19.1422M12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12C17 14.7614 14.7614 17 12 17Z");
                if (btnAccount != null && btnAccount.getScene() != null) {
                    btnAccount.getScene().getRoot().getStyleClass().remove("dark-mode");
                    accountMenu.getStyleClass().remove("dark-mode");
                    settingsMenu.getStyleClass().remove("dark-mode");
                }
            } else {
                switchIcon.setTranslateX(4);
                switchIcon.setContent("M18 15C13.0294 15 9 10.9706 9 6C9 5.09074 9.13484 4.21311 9.38561 3.38574C5.69007 4.50583 3 7.93883 3 12.0001C3 16.9707 7.02944 20.9999 12 20.9999C16.0613 20.9999 19.4943 18.3103 20.6144 14.6147C19.787 14.8655 18.9093 15 18 15Z");
                if (btnAccount != null && btnAccount.getScene() != null) {
                    btnAccount.getScene().getRoot().getStyleClass().add("dark-mode");
                    accountMenu.getStyleClass().add("dark-mode");
                    settingsMenu.getStyleClass().add("dark-mode");
                }
            }
        });
        themeBox.getChildren().addAll(lblTheme, spacer, themeSwitch);
        themeItem.setContent(themeBox);

        settingsMenu.getItems().addAll(themeItem, new SeparatorMenuItem(), itemExit);

        
        listMessages.setCellFactory(param -> new MessageListCell());
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String content = txtMessage.getText().trim();
        if (!content.isEmpty() && currentStaffId != null) {
            CustomerContext.getInstance().getNetworkService().sendChatMessage(content, currentStaffId);
            ChatMessage msg = new ChatMessage(content, ChatMessage.Type.MY_MSG, "Bạn");
            listMessages.getItems().add(msg);
            listMessages.scrollTo(listMessages.getItems().size() - 1);
            txtMessage.clear();
        }
    }

    @Override
    public void onConnectedToQueue() {
        Platform.runLater(() -> {
            lblStatus.setText("Đang chờ nhân viên kết nối...");
            listMessages.getItems().add(new ChatMessage("Bạn đang ở trong hàng đợi. Vui lòng chờ...", ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
        });
    }

    @Override
    public void onPairedWithStaff(String staffName, String staffId) {
        Platform.runLater(() -> {
            currentStaffId = staffId;
            lblTitle.setText("Hỗ trợ viên: " + staffName);
            lblStatus.setText("Đã kết nối thành công");
            txtMessage.setDisable(false);
            btnSend.setDisable(false);
            listMessages.getItems().add(new ChatMessage("Bạn đang chat với " + staffName, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
        });
    }

    @Override
    public void onMessageReceived(String senderName, String message) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(message, ChatMessage.Type.STAFF_MSG, senderName);
            listMessages.getItems().add(msg);
            listMessages.scrollTo(listMessages.getItems().size() - 1);
        });
    }

    @Override
    public void onSessionEnded(String reason) {
        Platform.runLater(() -> {
            listMessages.getItems().add(new ChatMessage(reason, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
            txtMessage.setDisable(true);
            btnSend.setDisable(true);
            lblStatus.setText("Phiên đã kết thúc");
            currentStaffId = null;
        });
    }

    @Override
    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            lblStatus.setText("Lỗi: " + errorMessage);
        });
    }
    

    @FXML
    private void showAccountMenu(MouseEvent event) {
        if (btnAccount != null) {
            accountMenu.show(btnAccount, Side.RIGHT, 10, 0);
        }
    }

    @FXML
    private void showSettingsMenu(MouseEvent event) {
        if (btnSettings != null) {
            settingsMenu.show(btnSettings, Side.RIGHT, 10, 0);
        }
    }

    // --- Custom Cell ---
    private class MessageListCell extends ListCell<ChatMessage> {
        private HBox root = new HBox();
        private Label lblText = new Label();
        private Label lblTime = new Label();
        private Label lblStatus = new Label();
        private VBox messageAndStatus = new VBox(2);
        private HBox timeAndBubble = new HBox(5);
        
        public MessageListCell() {
            lblText.setWrapText(true);
            lblText.setMaxWidth(350);
            lblText.setPadding(new Insets(10, 15, 10, 15));
            
            lblTime.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
            lblStatus.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
        }
        
        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                root.getChildren().clear();
                messageAndStatus.getChildren().clear();
                timeAndBubble.getChildren().clear();
                
                lblText.setText(item.getText());
                lblTime.setText(item.getFormattedTime());
                
                if (item.getType() == ChatMessage.Type.MY_MSG) {
                    lblText.setStyle("-fx-background-color: #0084FF; -fx-text-fill: white; -fx-background-radius: 15 15 0 15; -fx-font-size: 14px;");
                    timeAndBubble.setAlignment(Pos.BOTTOM_RIGHT);
                    timeAndBubble.getChildren().addAll(lblTime, lblText);
                    
                    messageAndStatus.setAlignment(Pos.TOP_RIGHT);
                    messageAndStatus.getChildren().add(timeAndBubble);
                    
                    boolean isLast = (getIndex() == getListView().getItems().size() - 1);
                    if (isLast) {
                        lblStatus.setText("Đã nhận");
                        messageAndStatus.getChildren().add(lblStatus);
                    }
                    
                    root.setAlignment(Pos.CENTER_RIGHT);
                    root.getChildren().add(messageAndStatus);
                } else if (item.getType() == ChatMessage.Type.SYSTEM_MSG) {
                    lblText.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 12px; -fx-font-style: italic;");
                    root.setAlignment(Pos.CENTER);
                    root.getChildren().add(lblText);
                } else {
                    lblText.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 15 15 15 0; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                    timeAndBubble.setAlignment(Pos.BOTTOM_LEFT);
                    timeAndBubble.getChildren().addAll(lblText, lblTime);
                    
                    root.setAlignment(Pos.CENTER_LEFT);
                    root.getChildren().add(timeAndBubble);
                }
                
                setGraphic(root);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
            }
        }
    }
}
