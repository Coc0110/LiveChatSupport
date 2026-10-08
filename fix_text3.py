file_path = 'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

import re
text = re.sub(r'promptText=".*?"', 'promptText="Tìm kiếm..."', text, count=1)
text = re.sub(r'<Label text=".*?textFill="#888888"', '<Label text="Đang hỗ trợ khách hàng:" textFill="#888888"', text)
text = re.sub(r'<Label fx:id="lblCurrentClient" text=".*?"', '<Label fx:id="lblCurrentClient" text="Chưa chọn khách hàng"', text)
text = re.sub(r'<Label fx:id="lblStatus" text=".*?"', '<Label fx:id="lblStatus" text="Hãy chọn một khách hàng bên trái để chat"', text)
text = re.sub(r'promptText="Nh.*?tin.*?"', 'promptText="Nhập tin nhắn..."', text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed FXML text 3')
