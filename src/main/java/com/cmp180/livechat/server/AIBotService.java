package com.cmp180.livechat.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dịch vụ LCS chat bot hỗ trợ tự động cho khách hàng khi đang trong hàng chờ.
 * Hỗ trợ liên kết trực tiếp Google Gemini API (gemini-2.5-flash) bằng API Key của người dùng.
 * Cung cấp cơ chế tự động ghi nhật ký (Server Log) và chuyển đổi sang bộ quy tắc chuyên gia (Fallback) khi chưa có Key hoặc lỗi mạng.
 */
public class AIBotService {

    public static final String PRIMARY_MODEL = "gemini-2.5-flash";
    public static final String FALLBACK_MODEL = "gemini-flash-latest";

    private static volatile AIBotService instance;
    private volatile String customApiKey = null;
    private volatile boolean onlineGeminiEnabled = true;
    private final HttpClient httpClient;
    private Consumer<String> serverLogger = null;

    public static AIBotService getInstance() {
        if (instance == null) {
            synchronized (AIBotService.class) {
                if (instance == null) {
                    instance = new AIBotService();
                }
            }
        }
        return instance;
    }

    private AIBotService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Gán hàm nhận nhật ký từ Server (ServerGUI.log hoặc ServerMain.log).
     */
    public void setLogger(Consumer<String> logger) {
        this.serverLogger = logger;
    }

    /**
     * Ghi nhật ký ra console hoặc giao diện Server Log nếu đã liên kết logger.
     */
    public void log(String message) {
        if (serverLogger != null) {
            try {
                serverLogger.accept(message);
            } catch (Exception e) {
                System.out.println(message);
            }
        } else {
            System.out.println(message);
        }
    }

    /**
     * Cho phép bật/tắt gọi trực tuyến Google Gemini API (dùng cho môi trường thử nghiệm).
     */
    public void setOnlineGeminiEnabled(boolean enabled) {
        this.onlineGeminiEnabled = enabled;
    }

    public boolean isOnlineGeminiEnabled() {
        return onlineGeminiEnabled;
    }

    /**
     * Cho phép gán API Key động trực tiếp vào bộ nhớ.
     */
    public void setCustomApiKey(String apiKey) {
        this.customApiKey = (apiKey != null && !apiKey.trim().isEmpty()) ? apiKey.trim() : null;
    }

    /**
     * Thông tin chi tiết về việc tìm kiếm và nhận diện Gemini API Key.
     */
    public static class KeyResolution {
        public final boolean found;
        public final String key;
        public final String source;

        public KeyResolution(boolean found, String key, String source) {
            this.found = found;
            this.key = key;
            this.source = source;
        }

        public String getMaskedKey() {
            if (key == null || key.length() < 8) return "***";
            int prefixLen = Math.min(10, key.length() / 2);
            return key.substring(0, prefixLen) + "...***" + key.substring(key.length() - 3);
        }
    }

    /**
     * Quét và nhận diện Gemini API Key từ nhiều nguồn:
     * 1. API Key gán động qua bộ nhớ (customApiKey)
     * 2. Biến môi trường hệ điều hành: GEMINI_API_KEY
     * 3. Thuộc tính máy ảo Java: -DGEMINI_API_KEY=...
     * 4. Các tệp tin cấu hình: gemini_api_key.txt, config/gemini_api_key.txt (tại thư mục hiện tại, user.dir, parent, user.home)
     */
    public KeyResolution resolveApiKey() {
        if (customApiKey != null && !customApiKey.trim().isEmpty()) {
            return new KeyResolution(true, customApiKey.trim(), "Bộ nhớ ứng dụng (Dynamic Custom Key)");
        }

        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return new KeyResolution(true, envKey.trim(), "Biến môi trường hệ điều hành (GEMINI_API_KEY)");
        }

        String propKey = System.getProperty("GEMINI_API_KEY");
        if (propKey != null && !propKey.trim().isEmpty()) {
            return new KeyResolution(true, propKey.trim(), "Thuộc tính máy ảo Java (-DGEMINI_API_KEY)");
        }

        String userDir = System.getProperty("user.dir", ".");
        String userHome = System.getProperty("user.home", ".");

        List<File> candidateFiles = new ArrayList<>();
        candidateFiles.add(new File("gemini_api_key.txt"));
        candidateFiles.add(new File("config/gemini_api_key.txt"));
        candidateFiles.add(new File(userDir, "gemini_api_key.txt"));
        candidateFiles.add(new File(userDir, "config/gemini_api_key.txt"));
        candidateFiles.add(new File("../gemini_api_key.txt"));
        candidateFiles.add(new File(userHome, ".gemini/gemini_api_key.txt"));

        for (File f : candidateFiles) {
            if (f.exists() && f.isFile()) {
                try {
                    String raw = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                    if (raw.startsWith("\uFEFF")) {
                        raw = raw.substring(1);
                    }
                    String[] lines = raw.split("\\r?\\n");
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                            continue;
                        }
                        if (trimmed.startsWith("GEMINI_API_KEY=")) {
                            trimmed = trimmed.substring("GEMINI_API_KEY=".length()).trim();
                        }
                        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
                            (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
                            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
                        }
                        if (trimmed.length() >= 10) {
                            return new KeyResolution(true, trimmed, "Tệp tin '" + f.getName() + "' (" + f.getAbsolutePath() + ")");
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        return new KeyResolution(false, null, "Chưa tìm thấy (Đã quét: gemini_api_key.txt, config/gemini_api_key.txt, biến môi trường GEMINI_API_KEY)");
    }

    /**
     * Trả về API Key nếu có.
     */
    public String getGeminiApiKey() {
        KeyResolution res = resolveApiKey();
        return res.found ? res.key : null;
    }

    /**
     * Kiểm tra trạng thái Gemini API Key và thông báo rõ ràng lên Server Log.
     */
    public void checkAndLogStatus() {
        KeyResolution res = resolveApiKey();
        if (res.found) {
            log("[AI CHAT BOT] ================================================================");
            log("[AI CHAT BOT] [✓ KÍCH HOẠT] Google Gemini AI đã sẵn sàng hoạt động!");
            log("[AI CHAT BOT] - Nguồn nhận diện: " + res.source);
            log("[AI CHAT BOT] - Khóa API đã nhận: " + res.getMaskedKey() + " (Độ dài: " + res.key.length() + " ký tự)");
            log("[AI CHAT BOT] - Mô hình AI: " + PRIMARY_MODEL + " (Dự phòng: " + FALLBACK_MODEL + ")");
            log("[AI CHAT BOT] - Trạng thái: SẴN SÀNG trả lời tự động cho khách hàng trong hàng chờ.");
            log("[AI CHAT BOT] ================================================================");
        } else {
            log("[AI CHAT BOT] ================================================================");
            log("[AI CHAT BOT] [⚠️ CẢNH BÁO] CHƯA NHẬN DIỆN ĐƯỢC GOOGLE GEMINI API KEY!");
            log("[AI CHAT BOT] - Đã quét các vị trí: gemini_api_key.txt, config/gemini_api_key.txt, biến GEMINI_API_KEY");
            log("[AI CHAT BOT] - Hướng dẫn: Bạn vui lòng dán Gemini API Key vào file 'gemini_api_key.txt' tại thư mục gốc của Server.");
            log("[AI CHAT BOT] - Chế độ hiện tại: Hệ thống tạm thời sử dụng Bộ tri thức chuyên gia nội bộ (Local Fallback).");
            log("[AI CHAT BOT] ================================================================");
        }
    }

    /**
     * Tạo câu trả lời tự động:
     * Ưu tiên gọi Google Gemini API nếu có API Key; nếu không có hoặc gọi thất bại, tự động dùng tri thức nội bộ.
     */
    public String generateResponse(ClientHandler customer, String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Tôi có thể giúp gì cho bạn? Bạn hãy chia sẻ câu hỏi hoặc vấn đề bạn đang gặp phải nhé!";
        }

        KeyResolution keyRes = resolveApiKey();
        if (onlineGeminiEnabled && keyRes.found) {
            String geminiResponse = callGemini(keyRes.key, customer, query);
            if (geminiResponse != null && !geminiResponse.trim().isEmpty()) {
                return geminiResponse.trim();
            }
        } else if (onlineGeminiEnabled) {
            String customerName = (customer != null && customer.getName() != null) ? customer.getName() : "Khách hàng";
            log("[AI CHAT BOT] Khách hàng '" + customerName + "' gửi câu hỏi: \"" + query + "\"");
            log("[AI CHAT BOT] [⚠️ CHƯA CÓ KEY] Đang xử lý bằng Bộ tri thức chuyên gia nội bộ (Local Fallback).");
        }

        // Fallback: Dùng bộ tri thức & xử lý ngôn ngữ nội bộ (Local Knowledge Base)
        return generateLocalResponse(customer, query);
    }

    /**
     * Gửi yêu cầu đến Google Gemini API với cơ chế tự động thử model chính và model dự phòng.
     */
    private String callGemini(String apiKey, ClientHandler customer, String query) {
        String customerName = (customer != null && customer.getName() != null) ? customer.getName() : "Khách hàng";
        log("[AI CHAT BOT] Khách hàng '" + customerName + "' gửi câu hỏi: \"" + query + "\"");
        log("[AI CHAT BOT] Đang kết nối Google Gemini API (" + PRIMARY_MODEL + ")...");
        try {
            String answer = callGeminiWithModel(PRIMARY_MODEL, apiKey, customer, query);
            log("[AI CHAT BOT] [✓ THÀNH CÔNG] Google Gemini (" + PRIMARY_MODEL + ") phản hồi thành công (" + answer.length() + " ký tự).");
            return answer;
        } catch (Exception e1) {
            log("[AI CHAT BOT] [⚠️ CẢNH BÁO] Lỗi khi gọi mô hình " + PRIMARY_MODEL + ": " + e1.getMessage());
            try {
                log("[AI CHAT BOT] Đang thử kết nối mô hình dự phòng (" + FALLBACK_MODEL + ")...");
                String answer = callGeminiWithModel(FALLBACK_MODEL, apiKey, customer, query);
                log("[AI CHAT BOT] [✓ THÀNH CÔNG] Google Gemini (" + FALLBACK_MODEL + ") phản hồi thành công (" + answer.length() + " ký tự).");
                return answer;
            } catch (Exception e2) {
                log("[AI CHAT BOT] [❌ THẤT BÀI] Không thể kết nối Google Gemini API: " + e2.getMessage());
                log("[AI CHAT BOT] -> Tự động kích hoạt Bộ tri thức chuyên gia nội bộ (Local Fallback).");
            }
        }
        return null;
    }

    private String callGeminiWithModel(String model, String apiKey, ClientHandler customer, String query) throws Exception {
        String customerName = (customer != null && customer.getName() != null) ? customer.getName() : "Khách hàng";
        String topic = (customer != null && customer.getIssueTopic() != null) ? customer.getIssueTopic() : "Hỗ trợ chung";

        String systemPrompt = "Bạn là LCS chat bot, trợ lý chăm sóc khách hàng thông minh của hệ thống LiveChat Support (chuyên về thiết bị công nghệ, Laptop, PC, linh kiện máy tính, hỗ trợ kỹ thuật, bảo hành và đơn hàng).\n"
                + "Khách hàng đang trong hàng chờ nhân viên tư vấn. Hãy trả lời súc tích, tự nhiên, thân thiện bằng tiếng Việt, có sử dụng emoji phù hợp (không trả lời quá dài dòng gây ngợp khung chat).\n"
                + "Thông tin khách: Tên: " + customerName + ", Chủ đề quan tâm: " + topic + ".\n"
                + "QUY TẮC VỀ PHẠM VI HỖ TRỢ:\n"
                + "1. Bạn CHỈ CHUYÊN hỗ trợ: tư vấn mua Laptop/PC, linh kiện, khắc phục sự cố phần mềm/phần cứng máy tính, chính sách bảo hành đổi trả, và tra cứu đơn hàng vận chuyển.\n"
                + "2. NẾU KHÁCH HỎI NGOÀI CHỦ ĐỀ HỖ TRỢ (như nấu ăn, làm thơ, giải toán, bài tập, thời tiết, tin tức giải trí, đời tư cá nhân, chính trị...): Hãy từ chối một cách lịch sự, nhã nhặn trong 1-2 câu; giải thích rằng bạn là trợ lý chuyên trách sản phẩm công nghệ của LiveChat Support nên không hỗ trợ lĩnh vực này, và khéo léo hỏi xem khách có cần hỗ trợ gì về máy tính, kỹ thuật hoặc đơn hàng không.\n"
                + "3. Nếu khách cung cấp số điện thoại hoặc mã đơn hàng: Hãy xác nhận bạn đã lưu lại để nhân viên CSKH tra cứu ngay khi tiếp nhận.\n"
                + "4. Nếu khách muốn gặp người thật / nhân viên tư vấn: Thông báo hệ thống đang điều phối nhân viên phù hợp nhất.";

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        String jsonPayload = "{\n"
                + "  \"system_instruction\": {\n"
                + "    \"parts\": [{\"text\": \"" + escapeJson(systemPrompt) + "\"}]\n"
                + "  },\n"
                + "  \"contents\": [\n"
                + "    {\"role\": \"user\", \"parts\": [{\"text\": \"" + escapeJson(query) + "\"}]}\n"
                + "  ],\n"
                + "  \"generationConfig\": {\n"
                + "    \"temperature\": 0.7,\n"
                + "    \"maxOutputTokens\": 800,\n"
                + "    \"thinkingConfig\": {\n"
                + "      \"thinkingBudget\": 0\n"
                + "    }\n"
                + "  }\n"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            String extracted = extractTextFromGeminiResponse(responseBody);
            if (extracted != null && !extracted.trim().isEmpty()) {
                return extracted.trim();
            }
            throw new IOException("HTTP 200 nhưng không trích xuất được nội dung trả về: " + responseBody);
        } else {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    private String extractTextFromGeminiResponse(String json) {
        if (json == null) return null;
        int candIdx = json.indexOf("\"candidates\"");
        if (candIdx == -1) return null;

        StringBuilder result = new StringBuilder();
        int searchIdx = candIdx;

        while (true) {
            int textIdx = json.indexOf("\"text\"", searchIdx);
            if (textIdx == -1) break;

            int usageIdx = json.indexOf("\"usageMetadata\"");
            if (usageIdx != -1 && textIdx > usageIdx) {
                break;
            }

            int colonIdx = json.indexOf(':', textIdx);
            if (colonIdx == -1) break;
            int startQuote = json.indexOf('"', colonIdx);
            if (startQuote == -1) break;

            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            int i = startQuote + 1;
            for (; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escape) {
                    if (c == 'n') sb.append('\n');
                    else if (c == 'r') sb.append('\r');
                    else if (c == 't') sb.append('\t');
                    else if (c == '"') sb.append('"');
                    else if (c == '\\') sb.append('\\');
                    else if (c == 'u' && i + 4 < json.length()) {
                        String hex = json.substring(i + 1, i + 5);
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (Exception ex) {
                            sb.append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            if (sb.length() > 0) {
                if (result.length() > 0) result.append("\n");
                result.append(sb);
            }
            searchIdx = i + 1;
        }

        String res = result.toString().trim();
        return res.isEmpty() ? null : res;
    }

    /**
     * Bộ tri thức chuyên gia nội bộ dự phòng (Fallback)
     */
    public String generateLocalResponse(ClientHandler customer, String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        String topic = customer != null ? customer.getIssueTopic() : "Tư vấn chung";
        String customerName = (customer != null && customer.getName() != null) ? customer.getName() : "bạn";

        // 1. Nhận diện số điện thoại cung cấp (ví dụ: 0333666999, +84988...)
        Matcher phoneMatcher = Pattern.compile("(?:\\+84|0)[3|5|7|8|9][0-9]{8}").matcher(q.replaceAll("[\\s.-]", ""));
        if (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            return "Dạ LCS chat bot đã ghi nhận số điện thoại của bạn: **" + phone + "** 📱\n" +
                   "Thông tin liên hệ này đã được chuyển đến nhân viên CSKH phụ trách. Nhân viên sẽ tra cứu ngay lộ trình đơn hàng và gọi hỗ trợ bạn ngay khi kết nối nhé!";
        }

        // 2. Nhận diện mã đơn hàng (ví dụ: DH12345, LCS6789, #123456)
        Matcher orderMatcher = Pattern.compile("(?:dh|lcs|bill|order|#)[0-9]{4,12}").matcher(q);
        if (orderMatcher.find() && !matchesAny(q, "rtx", "ram", "i5", "i7", "năm")) {
            String orderCode = orderMatcher.group().toUpperCase();
            return "Dạ LCS chat bot đã ghi nhận mã đơn hàng: **" + orderCode + "** 📦\n" +
                   "Hệ thống đã lưu lại mã đơn để nhân viên CSKH tra cứu ngay lộ trình và tình trạng kiện hàng khi tiếp nhận hỗ trợ bạn nhé!";
        }

        // 3. Khách hàng xác nhận thông tin đã gửi ("ở trên", "vừa đưa", "vừa gửi", "gửi rồi", "đưa rồi")
        if (matchesAny(q, "ở trên", "vừa đưa", "đưa rồi", "vừa gửi", "gửi rồi", "vừa nhắn", "nhìn ở trên", "đã cho", "đã nói", "phía trên")) {
            return "Dạ LCS chat bot đã thấy thông tin bạn cung cấp ở trên rồi ạ! 👍\n" +
                   "Thông tin này đã được đính kèm vào phiên hỗ trợ. Bạn vui lòng đợi trong giây lát, nhân viên CSKH sẽ kết nối và xử lý ngay cho bạn nhé!";
        }

        // 4. Khách hàng phản hồi về câu nói của bot / yêu cầu đổi câu ("nói gì khác", "lặp lại", "nói câu khác", "chán thế"...)
        if (matchesAny(q, "nói gì khác", "câu khác", "lặp lại", "nói câu khác", "chán thế", "chán quá", "nói mãi", "mỗi một câu", "cứ nói hoài", "nói gì mới", "lặp đi lặp lại", "cứ lặp")) {
            return "Dạ thành thật xin lỗi bạn vì câu trả lời trước đó làm bạn phiền lòng! 🙏\n" +
                   "Tôi là LCS chat bot hỗ trợ tự động trong hàng chờ. Tôi có thể hỗ trợ bạn:\n" +
                   "• 📦 Tra cứu thời gian giao hàng & quy định đồng kiểm\n" +
                   "• 🛡️ Chính sách bảo hành 1-đổi-1 & bảo hành điện tử\n" +
                   "• 💻 Tư vấn Laptop & Cấu hình PC theo ngân sách\n" +
                   "• 🔧 Hướng dẫn xử lý lỗi màn hình xanh, sập nguồn, quá nhiệt\n" +
                   "Hoặc bạn chỉ cần đợi chút xíu, hệ thống sẽ kết nối với Nhân viên CSKH ngay khi có người rảnh nhé!";
        }

        // 5. Chào hỏi ban đầu
        if (matchesAny(q, "chào", "hello", "hi", "alo", "ơi", "ad ơi", "bot ơi", "ai ơi", "hey")) {
            return "Xin chào " + customerName + "! 👋\nTôi là LCS chat bot của LiveChat Support. Tôi có thể hỗ trợ bạn thông tin về sản phẩm, kỹ thuật, bảo hành hoặc đơn hàng. Bạn cần tôi trợ giúp điều gì ạ?";
        }

        // 6. Yêu cầu gặp nhân viên / tư vấn viên trực tiếp
        if (matchesAny(q, "nhân viên", "người thật", "tổng đài", "gặp người", "kết nối nhân viên", "chuyển người", "chuyển nhân viên", "tư vấn viên")) {
            return "📌 Tôi đã ghi nhận yêu cầu muốn kết nối với Nhân viên CSKH của bạn.\nHiện tại hệ thống đang tìm kiếm nhân viên phù hợp nhất. Ngay khi có nhân viên sẵn sàng, màn hình sẽ hiển thị thông báo để bạn chuyển qua ngay nhé!";
        }

        // 7. Lời cảm ơn / tạm biệt
        if (matchesAny(q, "cảm ơn", "cam on", "thank", "tks", "ok cảm ơn", "ok bạn", "tuyệt", "good")) {
            return "Dạ không có gì ạ! Rất vui được hỗ trợ " + customerName + ". Nếu cần thêm thông tin gì, bạn cứ nhắn cho tôi nhé. Chúc bạn một ngày tốt lành! 😊";
        }
        if (matchesAny(q, "tạm biệt", "tam biet", "bye", "goodbye")) {
            return "Tạm biệt bạn! Cảm ơn bạn đã liên hệ LiveChat Support. Chúc bạn luôn có những trải nghiệm tuyệt vời! 👋";
        }

        // 8. Hỗ trợ Kỹ thuật & Cài đặt
        if (matchesAny(q, "đen màn hình", "không lên", "mất nguồn", "sập nguồn", "bật không sáng", "bị tắt")) {
            return "🔧 **Hướng dẫn khắc phục sự cố máy không lên màn hình / sập nguồn:**\n" +
                   "1. **Xả tĩnh điện máy:** Rút sạc và tất cả thiết bị ngoại vi (chuột, USB), giữ phím Nguồn liên tục trong 20 giây rồi thả ra.\n" +
                   "2. **Kiểm tra bộ sạc:** Cắm sạc trực tiếp vào ổ điện tường (chờ 5 phút xem đèn LED báo sạc có sáng không).\n" +
                   "3. **Kiểm tra màn hình ngoài:** Nếu máy vẫn chạy quạt nhưng màn hình tối, thử cắm qua cổng HDMI sang TV/màn hình ngoài.\n" +
                   "Nếu các bước trên vẫn không lên, máy có thể bị lỏng chân RAM hoặc lỗi bo mạch. Nhân viên kỹ thuật sẽ kiểm tra chi tiết cho bạn ngay khi kết nối!";
        }
        if (matchesAny(q, "nóng", "nhiệt độ", "quạt kêu", "kêu to", "quá nhiệt", "lag", "đơ")) {
            return "❄️ **Cách giảm nhiệt độ và hạn chế quạt kêu to:**\n" +
                   "• Kê cao mặt đáy laptop khoảng 2 - 3cm hoặc dùng đế tản nhiệt để gió lưu thông tốt.\n" +
                   "• Tuyệt đối không đặt laptop lên nệm, chăn gối khi dùng vì sẽ bít kín khe thoát khí.\n" +
                   "• Vệ sinh bụi và tra keo tản nhiệt định kỳ mỗi 6 - 9 tháng (Shop có gói vệ sinh chuyên sâu miễn phí cho khách hàng).\n" +
                   "• Bật chế độ 'Performance' hoặc 'Balanced' trong phần mềm quản trị quạt của hãng (Armoury Crate, Legion Vantage...).";
        }
        if (matchesAny(q, "màn hình xanh", "bsod", "dump", "dump file", "lỗi win", "cài win", "windows", "driver", "wifi", "bluetooth", "âm thanh")) {
            return "🛠️ **Khắc phục lỗi Windows & Driver:**\n" +
                   "• **Lỗi Màn hình xanh (BSOD):** Thường do xung đột driver card đồ họa hoặc Windows Update. Bạn hãy khởi động vào chế độ Safe Mode và gỡ bản cập nhật gần nhất.\n" +
                   "• **Mất Wifi / Bluetooth:** Nhấn phím Windows + X -> Device Manager -> Network Adapters -> Chuột phải vào card Wifi chọn 'Update driver' hoặc khởi động lại máy.\n" +
                   "• Nếu cần cài đặt Windows bản quyền hoặc phần mềm đồ họa, nhân viên có thể Ultraviewer/TeamViewer hỗ trợ bạn từ xa!";
        }

        // 9. Chính sách Bảo hành & Đổi trả
        if (matchesAny(q, "bảo hành", "đổi trả", "đổi máy", "chính sách", "tem", "hư", "hỏng", "sửa")) {
            return "🛡️ **Chính sách Bảo hành & Đổi trả chính hãng:**\n" +
                   "• **Đổi mới 1 - đổi - 1 trong 30 ngày:** Áp dụng nếu máy phát sinh lỗi phần cứng từ nhà sản xuất (lỗi màn hình, nguồn, bo mạch, ổ cứng).\n" +
                   "• **Thời hạn bảo hành:** 12 đến 24 tháng theo số Serial Number / IMEI trên thân máy (bảo hành điện tử, không cần giữ hóa đơn giấy).\n" +
                   "• **Điều kiện bảo hành:** Máy còn nguyên tem bảo hành, không có dấu hiệu cấn móp va đập mạnh, nứt vỡ hoặc vào nước.\n" +
                   "• **Thời gian xử lý:** Từ 3 - 7 ngày làm việc.";
        }

        // 10. Tra cứu Đơn hàng & Giao nhận
        if (matchesAny(q, "đơn hàng", "đơn", "vận chuyển", "giao hàng", "giao đến", "giao tới", "ship", "bao lâu", "khi nào", "chừng nào", "mấy ngày", "đến nơi", "tới nơi", "mã vận đơn", "tra cứu", "tiền ship", "phí ship", "thanh toán", "nhận hàng")) {
            return "📦 **Thông tin Giao hàng & Vận chuyển:**\n" +
                   "• **Khu vực nội thành:** Giao hỏa tốc trong **2 - 4 giờ**.\n" +
                   "• **Các tỉnh thành khác:** Giao tiêu chuẩn từ **2 - 3 ngày làm việc** qua Viettel Post / GHTK.\n" +
                   "• **Quy định nhận hàng:** Quý khách được **đồng kiểm ngoại quan** trước khi thanh toán (mở hộp kiểm tra máy nguyên vẹn không trầy xước).\n" +
                   "👉 Bạn có thể gửi **Số điện thoại** hoặc **Mã đơn hàng** để LCS chat bot chuyển cho nhân viên tra cứu chính xác lộ trình kiện hàng cho bạn nhé!";
        }

        // 11. Tư vấn Mua Laptop & Build PC
        if (matchesAny(q, "laptop", "pc", "máy tính", "build pc", "mua máy", "cấu hình", "gaming", "văn phòng", "đồ họa", "render", "ram", "cpu", "card màn hình", "vga", "rtx", "i5", "i7", "ryzen", "macbook")) {
            if (matchesAny(q, "build pc", "cây máy tính", "case", "lắp pc")) {
                return "🖥️ **Cấu hình PC khuyên dùng năm 2026:**\n" +
                       "• **Cấu hình Gaming 15 triệu:** Core i5-12400F | B760M | 16GB DDR4 | RTX 3060 12GB | Nguồn 650W 80 Plus.\n" +
                       "• **Cấu hình Gaming/Đồ họa 25 triệu:** Core i5-13400F hoặc Ryzen 5 7600 | 32GB DDR5 | RTX 4060Ti | SSD 1TB NVMe | Nguồn 750W.\n" +
                       "Bên mình hỗ trợ lắp ráp miễn phí, bảo hành tận nơi 3 năm cho toàn bộ linh kiện!";
            }
            if (matchesAny(q, "gaming", "chơi game", "game")) {
                return "💻 **Gợi ý Laptop Gaming nổi bật theo tầm giá:**\n" +
                       "• **16 - 20 triệu:** Asus TUF Gaming F15, Acer Nitro V15 (Core i5 gen 13, RTX 2050/3050, 16GB RAM) - Chơi mượt LOL, Valorant, GTA V, CS2.\n" +
                       "• **20 - 30 triệu:** Lenovo LOQ 15, Acer Nitro 16, Asus TUF A15 (Ryzen 7 / i7, RTX 4050/4060) - Chiến mượt Cyberpunk 2077, Black Myth Wukong.\n" +
                       "• **Trên 30 triệu:** Lenovo Legion Pro 5, ROG Strix G16 (RTX 4070, Màn 2K 165Hz-240Hz).\n" +
                       "👉 Ngân sách dự kiến của bạn khoảng bao nhiêu triệu để tôi tư vấn cấu hình tối ưu nhất?";
            }
            if (matchesAny(q, "văn phòng", "học tập", "sinh viên", "nhẹ", "mỏng nhẹ")) {
                return "💼 **Gợi ý Laptop Văn phòng & Học tập mỏng nhẹ, pin trâu:**\n" +
                       "• **Dưới 15 triệu:** Asus Vivobook 15, Acer Aspire 5, HP 14s (Core i3/i5, RAM 16GB, SSD 512GB).\n" +
                       "• **15 - 25 triệu:** Lenovo ThinkBook 14 G6, Dell Inspiron 5430, HP Pavilion 14, MacBook Air M1/M2.\n" +
                       "• **Yêu cầu khuyên dùng:** Tối thiểu 16GB RAM để mở nhiều tab Chrome và tác vụ mượt mà không bị giật lag!";
            }
            return "💻 **Tư vấn Thiết bị & Cấu hình:**\n" +
                   "Hệ thống hiện sẵn hàng nhiều dòng Laptop Gaming, Laptop Văn phòng và linh kiện PC chính hãng từ ASUS, Lenovo, Dell, MSI, Apple.\n" +
                   "Bạn có thể cho tôi biết ngân sách dự kiến và nhu cầu chính (học tập, code, đồ họa, chơi game) để tôi gợi ý mẫu máy ưng ý nhất nhé!";
        }

        // 12. Câu hỏi ngoài phạm vi hỗ trợ (nấu ăn, bài tập, thời tiết, giải trí, triết học...)
        if (matchesAny(q, "nấu ăn", "món ăn", "công thức", "thời tiết", "bài tập", "giải toán", "làm thơ", "hát", "kể chuyện", "yêu", "tình yêu", "bói", "xem bói", "chính trị", "tổng thống", "bóng đá", "thể thao")) {
            return "Dạ LCS chat bot là trợ lý chuyên trách về Laptop, PC, Kỹ thuật và Đơn hàng tại LiveChat Support nên xin phép chưa thể hỗ trợ chủ đề này ạ! 😊\n" +
                   "Bạn có cần tôi tư vấn về thiết bị công nghệ, kiểm tra thời gian giao hàng hay chính sách bảo hành không ạ?";
        }

        // 13. Trả lời mặc định thông minh theo Topic ban đầu của khách
        if (topic.contains("Kỹ thuật") || topic.contains("Cài đặt")) {
            return "🔧 LCS chat bot đã ghi nhận yêu cầu kỹ thuật của bạn: *\"" + query + "\"*.\n" +
                   "Bạn có thể chia sẻ thêm mã lỗi hiển thị hoặc máy có dấu hiệu va chạm, vào nước trước đó không để nhân viên kỹ thuật chẩn đoán chính xác nhé!";
        } else if (topic.contains("Mua Laptop") || topic.contains("Build PC")) {
            return "💡 LCS chat bot đã ghi nhận nhu cầu tư vấn: *\"" + query + "\"*.\n" +
                   "Bạn có thể cho tôi biết thêm mức ngân sách dự kiến (ví dụ: 15-20 triệu) và nhu cầu chính để tôi gợi ý cấu hình phù hợp nhất nhé!";
        } else if (topic.contains("Bảo hành") || topic.contains("Đổi trả")) {
            return "🛡️ LCS chat bot đã ghi nhận câu hỏi bảo hành: *\"" + query + "\"*.\n" +
                   "Hệ thống hỗ trợ 1-đổi-1 trong 30 ngày và bảo hành chính hãng 12-24 tháng. Bạn có thể gửi số Serial Number trên thân máy để nhân viên tra cứu thời hạn bảo hành nhé!";
        } else if (topic.contains("Đơn hàng") || topic.contains("Giao vận")) {
            return "📦 LCS chat bot đã lưu lại tin nhắn của bạn về đơn hàng: *\"" + query + "\"*.\n" +
                   "Thời gian giao hàng tiêu chuẩn là 2-4h (nội thành) hoặc 2-3 ngày (tỉnh). Nhân viên CSKH sẽ kiểm tra trực tiếp tiến độ đơn hàng và phản hồi cho bạn ngay khi kết nối!";
        }

        // 14. Phản hồi chung tổng quát
        return "🤖 LCS chat bot chuyên hỗ trợ về Laptop, PC, Khắc phục sự cố, Bảo hành và Đơn hàng.\n" +
               "Đối với câu hỏi của bạn, hệ thống đang sắp xếp Nhân viên CSKH để hỗ trợ bạn chi tiết nhất nhé!";
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (matchesKeyword(text, kw)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesKeyword(String text, String kw) {
        if (kw.length() <= 3) {
            String[] tokens = text.split("[\\s,?.!;:\"'()]+");
            for (String t : tokens) {
                if (t.equalsIgnoreCase(kw)) return true;
            }
            return false;
        }
        return text.contains(kw);
    }

    public static void main(String[] args) {
        AIBotService bot = AIBotService.getInstance();
        bot.setLogger(System.out::println);
        bot.checkAndLogStatus();
        String testQuery = (args != null && args.length > 0) ? String.join(" ", args) : "Tư vấn mua laptop 20 triệu";
        System.out.println("\n[THỬ NGHIỆM TRỰC TIẾP] Gửi câu hỏi: \"" + testQuery + "\"");
        String reply = bot.generateResponse(null, testQuery);
        System.out.println("\n[PHẢN HỒI TỪ BOT]:\n" + reply);
    }
}
