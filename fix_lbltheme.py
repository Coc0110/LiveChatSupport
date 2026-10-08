for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChatController.java']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    text = text.replace('lblTheme.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-family: \'Segoe UI\';");', 'lblTheme.setStyle("-fx-font-size: 14px; -fx-font-family: \'Segoe UI\';");')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

print('Removed inline text-fill from lblTheme')
