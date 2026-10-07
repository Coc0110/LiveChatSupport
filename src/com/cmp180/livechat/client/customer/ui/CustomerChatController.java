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

public class CustomerChatController implements CustomerNetworkListener {

    @FXML private ListView<ChatMessage> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Label lblTitle;
    @FXML private Label lblStatus;

    private String currentStaffId = null;

    @FXML
    public void initialize() {
        CustomerContext.getInstance().getNetworkService().addListener(this);
        
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
    
    // --- Custom Cell ---
    private class MessageListCell extends ListCell<ChatMessage> {
        private HBox root = new HBox();
        private Label lblText = new Label();
        private Label lblTime = new Label();
        private Label lblStatus = new Label();
        private VBox metaBox = new VBox(2);
        
        public MessageListCell() {
            lblText.setWrapText(true);
            lblText.setMaxWidth(350);
            lblText.setPadding(new Insets(10, 15, 10, 15));
            
            lblTime.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
            lblStatus.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
            
            metaBox.getChildren().addAll(lblTime, lblStatus);
            metaBox.setAlignment(Pos.BOTTOM_RIGHT);
        }
        
        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                root.getChildren().clear();
                metaBox.getChildren().clear();
                
                lblText.setText(item.getText());
                lblTime.setText(item.getFormattedTime());
                lblStatus.setText(item.getStatus());
                
                if (item.getType() == ChatMessage.Type.MY_MSG) {
                    lblText.setStyle("-fx-background-color: #0084FF; -fx-text-fill: white; -fx-background-radius: 15 15 0 15; -fx-font-size: 14px;");
                    metaBox.getChildren().addAll(lblTime, lblStatus);
                    metaBox.setAlignment(Pos.BOTTOM_RIGHT);
                    root.setAlignment(Pos.CENTER_RIGHT);
                    root.getChildren().addAll(metaBox, lblText);
                    HBox.setMargin(metaBox, new Insets(0, 5, 0, 0));
                } else if (item.getType() == ChatMessage.Type.SYSTEM_MSG) {
                    lblText.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 12px; -fx-font-style: italic;");
                    root.setAlignment(Pos.CENTER);
                    root.getChildren().add(lblText);
                } else {
                    lblText.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 15 15 15 0; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                    metaBox.getChildren().add(lblTime); // Chỉ hiện giờ, ko hiện "Đã gửi" cho đối phương
                    metaBox.setAlignment(Pos.BOTTOM_LEFT);
                    root.setAlignment(Pos.CENTER_LEFT);
                    root.getChildren().addAll(lblText, metaBox);
                    HBox.setMargin(metaBox, new Insets(0, 0, 0, 5));
                }
                
                setGraphic(root);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
            }
        }
    }
}
