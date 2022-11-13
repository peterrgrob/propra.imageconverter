package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class DataFormat {
        
    private Encoding encoding = Encoding.NONE;
    private String alphabet = new String();
    
    // Verwendete Kodierung der Daten
    public enum Encoding {
        NONE(0),
        BASE_2(2),
        BASE_4(4),
        BASE_8(8),
        BASE_16(16),       
        BASE_32(32),
        BASE_64(64);
        
        private final int bitLength;
        
        private Encoding(int bitLength) {
            this.bitLength = bitLength; 
        }
    }

    /**
     *
     * @return
     */
    public int getBitLength() {
        return encoding.bitLength;
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
