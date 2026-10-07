import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Patch {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/server/SessionManager.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        // Remove the early return
        content = content.replace("if (availableStaffs.isEmpty()) return;", "");
        
        // Change loop to allStaffs
        content = content.replace("for (ClientHandler staff : availableStaffs) {\n            staff.sendMessage(msg);\n        }", 
            "for (ClientHandler staff : allStaffs) {\n            staff.sendMessage(msg);\n        }");
            
        // We also need to make sure allStaffs is populated
        if (!content.contains("allStaffs.add(client)")) {
            content = content.replace("availableStaffs.add(client);", "availableStaffs.add(client);\n            allStaffs.add(client);");
            content = content.replace("availableStaffs.remove(client);", "availableStaffs.remove(client);\n        allStaffs.remove(client);");
        }
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("Patch 2 applied!");
    }
}
