package com.cmp180.livechat;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class FxmlLoadingTest {

    @BeforeAll
    public static void initJavaFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                latch.countDown();
            });
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testCustomerChatFxmlLoadsSuccessfully() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                URL resource = getClass().getResource("/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml");
                assertNotNull(resource, "CustomerChat.fxml resource should not be null");
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                assertNotNull(root, "Loaded root should not be null");
                assertNotNull(loader.getController(), "Controller should not be null");
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            errorRef.get().printStackTrace();
            fail("CustomerChat.fxml failed to load: " + errorRef.get().getMessage());
        }
    }

    @Test
    public void testCustomerLoginFxmlLoadsSuccessfully() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                URL resource = getClass().getResource("/com/cmp180/livechat/client/customer/ui/CustomerLogin.fxml");
                assertNotNull(resource, "CustomerLogin.fxml resource should not be null");
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                assertNotNull(root, "Loaded root should not be null");
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            errorRef.get().printStackTrace();
            fail("CustomerLogin.fxml failed to load: " + errorRef.get().getMessage());
        }
    }

    @Test
    public void testLCSChatFxmlLoadsSuccessfully() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                URL resource = getClass().getResource("/com/cmp180/livechat/client/staff/ui/LCSChat.fxml");
                assertNotNull(resource, "LCSChat.fxml resource should not be null");
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                assertNotNull(root, "Loaded root should not be null");
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            errorRef.get().printStackTrace();
            fail("LCSChat.fxml failed to load: " + errorRef.get().getMessage());
        }
    }

    @Test
    public void testLCSLoginFxmlLoadsSuccessfully() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                URL resource = getClass().getResource("/com/cmp180/livechat/client/staff/ui/LCSLogin.fxml");
                assertNotNull(resource, "LCSLogin.fxml resource should not be null");
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                assertNotNull(root, "Loaded root should not be null");
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            errorRef.get().printStackTrace();
            fail("LCSLogin.fxml failed to load: " + errorRef.get().getMessage());
        }
    }
}
