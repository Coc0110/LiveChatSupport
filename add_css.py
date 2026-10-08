file_path = 'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('<BorderPane prefHeight="650.0" prefWidth="1000.0"', '<BorderPane stylesheets="@menu_style.css" prefHeight="650.0" prefWidth="1000.0"')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Added stylesheet')
