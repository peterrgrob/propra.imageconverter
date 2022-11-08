package propra.imageconverter.image;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Closeable;
import propra.imageconverter.util.Checkable;
import propra.imageconverter.util.Checksum;
import propra.imageconverter.util.DataBuffer;
import propra.imageconverter.util.DataTranscoder;
import propra.imageconverter.util.Validatable;

/**
 * Basisklasse für Bildformatspezifische Konvertierungen. 
 * 
 * @author pg
 */
public abstract class ImageModel implements Closeable, Checkable, Validatable {
    
    protected ImageFilterColor colorFilter; 
    protected ImageTranscoder colorCompression;    
    
    protected final int blockSize = 128 * 4096 * 3;
    protected DataBuffer temporaryBuffer = new DataBuffer(blockSize);
    protected long bytesTransfered;
        
    protected RandomAccessFile stream;
    protected Checksum checksumObj;  
        
    protected ImageHeader header;
    protected int headerSize;


    /**
     *
     * @param stream
     */
    public ImageModel(RandomAccessFile stream) {
        this.stream = stream;
        this.colorFilter = new ImageFilterColor();
    }
            
    /**
     * Wandelt einen allgemeinen ImageHeader in Bytes um.
     * 
     * @param info
     * @throws java.io.IOException
     */
    public abstract void writeHeader(ImageHeader info) throws IOException;

    /**
     * Wandelt Bytes in einen allgmeinen ImageHeader um. 
     * 
     * @return
     * @throws java.io.IOException
     */
    public abstract ImageHeader readHeader() throws IOException;
    
    /**
     * Wandelt Bilddaten in bytes um. 
     * 
     * @param data
     * @param colorFormat
     * @return
     * @throws java.io.IOException
     */
    public DataBuffer writeImageData(DataBuffer data, ColorFormat colorFormat) throws IOException {
        if(!isValid() 
        || data == null) {
            throw new IllegalArgumentException();
        }

        colorFilter.setInFormat(colorFormat);
        colorFilter.filter(data);        
        
        updateChecksum(data);  
        writeDataToStream(data, 0, data.getCurrDataLength());
        return data;
    }
    
    /**
     * Erstellt Image aus Byte Daten 
     * 
     * @param buffer
     * @return
     * @throws java.io.IOException
     */
    public int readImageData(DataBuffer buffer) throws IOException {
        if(!isValid() 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Kompression?
        if(colorCompression != null) {
            // Daten aus der Datei lesen und dekodieren
            int len = temporaryBuffer.getSize();
            long remainingBytes = stream.length() - stream.getFilePointer();
            if(temporaryBuffer.getSize() > remainingBytes) {
                len = (int)(remainingBytes);
            }
            readDataFromStream(temporaryBuffer, 0, len);
            colorCompression.transcode( DataTranscoder.Operation.DECODE, 
                                        temporaryBuffer, 
                                        buffer);
            
        } else {
            // Daten aus der Datei direkt in buffer lesen
            int len = buffer.getSize();
            if(bytesTransfered + buffer.getSize() > header.getImageSize()) {
                len = (int)(header.getImageSize() - bytesTransfered);
            }
            readDataFromStream(buffer, 0, len);
        }
        
        buffer.getBuffer().clear();
        updateChecksum(buffer);   
        bytesTransfered += buffer.getCurrDataLength(); 
        return buffer.getCurrDataLength();
    }
    
    /**
     *
     */
    public void beginImageData() {
        if(isCheckable()) {
            checksumObj.begin();
        }
        
        colorFilter.setOutFormat(header.getColorFormat());
        colorFilter.begin();
        
        colorCompression = header.getColorFormat().createTranscoder();
        if(colorCompression != null) {
            colorCompression.begin(header.getColorFormat());
        }
        
        bytesTransfered = 0;
    }
    
    /**
     *
     * @return
     */
    public long endImageData() {
        if(isCheckable()) {
            checksumObj.end();
        }
        colorFilter.end(); 
        if(colorCompression != null) {
            colorCompression.end();
        }
        return bytesTransfered;
    }
    
    /**
     *
     * @return
     * @throws IOException
     */
    public boolean hasMoreImageData() throws IOException {
        return (header.getImageSize() - bytesTransfered) != 0;
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
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        if(stream != null) {
            stream.close();
        }
    }

    /**
     *
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    protected DataBuffer readDataFromStream(DataBuffer buffer, int offset, int len) throws IOException {
        if(stream == null
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        int rl = stream.read(buffer.getBytes(), offset, len);
        if(rl != len) {
            throw new IOException();
        }
        buffer.setCurrDataLength(len);
        return buffer;
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    protected DataBuffer writeDataToStream(DataBuffer buffer, int offset, int len) throws IOException {
        if(stream == null 
        || buffer == null) {
            throw new IllegalArgumentException();
        }
        stream.write(buffer.getBytes(), offset, len);
        return buffer;
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
    protected void updateChecksum(DataBuffer bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        if(isCheckable()) {
            checksumObj.filter(bytes);
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

    /**
     *
     * @return
     */
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
