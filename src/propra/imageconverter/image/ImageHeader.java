package propra.imageconverter.image;

/**
 * Klasse für einen allgemeinen Bildkopf
 */
public class ImageHeader {
    
    // Bildattribute
    private int width;
    private int height;
    private int pixelSize;   
    private long dataLength;
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
        this.dataLength = src.dataLength;
        this.colorType = new ColorFormat(src.colorFormat());
    }
    
    /**
     * 
     * @return 
     * @return  
     */
    public boolean isValid() {
        return (    width > 0 
                &&  height > 0 
                &&  pixelSize == 3);
    }
    
    /*
     *  Getter/Setter
     */

    /**
     *
     * @return
     */

    public int pixelCount() {
        return width * height;
    }
    
    /**
     *
     * @return
     */
    public long imageSize() {
        return width * height * pixelSize;
    }
    
    /**
     *
     * @return
     */
    public ColorFormat colorFormat() {
        return colorType;
    }

    /**
     *
     * @param colorType
     */
    public void colorFormat(ColorFormat colorType) {
        this.colorType = colorType;
    }
    
    /**
     *
     * @return
     */
    public int pixelSize() {
        return pixelSize;
    }

    /**
     *
     * @param elementSize
     */
    public void pixelSize(int elementSize) {
        this.pixelSize = elementSize;
    }

    /**
     *
     * @return
     */
    public long checksum() {
        return checksum;
    }

    /**
     *
     * @param checksum
     */
    public void checksum(long checksum) {
        this.checksum = checksum;
    }
    
    /**
     *
     * @return
     */
    public int height() {
        return height;
    }

    /**
     *
     * @param height
     */
    public void height(int height) {
        this.height = height;
    }

    /**
     *
     * @return
     */
    public int width() {
        return width;
    }

    /**
     *
     * @param width
     */
    public void width(int width) {
        this.width = width;
    }

    /**
     *
     * @return
     */
    public long encodedSize() {
        return dataLength;
    }

    /**
     *
     * @param encodedSize
     */
    public void encodedSize(long encodedSize) {
        this.dataLength = encodedSize;
    }
}
