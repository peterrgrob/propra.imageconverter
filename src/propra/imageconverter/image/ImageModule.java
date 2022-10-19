package propra.imageconverter.image;

import java.nio.ByteOrder;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.Validatable;

/**
 * Basisklasse für Bildformatspezifische Konvertierungen. 
 * 
 * @author pg
 */
public abstract class ImageModule implements Checkable, Validatable {
    
    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    protected ImageHeader header;
    Checksum checksumObj;  
    int headerSize;
    long streamLen;

    /**
     *
     * @param streamLen
     */
    public ImageModule(long streamLen) {
        this.streamLen = streamLen;
    }
            
    /**
     * Wandelt einen allgemeinen ImageHeader in Bytes um.
     * 
     * @param info
     * @return
     */
    public abstract DataBuffer headerOut(ImageHeader info);

    /**
     * Wandelt Bytes in einen allgmeinen ImageHeader um. 
     * 
     * @param data
     * @return
     */
    public abstract ImageHeader headerIn(DataBuffer data);
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @param data
     * @param colorFormat
     * @return
     */
    public DataBuffer dataOut(DataBuffer data, ColorFormat colorFormat) {
        if(!isValid() 
        || data == null) {
            throw new IllegalArgumentException();
        }
        
        // Generische Farbkonvertierung
        if(header.getColorType().compareTo(colorFormat) != 0) {
            byte[] color = data.getBytes();
            
            for(int i=0; i<data.getSize(); i+=3) {
                header.getColorType().convertColor(color, 
                                        i,
                                        colorFormat);
            }
        }
        
        updateCheck(data.getBytes());
        
        return data;
    }
    
    /**
     * Erstellt Image aus Byte Daten 
     * 
     * @param data
     * @return
     */
    public DataBuffer dataIn(DataBuffer data) {
        if(!isValid() 
        || data == null) {
            throw new IllegalArgumentException();
        }
               
        updateCheck(data.getBytes());                
        return data;
    }
    
    /**
     *
     */
    public void beginDataTransfer() {
        if(isCheckable()) {
            checksumObj.begin();
        }
    }

    /**
     *
     * @return
     */
    public long finishDataTransfer() {
        if(isCheckable()) {
            header.setChecksum(checksumObj.finish());
            return header.getChecksum();
        }
        return 0;
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
     */
    protected void updateCheck(byte[] bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        if(isCheckable()) {
            checksumObj.update(bytes,0,bytes.length);
        }
    } 
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return (    header != null 
                &&  streamLen > 0);
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
}
