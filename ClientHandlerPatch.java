import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class ClientHandlerPatch {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/server/ClientHandler.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        // Remove 'private ChatSession session;'
        // We will just let SessionManager handle it.
        // Actually, to make it easier, let ClientHandler still have ONE session for Customers,
        // but for Staff, they use SessionManager.routeMessage
        
        String oldChatLogic = 
            "            case Protocol.CMD_CHAT:\n" +
            "                if (session != null) {\n" +
            "                    ClientHandler partner = (this == session.getCustomer()) ? session.getStaff() : session.getCustomer();\n" +
            "                    if (partner != null) {\n" +
            "                        // Gá»\u00ADi Message qua bÃªn kia\n" +
            "                        partner.sendMessage(new Message(Protocol.CMD_CHAT, this.name, msg.getContent()));\n" +
            "                    }\n" +
            "                } else {\n" +
            "                    sendMessage(new Message(Protocol.CMD_ERROR, \"Server\", \"Báº¡n chÆ°a Ä‘Æ°á»£c káº¿t ná»‘i vá»›i ai Ä‘á»ƒ chat.\"));\n" +
            "                }\n" +
            "                break;";

        String newChatLogic = 
            "            case Protocol.CMD_CHAT:\n" +
            "                sessionManager.routeMessage(this, msg);\n" +
            "                break;";
            
        // We can just use string replace but Mojibake makes it hard. We'll use regex.
        content = content.replaceAll("(?s)case Protocol\\.CMD_CHAT:.*?break;", newChatLogic);
        
        String oldEndLogic = 
            "            case Protocol.CMD_END_SESSION:\n" +
            "                sessionManager.closeSession(this.session, this.name + \" Ä‘Ã£ káº¿t thÃºc phiÃªn chat.\");\n" +
            "                break;";
            
        String newEndLogic = 
            "            case Protocol.CMD_END_SESSION:\n" +
            "                sessionManager.endSession(this, msg.getReceiverId(), this.name + \" Ä‘Ã£ káº¿t thÃºc phiÃªn chat.\");\n" +
            "                break;";
            
        content = content.replaceAll("(?s)case Protocol\\.CMD_END_SESSION:.*?break;", newEndLogic);
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("ClientHandlerPatch applied!");
    }
}
