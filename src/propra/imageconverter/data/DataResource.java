package propra.imageconverter.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import propra.imageconverter.util.CheckedOutputStream;
import propra.imageconverter.util.CheckedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import propra.imageconverter.util.Checksum;

/**
 *
 */
public class DataResource implements IDataResource {
    
    // Dateiresource
    protected RandomAccessFile binaryFile;
    
    // Zugeordnete Streams
    protected CheckedOutputStream outStream;
    protected CheckedInputStream inStream; 
    
    // Zugeordneter Codec zum lesen/schreiben der Daten
    protected IDataCodec inCodec;
    
    // Prüfsumme 
    protected Checksum checksum;
    
    /**
     * 
     */
    public DataResource(String file,
                        boolean write) throws IOException {
        File fileObj;
        if(write) {
            fileObj = createFileAndDirectory(file); 
        } else {
            fileObj = new File(file);
        }
        
        binaryFile = new RandomAccessFile(fileObj, "r" + (write ? "w":""));
        inStream = new CheckedInputStream(new BufferedInputStream(Channels.newInputStream(binaryFile.getChannel())));
        outStream = new CheckedOutputStream(new BufferedOutputStream(Channels.newOutputStream(binaryFile.getChannel())));
    }

    /**
     *
     */
    @Override
    public void close() throws IOException {
        if(outStream != null) {
            outStream.close();
        }
        if(inStream != null) {
            inStream.close();
        }
        if(binaryFile != null) {
            binaryFile.close();
        }
    }
    
    /**
     *
     */
    public boolean isValid() {
        return binaryFile != null;
    }

    /**
     * 
     */
    @Override
    public long length() throws IOException {
        checkState();
        return binaryFile.length();
    }
    
    /**
     * 
     */
    @Override
    public long position() throws IOException {
        checkState();
        return binaryFile.getFilePointer();        
    }
    
    /**
     * 
     */
    @Override
    public void position(long p) throws IOException {
        checkState();
        binaryFile.seek(p);        
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
    protected void checkState() {
        if(!isValid()) {
            throw new IllegalArgumentException("Ungültige Resource!");
        }
    }
    
    /**
     * Verzeichnisse und Datei erstellen, falls nötig
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
}
