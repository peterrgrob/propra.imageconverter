package propra.imageconverter.data;

import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public interface DataTranscoder extends Validatable {
    
    public enum Operation {
        ENCODE,
        DECODE,
        PASS,
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
    public long transcode(  Operation op,
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
    public void setDataFormat(DataFormat dataFormat);
    
    /**
     *
     * @return Aktuelles Daten Format
     */
    public DataFormat getDataFormat();
}
