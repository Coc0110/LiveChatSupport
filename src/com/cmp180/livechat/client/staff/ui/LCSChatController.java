package com.cmp180.livechat.client.staff.ui;

import com.cmp180.livechat.client.staff.context.StaffContext;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import com.cmp180.livechat.client.staff.model.CustomerItem;
import com.cmp180.livechat.client.staff.model.ChatMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class LCSChatController implements StaffNetworkListener {

    @FXML private ListView<CustomerItem> listClients;
    @FXML private ListView<ChatMessage> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblIssue;
    @FXML private Label lblStatus;
    @FXML private Circle statusDot;
    
    private Map<String, List<ChatMessage>> chatHistories = new HashMap<>();
    private ObservableList<CustomerItem> localQueue = FXCollections.observableArrayList();
    private String currentCustomerId = null;
    private Timeline refreshTimer;

    @FXML
    public void initialize() {
        StaffContext.getInstance().getNetworkService().addListener(this);
        StaffContext.getInstance().getNetworkService().requestQueueUpdate();
        
        listClients.setItems(localQueue);
        
        listMessages.setCellFactory(param -> new MessageListCell());
        listClients.setCellFactory(param -> new CustomerListCell());

        listClients.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isActive()) {
                if (newVal.hasUnread()) {
                    newVal.setUnread(false);
                    sortAndRefreshQueue();
                }
                selectCustomer(newVal.getId(), newVal.getFullName());
            }
        });
        
        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> listClients.refresh()));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }
    
    private void sortAndRefreshQueue() {
        Collections.sort(localQueue, (a, b) -> {
            // Group 1: Unread
            if (a.hasUnread() != b.hasUnread()) return a.hasUnread() ? -1 : 1;
            
            // Group 2: Active vs Waiting
            if (a.isActive() != b.isActive()) return a.isActive() ? -1 : 1;
            
            if (a.isActive()) {
                // Both active: sort by last message time (descending)
                return Long.compare(b.getLastMessageTime(), a.getLastMessageTime());
            } else {
                // Both waiting: sort by joined time (ascending - oldest first)
                return Long.compare(a.getJoinedTime(), b.getJoinedTime());
            }
        });
        listClients.refresh();
    }
    
    private void selectCustomer(String customerId, String fullDisplayName) {
        currentCustomerId = customerId;
        String name = fullDisplayName;
        String issue = "";
        if (fullDisplayName.contains(" (") && fullDisplayName.endsWith(")")) {
            int idx = fullDisplayName.indexOf(" (");
            name = fullDisplayName.substring(0, idx);
            issue = fullDisplayName.substring(idx);
        }
        lblCurrentClient.setText(name);
        lblIssue.setText(issue);
        lblStatus.setText("Đang trong cuộc trò chuyện");
        statusDot.setFill(Color.web("#00C853"));
        
        listMessages.getItems().clear();
        if (chatHistories.containsKey(customerId)) {
            listMessages.getItems().addAll(chatHistories.get(customerId));
        }
        if (listMessages.getItems().size() > 0) {
            listMessages.scrollTo(listMessages.getItems().size() - 1);
        }
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String content = txtMessage.getText().trim();
        if (!content.isEmpty() && currentCustomerId != null) {
            StaffContext.getInstance().getNetworkService().sendChatMessage(content, currentCustomerId);
            
            ChatMessage msg = new ChatMessage(content, ChatMessage.Type.MY_MSG, "Bạn");
            chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(msg);
            listMessages.getItems().add(msg);
            listMessages.scrollTo(listMessages.getItems().size() - 1);
            txtMessage.clear();
            
            // Update local state
            for (CustomerItem c : localQueue) {
                if (c.getId().equals(currentCustomerId)) {
                    c.setLastMessageTime(System.currentTimeMillis());
                    sortAndRefreshQueue();
                    break;
                }
            }
        }
    }

    @Override
    public void onLoginSuccess() {}

    @Override
    public void onQueueUpdated(List<CustomerItem> serverQueue) {
        Platform.runLater(() -> {
            // Merge logic
            List<CustomerItem> toRemove = new ArrayList<>(localQueue);
            for (CustomerItem srvItem : serverQueue) {
                boolean found = false;
                for (CustomerItem locItem : localQueue) {
                    if (locItem.getId().equals(srvItem.getId())) {
                        locItem.setActive(srvItem.isActive());
                        locItem.setFullName(srvItem.getFullName());
                        toRemove.remove(locItem);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    localQueue.add(srvItem);
                }
            }
            localQueue.removeAll(toRemove);
            sortAndRefreshQueue();
        });
    }

    @Override
    public void onPairedWithCustomer(String customerName, String customerId) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage("Bạn đã được kết nối với " + customerName, ChatMessage.Type.SYSTEM_MSG, "Hệ thống");
            chatHistories.computeIfAbsent(customerId, k -> new ArrayList<>()).add(msg);
            
            for (CustomerItem c : localQueue) {
                if (c.getId().equals(customerId)) {
                    c.setActive(true);
                    c.setLastMessageTime(System.currentTimeMillis());
                    break;
                }
            }
            sortAndRefreshQueue();
            selectCustomer(customerId, customerName);
        });
    }

    @Override
    public void onMessageReceived(String senderName, String senderId, String message) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(message, ChatMessage.Type.CUSTOMER_MSG, senderName);
            chatHistories.computeIfAbsent(senderId, k -> new ArrayList<>()).add(msg);
            
            boolean isSelected = senderId.equals(currentCustomerId);
            
            if (isSelected) {
                listMessages.getItems().add(msg);
                listMessages.scrollTo(listMessages.getItems().size() - 1);
            }
            
            for (CustomerItem c : localQueue) {
                if (c.getId().equals(senderId)) {
                    c.setLastMessageTime(System.currentTimeMillis());
                    if (!isSelected) {
                        c.setUnread(true);
                    }
                    break;
                }
            }
            sortAndRefreshQueue();
        });
    }

    @Override
    public void onSessionEnded(String reason) {
        Platform.runLater(() -> {
            if (currentCustomerId != null) {
                ChatMessage msg = new ChatMessage(reason, ChatMessage.Type.SYSTEM_MSG, "Hệ thống");
                chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(msg);
                listMessages.getItems().add(msg);
                listMessages.scrollTo(listMessages.getItems().size() - 1);
                lblStatus.setText("Phiên đã kết thúc");
                statusDot.setFill(Color.web("#888888"));
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            lblStatus.setText("Lỗi: " + errorMessage);
        });
    }
    
    // --- Custom Cells ---
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
    
    private class CustomerListCell extends ListCell<CustomerItem> {
        private HBox root = new HBox(10);
        private Label avatar = new Label();
        private Label nameLabel = new Label();
        private Label issueLabel = new Label();
        private VBox textContainer = new VBox(3);
        private Circle dot = new Circle(4);
        private SVGPath clockIcon = new SVGPath();
        private Label timeLabel = new Label();
        private Button btnAccept = new Button("Nhận");
        private HBox timeBox = new HBox(3);
        private VBox rightContainer = new VBox(5);
        private Region spacer = new Region();
        
        public CustomerListCell() {
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(10));
            
            avatar.setMinSize(40, 40);
            avatar.setMaxSize(40, 40);
            avatar.setAlignment(Pos.CENTER);
            avatar.setStyle("-fx-background-color: #0084FF; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-weight: bold; -fx-font-size: 16;");
            
            nameLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");
            nameLabel.setMaxWidth(130);
            nameLabel.setEllipsisString("...");
            nameLabel.setWrapText(false);
            
            issueLabel.setMaxWidth(130);
            issueLabel.setEllipsisString("...");
            issueLabel.setWrapText(false);
            
            textContainer.setMinWidth(0);
            HBox.setHgrow(textContainer, Priority.SOMETIMES);

            issueLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12;");
            
            textContainer.getChildren().addAll(nameLabel, issueLabel);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            clockIcon.setContent("M12 7V12H17M12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21Z");
            clockIcon.setStrokeWidth(2);
            clockIcon.setFill(Color.TRANSPARENT);
            timeBox.getChildren().addAll(clockIcon, timeLabel);
            timeBox.setAlignment(Pos.CENTER);
            
            btnAccept.setStyle("-fx-background-color: #0084FF; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 3 10; -fx-background-radius: 10;");
            
            rightContainer.getChildren().addAll(timeBox, btnAccept);
            rightContainer.setAlignment(Pos.CENTER_RIGHT);
        }
        
        private String toHexString(Color color) {
            return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
        }

        @Override
        protected void updateItem(CustomerItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                root.getChildren().clear();
                
                String displayName = item.getFullName();
                if (displayName.endsWith(" (Đang chat)")) displayName = displayName.replace(" (Đang chat)", "");
                
                avatar.setText(displayName.substring(0, 1).toUpperCase());
                
                String name = displayName;
                String issue = "Cần hỗ trợ";
                if (displayName.contains(" (") && displayName.endsWith(")")) {
                    int idx = displayName.indexOf(" (");
                    name = displayName.substring(0, idx);
                    issue = displayName.substring(idx + 2, displayName.length() - 1);
                }
                
                nameLabel.setText(name);
                issueLabel.setText(issue);
                
                root.getChildren().addAll(avatar, textContainer, spacer);
                
                if (item.isActive()) {
                    // Chấm đỏ nếu có tin nhắn chưa đọc, chấm xanh nếu bình thường
                    dot.setFill(item.hasUnread() ? Color.web("#F44336") : Color.web("#00C853"));
                    root.getChildren().add(dot);
                } else {
                    long waitTimeMillis = System.currentTimeMillis() - item.getJoinedTime();
                    long waitMinutes = waitTimeMillis / 60000;
                    
                    Color iconColor = Color.web("#00C853");
                    if (waitMinutes >= 5) iconColor = Color.web("#F44336");
                    else if (waitMinutes >= 2) iconColor = Color.web("#FF9800");
                    
                    clockIcon.setStroke(iconColor);
                    timeLabel.setText(waitMinutes + "p");
                    timeLabel.setStyle("-fx-text-fill: " + toHexString(iconColor) + "; -fx-font-size: 12; -fx-font-weight: bold;");
                    
                    btnAccept.setOnAction(e -> {
                        StaffContext.getInstance().getNetworkService().acceptCustomer(item.getId());
                    });
                    
                    root.getChildren().add(rightContainer);
                }
                
                setGraphic(root);
                setText(null);
                
                if (isSelected()) {
                    setStyle("-fx-background-color: #e5e5ea;");
                } else {
                    setStyle("-fx-background-color: transparent;");
                }
            }
        }
    }
}
