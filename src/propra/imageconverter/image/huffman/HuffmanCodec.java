package propra.imageconverter.image.huffman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.DataFormat.Operation;
import propra.imageconverter.data.IDataResource;

/**
 *
 * @author pg
 */
public class HuffmanCodec extends DataCodecRaw {
    
    /**
     *  Histogramm der Daten
     */
    private final int[] histogram = new int[256]; 
    
    /**
     *  Huffman-Baum zur Kodierung
     */
    private HuffmanTree<Byte> huffmanTree;
    
    /**
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
        if(op == Operation.ANALYZE) {
            Arrays.fill(histogram, 0);
        }
    }
 
    
    
    /**
     *  Ermittelt die Häufigkeit der jeweiligen Daten 
     */
    @Override
    public void analyze(DataBlock block) {
        if(block == null) {
            throw new IllegalArgumentException();
        }
        
        // Histogram erstellen
        byte[] buffer = block.array();
        int offset = 0;
        
        while(offset < block.data.limit()) {
            histogram[buffer[offset] & 0xFF]++;
            offset++;
        }
    }
    
    /**
     * 
     */
    @Override
    public void end() throws IOException {
        if(operation == Operation.ANALYZE) {
            /**
             *  Baum aus Histogramm erstellen
             */
            huffmanTree = new HuffmanTree<>();
            ArrayList<HuffmanTree.SymbolTupel> hl = new ArrayList<>();
            
            for(int i=0;i<256;i++) {
                if(histogram[i] > 0) {
                    hl.add(new HuffmanTree.SymbolTupel(Byte.valueOf((byte)i), histogram[i]));
                }
            }
            
            huffmanTree.build(hl);
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
}
