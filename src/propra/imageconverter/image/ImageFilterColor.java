package propra.imageconverter.image;

import propra.imageconverter.data.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class ImageFilterColor extends ImageFilter {
       
    @Override
    public void begin() {}

    @Override
    public DataBuffer filter(DataBuffer inOut) {
        return filter(inOut, inOut);
    }

    @Override
    public DataBuffer filter(DataBuffer in, DataBuffer out) {
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

    @Override
    public void end() {
    }

    @Override
    public boolean isValid() {
        return (inFormat != null);
    }

    @Override
    public void reset() {
        inFormat = null;
        outFormat = null;
    }
}
