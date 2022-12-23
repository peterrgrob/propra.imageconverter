package propra.imageconverter.data;

import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * 
 * @author pg
 */
public class DataResource implements IDataResource {
    
    // Dateiresource
    protected RandomAccessFile binaryFile;
    
    // Zugeordnete Streams
    protected CheckedOutputStream outStream;
    protected CheckedInputStream inStream; 
    
    // Zugeordneter Codec zum lesen/schreiben der Daten
    protected IDataCodec inCodec;
    
    /**
     * 
     * @param file
     * @param write
     * @throws java.io.IOException
     */
    public DataResource(String file, boolean write) throws IOException {
        File fileObj;
        if(write) {
            fileObj = createFileAndDirectory(file); 
        } else {
            fileObj = new File(file);
        }
        
        binaryFile = new RandomAccessFile(fileObj, "r" + (write ? "w":""));
        inStream = new CheckedInputStream(
                   new BufferedInputStream(Channels.newInputStream(binaryFile.getChannel())));
        outStream = new CheckedOutputStream(
                    new BufferedOutputStream(Channels.newOutputStream(binaryFile.getChannel())));
    }

    /**
     *
     * @throws java.io.IOException
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
     * @return  
     */
    public boolean isValid() {
        return binaryFile != null;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    @Override
    public long length() throws IOException {
        checkState();
        return binaryFile.length();
    }
    
    /**
     * 
     * @return
     * @throws IOException
     */
    @Override
    public long position() throws IOException {
        checkState();
        return binaryFile.getFilePointer();        
    }
    
    /**
     * 
     * @param p
     * @throws IOException
     */
    @Override
    public void position(long p) throws IOException {
        checkState();
        binaryFile.seek(p);        
    } 
        
    /**
     *  
     * @return 
     */
    @Override
    public CheckedInputStream getInputStream() {
        checkState();
        return inStream;
    }
    
    /**
     *  
     * @return 
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
     * @param filePath
     * @return 
     * @throws java.io.IOException
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
