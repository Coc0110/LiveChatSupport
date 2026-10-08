import re

def fix_vbox(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # Remove BorderPane.alignment="CENTER" from the VBox in <left>
    text = text.replace('<VBox alignment="TOP_CENTER" prefHeight="650.0" prefWidth="65.0" style="-fx-background-color: #0084FF; -fx-padding: 15 0 15 0; -fx-spacing: 15;" BorderPane.alignment="CENTER">', '<VBox alignment="TOP_CENTER" prefWidth="65.0" style="-fx-background-color: #0084FF; -fx-padding: 15 0 15 0; -fx-spacing: 15;">')
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

fix_vbox('src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml')
fix_vbox('src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml')
print('Fixed sidebar vertical stretch')
