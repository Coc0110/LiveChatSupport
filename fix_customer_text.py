file_path = 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

import re
text = re.sub(r'text="Trung t.*?m H.*? tr.*?"', 'text="Trung tâm Hỗ trợ"', text)
text = re.sub(r'text=".*?ch.*? nh.*?n vi.*?n k.*?t n.*?i.*?"', 'text="Đang chờ nhân viên kết nối..."', text)
text = re.sub(r'promptText="Nh.*?p tin nh.*?n.*?"', 'promptText="Nhập tin nhắn..."', text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed CustomerChat text')
