package propra.imageconverter.image;

import java.nio.ByteBuffer;
import propra.imageconverter.data.IDataTranscoder;

/**
 *
 * @author pg
 */
public abstract class ImageTranscoder implements IDataTranscoder {
    
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
                        ByteBuffer in,
                        ByteBuffer out) {
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
    protected abstract long _encode(ByteBuffer in, ByteBuffer out);
    protected abstract long _decode(ByteBuffer in, ByteBuffer out);
    
    /**
     *
     * @param in
     * @param out
     * @return
     */
    protected long _pass(ByteBuffer in, ByteBuffer out) {
        if(in.limit()> out.limit()) {
            throw new IllegalArgumentException("Ungültige Blockgröße");
        }
        
        // Daten kopieren
        out.put(in);
        
        // Positionszeiger zurücksetzen
        out.clear();
        
        // Anzahl der dekodierten Bytes setzen und zurückgeben
        out.limit(in.limit());        
        return in.limit();
    }
    
    /**
     *
     * @param inFormat
     */
    @Override
    public void begin() {

    } 
}
