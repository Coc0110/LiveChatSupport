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
    }

    @Test
    public void testStaffPrimarySkillInDatabase() {
        DatabaseManager.StaffRecord staff01 = db.authenticateStaff("staff01", "123456");
        assertNotNull(staff01);
        assertNotNull(staff01.primarySkill, "primarySkill không được null trong DB");
    }
}
