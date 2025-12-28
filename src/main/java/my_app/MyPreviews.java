package my_app;

import javafx.scene.control.Label;
import megalodonte.previewer.PreviewItem;

import java.util.List;

public class MyPreviews {

    public List<PreviewItem> getPreviews() {
        return List.of(
                new PreviewItem("Tela Principal", () -> new UI().render().getNode())
        );
    }
}
