package propra.imageconverter.image;

import java.nio.ByteOrder;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.util.DataBuffer;

/**
 * Basisklasse für Bildformatspezifische Reader/Writer Operationen. 
 * 
 * @author pg
 */
public abstract class ImagePlugin implements Checkable {
    
    int headerSize;
    int headerPosition;
    protected ImageHeader header;
    Checksum checksumObj; 
    int initialAvailableBytes;
    int contentPosition;
    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    
    /**
     * Wandelt einen allgemeinen ImageHeader in Bytes um.
     * 
     * @param info
     * @return
     */
    public abstract DataBuffer headerToBytes(ImageHeader info);

    /**
     * Wandelt Bytes in einen allgmeinen ImageHeader um. 
     * 
     * @param data
     * @return
     */
    public abstract ImageHeader bytesToHeader(DataBuffer data);
    
    /**
     * Wandelt Bilddaten in bytes um. 
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
     * @param image
     * @return
     */
    public ImageBuffer contentToBytes_(ImageBuffer image) {
        if(!header.isValid() 
        || image == null) {
            throw new IllegalArgumentException();
        }
        
        ImageBuffer output = image.convertTo(header);
        return output; 
    }
    
    
    /**
     * Erstellt ImageBuffer aus Byte Daten 
     * 
     * @param data
     * @return
     */
    public ImageBuffer bytesToContent(DataBuffer data) {
        return new ImageBuffer(data.getBuffer().array(), header);
    }
    
    /**
     * Berechnet aktuelle Prüfsumme für Byte Daten und speichert im Header 
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

    /**
     *
     * @return
     */
    public int getHeaderSize() {
        return headerSize;
    }

    /**
     *
     * @param headerSize
     */
    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    /**
     *
     * @return
     */
    public int getHeaderPosition() {
        return headerPosition;
    }

    /**
     *
     * @param headerPosition
     */
    public void setHeaderPosition(int headerPosition) {
        this.headerPosition = headerPosition;
    }

    /**
     *
     * @return
     */
    public int getContentPosition() {
        return contentPosition;
    }

    /**
     *
     * @param contentPosition
     */
    public void setContentPosition(int contentPosition) {
        this.contentPosition = contentPosition;
    }

    /**
     *
     * @return
     */
    public ImageHeader getHeader() {
        return header;
    }

    /**
     *
     * @param header
     */
    public void setHeader(ImageHeader header) {
        this.header = header;
    }

    /**
     *
     * @return
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     *
     * @param byteOrder
     */
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     *
     * @return
     */
    public int getInitialAvailableBytes() {
        return initialAvailableBytes;
    }

    /**
     *
     * @param initialAvailableBytes
     */
    public void setInitialAvailableBytes(int initialAvailableBytes) {
        this.initialAvailableBytes = initialAvailableBytes;
    }
}
