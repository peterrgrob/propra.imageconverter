package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import propra.imageconverter.data.BitInputStream;
import propra.imageconverter.data.DataBlock;
import static propra.imageconverter.data.DataCodecRaw.DEFAULT_BLOCK_SIZE;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataListener;

/**
 *
 * @author pg
 */
public class ImageCodecHuffman extends ImageCodecRaw {
    
    //  Histogramm der Daten
    private final int[] histogram = new int[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    /*
     *  Konstruktor
     */
    public ImageCodecHuffman(ImageResource resource) {
        super(resource);
    }

    /**
     * 
     */
    @Override
    public void begin(Operation op) throws IOException {
        super.begin(op);
        
        if(op == Operation.ANALYZE_ENCODER) {
            Arrays.fill(histogram, 0);
        } 
    }
 
    /**
     *  Ermittelt die Häufigkeit der Symbole im Datenblock
     */
    @Override
    public void analyze(DataBlock block) {
        if(block == null) {
            throw new IllegalArgumentException();
        }
        
        if(operation == Operation.ANALYZE_ENCODER) {
            
            // Histogram aktualisieren für den aktuellen Block
            byte[] buffer = block.array();
            int offset = 0;

            while(offset < block.data.limit()) {
                histogram[buffer[offset] & 0xFF]++;
                offset++;
            }
        }
    }
    
    /**
     * 
     */
    @Override
    public void end() throws IOException {
        if(operation == Operation.ANALYZE_ENCODER) {
            
            /*
             *  Nach der Encoder-Analyse den entsprechenden Huffman Baum aus dem 
             *  ermittelten Histogram erstellen
             */
            huffmanTree = new HuffmanTree();
            huffmanTree.buildFromHistogram(histogram);
        }
        
        super.end();
    }
    
    /**
     * 
     */
    @Override
    public boolean analyzeNecessary(Operation op) {
        return op == Operation.ENCODE;
    }

    /**
     * 
     */
    @Override
    public void decode( DataBlock block, 
                        IDataListener listener) throws IOException {
        
        // Ausgabepuffer vorbereiten
        if(block.data == null) {
            block.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        
        int symbol;
        
        // BitStream erstellen
        BitInputStream stream = new BitInputStream(resource.getCheckedInputStream());
        
        // Kodierten Baum einlesen und erstellen
        huffmanTree = new HuffmanTree();
        huffmanTree.buildFromResource(stream);
        
        // Lädt, dekodiert und sendet Pixelblöcke an Listener  
        while((symbol = huffmanTree.decodeSymbol(stream)) != -1) {

            // Symbol speichern
            block.data.put((byte)symbol);

            // Wenn Blockgröße erreicht an Listener senden
            if(block.data.capacity() == block.data.position()) {

                dispatchData(   IDataListener.Event.DATA_BLOCK_DECODED, 
                                listener, 
                                block);    
            }
        }
        
        // Restliche Daten im Puffer übertragen
        block.lastBlock = true;
        dispatchData(   IDataListener.Event.DATA_BLOCK_DECODED, 
                        listener, 
                        block);     
    }

    /*
     * 
     */
    @Override
    public void encode( DataBlock block, 
                        IDataListener listener) throws IOException {
        if(huffmanTree == null) {
            throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
        }
        
        ByteBuffer buff = block.data;
        
        /**
         *  Symbole im Puffer iterieren, per Huffmantree zu Bitcode umsetzen und 
         *  diesen in der Resource speichern
         */
        while(buff.position() < buff.limit()) {
            
            byte symbol = buff.get();
            
            
        }
    }
    
    /**
     * 
     */
    
}
