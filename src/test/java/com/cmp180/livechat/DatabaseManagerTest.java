package com.cmp180.livechat;

import com.cmp180.livechat.common.SecurityManager;
import com.cmp180.livechat.server.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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

        // Tìm lại theo ID
        DatabaseManager.CustomerRecord foundById = db.getCustomerById(cust.customerId);
        assertNotNull(foundById);
        assertEquals(testName, foundById.fullName);
        assertEquals(testPhone, foundById.phoneOrEmail);
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

    @Test
    public void testSaltedPasswordHashingAndVerification() {
        String pass = "SecurePass@2026";
        String salted1 = SecurityManager.hashPasswordWithSalt(pass);
        String salted2 = SecurityManager.hashPasswordWithSalt(pass);

        assertNotNull(salted1);
        assertNotNull(salted2);
        assertTrue(salted1.contains(":"));
        assertTrue(salted2.contains(":"));
        // Hai lần băm phải khác nhau do sinh salt ngẫu nhiên
        assertNotEquals(salted1, salted2);

        // Kiểm tra verify thành công với mật khẩu đúng
        assertTrue(SecurityManager.verifyPassword(pass, salted1));
        assertTrue(SecurityManager.verifyPassword(pass, salted2));

        // Kiểm tra verify thất bại với mật khẩu sai
        assertFalse(SecurityManager.verifyPassword("WrongPassword", salted1));
        assertFalse(SecurityManager.verifyPassword("", salted1));
        assertFalse(SecurityManager.verifyPassword(null, salted1));

        // Kiểm tra tương thích với chuỗi SHA-256 legacy không có salt
        String legacyHash = SecurityManager.hashPassword("legacy123");
        assertTrue(SecurityManager.verifyPassword("legacy123", legacyHash));
        assertFalse(SecurityManager.verifyPassword("wrong", legacyHash));
    }

    @Test
    public void testCBCEncryptionRandomIVAndLegacyFallback() throws Exception {
        String plain = "Thông tin bảo mật quan trọng!";
        String enc1 = SecurityManager.encrypt(plain);
        String enc2 = SecurityManager.encrypt(plain);

        // Với CBC + random IV, 2 lần mã hóa cùng dữ liệu phải ra 2 chuỗi khác nhau
        assertNotEquals(enc1, enc2);

        // Cả 2 đều phải giải mã chính xác về nội dung gốc
        assertEquals(plain, SecurityManager.decrypt(enc1));
        assertEquals(plain, SecurityManager.decrypt(enc2));

        // Kiểm tra khả năng giải mã tin nhắn legacy AES/ECB (mã hóa trực tiếp bằng ECB như trước)
        javax.crypto.Cipher ecbCipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec("LiveChatKey12345".getBytes(java.nio.charset.StandardCharsets.UTF_8), "AES");
        ecbCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        byte[] ecbEncrypted = ecbCipher.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String legacyEcbBase64 = java.util.Base64.getEncoder().encodeToString(ecbEncrypted);

        // SecurityManager.decrypt phải tự động fallback và giải mã thành công bản tin ECB cũ
        String decryptedLegacy = SecurityManager.decrypt(legacyEcbBase64);
        assertEquals(plain, decryptedLegacy);
    }

    @Test
    public void testCustomerHistoryAndNameLookup() {
        String phone = "0933221100";
        String name = "Trần Minh Quang";
        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(phone, name);
        assertNotNull(cust);

        // Tra cứu theo tên và phoneOrName
        DatabaseManager.CustomerRecord byName = db.findCustomerByName("Trần Minh Quang");
        assertNotNull(byName);
        assertEquals(cust.customerId, byName.customerId);

        DatabaseManager.CustomerRecord byPhoneOrName = db.findCustomerByPhoneOrName("0933221100");
        assertNotNull(byPhoneOrName);
        assertEquals(cust.customerId, byPhoneOrName.customerId);

        // Tạo 2 session và tin nhắn cho khách hàng này
        String sess1 = UUID.randomUUID().toString();
        String sess2 = UUID.randomUUID().toString();
        db.createSession(sess1, cust.customerId, "staff01", "Tư vấn Laptop");
        db.createSession(sess2, cust.customerId, "staff01", "Bảo hành");

        db.saveMessage(sess1, UUID.randomUUID().toString(), "CUSTOMER", name, "Tin nhắn 1", null, null, null, 0, false, System.currentTimeMillis() - 2000);
        db.saveMessage(sess1, UUID.randomUUID().toString(), "STAFF", "CSKH", "Tin nhắn 2", null, null, null, 0, false, System.currentTimeMillis() - 1500);
        db.saveMessage(sess2, UUID.randomUUID().toString(), "CUSTOMER", name, "Tin nhắn 3 ở phiên 2", null, null, null, 0, false, System.currentTimeMillis() - 1000);

        // Lấy toàn bộ tin nhắn xuyên suốt tất cả các phiên của khách hàng
        List<DatabaseManager.MessageRecord> allMsgs = db.getAllMessagesByCustomer(cust.customerId);
        assertTrue(allMsgs.size() >= 3, "Phải lấy được tất cả tin nhắn từ cả 2 phiên của khách hàng");
    }

    @Test
    @DisplayName("Kiểm tra cập nhật thông tin hồ sơ khách hàng (Email, Hạng VIP, Ghi chú CSKH)")
    void testUpdateCustomerProfileAndFetch() {
        String phone = "0944556677";
        String initialName = "Lê Hoàng Nam";
        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(phone, initialName);
        assertNotNull(cust);

        // Cập nhật thông tin hồ sơ
        String updatedName = "Lê Hoàng Nam (VIP Pro)";
        String email = "hoangnam.le@example.com";
        String tier = "VIP";
        String notes = "Khách hàng thân thiết doanh nghiệp, ưu tiên kỹ thuật";

        boolean updated = db.updateCustomerProfile(cust.customerId, updatedName, phone, email, tier, notes);
        assertTrue(updated, "Cập nhật hồ sơ khách hàng phải thành công");

        // Tra cứu lại từ DB
        DatabaseManager.CustomerRecord fetched = db.getCustomerById(cust.customerId);
        assertNotNull(fetched);
        assertEquals(updatedName, fetched.fullName);
        assertEquals(phone, fetched.phoneOrEmail);
        assertEquals(email, fetched.email);
        assertEquals(tier, fetched.tier);
        assertEquals(notes, fetched.notes);

        // Tra cứu theo số điện thoại
        DatabaseManager.CustomerRecord fetchedByPhone = db.findCustomerByPhone(phone);
        assertNotNull(fetchedByPhone);
        assertEquals(email, fetchedByPhone.email);
        assertEquals(tier, fetchedByPhone.tier);
    }

    @Test
    @DisplayName("Kiểm tra cập nhật tên đăng nhập và mật khẩu Staff (Họ tên cố định)")
    void testUpdateStaffCredentials() {
        String testUser = "staff_cred_" + UUID.randomUUID().toString().substring(0, 8);
        String pass = "OldPass123";
        String fullName = "Staff Credential Test";
        boolean created = db.addStaff(testUser, pass, fullName, "STAFF");
        assertTrue(created);

        DatabaseManager.StaffRecord s = db.authenticateStaff(testUser, pass);
        assertNotNull(s);

        // Đổi tên đăng nhập và mật khẩu mới
        String newUser = testUser + "_updated";
        String newPass = "NewPass456";
        boolean updated = db.updateStaffCredentials(s.staffId, newUser, newPass);
        assertTrue(updated, "Cập nhật credentials staff phải thành công");

        // Đăng nhập bằng tên và pass mới
        DatabaseManager.StaffRecord newAuth = db.authenticateStaff(newUser, newPass);
        assertNotNull(newAuth, "Phải đăng nhập được với username và password mới");
        assertEquals(fullName, newAuth.fullName, "Họ và tên nhân viên phải được giữ nguyên cố định");

        // Đăng nhập bằng username cũ phải thất bại
        assertNull(db.authenticateStaff(testUser, pass));

        // Trùng username với tài khoản khác phải thất bại
        boolean dupUpdate = db.updateStaffCredentials(s.staffId, "staff01", "123456");
        assertFalse(dupUpdate, "Không được đổi tên trùng với staff khác đang hoạt động");

        // Kiểm tra verifyStaffPassword
        assertTrue(db.verifyStaffPassword(s.staffId, newPass), "Mật khẩu đúng phải xác thực thành công");
        assertFalse(db.verifyStaffPassword(s.staffId, "WrongPass"), "Mật khẩu sai phải xác thực thất bại");
    }

    @Test
    @DisplayName("Kiểm tra chuẩn hoá topic và kiểm tra chuỗi phiên ghép")
    void testTopicNormalizationAndChaining() {
        assertTrue(DatabaseManager.isChainedTopic("Hỏi tiếp: Tư vấn Mua hàng"));
        assertTrue(DatabaseManager.isChainedTopic("hỏi tiếp:  Bảo hành"));
        assertTrue(DatabaseManager.isChainedTopic("Re: Khiếu nại"));
        assertFalse(DatabaseManager.isChainedTopic("Tư vấn Mua hàng"));
        assertFalse(DatabaseManager.isChainedTopic("Hỗ trợ Kỹ thuật & Cài đặt"));

        assertEquals("Tư vấn Mua hàng", DatabaseManager.normalizeTopic("Hỏi tiếp: Tư vấn Mua hàng"));
        assertEquals("Tư vấn Mua hàng", DatabaseManager.normalizeTopic("Hỏi tiếp: Hỏi tiếp: Tư vấn Mua hàng"));
        assertEquals("Bảo hành & Đổi trả", DatabaseManager.normalizeTopic("Re: Bảo hành & Đổi trả"));
        assertEquals("Vấn đề khác", DatabaseManager.normalizeTopic("Vấn đề khác"));
    }

    @Test
    @DisplayName("Kiểm tra getMessagesForReopenSession lọc đúng chuỗi phiên và không làm rò rỉ chủ đề khác")
    void testGetMessagesForReopenSession() throws InterruptedException {
        String testPhone = "0977" + (System.currentTimeMillis() % 1000000);
        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(testPhone, "Khách Ghép Phiên");
        DatabaseManager.StaffRecord staff = db.authenticateStaff("staff01", "123456");
        assertNotNull(cust);
        assertNotNull(staff);

        // Phiên 1: Standalone topic "Tư vấn Mua hàng"
        String sess1 = UUID.randomUUID().toString();
        db.createSession(sess1, cust.customerId, staff.staffId, "Tư vấn Mua hàng");
        db.saveMessage(sess1, UUID.randomUUID().toString(), "CUSTOMER", cust.fullName, "Tin nhắn 1 - Phiên 1", null, null, null, 0, false, System.currentTimeMillis() - 3000);
        db.closeSession(sess1, 5, "Tốt");

        Thread.sleep(10);

        // Phiên 2: Khác chủ đề "Hỗ trợ Kỹ thuật & Cài đặt" (không liên quan)
        String sessOther = UUID.randomUUID().toString();
        db.createSession(sessOther, cust.customerId, staff.staffId, "Hỗ trợ Kỹ thuật & Cài đặt");
        db.saveMessage(sessOther, UUID.randomUUID().toString(), "CUSTOMER", cust.fullName, "Tin nhắn Kỹ thuật - Không được lọt", null, null, null, 0, false, System.currentTimeMillis() - 2000);
        db.closeSession(sessOther, 4, "Tạm ổn");

        Thread.sleep(10);

        // Phiên 3: Phiên ghép "Hỏi tiếp: Tư vấn Mua hàng" (nối tiếp phiên 1)
        String sess2 = UUID.randomUUID().toString();
        db.createSession(sess2, cust.customerId, staff.staffId, "Hỏi tiếp: Tư vấn Mua hàng");
        db.saveMessage(sess2, UUID.randomUUID().toString(), "CUSTOMER", cust.fullName, "Tin nhắn 2 - Phiên 3", null, null, null, 0, false, System.currentTimeMillis() - 1000);
        db.closeSession(sess2, 5, "Hài lòng");

        Thread.sleep(10);

        // Phiên 4: Diễn ra sau phiên 3 (subsequent session)
        String sessSubsequent = UUID.randomUUID().toString();
        db.createSession(sessSubsequent, cust.customerId, staff.staffId, "Hỏi tiếp: Tư vấn Mua hàng");
        db.saveMessage(sessSubsequent, UUID.randomUUID().toString(), "CUSTOMER", cust.fullName, "Tin nhắn tương lai", null, null, null, 0, false, System.currentTimeMillis());
        db.closeSession(sessSubsequent, 5, "Xong");

        // 1. Mở lại phiên 1 (standalone): chỉ được có tin nhắn của phiên 1
        List<DatabaseManager.MessageRecord> msgsSess1 = db.getMessagesForReopenSession(sess1);
        assertEquals(1, msgsSess1.size());
        assertEquals("Tin nhắn 1 - Phiên 1", msgsSess1.get(0).content);

        // 2. Mở lại phiên 3 ("Hỏi tiếp: Tư vấn Mua hàng"):
        // Phải nạp phiên 1 VÀ phiên 3, TUYỆT ĐỐI không có phiên Kỹ thuật và không có phiên tương lai
        List<DatabaseManager.MessageRecord> msgsSess2 = db.getMessagesForReopenSession(sess2);
        assertEquals(2, msgsSess2.size(), "Phải nạp đúng 2 tin nhắn từ chuỗi phiên 1 và phiên 3");
        assertEquals("Tin nhắn 1 - Phiên 1", msgsSess2.get(0).content);
        assertEquals("Tin nhắn 2 - Phiên 3", msgsSess2.get(1).content);

        // 3. Mở lại phiên Kỹ thuật: chỉ có tin nhắn của phiên Kỹ thuật
        List<DatabaseManager.MessageRecord> msgsOther = db.getMessagesForReopenSession(sessOther);
        assertEquals(1, msgsOther.size());
        assertEquals("Tin nhắn Kỹ thuật - Không được lọt", msgsOther.get(0).content);
    }

    @Test
    @DisplayName("Kiểm tra 45 tài khoản nhân viên CSKH sinh viên được gán cứng và đăng nhập bằng MSSV / Họ tên")
    void testHardcodedStudentStaffList() {
        assertEquals(45, DatabaseManager.DEFAULT_STUDENT_STAFF.length, "Phải có đúng 45 sinh viên trong danh sách gán cứng");

        // 1. Kiểm tra tài khoản Hoàng Trung Anh (MSSV: 2380602692)
        DatabaseManager.StaffRecord htaByMssv = db.authenticateStaff("2380602692", "2380602692");
        assertNotNull(htaByMssv, "Đăng nhập bằng MSSV 2380602692 phải thành công");
        assertEquals("Hoàng Trung Anh", htaByMssv.fullName);
        assertEquals("2380602692", htaByMssv.username);

        // Đăng nhập bằng Họ và tên
        DatabaseManager.StaffRecord htaByName = db.authenticateStaff("Hoàng Trung Anh", "2380602692");
        assertNotNull(htaByName, "Đăng nhập bằng Họ và tên 'Hoàng Trung Anh' phải thành công");
        assertEquals("Hoàng Trung Anh", htaByName.fullName);

        // Sai mật khẩu
        assertNull(db.authenticateStaff("2380602692", "sai_mat_khau"));
        assertNull(db.authenticateStaff("Hoàng Trung Anh", "123456"));

        // 2. Kiểm duyệt toàn bộ 45 tài khoản sinh viên đều đăng nhập thành công với mật khẩu là MSSV
        for (String[] entry : DatabaseManager.DEFAULT_STUDENT_STAFF) {
            String mssv = entry[0];
            String expectedName = entry[1];

            DatabaseManager.StaffRecord record = db.authenticateStaff(mssv, mssv);
            assertNotNull(record, "Nhân viên " + expectedName + " (MSSV: " + mssv + ") phải đăng nhập thành công");
            assertEquals(expectedName, record.fullName);
            assertEquals("STAFF", record.role);
            assertNotNull(record.primarySkill);
        }
    }
}

