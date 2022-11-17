package propra.imageconverter.data;

import propra.imageconverter.checksum.Checksum;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.data.DataFormat.Mode;
import propra.imageconverter.image.ImageHeader;

/**
 *
 * @author pg
 */
public class DataReader implements Closeable {
    
    private final Mode mode;
    protected RandomAccessFile binaryReader;
    protected BufferedReader txtReader;
    protected Checksum checksumObj; 
    
    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public DataReader(String file, Mode mode) throws IOException {
        this.mode = mode;
        if(mode == Mode.BINARY) {
            binaryReader = createBinaryReader(file);
        } else {
            txtReader = createTextReader(file);
        }
    }
    
    /**
     * 
     * @return 
     */
    public Mode getMode() {
        return mode;
    }
    
    /**
     * 
     * @throws java.io.IOException
     */
    public void begin() throws IOException {
        // Prüfsumme initialisieren
        if(checksumObj != null) {
            checksumObj.begin();
        }
    }
    
    /**
     * 
     * @return 
     */
    public long end() {
        return 0;
    }
    
    /**
     * 
     * @param buffer
     * @return
     * @throws IOException 
     */
    public int read(DataBuffer buffer) throws IOException {
        return read(buffer, 0, buffer.getDataLength());
    }
    
    /**
     * 
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException 
     */
    public int read(DataBuffer buffer, int offset, int len) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == Mode.BINARY) {
            binaryReader.read( buffer.getBytes(), 
                                offset, 
                                len);
            buffer.setDataLength(len);
        } else {
            throw new UnsupportedOperationException("");
        }
        
        return len;
    }
    
    /**
     * 
     * @return
     * @throws IOException 
     */
    public String readLine() throws IOException {
        if(mode == Mode.BINARY) {
            return binaryReader.readLine();
        } else {
            return txtReader.readLine();
        }
    }
    
    /**
     *
     * @return
     * @throws java.io.IOException
     */
    public long getSize() throws IOException {
        if(mode == Mode.BINARY) {
            return binaryReader.length();
        } else {
            throw new UnsupportedOperationException("");
        }  
    }
    
    
    /**
     *
     * @return
     * @throws IOException
     */
    public boolean hasMoreData() throws IOException {
        if(mode == mode.BINARY) {
            return (binaryReader.length() < binaryReader.getFilePointer());
        } 
        return false;
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private BufferedReader createTextReader(String filePath) throws IOException {
        return new BufferedReader(new FileReader(filePath)); 
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private RandomAccessFile createBinaryReader(String filePath) throws IOException {
        return new RandomAccessFile(filePath, "rw");
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(binaryReader != null) {
            binaryReader.close();
        }
        if(txtReader != null) {
            txtReader.close();
        }
    }
    
    /**
     *
     * @return
     */
    public Checksum getChecksumObj() {
        return checksumObj;
    }

    /**
     *
     * @return
     */
    public long getChecksum() {
        if(checksumObj != null) {
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
        if(checksumObj != null) {
            checksumObj.apply(bytes);
        }
    }
}
