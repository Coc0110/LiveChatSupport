file_path = 'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

import re

# We will use regex to find the attributes and replace their contents
text = re.sub(r'text=".*?(?:h|H).*?kh.*?(?:h|H).*?ng.*?"', 'text="Đang hỗ trợ khách hàng:"', text, count=1)
text = re.sub(r'text="Ch.*?kh.*?ng"', 'text="Chưa chọn khách hàng"', text)
text = re.sub(r'text="H.*?chat"', 'text="Hãy chọn một khách hàng bên trái để chat"', text)
text = re.sub(r'promptText="T.*?m.*?"', 'promptText="Tìm kiếm..."', text)
text = re.sub(r'promptText="Nh.*?tin.*?"', 'promptText="Nhập tin nhắn..."', text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed FXML text 2')
