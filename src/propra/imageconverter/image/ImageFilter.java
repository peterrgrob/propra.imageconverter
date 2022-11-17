package propra.imageconverter.image;

import propra.imageconverter.data.DataFilter;

/**
 *
 * @author pg
 */
public abstract class ImageFilter implements DataFilter {
    
    ColorFormat inFormat;
    ColorFormat outFormat;

    /**
     * 
     */
    public ImageFilter() {
        
    }
    
    /**
     * 
     * @param in
     * @param out 
     */
    public ImageFilter(ColorFormat in, ColorFormat out) {
        inFormat = in;
        outFormat = out;
    }
    
    /**
     *
     * @param format
     */
    public void inFormat(ColorFormat format) {
        this.inFormat = format;
    }

    /**
     *
     * @param format
     */
    public void outFormat(ColorFormat format) {
        this.outFormat = format;
    }

    /**
     *
     * @return
     */
    public ColorFormat inFormat() {
        return inFormat;
    }

    /**
     *
     * @return
     */
    public ColorFormat outFormat() {
        return outFormat;
    }
}
