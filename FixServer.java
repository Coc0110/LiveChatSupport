import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class FixServer {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("src/com/cmp180/livechat/server/ServerMain.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        content = content.replace("=== H? TH?NG", "=== HỆ THỐNG");
        content = content.replace("[K_T N?I MsI]", "[KẾT NỐI MỚI]");
        content = content.replace("L-i Server:", "Lỗi Server:");
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
