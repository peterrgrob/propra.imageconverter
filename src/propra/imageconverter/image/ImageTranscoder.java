package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public interface ImageTranscoder {

    /**
     *
     * @param src
     * @param srcFormat
     * @param target
     * @param targetFormat
     * @return
     */
    public DataBuffer encode(   DataBuffer src, ColorFormat srcFormat, 
                                DataBuffer target, ColorFormat targetFormat);
    
    /**
     *
     * @param src
     * @param srcFormat
     * @param target
     * @param targetFormat
     * @return
     */
    public DataBuffer decode(   DataBuffer src, ColorFormat srcFormat, 
                                DataBuffer target, ColorFormat targetFormat);
}
