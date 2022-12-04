package propra.imageconverter.image.huffman;

import java.util.HashMap;
import java.util.Map;
import propra.imageconverter.data.DataCodecRaw;
import propra.imageconverter.data.IDataResource;

/**
 *
 * @author pg
 */
public class HuffmanCodec extends DataCodecRaw {
    
    /**
     *  Histogramm der Daten
     */
    private final Map<Integer, Integer> histogram = new HashMap<>();
    
    /**
     *  Huffman-Baum zur Kodierung
     */
    private final HuffmanTree<Integer> huffmanTree = new HuffmanTree<>();
    
    /**
     *  Konstruktor
     */
    public HuffmanCodec(IDataResource resource) {
        super(resource);
    }
    
}
