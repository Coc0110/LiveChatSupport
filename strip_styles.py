import re

def process_fxml(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        text = f.read()

    # Replace inline colors with style classes
    # 1. white background
    text = re.sub(r'style="([^"]*)-fx-background-color:\s*white;?([^"]*)"', r'style="\1\2" styleClass="bg-white"', text)
    # 2. #e5e5ea background
    text = re.sub(r'style="([^"]*)-fx-background-color:\s*#e5e5ea;?([^"]*)"', r'style="\1\2" styleClass="bg-gray"', text)
    # 3. #f0f2f5 background
    text = re.sub(r'style="([^"]*)-fx-background-color:\s*#f0f2f5;?([^"]*)"', r'style="\1\2" styleClass="bg-input"', text)
    
    # 4. text colors
    text = re.sub(r'style="([^"]*)-fx-text-fill:\s*black;?([^"]*)"', r'style="\1\2" styleClass="text-main"', text)
    text = text.replace('textFill="#888888"', 'styleClass="text-sub"')

    # Clean up empty styles
    text = text.replace('style=" "', '')
    text = text.replace('style=""', '')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)

process_fxml('src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml')
process_fxml('src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml')
print('Stripped inline styles')
