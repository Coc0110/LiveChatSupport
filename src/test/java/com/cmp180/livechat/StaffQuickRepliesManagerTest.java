package com.cmp180.livechat;

import com.cmp180.livechat.client.staff.ui.StaffQuickRepliesManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StaffQuickRepliesManagerTest {

    @Test
    public void testQuickRepliesCrudAndInitialEmpty() {
        String testStaff = "test_staff_" + System.currentTimeMillis();

        // 1. Ban đầu phải để trống
        List<String> initial = StaffQuickRepliesManager.getQuickReplies(testStaff);
        assertTrue(initial.isEmpty(), "Ban đầu danh sách tin nhắn nhanh cá nhân của nhân viên phải trống");

        // 2. Thêm tin nhắn nhanh
        StaffQuickRepliesManager.addQuickReply(testStaff, "Dạ em chào anh/chị!");
        StaffQuickRepliesManager.addQuickReply(testStaff, "Em đang kiểm tra hệ thống ạ.");

        List<String> afterAdd = StaffQuickRepliesManager.getQuickReplies(testStaff);
        assertEquals(2, afterAdd.size());
        assertEquals("Dạ em chào anh/chị!", afterAdd.get(0));
        assertEquals("Em đang kiểm tra hệ thống ạ.", afterAdd.get(1));

        // 3. Sửa tin nhắn nhanh
        StaffQuickRepliesManager.updateQuickReply(testStaff, 0, "Dạ em chào anh/chị, em có thể hỗ trợ gì ạ?");
        List<String> afterEdit = StaffQuickRepliesManager.getQuickReplies(testStaff);
        assertEquals("Dạ em chào anh/chị, em có thể hỗ trợ gì ạ?", afterEdit.get(0));

        // 4. Xóa tin nhắn nhanh
        StaffQuickRepliesManager.deleteQuickReply(testStaff, 1);
        List<String> afterDelete = StaffQuickRepliesManager.getQuickReplies(testStaff);
        assertEquals(1, afterDelete.size());
        assertEquals("Dạ em chào anh/chị, em có thể hỗ trợ gì ạ?", afterDelete.get(0));

        // Dọn dẹp
        StaffQuickRepliesManager.saveQuickReplies(testStaff, List.of());
        assertTrue(StaffQuickRepliesManager.getQuickReplies(testStaff).isEmpty());
    }

    @Test
    public void testStripReopenPrefixRegex() {
        String topic1 = "Hỏi tiếp: Hỏi tiếp: Hỏi tiếp: Tư vấn Mua hàng";
        String cleaned1 = topic1.replaceAll("^(Hỏi tiếp:\\s*)+", "").trim();
        assertEquals("Tư vấn Mua hàng", cleaned1);

        String topic2 = "Hỏi tiếp: Bảo hành máy tính";
        String cleaned2 = topic2.replaceAll("^(Hỏi tiếp:\\s*)+", "").trim();
        assertEquals("Bảo hành máy tính", cleaned2);

        String topic3 = "Tư vấn kỹ thuật";
        String cleaned3 = topic3.replaceAll("^(Hỏi tiếp:\\s*)+", "").trim();
        assertEquals("Tư vấn kỹ thuật", cleaned3);
    }
}
