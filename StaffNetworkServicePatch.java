import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class StaffNetworkServicePatch {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/network/StaffNetworkService.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        content = content.replace("l.onPairedWithCustomer(msg.getContent());", "l.onPairedWithCustomer(msg.getSenderId(), msg.getReceiverId());");
        content = content.replace("l.onMessageReceived(msg.getSenderId(), msg.getContent());", "l.onMessageReceived(msg.getSenderId(), msg.getReceiverId(), msg.getContent());");
        
        content = content.replace("public void sendChatMessage(String text) {\n        sendMessage(new Message(Protocol.CMD_CHAT, \"Staff\", text));\n    }",
            "public void sendChatMessage(String text, String customerId) {\n        sendMessage(new Message(Protocol.CMD_CHAT, \"Staff\", customerId, text));\n    }");
            
        content = content.replace("public void acceptCustomer(String customerId) {\n        sendMessage(new Message(Protocol.CMD_ACCEPT_CUST, \"Staff\", customerId));\n    }",
            "public void acceptCustomer(String customerId) {\n        sendMessage(new Message(Protocol.CMD_ACCEPT_CUST, \"Staff\", null, customerId));\n    }");
            
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("StaffNetworkServicePatch applied!");
    }
}
