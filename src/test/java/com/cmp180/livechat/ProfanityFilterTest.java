package com.cmp180.livechat;

import com.cmp180.livechat.client.customer.ui.CustomerLoginController;
import com.cmp180.livechat.common.ProfanityFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử đơn vị cho hệ thống kiểm duyệt ngôn từ vi phạm tiêu chuẩn cộng đồng (ProfanityFilter)
 * và ràng buộc đặt tên khách hàng.
 */
public class ProfanityFilterTest {

    @Test
    @DisplayName("Kiểm tra lọc các cụm từ thô tục tiếng Việt phổ biến sang ***")
    public void testMaskingVietnameseMultiWord() {
        String input1 = "mày là đồ mất dạy";
        String filtered1 = ProfanityFilter.filter(input1);
        assertEquals("mày là đồ ***", filtered1);

        String input2 = "chửi địt mẹ mày bây giờ";
        String filtered2 = ProfanityFilter.filter(input2);
        assertTrue(filtered2.contains("***"));
        assertFalse(filtered2.toLowerCase().contains("địt mẹ"));

        String input3 = "thằng óc chó này";
        String filtered3 = ProfanityFilter.filter(input3);
        assertEquals("thằng *** này", filtered3);
    }

    @Test
    @DisplayName("Kiểm tra lọc các từ đơn và từ viết tắt chửi bậy tiếng Việt")
    public void testMaskingVietnameseSingleWordsAndAbbreviations() {
        String input = "cái gì vcl thế này, đm mày nói đéo nghe à";
        String filtered = ProfanityFilter.filter(input);
        assertFalse(filtered.contains("vcl"));
        assertFalse(filtered.contains("đm"));
        assertFalse(filtered.contains("đéo"));
        assertTrue(filtered.contains("***"));

        String input2 = "clgt dmm bực mình";
        String filtered2 = ProfanityFilter.filter(input2);
        assertFalse(filtered2.contains("clgt"));
        assertFalse(filtered2.contains("dmm"));
        assertTrue(filtered2.contains("***"));
    }

    @Test
    @DisplayName("Kiểm tra lọc các từ chửi thề tiếng Anh")
    public void testMaskingEnglishProfanities() {
        String input = "what the fuck is this shit";
        String filtered = ProfanityFilter.filter(input);
        assertEquals("what the *** is this ***", filtered);

        String input2 = "son of a bitch, wtf";
        String filtered2 = ProfanityFilter.filter(input2);
        assertFalse(filtered2.toLowerCase().contains("bitch"));
        assertFalse(filtered2.toLowerCase().contains("wtf"));
        assertTrue(filtered2.contains("***"));
    }

    @Test
    @DisplayName("Kiểm tra lọc các biến thể kéo dài chữ hoặc chèn dấu chấm/gạch nối")
    public void testMaskingWithVariationsAndRepetitions() {
        String input1 = "địtttt mẹeee";
        String filtered1 = ProfanityFilter.filter(input1);
        assertEquals("***", filtered1);

        String input2 = "fuuuuck you";
        String filtered2 = ProfanityFilter.filter(input2);
        assertEquals("*** you", filtered2);

        String input3 = "đ.m cái này";
        String filtered3 = ProfanityFilter.filter(input3);
        assertEquals("*** cái này", filtered3);
    }

    @Test
    @DisplayName("Kiểm tra tránh nhận diện nhầm từ thông thường (Scunthorpe Problem)")
    public void testNoFalsePositives() {
        // "đơn hàng" không bị dính "đ"
        assertEquals("Kiểm tra tình trạng đơn hàng của tôi", ProfanityFilter.filter("Kiểm tra tình trạng đơn hàng của tôi"));

        // "classic" không bị dính "ass"
        assertEquals("Giao diện classic rất đẹp", ProfanityFilter.filter("Giao diện classic rất đẹp"));

        // "admin" không bị dính "dm"
        assertEquals("Xin chào admin hỗ trợ", ProfanityFilter.filter("Xin chào admin hỗ trợ"));

        // "đặc điểm" không bị dính "đm"
        assertEquals("Nêu đặc điểm của sản phẩm", ProfanityFilter.filter("Nêu đặc điểm của sản phẩm"));

        // "buổi sáng" không bị dính "buồi"
        assertEquals("Chào buổi sáng nhân viên tư vấn", ProfanityFilter.filter("Chào buổi sáng nhân viên tư vấn"));

        // "lồng đèn" không bị dính "lồn"
        assertEquals("Bán lồng đèn trung thu", ProfanityFilter.filter("Bán lồng đèn trung thu"));

        // "du lịch" không bị dính "du" hay "đụ"
        assertEquals("Tư vấn tour du lịch", ProfanityFilter.filter("Tư vấn tour du lịch"));

        // "bác sĩ", "hỗ trợ khách hàng"
        assertEquals("Bác sĩ chuyên khoa hỗ trợ khách hàng", ProfanityFilter.filter("Bác sĩ chuyên khoa hỗ trợ khách hàng"));
    }

    @Test
    @DisplayName("Kiểm tra phương thức containsProfanity")
    public void testContainsProfanity() {
        assertTrue(ProfanityFilter.containsProfanity("địt mẹ"));
        assertTrue(ProfanityFilter.containsProfanity("fuck you"));
        assertTrue(ProfanityFilter.containsProfanity("vcl"));
        assertTrue(ProfanityFilter.containsProfanity("thằng chó"));

        assertFalse(ProfanityFilter.containsProfanity("Xin chào bạn"));
        assertFalse(ProfanityFilter.containsProfanity("Hỗ trợ kỹ thuật đơn hàng"));
        assertFalse(ProfanityFilter.containsProfanity(null));
        assertFalse(ProfanityFilter.containsProfanity(""));
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ tên khách hàng - Tên hợp lệ (chữ cái, dấu cách, dấu '-')")
    public void testCustomerNameValidation_ValidNames() {
        assertTrue(CustomerLoginController.isValidCustomerName("Nguyễn Văn An"));
        assertTrue(CustomerLoginController.isValidCustomerName("Trần-Thị-Bích"));
        assertTrue(CustomerLoginController.isValidCustomerName("Jean-Luc Picard"));
        assertTrue(CustomerLoginController.isValidCustomerName("Hoàng Trung Anh"));
        assertTrue(CustomerLoginController.isValidCustomerName("Lê Văn B-C"));
    }

    @Test
    @DisplayName("Kiểm tra tên khách hàng chứa ký tự cấm (/ . , + số hoặc ký tự lạ) đều bị từ chối")
    public void testCustomerNameValidation_InvalidCharacters() {
        // Chứa dấu gạch chéo /
        assertFalse(CustomerLoginController.isValidCustomerName("Nguyễn Văn A/B"));

        // Chứa dấu chấm .
        assertFalse(CustomerLoginController.isValidCustomerName("Nguyễn.Văn.A"));

        // Chứa dấu phẩy ,
        assertFalse(CustomerLoginController.isValidCustomerName("Nguyễn, Văn A"));

        // Chứa dấu cộng +
        assertFalse(CustomerLoginController.isValidCustomerName("Nguyễn+Văn+A"));

        // Chứa chữ số
        assertFalse(CustomerLoginController.isValidCustomerName("Hoàng Trung Anh 123"));
        assertFalse(CustomerLoginController.isValidCustomerName("User007"));

        // Chứa ký tự đặc biệt khác @, $, #, *
        assertFalse(CustomerLoginController.isValidCustomerName("User@LiveChat"));
        assertFalse(CustomerLoginController.isValidCustomerName("VIP#1"));

        // Quá ngắn hoặc chỉ toàn dấu gạch ngang / khoảng trắng
        assertFalse(CustomerLoginController.isValidCustomerName("A"));
        assertFalse(CustomerLoginController.isValidCustomerName("---"));
        assertFalse(CustomerLoginController.isValidCustomerName("   "));
        assertFalse(CustomerLoginController.isValidCustomerName(null));
    }

    @Test
    @DisplayName("Kiểm tra tên khách hàng chứa từ ngữ vi phạm tiêu chuẩn cộng đồng bị từ chối & chuyển thành ***")
    public void testCustomerNameValidation_ProfanityInName() {
        String badName1 = "Nguyễn Văn Địt Mẹ";
        assertTrue(ProfanityFilter.containsProfanity(badName1));
        assertFalse(CustomerLoginController.isValidCustomerName(badName1));
        assertEquals("Nguyễn Văn ***", ProfanityFilter.filter(badName1));

        String badName2 = "Fuck You";
        assertTrue(ProfanityFilter.containsProfanity(badName2));
        assertFalse(CustomerLoginController.isValidCustomerName(badName2));
        assertEquals("*** You", ProfanityFilter.filter(badName2));

        String badName3 = "Thằng Chó An";
        assertTrue(ProfanityFilter.containsProfanity(badName3));
        assertFalse(CustomerLoginController.isValidCustomerName(badName3));
        assertEquals("*** An", ProfanityFilter.filter(badName3));

        String badName4 = "Trần VCL";
        assertTrue(ProfanityFilter.containsProfanity(badName4));
        assertFalse(CustomerLoginController.isValidCustomerName(badName4));
        assertEquals("Trần ***", ProfanityFilter.filter(badName4));
    }
}
