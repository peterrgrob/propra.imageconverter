package propra.imageconverter.util;

import java.util.HashMap;

/**
 *
 * @author pg
 */
public class DataFormat {
    
    protected Encoding encoding = Encoding.NONE;
    
    // Alphabettabellen, aus Performancegründen keine Hashmap
    private String alphabet = new String();
    private byte[] alphabetMap = new byte[256];
    
    // Verwendete Kodierung der Daten mit parametrisierten
    // Einstellungen für die Base-N Kodierung.
    public enum Encoding {
        NONE(0,0, 0),
        BASE_2(1,1, 8),
        BASE_4(2,1, 4),
        BASE_8(3,3, 8),
        BASE_16(4,1, 2),       
        BASE_32(5,5, 8),
        BASE_64(6,3,4),
        RLE(0,0, 0);
        
        // Bitlänge für BaseN Kodierungen
        private final int blockLength;
        private final int charLength;
        private final int bitCount;
        
        private Encoding(   int bitCount, 
                            int blockLength, 
                            int charLength) {
            this.blockLength = blockLength; 
            this.bitCount = bitCount;
            this.charLength = charLength;
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
    public byte[] getAlphabetMap() {
        return alphabetMap;
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
    
    
    public int getCharLength() {
        return encoding.charLength;
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
        
        // Alphabet setzen und Mappingarray erstellen
        this.alphabet = new String(alphabet);
        for(int i=0; i<alphabet.length(); i++) {
            alphabetMap[alphabet.getBytes()[i]] = (byte)i;
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
