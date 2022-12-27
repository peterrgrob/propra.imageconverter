package propra.imageconverter.data;

import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;


/**
 * Klasse bietet Basisfunktionen für Dateiressourcen 
 * @author pg
 */
public class DataResource implements IDataResource {
    
    // Kodierungstypen der Daten
    public enum Compression {
        NONE,
        RLE,
        BASEN,
        HUFFMAN;
    }
    
    // Aktuelle Kodierung
    protected Compression compression = Compression.NONE;
    
    // Dateiresource
    protected RandomAccessFile binaryFile;
    
    // Zugeordnete I/O Streams
    protected CheckedOutputStream outStream;
    protected CheckedInputStream inStream; 
    
    // Zugeordneter Codec zum lesen/schreiben der Daten
    protected IDataTranscoder inCodec;
    
    /**
     * 
     * @param file
     * @param write
     * @throws java.io.IOException
     */
    public DataResource(String file, boolean write) throws IOException {
        File fileObj;
        if(write) {
            fileObj = DataUtil.createFileAndDirectory(file); 
        } else {
            fileObj = new File(file);
        }
        
        binaryFile = new RandomAccessFile(fileObj, "r" + (write ? "w":""));
        inStream = new CheckedInputStream(
                   new BufferedInputStream(Channels.newInputStream(binaryFile.getChannel())));
        outStream = new CheckedOutputStream(
                    new BufferedOutputStream(Channels.newOutputStream(binaryFile.getChannel())));
        
        if(binaryFile == null 
        || inStream == null
        || outStream == null) {
            throw new IOException("Fehler beim öffnen der Datei!");
        }
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    @Override
    public long length() throws IOException {
        return binaryFile.length();
    }
    
    /**
     * 
     * @return
     * @throws IOException
     */
    @Override
    public long position() throws IOException {
        return binaryFile.getFilePointer();        
    }
    
    /**
     * 
     * @param p
     * @throws IOException
     */
    @Override
    public void position(long p) throws IOException {
        binaryFile.seek(p);        
    } 
        
    /**
     *  
     * @return 
     */
    @Override
    public CheckedInputStream getInputStream() {
        return inStream;
    }
    
    /**
     *  
     * @return 
     */
    @Override
    public CheckedOutputStream getOutputStream() {
        return outStream;       
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
}
