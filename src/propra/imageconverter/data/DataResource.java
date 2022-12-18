package propra.imageconverter.data;

import propra.imageconverter.util.CheckedOutputStream;
import propra.imageconverter.util.CheckedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.util.Checksum;

/**
 *
 * @author pg
 */
public class DataResource implements IDataResource,
                                     AutoCloseable {
    
    protected final DataFormat.IOMode mode;
    protected RandomAccessFile binaryFile;
    protected CheckedOutputStream outStream;
    protected CheckedInputStream inStream;
    protected BufferedWriter txtWriter;
    protected BufferedReader txtReader;   
    
    // Zugeordneter Codec zum lesen/schreiben der Daten
    protected IDataCodec inCodec;
    
    // Prüfsumme 
    protected Checksum checksum;
    
    /**
     * 
     */
    public DataResource(String file, 
                        DataFormat.IOMode mode,
                        boolean write) throws IOException {
        this.mode = mode;
        File fileObj;
                
        if(write) {
            fileObj = createFileAndDirectory(file); 
        } else {
            fileObj = new File(file);
        }
        
        if(mode == DataFormat.IOMode.BINARY) {
            binaryFile = new RandomAccessFile(fileObj, "r" + (write ? "w":""));
            inStream = new CheckedInputStream(Channels.newInputStream(binaryFile.getChannel()));
            outStream = new CheckedOutputStream(Channels.newOutputStream(binaryFile.getChannel()));
        } else {
            txtWriter = new BufferedWriter(new FileWriter(fileObj));
        }
    }
    
    /**
     * Verzeichnisse und Dateil erstellen, falls nötig
     */
    public static File createFileAndDirectory(String filePath) throws IOException {
     
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
     */
    @Override
    public void close() throws IOException {
        if(outStream != null) {
            outStream.close();
        }
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
     */
    @Override
    public CheckedInputStream getInputStream() {
        checkState();
        return inStream;
    }
    
    /**
     *  
     */
    @Override
    public CheckedOutputStream getOutputStream() {
        checkState();
        return outStream;       
    }

    /**
     * 
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
