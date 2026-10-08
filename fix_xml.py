for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # Fix / styleClass="bordered"> and / >
    text = text.replace('/ styleClass="bordered">', 'styleClass="bordered" />')
    text = text.replace('/ >', '/>')
    text = text.replace('styleClass="bordered" >', 'styleClass="bordered">')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

print('Fixed XML syntax')
