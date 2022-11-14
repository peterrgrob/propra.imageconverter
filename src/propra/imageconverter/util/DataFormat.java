package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class DataFormat {
    
    protected Encoding encoding = Encoding.NONE;
    private String alphabet = new String();
    
    // Verwendete Kodierung der Daten
    public enum Encoding {
        NONE(0,0),
        BASE_2(1,1),
        BASE_4(2,1),
        BASE_8(3,3),
        BASE_16(4,1),       
        BASE_32(5,5),
        BASE_64(6,3),
        RLE(0,0);
        
        // Bitlänge für BaseN Kodierungen
        private final int blockLength;
        private final int bitCount;
        
        private Encoding(int bitCount, int blockLength) {
            this.blockLength = blockLength; 
            this.bitCount = bitCount;
        }
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
        setEncoding(encoding);
    } 
    
    
    /**
     *
     * @param alphabet
     */
    public DataFormat(String alphabet) {
        setEncoding(alphabet);
    } 
    
        /**
     * 
     * @param src
     */
    public DataFormat(DataFormat src) {
        encoding = src.encoding;
    }
    
    /**
     *
     * @return
     */
    public String getAlphabet() {
        return alphabet;
    }
    
    /**
     *
     * @return
     */
    public int getBlockLength() {
        return encoding.blockLength;
    }
    
    /**
     *
     * @return
     */
    public int getBitCount() {
        return encoding.bitCount;
    }
    
    /**
     *
     * @return
     */
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     *
     * @param dataEncoding
     */
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }
    
    
    /**
     *
     * @param dataEncoding
     */
    public void setEncoding(String alphabet) {
        if(alphabet == null) {
            throw new IllegalArgumentException();
        }
        
        this.alphabet = new String(alphabet);
        
        // Kodierung ableiten
        switch(alphabet.length()) {
            case 2 -> {this.encoding = Encoding.BASE_2;}
            case 4 -> {this.encoding = Encoding.BASE_4;}     
            case 8 -> {this.encoding = Encoding.BASE_8;}
            case 16 -> {this.encoding = Encoding.BASE_16;}
            case 32 -> {this.encoding = Encoding.BASE_32;}
            case 64 -> {this.encoding = Encoding.BASE_64;}
            default -> {throw new IllegalArgumentException("Ungültige Base-N Alphabetlänge");}
        }
    }
}
