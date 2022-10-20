package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
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
    
    protected ImageTranscoderColor colorTranscoder = new ImageTranscoderColor(); 
    protected int blockSize = 4096 * 3;
    protected RandomAccessFile stream;
    protected ImageHeader header;
    protected Checksum checksumObj;  
    protected int headerSize;
    protected long bytesRead;

    /**
     *
     */
    public ImageModule(RandomAccessFile stream) {
        this.stream = stream;
    }
            
    /**
     * Wandelt einen allgemeinen ImageHeader in Bytes um.
     * 
     * @param info
     * @return
     */
    public abstract void writeHeader(ImageHeader info) throws IOException;

    /**
     * Wandelt Bytes in einen allgmeinen ImageHeader um. 
     * 
     * @param data
     * @return
     */
    public abstract ImageHeader readHeader() throws IOException;
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @param data
     * @param colorFormat
     * @return
     */
    public DataBuffer writeImageData(DataBuffer data, int len, ColorFormat colorFormat) throws IOException {
        if(!isValid() 
        || data == null
        || len <= 0) {
            throw new IllegalArgumentException();
        }
        
        colorTranscoder.encode( data, 
                                colorFormat, 
                                data, 
                                header.getColorType(), 
                                len);        
        updateCheck(data.getBytes(), len);  
        stream.write(data.getBytes(), 0, len);
       
        return data;
    }
    
    /**
     * Erstellt Image aus Byte Daten 
     * 
     * @param data
     * @return
     */
    public int readImageData(DataBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        int len = buffer.getSize();
        
        if(bytesRead + buffer.getSize() > header.getBufferSize()) {
            len = (int)(header.getBufferSize() - bytesRead);
        }
        
        if(len != stream.read(buffer.getBytes(), 0, len)) {
            throw new IOException();
        }
        
        updateCheck(buffer.getBytes(), len);   
        bytesRead += len; 
        return len;
    }
    
    /**
     *
     */
    public void beginImageData() {
        if(isCheckable()) {
            checksumObj.begin();
        }
        
        bytesRead = 0;
    }
    
    /**
     *
     * @return
     */
    public long endImageData() {
        if(isCheckable()) {
            header.setChecksum(checksumObj.end());
        }
        return bytesRead;
    }
    
    public boolean hasMoreImageData() throws IOException {
        return (header.getBufferSize() - bytesRead) != 0;
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
     * @return
     */
    @Override
    public long getChecksum() {
        if( isCheckable()
        &&  checksumObj != null) {
            return checksumObj.getValue();
        }
        return 0;
    }
    
    /**
     *
     * @param bytes
     */
    protected void updateCheck(byte[] bytes, int len) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        if(isCheckable()) {
            checksumObj.update(bytes,0,len);
        }
    } 
    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return (    header != null 
                &&  stream != null);
    }

    /**
     *
     * @return
     */
    public int getHeaderSize() {
        return headerSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    /**
     *
     * @return
     */
    public ImageHeader getHeader() {
        return header;
    }
}
