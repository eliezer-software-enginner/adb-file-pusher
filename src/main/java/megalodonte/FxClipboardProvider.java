package megalodonte;

import javafx.scene.input.ClipboardContent;

public class FxClipboardProvider implements ClipboardProvider {
    @Override
    public void setString(String texto) {
        var clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        var content = new ClipboardContent();
        content.putString(texto);
        clipboard.setContent(content);
    }

    @Override
    public String getString() {
        return javafx.scene.input.Clipboard
                .getSystemClipboard()
                .getString();
    }
}