package propra.imageconverter.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import propra.imageconverter.util.Validatable;

/**
 *
 * @author pg
 */
public class DataReader implements Validatable {
    
    private final Mode mode;
    protected RandomAccessFile binaryReader;
    protected BufferedReader txtReader;
    
    public enum Mode {
        BINARY,
        TEXT;
    }
    
    public DataReader(String file, Mode mode) throws IOException {
        this.mode = mode;
        if(mode == Mode.BINARY) {
            binaryReader = createBinaryReader(file);
        } else {
            txtReader = createTextReader(file);
        }
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void begin() {
    }
    
    public void end() {
    }
    
    public int read(DataBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == Mode.BINARY) {
            binaryReader.read( buffer.getBytes(), 
                                0, 
                                buffer.getCurrDataLength());
        } else {
            throw new UnsupportedOperationException("");
        }
        
        return buffer.getCurrDataLength();
    }
    
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
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    private BufferedReader createTextReader(String filePath) throws IOException {
        return new BufferedReader(new FileReader(filePath)); 
    }
    
    private RandomAccessFile createBinaryReader(String filePath) throws IOException {
        return new RandomAccessFile(filePath, "rw");
    }
}
