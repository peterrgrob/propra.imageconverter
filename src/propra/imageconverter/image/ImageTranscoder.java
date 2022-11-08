package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataFormat;
import propra.imageconverter.util.DataTranscoder;

/**
 *
 * @author pg
 */
public abstract class ImageTranscoder implements DataTranscoder {
    
    private ColorFormat inFormat;

    /**
     *
     * @param inFormat
     */
    public void begin(ColorFormat inFormat) {
        this.inFormat = inFormat;
    }   

    /**
     *
     */
    @Override
    public void end() {
 
    }
    
    @Override
    public long transcode(Operation op,
                                DataBuffer in,
                                DataBuffer out) {
        if( in == null 
        ||  out == null
        ||  !isValid()) {
            throw new IllegalArgumentException();
        }
        
        switch(op) {
            case PASS -> {
                return _pass(in, out);
            }
            case ENCODE -> {
                return _encode(in, out);
            }
            case DECODE -> {   
                return _decode(in, out);
            }
        }
            
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     * Get the value of inFormat
     *
     * @return the value of inFormat
     */
    public ColorFormat getFormat() {
        return inFormat;
    }
    
    protected abstract long _encode(DataBuffer in, DataBuffer out);
    protected abstract long _decode(DataBuffer in, DataBuffer out);
    protected abstract long _pass(DataBuffer in, DataBuffer out);
    
    /**
     *
     * @param inFormat
     */
    @Override
    public void begin(DataFormat inFormat) {
        
    } 
}
