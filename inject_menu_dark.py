for file_path in ['src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java', 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChatController.java']:
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    text = text.replace('btnAccount.getScene().getRoot().getStyleClass().remove("dark-mode");', 'btnAccount.getScene().getRoot().getStyleClass().remove("dark-mode");\n                    accountMenu.getStyleClass().remove("dark-mode");\n                    settingsMenu.getStyleClass().remove("dark-mode");')
    
    text = text.replace('btnAccount.getScene().getRoot().getStyleClass().add("dark-mode");', 'btnAccount.getScene().getRoot().getStyleClass().add("dark-mode");\n                    accountMenu.getStyleClass().add("dark-mode");\n                    settingsMenu.getStyleClass().add("dark-mode");')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

print('Injected context menu dark mode toggle')
