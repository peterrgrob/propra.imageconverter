package propra.imageconverter.image;

import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public interface ImageFilter {

    /**
     *
     * @return
     */
    public void beginFilter();
    
    /**
     *
     * @param src
     * @param srcFormat
     * @return
     */
    public DataBuffer filter(DataBuffer src, ColorFormat srcFormat);
    
    /**
     *
     * @param src
     * @param srcFormat
     * @param target
     * @param targetFormat
     * @return
     */
    public DataBuffer filter(   DataBuffer src, ColorFormat srcFormat,
                                DataBuffer target, ColorFormat targetFormat);
    
    /**
     *
     * @return
     */
    public void endFiter();
}
