import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class FixMessage {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/ZaloChatController.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        String oldMethod = 
            "    @Override\n" +
            "    public void onMessageReceived(String senderName, String message) {\n" +
            "        Platform.runLater(() -> {\n" +
            "            listMessages.getItems().add(senderName + \": \" + message);\n" +
            "            // Tá»± Ä‘á»™ng cuá»™n xuá»‘ng cuá»‘i\n" +
            "            listMessages.scrollTo(listMessages.getItems().size() - 1);\n" +
            "        });\n" +
            "    }";

        String oldMethodAlt = 
            "    @Override\r\n" +
            "    public void onMessageReceived(String senderName, String message) {\r\n" +
            "        Platform.runLater(() -> {\r\n" +
            "            listMessages.getItems().add(senderName + \": \" + message);\r\n" +
            "            // Tá»± Ä‘á»™ng cuá»™n xuá»‘ng cuá»‘i\r\n" +
            "            listMessages.scrollTo(listMessages.getItems().size() - 1);\r\n" +
            "        });\r\n" +
            "    }";
            
        String newMethod = 
            "    @Override\n" +
            "    public void onMessageReceived(String senderName, String senderId, String message) {\n" +
            "        Platform.runLater(() -> {\n" +
            "            chatHistories.computeIfAbsent(senderId, k -> new ArrayList<>()).add(senderName + \": \" + message);\n" +
            "            if (senderId.equals(currentCustomerId)) {\n" +
            "                listMessages.getItems().add(senderName + \": \" + message);\n" +
            "                listMessages.scrollTo(listMessages.getItems().size() - 1);\n" +
            "            }\n" +
            "        });\n" +
            "    }";
            
        content = content.replace(oldMethod, newMethod);
        content = content.replace(oldMethodAlt, newMethod);
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("Message method fixed!");
    }
}
