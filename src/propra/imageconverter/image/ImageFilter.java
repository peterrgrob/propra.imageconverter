package propra.imageconverter.image;

import propra.imageconverter.util.*;

/**
 *
 * @author pg
 */
public abstract class ImageFilter implements DataFilter {
    
    ColorFormat inFormat;
    ColorFormat outFormat;

    public ImageFilter() {
    }
    
    /**
     *
     * @param format
     */
    public void setInFormat(ColorFormat format) {
        this.inFormat = format;
    }

    /**
     *
     * @param format
     */
    public void setOutFormat(ColorFormat format) {
        this.outFormat = format;
    }

    /**
     *
     * @return
     */
    public ColorFormat getInFormat() {
        return inFormat;
    }

    /**
     *
     * @return
     */
    public ColorFormat getOutFormat() {
        return outFormat;
    }
}
