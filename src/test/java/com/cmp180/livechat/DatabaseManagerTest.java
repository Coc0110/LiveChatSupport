package com.cmp180.livechat;

import com.cmp180.livechat.common.SecurityManager;
import com.cmp180.livechat.server.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private static DatabaseManager db;

    @BeforeAll
    public static void setUp() {
        db = DatabaseManager.getInstance();
    }

    @Test
    public void testStaffAuthentication() {
        // Test tài khoản mặc định
        DatabaseManager.StaffRecord staff = db.authenticateStaff("staff01", "123456");
        assertNotNull(staff, "Tài khoản staff01 phải đăng nhập thành công");
        assertEquals("staff01", staff.username);
        assertEquals("Nguyễn Văn An", staff.fullName);

        // Test sai mật khẩu
        DatabaseManager.StaffRecord wrongPass = db.authenticateStaff("staff01", "sai_mat_khau");
        assertNull(wrongPass, "Sai mật khẩu phải trả về null");

        // Test tài khoản không tồn tại
        DatabaseManager.StaffRecord notFound = db.authenticateStaff("unknown_user", "123");
        assertNull(notFound, "Tài khoản không tồn tại phải trả về null");
    }

    @Test
    public void testCustomerRegistrationAndLookup() {
        String testPhone = "0999888777";
        String testName = "Khách Hàng Test";

        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(testPhone, testName);
        assertNotNull(cust);
        assertEquals(testPhone, cust.phoneOrEmail);
        assertEquals(testName, cust.fullName);

        // Tìm lại theo SĐT
        DatabaseManager.CustomerRecord found = db.findCustomerByPhone(testPhone);
        assertNotNull(found);
        assertEquals(cust.customerId, found.customerId);
    }

    @Test
    public void testSessionAndMessages() {
        String testPhone = "0988112233";
        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(testPhone, "Người Thử Nghiệm");
        DatabaseManager.StaffRecord staff = db.authenticateStaff("staff01", "123456");

        assertNotNull(cust);
        assertNotNull(staff);

        String sessionId = UUID.randomUUID().toString();
        db.createSession(sessionId, cust.customerId, staff.staffId, "Hỗ trợ Kỹ thuật");

        // Lưu tin nhắn
        String msgId = UUID.randomUUID().toString();
        String enc = SecurityManager.encrypt("Xin chào CSKH!");
        db.saveMessage(sessionId, msgId, "CUSTOMER", cust.fullName, enc, null, null, null, 0, false, System.currentTimeMillis());

        // Lấy lại tin nhắn của session
        List<DatabaseManager.MessageRecord> msgs = db.getMessagesBySession(sessionId);
        assertFalse(msgs.isEmpty());
        assertEquals("CUSTOMER", msgs.get(0).senderRole);
        assertEquals("Xin chào CSKH!", SecurityManager.decrypt(msgs.get(0).content));

        // Đóng session với rating 5 sao
        db.closeSession(sessionId, 5, "Dịch vụ tuyệt vời!");

        List<DatabaseManager.SessionRecord> sessions = db.getSessionsByCustomer(cust.customerId);
        assertFalse(sessions.isEmpty());
        assertEquals(5, sessions.get(0).rating);
        assertEquals("CLOSED", sessions.get(0).status);
    }

    @Test
    public void testEncryptionAndDecryption() {
        String original = "Nội dung tin nhắn bảo mật 12345!@#$%^";
        String encrypted = SecurityManager.encrypt(original);
        assertNotEquals(original, encrypted);

        String decrypted = SecurityManager.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testPasswordHashing() {
        String pass = "123456";
        String hashed1 = SecurityManager.hashPassword(pass);
        String hashed2 = SecurityManager.hashPassword(pass);
        assertNotNull(hashed1);
        assertEquals(64, hashed1.length());
        assertEquals(hashed1, hashed2);
        assertNotEquals(pass, hashed1);
    }

    @Test
    public void testPhoneNumberAndNameValidation() {
        // Test phone normalization & validation
        assertTrue(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidPhoneNumber("0912345678"));
        assertTrue(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidPhoneNumber("+84912345678"));
        assertTrue(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidPhoneNumber("098 123 4567"));
        assertFalse(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidPhoneNumber("0333"));
        assertFalse(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidPhoneNumber("abc"));
        assertFalse(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidPhoneNumber("0123456789"));

        // Test name validation
        assertTrue(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidCustomerName("Nguyễn Văn A"));
        assertTrue(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidCustomerName("John Doe"));
        assertFalse(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidCustomerName("a"));
        assertFalse(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidCustomerName("123456"));
        assertFalse(com.cmp180.livechat.client.customer.ui.CustomerLoginController.isValidCustomerName("..."));
    }
}
