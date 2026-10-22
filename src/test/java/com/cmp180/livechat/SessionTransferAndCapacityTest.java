package com.cmp180.livechat;

import com.cmp180.livechat.client.staff.model.OnlineStaffItem;
import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.server.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTransferAndCapacityTest {

    private static DatabaseManager db;

    @BeforeAll
    public static void setUp() {
        db = DatabaseManager.getInstance();
    }

    @Test
    public void testOnlineStaffItemCapacityGuard() {
        OnlineStaffItem staff1 = new OnlineStaffItem("id1", "Nguyễn Văn A", "Kỹ thuật", 1);
        assertFalse(staff1.isBusy(), "Staff với 1 session không được xem là busy");
        assertEquals("Kỹ thuật", staff1.getPrimarySkill());

        OnlineStaffItem staff2 = new OnlineStaffItem("id2", "Trần Thị B", "Bảo hành", 3);
        assertTrue(staff2.isBusy(), "Staff với 3 sessions phải là busy (đầy tải)");

        OnlineStaffItem staff3 = new OnlineStaffItem("id3", "Lê Văn C", "", 0);
        assertEquals("Tất cả", staff3.getPrimarySkill(), "Kỹ năng trống phải mặc định là 'Tất cả'");
    }

    @Test
    public void testProtocolCommandsExist() {
        assertEquals("GET_ONLINE_STAFF", Protocol.CMD_GET_ONLINE_STAFF);
        assertEquals("ONLINE_STAFF_LIST", Protocol.CMD_ONLINE_STAFF_LIST);
        assertEquals("TRANSFER_SESSION", Protocol.CMD_TRANSFER_SESSION);
        assertEquals("HEARTBEAT", Protocol.CMD_HEARTBEAT);
        assertEquals(10 * 1024 * 1024, Protocol.MAX_FILE_SIZE, "MAX_FILE_SIZE phải là 10 MB để tránh rủi ro OOM");
    }

    @Test
    public void testStaffPrimarySkillInDatabase() {
        DatabaseManager.StaffRecord staff01 = db.authenticateStaff("staff01", "123456");
        assertNotNull(staff01);
        assertNotNull(staff01.primarySkill, "primarySkill không được null trong DB");
    }

    @Test
    public void testReopenSessionHistoryRetrieval() {
        // Tạo một khách hàng và phiên chat giả lập
        String phone = "0987654321";
        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(phone, "Nguyễn Reopen Test");
        assertNotNull(cust);

        String testSessionId = "test-reopen-" + System.currentTimeMillis();
        db.createSession(testSessionId, cust.customerId, "staff01", "Hỗ trợ Kỹ thuật");

        // Lưu 3 tin nhắn: khách gửi, nhân viên trả lời, và hệ thống
        long now = System.currentTimeMillis();
        String m1 = java.util.UUID.randomUUID().toString();
        String m2 = java.util.UUID.randomUUID().toString();
        String m3 = java.util.UUID.randomUUID().toString();
        db.saveMessage(testSessionId, m1, "CUSTOMER", "Nguyễn Reopen Test", "Khách hỏi lần 1", null, null, null, 0, false, now);
        db.saveMessage(testSessionId, m2, "STAFF", "Nguyễn Văn An", "CSKH trả lời lần 1", null, null, null, 0, false, now + 100);
        db.saveMessage(testSessionId, m3, "SYSTEM", "Hệ thống", "Thông báo hệ thống", null, null, null, 0, false, now + 200);

        // Đóng phiên
        db.closeSession(testSessionId, 5, "Đã hỗ trợ xong đợt 1");

        // Mô phỏng việc khách bấm 'Mở lại phiên' -> Hệ thống lấy lại lịch sử
        java.util.List<DatabaseManager.MessageRecord> recoveredMsgs = db.getMessagesBySession(testSessionId);
        assertNotNull(recoveredMsgs);
        assertEquals(3, recoveredMsgs.size(), "Phải lấy lại đủ 3 tin nhắn của phiên cũ");
        assertEquals("CUSTOMER", recoveredMsgs.get(0).senderRole);
        assertEquals("STAFF", recoveredMsgs.get(1).senderRole);
        assertEquals("SYSTEM", recoveredMsgs.get(2).senderRole);
    }

    @Test
    public void testFilePersistenceAndRetrieval() {
        String testSessionId = "test-file-" + System.currentTimeMillis();
        db.createSession(testSessionId, "cust-test", "staff01", "Gửi tệp");

        String msgId = java.util.UUID.randomUUID().toString();
        byte[] sampleBytes = "Hello CMP180 LiveChat Storage Test".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        db.saveMessage(testSessionId, msgId, "CUSTOMER", "Khách Demo", "Gửi tệp đính kèm",
                null, null, "sample.txt", sampleBytes.length, false, System.currentTimeMillis(), sampleBytes);

        java.util.List<DatabaseManager.MessageRecord> records = db.getMessagesBySession(testSessionId);
        assertNotNull(records);
        assertEquals(1, records.size());

        DatabaseManager.MessageRecord rec = records.get(0);
        assertEquals("sample.txt", rec.fileName);
        assertNotNull(rec.filePath, "filePath phải được lưu trong database");
        assertNotNull(rec.fileData, "fileData phải được đọc lại từ ổ đĩa máy chủ");
        assertArrayEquals(sampleBytes, rec.fileData, "Dữ liệu file tải lại phải trùng khớp chính xác");
    }

    @Test
    public void testCustomerTopicChainRetrieval() {
        String phone = "0933" + (System.currentTimeMillis() % 1000000);
        DatabaseManager.CustomerRecord cust = db.registerOrUpdateCustomer(phone, "Khách Hàng Chuỗi Đề Tài");
        assertNotNull(cust);

        // Phiên 1: Standalone topic "Tra cứu Đơn hàng & Giao vận"
        String sess1 = "chain-test-1-" + System.currentTimeMillis();
        db.createSession(sess1, cust.customerId, "staff01", "Tra cứu Đơn hàng & Giao vận");
        db.saveMessage(sess1, java.util.UUID.randomUUID().toString(), "CUSTOMER", cust.fullName, "Đơn hàng của tôi đang ở đâu?", null, null, null, 0, false, System.currentTimeMillis() - 2000);
        db.closeSession(sess1, 5, "Xong đợt 1");

        // 1. Nếu mở chủ đề mới tinh (không có 'Hỏi tiếp:'), không được nạp tin nhắn cũ
        java.util.List<DatabaseManager.MessageRecord> freshTopicMsgs = db.getMessagesForCustomerTopicChain(cust.customerId, "Tư vấn Mua Laptop & Build PC");
        assertTrue(freshTopicMsgs.isEmpty(), "Chủ đề mới không bắt đầu bằng 'Hỏi tiếp:' phải bắt đầu với danh sách rỗng");

        // 2. Nếu mở chủ đề nối tiếp "Hỏi tiếp: Tra cứu Đơn hàng & Giao vận", phải tìm thấy phiên 1
        java.util.List<DatabaseManager.MessageRecord> chainedMsgs = db.getMessagesForCustomerTopicChain(cust.customerId, "Hỏi tiếp: Tra cứu Đơn hàng & Giao vận");
        assertEquals(1, chainedMsgs.size(), "Phải nạp đúng tin nhắn từ phiên trước trong chuỗi");
        assertEquals("Đơn hàng của tôi đang ở đâu?", chainedMsgs.get(0).content);
    }

    @Test
    public void testStaffNetworkServiceHistoryCachingWithoutListener() {
        com.cmp180.livechat.client.staff.network.StaffNetworkService service = new com.cmp180.livechat.client.staff.network.StaffNetworkService();
        String testCustId = "test-cust-cache-id";

        // Trước khi có listener nào, lịch sử trống
        assertTrue(service.getCachedChatHistory(testCustId).isEmpty());

        // Kiểm tra CMD_REQ_CHAT_HISTORY
        assertEquals("REQ_CHAT_HISTORY", Protocol.CMD_REQ_CHAT_HISTORY);
    }
}
