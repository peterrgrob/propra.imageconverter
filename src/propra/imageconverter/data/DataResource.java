package propra.imageconverter.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.checksum.Checksum;

/**
 *
 * @author pg
 */
public class DataResource implements IDataResource {
    
    protected final DataFormat.IOMode mode;
    protected RandomAccessFile binaryFile;
    protected BufferedWriter txtWriter;
    protected BufferedReader txtReader;   

    /**
     * 
     * @param file
     * @param mode 
     * @throws java.io.IOException 
     */
    public DataResource(String file, DataFormat.IOMode mode) throws IOException {
        this.mode = mode;
        if(mode == DataFormat.IOMode.BINARY) {
            binaryFile = createBinaryFile(file);
        } else {
            txtWriter = createTextWriter(file);
        }
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
    private static RandomAccessFile createBinaryFile(String filePath) throws IOException {
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

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(binaryFile != null) {
            binaryFile.close();
        }
        if(txtWriter != null) {
            txtWriter.close();
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
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile != null;
        }
        return true;
    }
    
    /**
     * 
     * @param checksum
     * @return 
     */
    @Override
    public boolean checkChecksum(Checksum checksum) {
        return true;
    }
    
    /**
     * 
     */
    protected void checkState() {
        if(!isValid()) {
            throw new IllegalArgumentException("Ungültiger Zustand!");
        }
    }
    
    
    @Override
    public void position(long pos) throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            binaryFile.seek(pos);
        }
    }

    @Override
    public long position() throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile.getFilePointer();
        }
        return 0;
    }

    @Override
    public long length() throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile.length();
        }
        return 0;
    }

    @Override
    public void read(ByteBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == DataFormat.IOMode.BINARY) {
            int len = binaryFile.read(  buffer.array(), 
                                        buffer.position(), 
                                        buffer.capacity());
            buffer.limit(len);
        } 
    }

    @Override
    public void read(long offset, ByteBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == DataFormat.IOMode.BINARY) {
            long p = binaryFile.getFilePointer();
            binaryFile.seek(offset);
            
            int len = binaryFile.read(  buffer.array(), 
                                        buffer.position(), 
                                        buffer.capacity());
            
            buffer.limit(len);
            binaryFile.seek(p);
        } 
    }

    @Override
    public ByteBuffer read(long offset, int length) throws IOException {
        ByteBuffer nb = ByteBuffer.allocate(length);
        read(offset, nb);
        return nb;
    }
    
        /**
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public String readLine() throws IOException {
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile.readLine();
        } else {
            return txtReader.readLine();
        }
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == DataFormat.IOMode.BINARY) {
            
            binaryFile.write(buffer.array(), 
                            buffer.position(), 
                            buffer.limit());
        } 
    }

    @Override
    public void write(long offset, ByteBuffer buffer) throws IOException {
        if(!isValid()
        ||  buffer == null) {
            throw new IllegalStateException();
        }
        
        if(mode == DataFormat.IOMode.BINARY) {
            long p = binaryFile.getFilePointer();
            binaryFile.seek(offset);
            
            binaryFile.write(buffer.array(), 
                            buffer.position(), 
                            buffer.limit());
            
            binaryFile.seek(p);
        } 
    }
    
    /**
     *
     * @param buffer
     * @throws IOException
     */
    /*protected void writeBinaryToTextFile(ByteBuffer buffer) throws IOException {
        if(buffer == null) {
            throw new IllegalArgumentException();
        }
        
        // Bytes iterieren und als Zeichen in Datei schreiben
        while(buffer.hasRemaining()) {
            txtWriter.write((byte)buffer.get());
        }
        
        // Schreiben erzwingen
        txtWriter.flush();
    }*/
}
