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
        ColorFormat.convertColor(src.getBytes(), 0, srcFormat, 
                                target.getBytes(), 0, targetFormat,
                                src.getSize());
        
        return target;
    }

    @Override
    public DataBuffer decode(DataBuffer src, ColorFormat srcFormat, 
                             DataBuffer target, ColorFormat targetFormat) {
        
        return encode(src, srcFormat, target, targetFormat);
    }
}
