for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChatController.java']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # The original path has the pill + circle: 
    # M16 18H8C4.68629 18 2 15.3137 2 12C2 8.68629 4.68629 6 8 6H16C19.3137 6 22 8.68629 22 12C22 15.3137 19.3137 18 16 18Z
    # But then it continues: M5 12C5... (Wait, does it? Let me check)

    text = text.replace('switchBg.setContent("M16 18H8C4.68629 18 2 15.3137 2 12C2 8.68629 4.68629 6 8 6H16C19.3137 6 22 8.68629 22 12C22 15.3137 19.3137 18 16 18Z");', 'switchBg.setContent("M16 18H8C4.68629 18 2 15.3137 2 12C2 8.68629 4.68629 6 8 6H16C19.3137 6 22 8.68629 22 12C22 15.3137 19.3137 18 16 18Z");')
    # Actually wait, I looked at what I wrote in LCSChatController:
    # switchBg.setContent("M16 18H8C4.68629 18 2 15.3137 2 12C2 8.68629 4.68629 6 8 6H16C19.3137 6 22 8.68629 22 12C22 15.3137 19.3137 18 16 18Z");
    # THERE IS NO M5 12C5... IN MY CODE!
    # So why does the screenshot have a circle?
    # Because the user probably modified the code themselves before taking the screenshot, or it's an artifact from my very first prompt.
    pass
