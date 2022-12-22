package propra.imageconverter.image.huffman;

import propra.imageconverter.util.BitOutputStream;
import propra.imageconverter.util.BitInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import propra.imageconverter.util.*;
import static propra.imageconverter.data.DataCodec.DEFAULT_BLOCK_SIZE;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.image.ImageCodec;
import propra.imageconverter.image.ImageResource;
import propra.imageconverter.data.IDataTarget;

/**
 * 
 * @author pg
 */
public class ImageCodecHuffman extends ImageCodec {
    
    //  Histogramm der Daten
    private final long[] histogram = new long[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    // Ausgabe Bitstream
    BitOutputStream outStream;  
    
    /**
     *
     * @param resource
     */
    public ImageCodecHuffman(ImageResource resource) {
        super(resource);
    }

    /**
     * Bereitet Datenverarbeitung vor
     * 
     * @param op
     * @throws IOException 
     */
    @Override
    public void begin(Operation op) throws IOException {
        super.begin(op);
        
        switch(op) {
            case ENCODER_ANALYZE -> {
                Arrays.fill(histogram, 0);
            }
            case ENCODE -> {
                /*
                 *  BitStream erstellen und Baum als Bitfolge in Stream kodieren
                 */
                outStream = new BitOutputStream(resource.getOutputStream());
                huffmanTree.storeTreeInStream(outStream);
            }
        }
    }
 
    /**
     * Ermittelt die Häufigkeit der Symbole im Datenblock
     *
     * @param block
     * @param last 
     */
    @Override
    public void analyze(ByteBuffer block, boolean last) {
        if(block == null) {
            throw new IllegalArgumentException();
        }
        
        if(operation == Operation.ENCODER_ANALYZE) {           
            // Histogram aktualisieren fmit dem Datenblock
            byte[] buffer = block.array();
            int offset = 0;

            while(offset < block.limit()) {
                histogram[buffer[offset] & 0xFF]++;
                offset++;
            }
        } 
    }
    
    /**
     * Schließt Blockweise Datenverarbeitung ab
     * 
     * @throws IOException 
     */
    @Override
    public void end() throws IOException {
        switch(operation) {
            case ENCODER_ANALYZE -> {
               /**
                *  Histogram prüfen
                */
                long sum = 0;
                for(long i:histogram) {
                    sum += i;
                }  
                if(sum != image.getHeader().imageSize()) {
                    throw new IOException("Fehlerhafte Bilddaten (Histogram)");
                }

                /*
                 *  Nach der Encoder-Analyse den entsprechenden Huffman Baum aus dem 
                 *  ermittelten Histogram erstellen
                 */
                huffmanTree = new HuffmanTree();
                huffmanTree.buildTreeFromHistogram(histogram);
            }
            case ENCODE -> {
                /*
                 *  Stream flushen und kodierte Datengröße aktualisieren
                 */
                outStream.flush();
                image.getHeader().encodedSize(outStream.getByteCounter()); 
            }
        }

        super.end();
    }
    
    /**
     * 
     * @param op
     * @return 
     */
    @Override
    public boolean analyzeNecessary(Operation op) {
        return op == Operation.ENCODE;
    }

    /**
     * Dekodiert Huffman kodierten Datenblock
     * 
     * @param listener
     * @throws IOException 
     */
    @Override
    public void decode(IDataTarget listener) throws IOException {
        
        // Ausgabepuffer vorbereiten
        int symbolCtr = 0;
        
        // BitStream erstellen
        BitInputStream stream = new BitInputStream(resource.getInputStream());
        ByteBuffer data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        // Kodierten Baum einlesen und erstellen
        huffmanTree = new HuffmanTree();
        huffmanTree.buildTreeFromResource(stream);
        
        // Lädt, dekodiert und sendet Pixelblöcke an Listener  
        while(symbolCtr++ < image.getHeader().imageSize()) {
            
            // Symbol dekodieren
            int symbol = huffmanTree.decodeSymbol(stream);
            if(symbol == -1) {
                break;
            }
            
            // Symbol speichern
            data.put((byte)symbol);

            // Wenn Blockgröße erreicht an Listener senden
            if(data.capacity() == data.position()) {                
                dispatchData(IDataTarget.Event.DATA_BLOCK_DECODED, listener, 
                                data,false);    
            }
        }
        
        // Restliche Daten im Puffer übertragen
        dispatchData(IDataTarget.Event.DATA_BLOCK_DECODED, 
                        listener, 
                        data,
                        true);     
    }


    /**
     * 
     * @param block
     * @param last
     * @throws IOException 
     */
    @Override
    public void encode(ByteBuffer block, boolean last) throws IOException {
        if(huffmanTree == null) {
            throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
        }
        
        /**
         *  Symbole im Puffer iterieren, per Huffmantree zu Bitcode umsetzen und 
         *  diesen in der Resource speichern
         */
        byte[] c = new byte[3];
        while(block.position() < block.limit()) {
            
            // Pixel lesen und Farbe konvertieren
            block.get(c);
                    
            outStream.write(huffmanTree.encodeSymbol(c[0] & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c[1] & 0xFF));
            outStream.write(huffmanTree.encodeSymbol(c[2] & 0xFF));
        }
    }   
}
