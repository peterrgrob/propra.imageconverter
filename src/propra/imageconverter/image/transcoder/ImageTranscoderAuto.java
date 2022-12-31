package propra.imageconverter.image.transcoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import propra.imageconverter.data.IDataTarget;
import propra.imageconverter.data.IDataTranscoder;
import propra.imageconverter.data.CheckedInputStream;
import propra.imageconverter.data.CheckedOutputStream;

/**
 * Transcoder für die automatische Wahl der besten Kompressionmethode.
 * 
 * Bündelt dazu mehrere Encoder und ermittelt nach Abschluss der Kodierung 
 * das beste Verfahren. Die Kompression wird zum Vergleich in einen NullStream 
 * durchgeführt, ein Vergleich über Entropie, oder ähnliches scheint für die 
 * Anforderungen des Praktikums nicht unbedingt nötig zu sein, wenn überhaupt 
 * möglich.
 */
public class ImageTranscoderAuto extends ImageTranscoderRaw {
    
    // Liste der Encoder
    private final ArrayList<ImageTranscoderRaw> encoderList;
    
    // Bester Codec
    private ImageTranscoderRaw winner;
    
    /**
     * Initialisiert mit Encoder Liste
     */
    public ImageTranscoderAuto( ArrayList<ImageTranscoderRaw> encoder) {
        super(null);
        this.encoderList = encoder;
    }
    
    @Override
    public Compression getCompression() {
        return Compression.AUTO;
    }

    /**
     * Gibt den besten Transcoder zurück
     */
    public ImageTranscoderRaw getWinner() {
        return winner;
    }
    
    /**
     * Analyse nötig für Huffman
     */
    @Override
    public boolean analyzeNecessary() {
        return true;
    }

    /**
     * Initialisiert Encoding
     */
    @Override
    public IDataTranscoder beginEncoding(EncodeMode op, CheckedOutputStream out) throws IOException {
        super.beginEncoding(op, out);

        // Encoding mit NullStream beginnen
        for(ImageTranscoderRaw e : encoderList) {
            e.beginEncoding(op, new CheckedOutputStream(OutputStream.nullOutputStream()));
        }
        
        return this;
    }
    
    /**
     * Blöcke an Codecs weiterreichen
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        for(ImageTranscoderRaw e : encoderList) {
            e.encode(block, last);
            block.rewind();
        }            
    }

    /**
     * Beendet Encoding/Analyse und bestimmt den besten Transcoder
     */
    @Override
    public long endEncoding() throws IOException {
        if(null != operation) switch (operation) {
            case ANALYZE -> {
                // Analyse abschließen
                for(ImageTranscoderRaw e : encoderList) {
                    e.endEncoding();
                }
            }
            case ENCODE -> {
                // Besten Codec ermitteln
                long min = Long.MAX_VALUE;
                long t = 0;
                for(ImageTranscoderRaw e : encoderList) {
                    if((t = e.endEncoding()) < min) {
                       winner = e;
                       min = t; 
                    }
                }
                encodedBytes = min;
            }

        }
        return super.endEncoding();
    }
    
    @Override
    public void decode(CheckedInputStream in, IDataTarget target) throws IOException {
        throw new UnsupportedOperationException();
    }
}
