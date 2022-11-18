package propra.imageconverter.basen;

import propra.imageconverter.data.DataFormat;

/**
 *
 * @author pg
 */
public class BaseNFormat extends DataFormat {
    
    // Alphabettabellen, aus Performancegründen keine Hashmap
    private String alphabet = new String();
    private byte[] alphabetMap = new byte[256];
    private BaseNEncoding baseEncoding;
    
        /* 
     * Verwendete Kodierung der Daten mit parametrisierten
     * Einstellungen für die Base-N Kodierung, dabei ist die Blocklänge
     * immer ein vielfaches von 8 Bit und dem Bitcount der Base-N Kodierung
     */
    public enum BaseNEncoding {
        NONE(0,0, 0),
        BASE_2(1,1, 8),
        BASE_4(2,1, 4),
        BASE_8(3,3, 8),
        BASE_16(4,1, 2),       
        BASE_32(5,5, 8),
        BASE_64(6,3,4);
        
        // Daten für Base-N Kodierungen
        private final int blockLength;
        private final int charLength;
        private final int bitCount;
        
        private BaseNEncoding(   int bitCount, 
                            int blockLength, 
                            int charLength) {
            this.blockLength = blockLength; 
            this.bitCount = bitCount;
            this.charLength = charLength;
        }
    }

    public BaseNFormat() {
        this.baseEncoding = BaseNEncoding.NONE;
    }
    
    /**
     *
     * @param alphabet
     */
    public BaseNFormat(String alphabet) {
        setEncoding(alphabet);
    } 
    
    /**
     * 
     * @param src
     */
    public BaseNFormat(BaseNFormat src) {
        super(src);
        this.alphabet = new String(src.alphabet);
        System.arraycopy(src.alphabetMap , 0, 
                        this.alphabetMap, 0, 256);
        this.baseEncoding = src.baseEncoding;
    }
    
    /**
     *
     * @param alphabet
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
                case 2 -> {this.baseEncoding = BaseNEncoding.BASE_2;}
                case 4 -> {this.baseEncoding = BaseNEncoding.BASE_4;}     
                case 8 -> {this.baseEncoding = BaseNEncoding.BASE_8;}
                case 16 -> {this.baseEncoding = BaseNEncoding.BASE_16;}
                case 32 -> {this.baseEncoding = BaseNEncoding.BASE_32;}
                case 64 -> {this.baseEncoding = BaseNEncoding.BASE_64;}
                default -> {throw new IllegalArgumentException("Ungültige Base-N Alphabetlänge");}
            }
            
            encoding = Encoding.BASEN;
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        switch(this.baseEncoding) {
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
        return baseEncoding.blockLength;
    }
    
    /**
     *
     * @return
     */
    public int getBitCount() {
        return baseEncoding.bitCount;
    }
    
    public int getCharLength() {
        return baseEncoding.charLength;
    }
}
