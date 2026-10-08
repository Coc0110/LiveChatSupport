file_path_staff = 'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChat.fxml'
with open(file_path_staff, 'r', encoding='utf-8') as f:
    staff_content = f.read()

import re
left_match = re.search(r'(<left>.*?</left>)', staff_content, re.DOTALL)
if not left_match:
    print('Left not found')
    exit(1)
left_xml = left_match.group(1)

file_path_customer = 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml'
with open(file_path_customer, 'r', encoding='utf-8') as f:
    customer_content = f.read()

# Add stylesheets to BorderPane
customer_content = customer_content.replace('<BorderPane prefHeight="650.0" prefWidth="500.0" style="-fx-background-color: #e5e5ea;"', '<BorderPane prefHeight="650.0" prefWidth="565.0" style="-fx-background-color: #e5e5ea;" stylesheets="@menu_style.css"')

# Insert <left> after BorderPane opening tag
customer_content = re.sub(r'(<BorderPane.*?>)', r'\1\n   ' + left_xml, customer_content, count=1)

with open(file_path_customer, 'w', encoding='utf-8') as f:
    f.write(customer_content)
print('Patched CustomerChat.fxml')
