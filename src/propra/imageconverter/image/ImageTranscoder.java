package propra.imageconverter.image;

import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.data.DataTranscoder;

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
    public long apply(  Operation op,
                        DataBuffer in,
                        DataBuffer out) {
        if( in == null 
        ||  out == null
        ||  !isValid()) {
            throw new IllegalArgumentException();
        }
        
        // Operation delegieren
        switch(op) {
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
    
    // Zu implementierende Untermethoden
    protected abstract long _encode(DataBuffer in, DataBuffer out);
    protected abstract long _decode(DataBuffer in, DataBuffer out);
    
    /**
     *
     * @param in
     * @param out
     * @return
     */
    protected long _pass(DataBuffer in, DataBuffer out) {
        if(in.getSize() > out.getSize()) {
            throw new IllegalArgumentException("Ungültige Blockgröße");
        }
        
        // Daten kopieren
        out.getBuffer().put(in.getBuffer());
        
        // Positionszeiger zurücksetzen
        out.getBuffer().clear();
        
        // Anzahl der dekodierten Bytes setzen und zurückgeben
        out.setDataLength(in.getSize());        
        return in.getSize();
    }
    
    /**
     *
     * @param inFormat
     */
    @Override
    public void begin() {

    } 
}
