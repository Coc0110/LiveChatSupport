package com.cmp180.livechat.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bộ kiểm duyệt và lọc ngôn từ vi phạm tiêu chuẩn cộng đồng.
 * Chuyển các từ ngữ thô tục, bậy bạ thành '***'.
 */
public class ProfanityFilter {

    private static final List<Pattern> PROFANITY_PATTERNS = new ArrayList<>();

    static {
        // 1. Các cụm từ thô tục / xúc phạm nhiều từ (Ưu tiên kiểm tra trước)
        String[] multiWordPhrases = {
            // Tiếng Việt có dấu & không dấu (hỗ trợ kéo dài nguyên âm vd: mẹeee, máaaa)
            "đ+ị+t+[\\s\\._\\-]*m+[eéèẻẽẹêếềểễệ]+", "d+i+t+[\\s\\._\\-]*m+[eéèẻẽẹêếềểễệ]+",
            "đ+ị+t+[\\s\\._\\-]*b+[aáàảãạâấầẩẫậăắằẳẵặ]+", "d+i+t+[\\s\\._\\-]*b+[aáàảãạâấầẩẫậăắằẳẵặ]+",
            "đ+ị+t+[\\s\\._\\-]*c+[uúùủũụưứừửữự]+", "d+i+t+[\\s\\._\\-]*c+[uúùủũụưứừửữự]+",
            "đ+ụ+[\\s\\._\\-]*m+[aáàảãạâấầẩẫậăắằẳẵặ]+", "d+u+[\\s\\._\\-]*m+[aáàảãạâấầẩẫậăắằẳẵặ]+",
            "đ+ụ+[\\s\\._\\-]*m+[eéèẻẽẹêếềểễệ]+", "d+u+[\\s\\._\\-]*m+[eéèẻẽẹêếềểễệ]+",
            "đ+ụ+[\\s\\._\\-]*c+[uúùủũụưứừửữự]+", "d+u+[\\s\\._\\-]*c+[uúùủũụưứừửữự]+",
            "đ+ụ+[\\s\\._\\-]*c+h+[aáàảãạâấầẩẫậăắằẳẵặ]+", "d+u+[\\s\\._\\-]*c+h+[aáàảãạâấầẩẫậăắằẳẵặ]+",
            "c+h+[oóòỏõọôốồổỗộơớờởỡợ]+[\\s\\._\\-]*đ+[eéèẻẽẹêếềểễệ]+", "c+h+o+[\\s\\._\\-]*d+e+",
            "c+h+[oóòỏõọôốồổỗộơớờởỡợ]+[\\s\\._\\-]*c+h+[eéèẻẽẹêếềểễệ]+t+", "c+h+o+[\\s\\._\\-]*c+h+e+t+",
            "m+ẹ+[\\s\\._\\-]*k+i+[eéèẻẽẹêếềểễệ]+p+", "m+e+[\\s\\._\\-]*k+i+e+p+",
            "đ+ĩ+[\\s\\._\\-]*m+[eéèẻẽẹêếềểễệ]+", "d+i+[\\s\\._\\-]*m+[eéèẻẽẹêếềểễệ]+",
            "c+o+n+[\\s\\._\\-]*đ+ĩ+", "c+o+n+[\\s\\._\\-]*d+i+",
            "t+h+ằ+n+g+[\\s\\._\\-]*c+h+[oóòỏõọôốồổỗộơớờởỡợ]+", "t+h+a+n+g+[\\s\\._\\-]*c+h+o+",
            "m+[eéèẻẽẹêếềểễệ]+[\\s\\._\\-]*m+[aáàảãạâấầẩẫậăắằẳẵặ]+y+", "m+e+[\\s\\._\\-]*m+a+y+",
            "b+[oóòỏõọôốồổỗộơớờởỡợ]+[\\s\\._\\-]*m+[aáàảãạâấầẩẫậăắằẳẵặ]+y+", "b+o+[\\s\\._\\-]*m+a+y+",
            "ó+c+[\\s\\._\\-]*c+h+[oóòỏõọôốồổỗộơớờởỡợ]+", "o+c+[\\s\\._\\-]*c+h+o+",
            "n+g+u+[\\s\\._\\-]*n+h+ư+[\\s\\._\\-]*c+h+[oóòỏõọôốồổỗộơớờởỡợ]+", "n+g+u+[\\s\\._\\-]*n+h+u+[\\s\\._\\-]*c+h+o+",
            "k+h+ố+n+[\\s\\._\\-]*n+ạ+n+", "k+h+o+n+[\\s\\._\\-]*n+a+n+",
            "m+ấ+t+[\\s\\._\\-]*d+ạ+y+", "m+a+t+[\\s\\._\\-]*d+a+y+",
            "c+h+ế+t+[\\s\\._\\-]*t+i+ệ+t+", "c+h+e+t+[\\s\\._\\-]*t+i+e+t+",
            "đ+ồ+[\\s\\._\\-]*c+h+[oóòỏõọôốồổỗộơớờởỡợ]+", "d+o+[\\s\\._\\-]*c+h+o+",
            "c+á+i+[\\s\\._\\-]*l+[oóòỏõọôốồổỗộơớờởỡợ]+n+", "c+a+i+[\\s\\._\\-]*l+o+n+",
            "c+o+n+[\\s\\._\\-]*c+[aáàảãạâấầẩẫậăắằẳẵặ]+c+", "c+o+n+[\\s\\._\\-]*c+a+c+",

            // Tiếng Anh
            "m+o+t+h+e+r+[\\s\\._\\-]*f+u+c+k+e+r+",
            "m+o+t+h+e+r+[\\s\\._\\-]*f+u+c+k+i+n+g+",
            "s+o+n+[\\s\\._\\-]*o+f+[\\s\\._\\-]*a+[\\s\\._\\-]*b+i+t+c+h+",
            "b+u+l+l+[\\s\\._\\-]*s+h+i+t+"
        };

        // 2. Các từ đơn thô tục, viết tắt chửi thề (Cần ranh giới từ để tránh Scunthorpe problem)
        String[] singleWords = {
            // Tiếng Việt có dấu tuyệt đối
            "đ+ị+t+", "đ+ụ+", "đ+é+[oóòỏõọ]+", "đ+é+c+","đ+j+t","d+j+t",
            "l+ồ+[non]*", "c+ặ+[ca]*", "b+u+ồ+[io]*", "d+á+i+", "đ+ĩ+",

            // Viết tắt chửi bậy tiếng Việt (hỗ trợ kèm dấu chấm, gạch nối giữa các chữ cái vd: đ.m, v.c.l)
            "đ+[\\._\\-]*m+", "d+[\\._\\-]*m+", "đ+[\\._\\-]*m+[\\._\\-]*m+", "d+[\\._\\-]*m+[\\._\\-]*m+",
            "đ+[\\._\\-]*c+[\\._\\-]*m+", "d+[\\._\\-]*c+[\\._\\-]*m+",
            "đ+[\\._\\-]*k+[\\._\\-]*m+", "d+[\\._\\-]*k+[\\._\\-]*m+",
            "đ+[\\._\\-]*c+[\\._\\-]*l+", "d+[\\._\\-]*c+[\\._\\-]*l+",
            "v+[\\._\\-]*c+[\\._\\-]*l+", "v+[\\._\\-]*c+[\\._\\-]*c+", "v+[\\._\\-]*l+", "v+[\\._\\-]*k+[\\._\\-]*l+", "v+l+o+n+", "v+l+o+z+", "v+[\\._\\-]*c+[\\._\\-]*l+l+",
            "c+[\\._\\-]*l+[\\._\\-]*g+[\\._\\-]*t+", "c+[\\._\\-]*đ+[\\._\\-]*m+", "c+[\\._\\-]*d+[\\._\\-]*m+",

            // Tiếng Anh thô tục
            "f+u+c+k+", "f+u+c+k+i+n+g+", "f+u+c+k+e+r+", "f+u+c+k+e+d+", "f\\*c+k+",
            "s+h+i+t+", "b+i+t+c+h+", "b+i+t+c+h+e+s+", "a+s+s+h+o+l+e+", "b+a+s+t+a+r+d+",
            "c+u+n+t+", "p+u+s+s+y+", "d+i+c+k+",
            "w+t+f+", "s+t+f+u+"
        };

        for (String phrase : multiWordPhrases) {
            String regex = "(?i)(?<!\\p{L})" + phrase + "(?!\\p{L})";
            PROFANITY_PATTERNS.add(Pattern.compile(regex, Pattern.UNICODE_CASE));
        }

        for (String word : singleWords) {
            String regex = "(?i)(?<!\\p{L})" + word + "(?!\\p{L})";
            PROFANITY_PATTERNS.add(Pattern.compile(regex, Pattern.UNICODE_CASE));
        }
    }

    /**
     * Kiểm tra chuỗi văn bản có chứa từ ngữ vi phạm tiêu chuẩn cộng đồng hay không.
     */
    public static boolean containsProfanity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        for (Pattern p : PROFANITY_PATTERNS) {
            if (p.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lọc và chuyển đổi các từ ngữ vi phạm tiêu chuẩn cộng đồng thành '***'.
     */
    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (Pattern p : PROFANITY_PATTERNS) {
            Matcher m = p.matcher(result);
            if (m.find()) {
                result = m.replaceAll("***");
            }
        }
        return result;
    }

    /**
     * Lấy danh sách các từ ngữ vi phạm được phát hiện trong văn bản.
     */
    public static List<String> findProfanities(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> found = new ArrayList<>();
        for (Pattern p : PROFANITY_PATTERNS) {
            Matcher m = p.matcher(text);
            while (m.find()) {
                String match = m.group();
                if (!found.contains(match)) {
                    found.add(match);
                }
            }
        }
        return found;
    }
}
