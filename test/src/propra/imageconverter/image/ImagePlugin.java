package propra.imageconverter.image;

import java.nio.ByteOrder;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.util.DataBuffer;

/**
 *
 * @author pg
 */
public abstract class ImagePlugin implements Checkable {
    int headerSize;
    int headerPosition;
    int contentPosition;
    protected ImageHeader header;
    Checksum checksumObj;
    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    int initialAvailableBytes;
    
    /**
     *
     * @param info
     * @return
     */
    public abstract DataBuffer headerToBytes(ImageHeader info);

    /**
     *
     * @param data
     * @return
     */
    public abstract ImageHeader bytesToHeader(DataBuffer data);
    
    /**
     *
     * @param image
     * @param headerData
     * @return
     */
    public ImageBuffer contentToBytes(ImageBuffer image, DataBuffer headerData) {
        if(!header.isValid() 
        || image == null
        || headerData == null) {
            throw new IllegalArgumentException();
        }
        
        ImageBuffer output = image.convertTo(header);
        output.getHeader().setChecksum(checkContent(output, headerData));
        return output; 
    }

    /**
     *
     * @param data
     * @return
     */
    public ImageBuffer bytesToContent(DataBuffer data) {
        return new ImageBuffer(data.getBuffer().array(), header);
    }
    
    /**
     *
     * @param data
     * @param headerData
     * @return
     */
    public long checkContent(DataBuffer data, DataBuffer headerData) {
        return header.getChecksum();
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean isCheckable() {
        return (checksumObj != null);
    }

    /**
     *
     * @return
     */
    @Override
    public Checksum getChecksumObj() {
        return checksumObj;
    }

    /**
     *
     * @param bytes
     * @return
     */
    @Override
    public long check(byte[] bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        return checksumObj.update(bytes);
    }  

    public int getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getHeaderPosition() {
        return headerPosition;
    }

    public void setHeaderPosition(int headerPosition) {
        this.headerPosition = headerPosition;
    }

    public int getContentPosition() {
        return contentPosition;
    }

    public void setContentPosition(int contentPosition) {
        this.contentPosition = contentPosition;
    }

    public ImageHeader getHeader() {
        return header;
    }

    public void setHeader(ImageHeader header) {
        this.header = header;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public int getInitialAvailableBytes() {
        return initialAvailableBytes;
    }

    public void setInitialAvailableBytes(int initialAvailableBytes) {
        this.initialAvailableBytes = initialAvailableBytes;
    }
}
