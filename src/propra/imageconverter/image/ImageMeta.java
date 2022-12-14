package propra.imageconverter.image;

/**
 * Klasse für einen allgemeinen Bildkopf
 * 
 */
public class ImageMeta {
    
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
    public ImageMeta() {
    }

    /**
     *
     */
    public ImageMeta(ImageMeta src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }
        
        this.width = src.width;
        this.height = src.height;
        this.pixelSize = src.pixelSize;
        this.checksum = src.checksum;
        this.colorType = new ColorFormat(src.colorFormat());
    }
    
    /**
     * 
     */
    public boolean isValid() {
        return (    width > 0 
                &&  height > 0 
                &&  pixelSize == 3);
    }
    
    /*
     *  Getter/Setter
     */
    public int pixelCount() {
        return width * height;
    }
    
    public int imageSize() {
        return width * height * pixelSize;
    }
    
    public ColorFormat colorFormat() {
        return colorType;
    }

    public void colorFormat(ColorFormat colorType) {
        this.colorType = colorType;
    }
    
    public int pixelSize() {
        return pixelSize;
    }

    public void pixelSize(int elementSize) {
        this.pixelSize = elementSize;
    }

    public long checksum() {
        return checksum;
    }

    public void checksum(long checksum) {
        this.checksum = checksum;
    }
    
    public int height() {
        return height;
    }

    public void height(int height) {
        this.height = height;
    }

    public int width() {
        return width;
    }

    public void width(int width) {
        this.width = width;
    }

    public long encodedSize() {
        return dataLength;
    }

    public void encodedSize(long encodedSize) {
        this.dataLength = encodedSize;
    }
}
