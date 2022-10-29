package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageTranscoderColor implements ImageTranscoder {

    @Override
    public DataBuffer encode(DataBuffer src, ColorFormat srcFormat, 
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
    public DataBuffer decode(DataBuffer src, ColorFormat srcFormat, 
                             DataBuffer target, ColorFormat targetFormat) {
        
        return encode(src, srcFormat, target, targetFormat);
    }
}
