package my_app;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.stage.Stage;
import megalodonte.hotreload.Reloader;
import megalodonte.previewer.PreviewItem;
import megalodonte.previewer.PreviewRoot;
import megalodonte.previewer.PreviewRootv2;
import megalodonte.previewer.ReloadContext;
import java.util.List;

public class UIReloaderImpl implements Reloader {
    private static final String PREVIEW_ROOT_CLASS = PreviewRootv2.class.getName();

    @Override
    public void reload(Object context) {
        // Agora verificamos se o contexto é o nosso record
        if (context instanceof ReloadContext(Stage stage, String implementationClass,  String providerClassName,
                                             String lastSelectedName)) {
            Platform.runLater(() -> {
                try {
                    final var currentClassLoader = this.getClass().getClassLoader();
                    Class<?> previewRootClass = currentClassLoader.loadClass(PREVIEW_ROOT_CLASS);

                    // 1. Definimos os tipos dos parâmetros do novo construtor
                    var constructor = previewRootClass.getConstructor(
                            Stage.class,
                            String.class,
                            String.class
                    );

                    // 2. Instanciamos passando os dados do registro
                    Parent newRoot = (Parent) constructor.newInstance(
                            stage,
                            implementationClass,
                            providerClassName
                    );

                    // 3. Substituímos o root da cena
                    stage.getScene().setRoot(newRoot);

                    if (lastSelectedName != null) {
                        // Lógica para re-selecionar o componente automaticamente
                        // Você pode expor um método no PreviewRoot: newRoot.selectByName(ctx.lastSelectedName());
                    }

                    System.out.println("[Megalodonte] UI recarregada com sucesso!");

                } catch (Exception e) {
                    System.err.println("[Megalodonte] Erro ao instanciar nova UI.");
                    e.printStackTrace();
                }
            });
        }
    }
}