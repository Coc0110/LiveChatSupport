package com.cmp180.livechat;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerListCellLayoutTest {

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
    public void testLongTextDoesNotOverflowCell() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[]{false};

        Platform.runLater(() -> {
            try {
                ListView<String> lv = new ListView<>();
                lv.setPrefWidth(300);
                lv.setMaxWidth(300);

                ListCell<String> cell = new ListCell<>() {
                    private HBox root = new HBox(8);
                    private Label avatar = new Label("L");
                    private Label nameLabel = new Label();
                    private Label issueLabel = new Label();
                    private VBox textContainer = new VBox(2);
                    private Button btnAccept = new Button("Tiếp nhận");
                    private VBox rightContainer = new VBox(4);

                    {
                        root.setAlignment(Pos.CENTER_LEFT);
                        root.setPadding(new Insets(8, 10, 8, 10));

                        DoubleBinding cellWidth = Bindings.createDoubleBinding(
                                () -> Math.max(0.0, lv.getWidth() - 20.0),
                                lv.widthProperty()
                        );
                        prefWidthProperty().bind(cellWidth);
                        maxWidthProperty().bind(cellWidth);
                        root.prefWidthProperty().bind(prefWidthProperty());
                        root.maxWidthProperty().bind(maxWidthProperty());

                        avatar.setMinSize(40, 40);
                        avatar.setMaxSize(40, 40);

                        nameLabel.setMinWidth(0);
                        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                        nameLabel.setEllipsisString("...");
                        nameLabel.setWrapText(false);

                        issueLabel.setMinWidth(0);
                        issueLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                        issueLabel.setEllipsisString("...");
                        issueLabel.setWrapText(false);

                        textContainer.setMinWidth(0);
                        textContainer.setFillWidth(true);
                        HBox.setHgrow(textContainer, Priority.ALWAYS);
                        textContainer.getChildren().addAll(nameLabel, issueLabel);

                        nameLabel.maxWidthProperty().bind(textContainer.widthProperty());
                        issueLabel.maxWidthProperty().bind(textContainer.widthProperty());

                        btnAccept.setMinWidth(Region.USE_PREF_SIZE);
                        rightContainer.getChildren().add(btnAccept);
                        rightContainer.setAlignment(Pos.CENTER_RIGHT);
                        rightContainer.setMinWidth(Region.USE_PREF_SIZE);
                        rightContainer.setMaxWidth(Region.USE_PREF_SIZE);

                        root.getChildren().addAll(avatar, textContainer, rightContainer);
                        setGraphic(root);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            nameLabel.setText("Lữ Trịnh Phương Nghi");
                            issueLabel.setText("⚠ Đang tư vấn dở • Tư vấn Mua Laptop & Linh kiện phụ kiện máy tính chính hãng cao cấp");
                        }
                    }
                };

                lv.getItems().add("item1");
                Scene scene = new Scene(lv, 300, 400);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();

                // Trigger layout pass
                lv.applyCss();
                lv.layout();

                // Check cell width
                double expectedMaxWidth = 300 - 20; // 280
                assertTrue(cell.getWidth() <= 300, "Cell width should not exceed ListView width: " + cell.getWidth());
                assertTrue(cell.getMaxWidth() <= expectedMaxWidth + 1, "Cell max width should be bounded: " + cell.getMaxWidth());

                stage.close();
                success[0] = true;
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(success[0], "Layout test should succeed");
    }
}
