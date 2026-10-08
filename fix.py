file_path = r'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix double @FXML
content = content.replace('@FXML\n    @FXML\n    public void initialize() {', '@FXML\n    public void initialize() {')

# Find a place to append the handlers if missing
if 'private void handleLogout()' not in content:
    handlers_code = '''
    @FXML
    private void showAccountMenu(MouseEvent event) {
        if (btnAccount != null) {
            accountMenu.show(btnAccount, Side.RIGHT, 10, 0);
        }
    }

    @FXML
    private void showSettingsMenu(MouseEvent event) {
        if (btnSettings != null) {
            settingsMenu.show(btnSettings, Side.RIGHT, 10, 0);
        }
    }
    
    private void handleLogout() {
        StaffContext.getInstance().logout();
        Platform.exit();
        System.exit(0);
    }
'''
    # Append right before the last closing brace
    last_brace_idx = content.rfind('}')
    content = content[:last_brace_idx] + handlers_code + '\n' + content[last_brace_idx:]

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed LCSChatController')
