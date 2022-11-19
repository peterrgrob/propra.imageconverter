package propra.imageconverter.data;

import propra.imageconverter.checksum.Checksum;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import propra.imageconverter.data.DataFormat.IOMode;

/**
 *
 * @author pg
 */
public class DataReader implements Closeable {
    
    private final IOMode ioMode;
    protected RandomAccessFile binaryReader;
    protected BufferedReader txtReader;
    protected Checksum checksumObj; 
    
    /**
     * 
     * @param file
     * @param mode
     * @throws IOException 
     */
    public DataReader(String file, IOMode mode) throws IOException {
        this.ioMode = mode;
        
        // Prüfe auf existenz
        if(!fileExists(file)) {
            throw new FileNotFoundException("Datei nicht gefunden!");
        }
        
        if(mode == IOMode.BINARY) {
            binaryReader = createBinaryReader(file);
        } else {
            txtReader = createTextReader(file);
        }
    }
    
    /**
     * 
     * @return 
     */
    public IOMode getMode() {
        return ioMode;
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
    public int read(ByteBuffer buffer) throws IOException {
        return read(buffer, 0, buffer.limit());
    }
    
    /**
     * 
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException 
     */
    public int read(ByteBuffer buffer, int offset, int len) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(ioMode == IOMode.BINARY) {
            binaryReader.read( buffer.array(), 
                                offset, 
                                len);
            buffer.limit(len);
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
        if(ioMode == IOMode.BINARY) {
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
        if(ioMode == IOMode.BINARY) {
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
        if(ioMode == ioMode.BINARY) {
            return (binaryReader.length() < binaryReader.getFilePointer());
        } 
        return false;
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
    public boolean isValid() {
        if(ioMode == ioMode.BINARY) {
            return binaryReader != null;
        } else {
            return txtReader != null;
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
    protected void updateChecksum(ByteBuffer bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        if(checksumObj != null) {
            checksumObj.apply(bytes);
        }
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private static BufferedReader createTextReader(String filePath) throws IOException {
        return new BufferedReader(new FileReader(filePath)); 
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private static RandomAccessFile createBinaryReader(String filePath) throws IOException {
        return new RandomAccessFile(filePath, "rw");
    }
    
    /**
     * 
     * @param file
     * @return 
     */
    private static boolean fileExists(String file) {
        File f = new File(file);
        if(!f.exists()) {
            return false;
        }
        return true;
    }
}
