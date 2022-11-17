package propra.imageconverter.data;

import propra.imageconverter.checksum.Checksum;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.data.DataFormat.Mode;

/**
 *
 * @author pg
 */
public class DataWriter implements Closeable {
    
    protected final Mode mode;
    protected RandomAccessFile binaryWriter;
    protected BufferedWriter txtWriter;
    protected Checksum checksumObj; 
    
    /**
     *
     * @param file
     * @param mode
     * @throws IOException
     */
    public DataWriter(String file, Mode mode) throws IOException {
        this.mode = mode;
        if(mode == Mode.BINARY) {
            binaryWriter = createBinaryWriter(file);
        } else {
            txtWriter = createTextWriter(file);
        }
    }
    
    /**
     *
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
    public int write(DataBuffer buffer) throws IOException {
        return write(buffer, 0, buffer.getDataLength());
    }
    
    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public int write(DataBuffer buffer, int offset, int len) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        // Binär oder als Text in Ausgabedatei schreiben
        if(mode == Mode.BINARY) {
            binaryWriter.write( buffer.getBytes(), 
                                offset, 
                                len);
        } else {
            writeBinaryToTextFile(buffer);
        }
        
        return buffer.getDataLength();
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
     * @return
     */
    public Mode getMode() {
        return mode;
    }
    
    /**
     *
     * @param buffer
     * @throws IOException
     */
    protected void writeBinaryToTextFile(DataBuffer buffer) throws IOException {
        if(buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Bytes iterieren und als Zeichen in Datei schreiben
        while(buffer.getBuffer().hasRemaining()) {
            txtWriter.write((byte)buffer.getBuffer().get());
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
    private BufferedWriter createTextWriter(String filePath) throws IOException {
        File fileObj = createFileAndDirectory(filePath);
        return new BufferedWriter(new FileWriter(fileObj)); 
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private RandomAccessFile createBinaryWriter(String filePath) throws IOException {
        File fileObj = createFileAndDirectory(filePath);
        return new RandomAccessFile(fileObj, "rw");
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    private File createFileAndDirectory(String filePath) throws IOException {
        
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
    protected void updateChecksum(DataBuffer bytes) {
        if (bytes == null ) {
            throw new IllegalArgumentException();
        }
        if(checksumObj != null) {
            checksumObj.apply(bytes);
        }
    }
}
