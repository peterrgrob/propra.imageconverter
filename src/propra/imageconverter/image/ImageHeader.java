package propra.imageconverter.image;

import propra.imageconverter.util.Validatable;

/**
 * Klasse für einen allgemeinen Bildkopf
 * 
 * @author pg
 */
public class ImageHeader implements Validatable {
    
    // Attribute
    private int width;
    private int height;
    private int pixelSize;    
    private long checksum;
    private ColorFormat colorType = new ColorFormat();
  
    /**
     * 
     */
    public ImageHeader() {
    }

    /**
     *
     * @param src
     */
    public ImageHeader(ImageHeader src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }
        
        this.width = src.width;
        this.height = src.height;
        this.pixelSize = src.pixelSize;
        this.checksum = src.checksum;
        this.colorType = new ColorFormat(src.getColorFormat());
    }
    
    /**
     * 
     * @return true, wenn zulässiger Bildkopf vorliegt.
     */
    @Override
    public boolean isValid() {
        return (    width > 0 
                &&  height > 0 
                &&  pixelSize == 3);
    }
    
    /**
     * 
     * @return 
     */
    public int getPixelCount() {
        return width * height;
    }
    
    public int getImageSize() {
        return width * height * pixelSize;
    }
    
    public ColorFormat getColorFormat() {
        return colorType;
    }

    public void setColorFormat(ColorFormat colorType) {
        this.colorType = colorType;
    }
    
    public int getPixelSize() {
        return pixelSize;
    }

    public void setPixelSize(int elementSize) {
        this.pixelSize = elementSize;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
    
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
