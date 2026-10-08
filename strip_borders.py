import re

def strip_borders(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # Replace inline border color with styleClass
    text = re.sub(r'-fx-border-color:\s*#e0e0e0;?', '', text)
    
    # We will add a styleClass="bordered" where border-width is defined
    # Actually, if we just define .bg-white { -fx-border-color: #e0e0e0; } it might apply border where not wanted.
    # Better to just add a specific class to bordered elements.
    # Let's just find where border-width is > 0
    text = re.sub(r'style="([^"]*)-fx-border-width:([^"]*)"([^>]*)', r'style="\1-fx-border-width:\2"\3 styleClass="bordered"', text)

    # Clean up double styleClass="bg-white" styleClass="bordered"
    text = re.sub(r'styleClass="([^"]*)"([^>]*)styleClass="([^"]*)"', r'styleClass="\1 \3"\2', text)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

strip_borders('src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml')
strip_borders('src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml')
print('Stripped borders')
