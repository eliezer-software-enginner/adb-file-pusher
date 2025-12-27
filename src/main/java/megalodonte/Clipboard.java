package megalodonte;

public class Clipboard {

    private static ClipboardProvider provider = new FxClipboardProvider();

    // permite trocar no teste
    static void setProvider(ClipboardProvider newProvider) {
        provider = newProvider;
    }

    public static void setString(String texto) {
        provider.setString(texto);
    }

    public static String getString() {
        return provider.getString();
    }
}
