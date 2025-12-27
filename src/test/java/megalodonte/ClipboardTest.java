package megalodonte;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClipboardTest {

    @AfterEach
    void resetProvider() {
        // evita vazamento entre testes
        Clipboard.setProvider(new FxClipboardProvider());
    }

    @Test
    void setString_deveDelegarParaProvider() {
        ClipboardProvider providerMock = mock(ClipboardProvider.class);
        Clipboard.setProvider(providerMock);

        String texto = "Mockito funciona";

        Clipboard.setString(texto);

        verify(providerMock, times(1)).setString(texto);
    }

    @Test
    void getString_deveRetornarValorDoProvider() {
        ClipboardProvider providerMock = mock(ClipboardProvider.class);
        Clipboard.setProvider(providerMock);

        when(providerMock.getString()).thenReturn("Texto mockado");

        String resultado = Clipboard.getString();

        assertEquals("Texto mockado", resultado);
        verify(providerMock, times(1)).getString();
    }
}
