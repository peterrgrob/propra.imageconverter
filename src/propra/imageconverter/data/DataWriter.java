package propra.imageconverter.data;

import propra.imageconverter.checksum.Checksum;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.data.DataFormat.IOMode;

/**
 *
 * @author pg
 */
public class DataWriter implements Closeable {
    
    protected final IOMode mode;
    protected RandomAccessFile binaryWriter;
    protected BufferedWriter txtWriter;
    protected Checksum checksumObj; 
    
    /**
     *
     * @param file
     * @param mode
     * @throws IOException
     */
    public DataWriter(String file, IOMode mode) throws IOException {
        this.mode = mode;
        if(mode == IOMode.BINARY) {
            binaryWriter = createBinaryWriter(file);
        } else {
            txtWriter = createTextWriter(file);
        }
    }
    
    /**
     *
     * @throws java.io.IOException
     */
    public void begin() throws IOException {
    }
    
    /**
     *
     */
    public void end() {
    }
    
    /**
     * 
     * @param buffer
     * @return
     * @throws IOException 
     */
    public int write(ByteBuffer buffer) throws IOException {
        return write(buffer, 0, buffer.limit());
    }
    
    /**
     *
     * @param buffer
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    public int write(ByteBuffer buffer, int offset, int len) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        // Binär oder als Text in Ausgabedatei schreiben
        if(mode == IOMode.BINARY) {
            binaryWriter.write( buffer.array(), 
                                offset, 
                                len);
        } else {
            writeBinaryToTextFile(buffer);
        }
        
        return buffer.limit();
    }
    
    /**
     *
     * @return
     */
    public boolean isValid() {
        if(mode == IOMode.BINARY) {
            return binaryWriter != null;
        } else {
            return txtWriter != null;
        }
    }
    
        
    /**
     *
     * @return
     */
    public IOMode getMode() {
        return mode;
    }
    
        /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(binaryWriter != null) {
            binaryWriter.close();
        }
        if(txtWriter != null) {
            txtWriter.close();
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
     * @param buffer
     * @throws IOException
     */
    protected void writeBinaryToTextFile(ByteBuffer buffer) throws IOException {
        if(buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Bytes iterieren und als Zeichen in Datei schreiben
        while(buffer.hasRemaining()) {
            txtWriter.write((byte)buffer.get());
        }
        
        // Schreiben erzwingen
        txtWriter.flush();
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private static BufferedWriter createTextWriter(String filePath) throws IOException {
        File fileObj = createFileAndDirectory(filePath);
        return new BufferedWriter(new FileWriter(fileObj)); 
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private static RandomAccessFile createBinaryWriter(String filePath) throws IOException {
        File fileObj = createFileAndDirectory(filePath);
        return new RandomAccessFile(fileObj, "rw");
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private static File createFileAndDirectory(String filePath) throws IOException {
        
        // Verzeichnisse erstellen, falls nötig
        Path outDirs = Paths.get(filePath);
        Files.createDirectories(outDirs.getParent());
        
        // Ausgabedatei erstellen
        File file = new File(filePath);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        return file;
    }
}
