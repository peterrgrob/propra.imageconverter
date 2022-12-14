package propra.imageconverter.image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import propra.imageconverter.data.BitStream;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import static propra.imageconverter.data.DataCodecRaw.DEFAULT_BLOCK_SIZE;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataListener;
import propra.imageconverter.data.IDataResource;

/**
 *
 * @author pg
 */
public class HuffmanCodec extends DataCodecRaw {
    
    //  Histogramm der Daten
    private final int[] histogram = new int[256]; 
    
    //  Huffman-Baum zur Kodierung
    private HuffmanTree huffmanTree;
    
    /*
     *  Konstruktor
     */
    public HuffmanCodec(IDataResource resource) {
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
        } else if(op == Operation.DECODE) {    
            // Kodierten Baum einlesen und erstellen
            huffmanTree = new HuffmanTree();
            huffmanTree.buildFromResource(new BitStream(resource));
            
            // TODO Gelesene Daten filtern
            // dispatchEvent(IDataListener.Event.DATA_IO_READ, target, block);
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

    @Override
    public void decode(DataBlock block, IDataListener listener) throws IOException {
        if(huffmanTree == null) {
            throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
        }
        
        // Puffer vorbereiten
        if(block.data == null) {
            block.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        }
        DataBlock inBlock = new DataBlock();
        inBlock.data = ByteBuffer.allocate(DEFAULT_BLOCK_SIZE);
        
        int symbol;
        
        /*
         * Lädt, dekodiert und sendet Pixelblöcke an Listener  
         */
        while(resource.position() < resource.length()) {
            
            // Block von Resource laden
            super.decode(inBlock, listener);

            // BitStream erstellen
            BitStream stream = new BitStream(inBlock);
        
            // Bits dekodieren
            while((symbol = huffmanTree.decodeBits(stream)) != -1) {
                
                // Symbol speichern
                block.data.put((byte)symbol);

                // Wenn Blockgröße erreicht an Listener senden
                if(block.data.capacity() == block.data.position()) {
                    
                    dispatchEvent(  IDataListener.Event.DATA_BLOCK_DECODED, 
                                    listener, 
                                    block);
                    
                    block.data.clear();
                }
            }
            
            inBlock.data.clear();
        }
    }

    /*
     * 
     */
    @Override
    public void encode(DataBlock block, IDataListener listener) throws IOException {
        if(huffmanTree == null) {
            throw new IllegalStateException("Huffmann-Tree nicht initialisiert.");
        }
        
        
    }
    
    /**
     * 
     */
    
}
