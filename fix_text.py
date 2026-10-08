import re

file_path = 'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# Fix corrupted strings
replacements = {
    r'CAAEoT TRAA\?I: Thanh cAA\'ng cAAA': 'CỘT TRÁI: Thanh công cụ',
    r'\?cy cAc nAt d>i xu
g Ay': 'Đẩy các nút dưới xuống đáy',
    r'CAAA,AA<"T GIAAA,AA,ArA: Danh sA\'A,Ach hAAA,AA\?zAi thoAAA,AA,Ai': 'CỘT GIỮA: Danh sách hội thoại',
    r'A\?\? tAAm kiAAAm bo trAAn giAA\?~ng Zalo': 'Ô tìm kiếm bo tròn giống Zalo',
    r'promptText="TAAm kiAAAm\.\.\."': 'promptText="Tìm kiếm..."',
    r'Border 0 A,\?~AA\' mAAAt viAAA\?n ListView, tAAAo cAAAm giAAc phAAA3ng': 'Border 0 để mất viền ListView, tạo cảm giác phẳng',
    r'CAAA,AA<"T PHAAA,AA,AI: Khung chat chA\'A,A-nh': 'CỘT PHẢI: Khung chat chính',
    r'text="A,A\?ang hAA\?" trAAA khAAch hAAng:"': 'text="Đang hỗ trợ khách hàng:"',
    r'text="ChA\+Aa chAAA\?n khAAch hAAng"': 'text="Chưa chọn khách hàng"',
    r'text="HAAy chAAA\?n mAA,t khAAch hAAng bAAn trAAi A,\?~AA\' chat"': 'text="Hãy chọn một khách hàng bên trái để chat"',
    r'Khung hiAAA\?n thAAA\? tin nhAAA_n': 'Khung hiển thị tin nhắn',
    r'Khu vAAAc soAAAn tin nhAAA_n': 'Khu vực soạn tin nhắn',
    r'Thanh cAA\'ng cAAA \(A,\?~AA-nh kAA"m\)': 'Thanh công cụ (đính kèm)',
    r'VAAAng Text vAA NAAAot GAAAi': 'Vùng Text và Nút Gửi',
    r'promptText="NhAAAp tin nhAAA_n\.\.\."': 'promptText="Nhập tin nhắn..."'
}

for bad, good in replacements.items():
    text = re.sub(bad, good, text)

# Just hardcode the fixes if regex fails
text = text.replace('text="A,A?ang hAA?" trAAA khAAch hAAng:"', 'text="Đang hỗ trợ khách hàng:"')
text = text.replace('text="ChA+Aa chAAA?n khAAch hAAng"', 'text="Chưa chọn khách hàng"')
text = text.replace('text="HAAy chAAA?n mAA,t khAAch hAAng bAAn trAAi A,?~AA\' chat"', 'text="Hãy chọn một khách hàng bên trái để chat"')
text = text.replace('promptText="NhAAAp tin nhAAA_n..."', 'promptText="Nhập tin nhắn..."')
text = text.replace('promptText="TAAm kiAAAm..."', 'promptText="Tìm kiếm..."')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed FXML text')
