package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class BaseN implements DataTranscoder{
    
    String alphabet;
    
    /**
     *
     * @param alphabet
     */
    public BaseN(String alphabet) {
        alphabet = new String(alphabet);
    }
    
    @Override
    public void begin() {
        
    }

    @Override
    public long transcode(Operation op, DataBuffer in, DataBuffer out) {
        if( !isValid()
        ||  in == null
        ||  out == null) {
            throw new IllegalArgumentException();
        }
        
        
        
        return 0;
    }

    @Override
    public void end() {
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
