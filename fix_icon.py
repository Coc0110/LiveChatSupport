for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChatController.java']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    import re
    # 1. Add prefWidth to themeBox
    text = text.replace('HBox themeBox = new HBox(10);', 'HBox themeBox = new HBox(10);\n        themeBox.setPrefWidth(180);')
    
    # 2. Increase icon size
    text = text.replace('switchIcon.setScaleX(0.5);', 'switchIcon.setScaleX(0.7);')
    text = text.replace('switchIcon.setScaleY(0.5);', 'switchIcon.setScaleY(0.7);')
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

print('Fixed icon size and alignment')
