package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public class ImageTranscoderColor implements ImageTranscoder {

    @Override
    public DataBuffer encode(DataBuffer src, ColorFormat srcFormat, 
                             DataBuffer target, ColorFormat targetFormat, int len) {
        if( src == null 
        ||  srcFormat == null
        ||  target == null
        ||  targetFormat == null) {
            throw new IllegalArgumentException();
        }
        
        // Generische Farbkonvertierung
        ColorFormat.convertColorArray(  src.getBytes(), 0, srcFormat, 
                                        target.getBytes(), 0, targetFormat,
                                        len);
        
        return target;
    }

    @Override
    public DataBuffer decode(DataBuffer src, ColorFormat srcFormat, 
                             DataBuffer target, ColorFormat targetFormat, int len) {
        
        return encode(src, srcFormat, target, targetFormat, len);
    }
}
