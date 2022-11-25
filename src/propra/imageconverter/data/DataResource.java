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
    public DataResource(String file, 
                        DataFormat.IOMode mode) throws IOException {
        this.mode = mode;
        
        File fileObj = createFileAndDirectory(file); 
        if(mode == DataFormat.IOMode.BINARY) {
            binaryFile = new RandomAccessFile(fileObj, "rw");
        } else {
            txtWriter = new BufferedWriter(new FileWriter(fileObj));
        }
    }
    
    /**
     * Verzeichnisse und Dateil erstellen, falls nötig
     * @param filePath
     * @return
     * @throws IOException 
     */
    private static File createFileAndDirectory(String filePath) throws IOException {
     
        Path outDirs = Paths.get(filePath);
        Files.createDirectories(outDirs.getParent());
        
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
     * @param pos
     * @throws IOException 
     */
    @Override
    public void position(long pos) throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            binaryFile.seek(pos);
        }
    }

    /**
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public long position() throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile.getFilePointer();
        }
        return 0;
    }

    /**
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public long length() throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile.length();
        }
        return 0;
    }

    /**
     * 
     * @param buffer
     * @return 
     * @throws IOException 
     */
    @Override
    public int read(ByteBuffer buffer) throws IOException {     
        checkState();
        if(mode == DataFormat.IOMode.BINARY
        && buffer != null) {
            int len = binaryFile.read(  buffer.array(), 
                                        buffer.position(), 
                                        buffer.capacity());
            buffer.limit(len);
            return len;
        } 
        return 0;
    }
    
    /**
     * 
     * @param offset
     * @param buffer
     * @return 
     * @throws IOException 
     */
    @Override
    public int read(long offset, ByteBuffer buffer) throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY
        && buffer != null) {
            long p = binaryFile.getFilePointer();
            binaryFile.seek(offset);
            
            int len = binaryFile.read(  buffer.array(), 
                                        buffer.position(), 
                                        buffer.capacity());
            
            buffer.limit(len);
            binaryFile.seek(p);
            return len;
        } 
        return 0;
    }

    /**
     * 
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     */
    @Override
    public ByteBuffer read(long offset, int length) throws IOException {
        checkState();
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
        checkState();
        if(mode == DataFormat.IOMode.BINARY) {
            return binaryFile.readLine();
        } else {
            return txtReader.readLine();
        }
    }

    /**
     * 
     * @param buffer
     * @throws IOException 
     */
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY
        && buffer != null) {
            binaryFile.write(buffer.array(), 
                            buffer.position(), 
                            buffer.limit());
        } 
    }

    /**
     * 
     * @param offset
     * @param buffer
     * @throws IOException 
     */
    @Override
    public void write(long offset, ByteBuffer buffer) throws IOException {
        checkState();
        if(mode == DataFormat.IOMode.BINARY
        && buffer != null) {
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
    
    /**
     * 
     */
    protected void checkState() {
        if(!isValid()) {
            throw new IllegalArgumentException("Ungültige Resource!");
        }
    }
}
