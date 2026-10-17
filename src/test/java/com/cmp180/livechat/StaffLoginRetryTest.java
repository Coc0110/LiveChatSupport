package com.cmp180.livechat;

import com.cmp180.livechat.client.staff.model.CustomerItem;
import com.cmp180.livechat.client.staff.network.StaffNetworkListener;
import com.cmp180.livechat.client.staff.network.StaffNetworkService;
import com.cmp180.livechat.common.Message;
import com.cmp180.livechat.common.Protocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class StaffLoginRetryTest {

    private ServerSocket serverSocket;
    private int port;
    private volatile boolean serverRunning = true;
    private Thread serverThread;

    @BeforeEach
    public void startMockServer() throws Exception {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        serverRunning = true;

        serverThread = new Thread(() -> {
            try {
                while (serverRunning) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                            oos.flush();
                            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

                            Message msg;
                            while ((msg = (Message) ois.readObject()) != null) {
                                if (Protocol.CMD_STAFF_LOGIN.equals(msg.getCommand())) {
                                    String user = msg.getSenderId();
                                    String pass = msg.getContent();
                                    if ("admin".equals(user) && "correct123".equals(pass)) {
                                        oos.writeObject(new Message(Protocol.CMD_LOGIN_SUCCESS, "staff-001", "Admin User"));
                                        oos.flush();
                                    } else {
                                        oos.writeObject(new Message(Protocol.CMD_ERROR, "Server", "Đăng nhập thất bại: Tên đăng nhập hoặc mật khẩu không chính xác!"));
                                        oos.flush();
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }, "Mock-ClientHandler").start();
                }
            } catch (Exception ignored) {
            }
        }, "Mock-Server-Acceptor");
        serverThread.start();
    }

    @AfterEach
    public void stopMockServer() throws Exception {
        serverRunning = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Test
    public void testFailedLoginCanRetryAndSucceed() throws Exception {
        StaffNetworkService service = new StaffNetworkService();

        CountDownLatch errorLatch = new CountDownLatch(1);
        CountDownLatch successLatch = new CountDownLatch(1);
        AtomicBoolean errorReceived = new AtomicBoolean(false);
        AtomicBoolean successReceived = new AtomicBoolean(false);

        StaffNetworkListener listener = new StaffNetworkListener() {
            @Override
            public void onLoginSuccess() {
                successReceived.set(true);
                successLatch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                if (errorMessage.contains("Đăng nhập thất bại")) {
                    errorReceived.set(true);
                    errorLatch.countDown();
                }
            }

            @Override public void onQueueUpdated(List<CustomerItem> waitingCustomers) {}
            @Override public void onPairedWithCustomer(String customerName, String customerId) {}
            @Override public void onMessageReceived(String senderName, String senderId, String message) {}
            @Override public void onSessionEnded(String reason) {}
        };

        service.addListener(listener);

        // First attempt: wrong password
        service.connectAndLogin("127.0.0.1", port, "admin", "wrong_pass");
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS), "Should receive login error on wrong password");
        assertTrue(errorReceived.get());

        // Second attempt: correct password without restarting application
        service.connectAndLogin("127.0.0.1", port, "admin", "correct123");
        assertTrue(successLatch.await(5, TimeUnit.SECONDS), "Should receive login success on retry with correct password");
        assertTrue(successReceived.get());

        service.disconnect();
    }
}
