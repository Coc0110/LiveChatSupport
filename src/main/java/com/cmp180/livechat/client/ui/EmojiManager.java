package com.cmp180.livechat.client.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiManager {

    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public static String toHexCode(String emoji) {
        if (emoji == null || emoji.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < emoji.length(); ) {
            int codePoint = emoji.codePointAt(i);
            if (codePoint != 0xFE0F) { // Bỏ qua Variation Selector 16
                if (sb.length() > 0) sb.append("-");
                sb.append(Integer.toHexString(codePoint).toLowerCase());
            }
            i += Character.charCount(codePoint);
        }
        return sb.toString();
    }

    public static Image getEmojiImage(String emoji) {
        String hex = toHexCode(emoji);
        if (hex.isEmpty()) return null;
        return imageCache.computeIfAbsent(hex, h -> {
            String path = "/emojis/" + h + ".png";
            InputStream is = EmojiManager.class.getResourceAsStream(path);
            if (is != null) {
                return new Image(is);
            }
            return null;
        });
    }

    public static Node getEmojiNode(String emoji, double size) {
        Image img = getEmojiImage(emoji);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        }
        Label lbl = new Label(emoji);
        lbl.setStyle("-fx-font-size: " + (size * 0.8) + "px; -fx-font-family: 'Segoe UI Emoji', sans-serif;");
        return lbl;
    }

    public static TextFlow createEmojiTextFlow(String text, String textColorHex, double fontSize) {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(350);
        flow.setLineSpacing(3);

        if (text == null || text.isEmpty()) {
            return flow;
        }

        StringBuilder textBuf = new StringBuilder();
        int i = 0;
        int len = text.length();

        while (i < len) {
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);

            String candidate = new String(Character.toChars(cp));
            if (i + charCount < len && text.codePointAt(i + charCount) == 0xFE0F) {
                candidate += new String(Character.toChars(0xFE0F));
            }

            Image emojiImg = getEmojiImage(candidate);
            if (emojiImg != null) {
                if (textBuf.length() > 0) {
                    Text t = new Text(textBuf.toString());
                    t.setStyle("-fx-fill: " + textColorHex + "; -fx-font-size: " + fontSize + "px; -fx-font-family: 'Segoe UI';");
                    flow.getChildren().add(t);
                    textBuf.setLength(0);
                }

                ImageView iv = new ImageView(emojiImg);
                double emojiSize = fontSize + 4; // ví dụ 18px cho cỡ chữ 14px
                iv.setFitWidth(emojiSize);
                iv.setFitHeight(emojiSize);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                flow.getChildren().add(iv);

                i += candidate.length();
            } else {
                textBuf.append(Character.toChars(cp));
                i += charCount;
            }
        }

        if (textBuf.length() > 0) {
            Text t = new Text(textBuf.toString());
            t.setStyle("-fx-fill: " + textColorHex + "; -fx-font-size: " + fontSize + "px; -fx-font-family: 'Segoe UI';");
            flow.getChildren().add(t);
        }

        return flow;
    }
}
