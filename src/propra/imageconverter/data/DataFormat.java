package propra.imageconverter.data;

/**
 *
 * @author pg
 */
public class DataFormat {
    
    protected Encoding encoding = Encoding.NONE;
    
    // Alphabettabellen, aus Performancegründen keine Hashmap
    private String alphabet = new String();
    private byte[] alphabetMap = new byte[256];
    
    /* 
     * Verwendete Kodierung der Daten mit parametrisierten
     * Einstellungen für die Base-N Kodierung, dabei ist die Blocklänge
     * immer ein vielfaches von 8 Bit und dem Bitcount der Base-N Kodierung
     */
    public enum Encoding {
        NONE(0,0, 0),
        BASE_2(1,1, 8),
        BASE_4(2,1, 4),
        BASE_8(3,3, 8),
        BASE_16(4,1, 2),       
        BASE_32(5,5, 8),
        BASE_64(6,3,4),
        RLE(0,0, 0);
        
        // Daten für Base-N Kodierungen
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
        this.alphabet = new String(src.alphabet);
        System.arraycopy(src.alphabetMap , 0, 
                        this.alphabetMap, 0, 256);
        this.encoding = src.encoding;
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
        
        if(alphabet.length() > 0) {
            
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
    
    /**
     *
     * @return
     */
    public boolean isValid() {
        switch(this.encoding) {
            case BASE_2, BASE_4, BASE_8, BASE_16, BASE_32, BASE_64 -> {
                if(alphabet == null) {
                    return false;
                }
                if(!(alphabet.length() == 2
                        || alphabet.length() == 4
                        || alphabet.length() == 8
                        || alphabet.length() == 16
                        || alphabet.length() == 32
                        || alphabet.length() == 64)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     *
     * @return
     */
    public boolean isBaseN() {
        switch(this.encoding) {
            case BASE_2, BASE_4, BASE_8, BASE_16, BASE_64 -> {
                return true;
            }
        }
        return false;
    }
    
    public boolean isBinary() {
        return (encoding == Encoding.RLE);
    }
}
