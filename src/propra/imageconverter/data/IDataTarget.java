package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface für Datenziele die Daten vom Decoder empfangen.
 */
@FunctionalInterface
public interface IDataTarget {
    /**
     * Wird vom Sender aufgerufen mit Daten zum Verarbeiten
     */
    public void onData(ByteBuffer data, boolean lastBlock, IDataTranscoder caller) throws IOException;  
}
