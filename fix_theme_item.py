file_path = 'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

import re

# Replace CustomMenuItem setup
old_code = '''        CustomMenuItem themeItem = new CustomMenuItem();
        themeItem.setHideOnClick(false);
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTheme = new Label("Giao diện sáng/tối");'''

new_code = '''        CustomMenuItem themeItem = new CustomMenuItem();
        themeItem.setHideOnClick(false);
        themeItem.getStyleClass().add("menu-item"); // Apply CSS styling
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTheme = new Label("Giao diện sáng/tối");
        lblTheme.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);'''

text = text.replace(old_code, new_code)
text = text.replace('themeBox.getChildren().addAll(lblTheme, themeSwitch);', 'themeBox.getChildren().addAll(lblTheme, spacer, themeSwitch);')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed CustomMenuItem')
