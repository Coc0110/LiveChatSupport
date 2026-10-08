file_path = r'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml'
with open(file_path, 'rb') as f:
    data = f.read()
if data.startswith(b'\xef\xbb\xbf'):
    data = data[3:]
with open(file_path, 'wb') as f:
    f.write(data)
print('Removed BOM')
