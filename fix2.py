file_path = r'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java'
import re
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix double @FXML
content = re.sub(r'@FXML\s*@FXML', '@FXML', content)
content = re.sub(r'@FXML\s*@FXML', '@FXML', content)

# Remove StaffContext.getInstance().logout();
content = content.replace('StaffContext.getInstance().logout();', '')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed again')
