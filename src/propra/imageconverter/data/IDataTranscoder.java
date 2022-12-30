package propra.imageconverter.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;

/**
 * Interface für Transcoder, diese implementieren die Kodierung und Dekodierung
 * der Nutzdaten einer Ressource (Pixel, BaseN, Binärdaten)
 */
public interface IDataTranscoder extends IDataTarget {
    
    // Encodingmodus
    public enum EncodeMode {
        NONE,
        ENCODE,
        ANALYZE,
        AUTO;
    }
    
    // Komprimierungen
    public enum Compression {
        UNCOMPRESSED,
        RLE,
        BASEN,
        HUFFMAN,
        AUTO;
    }

    /**
     * Dekodiert alle Daten des Streams und sendet diese ggfs in mehreren
     * Blöcken an das Target
     */
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException;
    
    /**
     * Gibt den Kompressionstyp zurück
     */
    public Compression getCompression();
    
    /**
     * Analyse der ganzen Daten vor Kodierung nötig?
     */
    public boolean analyzeNecessary();
    
    /**
     * Kodierung initialisieren mit Modus und Ausgabestream
     */
    public IDataTranscoder beginEncoding(EncodeMode op, CheckedOutputStream out) throws IOException;
   
    /**
     * Gibt aktuellen EncodingModus zurück
     */
    public EncodeMode getOperation();
    
    /**
     * Kodiert Daten des aktuellen Blocks in den Stream
     */
    public void encode(ByteBuffer data, boolean last) throws IOException;
    
    /**
     * Beendet Encoding und gibt die Anzahl der kodierten Bytes zurück
     */
    public long endEncoding() throws IOException;
}
