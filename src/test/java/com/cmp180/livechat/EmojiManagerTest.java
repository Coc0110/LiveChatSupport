package com.cmp180.livechat;

import com.cmp180.livechat.client.ui.EmojiManager;
import com.cmp180.livechat.client.ui.EmojiPickerPopup;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EmojiManagerTest {

    @BeforeAll
    public static void initJavaFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                latch.countDown();
            });
        } catch (IllegalStateException e) {
            Platform.runLater(() -> Platform.setImplicitExit(false));
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testAllPickerEmojisHaveValidImages() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                TextField tf = new TextField();
                EmojiPickerPopup popup = new EmojiPickerPopup(tf);

                String[][] allCategories = new String[][]{
                        new String[]{
                                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
                                "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
                                "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
                                "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "😣", "😖",
                                "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯",
                                "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔",
                                "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦",
                                "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴",
                                "🤢", "🤮", "🤧", "😷", "🤒", "🤕"
                        },
                        new String[]{
                                "👍", "👎", "👌", "✌", "🤞", "🤟", "🤘", "🤙", "👈", "👉",
                                "👆", "👇", "☝", "👋", "🤚", "✋", "🖖", "👏", "🙌", "👐",
                                "🤲", "🤝", "🙏", "✍", "💅", "🤳", "💪", "👂", "👃", "👀",
                                "👅", "👄"
                        },
                        new String[]{
                                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                                "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌",
                                "💋", "💐", "🌹", "🥀", "🌺", "🌸", "🌷", "🌻"
                        },
                        new String[]{
                                "🔥", "✨", "⭐", "🌟", "💫", "💥", "💯", "💢", "🎉", "🎊",
                                "🎁", "🎈", "🏆", "🥇", "🥈", "🥉", "☕", "🍺", "🍻", "🍕",
                                "🍔", "🍟", "🍿", "🚀", "💡", "🔔", "📢", "💬", "💭", "⚠️",
                                "✅", "❌", "❓", "❗", "☀️", "🌙", "⚡", "🍀", "💎", "💰"
                        }
                };

                for (String[] catEmojis : allCategories) {
                    for (String em : catEmojis) {
                        String hex = EmojiManager.toHexCode(em);
                        String path = "/emojis/" + hex + ".png";
                        InputStream is = EmojiManager.class.getResourceAsStream(path);
                        assertNotNull(is, "Missing PNG resource for emoji: " + em + " (hex: " + hex + ")");

                        TextFlow tfFlow = EmojiManager.createEmojiTextFlow("A " + em + " B", "#000000", 14);
                        boolean foundImageView = tfFlow.getChildren().stream().anyMatch(n -> n instanceof ImageView);
                        assertTrue(foundImageView, "Emoji should produce ImageView in createEmojiTextFlow: " + em + " (hex: " + hex + ")");
                    }
                }
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (err.get() != null) {
            throw new AssertionError(err.get());
        }
    }

    @Test
    public void testEmojiInsertionAndCaretTracking() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                javafx.scene.Scene scene = new javafx.scene.Scene(new javafx.scene.layout.VBox());
                TextField tf = new TextField();
                ((javafx.scene.layout.VBox) scene.getRoot()).getChildren().add(tf);

                EmojiPickerPopup picker = new EmojiPickerPopup(tf);

                // Case 1: Initial empty text, insert emoji
                picker.insertEmoji("😀");
                assertEquals("😀", tf.getText());
                assertEquals("😀".length(), tf.getCaretPosition());
                assertEquals("", tf.getSelectedText());

                // Case 2: Append subsequent emoji (should append at end)
                picker.insertEmoji("👍");
                assertEquals("😀👍", tf.getText());
                assertEquals("😀👍".length(), tf.getCaretPosition());
                assertEquals("", tf.getSelectedText());

                // Case 3: Text already has content, insert emoji
                tf.setText("Hello");
                tf.positionCaret(5);
                picker.insertEmoji("🎉");
                assertEquals("Hello🎉", tf.getText());
                assertEquals("Hello🎉".length(), tf.getCaretPosition());

                // Case 4: Selection replacement
                tf.setText("AbcDef");
                tf.selectRange(0, 3); // "Abc"
                picker.insertEmoji("🔥");
                assertEquals("🔥Def", tf.getText());
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (err.get() != null) {
            throw new AssertionError(err.get());
        }
    }
}
