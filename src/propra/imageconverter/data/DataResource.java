package propra.imageconverter.data;

import propra.imageconverter.util.CheckedInputStream;
import propra.imageconverter.util.CheckedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import propra.imageconverter.data.IDataTranscoder.Compression;


/**
 * Klasse bietet Basisfunktionen für Dateiressourcen 
 */
public class DataResource implements IDataResource {
    
    // Aktuelle Kodierung
    protected Compression compression = Compression.UNCOMPRESSED;
    
    // Dateiresource
    protected RandomAccessFile binaryFile;
    
    // Zugeordnete I/O Streams
    protected CheckedOutputStream outStream;
    protected CheckedInputStream inStream; 
    
    /**
     * Konstruktor, öffnet Streams und erstellt Dateien/Verzeichnis
     */
    public DataResource(String file, boolean write) throws IOException {
        
        File fileObj = write ? DataUtil.createFileAndDirectory(file) : new File(file); 
 
        binaryFile = new RandomAccessFile(fileObj, "r" + (write ? "w":""));
        inStream = new CheckedInputStream(
                   new BufferedInputStream(Channels.newInputStream(binaryFile.getChannel())));
        outStream = new CheckedOutputStream(
                    new BufferedOutputStream(Channels.newOutputStream(binaryFile.getChannel())));
        
        if(binaryFile == null || inStream == null || outStream == null) {
            throw new IOException("Fehler beim öffnen der Datei!");
        }
    }

    /**
     * Gibt Länge der Ressource zurück
     */
    @Override
    public long length() throws IOException {
        return binaryFile.length();
    }
    
    /**
     * Gibt aktuelle Position in der Ressource zurück
     */
    @Override
    public long position() throws IOException {
        return binaryFile.getFilePointer();        
    }
    
    /**
     * Setzt aktuelle Position
     */
    @Override
    public void position(long p) throws IOException {
        binaryFile.seek(p);        
    } 
        
    /**
     *  Gibt Streams der Ressource zurück
     */
    @Override
    public CheckedInputStream getInputStream() {
        return inStream;
    }
    
    @Override
    public CheckedOutputStream getOutputStream() {
        return outStream;     
    }
     
    /**
     * Schließt Streams und Filehandle
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
