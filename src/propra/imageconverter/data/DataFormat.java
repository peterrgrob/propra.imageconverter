package propra.imageconverter.data;

/**
 *
 */
public class DataFormat {
    
    // Aktuelle Kodierung
    protected Encoding encoding = Encoding.NONE;
    
    // Kodierungstypen der Daten
    public enum Encoding {
        NONE,
        RLE,
        BASEN,
        HUFFMAN;
    }

    /**
     *
     */
    public DataFormat() {
    }
    
    /**
     *
     * @param encoding
     */
    public DataFormat(Encoding encoding) {
        this.encoding(encoding);
    } 
    
    /**
     * 
     * @param src
     */
    public DataFormat(DataFormat src) {
        this.encoding = src.encoding;
    }
    
    /**
     * 
     * @param encoding
     * @return  
     */
    public DataFormat encoding(Encoding encoding) {
        this.encoding = encoding;
        return this;
    }
    
    /**
     * 
     * @return 
     */
    public Encoding encoding() {
        return encoding;
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return true;
    }
}
