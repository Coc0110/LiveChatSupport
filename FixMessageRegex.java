import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class FixMessageRegex {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/ZaloChatController.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
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
            "    }\n";
            
        content = content.replaceAll("(?s)    @Override\\s*public void onMessageReceived\\(String senderName, String message\\) \\{.*?    \\}", newMethod);
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("Message method fixed via Regex!");
    }
}
