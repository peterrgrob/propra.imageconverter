package propra.imageconverter.image;

import propra.imageconverter.data.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageFilterColor extends ImageFilter {

    /**
     * 
     */
    @Override
    public void begin() {}

    /**
     * 
     * @param inOut
     * @return 
     */
    @Override
    public DataBuffer apply(DataBuffer inOut) {
        return apply(inOut, inOut);
    }

    /**
     * 
     * @param in
     * @param out
     * @return 
     */
    @Override
    public DataBuffer apply(DataBuffer in, DataBuffer out) {
        if( in == null 
        ||  out == null
        ||  !isValid()) {
            throw new IllegalArgumentException();
        }
        
        // Generische Farbkonvertierung
        if(!inFormat.equals(outFormat)) {
            ColorFormat.convertColorBuffer(in, inFormat, 
                                            out, outFormat);
        }
        
        return out;
    }

    /**
     * 
     */
    @Override
    public void end() {
    }

    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return (inFormat != null);
    }
}
