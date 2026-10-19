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
import com.cmp180.livechat.common.VoiceManager;
import com.cmp180.livechat.client.ui.IncomingCallDialog;
import com.cmp180.livechat.client.ui.VoiceCallDialog;
import com.cmp180.livechat.client.ui.UserProfileDialog;
import com.cmp180.livechat.common.UdpMediaClient;

import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.common.SoundManager;
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
import javafx.scene.control.ButtonType;
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
    @FXML private Button btnQuickReplies;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblIssue;
    @FXML private Label lblStatus;
    @FXML private Circle statusDot;
    @FXML private Button btnEndSession;
    @FXML private Button btnTransferSession;
    @FXML private Button btnCustomerProfile;
    @FXML private Button btnVoiceCall;
    @FXML private Button btnRecordVoice;
    private boolean isRecordingVoice = false;
    private VoiceCallDialog activeCallDialog = null;
    private UdpMediaClient udpMediaClient = null;
    @FXML private HBox inputBar;
    @FXML private HBox recordingBar;
    @FXML private javafx.scene.shape.Circle recordingDot;
    @FXML private Label lblRecordingTimer;
    @FXML private HBox liveWaveContainer;
    @FXML private Button btnCancelRecord;
    @FXML private Button btnFinishRecord;
    private final List<Rectangle> waveBars = new ArrayList<>();
    private Timeline waveVisualizerTimer;
    


    private Timeline typingDebounceTimer;
    private boolean isTypingReported = false;
    
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
        itemProfile.setOnAction(e -> {
            if (btnAccount != null && btnAccount.getScene() != null) {
                String sId = StaffContext.getInstance().getNetworkService().getStaffId();
                String sName = StaffContext.getInstance().getNetworkService().getStaffName();
                UserProfileDialog.showStaffProfile(btnAccount.getScene().getWindow(), sId, sName);
            }
        });
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
        if (btnQuickReplies != null) btnQuickReplies.setDisable(true);
        if (btnCustomerProfile != null) btnCustomerProfile.setDisable(true);
        if (btnVoiceCall != null) btnVoiceCall.setDisable(true);
        if (btnRecordVoice != null) btnRecordVoice.setDisable(true);

        if (txtMessage != null) {
            txtMessage.textProperty().addListener((obs, oldVal, newVal) -> {
                if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;
                if (newVal != null && !newVal.trim().isEmpty()) {
                    if (!isTypingReported) {
                        isTypingReported = true;
                        StaffContext.getInstance().getNetworkService().sendTypingStatus(currentCustomerId, true);
                    }
                    if (typingDebounceTimer != null) {
                        typingDebounceTimer.stop();
                    }
                    typingDebounceTimer = new Timeline(new KeyFrame(Duration.millis(2500), e -> {
                        isTypingReported = false;
                        StaffContext.getInstance().getNetworkService().sendTypingStatus(currentCustomerId, false);
                    }));
                    typingDebounceTimer.play();
                } else {
                    if (isTypingReported) {
                        isTypingReported = false;
                        StaffContext.getInstance().getNetworkService().sendTypingStatus(currentCustomerId, false);
                    }
                    if (typingDebounceTimer != null) {
                        typingDebounceTimer.stop();
                    }
                }
            });

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
        if (btnQuickReplies != null) btnQuickReplies.setDisable(false);
        if (btnCustomerProfile != null) btnCustomerProfile.setDisable(false);
        if (btnEndSession != null) btnEndSession.setDisable(false);
        if (btnTransferSession != null) btnTransferSession.setDisable(false);
        if (btnVoiceCall != null) btnVoiceCall.setDisable(false);
        if (btnRecordVoice != null) btnRecordVoice.setDisable(false);
        
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
    private void handleQuickReplies(ActionEvent event) {
        if (btnQuickReplies == null) return;
        ContextMenu quickMenu = new ContextMenu();

        String staffKey = StaffContext.getInstance().getNetworkService().getStaffId();
        if (staffKey == null || staffKey.isEmpty()) {
            staffKey = StaffContext.getInstance().getNetworkService().getStaffName();
        }

        List<String> userReplies = StaffQuickRepliesManager.getQuickReplies(staffKey);
        if (userReplies.isEmpty()) {
            MenuItem emptyItem = new MenuItem("(Chưa có tin nhắn nhanh cá nhân)");
            emptyItem.setDisable(true);
            quickMenu.getItems().add(emptyItem);
        } else {
            for (String tpl : userReplies) {
                MenuItem item = new MenuItem(tpl);
                item.setOnAction(e -> {
                    String cur = txtMessage.getText();
                    if (cur == null || cur.trim().isEmpty()) {
                        txtMessage.setText(tpl);
                    } else {
                        txtMessage.setText(cur + " " + tpl);
                    }
                    txtMessage.positionCaret(txtMessage.getText().length());
                    txtMessage.requestFocus();
                });
                quickMenu.getItems().add(item);
            }
        }

        quickMenu.getItems().add(new SeparatorMenuItem());

        MenuItem manageItem = new MenuItem("Quản lý tin nhắn nhanh...");
        final String finalStaffKey = staffKey;
        manageItem.setOnAction(e -> {
            Window owner = btnQuickReplies != null && btnQuickReplies.getScene() != null ? btnQuickReplies.getScene().getWindow() : null;
            QuickRepliesDialog.show(owner, finalStaffKey, null);
        });
        quickMenu.getItems().add(manageItem);

        if (ThemeManager.isDarkMode()) {
            quickMenu.getStyleClass().addAll("dark-mode", "dark-menu");
        }
        quickMenu.show(btnQuickReplies, Side.TOP, 0, -5);
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String content = txtMessage.getText().trim();
        if (content.isEmpty() || currentCustomerId == null) {
            return;
        }

        if (content.length() > Protocol.MAX_TEXT_LENGTH) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            if (btnSend != null && btnSend.getScene() != null && btnSend.getScene().getWindow() != null) {
                alert.initOwner(btnSend.getScene().getWindow());
            }
            alert.setTitle("Tin nhắn quá dài");
            alert.setHeaderText("Không thể gửi tin nhắn");
            alert.setContentText("Nội dung tin nhắn vượt quá giới hạn " + Protocol.MAX_TEXT_LENGTH + " ký tự (Hiện tại: " + content.length() + " ký tự). Vui lòng rút ngắn nội dung hoặc gửi bằng tệp đính kèm.");
            ThemeManager.applyTheme(alert);
            alert.showAndWait();
            return;
        }

        if (isTypingReported) {
            isTypingReported = false;
            StaffContext.getInstance().getNetworkService().sendTypingStatus(currentCustomerId, false);
        }
        if (typingDebounceTimer != null) {
            typingDebounceTimer.stop();
        }

        String msgId = UUID.randomUUID().toString();
        String replyId = (replyingTo != null) ? replyingTo.getId() : null;
        String replySender = null;
        String replyText = null;
        if (replyingTo != null) {
            replySender = (replyingTo.getType() == ChatMessage.Type.MY_MSG) ? "Bạn" : replyingTo.getSenderName();
            replyText = replyingTo.isRecalled() 
                    ? "Tin nhắn đã được thu hồi" 
                    : (replyingTo.isFile() 
                        ? ("[" + (replyingTo.isVoice() ? "Tin nhắn thoại" : (replyingTo.isImage() ? "Hình ảnh" : "Tệp")) + "] " + replyingTo.getFileName()) 
                        : replyingTo.getText());
        }

        StaffContext.getInstance().getNetworkService().sendChatMessage(content, currentCustomerId, msgId, replySender, replyText, replyId);
        
        ChatMessage msg = new ChatMessage(msgId, content, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, replyId);
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
            ThemeManager.applyTheme(alert);
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
            ThemeManager.applyTheme(alert);
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
            alert.setContentText("Dung lượng tệp (" + String.format("%.1f MB", file.length() / (1024.0 * 1024.0)) + ") vượt quá giới hạn cho phép là " + (Protocol.MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
            ThemeManager.applyTheme(alert);
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
            if (btnAttachFile != null && btnAttachFile.getScene() != null && btnAttachFile.getScene().getWindow() != null) {
                alert.initOwner(btnAttachFile.getScene().getWindow());
            }
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể đọc tệp");
            alert.setContentText(ex.getMessage());
            ThemeManager.applyTheme(alert);
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
        String replyId = (replyingTo != null) ? replyingTo.getId() : null;
        String replySender = (replyingTo != null) ? (replyingTo.getType() == ChatMessage.Type.MY_MSG ? "chính mình" : replyingTo.getSenderName()) : null;
        String replyText = (replyingTo != null) ? (replyingTo.isRecalled() ? "Tin nhắn đã được thu hồi" : (replyingTo.isFile() ? ("[" + (replyingTo.isVoice() ? "Tin nhắn thoại" : (replyingTo.isImage() ? "Hình ảnh" : "Tệp")) + "] " + replyingTo.getFileName()) : replyingTo.getText())) : null;

        StaffContext.getInstance().getNetworkService().sendFileMessage(fileName, fileSize, fileData, isImage, caption, currentCustomerId, msgId, replySender, replyText, replyId);

        ChatMessage msg = new ChatMessage(msgId, caption, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, replyId, fileName, fileSize, fileData, isImage);
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
                ? ("[" + (msg.isVoice() ? "Tin nhắn thoại" : (msg.isImage() ? "Hình ảnh" : "Tệp")) + "] " + msg.getFileName() + (msg.getText() != null && !msg.getText().isEmpty() ? (" - " + msg.getText()) : ""))
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
        updateQuotesForRecalledMessage(msg.getId(), msg.getText());
        if (replyingTo != null && msg.getId().equals(replyingTo.getId())) {
            handleCancelReply();
        }
        listMessages.refresh();
    }

    private void updateQuotesForRecalledMessage(String recalledId, String recalledText) {
        if (recalledId == null) return;
        for (List<ChatMessage> list : chatHistories.values()) {
            for (ChatMessage m : list) {
                if (recalledId.equals(m.getReplyToId()) || (recalledText != null && !recalledText.isEmpty() && recalledText.equals(m.getReplyToText()))) {
                    m.setReplyToText("Tin nhắn đã được thu hồi");
                }
            }
        }
        for (ChatMessage m : listMessages.getItems()) {
            if (recalledId.equals(m.getReplyToId()) || (recalledText != null && !recalledText.isEmpty() && recalledText.equals(m.getReplyToText()))) {
                m.setReplyToText("Tin nhắn đã được thu hồi");
            }
        }
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
        onChatHistoryReceived(customerId, senderRole, senderName, timestamp, message, messageId, replyToSender, replyToText, null, isRecalled, fileName, fileSize, fileData, isImage);
    }

    @Override
    public void onChatHistoryReceived(String customerId, String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, String replyToId, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
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
            ChatMessage historyMsg = new ChatMessage(messageId, displayText, type, senderName, timestamp, replyToSender, replyToText, replyToId, fileName, fileSize, fileData, isImage);
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
            SoundManager.playNotificationSound();
        });
    }

    @Override
    public void onMessageReceived(String senderName, String senderId, String message) {
        onMessageReceived(senderName, senderId, message, UUID.randomUUID().toString(), null, null);
    }

    @Override
    public void onMessageReceived(String senderName, String senderId, String message, String messageId, String replyToSender, String replyToText) {
        onMessageReceived(senderName, senderId, message, messageId, replyToSender, replyToText, null);
    }

    @Override
    public void onMessageReceived(String senderName, String senderId, String message, String messageId, String replyToSender, String replyToText, String replyToId) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, message, ChatMessage.Type.CUSTOMER_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, replyToId);
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
            SoundManager.playNotificationSound();
        });
    }

    @Override
    public void onFileReceived(String senderName, String senderId, String messageId, String caption, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onFileReceived(senderName, senderId, messageId, caption, replyToSender, replyToText, null, fileName, fileSize, fileData, isImage);
    }

    @Override
    public void onFileReceived(String senderName, String senderId, String messageId, String caption, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, caption, ChatMessage.Type.CUSTOMER_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, replyToId, fileName, fileSize, fileData, isImage);
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
            SoundManager.playNotificationSound();
        });
    }

    @Override
    public void onTypingStatusChanged(String customerId, String senderName, boolean isTyping) {
        Platform.runLater(() -> {
            if (currentCustomerId == null || !currentCustomerId.equals(customerId)) return;
            if (isTyping) {
                lblStatus.setText((senderName != null ? senderName : "Khách hàng") + " đang soạn tin...");
                lblStatus.setStyle("-fx-text-fill: #3b82f6; -fx-font-style: italic;");
            } else {
                lblStatus.setText("Đang trong cuộc trò chuyện");
                lblStatus.setStyle("");
            }
        });
    }

    @Override
    public void onMessageRecalled(String messageId) {
        Platform.runLater(() -> {
            if (messageId == null) return;
            String originalText = null;
            for (List<ChatMessage> list : chatHistories.values()) {
                for (ChatMessage m : list) {
                    if (messageId.equals(m.getId())) {
                        m.setRecalled(true);
                        originalText = m.getText();
                        break;
                    }
                }
            }
            for (ChatMessage m : listMessages.getItems()) {
                if (messageId.equals(m.getId())) {
                    m.setRecalled(true);
                    originalText = m.getText();
                    break;
                }
            }
            updateQuotesForRecalledMessage(messageId, originalText);
            if (replyingTo != null && messageId.equals(replyingTo.getId())) {
                handleCancelReply();
            }
            listMessages.refresh();
        });
    }

    @FXML
    private void handleEndSession(ActionEvent event) {
        if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Kết thúc hỗ trợ");
        alert.setContentText("Bạn có chắc chắn muốn kết thúc phiên hỗ trợ cho khách hàng này không?");
        ButtonType btnYes = new ButtonType("Kết thúc", javafx.scene.control.ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Hủy", javafx.scene.control.ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);

        if (btnEndSession != null && btnEndSession.getScene() != null && btnEndSession.getScene().getWindow() != null) {
            alert.initOwner(btnEndSession.getScene().getWindow());
        }
        ThemeManager.applyTheme(alert);

        alert.showAndWait().ifPresent(res -> {
            if (res == btnYes) {
                StaffContext.getInstance().getNetworkService().endSession(currentCustomerId);
            }
        });
    }

    @FXML
    private void handleTransferSession(ActionEvent event) {
        if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;

        String displayName = lblCurrentClient.getText();
        Window owner = btnTransferSession != null && btnTransferSession.getScene() != null ? btnTransferSession.getScene().getWindow() : null;
        TransferSessionDialog.show(owner, currentCustomerId, displayName, StaffContext.getInstance().getNetworkService());
    }

    @FXML
    private void handleViewCustomerProfile() {
        if (currentCustomerId == null) return;
        Window owner = btnCustomerProfile != null && btnCustomerProfile.getScene() != null ? btnCustomerProfile.getScene().getWindow() : null;
        CustomerProfileDialog.show(owner, currentCustomerId, lblCurrentClient.getText(), newName -> {
            if (newName != null && !newName.trim().isEmpty()) {
                lblCurrentClient.setText(newName.trim());
                for (CustomerItem c : localQueue) {
                    if (c.getId().equals(currentCustomerId)) {
                        String issue = (lblIssue != null && !lblIssue.getText().isEmpty()) ? " " + lblIssue.getText() : "";
                        c.setFullName(newName.trim() + issue);
                        break;
                    }
                }
                listClients.refresh();
            }
        });
    }

    @Override
    public void onSessionEnded(String reason) {
        Platform.runLater(() -> {
            handleCancelReply();
            if (btnAttachImage != null) btnAttachImage.setDisable(true);
            if (btnAttachFile != null) btnAttachFile.setDisable(true);
            if (btnQuickReplies != null) btnQuickReplies.setDisable(true);
            if (btnCustomerProfile != null) btnCustomerProfile.setDisable(true);
            if (btnVoiceCall != null) btnVoiceCall.setDisable(true);
            if (btnRecordVoice != null) btnRecordVoice.setDisable(true);
            if (activeCallDialog != null) { activeCallDialog.closeDirectly(); activeCallDialog = null; }
            IncomingCallDialog.close();
            VoiceManager.getInstance().stopPlayback();
            VoiceManager.getInstance().stopCallAudio();
            stopCallMedia();
            if (isRecordingVoice) { handleCancelRecord(); }
            if (btnEndSession != null) btnEndSession.setDisable(true);
            if (btnTransferSession != null) btnTransferSession.setDisable(true);
            if (currentCustomerId != null) {
                ChatMessage msg = new ChatMessage(reason, ChatMessage.Type.SYSTEM_MSG, "Hệ thống");
                chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(msg);
                listMessages.getItems().add(msg);
                listMessages.scrollTo(listMessages.getItems().size() - 1);
                lblStatus.setText("Phiên đã kết thúc");
                statusDot.setFill(Color.web("#888888"));

                for (CustomerItem c : localQueue) {
                    if (c.getId().equals(currentCustomerId)) {
                        c.setActive(false);
                        break;
                    }
                }
                sortAndRefreshQueue();
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
    

    private void initLiveWaveVisualizer() {
        if (liveWaveContainer == null) return;
        liveWaveContainer.getChildren().clear();
        waveBars.clear();
        boolean dark = ThemeManager.isDarkMode();
        for (int i = 0; i < 14; i++) {
            Rectangle bar = new Rectangle(3.5, 4);
            bar.setArcWidth(2);
            bar.setArcHeight(2);
            bar.setFill(Color.web(dark ? "#f87171" : "#ef4444"));
            waveBars.add(bar);
            liveWaveContainer.getChildren().add(bar);
        }
    }

    private void startWaveVisualizer() {
        stopWaveVisualizer();
        waveVisualizerTimer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            int sec = VoiceManager.getInstance().getRecordingDurationSeconds();
            if (lblRecordingTimer != null) {
                lblRecordingTimer.setText(String.format("%02d:%02d", sec / 60, sec % 60));
            }
            if (recordingDot != null) {
                recordingDot.setOpacity(recordingDot.getOpacity() > 0.5 ? 0.25 : 1.0);
            }
            double vol = VoiceManager.getInstance().getLiveVolume();
            for (int i = 0; i < waveBars.size(); i++) {
                double waveOffset = Math.sin((System.currentTimeMillis() / 150.0) + (i * 0.5));
                double h = Math.max(4, 4 + (vol * 24 * (0.6 + 0.4 * Math.abs(waveOffset))));
                waveBars.get(i).setHeight(h);
            }
        }));
        waveVisualizerTimer.setCycleCount(Timeline.INDEFINITE);
        waveVisualizerTimer.play();
    }

    private void stopWaveVisualizer() {
        if (waveVisualizerTimer != null) {
            waveVisualizerTimer.stop();
            waveVisualizerTimer = null;
        }
    }

    @FXML
    private void handleRecordVoice(ActionEvent event) {
        if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;
        if (!isRecordingVoice) {
            if (!VoiceManager.isMicrophoneAvailable()) {
                showWarningAlert("Không tìm thấy Microphone", "Không tìm thấy thiết bị Microphone khả dụng trên máy tính. Vui lòng kết nối micro hoặc kiểm tra quyền truy cập microphone trong cài đặt Windows.");
                return;
            }
            boolean started = VoiceManager.getInstance().startRecording();
            if (!started) {
                showWarningAlert("Không thể mở Micro", "Không thể khởi động thiết bị thu âm (micro). Vui lòng kiểm tra quyền truy cập micro của ứng dụng.");
                return;
            }
            isRecordingVoice = true;
            btnRecordVoice.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 12; -fx-cursor: hand; -fx-padding: 3 6;");
            if (inputBar != null) {
                inputBar.setVisible(false);
                inputBar.setManaged(false);
            }
            if (recordingBar != null) {
                recordingBar.setVisible(true);
                recordingBar.setManaged(true);
            }
            initLiveWaveVisualizer();
            startWaveVisualizer();
        } else {
            handleFinishRecord();
        }
    }

    @FXML
    private void handleCancelRecord() {
        if (!isRecordingVoice) return;
        isRecordingVoice = false;
        stopWaveVisualizer();
        VoiceManager.getInstance().stopRecording();
        btnRecordVoice.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        if (recordingBar != null) {
            recordingBar.setVisible(false);
            recordingBar.setManaged(false);
        }
        if (inputBar != null) {
            inputBar.setVisible(true);
            inputBar.setManaged(true);
        }
    }

    @FXML
    private void handleFinishRecord() {
        if (!isRecordingVoice) return;
        isRecordingVoice = false;
        stopWaveVisualizer();
        btnRecordVoice.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        if (recordingBar != null) {
            recordingBar.setVisible(false);
            recordingBar.setManaged(false);
        }
        if (inputBar != null) {
            inputBar.setVisible(true);
            inputBar.setManaged(true);
        }

        VoiceManager.RecordingResult res = VoiceManager.getInstance().finishRecording();
        if (res.status == VoiceManager.RecordingResult.Status.TOO_SHORT) {
            showWarningAlert("Thu âm quá ngắn", "Thời gian thu âm quá ngắn (dưới 1 giây). Vui lòng nói lâu hơn để gửi tin nhắn thoại.");
            return;
        }
        if (res.status == VoiceManager.RecordingResult.Status.SILENT_OR_NO_MIC) {
            showWarningAlert("Không phát hiện âm thanh", "Không phát hiện âm thanh từ Microphone! Đoạn thu hoàn toàn im lặng. Vui lòng kiểm tra lại kết nối micro hoặc mức âm lượng microphone trên máy tính.");
            return;
        }
        if (res.status == VoiceManager.RecordingResult.Status.SUCCESS && res.wavData != null) {
            int durationSec = res.durationSeconds;
            String fileName = "voice_" + System.currentTimeMillis() + ".wav";
            String msgId = UUID.randomUUID().toString();
            String replyId = (replyingTo != null) ? replyingTo.getId() : null;
            String replySender = (replyingTo != null) ? (replyingTo.getType() == ChatMessage.Type.MY_MSG ? "Bạn" : replyingTo.getSenderName()) : null;
            String replyText = (replyingTo != null) ? (replyingTo.isRecalled() ? "Tin nhắn đã được thu hồi" : (replyingTo.isFile() ? ("[" + (replyingTo.isVoice() ? "Tin nhắn thoại" : (replyingTo.isImage() ? "Hình ảnh" : "Tệp")) + "] " + replyingTo.getFileName()) : replyingTo.getText())) : null;

            StaffContext.getInstance().getNetworkService().sendVoiceMessage(fileName, res.wavData.length, res.wavData, durationSec, currentCustomerId, msgId, replySender, replyText, replyId);

            ChatMessage msg = new ChatMessage(msgId, "Tin nhắn thoại (" + durationSec + "s)", ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, replyId, fileName, res.wavData.length, res.wavData, false);
            chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(msg);
            listMessages.getItems().add(msg);
            listMessages.scrollTo(listMessages.getItems().size() - 1);
            handleCancelReply();

            for (CustomerItem ci : localQueue) {
                if (ci.getId().equals(currentCustomerId)) {
                    ci.setLastMessageTime(System.currentTimeMillis());
                    sortAndRefreshQueue();
                    break;
                }
            }
        }
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (btnRecordVoice != null && btnRecordVoice.getScene() != null && btnRecordVoice.getScene().getWindow() != null) {
            alert.initOwner(btnRecordVoice.getScene().getWindow());
        }
        ThemeManager.applyTheme(alert);
        alert.showAndWait();
    }

    private synchronized void startCallMedia(String peerId) {
        stopCallMedia();
        try {
            String host = StaffContext.getInstance().getNetworkService().getServerHost();
            int port = StaffContext.getInstance().getNetworkService().getServerPort();
            String myId = StaffContext.getInstance().getNetworkService().getStaffId();
            udpMediaClient = new UdpMediaClient(myId, peerId, host, port);
            udpMediaClient.setVoiceConsumer(chunk -> {
                VoiceManager.getInstance().playCallChunk(chunk);
            });
            udpMediaClient.start();
            System.out.println(String.format("[Staff] Khởi chạy UDP Media: myId=%s, peerId=%s, host=%s:%d", myId, peerId, host, port));
        } catch (Exception e) {
            System.err.println("[Staff] Lỗi khởi động UDP Media: " + e.getMessage());
        }
    }

    private synchronized void stopCallMedia() {
        if (udpMediaClient != null) {
            try {
                udpMediaClient.close();
            } catch (Exception ignored) {}
            udpMediaClient = null;
        }
    }

    @FXML
    private void handleVoiceCall(ActionEvent event) {
        if (currentCustomerId == null || !StaffContext.getInstance().getNetworkService().isConnected()) return;
        if (activeCallDialog != null) return;

        String targetCustId = currentCustomerId;
        String custName = lblCurrentClient.getText().trim();

        StaffContext.getInstance().getNetworkService().sendCallRequest(targetCustId);

        activeCallDialog = new VoiceCallDialog(
            btnVoiceCall.getScene().getWindow(),
            custName,
            true,
            () -> {
                StaffContext.getInstance().getNetworkService().sendCallEnd(targetCustId);
                VoiceManager.getInstance().stopCallAudio();
                stopCallMedia();
                activeCallDialog = null;
            }
        );
        activeCallDialog.show();
    }

    @Override
    public void onIncomingCall(String callerName, String callerId) {
        Platform.runLater(() -> {
            if (activeCallDialog != null) {
                StaffContext.getInstance().getNetworkService().sendCallReject(callerId, "Bận");
                return;
            }

            Window owner = btnVoiceCall != null && btnVoiceCall.getScene() != null ? btnVoiceCall.getScene().getWindow() : null;
            IncomingCallDialog.show(owner, callerName, accepted -> {
                if (accepted) {
                    StaffContext.getInstance().getNetworkService().sendCallAccept(callerId);
                    activeCallDialog = new VoiceCallDialog(
                        owner,
                        callerName,
                        false,
                        () -> {
                            StaffContext.getInstance().getNetworkService().sendCallEnd(callerId);
                            VoiceManager.getInstance().stopCallAudio();
                            stopCallMedia();
                            activeCallDialog = null;
                        }
                    );
                    activeCallDialog.show();
                    activeCallDialog.setConnected();
                    startCallMedia(callerId);
                    VoiceManager.getInstance().startCallAudio(chunk -> {
                        if (udpMediaClient != null) {
                            udpMediaClient.sendVoice(chunk);
                        }
                    });
                } else {
                    StaffContext.getInstance().getNetworkService().sendCallReject(callerId, "Từ chối");
                }
            });
        });
    }

    @Override
    public void onCallAccepted(String peerName) {
        Platform.runLater(() -> {
            if (activeCallDialog != null) {
                activeCallDialog.setConnected();
                if (currentCustomerId != null) {
                    startCallMedia(currentCustomerId);
                    VoiceManager.getInstance().startCallAudio(chunk -> {
                        if (udpMediaClient != null) {
                            udpMediaClient.sendVoice(chunk);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onCallRejected(String reason) {
        Platform.runLater(() -> {
            if (activeCallDialog != null) {
                activeCallDialog.closeDirectly();
                activeCallDialog = null;
                VoiceManager.getInstance().stopCallAudio();
                stopCallMedia();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cuộc gọi");
                alert.setHeaderText(null);
                alert.setContentText("Cuộc gọi đã bị từ chối: " + (reason != null && !reason.isEmpty() ? reason : "Khách hàng bận"));
                if (btnVoiceCall != null && btnVoiceCall.getScene() != null && btnVoiceCall.getScene().getWindow() != null) {
                    alert.initOwner(btnVoiceCall.getScene().getWindow());
                }
                ThemeManager.applyTheme(alert);
                alert.showAndWait();
            }
        });
    }

    @Override
    public void onCallEnded() {
        Platform.runLater(() -> {
            IncomingCallDialog.close();
            if (activeCallDialog != null) {
                activeCallDialog.closeDirectly();
                activeCallDialog = null;
                VoiceManager.getInstance().stopCallAudio();
                stopCallMedia();
            }
        });
    }

    @Override
    public void onCallDataReceived(byte[] audioData) {
        VoiceManager.getInstance().playCallChunk(audioData);
    }

    @Override
    public void onVoiceMessageReceived(String senderName, String senderId, String messageId, String durationText, String replyToSender, String replyToText, byte[] audioData) {
        onVoiceMessageReceived(senderName, senderId, messageId, durationText, replyToSender, replyToText, null, audioData);
    }

    @Override
    public void onVoiceMessageReceived(String senderName, String senderId, String messageId, String durationText, String replyToSender, String replyToText, String replyToId, byte[] audioData) {
        Platform.runLater(() -> {
            String display = (durationText != null && !durationText.isEmpty()) ? ("Tin nhắn thoại (" + durationText + ")") : "Tin nhắn thoại";
            ChatMessage msg = new ChatMessage(messageId, display, ChatMessage.Type.CUSTOMER_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, replyToId, "voice_" + System.currentTimeMillis() + ".wav", audioData != null ? audioData.length : 0, audioData, false);
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
            SoundManager.playNotificationSound();
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
                    if (item.isVoice()) {
                        MenuItem itemPlayVoice = new MenuItem("Nghe tin nhắn thoại");
                        itemPlayVoice.setOnAction(ev -> VoiceManager.getInstance().playWav(item.getFileData(), null));
                        menu.getItems().add(itemPlayVoice);
                        MenuItem itemSaveVoice = new MenuItem("Lưu âm thanh về máy...");
                        itemSaveVoice.setOnAction(ev -> saveFileToDisk(item));
                        menu.getItems().add(itemSaveVoice);
                    } else if (item.isImage()) {
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
                    ThemeManager.applyTheme(alert);
                    alert.showAndWait();
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(getScene().getWindow());
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Không thể lưu tệp");
                    alert.setContentText(ex.getMessage());
                    ThemeManager.applyTheme(alert);
                    alert.showAndWait();
                }
            }
        }

        private HBox createVoiceCard(ChatMessage item, boolean dark, boolean isMine) {
            HBox card = new HBox(10);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(8, 12, 8, 10));
            card.setPrefWidth(220);
            card.setMaxWidth(250);

            String cardBg = isMine 
                    ? "rgba(0, 0, 0, 0.15)" 
                    : (dark ? "rgba(255, 255, 255, 0.08)" : "#f0f2f5");
            card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 12;");

            Button btnPlay = new Button();
            btnPlay.getStyleClass().add("voice-play-btn");
            btnPlay.setPrefSize(34, 34);
            btnPlay.setMinSize(34, 34);
            btnPlay.setCursor(Cursor.HAND);
            btnPlay.setStyle("-fx-background-radius: 17; -fx-background-color: " 
                    + (isMine ? "#ffffff" : ThemeManager.getPrimaryColor()) 
                    + "; -fx-padding: 0; -fx-alignment: center;");

            SVGPath playIcon = new SVGPath();
            playIcon.setContent("M8 5v14l11-7z");
            playIcon.setFill(Color.web(isMine ? ThemeManager.getPrimaryColor() : "#ffffff"));
            playIcon.setScaleX(1.1);
            playIcon.setScaleY(1.1);
            btnPlay.setGraphic(playIcon);

            int pcmBytes = (item.getFileData() != null) ? Math.max(0, item.getFileData().length - 44) : 0;
            int totalSec = Math.max(1, pcmBytes / 32000);
            String durText = String.format("%d:%02d", totalSec / 60, totalSec % 60);

            VBox details = new VBox(4);
            details.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(details, Priority.ALWAYS);

            HBox waveBox = new HBox(3);
            waveBox.setAlignment(Pos.CENTER_LEFT);
            int[] barHeights = {8, 14, 18, 12, 22, 16, 10, 18, 14, 20, 12, 8};
            for (int h : barHeights) {
                Rectangle bar = new Rectangle(3, h);
                bar.setArcWidth(2);
                bar.setArcHeight(2);
                bar.setFill(Color.web(isMine ? "#ffffff" : (dark ? "#9ca3af" : "#64748b")));
                waveBox.getChildren().add(bar);
            }

            Label lblDuration = new Label(durText);
            lblDuration.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            lblDuration.setStyle("-fx-text-fill: " + (isMine ? "rgba(255,255,255,0.9)" : (dark ? "#d1d5db" : "#475569")) + ";");

            details.getChildren().addAll(waveBox, lblDuration);
            card.getChildren().addAll(btnPlay, details);

            btnPlay.setOnAction(e -> {
                if (VoiceManager.getInstance().isPlaying()) {
                    VoiceManager.getInstance().stopPlayback();
                    playIcon.setContent("M8 5v14l11-7z");
                } else {
                    playIcon.setContent("M6 19h4V5H6v14zm8-14v14h4V5h-4z");
                    VoiceManager.getInstance().playWav(item.getFileData(), isPlaying -> {
                        Platform.runLater(() -> {
                            if (!isPlaying) {
                                playIcon.setContent("M8 5v14l11-7z");
                            }
                        });
                    });
                }
            });

            return card;
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

                        boolean isQuoteRecalled = "Tin nhắn đã được thu hồi".equals(item.getReplyToText());
                        if (!isQuoteRecalled && item.getReplyToId() != null) {
                            for (ChatMessage m : listMessages.getItems()) {
                                if (item.getReplyToId().equals(m.getId()) && m.isRecalled()) {
                                    isQuoteRecalled = true;
                                    break;
                                }
                            }
                        }

                        String quoteDisplayText = isQuoteRecalled ? "Tin nhắn đã được thu hồi" : item.getReplyToText();
                        lblQuoteText.setText(quoteDisplayText);
                        quoteBox.getChildren().setAll(lblQuoteSender, lblQuoteText);

                        if (item.getType() == ChatMessage.Type.MY_MSG) {
                            quoteBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.15); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: rgba(255, 255, 255, 0.8); -fx-border-width: 0 0 0 3;");
                            lblQuoteSender.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                            lblQuoteText.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (isQuoteRecalled ? "rgba(255, 255, 255, 0.65)" : "rgba(255, 255, 255, 0.85)") + ";" + (isQuoteRecalled ? " -fx-font-style: italic;" : ""));
                        } else {
                            quoteBox.setStyle(isDarkTheme 
                                ? "-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: " + ThemeManager.getPrimaryColor() + "; -fx-border-width: 0 0 0 3;" 
                                : "-fx-background-color: rgba(0, 0, 0, 0.05); -fx-background-radius: 8; -fx-padding: 4 8 4 8; -fx-border-color: " + ThemeManager.getPrimaryColor() + "; -fx-border-width: 0 0 0 3;" );
                            lblQuoteSender.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + ThemeManager.getPrimaryColor() + ";");
                            lblQuoteText.setStyle(isDarkTheme 
                                ? "-fx-font-size: 11px; -fx-text-fill: " + (isQuoteRecalled ? "#888888" : "#b0b3b8") + ";" + (isQuoteRecalled ? " -fx-font-style: italic;" : "")
                                : "-fx-font-size: 11px; -fx-text-fill: " + (isQuoteRecalled ? "#888888" : "#65676b") + ";" + (isQuoteRecalled ? " -fx-font-style: italic;" : ""));
                        }
                        bubbleBox.getChildren().add(quoteBox);
                    }

                    boolean hasCaption = item.getText() != null && !item.getText().trim().isEmpty();
                    boolean hasReply = item.getReplyToText() != null && !item.getReplyToText().trim().isEmpty();
                    boolean isSoloImage = item.isFile() && item.isImage() && !hasCaption && !hasReply;

                    String textColor;
                    String cellPadding = item.isVoice() ? "3 3 3 3" : (item.isImage() ? "4 4 6 4" : "8 14 8 14");
                    if (item.getType() == ChatMessage.Type.MY_MSG) {
                        textColor = "#ffffff";
                        if (isSoloImage) {
                            bubbleBox.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                        } else {
                            bubbleBox.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-background-radius: 15 15 0 15; -fx-padding: " + cellPadding + ";");
                        }
                    } else {
                        textColor = isDarkTheme ? "#e4e6eb" : "#050505";
                        if (isSoloImage) {
                            bubbleBox.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                        } else {
                            if (isDarkTheme) {
                                bubbleBox.setStyle("-fx-background-color: #242526; -fx-background-radius: 15 15 15 0; -fx-padding: " + cellPadding + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
                            } else {
                                bubbleBox.setStyle("-fx-background-color: white; -fx-background-radius: 15 15 15 0; -fx-padding: " + cellPadding + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                            }
                        }
                    }

                    if (item.isFile()) {
                        if (item.isVoice()) {
                            HBox voiceCard = createVoiceCard(item, isDarkTheme, item.getType() == ChatMessage.Type.MY_MSG);
                            bubbleBox.getChildren().add(voiceCard);
                        } else if (item.isImage()) {
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

                        if (hasCaption && !item.isVoice()) {
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
                issueLabel.setTooltip(new Tooltip(item.isInterrupted() ? ("Đang tư vấn dở • " + issue) : issue));
                
                boolean isDarkTheme = getScene() != null && getScene().getRoot().getStyleClass().contains("dark-mode");
                nameLabel.setText(name);
                nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: normal; -fx-text-fill: " + (isDarkTheme ? "#e4e6eb;" : "#1c1e21;"));
                if (!item.isActive() && item.isInterrupted()) {
                    issueLabel.setText("Đang tư vấn dở • " + issue);
                    issueLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + (isDarkTheme ? "#FFB74D;" : "#E65100;"));
                } else {
                    issueLabel.setText(issue);
                    issueLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + (isDarkTheme ? "#b0b3b8;" : "#888888;"));
                }

                long activeCount = localQueue.stream().filter(CustomerItem::isActive).count();
                boolean isLimitReached = activeCount >= 3;

                if (isLimitReached) {
                    btnAccept.setDisable(true);
                    btnAccept.setText(item.isInterrupted() ? "Tiếp nhận" : "Nhận");
                    btnAccept.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: #E0E0E0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8; -fx-cursor: default;");
                    btnAccept.setTooltip(new Tooltip("Đang phục vụ tối đa 3 khách hàng. Vui lòng kết thúc một phiên trước khi nhận thêm."));
                } else {
                    btnAccept.setDisable(false);
                    btnAccept.setTooltip(null);
                    if (item.isInterrupted()) {
                        btnAccept.setText("Tiếp nhận");
                        btnAccept.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8; -fx-cursor: hand;");
                    } else {
                        btnAccept.setText("Nhận");
                        btnAccept.setStyle("-fx-background-color: " + ThemeManager.getPrimaryColor() + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8; -fx-cursor: hand;");
                    }
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
            boolean wasMaximized = stage.isMaximized();
            boolean wasFullScreen = stage.isFullScreen();
            Scene scene = new Scene(root);
            ThemeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("LiveChat - Đăng nhập nhân viên");
            if (wasFullScreen) {
                stage.setFullScreen(true);
            } else if (wasMaximized) {
                stage.setMaximized(true);
                Platform.runLater(() -> stage.setMaximized(true));
            } else {
                stage.centerOnScreen();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
