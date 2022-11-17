package propra.imageconverter.data;

/**
 *
 * @author pg
 */
public interface DataTranscoder {
    
    public enum Operation {
        ENCODE,
        DECODE,
        FILTER,
        NONE;
    }
    
    /**
     *
     */
    public void begin();
     
    /**
     *
     * @param op
     * @param in
     * @param out
     * @return Anzahl der kodierten Bytes
     */
    public long apply(  Operation op,
                        DataBuffer in,
                        DataBuffer out);
    
    /**
     *
     */
    public void end();
    
    /**
     *
     * @param dataFormat
     */
    public void dataFormat(DataFormat dataFormat);
    
    /**
     *
     * @return Aktuelles Daten Format
     */
    public DataFormat dataFormat();
}
