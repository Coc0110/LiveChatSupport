import os
import re

file_path = r'src/main/java/com/cmp180/livechat/client/staff/ui/LCSChatController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add imports
imports = '''
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
'''
content = content.replace('import javafx.util.Duration;', 'import javafx.util.Duration;' + imports)

# 2. Add class variables
vars_code = '''
    @FXML private Button btnAccount;
    @FXML private Button btnSettings;
    private ContextMenu accountMenu;
    private ContextMenu settingsMenu;
'''
content = content.replace('public void initialize() {', vars_code + '\n    @FXML\n    public void initialize() {')

# 3. Add ContextMenu init logic inside initialize()
init_code = '''
        // --- Account Menu Setup ---
        accountMenu = new ContextMenu();
        MenuItem itemProfile = new MenuItem("Hồ sơ của bạn");
        MenuItem itemAccSettings = new MenuItem("Cài đặt");
        MenuItem itemLogout = new MenuItem("Đăng xuất");
        itemLogout.setOnAction(e -> handleLogout());
        accountMenu.getItems().addAll(itemProfile, itemAccSettings, new SeparatorMenuItem(), itemLogout);

        // --- Settings Menu Setup ---
        settingsMenu = new ContextMenu();
        MenuItem itemExit = new MenuItem("Thoát ứng dụng");
        itemExit.setOnAction(e -> System.exit(0));

        CustomMenuItem themeItem = new CustomMenuItem();
        themeItem.setHideOnClick(false);
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTheme = new Label("Giao diện sáng/tối");

        StackPane themeSwitch = new StackPane();
        themeSwitch.setCursor(Cursor.HAND);
        SVGPath switchBg = new SVGPath();
        switchBg.setContent("M16 18H8C4.68629 18 2 15.3137 2 12C2 8.68629 4.68629 6 8 6H16C19.3137 6 22 8.68629 22 12C22 15.3137 19.3137 18 16 18Z");
        switchBg.setStroke(Color.GRAY);
        switchBg.setStrokeWidth(2);
        switchBg.setFill(Color.TRANSPARENT);

        SVGPath switchIcon = new SVGPath();
        switchIcon.setContent("M12 4V2M12 20V22M6.41421 6.41421L5 5M17.728 17.728L19.1422 19.1422M4 12H2M20 12H22M17.7285 6.41421L19.1427 5M6.4147 17.728L5.00049 19.1422M12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12C17 14.7614 14.7614 17 12 17Z");
        switchIcon.setStroke(Color.GRAY);
        switchIcon.setStrokeWidth(2);
        switchIcon.setFill(Color.TRANSPARENT);
        switchIcon.setScaleX(0.5);
        switchIcon.setScaleY(0.5);
        switchIcon.setTranslateX(-4);

        themeSwitch.getChildren().addAll(switchBg, switchIcon);
        themeSwitch.setOnMouseClicked(e -> {
            boolean isDark = switchIcon.getTranslateX() > 0;
            if (isDark) {
                switchIcon.setTranslateX(-4);
                switchIcon.setContent("M12 4V2M12 20V22M6.41421 6.41421L5 5M17.728 17.728L19.1422 19.1422M4 12H2M20 12H22M17.7285 6.41421L19.1427 5M6.4147 17.728L5.00049 19.1422M12 17C9.23858 17 7 14.7614 7 12C7 9.23858 9.23858 7 12 7C14.7614 7 17 9.23858 17 12C17 14.7614 14.7614 17 12 17Z");
            } else {
                switchIcon.setTranslateX(4);
                switchIcon.setContent("M18 15C13.0294 15 9 10.9706 9 6C9 5.09074 9.13484 4.21311 9.38561 3.38574C5.69007 4.50583 3 7.93883 3 12.0001C3 16.9707 7.02944 20.9999 12 20.9999C16.0613 20.9999 19.4943 18.3103 20.6144 14.6147C19.787 14.8655 18.9093 15 18 15Z");
            }
        });
        themeBox.getChildren().addAll(lblTheme, themeSwitch);
        themeItem.setContent(themeBox);

        settingsMenu.getItems().addAll(themeItem, new SeparatorMenuItem(), itemExit);
'''
content = content.replace('StaffContext.getInstance().getNetworkService().addListener(this);', init_code + '\n        StaffContext.getInstance().getNetworkService().addListener(this);')

# 4. Add handlers
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
# add at the end of class
content = content.replace('    // Thêm các lớp ListCell ở dưới', handlers_code + '\n    // Thêm các lớp ListCell ở dưới')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Patched LCSChatController successfully.")
