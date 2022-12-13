package propra.imageconverter.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import propra.imageconverter.data.BitResource;
import propra.imageconverter.data.DataBlock;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.DataFormat.Operation;
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
            huffmanTree.buildFromResource(new BitResource(resource));
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
            ArrayList<HuffmanTree.SymbolTupel> hl = new ArrayList<>();
            
            // Symbole mit Anzahl größer 0 in Liste packen
            for(int i=0; i<256; i++) {
                if(histogram[i] > 0) {
                    hl.add(new HuffmanTree.SymbolTupel( Byte.valueOf((byte)i), 
                                                        histogram[i]));
                }
            }
            
            // Baum erstellen
            huffmanTree.buildFromHistogram(hl);
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
