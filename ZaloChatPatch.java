import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class ZaloChatPatch {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/ZaloChatController.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        // Add required fields
        String imports = "import java.util.ArrayList;\nimport java.util.List;\n";
        content = content.replace("import java.util.HashMap;", imports + "import java.util.HashMap;");
        
        String fields = 
            "    private Map<String, String> currentQueueMap = new HashMap<>();\n" +
            "    private String pairedCustomerName = null;\n" +
            "    private Map<String, List<String>> chatHistories = new HashMap<>();\n" +
            "    private Map<String, String> activeCustomerNames = new HashMap<>();\n" +
            "    private String currentCustomerId = null;\n";
            
        content = content.replaceAll("    private Map<String, String> currentQueueMap = new HashMap[<>()]+;[^\n]*\n    private String pairedCustomerName = null;", fields);
        
        // Update onMessageReceived
        String newOnMessage = 
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
            
        content = content.replaceAll("(?s)    @Override\n    public void onMessageReceived.*?}\n    }", newOnMessage);
        
        // Update onPairedWithCustomer
        String newOnPaired = 
            "    @Override\n" +
            "    public void onPairedWithCustomer(String customerName, String customerId) {\n" +
            "        Platform.runLater(() -> {\n" +
            "            activeCustomerNames.put(customerId, customerName);\n" +
            "            chatHistories.computeIfAbsent(customerId, k -> new ArrayList<>()).add(\"--- Bạn đã được kết nối với \" + customerName + \" ---\");\n" +
            "            selectCustomer(customerId, customerName);\n" +
            "        });\n" +
            "    }\n" +
            "    \n" +
            "    private void selectCustomer(String customerId, String fullDisplayName) {\n" +
            "        currentCustomerId = customerId;\n" +
            "        String name = fullDisplayName;\n" +
            "        String issue = \"\";\n" +
            "        if (fullDisplayName.contains(\" (\") && fullDisplayName.endsWith(\")\")) {\n" +
            "            int idx = fullDisplayName.indexOf(\" (\");\n" +
            "            name = fullDisplayName.substring(0, idx);\n" +
            "            issue = fullDisplayName.substring(idx);\n" +
            "        }\n" +
            "        lblCurrentClient.setText(name);\n" +
            "        lblIssue.setText(issue);\n" +
            "        lblStatus.setText(\"Đang trong cuộc trò chuyện\");\n" +
            "        statusDot.setFill(Color.web(\"#00C853\"));\n" +
            "        \n" +
            "        listMessages.getItems().clear();\n" +
            "        if (chatHistories.containsKey(customerId)) {\n" +
            "            listMessages.getItems().addAll(chatHistories.get(customerId));\n" +
            "        }\n" +
            "    }\n";
            
        content = content.replaceAll("(?s)    @Override\n    public void onPairedWithCustomer.*?        }\\);\n    }", newOnPaired);
        
        // Update listClients selection listener
        String selectionLogic = 
            "        listClients.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {\n" +
            "            if (newVal != null) {\n" +
            "                String selectedId = null;\n" +
            "                for (Map.Entry<String, String> entry : currentQueueMap.entrySet()) {\n" +
            "                    if (entry.getValue().equals(newVal)) {\n" +
            "                        selectedId = entry.getKey();\n" +
            "                        break;\n" +
            "                    }\n" +
            "                }\n" +
            "                if (selectedId != null) {\n" +
            "                    if (newVal.endsWith(\" (Đang chat)\")) {\n" +
            "                        // Switch to this active chat\n" +
            "                        String originalName = newVal.replace(\" (Đang chat)\", \"\");\n" +
            "                        selectCustomer(selectedId, originalName);\n" +
            "                    } else {\n" +
            "                        // Accept new customer\n" +
            "                        StaffContext.getInstance().getNetworkService().acceptCustomer(selectedId);\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        });";
            
        content = content.replaceAll("(?s)        // Khi click v.*?;\\s*}\\s*}\\s*\\);", selectionLogic);
        
        // Update handleSend
        String handleSendLogic = 
            "    @FXML\n" +
            "    private void handleSend(ActionEvent event) {\n" +
            "        String content = txtMessage.getText().trim();\n" +
            "        if (!content.isEmpty() && currentCustomerId != null) {\n" +
            "            StaffContext.getInstance().getNetworkService().sendChatMessage(content, currentCustomerId);\n" +
            "            String formattedMsg = \"Bạn: \" + content;\n" +
            "            chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(formattedMsg);\n" +
            "            listMessages.getItems().add(formattedMsg);\n" +
            "            txtMessage.clear();\n" +
            "        }\n" +
            "    }";
        content = content.replaceAll("(?s)    @FXML\n    private void handleSend.*?    }", handleSendLogic);
        
        // Update onSessionEnded
        String sessionEndedLogic = 
            "    @Override\n" +
            "    public void onSessionEnded(String reason) {\n" +
            "        Platform.runLater(() -> {\n" +
            "            if (currentCustomerId != null) {\n" +
            "                chatHistories.computeIfAbsent(currentCustomerId, k -> new ArrayList<>()).add(\"--- \" + reason + \" ---\");\n" +
            "                listMessages.getItems().add(\"--- \" + reason + \" ---\");\n" +
            "                lblStatus.setText(\"Phiên đã kết thúc\");\n" +
            "                statusDot.setFill(Color.web(\"#888888\"));\n" +
            "            }\n" +
            "        });\n" +
            "    }";
        content = content.replaceAll("(?s)    @Override\n    public void onSessionEnded.*?    }", sessionEndedLogic);

        // Fix ListCell issue with " (Đang chat)"
        content = content.replace(
            "String issue = \"Cáº§n há»— trá»£\";",
            "String issue = \"Cần hỗ trợ\";\n                    if (item.endsWith(\" (Đang chat)\")) {\n                        item = item.replace(\" (Đang chat)\", \"\");\n                    }"
        );
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("ZaloChatPatch applied!");
    }
}
