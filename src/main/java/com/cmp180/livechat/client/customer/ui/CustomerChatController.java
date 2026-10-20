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
import javafx.scene.paint.ImagePattern;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.common.SoundManager;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import com.cmp180.livechat.client.ui.EmojiPickerPopup;
import com.cmp180.livechat.client.ui.EmojiManager;
import com.cmp180.livechat.client.ui.ThemeManager;
import com.cmp180.livechat.client.ui.SettingsDialog;
import com.cmp180.livechat.client.ui.FilePreviewDialog;
import com.cmp180.livechat.client.ui.ImageViewerDialog;
import com.cmp180.livechat.common.VoiceManager;
import com.cmp180.livechat.client.ui.IncomingCallDialog;
import com.cmp180.livechat.client.ui.VoiceCallDialog;
import com.cmp180.livechat.client.ui.VideoCallDialog;
import com.cmp180.livechat.client.ui.UserProfileDialog;
import com.cmp180.livechat.client.ui.ServerConfig;
import com.cmp180.livechat.common.UdpMediaClient;
import com.cmp180.livechat.common.VideoManager;

import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


public class CustomerChatController implements CustomerNetworkListener {

    @FXML private ListView<ChatMessage> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Button btnEmoji;
    @FXML private Button btnAttachImage;
    @FXML private Button btnAttachFile;
    @FXML private Button btnEndChat;
    @FXML private Button btnVoiceCall;
    @FXML private Button btnVideoCall;
    @FXML private Button btnStaffProfile;
    @FXML private Button btnRecordVoice;
    private boolean isRecordingVoice = false;
    private VoiceCallDialog activeCallDialog = null;
    private VideoCallDialog activeVideoCallDialog = null;
    private UdpMediaClient udpMediaClient = null;
    private boolean isRatingHandled = false;
    @FXML private HBox inputBar;
    @FXML private HBox recordingBar;
    @FXML private javafx.scene.shape.Circle recordingDot;
    @FXML private Label lblRecordingTimer;
    @FXML private HBox liveWaveContainer;
    @FXML private Button btnCancelRecord;
    @FXML private Button btnFinishRecord;
    private final List<Rectangle> waveBars = new ArrayList<>();
    private Timeline waveVisualizerTimer;
    

    @FXML private Label lblTitle;
    @FXML private Label lblStatus;
    @FXML private javafx.scene.shape.Circle statusDot;
    @FXML private HBox typingBar;
    @FXML private Label lblTyping;
    @FXML private javafx.scene.shape.Circle typingDot;

    private Timeline typingDebounceTimer;
    private boolean isTypingReported = false;

    @FXML private HBox replyBar;
    @FXML private ImageView replyImagePreview;
    @FXML private Label lblReplySender;
    @FXML private Label lblReplyText;
    @FXML private Button btnCancelReply;
    private ChatMessage replyingTo = null;

    private String currentStaffId = null;
    private String currentStaffName = null;

    @FXML private Button btnAccount;
    @FXML private Button btnSettings;
    private ContextMenu accountMenu;
    private ContextMenu settingsMenu;


    @FXML
    public void initialize() {
        CustomerContext.getInstance().getNetworkService().removeListenersByType("CustomerChatController");
        CustomerContext.getInstance().getNetworkService().addListener(this);

        // --- Account Menu Setup ---
        accountMenu = new ContextMenu();
        MenuItem itemProfile = new MenuItem("Hồ sơ của bạn");
        itemProfile.setOnAction(e -> {
            if (btnAccount != null && btnAccount.getScene() != null) {
                String cId = CustomerContext.getInstance().getNetworkService().getCustomerId();
                String phone = CustomerContext.getInstance().getNetworkService().getCustomerPhone();
                String name = CustomerContext.getInstance().getNetworkService().getCustomerName();
                UserProfileDialog.showCustomerProfile(btnAccount.getScene().getWindow(), cId, name, phone, newName -> {
                    CustomerContext.getInstance().getNetworkService().setCustomerName(newName);
                });
            }
        });
        MenuItem itemAccSettings = new MenuItem("Cài đặt");
        itemAccSettings.setOnAction(e -> {
            if (btnAccount != null && btnAccount.getScene() != null) {
                SettingsDialog.show(btnAccount.getScene().getWindow());
            }
        });
        MenuItem itemLogout = new MenuItem("Đăng xuất");
        itemLogout.setOnAction(e -> {
            CustomerContext.getInstance().getNetworkService().removeListener(this);
            CustomerContext.getInstance().getNetworkService().disconnect();
            try {
                Stage stage = (Stage) btnAccount.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerLogin.fxml"));
                Parent root = loader.load();
                boolean wasMaximized = stage.isMaximized();
                boolean wasFullScreen = stage.isFullScreen();
                Scene scene = new Scene(root);
                ThemeManager.applyTheme(scene);
                stage.setScene(scene);
                stage.setTitle("LiveChat - Hỗ trợ trực tuyến");
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
        });
        accountMenu.getItems().addAll(itemProfile, itemAccSettings, new SeparatorMenuItem(), itemLogout);

        Platform.runLater(() -> {
            if (btnAccount != null && btnAccount.getScene() != null) {
                ThemeManager.applyTheme(btnAccount.getScene());
                if (btnAccount.getScene().getWindow() != null) {
                    btnAccount.getScene().getWindow().setOnCloseRequest(ev -> {
                        CustomerContext.getInstance().getNetworkService().removeListener(this);
                        CustomerContext.getInstance().getNetworkService().disconnect();
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
                }
            });
        });

        
        listMessages.setCellFactory(param -> new MessageListCell());

        if (btnStaffProfile != null) btnStaffProfile.setDisable(true);
        if (btnVoiceCall != null) btnVoiceCall.setDisable(true);
        if (btnVideoCall != null) btnVideoCall.setDisable(true);
        if (btnRecordVoice != null) btnRecordVoice.setDisable(true);

        if (btnEmoji != null && txtMessage != null) {
            btnEmoji.disableProperty().bind(txtMessage.disableProperty());
            EmojiPickerPopup.attach(btnEmoji, txtMessage);
        }

        if (txtMessage != null) {
            txtMessage.textProperty().addListener((obs, oldVal, newVal) -> {
                if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
                if (newVal != null && !newVal.trim().isEmpty()) {
                    if (!isTypingReported) {
                        isTypingReported = true;
                        CustomerContext.getInstance().getNetworkService().sendTypingStatus(currentStaffId, true);
                    }
                    if (typingDebounceTimer != null) {
                        typingDebounceTimer.stop();
                    }
                    typingDebounceTimer = new Timeline(new KeyFrame(Duration.millis(2500), e -> {
                        isTypingReported = false;
                        CustomerContext.getInstance().getNetworkService().sendTypingStatus(currentStaffId, false);
                    }));
                    typingDebounceTimer.play();
                } else {
                    if (isTypingReported) {
                        isTypingReported = false;
                        CustomerContext.getInstance().getNetworkService().sendTypingStatus(currentStaffId, false);
                    }
                    if (typingDebounceTimer != null) {
                        typingDebounceTimer.stop();
                    }
                }
            });

            txtMessage.setOnKeyPressed(event -> {
                if (new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN).match(event)) {
                    if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
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
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String content = txtMessage.getText().trim();
        if (content.isEmpty() || currentStaffId == null) {
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
            CustomerContext.getInstance().getNetworkService().sendTypingStatus(currentStaffId, false);
        }
        if (typingDebounceTimer != null) {
            typingDebounceTimer.stop();
        }

        String msgId = UUID.randomUUID().toString();
        String replySender = null;
        String replyText = null;
        String replyId = null;
        if (replyingTo != null) {
            replyId = replyingTo.getId();
            replySender = (replyingTo.getType() == ChatMessage.Type.MY_MSG) ? "Bạn" : replyingTo.getSenderName();
            replyText = replyingTo.isRecalled() ? "Tin nhắn đã được thu hồi" : (replyingTo.isFile() 
                    ? ("[" + (replyingTo.isVoice() ? "Tin nhắn thoại" : (replyingTo.isImage() ? "Hình ảnh" : "Tệp")) + "] " + replyingTo.getFileName()) 
                    : replyingTo.getText());
        }

        CustomerContext.getInstance().getNetworkService().sendChatMessage(content, currentStaffId, msgId, replySender, replyText, replyId);
        ChatMessage msg = new ChatMessage(msgId, content, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, replyId);
        listMessages.getItems().add(msg);
        scrollToBottom();
        txtMessage.clear();
        handleCancelReply();
    }

    private void scrollToBottom() {
        if (listMessages == null || listMessages.getItems().isEmpty()) return;
        Platform.runLater(() -> {
            listMessages.scrollTo(listMessages.getItems().size() - 1);
            for (javafx.scene.Node node : listMessages.lookupAll(".scroll-bar")) {
                if (node instanceof javafx.scene.control.ScrollBar) {
                    javafx.scene.control.ScrollBar sb = (javafx.scene.control.ScrollBar) node;
                    if (sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                        sb.setValue(sb.getMax());
                    }
                }
            }
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(60));
            pt.setOnFinished(e -> {
                if (listMessages != null && !listMessages.getItems().isEmpty()) {
                    listMessages.scrollTo(listMessages.getItems().size() - 1);
                    for (javafx.scene.Node node : listMessages.lookupAll(".scroll-bar")) {
                        if (node instanceof javafx.scene.control.ScrollBar) {
                            javafx.scene.control.ScrollBar sb = (javafx.scene.control.ScrollBar) node;
                            if (sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                                sb.setValue(sb.getMax());
                            }
                        }
                    }
                }
            });
            pt.play();
        });
    }

    @FXML
    private void handleAttachImage() {
        if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
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
        if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
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

        CustomerContext.getInstance().getNetworkService().sendFileMessage(fileName, fileSize, fileData, isImage, caption, currentStaffId, msgId, replySender, replyText, replyId);

        ChatMessage msg = new ChatMessage(msgId, caption, ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, replyId, fileName, fileSize, fileData, isImage);
        listMessages.getItems().add(msg);
        scrollToBottom();
        handleCancelReply();
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
                ? ("[" + (msg.isVoice() ? "Tin nhắn thoại" : (msg.isImage() ? "Hình ảnh" : "Tệp")) + "] " + (msg.getText() != null && !msg.getText().isEmpty() ? msg.getText() : msg.getFileName()))
                : msg.getText();
        if (lblReplyText != null) lblReplyText.setText(replyDisplay);

        if (replyImagePreview != null) {
            if (msg.isImage() && msg.getFileData() != null) {
                try {
                    Image thumb = new Image(new ByteArrayInputStream(msg.getFileData()), 80, 80, true, true);
                    replyImagePreview.setImage(thumb);
                    Rectangle clip = new Rectangle(40, 40);
                    clip.setArcWidth(8);
                    clip.setArcHeight(8);
                    replyImagePreview.setClip(clip);
                    replyImagePreview.setVisible(true);
                    replyImagePreview.setManaged(true);
                } catch (Exception ex) {
                    replyImagePreview.setImage(null);
                    replyImagePreview.setVisible(false);
                    replyImagePreview.setManaged(false);
                }
            } else {
                replyImagePreview.setImage(null);
                replyImagePreview.setVisible(false);
                replyImagePreview.setManaged(false);
            }
        }
        txtMessage.requestFocus();
    }

    @FXML
    private void handleCancelReply() {
        this.replyingTo = null;
        if (replyImagePreview != null) {
            replyImagePreview.setImage(null);
            replyImagePreview.setVisible(false);
            replyImagePreview.setManaged(false);
        }
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
        updateQuotesForRecalledMessage(msg.getId(), msg.getText());
        if (replyingTo != null && msg.getId().equals(replyingTo.getId())) {
            handleCancelReply();
        }
        listMessages.refresh();
    }

    private void updateQuotesForRecalledMessage(String recalledId, String recalledText) {
        if (recalledId == null) return;
        for (ChatMessage m : listMessages.getItems()) {
            if (recalledId.equals(m.getReplyToId()) || (recalledText != null && !recalledText.isEmpty() && recalledText.equals(m.getReplyToText()))) {
                m.setReplyToText("Tin nhắn đã được thu hồi");
            }
        }
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
            if (statusDot != null) statusDot.setFill(javafx.scene.paint.Color.web("#eab308"));
            lblStatus.setText("Đang chờ nhân viên kết nối...");
            if (listMessages.getItems().isEmpty() || 
                !listMessages.getItems().get(listMessages.getItems().size() - 1).getText().contains("hàng đợi")) {
                listMessages.getItems().add(new ChatMessage("Bạn đang ở trong hàng đợi. Vui lòng chờ...", ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
            }
        });
    }

    @Override
    public void onReturnedToQueue(String message) {
        Platform.runLater(() -> {
            handleCancelReply();
            if (typingBar != null) { typingBar.setVisible(false); typingBar.setManaged(false); }
            if (currentStaffId != null) {
                currentStaffId = null;
                currentStaffName = null;
                txtMessage.setDisable(true);
                btnSend.setDisable(true);
                if (btnAttachImage != null) btnAttachImage.setDisable(true);
                if (btnAttachFile != null) btnAttachFile.setDisable(true);
                if (btnStaffProfile != null) btnStaffProfile.setDisable(true);
                if (btnEndChat != null) btnEndChat.setDisable(true);
                if (btnVoiceCall != null) btnVoiceCall.setDisable(true);
                if (btnVideoCall != null) btnVideoCall.setDisable(true);
                if (btnRecordVoice != null) btnRecordVoice.setDisable(true);
                if (activeCallDialog != null) { activeCallDialog.closeDirectly(); activeCallDialog = null; }
                if (activeVideoCallDialog != null) { activeVideoCallDialog.closeDirectly(); activeVideoCallDialog = null; }
                IncomingCallDialog.close();
                VoiceManager.getInstance().stopPlayback();
                VoiceManager.getInstance().stopCallAudio();
                VideoManager.getInstance().stopVideoCapture();
                stopCallMedia();
                if (isRecordingVoice) { handleCancelRecord(); }
                if (statusDot != null) statusDot.setFill(javafx.scene.paint.Color.web("#eab308"));
                lblStatus.setText("Đang chờ nhân viên khác tiếp nhận...");
                listMessages.getItems().add(new ChatMessage(message, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
                scrollToBottom();
            }
        });
    }

    @Override
    public void onPairedWithStaff(String staffName, String staffId) {
        Platform.runLater(() -> {
            isRatingHandled = false;
            handleCancelReply();
            currentStaffId = staffId;
            currentStaffName = staffName;
            lblTitle.setText("Hỗ trợ viên: " + staffName);
            lblStatus.setText("Đã kết nối thành công");
            if (statusDot != null) statusDot.setFill(javafx.scene.paint.Color.web("#22c55e"));
            txtMessage.setDisable(false);
            btnSend.setDisable(false);
            if (btnAttachImage != null) btnAttachImage.setDisable(false);
            if (btnAttachFile != null) btnAttachFile.setDisable(false);
            if (btnStaffProfile != null) btnStaffProfile.setDisable(false);
            if (btnEndChat != null) btnEndChat.setDisable(false);
            if (btnVoiceCall != null) btnVoiceCall.setDisable(false);
            if (btnVideoCall != null) btnVideoCall.setDisable(false);
            if (btnRecordVoice != null) btnRecordVoice.setDisable(false);
            listMessages.getItems().add(new ChatMessage("Bạn đang chat với " + staffName, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
            scrollToBottom();
            SoundManager.playNotificationSound();
        });
    }

    @Override
    public void onMessageReceived(String senderName, String message) {
        onMessageReceived(senderName, message, UUID.randomUUID().toString(), null, null);
    }

    @Override
    public void onMessageReceived(String senderName, String message, String messageId, String replyToSender, String replyToText) {
        onMessageReceived(senderName, message, messageId, replyToSender, replyToText, null);
    }

    @Override
    public void onMessageReceived(String senderName, String message, String messageId, String replyToSender, String replyToText, String replyToId) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, message, ChatMessage.Type.STAFF_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, replyToId);
            listMessages.getItems().add(msg);
            scrollToBottom();
            SoundManager.playNotificationSound();
        });
    }

    @Override
    public void onFileReceived(String senderName, String messageId, String caption, String replyToSender, String replyToText, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onFileReceived(senderName, messageId, caption, replyToSender, replyToText, null, fileName, fileSize, fileData, isImage);
    }

    @Override
    public void onFileReceived(String senderName, String messageId, String caption, String replyToSender, String replyToText, String replyToId, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        Platform.runLater(() -> {
            ChatMessage msg = new ChatMessage(messageId, caption, ChatMessage.Type.STAFF_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, replyToId, fileName, fileSize, fileData, isImage);
            listMessages.getItems().add(msg);
            scrollToBottom();
            SoundManager.playNotificationSound();
        });
    }

    @Override
    public void onTypingStatusChanged(String senderName, boolean isTyping) {
        Platform.runLater(() -> {
            if (currentStaffId == null) return;
            if (typingBar != null && lblTyping != null) {
                if (isTyping) {
                    lblTyping.setText((senderName != null ? senderName : "Hỗ trợ viên") + " đang soạn tin...");
                    typingBar.setVisible(true);
                    typingBar.setManaged(true);
                    scrollToBottom();
                } else {
                    typingBar.setVisible(false);
                    typingBar.setManaged(false);
                    scrollToBottom();
                }
            }
        });
    }

    private boolean hasHistorySeparator = false;

    @Override
    public void onChatHistoryReceived(String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        onChatHistoryReceived(senderRole, senderName, timestamp, message, messageId, replyToSender, replyToText, null, isRecalled, fileName, fileSize, fileData, isImage);
    }

    @Override
    public void onChatHistoryReceived(String senderRole, String senderName, long timestamp, String message, String messageId, String replyToSender, String replyToText, String replyToId, boolean isRecalled, String fileName, long fileSize, byte[] fileData, boolean isImage) {
        Platform.runLater(() -> {
            ChatMessage.Type type;
            if ("CUSTOMER".equalsIgnoreCase(senderRole)) {
                type = ChatMessage.Type.MY_MSG;
            } else if ("SYSTEM".equalsIgnoreCase(senderRole)) {
                type = ChatMessage.Type.SYSTEM_MSG;
            } else {
                type = ChatMessage.Type.STAFF_MSG;
            }

            if (!hasHistorySeparator) {
                hasHistorySeparator = true;
                listMessages.getItems().add(new ChatMessage("--- Lịch sử tư vấn trước đó ---", ChatMessage.Type.SYSTEM_MSG, "Hệ thống", timestamp));
            }

            ChatMessage historyMsg = new ChatMessage(messageId, message, type, senderName, timestamp, replyToSender, replyToText, replyToId, fileName, fileSize, fileData, isImage);
            historyMsg.setRecalled(isRecalled);
            listMessages.getItems().add(historyMsg);
            scrollToBottom();
        });
    }

    @Override
    public void onMessageRecalled(String messageId) {
        Platform.runLater(() -> {
            if (messageId == null) return;
            String originalText = null;
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
    private void handleEndChat(ActionEvent event) {
        if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Kết thúc cuộc trò chuyện");
        alert.setContentText("Bạn có chắc chắn muốn kết thúc phiên hỗ trợ này không?");
        ButtonType btnYes = new ButtonType("Kết thúc", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Hủy", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);

        if (btnEndChat != null && btnEndChat.getScene() != null && btnEndChat.getScene().getWindow() != null) {
            alert.initOwner(btnEndChat.getScene().getWindow());
        }
        ThemeManager.applyTheme(alert);

        alert.showAndWait().ifPresent(res -> {
            if (res == btnYes) {
                CustomerContext.getInstance().getNetworkService().endSession(currentStaffId);
            }
        });
    }

    @Override
    public void onSessionEnded(String reason) {
        Platform.runLater(() -> {
            if (isRatingHandled) return;
            isRatingHandled = true;

            handleCancelReply();
            listMessages.getItems().add(new ChatMessage(reason, ChatMessage.Type.SYSTEM_MSG, "Hệ thống"));
            txtMessage.setDisable(true);
            btnSend.setDisable(true);
            if (btnAttachImage != null) btnAttachImage.setDisable(true);
            if (btnAttachFile != null) btnAttachFile.setDisable(true);
            if (btnEndChat != null) btnEndChat.setDisable(true);
            if (btnStaffProfile != null) btnStaffProfile.setDisable(true);
            if (btnVoiceCall != null) btnVoiceCall.setDisable(true);
            if (btnVideoCall != null) btnVideoCall.setDisable(true);
            if (btnRecordVoice != null) btnRecordVoice.setDisable(true);
            if (activeCallDialog != null) { activeCallDialog.closeDirectly(); activeCallDialog = null; }
            if (activeVideoCallDialog != null) { activeVideoCallDialog.closeDirectly(); activeVideoCallDialog = null; }
            IncomingCallDialog.close();
            VoiceManager.getInstance().stopPlayback();
            VoiceManager.getInstance().stopCallAudio();
            VideoManager.getInstance().stopVideoCapture();
            stopCallMedia();
            if (isRecordingVoice) { handleCancelRecord(); }
            if (statusDot != null) statusDot.setFill(javafx.scene.paint.Color.web("#ef4444"));
            if (typingBar != null) { typingBar.setVisible(false); typingBar.setManaged(false); }
            lblStatus.setText("Phiên đã kết thúc. Bạn có thể đăng xuất để tạo phiên mới.");
            currentStaffId = null;
            currentStaffName = null;

            showRatingDialog();
        });
    }

    private void showRatingDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đánh giá chất lượng hỗ trợ (CSAT)");
        dialog.setHeaderText("Bạn cảm thấy thế nào về phiên tư vấn vừa rồi?");

        ToggleGroup group = new ToggleGroup();
        RadioButton rb5 = new RadioButton("⭐⭐⭐⭐⭐  Rất hài lòng (5 sao)");
        RadioButton rb4 = new RadioButton("⭐⭐⭐⭐  Hài lòng (4 sao)");
        RadioButton rb3 = new RadioButton("⭐⭐⭐  Bình thường (3 sao)");
        RadioButton rb2 = new RadioButton("⭐⭐  Chưa hài lòng (2 sao)");
        RadioButton rb1 = new RadioButton("⭐  Rất tệ (1 sao)");

        rb5.setToggleGroup(group);
        rb4.setToggleGroup(group);
        rb3.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb1.setToggleGroup(group);
        rb5.setSelected(true);

        boolean isDark = ThemeManager.isDarkMode();
        String textColor = isDark ? "#e4e6eb" : "#222222";
        String inputBg = isDark ? "#2b2c2e" : "#f8f9fa";
        String inputBorder = isDark ? "#3e4042" : "#e0e0e0";

        for (RadioButton rb : new RadioButton[]{rb5, rb4, rb3, rb2, rb1}) {
            rb.setStyle("-fx-text-fill: " + textColor + "; -fx-cursor: hand; -fx-font-size: 13px;");
        }

        TextField txtFeedback = new TextField();
        txtFeedback.setPromptText("Nhận xét hoặc góp ý thêm (nếu có)...");
        txtFeedback.setPrefHeight(35);
        txtFeedback.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textColor + "; -fx-border-color: " + inputBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10; -fx-font-size: 13px;");

        Label lblPromptRating = new Label("Chọn mức độ hài lòng:");
        lblPromptRating.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblPromptFeedback = new Label("Góp ý thêm:");
        lblPromptFeedback.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getChildren().addAll(
                lblPromptRating,
                rb5, rb4, rb3, rb2, rb1,
                lblPromptFeedback,
                txtFeedback
        );

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(content);
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (btnSend != null && btnSend.getScene() != null && btnSend.getScene().getWindow() != null) {
            dialog.initOwner(btnSend.getScene().getWindow());
        }
        ThemeManager.applyTheme(pane);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int stars = 5;
                if (rb4.isSelected()) stars = 4;
                else if (rb3.isSelected()) stars = 3;
                else if (rb2.isSelected()) stars = 2;
                else if (rb1.isSelected()) stars = 1;

                String feedback = txtFeedback.getText().trim();
                CustomerContext.getInstance().getNetworkService().sendRating(stars, feedback);
            }
        });

        askReturnToHome();
    }

    private void askReturnToHome() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Phiên hỗ trợ kết thúc");
        alert.setHeaderText("Cảm ơn bạn đã sử dụng dịch vụ hỗ trợ!");
        alert.setContentText("Bạn có muốn quay lại màn hình chính để tạo phiên mới hoặc xem lịch sử không?");
        
        ButtonType btnHome = new ButtonType("Về màn hình chính", ButtonBar.ButtonData.YES);
        ButtonType btnStay = new ButtonType("Ở lại xem tin nhắn", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnHome, btnStay);

        if (btnSend != null && btnSend.getScene() != null && btnSend.getScene().getWindow() != null) {
            alert.initOwner(btnSend.getScene().getWindow());
        }
        ThemeManager.applyTheme(alert);

        alert.showAndWait().ifPresent(res -> {
            if (res == btnHome) {
                returnToLoginScreen();
            }
        });
    }

    private void returnToLoginScreen() {
        CustomerContext.getInstance().getNetworkService().removeListener(this);
        CustomerContext.getInstance().getNetworkService().disconnect();
        try {
            Stage stage = (Stage) btnSend.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerLogin.fxml"));
            Parent root = loader.load();
            boolean wasMaximized = stage.isMaximized();
            boolean wasFullScreen = stage.isFullScreen();
            Scene scene = new Scene(root);
            ThemeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("LiveChat - Hỗ trợ trực tuyến");
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

    @Override
    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            lblStatus.setText(errorMessage);
            if (statusDot != null) statusDot.setFill(javafx.scene.paint.Color.web("#ef4444"));
            txtMessage.setDisable(true);
            btnSend.setDisable(true);
            if (btnAttachImage != null) btnAttachImage.setDisable(true);
            if (btnAttachFile != null) btnAttachFile.setDisable(true);
            if (btnEndChat != null) btnEndChat.setDisable(true);
            if (btnVoiceCall != null) btnVoiceCall.setDisable(true);
            if (btnVideoCall != null) btnVideoCall.setDisable(true);

            if (errorMessage != null && errorMessage.contains("đang được kết nối trong một phiên chat khác")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                if (btnSend != null && btnSend.getScene() != null && btnSend.getScene().getWindow() != null) {
                    alert.initOwner(btnSend.getScene().getWindow());
                }
                alert.setTitle("Đăng nhập trùng lặp");
                alert.setHeaderText("Số điện thoại đang trực tuyến");
                alert.setContentText(errorMessage);
                ThemeManager.applyTheme(alert);
                alert.showAndWait();
                returnToLoginScreen();
            }
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
        if (btnSettings != null && btnSettings.getScene() != null) {
            SettingsDialog.show(btnSettings.getScene().getWindow());
        }
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
        if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
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
            String replySender = (replyingTo != null) ? (replyingTo.getType() == ChatMessage.Type.MY_MSG ? "chính mình" : replyingTo.getSenderName()) : null;
            String replyText = (replyingTo != null) ? (replyingTo.isRecalled() ? "Tin nhắn đã được thu hồi" : (replyingTo.isFile() ? ("[" + (replyingTo.isVoice() ? "Tin nhắn thoại" : (replyingTo.isImage() ? "Hình ảnh" : "Tệp")) + "] " + replyingTo.getFileName()) : replyingTo.getText())) : null;

            CustomerContext.getInstance().getNetworkService().sendVoiceMessage(fileName, res.wavData.length, res.wavData, durationSec, currentStaffId, msgId, replySender, replyText, replyId);

            ChatMessage msg = new ChatMessage(msgId, "Tin nhắn thoại (" + durationSec + "s)", ChatMessage.Type.MY_MSG, "Bạn", System.currentTimeMillis(), replySender, replyText, replyId, fileName, res.wavData.length, res.wavData, false);
            listMessages.getItems().add(msg);
            scrollToBottom();
            handleCancelReply();
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
            String host = CustomerContext.getInstance().getNetworkService().getServerHost();
            int port = ServerConfig.getUdpPort();
            String myId = CustomerContext.getInstance().getNetworkService().getCustomerId();
            udpMediaClient = new UdpMediaClient(myId, peerId, host, port);
            udpMediaClient.setVoiceConsumer(chunk -> {
                VoiceManager.getInstance().playCallChunk(chunk);
            });
            udpMediaClient.setVideoConsumer(chunk -> {
                if (activeVideoCallDialog != null) {
                    activeVideoCallDialog.onRemoteFrameReceived(chunk);
                }
            });
            udpMediaClient.start();
            System.out.println(String.format("[Customer] Khởi chạy UDP Media: myId=%s, peerId=%s, host=%s:%d", myId, peerId, host, port));
        } catch (Exception e) {
            System.err.println("[Customer] Lỗi khởi động UDP Media: " + e.getMessage());
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

    private void recordCallEvent(String eventText, String targetStaffId, boolean broadcast) {
        String msgId = UUID.randomUUID().toString();
        ChatMessage msg = new ChatMessage(eventText, ChatMessage.Type.SYSTEM_MSG, "Hệ thống");
        listMessages.getItems().add(msg);
        scrollToBottom();
        if (broadcast && targetStaffId != null && CustomerContext.getInstance().getNetworkService().isConnected()) {
            CustomerContext.getInstance().getNetworkService().sendChatMessage(eventText, targetStaffId, msgId, null, null);
        }
    }

    @FXML
    private void handleViewStaffProfile() {
        if (currentStaffId == null) return;
        Window owner = btnStaffProfile != null && btnStaffProfile.getScene() != null ? btnStaffProfile.getScene().getWindow() : null;
        String name = currentStaffName != null ? currentStaffName : lblTitle.getText().replace("Hỗ trợ viên: ", "").trim();
        UserProfileDialog.showStaffProfileViewOnly(owner, currentStaffId, name);
    }

    @FXML
    private void handleVoiceCall(ActionEvent event) {
        if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
        if (activeCallDialog != null || activeVideoCallDialog != null) return;

        String targetStaffId = currentStaffId;
        String staffName = lblTitle.getText().replace("Hỗ trợ viên: ", "").trim();

        CustomerContext.getInstance().getNetworkService().sendCallRequest(targetStaffId);

        activeCallDialog = new VoiceCallDialog(
            btnVoiceCall.getScene().getWindow(),
            staffName,
            true,
            () -> {
                int sec = (activeCallDialog != null) ? activeCallDialog.getSecondsElapsed() : 0;
                boolean wasConnected = (activeCallDialog != null) && activeCallDialog.isConnected();
                CustomerContext.getInstance().getNetworkService().sendCallEnd(targetStaffId);
                VoiceManager.getInstance().stopCallAudio();
                stopCallMedia();
                activeCallDialog = null;

                if (wasConnected) {
                    String dur = String.format("%02d:%02d", sec / 60, sec % 60);
                    recordCallEvent("📞 Cuộc gọi thoại kết thúc (Thời lượng: " + dur + ")", targetStaffId, true);
                } else {
                    recordCallEvent("📞 Cuộc gọi nhỡ", targetStaffId, true);
                }
            }
        );
        activeCallDialog.show();
    }

    @FXML
    private void handleVideoCall(ActionEvent event) {
        if (currentStaffId == null || !CustomerContext.getInstance().getNetworkService().isConnected()) return;
        if (activeCallDialog != null || activeVideoCallDialog != null) return;

        String targetStaffId = currentStaffId;
        String staffName = lblTitle.getText().replace("Hỗ trợ viên: ", "").trim();
        String customerName = CustomerContext.getInstance().getNetworkService().getCustomerName();

        CustomerContext.getInstance().getNetworkService().sendVideoCallRequest(targetStaffId);

        activeVideoCallDialog = new VideoCallDialog(
            btnVideoCall.getScene().getWindow(),
            staffName,
            true,
            () -> {
                int sec = (activeVideoCallDialog != null) ? activeVideoCallDialog.getSecondsElapsed() : 0;
                boolean wasConnected = (activeVideoCallDialog != null) && activeVideoCallDialog.isConnected();
                CustomerContext.getInstance().getNetworkService().sendCallEnd(targetStaffId);
                VoiceManager.getInstance().stopCallAudio();
                VideoManager.getInstance().stopVideoCapture();
                stopCallMedia();
                activeVideoCallDialog = null;

                if (wasConnected) {
                    String dur = String.format("%02d:%02d", sec / 60, sec % 60);
                    recordCallEvent("📹 Cuộc gọi video kết thúc (Thời lượng: " + dur + ")", targetStaffId, true);
                } else {
                    recordCallEvent("📹 Cuộc gọi video nhỡ", targetStaffId, true);
                }
            }
        );
        activeVideoCallDialog.show();
        VideoManager.getInstance().startVideoCapture(
            customerName,
            localJpeg -> {
                if (activeVideoCallDialog != null) {
                    activeVideoCallDialog.onLocalFrameReceived(localJpeg);
                }
            },
            networkJpeg -> {
                if (udpMediaClient != null) {
                    udpMediaClient.sendVideo(networkJpeg);
                }
            }
        );
    }

    @Override
    public void onIncomingCall(String callerName, String callerId) {
        Platform.runLater(() -> {
            if (activeCallDialog != null || activeVideoCallDialog != null) {
                CustomerContext.getInstance().getNetworkService().sendCallReject(callerId, "Bận");
                return;
            }

            Window owner = btnVoiceCall != null && btnVoiceCall.getScene() != null ? btnVoiceCall.getScene().getWindow() : null;
            IncomingCallDialog.show(owner, callerName, false, accepted -> {
                if (accepted) {
                    CustomerContext.getInstance().getNetworkService().sendCallAccept(callerId);
                    activeCallDialog = new VoiceCallDialog(
                        owner,
                        callerName,
                        false,
                        () -> {
                            int sec = (activeCallDialog != null) ? activeCallDialog.getSecondsElapsed() : 0;
                            boolean wasConnected = (activeCallDialog != null) && activeCallDialog.isConnected();
                            CustomerContext.getInstance().getNetworkService().sendCallEnd(callerId);
                            VoiceManager.getInstance().stopCallAudio();
                            stopCallMedia();
                            activeCallDialog = null;

                            if (wasConnected) {
                                String dur = String.format("%02d:%02d", sec / 60, sec % 60);
                                recordCallEvent("📞 Cuộc gọi thoại kết thúc (Thời lượng: " + dur + ")", callerId, true);
                            } else {
                                recordCallEvent("📞 Cuộc gọi nhỡ", callerId, true);
                            }
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
                    CustomerContext.getInstance().getNetworkService().sendCallReject(callerId, "Từ chối");
                    recordCallEvent("📞 Cuộc gọi thoại • Bị từ chối", callerId, true);
                }
            });
        });
    }

    @Override
    public void onIncomingVideoCall(String callerName, String callerId) {
        Platform.runLater(() -> {
            if (activeCallDialog != null || activeVideoCallDialog != null) {
                CustomerContext.getInstance().getNetworkService().sendCallReject(callerId, "Bận");
                return;
            }

            Window owner = btnVideoCall != null && btnVideoCall.getScene() != null ? btnVideoCall.getScene().getWindow() : null;
            IncomingCallDialog.show(owner, callerName, true, accepted -> {
                if (accepted) {
                    CustomerContext.getInstance().getNetworkService().sendCallAccept(callerId);
                    String custName = CustomerContext.getInstance().getNetworkService().getCustomerName();
                    activeVideoCallDialog = new VideoCallDialog(
                        owner,
                        callerName,
                        false,
                        () -> {
                            int sec = (activeVideoCallDialog != null) ? activeVideoCallDialog.getSecondsElapsed() : 0;
                            boolean wasConnected = (activeVideoCallDialog != null) && activeVideoCallDialog.isConnected();
                            CustomerContext.getInstance().getNetworkService().sendCallEnd(callerId);
                            VoiceManager.getInstance().stopCallAudio();
                            VideoManager.getInstance().stopVideoCapture();
                            stopCallMedia();
                            activeVideoCallDialog = null;

                            if (wasConnected) {
                                String dur = String.format("%02d:%02d", sec / 60, sec % 60);
                                recordCallEvent("📹 Cuộc gọi video kết thúc (Thời lượng: " + dur + ")", callerId, true);
                            } else {
                                recordCallEvent("📹 Cuộc gọi video nhỡ", callerId, true);
                            }
                        }
                    );
                    activeVideoCallDialog.show();
                    activeVideoCallDialog.setConnected();
                    startCallMedia(callerId);
                    VoiceManager.getInstance().startCallAudio(chunk -> {
                        if (udpMediaClient != null) {
                            udpMediaClient.sendVoice(chunk);
                        }
                    });
                    VideoManager.getInstance().startVideoCapture(
                        custName,
                        localJpeg -> {
                            if (activeVideoCallDialog != null) {
                                activeVideoCallDialog.onLocalFrameReceived(localJpeg);
                            }
                        },
                        networkJpeg -> {
                            if (udpMediaClient != null) {
                                udpMediaClient.sendVideo(networkJpeg);
                            }
                        }
                    );
                } else {
                    CustomerContext.getInstance().getNetworkService().sendCallReject(callerId, "Từ chối");
                    recordCallEvent("📹 Cuộc gọi video • Bị từ chối", callerId, true);
                }
            });
        });
    }

    @Override
    public void onCallAccepted(String peerName) {
        Platform.runLater(() -> {
            if (activeCallDialog != null) {
                activeCallDialog.setConnected();
                if (currentStaffId != null) {
                    startCallMedia(currentStaffId);
                    VoiceManager.getInstance().startCallAudio(chunk -> {
                        if (udpMediaClient != null) {
                            udpMediaClient.sendVoice(chunk);
                        }
                    });
                }
            } else if (activeVideoCallDialog != null) {
                activeVideoCallDialog.setConnected();
                if (currentStaffId != null) {
                    startCallMedia(currentStaffId);
                    VoiceManager.getInstance().startCallAudio(chunk -> {
                        if (udpMediaClient != null) {
                            udpMediaClient.sendVoice(chunk);
                        }
                    });
                    String custName = CustomerContext.getInstance().getNetworkService().getCustomerName();
                    VideoManager.getInstance().startVideoCapture(
                        custName,
                        localJpeg -> {
                            if (activeVideoCallDialog != null) {
                                activeVideoCallDialog.onLocalFrameReceived(localJpeg);
                            }
                        },
                        networkJpeg -> {
                            if (udpMediaClient != null) {
                                udpMediaClient.sendVideo(networkJpeg);
                            }
                        }
                    );
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
                alert.setContentText("Cuộc gọi đã bị từ chối: " + (reason != null && !reason.isEmpty() ? reason : "Hỗ trợ viên bận"));
                if (btnVoiceCall != null && btnVoiceCall.getScene() != null && btnVoiceCall.getScene().getWindow() != null) {
                    alert.initOwner(btnVoiceCall.getScene().getWindow());
                }
                ThemeManager.applyTheme(alert);
                alert.showAndWait();
            } else if (activeVideoCallDialog != null) {
                activeVideoCallDialog.closeDirectly();
                activeVideoCallDialog = null;
                VoiceManager.getInstance().stopCallAudio();
                VideoManager.getInstance().stopVideoCapture();
                stopCallMedia();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cuộc gọi video");
                alert.setHeaderText(null);
                alert.setContentText("Cuộc gọi video đã bị từ chối: " + (reason != null && !reason.isEmpty() ? reason : "Hỗ trợ viên bận"));
                if (btnVideoCall != null && btnVideoCall.getScene() != null && btnVideoCall.getScene().getWindow() != null) {
                    alert.initOwner(btnVideoCall.getScene().getWindow());
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
            if (activeVideoCallDialog != null) {
                activeVideoCallDialog.closeDirectly();
                activeVideoCallDialog = null;
                VoiceManager.getInstance().stopCallAudio();
                VideoManager.getInstance().stopVideoCapture();
                stopCallMedia();
            }
        });
    }

    @Override
    public void onCallDataReceived(byte[] audioData) {
        VoiceManager.getInstance().playCallChunk(audioData);
    }

    @Override
    public void onVideoDataReceived(byte[] videoData) {
        if (activeVideoCallDialog != null) {
            activeVideoCallDialog.onRemoteFrameReceived(videoData);
        }
    }

    @Override
    public void onVoiceMessageReceived(String senderName, String messageId, String durationText, String replyToSender, String replyToText, byte[] audioData) {
        onVoiceMessageReceived(senderName, messageId, durationText, replyToSender, replyToText, null, audioData);
    }

    @Override
    public void onVoiceMessageReceived(String senderName, String messageId, String durationText, String replyToSender, String replyToText, String replyToId, byte[] audioData) {
        Platform.runLater(() -> {
            String display = (durationText != null && !durationText.isEmpty()) ? ("Tin nhắn thoại (" + durationText + ")") : "Tin nhắn thoại";
            ChatMessage msg = new ChatMessage(messageId, display, ChatMessage.Type.STAFF_MSG, senderName, System.currentTimeMillis(), replyToSender, replyToText, replyToId, "voice_" + System.currentTimeMillis() + ".wav", audioData != null ? audioData.length : 0, audioData, false);
            listMessages.getItems().add(msg);
            scrollToBottom();
            SoundManager.playNotificationSound();
        });
    }

    // --- Custom Cell ---
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

            boolean canRecall = (currentStaffId != null)
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

        private void renderCallPill(ChatMessage item) {
            actionRow.getChildren().clear();
            bubbleBox.getChildren().clear();
            quoteBox.getChildren().clear();

            boolean isDark = getScene() != null && getScene().getRoot().getStyleClass().contains("dark-mode");
            boolean isCompleted = item.getText().contains("kết thúc") || item.getText().contains("Thời lượng");

            HBox pill = new HBox(8);
            pill.setAlignment(Pos.CENTER);
            pill.setPadding(new Insets(6, 14, 6, 14));

            String bg = isCompleted 
                ? (isDark ? "rgba(16, 185, 129, 0.16)" : "#ecfdf5")
                : (isDark ? "rgba(239, 68, 68, 0.16)" : "#fef2f2");
            String border = isCompleted 
                ? (isDark ? "#059669" : "#a7f3d0")
                : (isDark ? "#dc2626" : "#fecaca");
            String textFill = isCompleted 
                ? (isDark ? "#34d399" : "#047857")
                : (isDark ? "#f87171" : "#b91c1c");

            pill.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 18; -fx-background-radius: 18; -fx-border-width: 1;");

            Label lblCall = new Label(item.getText());
            lblCall.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            lblCall.setStyle("-fx-text-fill: " + textFill + ";");

            Label lblTimePill = new Label(item.getFormattedTime());
            lblTimePill.setFont(Font.font("Segoe UI", 10.5));
            lblTimePill.setStyle("-fx-text-fill: " + (isDark ? "#94a3b8" : "#64748b") + ";");

            pill.getChildren().addAll(lblCall, lblTimePill);

            root.setAlignment(Pos.CENTER);
            root.getChildren().setAll(pill);
            setGraphic(root);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 6px;");
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

                if (item.getText() != null && (item.getText().startsWith("📞") || item.getText().startsWith("📹"))) {
                    renderCallPill(item);
                    return;
                }

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
                        ChatMessage repliedMsg = null;
                        if (!isQuoteRecalled && item.getReplyToId() != null) {
                            for (ChatMessage m : listMessages.getItems()) {
                                if (item.getReplyToId().equals(m.getId())) {
                                    repliedMsg = m;
                                    if (m.isRecalled()) {
                                        isQuoteRecalled = true;
                                    }
                                    break;
                                }
                            }
                        }

                        String quoteDisplayText = isQuoteRecalled ? "Tin nhắn đã được thu hồi" : item.getReplyToText();
                        lblQuoteText.setText(quoteDisplayText);

                        if (!isQuoteRecalled && repliedMsg != null && repliedMsg.isImage() && repliedMsg.getFileData() != null) {
                            HBox quoteInner = new HBox(8);
                            quoteInner.setAlignment(Pos.CENTER_LEFT);

                            VBox textCol = new VBox(2);
                            textCol.setAlignment(Pos.CENTER_LEFT);
                            HBox.setHgrow(textCol, Priority.ALWAYS);
                            textCol.getChildren().addAll(lblQuoteSender, lblQuoteText);

                            ImageView quoteThumb = new ImageView();
                            quoteThumb.setFitWidth(36);
                            quoteThumb.setFitHeight(36);
                            quoteThumb.setPreserveRatio(false);
                            Rectangle clip = new Rectangle(36, 36);
                            clip.setArcWidth(8);
                            clip.setArcHeight(8);
                            quoteThumb.setClip(clip);
                            try {
                                Image thumbImg = new Image(new ByteArrayInputStream(repliedMsg.getFileData()), 72, 72, true, true);
                                quoteThumb.setImage(thumbImg);
                            } catch (Exception ignored) {}

                            final ChatMessage finalReplied = repliedMsg;
                            quoteThumb.setCursor(Cursor.HAND);
                            quoteThumb.setOnMouseClicked(ev -> {
                                ev.consume();
                                ImageViewerDialog.show(getScene().getWindow(), finalReplied.getFileName(), finalReplied.getFileData());
                            });

                            quoteInner.getChildren().addAll(textCol, quoteThumb);
                            quoteBox.getChildren().setAll(quoteInner);
                        } else {
                            quoteBox.getChildren().setAll(lblQuoteSender, lblQuoteText);
                        }

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
}
