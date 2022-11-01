package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class ImageFilterColor implements ImageFilter, Validatable {
    
    @Override
    public void beginFilter() {}

    @Override
    public DataBuffer filter(DataBuffer src, ColorFormat srcFormat) {
        return filter(src, srcFormat, src, srcFormat);
    }

    @Override
    public DataBuffer filter(   DataBuffer src, ColorFormat srcFormat, 
                                DataBuffer target, ColorFormat targetFormat) {
        if( src == null 
        ||  srcFormat == null
        ||  target == null
        ||  targetFormat == null) {
            throw new IllegalArgumentException();
        }
        
        // Generische Farbkonvertierung
        if(!srcFormat.equals(targetFormat)) {
            ColorFormat.convertColorBuffer( src, srcFormat, 
                                        target, targetFormat);
        }
        
        return target;
    }

    @Override
    public void endFiter() {}

    @Override
    public boolean isValid() {return true;}
}
