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
import javafx.scene.control.Tooltip;
import javafx.scene.control.ContentDisplay;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import java.util.UUID;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.OverrunStyle;
import com.cmp180.livechat.client.ui.EmojiPickerPopup;
import com.cmp180.livechat.client.ui.EmojiManager;
import com.cmp180.livechat.client.ui.ThemeManager;
import com.cmp180.livechat.client.ui.SettingsDialog;
import com.cmp180.livechat.client.ui.FilePreviewDialog;
import com.cmp180.livechat.client.ui.ImageViewerDialog;
import com.cmp180.livechat.common.Protocol;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.Optional;


public class LCSChatController implements StaffNetworkListener {

    @FXML private ListView<CustomerItem> listClients;
    @FXML private ListView<ChatMessage> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Button btnEmoji;
    @FXML private Button btnAttachImage;
    @FXML private Button btnAttachFile;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblIssue;
    @FXML private Label lblStatus;
    @FXML private Circle statusDot;
    
    @FXML private HBox replyBar;
    @FXML private Label lblReplySender;
    @FXML private Label lblReplyText;
    @FXML private Button btnCancelReply;
    private ChatMessage replyingTo = null;
    
    private Map<String, List<ChatMessage>> chatHistories = new HashMap<>();
    private ObservableList<CustomerItem> localQueue = FXCollections.observableArrayList();
    private String currentCustomerId = null;
    private Timeline refreshTimer;

    @FXML private Button btnAccount;
    @FXML private Button btnSettings;
    private ContextMenu accountMenu;
    private ContextMenu settingsMenu;

    @FXML
    public void initialize() {
        
        // --- Account Menu Setup ---
        accountMenu = new ContextMenu();
        MenuItem itemProfile = new MenuItem("Hồ sơ của bạn");
        MenuItem itemAccSettings = new MenuItem("Cài đặt");
        itemAccSettings.setOnAction(e -> {
            if (btnAccount != null && btnAccount.getScene() != null) {
                SettingsDialog.show(btnAccount.getScene().getWindow());
            }
        });
        MenuItem itemLogout = new MenuItem("Đăng xuất");
        itemLogout.setOnAction(e -> handleLogout());
        accountMenu.getItems().addAll(itemProfile, itemAccSettings, new SeparatorMenuItem(), itemLogout);

        Platform.runLater(() -> {
            if (btnAccount != null && btnAccount.getScene() != null) {
                ThemeManager.applyTheme(btnAccount.getScene());
                if (btnAccount.getScene().getWindow() != null) {
                    btnAccount.getScene().getWindow().setOnCloseRequest(ev -> {
                        if (refreshTimer != null) refreshTimer.stop();
                        StaffContext.getInstance().getNetworkService().disconnect();
                        Platform.exit();
                        System.exit(0);
                    });
                }
            }
        });

        ThemeManager.addListener(() -> {
            Platform.runLater(() -> {
                if (btnAccount != null && btnAccount.getScene() != null) {
                    ThemeManager.applyTheme(btnAccount.getScene());
                    if (ThemeManager.isDarkMode()) {
                        if (!accountMenu.getStyleClass().contains("dark-menu")) {
                            accountMenu.getStyleClass().addAll("dark-mode", "dark-menu");
                        }
                    } else {
                        accountMenu.getStyleClass().removeAll("dark-mode", "dark-menu");
                    }
                    listMessages.refresh();
                    listClients.refresh();
                }
            });
        });

        StaffContext.getInstance().getNetworkService().addListener(this);
        StaffContext.getInstance().getNetworkService().requestQueueUpdate();
        
        listClients.setItems(localQueue);
        
        listMessages.setCellFactory(param -> new MessageListCell());
        listClients.setCellFactory(param -> new CustomerListCell());

        if (btnEmoji != null && txtMessage != null) {
            EmojiPickerPopup.attach(btnEmoji, txtMessage);
        }

        if (btnAttachImage != null) btnAttachImage.setDisable(true);
        if (btnAttachFile != null) btnAttachFile.setDisable(true);

        if (txtMessage != null) {
            txtMessage.setOnKeyPressed(event -> {
                if (new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN).match(event)) {
                    if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;
                    Clipboard cb = Clipboard.getSystemClipboard();
                    if (cb.hasFiles()) {
                        List<File> files = cb.getFiles();
                        if (!files.isEmpty()) {
                            File f = files.get(0);
                            processAndSendFile(f, isImageExtension(f.getName()));
                            event.consume();
                        }
                    } else if (cb.hasImage()) {
                        Image img = cb.getImage();
                        byte[] png = imageToPngBytes(img);
                        if (png != null) {
                            String name = "screenshot_" + System.currentTimeMillis() + ".png";
                            String initialCaption = txtMessage.getText().trim();
                            Window owner = txtMessage.getScene().getWindow();
                            Optional<FilePreviewDialog.FilePreviewResult> opt = FilePreviewDialog.show(owner, name, png, true, initialCaption);
                            if (opt.isPresent()) {
                                FilePreviewDialog.FilePreviewResult res = opt.get();
                                sendFilePayload(res.fileName, res.fileSize, res.fileData, res.isImage, res.caption);
                                txtMessage.clear();
                            }
                            event.consume();
                        }
                    }
                }
            });
        }

        listClients.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingSelection) return;
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

    private boolean isUpdatingSelection = false;
    
    private void sortAndRefreshQueue() {
        isUpdatingSelection = true;
        try {
            Collections.sort(localQueue, (a, b) -> {
                // Group 1: Unread
                if (a.hasUnread() != b.hasUnread()) return a.hasUnread() ? -1 : 1;
                
                // Group 2: Active vs Waiting
                if (a.isActive() != b.isActive()) return a.isActive() ? -1 : 1;
                
                if (a.isActive()) {
                    // Both active: sort by last message time (descending)
                    return Long.compare(b.getLastMessageTime(), a.getLastMessageTime());
                } else {
                    // Group 3: Waiting - prioritize interrupted ("Đang tư vấn dở") customers first
                    if (a.isInterrupted() != b.isInterrupted()) return a.isInterrupted() ? -1 : 1;
                    // Both waiting: sort by joined time (ascending - oldest first)
                    return Long.compare(a.getJoinedTime(), b.getJoinedTime());
                }
            });
            if (currentCustomerId != null) {
                for (CustomerItem c : localQueue) {
                    if (c.getId().equals(currentCustomerId)) {
                        listClients.getSelectionModel().select(c);
                        break;
                    }
                }
            }
            listClients.refresh();
        } finally {
            isUpdatingSelection = false;
        }
    }
    
    private void selectCustomer(String customerId, String fullDisplayName) {
        handleCancelReply();
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
        if (btnAttachImage != null) btnAttachImage.setDisable(false);
        if (btnAttachFile != null) btnAttachFile.setDisable(false);
        
        listMessages.getItems().clear();
        if (chatHistories.containsKey(customerId)) {
            listMessages.getItems().addAll(chatHistories.get(customerId));
        }
        if (listMessages.getItems().size() > 0) {
            listMessages.scrollTo(listMessages.getItems().size() - 1);
        }

        isUpdatingSelection = true;
        try {
            for (CustomerItem c : localQueue) {
                if (c.getId().equals(customerId)) {
                    listClients.getSelectionModel().select(c);
                    break;
                }
            }
            listClients.refresh();
        } finally {
            isUpdatingSelection = false;
        }
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String content = txtMessage.getText().trim();
        if (content.isEmpty() || currentCustomerId == null) {
            return;
        }

        if (content.length() > 2000) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            if (btnSend != null && btnSend.getScene() != null && btnSend.getScene().getWindow() != null) {
                alert.initOwner(btnSend.getScene().getWindow());
            }
            alert.setTitle("Tin nhắn quá dài");
            alert.setHeaderText("Không thể gửi tin nhắn");
            alert.setContentText("Nội dung tin nhắn vượt quá giới hạn 2.000 ký tự (Hiện tại: " + content.length() + " ký tự). Vui lòng rút ngắn nội dung hoặc gửi thành nhiều tin.");
            if (alert.getDialogPane().getScene() != null) {
                ThemeManager.applyTheme(alert.getDialogPane().getScene());
            }
            alert.showAndWait();
            return;
        }

        String msgId = UUID.randomUUID().toString();
        String replySender = null;
        String replyText = null;
        if (replyingTo != null) {
            replySender = (replyingTo.getType() == ChatMessage.Type.MY_MSG) ? "Bạn" : replyingTo.getSenderName();
            replyText = replyingTo.isFile() 
                    ? ("[" + (replyingTo.isImage() ? "Hình ảnh" : "Tệp") + "] " + replyingTo.getFileName()) 
                    : replyingTo.getText();
        }

        StaffContext.getInstance().getNetworkService().sendChatMessage(content, currentCustomerId, msgId, replySender, replyText);
        
        ChatMessage msg = new ChatMessage(msgId, content, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText);
        chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(msg);
        listMessages.getItems().add(msg);
        listMessages.scrollTo(listMessages.getItems().size() - 1);
        txtMessage.clear();
        handleCancelReply();
        
        // Update local state
        for (CustomerItem c : localQueue) {
            if (c.getId().equals(currentCustomerId)) {
                c.setLastMessageTime(System.currentTimeMillis());
                sortAndRefreshQueue();
                break;
            }
        }
    }

    @FXML
    private void handleAttachImage() {
        if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn hình ảnh để gửi");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Hình ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp, *.webp)", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("Tất cả tệp (*.*)", "*.*")
        );
        Window owner = btnAttachImage.getScene().getWindow();
        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            processAndSendFile(file, true);
        }
    }

    @FXML
    private void handleAttachFile() {
        if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn tệp đính kèm để gửi");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tất cả tệp (*.*)", "*.*"),
                new FileChooser.ExtensionFilter("Tài liệu (*.pdf, *.docx, *.xlsx, *.pptx, *.txt, *.zip, *.rar)", "*.pdf", "*.docx", "*.xlsx", "*.pptx", "*.txt", "*.zip", "*.rar")
        );
        Window owner = btnAttachFile.getScene().getWindow();
        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            boolean isImg = isImageExtension(file.getName());
            processAndSendFile(file, isImg);
        }
    }

    private boolean isImageExtension(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".webp");
    }

    private void processAndSendFile(File file, boolean isImage) {
        if (file == null || !file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            if (btnAttachFile != null && btnAttachFile.getScene() != null && btnAttachFile.getScene().getWindow() != null) {
                alert.initOwner(btnAttachFile.getScene().getWindow());
            }
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không tìm thấy tệp");
            alert.setContentText("Tệp đã chọn không tồn tại hoặc đã bị xóa!");
            if (alert.getDialogPane().getScene() != null) ThemeManager.applyTheme(alert.getDialogPane().getScene());
            alert.showAndWait();
            return;
        }

        if (file.length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            if (btnAttachFile != null && btnAttachFile.getScene() != null && btnAttachFile.getScene().getWindow() != null) {
                alert.initOwner(btnAttachFile.getScene().getWindow());
            }
            alert.setTitle("Tệp rỗng");
            alert.setHeaderText("Không thể đính kèm tệp");
            alert.setContentText("Tệp bạn chọn không có dữ liệu (0 byte). Vui lòng chọn tệp hợp lệ!");
            if (alert.getDialogPane().getScene() != null) ThemeManager.applyTheme(alert.getDialogPane().getScene());
            alert.showAndWait();
            return;
        }

        if (file.length() > Protocol.MAX_FILE_SIZE) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            if (btnAttachFile != null && btnAttachFile.getScene() != null && btnAttachFile.getScene().getWindow() != null) {
                alert.initOwner(btnAttachFile.getScene().getWindow());
            }
            alert.setTitle("Dung lượng vượt giới hạn");
            alert.setHeaderText("Không thể đính kèm tệp");
            alert.setContentText("Dung lượng tệp (" + String.format("%.1f MB", file.length() / (1024.0 * 1024.0)) + ") vượt quá giới hạn cho phép là 25 MB.");
            if (alert.getDialogPane().getScene() != null) ThemeManager.applyTheme(alert.getDialogPane().getScene());
            alert.showAndWait();
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String initialCaption = txtMessage.getText().trim();
            Window owner = btnAttachFile.getScene().getWindow();

            Optional<FilePreviewDialog.FilePreviewResult> opt = FilePreviewDialog.show(owner, file.getName(), bytes, isImage, initialCaption);
            if (opt.isPresent()) {
                FilePreviewDialog.FilePreviewResult res = opt.get();
                sendFilePayload(res.fileName, res.fileSize, res.fileData, res.isImage, res.caption);
                txtMessage.clear();
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(btnAttachFile.getScene().getWindow());
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể đọc tệp");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private byte[] imageToPngBytes(Image fxImage) {
        if (fxImage == null) return null;
        try {
            java.awt.image.BufferedImage bImage = javafx.embed.swing.SwingFXUtils.fromFXImage(fxImage, null);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void sendFilePayload(String fileName, long fileSize, byte[] fileData, boolean isImage, String caption) {
        String msgId = UUID.randomUUID().toString();
        String replySender = (replyingTo != null) ? (replyingTo.getType() == ChatMessage.Type.MY_MSG ? "chính mình" : replyingTo.getSenderName()) : null;
        String replyText = (replyingTo != null) ? (replyingTo.isFile() ? ("[" + (replyingTo.isImage() ? "Hình ảnh" : "Tệp") + "] " + replyingTo.getFileName()) : replyingTo.getText()) : null;

        StaffContext.getInstance().getNetworkService().sendFileMessage(fileName, fileSize, fileData, isImage, caption, currentCustomerId, msgId, replySender, replyText);

        ChatMessage msg = new ChatMessage(msgId, caption, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, fileName, fileSize, fileData, isImage);
        chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(msg);
        listMessages.getItems().add(msg);
        listMessages.scrollTo(listMessages.getItems().size() - 1);
        handleCancelReply();

        for (CustomerItem c : localQueue) {
            if (c.getId().equals(currentCustomerId)) {
                c.setLastMessageTime(System.currentTimeMillis());
                sortAndRefreshQueue();
                break;
            }
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
        String replyDisplay = msg.isFile() 
                ? ("[" + (msg.isImage() ? "Hình ảnh" : "Tệp") + "] " + msg.getFileName() + (msg.getText() != null && !msg.getText().isEmpty() ? (" - " + msg.getText()) : ""))
                : msg.getText();
        if (lblReplyText != null) lblReplyText.setText(replyDisplay);
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
        if (msg == null || currentCustomerId == null) return;
        long diff = System.currentTimeMillis() - msg.getTimestamp();
        if (diff > 5 * 60 * 1000) return;

        StaffContext.getInstance().getNetworkService().recallMessage(msg.getId(), currentCustomerId);
        msg.setRecalled(true);
        if (replyingTo != null && msg.getId().equals(replyingTo.getId())) {
            handleCancelReply();
        }
        listMessages.refresh();
    }

    private void deleteMessageForMe(ChatMessage msg) {
        if (msg == null) return;
        listMessages.getItems().remove(msg);
        if (currentCustomerId != null && chatHistories.containsKey(currentCustomerId)) {
            chatHistories.get(currentCustomerId).remove(msg);
        }
        if (replyingTo != null && msg.getId().equals(replyingTo.getId())) {
            handleCancelReply();
        }
    }

    @Override
    public void onLoginSuccess() {
        Platform.runLater(() -> {
            statusDot.setFill(Color.web("#00C853"));
            lblStatus.setText("Đang sẵn sàng tư vấn");
            StaffContext.getInstance().getNetworkService().requestQueueUpdate();
        });
    }

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
                        locItem.setInterrupted(srvItem.isInterrupted());
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
    public void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message) {
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message, UUID.randomUUID().toString(), null, null, false, null, 0, null, false);
    }

    @Override
    public void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled) {
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message, messageId, replyToSender, replyToText, isRecalled, null, 0, null, false);
    }

    @Override
    public void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        Platform.runLater(() -> {
            ChatMessage.Type type;
            String displayText = message;
            if ("STAFF".equalsIgnoreCase(senderRole)) {
                type = ChatMessage.Type.MY_MSG;
                displayText = message;
            } else if ("SYSTEM".equalsIgnoreCase(senderRole)) {
                type = ChatMessage.Type.SYSTEM_MSG;
            } else {
                type = ChatMessage.Type.CUSTOMER_MSG;
            }
            ChatMessage historyMsg = new ChatMessage(messageId, displayText, type, senderName, timestamp, replyToSender, replyToText, fileName, fileSize, fileData, isImage);
            historyMsg.setRecalled(isRecalled);

            List<ChatMessage> historyList = chatHistories.computeIfAbsent(customerId, k -> new ArrayList<>());
            if (historyList.isEmpty()) {
                historyList.add(new ChatMessage("--- Lịch sử tư vấn trước đó ---", ChatMessage.Type.SYSTEM_MSG, "Hệ thống", timestamp));
            }
            historyList.add(historyMsg);
            if (customerId.equals(currentCustomerId)) {
                listMessages.getItems().setAll(historyList);
                listMessages.scrollTo(listMessages.getItems().size() - 1);
            }
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
                    c.setInterrupted(false);
                    c.setLastMessageTime(System.currentTimeMillis());
                    break;
                }
            }
            currentCustomerId = customerId;
            sortAndRefreshQueue();
            selectCustomer(customerId, customerName);
        });
    }

    @Override
    public void onMessageReceived(String senderName, String senderId, String message) {
        onMessageReceived(senderName, senderId, message, UUID.randomUUID().toString(), null, null);
    }

    @Override
    public void onMessageReceived(String senderName, String senderId, String message, String messageId, String replyToSender, String replyToText) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, message, ChatMessage.Type.CUSTOMER_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText);
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
    public void onFileReceived(String senderName, String senderId, String messageId, String caption, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, caption, ChatMessage.Type.CUSTOMER_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, fileName, fileSize, fileData, isImage);
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
    public void onMessageRecalled(String messageId) {
        Platform.runLater(() -> {
            if (messageId == null) return;
            for (List<ChatMessage> list : chatHistories.values()) {
                for (ChatMessage m : list) {
                    if (messageId.equals(m.getId())) {
                        m.setRecalled(true);
                        break;
                    }
                }
            }
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
            if (btnAttachImage != null) btnAttachImage.setDisable(true);
            if (btnAttachFile != null) btnAttachFile.setDisable(true);
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
            lblStatus.setText(errorMessage);
            statusDot.setFill(Color.web("#F44336"));
        });
    }
    
    // --- Custom Cells ---
    private class MessageListCell extends ListCell<ChatMessage> {
        private HBox root = new HBox();
        private Label lblText = new Label();
        private Label lblTime = new Label();
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
                if (item.isFile()) {
                    if (item.isImage()) {
                        MenuItem itemOpenImg = new MenuItem("Xem hình ảnh");
                        itemOpenImg.setOnAction(ev -> ImageViewerDialog.show(getScene().getWindow(), item.getFileName(), item.getFileData()));
                        menu.getItems().add(itemOpenImg);

                        MenuItem itemCopyImg = new MenuItem("Sao chép hình ảnh");
                        itemCopyImg.setOnAction(ev -> {
                            try {
                                Image img = new Image(new ByteArrayInputStream(item.getFileData()));
                                Clipboard clipboard = Clipboard.getSystemClipboard();
                                ClipboardContent cc = new ClipboardContent();
                                cc.putImage(img);
                                clipboard.setContent(cc);
                            } catch (Exception ignored) {}
                        });
                        menu.getItems().add(itemCopyImg);
                    }
                    MenuItem itemSaveFile = new MenuItem("Lưu về máy...");
                    itemSaveFile.setOnAction(ev -> saveFileToDisk(item));
                    menu.getItems().add(itemSaveFile);
                }

                MenuItem itemReply = new MenuItem("Trả lời");
                itemReply.setOnAction(ev -> startReply(item));
                menu.getItems().add(itemReply);

                if (!item.isFile() && item.getText() != null && !item.getText().isEmpty()) {
                    MenuItem itemCopy = new MenuItem("Sao chép nội dung");
                    itemCopy.setOnAction(ev -> {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(item.getText());
                        clipboard.setContent(cc);
                    });
                    menu.getItems().add(itemCopy);
                }
            }

            boolean canRecall = (currentCustomerId != null)
                    && (item.getType() == ChatMessage.Type.MY_MSG)
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

        private void saveFileToDisk(ChatMessage item) {
            if (item == null || item.getFileData() == null) return;
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Lưu tệp về máy");
            chooser.setInitialFileName(item.getFileName());
            File dest = chooser.showSaveDialog(getScene().getWindow());
            if (dest != null) {
                try {
                    Files.write(dest.toPath(), item.getFileData());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(getScene().getWindow());
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Đã lưu tệp về: " + dest.getAbsolutePath());
                    alert.showAndWait();
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(getScene().getWindow());
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Không thể lưu tệp");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        }

        private HBox createFileCard(ChatMessage item, boolean dark, boolean isMine) {
            HBox card = new HBox(10);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(8, 12, 8, 10));
            card.setCursor(Cursor.HAND);
            card.setPrefWidth(240);
            card.setMaxWidth(260);

            String cardBg = isMine 
                    ? "rgba(0, 0, 0, 0.15)" 
                    : (dark ? "rgba(255, 255, 255, 0.08)" : "#f0f2f5");
            card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 10;");

            // Document icon
            SVGPath icon = new SVGPath();
            icon.setContent("M14 2H6C4.9 2 4 2.9 4 4V20C4 21.1 4.9 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2ZM18 20H6V4H13V9H18V20ZM12 18L7 13H10V10H14V13H17L12 18Z");
            icon.setFill(Color.web(isMine ? "#ffffff" : ThemeManager.getPrimaryColor()));
            icon.setScaleX(1.3);
            icon.setScaleY(1.3);

            StackPane iconHolder = new StackPane(icon);
            iconHolder.setPrefSize(36, 36);
            iconHolder.setStyle("-fx-background-color: " + (isMine ? "rgba(255,255,255,0.2)" : ThemeManager.getLightTint(ThemeManager.getPrimaryColor())) + "; -fx-background-radius: 8;");

            VBox infoBox = new VBox(2);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label lblName = new Label(item.getFileName());
            lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
            lblName.setStyle("-fx-text-fill: " + (isMine ? "#ffffff" : (dark ? "#ffffff" : "#1c1e21")) + ";");
            lblName.setMaxWidth(140);
            lblName.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);

            String metaText = item.getFileExtension() + " • " + item.getFormattedFileSize();
            Label lblMeta = new Label(metaText);
            lblMeta.setFont(Font.font("Segoe UI", 11));
            lblMeta.setStyle("-fx-text-fill: " + (isMine ? "rgba(255,255,255,0.8)" : (dark ? "#b0b3b8" : "#65676b")) + ";");

            infoBox.getChildren().addAll(lblName, lblMeta);

            // Download icon
            SVGPath dlIcon = new SVGPath();
            dlIcon.setContent("M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z");
            dlIcon.setFill(Color.web(isMine ? "#ffffff" : (dark ? "#b0b3b8" : "#65676b")));
            dlIcon.setScaleX(0.9);
            dlIcon.setScaleY(0.9);
            StackPane dlBtn = new StackPane(dlIcon);
            dlBtn.setPrefSize(28, 28);
            dlBtn.setStyle("-fx-background-color: " + (isMine ? "rgba(255,255,255,0.15)" : (dark ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.06)")) + "; -fx-background-radius: 14;");

            card.getChildren().addAll(iconHolder, infoBox, dlBtn);

            card.setOnMouseClicked(e -> saveFileToDisk(item));
            return card;
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
                                ? "-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: " + ThemeManager.getPrimaryColor() + "; -fx-border-width: 0 0 0 3;" 
                                : "-fx-background-color: rgba(0, 0, 0, 0.05); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: " + ThemeManager.getPrimaryColor() + "; -fx-border-width: 0 0 0 3;" );
                            lblQuoteSender.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.getPrimaryColor() + ";");
                            lblQuoteText.setStyle(isDarkTheme 
                                ? "-fx-font-size: 11px; -fx-text-fill: #b0b3b8;" 
                                : "-fx-font-size: 11px; -fx-text-fill: #65676b;");
                        }
                        bubbleBox.getChildren().add(quoteBox);
                    }

                    boolean hasCaption = item.getText() != null && !item.getText().trim().isEmpty();
                    boolean hasReply = item.getReplyToText() != null && !item.getReplyToText().trim().isEmpty();
                    boolean isSoloImage = item.isFile() && item.isImage() && !hasCaption && !hasReply;

                    String textColor;
                    if (item.getType() == ChatMessage.Type.MY_MSG) {
                        textColor = "#ffffff";
                        if (isSoloImage) {
                            bubbleBox.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                        } else {
                            bubbleBox.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-background-radius: 15 15 0 15; -fx-padding: " + (item.isImage() ? "4 4 6 4" : "8 14 8 14") + ";");
                        }
                    } else {
                        textColor = isDarkTheme ? "#e4e6eb" : "#050505";
                        if (isSoloImage) {
                            bubbleBox.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                        } else {
                            if (isDarkTheme) {
                                bubbleBox.setStyle("-fx-background-color: #242526; -fx-background-radius: 15 15 15 0; -fx-padding: " + (item.isImage() ? "4 4 6 4" : "8 14 8 14") + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
                            } else {
                                bubbleBox.setStyle("-fx-background-color: white; -fx-background-radius: 15 15 15 0; -fx-padding: " + (item.isImage() ? "4 4 6 4" : "8 14 8 14") + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                            }
                        }
                    }

                    if (item.isFile()) {
                        if (item.isImage()) {
                            try {
                                Image img = new Image(new ByteArrayInputStream(item.getFileData()));
                                if (img.isError() || img.getWidth() <= 0) {
                                    try {
                                        java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(new ByteArrayInputStream(item.getFileData()));
                                        if (bimg != null) {
                                            img = javafx.embed.swing.SwingFXUtils.toFXImage(bimg, null);
                                        }
                                    } catch (Exception ignored) {}
                                }

                                if (img != null && !img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
                                    double maxW = 260;
                                    double maxH = 260;
                                    double scale = Math.min(maxW / img.getWidth(), maxH / img.getHeight());
                                    if (scale > 1.0) scale = 1.0;
                                    double w = Math.max(1, Math.round(img.getWidth() * scale));
                                    double h = Math.max(1, Math.round(img.getHeight() * scale));

                                    Rectangle imgView = new Rectangle(w, h);
                                    imgView.setArcWidth(16);
                                    imgView.setArcHeight(16);
                                    imgView.setFill(new ImagePattern(img));
                                    imgView.setCursor(Cursor.HAND);

                                    imgView.setOnMouseClicked(ev -> {
                                        ImageViewerDialog.show(getScene().getWindow(), item.getFileName(), item.getFileData());
                                    });
                                    bubbleBox.getChildren().add(imgView);
                                } else {
                                    Label lblErr = new Label("Lỗi hiển thị ảnh");
                                    lblErr.setStyle("-fx-text-fill: " + textColor + "; -fx-font-style: italic;");
                                    bubbleBox.getChildren().add(lblErr);
                                }
                            } catch (Exception ex) {
                                Label lblErr = new Label("Lỗi hiển thị ảnh: " + ex.getMessage());
                                lblErr.setStyle("-fx-text-fill: " + textColor + "; -fx-font-style: italic;");
                                bubbleBox.getChildren().add(lblErr);
                            }
                        } else {
                            HBox fileCard = createFileCard(item, isDarkTheme, item.getType() == ChatMessage.Type.MY_MSG);
                            bubbleBox.getChildren().add(fileCard);
                        }

                        if (hasCaption) {
                            Node captionNode = EmojiManager.createEmojiTextFlow(item.getText(), textColor, 13.5);
                            VBox.setMargin(captionNode, new Insets(2, 6, 2, 6));
                            bubbleBox.getChildren().add(captionNode);
                        }
                    } else {
                        Node textNode = EmojiManager.createEmojiTextFlow(item.getText(), textColor, 14);
                        bubbleBox.getChildren().add(textNode);
                    }
                }

                if (item.getType() == ChatMessage.Type.MY_MSG) {
                    timeAndBubble.setAlignment(Pos.BOTTOM_RIGHT);
                    timeAndBubble.getChildren().addAll(lblTime, bubbleBox);

                    actionRow.setAlignment(Pos.CENTER_RIGHT);
                    actionRow.getChildren().addAll(btnOptions, timeAndBubble);

                    root.setAlignment(Pos.CENTER_RIGHT);
                    root.getChildren().add(actionRow);
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
    
    private class CustomerListCell extends ListCell<CustomerItem> {
        private HBox root = new HBox(8);
        private Label avatar = new Label();
        private Label nameLabel = new Label();
        private Label issueLabel = new Label();
        private VBox textContainer = new VBox(2);
        private Circle dot = new Circle(4);
        private SVGPath clockIcon = new SVGPath();
        private Label timeLabel = new Label();
        private Button btnAccept = new Button("Nhận");
        private HBox timeBox = new HBox(3);
        private VBox rightContainer = new VBox(4);
        
        public CustomerListCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(8, 10, 8, 10));
            
            // Khóa chặt kích thước của cell và root theo bề rộng của ListView để không bao giờ bị tràn lề
            listViewProperty().addListener((obs, oldVal, lv) -> {
                if (lv != null) {
                    bindCellWidth(lv);
                }
            });
            if (getListView() != null) {
                bindCellWidth(getListView());
            }
            root.prefWidthProperty().bind(prefWidthProperty());
            root.maxWidthProperty().bind(maxWidthProperty());
            
            avatar.setMinSize(40, 40);
            avatar.setMaxSize(40, 40);
            avatar.setPrefSize(40, 40);
            avatar.setAlignment(Pos.CENTER);
            avatar.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-weight: bold; -fx-font-size: 15;");
            
            nameLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");
            nameLabel.setMinWidth(0);
            nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            nameLabel.setEllipsisString("...");
            nameLabel.setWrapText(false);
            
            issueLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12;");
            issueLabel.setMinWidth(0);
            issueLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            issueLabel.setEllipsisString("...");
            issueLabel.setWrapText(false);
            
            textContainer.setMinWidth(0);
            textContainer.setFillWidth(true);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            textContainer.getChildren().addAll(nameLabel, issueLabel);
            
            // Ràng buộc chiều rộng chữ luôn co theo textContainer
            nameLabel.maxWidthProperty().bind(textContainer.widthProperty());
            issueLabel.maxWidthProperty().bind(textContainer.widthProperty());
            
            clockIcon.setContent("M12 7V12H17M12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21Z");
            clockIcon.setStrokeWidth(1.8);
            clockIcon.setFill(Color.TRANSPARENT);
            clockIcon.setScaleX(0.7);
            clockIcon.setScaleY(0.7);
            
            timeBox.getChildren().addAll(clockIcon, timeLabel);
            timeBox.setAlignment(Pos.CENTER_RIGHT);
            timeBox.setMinWidth(Region.USE_PREF_SIZE);
            
            btnAccept.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8; -fx-cursor: hand;");
            btnAccept.setMinWidth(Region.USE_PREF_SIZE);
            
            rightContainer.getChildren().addAll(timeBox, btnAccept);
            rightContainer.setAlignment(Pos.CENTER_RIGHT);
            rightContainer.setMinWidth(Region.USE_PREF_SIZE);
            rightContainer.setMaxWidth(Region.USE_PREF_SIZE);
        }
        
        private void bindCellWidth(ListView<?> lv) {
            DoubleBinding cellWidth = Bindings.createDoubleBinding(
                () -> Math.max(0.0, lv.getWidth() - 20.0),
                lv.widthProperty()
            );
            prefWidthProperty().bind(cellWidth);
            maxWidthProperty().bind(cellWidth);
        }
        
        private String toHexString(Color color) {
            return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
        }

        @Override
        protected void updateItem(CustomerItem item, boolean empty) {
            super.updateItem(item, empty);
            if (getListView() != null && !prefWidthProperty().isBound()) {
                bindCellWidth(getListView());
            }
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                nameLabel.setTooltip(null);
                issueLabel.setTooltip(null);
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
                
                nameLabel.setTooltip(new Tooltip(name));
                issueLabel.setTooltip(new Tooltip(item.isInterrupted() ? ("⚠ Đang tư vấn dở • " + issue) : issue));
                
                boolean isDarkTheme = getScene() != null && getScene().getRoot().getStyleClass().contains("dark-mode");
                nameLabel.setText(name);
                nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: normal; -fx-text-fill: " + (isDarkTheme ? "#e4e6eb;" : "#1c1e21;"));
                if (!item.isActive() && item.isInterrupted()) {
                    issueLabel.setText("⚠ Đang tư vấn dở • " + issue);
                    issueLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + (isDarkTheme ? "#FFB74D;" : "#E65100;"));
                    btnAccept.setText("Tiếp nhận");
                    btnAccept.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8; -fx-cursor: hand;");
                } else {
                    issueLabel.setText(issue);
                    issueLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + (isDarkTheme ? "#b0b3b8;" : "#888888;"));
                    btnAccept.setText("Nhận");
                    btnAccept.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8; -fx-cursor: hand;");
                }
                avatar.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-weight: bold; -fx-font-size: 15;");
                root.getChildren().addAll(avatar, textContainer);
                
                if (item.isActive()) {
                    // Chấm đỏ nếu có tin nhắn chưa đọc, chấm xanh nếu bình thường
                    dot.setFill(item.hasUnread() ? Color.web("#F44336") : Color.web("#00C853"));
                    root.getChildren().add(dot);
                } else {
                    long waitTimeMillis = System.currentTimeMillis() - item.getJoinedTime();
                    long waitMinutes = waitTimeMillis / 60000;
                    
                    Color iconColor = Color.web("#00C853");
                    if (waitMinutes >= 5 || item.isInterrupted()) iconColor = Color.web("#F44336");
                    else if (waitMinutes >= 2) iconColor = Color.web("#FF9800");
                    
                    clockIcon.setStroke(iconColor);
                    timeLabel.setText(waitMinutes + "p");
                    timeLabel.setStyle("-fx-text-fill: " + toHexString(iconColor) + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                    
                    btnAccept.setOnAction(e -> {
                        StaffContext.getInstance().getNetworkService().acceptCustomer(item.getId());
                    });
                    
                    root.getChildren().add(rightContainer);
                }
                
                setGraphic(root);
                setText(null);
                
                boolean isCurrentSelected = (currentCustomerId != null && currentCustomerId.equals(item.getId())) || (item.isActive() && isSelected());
                if (isCurrentSelected) {
                    setStyle("-fx-background-color: " + (isDarkTheme ? "#2e3033;" : "#e5e5ea;") + " -fx-padding: 0;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        }
    }

    @FXML
    private void showAccountMenu(MouseEvent event) {
        if (btnAccount != null) {
            accountMenu.show(btnAccount, Side.RIGHT, 10, 0);
        }
    }

    @FXML
    private void showSettingsMenu(MouseEvent event) {
        if (btnSettings != null && btnSettings.getScene() != null) {
            SettingsDialog.show(btnSettings.getScene().getWindow());
        }
    }
    
    private void handleLogout() {
        if (refreshTimer != null) refreshTimer.stop();
        StaffContext.getInstance().getNetworkService().disconnect();
        try {
            Stage stage = (Stage) btnAccount.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LCSLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            ThemeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("LiveChat - Đăng nhập nhân viên");
            stage.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
