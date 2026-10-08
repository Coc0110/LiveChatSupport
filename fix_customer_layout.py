file_path = 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

import re

# We need to wrap the existing top, center, bottom into a new <center><BorderPane>
# Currently it looks like:
# <BorderPane ...>
#    <left>...</left>
#    <top>...</top>
#    <center>...</center>
#    <bottom>...</bottom>
# </BorderPane>

# Remove left from root
left_match = re.search(r'(<left>.*?</left>)', text, re.DOTALL)
left_xml = left_match.group(1)
text = text.replace(left_xml, '')

# Now text has top, center, bottom directly under root BorderPane
# We replace the content inside the root BorderPane with:
# <left>...</left>
# <center>
#    <BorderPane>
#       <top>...</top>
#       <center>...</center>
#       <bottom>...</bottom>
#    </BorderPane>
# </center>

# Find everything between <BorderPane> and </BorderPane>
inner_content = re.search(r'(<BorderPane[^>]*>)(.*?)(</BorderPane>)', text, re.DOTALL)
root_start = inner_content.group(1)
root_middle = inner_content.group(2)
root_end = inner_content.group(3)

new_middle = f'''
   {left_xml}
   <center>
      <BorderPane>
         {root_middle.strip()}
      </BorderPane>
   </center>
'''

text = root_start + new_middle + root_end

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)
print('Fixed CustomerChat.fxml layout')
