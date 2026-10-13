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
import java.util.UUID;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.OverrunStyle;


public class CustomerChatController implements CustomerNetworkListener {

    @FXML private ListView<ChatMessage> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Label lblTitle;
    @FXML private Label lblStatus;

    @FXML private HBox replyBar;
    @FXML private Label lblReplySender;
    @FXML private Label lblReplyText;
    @FXML private Button btnCancelReply;
    private ChatMessage replyingTo = null;

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
        lblTheme.getStyleClass().add("theme-label");
        lblTheme.setStyle("-fx-text-fill: #1c1e21; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
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
                lblTheme.setStyle("-fx-text-fill: #1c1e21; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
                switchBg.setStroke(Color.web("#888888"));
                switchIcon.setStroke(Color.web("#888888"));
                if (btnAccount != null && btnAccount.getScene() != null) {
                    btnAccount.getScene().getRoot().getStyleClass().remove("dark-mode");
                }
                accountMenu.getStyleClass().removeAll("dark-mode", "dark-menu");
                settingsMenu.getStyleClass().removeAll("dark-mode", "dark-menu");
                listMessages.refresh();
            } else {
                switchIcon.setTranslateX(4);
                switchIcon.setContent("M18 15C13.0294 15 9 10.9706 9 6C9 5.09074 9.13484 4.21311 9.38561 3.38574C5.69007 4.50583 3 7.93883 3 12.0001C3 16.9707 7.02944 20.9999 12 20.9999C16.0613 20.9999 19.4943 18.3103 20.6144 14.6147C19.787 14.8655 18.9093 15 18 15Z");
                lblTheme.setStyle("-fx-text-fill: #e4e6eb; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
                switchBg.setStroke(Color.web("#b0b3b8"));
                switchIcon.setStroke(Color.web("#b0b3b8"));
                if (btnAccount != null && btnAccount.getScene() != null) {
                    btnAccount.getScene().getRoot().getStyleClass().add("dark-mode");
                }
                if (!accountMenu.getStyleClass().contains("dark-menu")) {
                    accountMenu.getStyleClass().addAll("dark-mode", "dark-menu");
                }
                if (!settingsMenu.getStyleClass().contains("dark-menu")) {
                    settingsMenu.getStyleClass().addAll("dark-mode", "dark-menu");
                }
                listMessages.refresh();
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
            String msgId = UUID.randomUUID().toString();
            String replySender = null;
            String replyText = null;
            if (replyingTo != null) {
                replySender = (replyingTo.getType() == ChatMessage.Type.MY_MSG) ? "Bạn" : replyingTo.getSenderName();
                replyText = replyingTo.getText();
            }

            CustomerContext.getInstance().getNetworkService().sendChatMessage(content, currentStaffId, msgId, replySender, replyText);
            ChatMessage msg = new ChatMessage(msgId, content, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText);
            listMessages.getItems().add(msg);
            listMessages.scrollTo(listMessages.getItems().size() - 1);
            txtMessage.clear();
            handleCancelReply();
        }
    }

    public void startReply(ChatMessage msg) {
        if (msg == null || msg.isRecalled()) return;
        this.replyingTo = msg;
        if (replyBar != null) {
            replyBar.setVisible(true);
            replyBar.setManaged(true);
        }
        String sender = (msg.getType() == ChatMessage.Type.MY_MSG) ? "chính mình" : msg.getSenderName();
        if (lblReplySender != null) lblReplySender.setText("Đang trả lời " + sender + ":");
        if (lblReplyText != null) lblReplyText.setText(msg.getText());
        txtMessage.requestFocus();
    }

    @FXML
    private void handleCancelReply() {
        this.replyingTo = null;
        if (replyBar != null) {
            replyBar.setVisible(false);
            replyBar.setManaged(false);
        }
    }

    private void recallMessage(ChatMessage msg) {
        if (msg == null || currentStaffId == null) return;
        long diff = System.currentTimeMillis() - msg.getTimestamp();
        if (diff > 5 * 60 * 1000) return;

        CustomerContext.getInstance().getNetworkService().recallMessage(msg.getId(), currentStaffId);
        msg.setRecalled(true);
        if (replyingTo != null && msg.getId().equals(replyingTo.getId())) {
            handleCancelReply();
        }
        listMessages.refresh();
    }

    private void deleteMessageForMe(ChatMessage msg) {
        if (msg == null) return;
        listMessages.getItems().remove(msg);
        if (replyingTo != null && msg.getId().equals(replyingTo.getId())) {
            handleCancelReply();
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
    public void onReturnedToQueue(String message) {
        Platform.runLater(() -> {
            handleCancelReply();
            if (currentStaffId != null) {
                currentStaffId = null;
                txtMessage.setDisable(true);
                btnSend.setDisable(true);
                lblStatus.setText("Đang chờ nhân viên khác tiếp nhận...");
                listMessages.getItems().add(new ChatMessage(message, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
                listMessages.scrollTo(listMessages.getItems().size() - 1);
            }
        });
    }

    @Override
    public void onPairedWithStaff(String staffName, String staffId) {
        Platform.runLater(() -> {
            handleCancelReply();
            currentStaffId = staffId;
            lblTitle.setText("Hỗ trợ viên: " + staffName);
            lblStatus.setText("Đã kết nối thành công");
            txtMessage.setDisable(false);
            btnSend.setDisable(false);
            listMessages.getItems().add(new ChatMessage("Bạn đang chat với " + staffName, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
            listMessages.scrollTo(listMessages.getItems().size() - 1);
        });
    }

    @Override
    public void onMessageReceived(String senderName, String message) {
        onMessageReceived(senderName, message, UUID.randomUUID().toString(), null, null);
    }

    @Override
    public void onMessageReceived(String senderName, String message, String messageId, String replyToSender, String replyToText) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, message, ChatMessage.Type.STAFF_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText);
            listMessages.getItems().add(msg);
            listMessages.scrollTo(listMessages.getItems().size() - 1);
        });
    }

    @Override
    public void onMessageRecalled(String messageId) {
        Platform.runLater(() -> {
            if (messageId == null) return;
            for (ChatMessage m : listMessages.getItems()) {
                if (messageId.equals(m.getId())) {
                    m.setRecalled(true);
                    break;
                }
            }
            if (replyingTo != null && messageId.equals(replyingTo.getId())) {
                handleCancelReply();
            }
            listMessages.refresh();
        });
    }

    @Override
    public void onSessionEnded(String reason) {
        Platform.runLater(() -> {
            handleCancelReply();
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
        private HBox actionRow = new HBox(5);
        private Button btnOptions = new Button("•••");
        private VBox bubbleBox = new VBox(4);
        private VBox quoteBox = new VBox(2);
        private Label lblQuoteSender = new Label();
        private Label lblQuoteText = new Label();
        private Label lblRecalled = new Label("Tin nhắn đã được thu hồi");

        public MessageListCell() {
            lblText.setWrapText(true);
            lblText.setMaxWidth(350);

            lblQuoteText.setWrapText(false);
            lblQuoteText.setMaxWidth(320);
            lblQuoteText.setTextOverrun(OverrunStyle.ELLIPSIS);

            lblTime.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
            lblStatus.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

            btnOptions.getStyleClass().add("msg-action-btn");
            btnOptions.setOpacity(0);

            root.setOnMouseEntered(e -> {
                ChatMessage item = getItem();
                if (item != null && !isEmpty() && item.getType() != ChatMessage.Type.SYSTEM_MSG) {
                    btnOptions.setOpacity(1.0);
                }
            });
            root.setOnMouseExited(e -> btnOptions.setOpacity(0));

            btnOptions.setOnAction(e -> {
                ChatMessage item = getItem();
                if (item != null && !isEmpty() && item.getType() != ChatMessage.Type.SYSTEM_MSG) {
                    showContextMenu(btnOptions, item, -1, -1);
                }
            });

            bubbleBox.setOnContextMenuRequested(e -> {
                ChatMessage item = getItem();
                if (item != null && !isEmpty() && item.getType() != ChatMessage.Type.SYSTEM_MSG) {
                    showContextMenu(bubbleBox, item, e.getScreenX(), e.getScreenY());
                    e.consume();
                }
            });
        }

        private void showContextMenu(javafx.scene.Node anchor, ChatMessage item, double screenX, double screenY) {
            ContextMenu menu = new ContextMenu();
            boolean isDarkTheme = getScene() != null && getScene().getRoot().getStyleClass().contains("dark-mode");
            if (isDarkTheme) {
                menu.getStyleClass().addAll("dark-mode", "dark-menu");
            }

            if (!item.isRecalled()) {
                MenuItem itemReply = new MenuItem("Trả lời");
                itemReply.setOnAction(ev -> startReply(item));
                menu.getItems().add(itemReply);

                MenuItem itemCopy = new MenuItem("Sao chép");
                itemCopy.setOnAction(ev -> {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(item.getText());
                    clipboard.setContent(cc);
                });
                menu.getItems().add(itemCopy);
            }

            boolean canRecall = (item.getType() == ChatMessage.Type.MY_MSG)
                    && !item.isRecalled()
                    && (System.currentTimeMillis() - item.getTimestamp() <= 5 * 60 * 1000);

            if (canRecall) {
                MenuItem itemRecall = new MenuItem("Thu hồi tin nhắn");
                itemRecall.setOnAction(ev -> recallMessage(item));
                menu.getItems().add(itemRecall);
            }

            MenuItem itemDelete = new MenuItem("Xóa ở phía tôi");
            itemDelete.setOnAction(ev -> deleteMessageForMe(item));
            menu.getItems().add(itemDelete);

            if (screenX > 0 && screenY > 0) {
                menu.show(anchor, screenX, screenY);
            } else {
                menu.show(anchor, Side.BOTTOM, 0, 0);
            }
        }

        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
                btnOptions.setOpacity(0);
            } else {
                root.getChildren().clear();
                messageAndStatus.getChildren().clear();
                timeAndBubble.getChildren().clear();
                actionRow.getChildren().clear();
                bubbleBox.getChildren().clear();
                quoteBox.getChildren().clear();

                if (item.getType() == ChatMessage.Type.SYSTEM_MSG) {
                    lblText.setText(item.getText());
                    lblText.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 12px; -fx-font-style: italic; -fx-padding: 0;");
                    root.setAlignment(Pos.CENTER);
                    root.getChildren().add(lblText);
                    setGraphic(root);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
                    return;
                }

                boolean isDarkTheme = getScene() != null && getScene().getRoot().getStyleClass().contains("dark-mode");
                lblTime.setText(item.getFormattedTime());

                if (item.isRecalled()) {
                    lblRecalled.setText("Tin nhắn đã được thu hồi");
                    lblRecalled.getStyleClass().setAll("bubble-recalled");
                    bubbleBox.getChildren().add(lblRecalled);
                    bubbleBox.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    if (item.getReplyToText() != null && !item.getReplyToText().isEmpty()) {
                        lblQuoteSender.setText(item.getReplyToSender() != null ? item.getReplyToSender() : "Trả lời");
                        lblQuoteText.setText(item.getReplyToText());
                        quoteBox.getChildren().setAll(lblQuoteSender, lblQuoteText);

                        if (item.getType() == ChatMessage.Type.MY_MSG) {
                            quoteBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.15); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: rgba(255, 255, 255, 0.8); -fx-border-width: 0 0 0 3;");
                            lblQuoteSender.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                            lblQuoteText.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.85);");
                        } else {
                            quoteBox.setStyle(isDarkTheme 
                                ? "-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: #0084FF; -fx-border-width: 0 0 0 3;" 
                                : "-fx-background-color: rgba(0, 0, 0, 0.05); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: #0084FF; -fx-border-width: 0 0 0 3;" );
                            lblQuoteSender.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0084FF;");
                            lblQuoteText.setStyle(isDarkTheme 
                                ? "-fx-font-size: 11px; -fx-text-fill: #b0b3b8;" 
                                : "-fx-font-size: 11px; -fx-text-fill: #65676b;");
                        }
                        bubbleBox.getChildren().add(quoteBox);
                    }

                    lblText.setText(item.getText());
                    if (item.getType() == ChatMessage.Type.MY_MSG) {
                        lblText.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 0;");
                        bubbleBox.setStyle("-fx-background-color: #0084FF; -fx-background-radius: 15 15 0 15; -fx-padding: 8 14 8 14;");
                    } else {
                        if (isDarkTheme) {
                            lblText.setStyle("-fx-background-color: transparent; -fx-text-fill: #e4e6eb; -fx-font-size: 14px; -fx-padding: 0;");
                            bubbleBox.setStyle("-fx-background-color: #242526; -fx-background-radius: 15 15 15 0; -fx-padding: 8 14 8 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
                        } else {
                            lblText.setStyle("-fx-background-color: transparent; -fx-text-fill: #050505; -fx-font-size: 14px; -fx-padding: 0;");
                            bubbleBox.setStyle("-fx-background-color: white; -fx-background-radius: 15 15 15 0; -fx-padding: 8 14 8 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                        }
                    }
                    bubbleBox.getChildren().add(lblText);
                }

                if (item.getType() == ChatMessage.Type.MY_MSG) {
                    timeAndBubble.setAlignment(Pos.BOTTOM_RIGHT);
                    timeAndBubble.getChildren().addAll(lblTime, bubbleBox);

                    actionRow.setAlignment(Pos.CENTER_RIGHT);
                    actionRow.getChildren().addAll(btnOptions, timeAndBubble);

                    messageAndStatus.setAlignment(Pos.TOP_RIGHT);
                    messageAndStatus.getChildren().add(actionRow);

                    boolean isLast = (getIndex() == getListView().getItems().size() - 1);
                    if (isLast && !item.isRecalled()) {
                        lblStatus.setText("Đã nhận");
                        messageAndStatus.getChildren().add(lblStatus);
                    }

                    root.setAlignment(Pos.CENTER_RIGHT);
                    root.getChildren().add(messageAndStatus);
                } else {
                    timeAndBubble.setAlignment(Pos.BOTTOM_LEFT);
                    timeAndBubble.getChildren().addAll(bubbleBox, lblTime);

                    actionRow.setAlignment(Pos.CENTER_LEFT);
                    actionRow.getChildren().addAll(timeAndBubble, btnOptions);

                    root.setAlignment(Pos.CENTER_LEFT);
                    root.getChildren().add(actionRow);
                }

                setGraphic(root);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 4px 10px;");
            }
        }
    }
}
