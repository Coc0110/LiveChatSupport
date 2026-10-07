import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class RestoreQueueUpdate {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/ZaloChatController.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        String method = 
            "    @Override\n" +
            "    public void onQueueUpdated(Map<String, String> waitingCustomers) {\n" +
            "        Platform.runLater(() -> {\n" +
            "            currentQueueMap = waitingCustomers;\n" +
            "            listClients.getItems().clear();\n" +
            "            for (String name : waitingCustomers.values()) {\n" +
            "                listClients.getItems().add(name);\n" +
            "            }\n" +
            "        });\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    public void onPairedWithCustomer";
            
        content = content.replace("    @Override\n    public void onPairedWithCustomer", method);
        content = content.replace("    @Override\r\n    public void onPairedWithCustomer", method);
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("Restored onQueueUpdated");
    }
}
