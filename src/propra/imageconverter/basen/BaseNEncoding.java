package propra.imageconverter.basen;

/* 
 *  Kodierungstypen der Daten mit parametrisierten
 *  Einstellungen für die Base-N Kodierung, dabei ist die Blocklänge
 *  immer ein gemeinsames vielfaches von 8 Bit und dem Bitcount der 
 *  Base-N Kodierung.
 */
public enum BaseNEncoding {
    NONE(0,0, 0),
    BASE_2(1,1, 8),
    BASE_4(2,1, 4),
    BASE_8(3,3, 8),
    BASE_16(4,1, 2),       
    BASE_32(5,5, 8),
    BASE_64(6,3,4);

    // Parameter für Base-N Kodierungen
    private final int blockLength;
    private final int charLength;
    private final int bitCount;

    /**
     * 
     * @param bitCount
     * @param blockLength
     * @param charLength 
     */
    private BaseNEncoding(  int bitCount, 
                            int blockLength, 
                            int charLength) {
        this.blockLength = blockLength; 
        this.bitCount = bitCount;
        this.charLength = charLength;
    }

    public int getBlockLength() {
        return blockLength;
    }

    public int getCharLength() {
        return charLength;
    }

    public int getBitCount() {
        return bitCount;
    }  
}
