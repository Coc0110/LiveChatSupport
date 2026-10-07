import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class FixBrackets {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/com/cmp180/livechat/client/staff/ui/ZaloChatController.java");
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        
        content = content.replace("        });\n    });\n    }", "        });\n    }");
        content = content.replace("        });\r\n    });\r\n    }", "        });\n    }");
        
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("Brackets fixed!");
    }
}
