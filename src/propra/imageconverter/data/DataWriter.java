package propra.imageconverter.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class DataWriter implements Validatable {
    
    protected final Mode mode = Mode.BINARY;
    protected RandomAccessFile binaryWriter;
    protected BufferedWriter txtWriter;
    
    public enum Mode {
        BINARY,
        TEXT;
    }
    
    public DataWriter(String file, Mode mode) throws IOException {
        if(mode == Mode.BINARY) {
            binaryWriter = createBinaryWriter(file);
        } else {
            txtWriter = createTextWriter(file);
        }
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void begin() {
    }
    
    public void end() {
    }
    
    public int write(DataBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == Mode.BINARY) {
            binaryWriter.write( buffer.getBytes(), 
                                0, 
                                buffer.getCurrDataLength());
        } else {
            // Zeichen in Datei schreiben
            writeBinaryToTextFile(buffer);
        }
        
        return buffer.getCurrDataLength();
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    protected void writeBinaryToTextFile(DataBuffer buffer) throws IOException {
        if(buffer == null) {
            throw new IllegalArgumentException();
        }
        
        while(buffer.getBuffer().hasRemaining()) {
            txtWriter.write((byte)buffer.getBuffer().get());
        }
        txtWriter.flush();
    }
    
    private BufferedWriter createTextWriter(String filePath) throws IOException {
        File fileObj = createFileAndDirectory(filePath);
        return new BufferedWriter(new FileWriter(fileObj)); 
    }
    
    private RandomAccessFile createBinaryWriter(String filePath) throws IOException {
        File fileObj = createFileAndDirectory(filePath);
        return new RandomAccessFile(fileObj, "rw");
    }
    
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
}
