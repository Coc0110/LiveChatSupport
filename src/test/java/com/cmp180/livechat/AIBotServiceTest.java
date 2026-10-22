package com.cmp180.livechat;

import com.cmp180.livechat.server.AIBotService;
import com.cmp180.livechat.server.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AIBotServiceTest {

    private AIBotService aiBotService;

    @BeforeEach
    public void setUp() {
        aiBotService = AIBotService.getInstance();
    }

    @Test
    @DisplayName("Kiểm tra singleton instance")
    public void testSingleton() {
        assertNotNull(aiBotService);
        assertSame(aiBotService, AIBotService.getInstance());
    }

    @Test
    @DisplayName("Kiểm tra tin nhắn rỗng hoặc null")
    public void testNullOrEmptyQuery() {
        String resNull = aiBotService.generateResponse(null, null);
        assertNotNull(resNull);
        assertTrue(resNull.contains("Tôi có thể giúp gì cho bạn"));

        String resBlank = aiBotService.generateResponse(null, "   ");
        assertNotNull(resBlank);
        assertTrue(resBlank.contains("Tôi có thể giúp gì cho bạn"));
    }

    @Test
    @DisplayName("Kiểm tra chào hỏi ban đầu")
    public void testGreeting() {
        String res = aiBotService.generateResponse(null, "Chào bot, bạn có ở đó không?");
        assertNotNull(res);
        assertTrue(res.contains("Xin chào"));
        assertTrue(res.contains("LCS chat bot"));
    }

    @Test
    @DisplayName("Kiểm tra yêu cầu kết nối nhân viên người thật")
    public void testRequestStaffTransfer() {
        String res = aiBotService.generateResponse(null, "Tôi muốn gặp nhân viên tư vấn người thật");
        assertNotNull(res);
        assertTrue(res.contains("Nhân viên CSKH") || res.contains("kết nối"));
        assertTrue(res.contains("sẵn sàng"));
    }

    @Test
    @DisplayName("Kiểm tra cung cấp số điện thoại khi tra cứu đơn")
    public void testPhoneNumberDetection() {
        String res = aiBotService.generateResponse(null, "0333666999");
        assertNotNull(res);
        assertTrue(res.contains("0333666999"));
        assertTrue(res.contains("số điện thoại") || res.contains("ghi nhận"));
    }

    @Test
    @DisplayName("Kiểm tra hỏi thời gian giao hàng")
    public void testOrderDeliveryInquiry() {
        String res = aiBotService.generateResponse(null, "đơn của tôi khi nào thì giao đến ?");
        assertNotNull(res);
        assertTrue(res.contains("Giao hàng") || res.contains("Vận chuyển"));
        assertTrue(res.contains("giờ") || res.contains("ngày"));
    }

    @Test
    @DisplayName("Kiểm tra khách báo đã gửi thông tin ở trên")
    public void testFollowUpAbove() {
        String res = aiBotService.generateResponse(null, "tôi vừa đưa số điện thoại ở trên");
        assertNotNull(res);
        assertTrue(res.contains("ở trên") || res.contains("cung cấp"));
        assertTrue(res.contains("nhân viên"));
    }

    @Test
    @DisplayName("Kiểm tra khách yêu cầu đổi câu nói / không lặp lại")
    public void testDifferentResponseRequest() {
        String res = aiBotService.generateResponse(null, "bạn có thể nói gì khác không ?");
        assertNotNull(res);
        assertTrue(res.contains("xin lỗi") || res.contains("LCS chat bot"));
        assertTrue(res.contains("Bảo hành") || res.contains("Giao hàng") || res.contains("hỗ trợ"));
    }

    @Test
    @DisplayName("Kiểm tra tư vấn Laptop Gaming")
    public void testGamingLaptopConsultation() {
        String res = aiBotService.generateResponse(null, "Tư vấn giúp mình laptop gaming tầm 20-30 triệu");
        assertNotNull(res);
        assertTrue(res.toLowerCase().contains("gaming"));
        assertTrue(res.contains("Lenovo") || res.contains("Asus") || res.contains("Acer"));
        assertTrue(res.contains("RTX"));
    }

    @Test
    @DisplayName("Kiểm tra tư vấn Laptop Văn phòng & Học tập")
    public void testOfficeLaptopConsultation() {
        String res = aiBotService.generateResponse(null, "Cần tìm máy laptop văn phòng mỏng nhẹ cho sinh viên");
        assertNotNull(res);
        assertTrue(res.contains("Văn phòng") || res.contains("Học tập"));
        assertTrue(res.contains("RAM") || res.contains("SSD"));
    }

    @Test
    @DisplayName("Kiểm tra tư vấn Build PC")
    public void testBuildPCConsultation() {
        String res = aiBotService.generateResponse(null, "Mình muốn build pc cây máy tính chơi game");
        assertNotNull(res);
        assertTrue(res.contains("Cấu hình PC") || res.contains("Cấu hình Gaming"));
        assertTrue(res.contains("RTX") || res.contains("Core i5"));
    }

    @Test
    @DisplayName("Kiểm tra hỗ trợ kỹ thuật: Máy đen màn hình / Sập nguồn")
    public void testTechnicalBlackScreen() {
        String res = aiBotService.generateResponse(null, "Máy mình bị đen màn hình bật không lên nguồn");
        assertNotNull(res);
        assertTrue(res.contains("Xả tĩnh điện") || res.contains("Nguồn"));
        assertTrue(res.contains("bộ sạc") || res.contains("màn hình"));
    }

    @Test
    @DisplayName("Kiểm tra hỗ trợ kỹ thuật: Máy nóng quạt kêu to")
    public void testTechnicalOverheating() {
        String res = aiBotService.generateResponse(null, "Laptop chạy bị quá nóng và quạt kêu to");
        assertNotNull(res);
        assertTrue(res.contains("nhiệt độ") || res.contains("tản nhiệt"));
        assertTrue(res.contains("bụi") || res.contains("keo tản nhiệt"));
    }

    @Test
    @DisplayName("Kiểm tra hỗ trợ kỹ thuật: Màn hình xanh BSOD / Windows")
    public void testTechnicalBSOD() {
        String res = aiBotService.generateResponse(null, "Máy bị lỗi màn hình xanh bsod và mất wifi");
        assertNotNull(res);
        assertTrue(res.contains("Màn hình xanh") || res.contains("BSOD") || res.contains("Safe Mode"));
    }

    @Test
    @DisplayName("Kiểm tra chính sách bảo hành & đổi trả")
    public void testWarrantyPolicy() {
        String res = aiBotService.generateResponse(null, "Chính sách bảo hành đổi trả máy như thế nào?");
        assertNotNull(res);
        assertTrue(res.contains("Bảo hành") || res.contains("Đổi trả"));
        assertTrue(res.contains("1 - đổi - 1") || res.contains("tháng"));
    }

    @Test
    @DisplayName("Kiểm tra tra cứu đơn hàng & vận chuyển")
    public void testOrderTracking() {
        String res = aiBotService.generateResponse(null, "Bao lâu thì đơn hàng của tôi giao tới nơi, có được đồng kiểm không?");
        assertNotNull(res);
        assertTrue(res.contains("Giao hàng") || res.contains("Vận chuyển") || res.contains("Đơn hàng"));
        assertTrue(res.contains("đồng kiểm") || res.contains("giờ") || res.contains("ngày"));
    }

    @Test
    @DisplayName("Kiểm tra câu hỏi ngoài chủ đề hỗ trợ")
    public void testOutOfScopeQuery() {
        String res = aiBotService.generateResponse(null, "Dạy tôi nấu ăn món canh chua cá lóc với");
        assertNotNull(res);
        assertTrue(res.contains("chưa thể hỗ trợ") || res.contains("chuyên"));
        assertTrue(res.contains("Laptop") || res.contains("máy tính") || res.contains("công nghệ"));
    }

    @Test
    @DisplayName("Kiểm tra lời cảm ơn và tạm biệt")
    public void testThanksAndGoodbye() {
        String resThanks = aiBotService.generateResponse(null, "Cảm ơn bot nhiều nha, rất nhiệt tình");
        assertNotNull(resThanks);
        assertTrue(resThanks.toLowerCase().contains("không có gì") || resThanks.contains("Rất vui được hỗ trợ"));

        String resBye = aiBotService.generateResponse(null, "Tạm biệt bot nhé");
        assertNotNull(resBye);
        assertTrue(resBye.contains("Tạm biệt") || resBye.contains("Cảm ơn bạn"));
    }
}
