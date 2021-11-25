package meowdb.util;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableUtils {

    public static void installCopyPasteHandler(TableView<?> table) {
        table.setOnKeyPressed(new TableKeyEventHandler());
    }

    public static class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        KeyCodeCombination pasteKeyCodeCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);

        public void handle(final KeyEvent keyEvent) {

            if (copyKeyCodeCombination.match(keyEvent)) {
                if( keyEvent.getSource() instanceof TableView) {
                    copySelectionToClipboard( (TableView<?>) keyEvent.getSource());
                    keyEvent.consume();
                }
            } 
            else if (pasteKeyCodeCombination.match(keyEvent)) {
                if( keyEvent.getSource() instanceof TableView) {
                    pasteClipboard( (TableView<?>) keyEvent.getSource());
                    keyEvent.consume();
                }
            }
        }
    }

    public static void copySelectionToClipboard(TableView<?> table) {

        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;

        for (TablePosition position : positionList) {

            int row = position.getRow();
            int col = position.getColumn();

            Object cell = (Object) table.getColumns().get(col).getCellData(row);

            if (cell == null) {
                cell = "";
            }

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');

            }

            String text = cell.toString();
            clipboardString.append(text);
            prevRow = row;
        }

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());

        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    public static void pasteClipboard(TableView<?> table) {

        TablePosition focusedCellPosition = table.getFocusModel().getFocusedCell();
        String pasteString = Clipboard.getSystemClipboard().getString();
        Pattern pattern = Pattern.compile("([^\t]*)\t([^\t]*)\t([^\n]*)(\n)?");
        Matcher matcher = pattern.matcher(pasteString);
        while (matcher.find()) {
            System.out.println(matcher.group(1) + "," + matcher.group(2) + "," + matcher.group(3));
        }
    }
}
