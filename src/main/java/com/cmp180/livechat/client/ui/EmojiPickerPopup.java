package com.cmp180.livechat.client.ui;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;

public class EmojiPickerPopup {

    public static class Category {
        public final String id;
        public final String title;
        public final String iconEmoji;
        public final String[] emojis;

        public Category(String id, String title, String iconEmoji, String[] emojis) {
            this.id = id;
            this.title = title;
            this.iconEmoji = iconEmoji;
            this.emojis = emojis;
        }
    }

    private final Popup popup = new Popup();
    private final VBox root = new VBox(6);
    private final FlowPane emojiPane = new FlowPane();
    private final HBox categoryBar = new HBox(4);
    private final List<Category> categories = new ArrayList<>();
    private Category currentCategory;
    private final TextField targetInput;

    private int lastCaretPosition = -1;
    private int lastSelectionStart = -1;
    private int lastSelectionEnd = -1;

    public EmojiPickerPopup(TextField targetInput) {
        this.targetInput = targetInput;
        initCategories();
        buildUI();
        setupInputTracking();
    }

    private void setupInputTracking() {
        if (targetInput == null) return;

        targetInput.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lastCaretPosition = newVal.intValue();
            }
        });

        targetInput.selectionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getLength() > 0) {
                lastSelectionStart = newVal.getStart();
                lastSelectionEnd = newVal.getEnd();
            } else {
                lastSelectionStart = -1;
                lastSelectionEnd = -1;
            }
        });

        targetInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && targetInput.getCaretPosition() >= 0) {
                lastCaretPosition = targetInput.getCaretPosition();
                IndexRange sel = targetInput.getSelection();
                if (sel != null && sel.getLength() > 0) {
                    lastSelectionStart = sel.getStart();
                    lastSelectionEnd = sel.getEnd();
                }
            }
        });
    }

    private void initCategories() {
        categories.add(new Category("smileys", "Cảm xúc", "😀", new String[]{
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
                "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
                "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
                "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "😣", "😖",
                "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯",
                "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔",
                "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦",
                "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴",
                "🤢", "🤮", "🤧", "😷", "🤒", "🤕"
        }));

        categories.add(new Category("gestures", "Cử chỉ", "👍", new String[]{
                "👍", "👎", "👌", "✌", "🤞", "🤟", "🤘", "🤙", "👈", "👉",
                "👆", "👇", "☝", "👋", "🤚", "✋", "🖖", "👏", "🙌", "👐",
                "🤲", "🤝", "🙏", "✍", "💅", "🤳", "💪", "👂", "👃", "👀",
                "👅", "👄"
        }));

        categories.add(new Category("hearts", "Yêu thương", "❤️", new String[]{
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌",
                "💋", "💐", "🌹", "🥀", "🌺", "🌸", "🌷", "🌻"
        }));

        categories.add(new Category("symbols", "Biểu tượng", "🎉", new String[]{
                "🔥", "✨", "⭐", "🌟", "💫", "💥", "💯", "💢", "🎉", "🎊",
                "🎁", "🎈", "🏆", "🥇", "🥈", "🥉", "☕", "🍺", "🍻", "🍕",
                "🍔", "🍟", "🍿", "🚀", "💡", "🔔", "📢", "💬", "💭", "⚠️",
                "✅", "❌", "❓", "❗", "☀️", "🌙", "⚡", "🍀", "💎", "💰"
        }));
    }

    private void buildUI() {
        popup.setAutoHide(true);

        root.setFocusTraversable(false);
        categoryBar.setFocusTraversable(false);
        emojiPane.setFocusTraversable(false);

        root.setPrefWidth(350);
        root.setMaxWidth(350);
        root.setPrefHeight(260);
        root.setPadding(new Insets(8));

        // Category Bar
        categoryBar.setAlignment(Pos.CENTER_LEFT);
        categoryBar.setPadding(new Insets(0, 0, 4, 0));

        // Emoji grid
        emojiPane.setHgap(4);
        emojiPane.setVgap(4);
        emojiPane.setAlignment(Pos.TOP_LEFT);
        emojiPane.setPadding(new Insets(4));

        ScrollPane scrollPane = new ScrollPane(emojiPane);
        scrollPane.setFocusTraversable(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Build category buttons
        boolean first = true;
        for (Category cat : categories) {
            Button catBtn = new Button(cat.title);
            catBtn.setFocusTraversable(false);
            Node iconNode = EmojiManager.getEmojiNode(cat.iconEmoji, 15);
            catBtn.setGraphic(iconNode);
            catBtn.setGraphicTextGap(5);
            catBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 6; -fx-background-radius: 8;");
            
            catBtn.setOnAction(e -> {
                selectCategory(cat);
                highlightCatButton(catBtn);
            });
            categoryBar.getChildren().add(catBtn);

            if (first) {
                first = false;
                currentCategory = cat;
                highlightCatButton(catBtn);
            }
        }

        root.getChildren().addAll(categoryBar, scrollPane);
        popup.getContent().add(root);

        loadEmojisForCategory(currentCategory);
    }

    private void highlightCatButton(Button activeBtn) {
        for (Node n : categoryBar.getChildren()) {
            if (n instanceof Button) {
                ((Button) n).setStyle("-fx-background-color: transparent; -fx-text-fill: #72808e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 6; -fx-background-radius: 8;");
            }
        }
        String brand = ThemeManager.getPrimaryColor();
        String brandLight = ThemeManager.getLightTint(brand);
        activeBtn.setStyle("-fx-background-color: " + brandLight + "; -fx-text-fill: " + brand + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 6; -fx-background-radius: 8;");
    }

    private void selectCategory(Category cat) {
        currentCategory = cat;
        loadEmojisForCategory(cat);
    }

    private void loadEmojisForCategory(Category cat) {
        emojiPane.getChildren().clear();
        if (cat == null || cat.emojis == null) return;

        for (String emoji : cat.emojis) {
            Button btn = new Button();
            btn.setFocusTraversable(false);
            Node emojiNode = EmojiManager.getEmojiNode(emoji, 22);
            btn.setGraphic(emojiNode);
            btn.setMinSize(34, 34);
            btn.setPrefSize(34, 34);
            btn.setMaxSize(34, 34);
            btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-background-radius: 6;");

            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(0, 131, 143, 0.15); -fx-cursor: hand; -fx-padding: 0; -fx-background-radius: 6;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-background-radius: 6;"));

            btn.setOnAction(e -> insertEmoji(emoji));

            emojiPane.getChildren().add(btn);
        }
    }

    public void insertEmoji(String emoji) {
        if (targetInput == null || emoji == null) return;

        String text = targetInput.getText() != null ? targetInput.getText() : "";
        int insertStart;
        int insertEnd;

        if (lastSelectionStart >= 0 && lastSelectionEnd > lastSelectionStart && lastSelectionEnd <= text.length()) {
            insertStart = lastSelectionStart;
            insertEnd = lastSelectionEnd;
        } else {
            int pos = (lastCaretPosition >= 0 && lastCaretPosition <= text.length())
                    ? lastCaretPosition
                    : (targetInput.getCaretPosition() >= 0 && targetInput.getCaretPosition() <= text.length())
                    ? targetInput.getCaretPosition()
                    : text.length();
            insertStart = pos;
            insertEnd = pos;
        }

        String updated = text.substring(0, insertStart) + emoji + text.substring(insertEnd);
        int newCaret = insertStart + emoji.length();

        targetInput.setText(updated);
        lastCaretPosition = newCaret;
        lastSelectionStart = -1;
        lastSelectionEnd = -1;

        if (!targetInput.isFocused()) {
            targetInput.requestFocus();
        }
        targetInput.deselect();
        targetInput.positionCaret(newCaret);
        Platform.runLater(() -> {
            targetInput.deselect();
            targetInput.positionCaret(newCaret);
            lastCaretPosition = newCaret;
            lastSelectionStart = -1;
            lastSelectionEnd = -1;
        });
    }

    public void showOrHide(Button anchorBtn) {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }

        if (targetInput != null && targetInput.isFocused()) {
            lastCaretPosition = targetInput.getCaretPosition();
            IndexRange sel = targetInput.getSelection();
            if (sel != null && sel.getLength() > 0) {
                lastSelectionStart = sel.getStart();
                lastSelectionEnd = sel.getEnd();
            }
        }

        boolean isDark = anchorBtn.getScene() != null && anchorBtn.getScene().getRoot().getStyleClass().contains("dark-mode");
        if (isDark) {
            root.setStyle("-fx-background-color: #242526; -fx-border-color: #393a3b; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 5);");
        } else {
            root.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d4dc; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 16, 0, 0, 5);");
        }

        Bounds bounds = anchorBtn.localToScreen(anchorBtn.getBoundsInLocal());
        if (bounds != null) {
            double x = bounds.getMaxX() - 350;
            double y = bounds.getMinY() - 270;
            popup.show(anchorBtn, x, y);
        }
    }

    public static void attach(Button btnEmoji, TextField txtMessage) {
        btnEmoji.setFocusTraversable(false);
        EmojiPickerPopup picker = new EmojiPickerPopup(txtMessage);
        btnEmoji.setOnAction(e -> picker.showOrHide(btnEmoji));
    }
}
