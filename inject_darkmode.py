for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChatController.java']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # The existing toggle logic is:
    # boolean isDark = switchIcon.getTranslateX() > 0;
    # if (isDark) { ... light ... } else { ... dark ... }
    
    # We will inject the dark mode class toggle:
    logic_light = '''switchIcon.setTranslateX(-4);
                switchIcon.setContent("M12 4V2M12 20V22M6.41421 6.41421L5 5M17.728 17.728L19.1422 19.1422M4 12H2M20 12H22M17.7285 6.41421L19.1427 5M6.4147 17.728L5.00049 19.1422M12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12C17 14.7614 14.7614 17 12 17Z");
                if (btnAccount != null && btnAccount.getScene() != null) {
                    btnAccount.getScene().getRoot().getStyleClass().remove("dark-mode");
                }'''
                
    logic_dark = '''switchIcon.setTranslateX(4);
                switchIcon.setContent("M18 15C13.0294 15 9 10.9706 9 6C9 5.09074 9.13484 4.21311 9.38561 3.38574C5.69007 4.50583 3 7.93883 3 12.0001C3 16.9707 7.02944 20.9999 12 20.9999C16.0613 20.9999 19.4943 18.3103 20.6144 14.6147C19.787 14.8655 18.9093 15 18 15Z");
                if (btnAccount != null && btnAccount.getScene() != null) {
                    btnAccount.getScene().getRoot().getStyleClass().add("dark-mode");
                }'''

    text = text.replace('switchIcon.setTranslateX(-4);\n                switchIcon.setContent("M12 4V2M12 20V22M6.41421 6.41421L5 5M17.728 17.728L19.1422 19.1422M4 12H2M20 12H22M17.7285 6.41421L19.1427 5M6.4147 17.728L5.00049 19.1422M12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12C17 14.7614 14.7614 17 12 17Z");', logic_light)
    text = text.replace('switchIcon.setTranslateX(4);\n                switchIcon.setContent("M18 15C13.0294 15 9 10.9706 9 6C9 5.09074 9.13484 4.21311 9.38561 3.38574C5.69007 4.50583 3 7.93883 3 12.0001C3 16.9707 7.02944 20.9999 12 20.9999C16.0613 20.9999 19.4943 18.3103 20.6144 14.6147C19.787 14.8655 18.9093 15 18 15Z");', logic_dark)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

print('Added Dark Mode logic')
