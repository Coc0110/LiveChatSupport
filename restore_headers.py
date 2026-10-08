file_path = 'src/main/java/com/cmp180/livechat/client/customer/ui/CustomerChat.fxml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

headers = '''<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.ListView?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Priority?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.shape.SVGPath?>
<?import javafx.scene.text.Font?>

'''

if not text.startswith('<?xml'):
    text = headers + text
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(text)
    print('Added headers back')
else:
    print('Headers already exist')
