package propra.imageconverter.util;

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
    
    public void begin();
     
    /**
     *
     * @return
     */
    public long transcode(  Operation op,
                            DataBuffer in,
                            DataBuffer out);
    
    public void end();
}
