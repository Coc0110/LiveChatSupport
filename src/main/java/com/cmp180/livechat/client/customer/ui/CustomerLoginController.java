package com.cmp180.livechat.client.customer.ui;

import com.cmp180.livechat.client.customer.context.CustomerContext;
import com.cmp180.livechat.client.customer.model.CustomerPastMessage;
import com.cmp180.livechat.client.customer.model.CustomerPastSession;
import com.cmp180.livechat.client.customer.network.CustomerNetworkListener;
import com.cmp180.livechat.client.ui.ServerConfig;
import com.cmp180.livechat.client.ui.SettingsDialog;
import com.cmp180.livechat.client.ui.ThemeManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

public class CustomerLoginController implements CustomerNetworkListener {

    private static final Preferences prefs = Preferences.userNodeForPackage(CustomerLoginController.class);
    private static final String PREF_LAST_PHONE = "customer_last_phone";
    private static final String PREF_LAST_NAME = "customer_last_name";

    // Root Container
    @FXML private AnchorPane rootPane;

    // Corner Buttons (Top corners of card)
    @FXML private Button btnBack;
    @FXML private SVGPath svgBack;
    @FXML private Button btnSettings;
    @FXML private SVGPath svgSettings;

    // Frame 1: Nhập số điện thoại
    @FXML private VBox panePhone;
    @FXML private Label lblTitlePhone;
    @FXML private Label lblSubPhone;
    @FXML private Label lblPhonePrompt;
    @FXML private TextField txtPhone;
    @FXML private Label lblErrorPhone;
    @FXML private Button btnCheckPhone;

    // Frame 2A: Khách mới nhập tên
    @FXML private VBox paneNewCustomer;
    @FXML private Label lblTitleNewCustomer;
    @FXML private Label lblSubNewCustomer;
    @FXML private Label lblNamePrompt;
    @FXML private TextField txtNewName;
    @FXML private Label lblErrorNew;
    @FXML private Button btnConfirmNewName;

    // Frame 2B: Khách cũ
    @FXML private VBox paneReturning;
    @FXML private Label lblGreetingReturning;
    @FXML private Label lblWelcomeReturning;
    @FXML private Button btnNewSession;
    @FXML private Button btnHistoryChoice;

    // Frame 3: Chọn chủ đề
    @FXML private VBox paneTopics;
    @FXML private Label lblTitleTopics;
    @FXML private Label lblSubTopics;
    @FXML private Button btnTopic1;
    @FXML private Button btnTopic2;
    @FXML private Button btnTopic3;
    @FXML private Button btnTopic4;
    @FXML private Button btnTopic5;
    @FXML private Label lblErrorTopic;
    @FXML private Button btnConfirmTopic;

    // Frame 4: Lịch sử phiên cũ
    @FXML private VBox paneHistory;
    @FXML private Label lblTitleHistory;
    @FXML private Label lblSubHistory;
    @FXML private Label lblPreviewTitle;
    @FXML private Label lblMessageCount;
    @FXML private ListView<CustomerPastSession> listPastSessions;
    @FXML private ScrollPane scrollPastMessages;
    @FXML private VBox boxPastMessages;
    @FXML private Button btnReopenSession;

    // State
    private String currentPhone = "";
    private String currentCustomerName = "";
    private boolean isExistingCustomer = false;
    private List<CustomerPastSession> pastSessionsList = new ArrayList<>();

    private final String[] TOPIC_NAMES = {
        "Tư vấn Mua Laptop & Build PC",
        "Hỗ trợ Kỹ thuật & Cài đặt",
        "Bảo hành & Đổi trả linh kiện",
        "Tra cứu Đơn hàng & Giao vận",
        "Thắc mắc & Dịch vụ khác"
    };
    private Button[] topicButtons;
    private int selectedTopicIndex = -1;

    // Guard chống spam click tạo phiên
    private boolean isStartingChat = false;

    public static String normalizePhoneNumber(String raw) {
        if (raw == null) return "";
        String clean = raw.replaceAll("[\\s\\-\\.\\(\\)]", "").trim();
        if (clean.startsWith("+84")) {
            clean = "0" + clean.substring(3);
        } else if (clean.startsWith("84") && clean.length() == 11) {
            clean = "0" + clean.substring(2);
        }
        return clean;
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        String clean = normalizePhoneNumber(phone);
        // Định dạng số di động Việt Nam chuẩn: 10 chữ số, bắt đầu bằng 03, 05, 07, 08, 09
        return clean.matches("^0[35789][0-9]{8}$");
    }

    public static boolean isValidCustomerName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 50) return false;
        // Phải chứa ít nhất 1 chữ cái (bao gồm chữ tiếng Việt có dấu)
        boolean hasLetter = trimmed.matches(".*[\\p{L}].*");
        // Không được chứa các ký tự thuần số hoặc thuần dấu đặc biệt
        boolean onlyPunctOrDigits = trimmed.matches("^[0-9\\s\\p{Punct}]+$");
        return hasLetter && !onlyPunctOrDigits;
    }

    @FXML
    public void initialize() {
        CustomerContext.getInstance().getNetworkService().addListener(this);

        // Nạp lại SĐT đã lưu trước đó nếu có
        String savedPhone = prefs.get(PREF_LAST_PHONE, "");
        if (txtPhone != null) {
            // Chặn gõ chữ cái / ký tự đặc biệt, giới hạn tối đa 12 ký tự
            txtPhone.setTextFormatter(new TextFormatter<String>(change -> {
                String newText = change.getControlNewText();
                if (newText.length() > 12) return null;
                if (newText.matches("^[0-9\\+\\s\\-]*$")) {
                    return change;
                }
                return null;
            }));
            // Tự động xóa thông báo lỗi khi khách hàng nhập lại
            txtPhone.textProperty().addListener((obs, oldV, newV) -> {
                if (lblErrorPhone != null) lblErrorPhone.setText("");
            });
            if (!savedPhone.isEmpty()) {
                txtPhone.setText(savedPhone);
            }
            txtPhone.setOnAction(this::handleCheckPhone);
        }

        if (txtNewName != null) {
            // Giới hạn độ dài tên tối đa 50 ký tự
            txtNewName.setTextFormatter(new TextFormatter<String>(change -> {
                if (change.getControlNewText().length() > 50) return null;
                return change;
            }));
            // Tự động xóa thông báo lỗi khi khách hàng nhập lại
            txtNewName.textProperty().addListener((obs, oldV, newV) -> {
                if (lblErrorNew != null) lblErrorNew.setText("");
            });
            txtNewName.setOnAction(this::handleConfirmNewName);
        }

        setupTopicButtons();
        setupReturningButtons();
        setupHistoryListView();

        Platform.runLater(this::applyCurrentTheme);

        ThemeManager.addListener(() -> {
            Platform.runLater(this::applyCurrentTheme);
        });
    }

    private void applyCurrentTheme() {
        boolean dark = ThemeManager.isDarkMode();
        String brandColor = ThemeManager.getPrimaryColor();
        String brandLight = ThemeManager.getLightTint(brandColor);
        String bgTint = dark ? "#141414" : ThemeManager.getBgTint(brandColor);
        String cardBg = dark ? "#1e1e1e" : "#ffffff";
        String cardBorder = dark ? "-fx-border-color: #333538; -fx-border-radius: 15;" : "-fx-border-color: transparent;";
        String cardShadow = dark ? "rgba(0,0,0,0.5), 18, 0, 0, 6" : "rgba(0,0,0,0.08), 15, 0, 0, 5";
        String textMain = dark ? "#e4e6eb" : "#222222";
        String textSub = dark ? "#a0a3a8" : "#666666";
        String inputBg = dark ? "#2b2c2e" : "#ffffff";
        String inputBorder = dark ? "#3e4042" : "#e0e0e0";
        Color arrowColor = dark ? Color.WHITE : Color.web("#1c1e21");
        Color settingsColor = dark ? Color.web("#e4e6eb") : Color.web("#555555");

        // 1. Root background
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + bgTint + ";");
            if (rootPane.getScene() != null) {
                ThemeManager.applyTheme(rootPane.getScene());
            }
        }

        // 2. Corner Buttons SVG strokes
        if (svgSettings != null) {
            svgSettings.setStroke(settingsColor);
        }
        if (svgBack != null) {
            svgBack.setStroke(arrowColor);
        }

        // 3. Card Panels background & shadow
        String baseCardStyle = "-fx-background-color: " + cardBg + "; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, " + cardShadow + "); " + cardBorder;
        if (panePhone != null) panePhone.setStyle(baseCardStyle + "; -fx-padding: 35 25 25 25;");
        if (paneNewCustomer != null) paneNewCustomer.setStyle(baseCardStyle + "; -fx-padding: 35 25 25 25;");
        if (paneReturning != null) paneReturning.setStyle(baseCardStyle + "; -fx-padding: 0;");
        if (paneTopics != null) paneTopics.setStyle(baseCardStyle + "; -fx-padding: 30 20 20 20;");
        if (paneHistory != null) paneHistory.setStyle(baseCardStyle + "; -fx-padding: 24 16 16 16;");

        // 4. Main Action Buttons (brand color background)
        Button[] mainBtns = { btnCheckPhone, btnConfirmNewName, btnConfirmTopic, btnReopenSession };
        for (Button b : mainBtns) {
            if (b != null) {
                b.setStyle("-fx-background-color: " + brandColor + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            }
        }

        // 5. TextFields
        TextField[] fields = { txtPhone, txtNewName };
        for (TextField f : fields) {
            if (f != null) {
                f.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + textMain + "; -fx-border-color: " + inputBorder + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 14; -fx-font-size: 13.5px;");
            }
        }

        // 6. Labels
        Label[] brandLabels = { lblTitlePhone, lblTitleNewCustomer, lblTitleTopics, lblTitleHistory };
        for (Label l : brandLabels) {
            if (l != null) l.setStyle("-fx-text-fill: " + brandColor + ";");
        }
        Label[] subLabels = { lblSubPhone, lblSubNewCustomer, lblGreetingReturning, lblSubTopics, lblSubHistory };
        for (Label l : subLabels) {
            if (l != null) l.setStyle("-fx-text-fill: " + textSub + ";");
        }
        Label[] promptLabels = { lblPhonePrompt, lblNamePrompt };
        for (Label l : promptLabels) {
            if (l != null) l.setStyle("-fx-text-fill: " + (dark ? "#cfd2d6" : "#444444") + ";");
        }
        if (lblWelcomeReturning != null) {
            lblWelcomeReturning.setStyle("-fx-text-fill: " + brandColor + ";");
        }
        if (lblPreviewTitle != null) {
            lblPreviewTitle.setStyle("-fx-text-fill: " + (dark ? "#cfd2d6" : "#475569") + ";");
        }
        if (lblMessageCount != null) {
            lblMessageCount.setStyle("-fx-text-fill: " + textSub + ";");
        }
        if (scrollPastMessages != null) {
            scrollPastMessages.setStyle("-fx-background-color: transparent; -fx-background: " + (dark ? "#18191a" : "#f8fafc") + "; -fx-border-color: " + (dark ? "#374151" : "#e2e8f0") + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        }
        if (listPastSessions != null) {
            listPastSessions.refresh();
        }

        // 7. Update options & topic buttons
        updateTopicStyles();
        setupReturningButtons();
    }

    private void setupTopicButtons() {
        topicButtons = new Button[] { btnTopic1, btnTopic2, btnTopic3, btnTopic4, btnTopic5 };
        selectedTopicIndex = -1;

        for (int i = 0; i < topicButtons.length; i++) {
            if (topicButtons[i] == null) continue;
            final int index = i;
            Button btn = topicButtons[i];

            btn.setOnMouseEntered(e -> {
                if (selectedTopicIndex != index) {
                    boolean dark = ThemeManager.isDarkMode();
                    String brandColor = ThemeManager.getPrimaryColor();
                    String hoverBg = dark ? "#2b2d30" : "#f0f9f9";
                    String hoverBorder = dark ? brandColor : "#80cbc4";
                    btn.setStyle("-fx-background-color: " + hoverBg + "; -fx-border-color: " + hoverBorder + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: " + brandColor + "; -fx-font-weight: normal; -fx-padding: 0 14; -fx-font-size: 13px;");
                }
            });

            btn.setOnMouseExited(e -> {
                if (selectedTopicIndex != index) {
                    updateTopicStyles();
                }
            });

            btn.setOnMouseClicked(e -> {
                selectedTopicIndex = index;
                updateTopicStyles();
                if (lblErrorTopic != null) lblErrorTopic.setText("");

                // Nháy đúp: chọn luôn và chuyển bước tiếp theo
                if (e.getClickCount() == 2) {
                    startChatWithTopic(TOPIC_NAMES[index]);
                }
            });
        }
    }

    private void updateTopicStyles() {
        if (topicButtons == null) return;
        boolean dark = ThemeManager.isDarkMode();
        String brandColor = ThemeManager.getPrimaryColor();
        String brandLight = dark ? "#1a3336" : "#e0f2f1";

        for (int i = 0; i < topicButtons.length; i++) {
            if (topicButtons[i] == null) continue;
            if (i == selectedTopicIndex) {
                topicButtons[i].setStyle("-fx-background-color: " + brandLight + "; -fx-border-color: " + brandColor + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: " + brandColor + "; -fx-font-weight: normal; -fx-padding: 0 14; -fx-font-size: 13px;");
            } else {
                String bg = dark ? "#27282a" : "#fafafa";
                String border = dark ? "#3b3c3e" : "#d0d7de";
                String text = dark ? "#e4e6eb" : "#333333";
                topicButtons[i].setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: " + text + "; -fx-font-weight: normal; -fx-padding: 0 14; -fx-font-size: 13px;");
            }
        }
    }

    private void setupReturningButtons() {
        Button[] retButtons = { btnNewSession, btnHistoryChoice };
        boolean dark = ThemeManager.isDarkMode();
        String brandColor = ThemeManager.getPrimaryColor();
        String bg = dark ? "#27282a" : "#fafafa";
        String border = dark ? "#3b3c3e" : "#d0d7de";
        String text = dark ? "#e4e6eb" : "#333333";

        for (Button btn : retButtons) {
            if (btn == null) continue;
            btn.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: " + text + "; -fx-font-weight: normal; -fx-font-size: 14px;");

            btn.setOnMouseEntered(e -> {
                String hoverBg = dark ? "#2b2d30" : "#f0f9f9";
                String hoverBorder = dark ? brandColor : "#80cbc4";
                btn.setStyle("-fx-background-color: " + hoverBg + "; -fx-border-color: " + hoverBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: " + brandColor + "; -fx-font-weight: normal; -fx-font-size: 14px;");
            });

            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: " + text + "; -fx-font-weight: normal; -fx-font-size: 14px;");
            });
        }
    }

    private void setupHistoryListView() {
        if (listPastSessions == null) return;

        // Double-click to continue selected session
        listPastSessions.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listPastSessions.getSelectionModel().getSelectedItem() != null) {
                handleReopenSelectedSession(null);
            }
        });

        listPastSessions.setCellFactory(lv -> new ListCell<CustomerPastSession>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            private final VBox card = new VBox(3);
            private final HBox topRow = new HBox(6);
            private final HBox botRow = new HBox(6);
            private final Label lblTopic = new Label();
            private final Label lblTime = new Label();
            private final Label lblStaff = new Label();
            private final Label lblRating = new Label();

            {
                card.setPadding(new Insets(6, 10, 6, 10));
                card.setCursor(javafx.scene.Cursor.HAND);

                topRow.setAlignment(Pos.CENTER_LEFT);
                lblTopic.setMaxWidth(185);
                lblTopic.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                lblTime.setStyle("-fx-font-size: 10px;");

                Region spacer1 = new Region();
                HBox.setHgrow(spacer1, Priority.ALWAYS);
                topRow.getChildren().addAll(lblTopic, spacer1, lblTime);

                botRow.setAlignment(Pos.CENTER_LEFT);
                lblStaff.setStyle("-fx-font-size: 11px;");

                Region spacer2 = new Region();
                HBox.setHgrow(spacer2, Priority.ALWAYS);
                botRow.getChildren().addAll(lblStaff, spacer2, lblRating);

                card.getChildren().addAll(topRow, botRow);
            }

            @Override
            protected void updateItem(CustomerPastSession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    lblTopic.setText(item.issueTopic != null ? item.issueTopic : "Tư vấn chung");
                    lblTime.setText(item.createdAt > 0 ? dateFormat.format(new Date(item.createdAt)) : "");
                    lblStaff.setText("CSKH: " + (item.staffName != null && !item.staffName.isEmpty() ? item.staffName : "Chưa chỉ định"));

                    if (item.rating > 0) {
                        StringBuilder stars = new StringBuilder();
                        for (int i = 0; i < item.rating; i++) stars.append("★");
                        lblRating.setText(stars.toString() + " (" + item.rating + "/5)");
                        lblRating.setStyle("-fx-text-fill: #d97706; -fx-font-size: 9.5px; -fx-font-weight: bold; -fx-background-color: rgba(245, 158, 11, 0.15); -fx-background-radius: 8; -fx-padding: 1 5;");
                    } else {
                        lblRating.setText("Chưa đánh giá");
                        lblRating.setStyle("-fx-text-fill: #64748b; -fx-font-size: 9.5px; -fx-background-color: rgba(100, 116, 139, 0.12); -fx-background-radius: 8; -fx-padding: 1 5;");
                    }

                    updateCardStyle(isSelected());
                    setGraphic(card);
                    setText(null);
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (!isEmpty() && getItem() != null) {
                    updateCardStyle(selected);
                }
            }

            private void updateCardStyle(boolean selected) {
                boolean dark = ThemeManager.isDarkMode();
                String brandColor = ThemeManager.getPrimaryColor();
                String brandLight = ThemeManager.getLightTint(brandColor);

                if (selected) {
                    String selBg = dark ? "#1a2c30" : brandLight;
                    card.setStyle("-fx-background-color: " + selBg + "; -fx-border-color: " + brandColor + "; -fx-border-width: 1.5; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 9;");
                    lblTopic.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + brandColor + ";");
                    lblStaff.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (dark ? "#e2e8f0" : "#1e293b") + ";");
                    lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (dark ? "#94a3b8" : "#64748b") + ";");
                } else {
                    String unselBg = dark ? "#262729" : "#f8fafc";
                    String unselBorder = dark ? "#3b3c3e" : "#e2e8f0";
                    card.setStyle("-fx-background-color: " + unselBg + "; -fx-border-color: " + unselBorder + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 9;");
                    lblTopic.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + (dark ? "#f1f5f9" : "#1e293b") + ";");
                    lblStaff.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (dark ? "#94a3b8" : "#64748b") + ";");
                    lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (dark ? "#64748b" : "#94a3b8") + ";");
                }
            }
        });

        if (btnReopenSession != null) {
            btnReopenSession.setDisable(true);
            btnReopenSession.setText("Hỏi tiếp phiên đã chọn");
        }

        listPastSessions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String shortId = newVal.sessionId != null && newVal.sessionId.length() > 8 ? newVal.sessionId.substring(0, 8) : newVal.sessionId;
                showPastMessageLoading("Đang tải tin nhắn phiên #" + shortId + "...");
                CustomerContext.getInstance().getNetworkService().requestSessionMessages(newVal.sessionId);
                if (btnReopenSession != null) {
                    btnReopenSession.setDisable(false);
                    String topic = (newVal.issueTopic != null && !newVal.issueTopic.isEmpty()) ? newVal.issueTopic : "Tư vấn";
                    if (topic.length() > 22) topic = topic.substring(0, 20) + "...";
                    btnReopenSession.setText("Hỏi tiếp: " + topic);
                }
            } else {
                showPastMessagePlaceholder("Chọn một phiên bên trên để xem tin nhắn.");
                if (btnReopenSession != null) {
                    btnReopenSession.setDisable(true);
                    btnReopenSession.setText("Hỏi tiếp phiên đã chọn");
                }
            }
        });
    }

    private void showPastMessageLoading(String text) {
        if (boxPastMessages == null) return;
        boxPastMessages.getChildren().clear();
        if (lblMessageCount != null) lblMessageCount.setText("");

        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(25, 10, 10, 10));

        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 11.5px; -fx-font-family: 'Segoe UI';");
        box.getChildren().add(lbl);

        boxPastMessages.getChildren().add(box);
    }

    private void showPastMessagePlaceholder(String text) {
        if (boxPastMessages == null) return;
        boxPastMessages.getChildren().clear();
        if (lblMessageCount != null) lblMessageCount.setText("");

        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(25, 10, 10, 10));

        SVGPath icon = new SVGPath();
        icon.setContent("M18 10h-1.26A8 8 0 1 0 9 20h9a3 3 0 0 0 0-6h-.18A3 3 0 0 0 18 10z");
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.web("#94a3b8"));
        icon.setStrokeWidth(1.5);

        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11.5px; -fx-font-family: 'Segoe UI';");
        box.getChildren().addAll(icon, lbl);

        boxPastMessages.getChildren().add(box);
    }

    private void showPane(VBox target) {
        VBox[] panes = {panePhone, paneNewCustomer, paneReturning, paneTopics, paneHistory};
        for (VBox p : panes) {
            if (p != null) {
                boolean active = (p == target);
                p.setVisible(active);
                p.setManaged(active);
            }
        }
        if (btnBack != null) {
            boolean canGoBack = (target != panePhone);
            btnBack.setVisible(canGoBack);
        }
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    @FXML
    private void handleOpenSettings(ActionEvent event) {
        if (btnSettings != null && btnSettings.getScene() != null) {
            SettingsDialog.show(btnSettings.getScene().getWindow());
        }
    }

    @FXML
    private void handleGlobalBack(ActionEvent event) {
        isStartingChat = false;
        if (paneNewCustomer.isVisible() || paneReturning.isVisible()) {
            showPane(panePhone);
        } else if (paneTopics.isVisible()) {
            if (isExistingCustomer) {
                showPane(paneReturning);
            } else {
                showPane(paneNewCustomer);
            }
        } else if (paneHistory.isVisible()) {
            showPane(paneReturning);
        }
    }

    @FXML
    private void handleCheckPhone(ActionEvent event) {
        String rawPhone = (txtPhone != null) ? txtPhone.getText() : "";
        String cleanPhone = normalizePhoneNumber(rawPhone);

        if (cleanPhone.isEmpty()) {
            if (lblErrorPhone != null) {
                lblErrorPhone.setText("Vui lòng nhập số điện thoại!");
            }
            return;
        }

        if (!isValidPhoneNumber(cleanPhone)) {
            if (lblErrorPhone != null) {
                lblErrorPhone.setText("SĐT không hợp lệ (cần 10 số, bắt đầu bằng 03, 05, 07, 08, 09)!");
            }
            return;
        }

        if (lblErrorPhone != null) lblErrorPhone.setText("");
        if (btnCheckPhone != null) {
            btnCheckPhone.setDisable(true);
            btnCheckPhone.setText("Đang kiểm tra...");
        }

        this.currentPhone = cleanPhone;
        String host = ServerConfig.getHost();
        int port = ServerConfig.getPort();
        CustomerContext.getInstance().getNetworkService().checkPhone(host, port, cleanPhone);
    }

    @FXML
    private void handleConfirmNewName(ActionEvent event) {
        String name = (txtNewName != null) ? txtNewName.getText().trim() : "";
        if (name.isEmpty()) {
            if (lblErrorNew != null) {
                lblErrorNew.setText("Vui lòng nhập họ và tên của bạn!");
            }
            return;
        }

        if (!isValidCustomerName(name)) {
            if (lblErrorNew != null) {
                lblErrorNew.setText("Tên không hợp lệ (tối thiểu 2 ký tự, không chứa toàn số hoặc ký tự lạ)!");
            }
            return;
        }

        this.currentCustomerName = name;
        resetTopicSelection();
        showPane(paneTopics);
    }

    @FXML
    private void handleGoToTopics(ActionEvent event) {
        resetTopicSelection();
        showPane(paneTopics);
    }

    @FXML
    private void handleGoToHistory(ActionEvent event) {
        showPane(paneHistory);
        if (listPastSessions != null && !listPastSessions.getItems().isEmpty()) {
            listPastSessions.getSelectionModel().select(0);
        } else {
            showPastMessagePlaceholder("Bạn chưa có lịch sử hỗ trợ nào.");
        }
    }

    @FXML
    private void handleConfirmTopic(ActionEvent event) {
        if (selectedTopicIndex < 0 || selectedTopicIndex >= TOPIC_NAMES.length) {
            if (lblErrorTopic != null) {
                lblErrorTopic.setText("Vui lòng chọn một vấn đề cần tư vấn!");
            }
            return;
        }
        startChatWithTopic(TOPIC_NAMES[selectedTopicIndex]);
    }

    private void resetTopicSelection() {
        selectedTopicIndex = -1;
        if (lblErrorTopic != null) lblErrorTopic.setText("");
        updateTopicStyles();
    }

    @FXML
    private void handleReopenSelectedSession(ActionEvent event) {
        CustomerPastSession selected = listPastSessions != null ? listPastSessions.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            if (btnReopenSession != null) btnReopenSession.setDisable(true);
            return;
        }
        startChatWithTopic("Hỏi tiếp: " + selected.issueTopic);
    }

    private void startChatWithTopic(String topic) {
        if (isStartingChat) return;
        isStartingChat = true;

        if (currentCustomerName == null || currentCustomerName.trim().isEmpty()) {
            currentCustomerName = "Khách hàng (" + (currentPhone.length() > 4 ? currentPhone.substring(currentPhone.length() - 4) : currentPhone) + ")";
        }

        prefs.put(PREF_LAST_PHONE, currentPhone);
        prefs.put(PREF_LAST_NAME, currentCustomerName);

        CustomerContext.getInstance().setCustomerPhone(currentPhone);
        CustomerContext.getInstance().setCustomerName(currentCustomerName);

        // Hủy đăng ký listener của màn hình login trước khi chuyển sang chat
        CustomerContext.getInstance().getNetworkService().removeListener(this);

        try {
            Stage stage = (Stage) panePhone.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerChat.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            ThemeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("LiveChat - Hỗ trợ trực tuyến");
            stage.centerOnScreen();

            // Đăng nhập và vào hàng đợi
            String host = ServerConfig.getHost();
            int port = ServerConfig.getPort();
            CustomerContext.getInstance().getNetworkService().connectAndLogin(host, port, currentPhone, currentCustomerName, topic);

        } catch (IOException e) {
            isStartingChat = false;
            e.printStackTrace();
            CustomerContext.getInstance().getNetworkService().addListener(this);
            showPane(paneTopics);
            if (lblErrorPhone != null) {
                lblErrorPhone.setText("Lỗi mở giao diện chat: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // NETWORK LISTENER CALLBACKS
    // ==========================================

    @Override
    public void onPhoneChecked(boolean isExisting, String customerName, List<CustomerPastSession> pastSessions) {
        Platform.runLater(() -> {
            if (btnCheckPhone != null) {
                btnCheckPhone.setDisable(false);
                btnCheckPhone.setText("Tiếp tục");
            }

            this.isExistingCustomer = isExisting;
            this.pastSessionsList = (pastSessions != null) ? pastSessions : new ArrayList<>();

            if (!isExisting) {
                // Khách hàng mới
                this.currentCustomerName = "";
                if (txtNewName != null) txtNewName.clear();
                if (lblErrorNew != null) lblErrorNew.setText("");
                showPane(paneNewCustomer);
                Platform.runLater(() -> {
                    if (txtNewName != null) txtNewName.requestFocus();
                });
            } else {
                // Khách hàng cũ: hiển thị tên sạch, không kèm (sdt), căn giữa
                String cleanName = (customerName != null && !customerName.isEmpty()) ? customerName : "Quý khách";
                if (cleanName.contains("(")) {
                    cleanName = cleanName.substring(0, cleanName.indexOf('(')).trim();
                }
                this.currentCustomerName = cleanName;

                if (lblGreetingReturning != null) {
                    lblGreetingReturning.setText("Chào bạn");
                }
                if (lblWelcomeReturning != null) {
                    lblWelcomeReturning.setText(cleanName);
                    lblWelcomeReturning.setStyle("-fx-text-fill: " + ThemeManager.getPrimaryColor() + ";");
                }
                if (btnHistoryChoice != null) {
                    btnHistoryChoice.setText("Xem lịch sử hỗ trợ (" + pastSessionsList.size() + ")");
                }
                if (listPastSessions != null) {
                    listPastSessions.getItems().setAll(pastSessionsList);
                }
                if (pastSessionsList.isEmpty()) {
                    showPastMessagePlaceholder("Bạn chưa có lịch sử hỗ trợ nào.");
                }
                showPane(paneReturning);
            }
        });
    }

    @Override
    public void onSessionMessagesLoaded(String sessionId, List<CustomerPastMessage> messages) {
        Platform.runLater(() -> {
            if (boxPastMessages == null) return;
            boxPastMessages.getChildren().clear();

            if (messages == null || messages.isEmpty()) {
                showPastMessagePlaceholder("Phiên này chưa có tin nhắn nào trao đổi.");
                if (lblMessageCount != null) lblMessageCount.setText("0 tin nhắn");
                return;
            }

            if (lblMessageCount != null) {
                lblMessageCount.setText(messages.size() + " tin nhắn");
            }

            boolean dark = ThemeManager.isDarkMode();
            String brandColor = ThemeManager.getPrimaryColor();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            for (CustomerPastMessage m : messages) {
                boolean isCustomer = "CUSTOMER".equalsIgnoreCase(m.senderRole);
                String timeStr = sdf.format(new Date(m.timestamp));

                HBox row = new HBox();
                VBox bubbleContainer = new VBox(2);
                bubbleContainer.setMaxWidth(230);

                if (isCustomer) {
                    row.setAlignment(Pos.CENTER_RIGHT);
                    bubbleContainer.setAlignment(Pos.TOP_RIGHT);

                    if (m.isRecalled) {
                        Label recalled = new Label("[Tin nhắn đã thu hồi]");
                        recalled.setStyle("-fx-background-color: transparent; -fx-border-color: #94a3b8; -fx-border-style: dashed; -fx-border-radius: 8; -fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 4 8; -fx-font-size: 10.5px;");
                        bubbleContainer.getChildren().add(recalled);
                    } else {
                        VBox bubble = new VBox(2);
                        bubble.setStyle("-fx-background-color: " + brandColor + "; -fx-background-radius: 10 10 2 10; -fx-padding: 6 9;");

                        if (m.fileName != null && !m.fileName.isEmpty()) {
                            Label fileLabel = new Label("📎 " + m.fileName);
                            fileLabel.setWrapText(true);
                            fileLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 11px;");
                            bubble.getChildren().add(fileLabel);
                        }
                        if (m.content != null && !m.content.isEmpty()) {
                            Label contentLabel = new Label(m.content);
                            contentLabel.setWrapText(true);
                            contentLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11.5px; -fx-font-family: 'Segoe UI';");
                            bubble.getChildren().add(contentLabel);
                        }
                        bubbleContainer.getChildren().add(bubble);
                    }

                    Label timeLabel = new Label(timeStr);
                    timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-padding: 0 2 0 0;");
                    bubbleContainer.getChildren().add(timeLabel);

                } else {
                    // Staff message
                    row.setAlignment(Pos.CENTER_LEFT);
                    bubbleContainer.setAlignment(Pos.TOP_LEFT);

                    String staffName = (m.senderName != null && !m.senderName.isEmpty()) ? m.senderName : "CSKH";
                    Label nameLabel = new Label(staffName);
                    nameLabel.setStyle("-fx-text-fill: " + brandColor + "; -fx-font-weight: bold; -fx-font-size: 9.5px; -fx-padding: 0 0 1 2;");
                    bubbleContainer.getChildren().add(nameLabel);

                    if (m.isRecalled) {
                        Label recalled = new Label("[Tin nhắn đã thu hồi]");
                        recalled.setStyle("-fx-background-color: transparent; -fx-border-color: #94a3b8; -fx-border-style: dashed; -fx-border-radius: 8; -fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 4 8; -fx-font-size: 10.5px;");
                        bubbleContainer.getChildren().add(recalled);
                    } else {
                        VBox bubble = new VBox(2);
                        String staffBg = dark ? "#262729" : "#ffffff";
                        String staffBorder = dark ? "#374151" : "#e2e8f0";
                        String staffText = dark ? "#e2e8f0" : "#1e293b";
                        bubble.setStyle("-fx-background-color: " + staffBg + "; -fx-border-color: " + staffBorder + "; -fx-border-radius: 10 10 10 2; -fx-background-radius: 10 10 10 2; -fx-padding: 6 9; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 2, 0, 0, 1);");

                        if (m.fileName != null && !m.fileName.isEmpty()) {
                            Label fileLabel = new Label("📎 " + m.fileName);
                            fileLabel.setWrapText(true);
                            fileLabel.setStyle("-fx-text-fill: " + staffText + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                            bubble.getChildren().add(fileLabel);
                        }
                        if (m.content != null && !m.content.isEmpty()) {
                            Label contentLabel = new Label(m.content);
                            contentLabel.setWrapText(true);
                            contentLabel.setStyle("-fx-text-fill: " + staffText + "; -fx-font-size: 11.5px; -fx-font-family: 'Segoe UI';");
                            bubble.getChildren().add(contentLabel);
                        }
                        bubbleContainer.getChildren().add(bubble);
                    }

                    Label timeLabel = new Label(timeStr);
                    timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-padding: 0 0 0 2;");
                    bubbleContainer.getChildren().add(timeLabel);
                }

                row.getChildren().add(bubbleContainer);
                boxPastMessages.getChildren().add(row);
            }

            if (scrollPastMessages != null) {
                Platform.runLater(() -> scrollPastMessages.setVvalue(1.0));
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            isStartingChat = false;
            if (btnCheckPhone != null) {
                btnCheckPhone.setDisable(false);
                btnCheckPhone.setText("Tiếp tục");
            }
            if (lblErrorPhone != null) {
                lblErrorPhone.setText(errorMessage);
            }
        });
    }

    @Override
    public void onConnectedToQueue() {}

    @Override
    public void onPairedWithStaff(String staffName, String staffId) {}

    @Override
    public void onMessageReceived(String senderName, String message) {}

    @Override
    public void onSessionEnded(String reason) {}
}
