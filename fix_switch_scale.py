for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChatController.java']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # Revert switchIcon scale
    text = text.replace('switchIcon.setScaleX(0.7);', 'switchIcon.setScaleX(0.5);')
    text = text.replace('switchIcon.setScaleY(0.7);', 'switchIcon.setScaleY(0.5);')

    # Add themeSwitch scale
    if 'themeSwitch.setScaleX' not in text:
        text = text.replace('themeSwitch.setCursor(Cursor.HAND);', 'themeSwitch.setCursor(Cursor.HAND);\n        themeSwitch.setScaleX(1.3);\n        themeSwitch.setScaleY(1.3);')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

print('Fixed switch scale logic')
