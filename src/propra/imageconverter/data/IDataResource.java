package propra.imageconverter.data;

import java.io.IOException;

/**
 * Interface für Ressourcen
 */
public interface IDataResource extends AutoCloseable {
    
    /**
     * Länge der Ressource
     */
    public long length() throws IOException;
    
    /**
     * Aktuelle Position des Streams in der Ressource
     */
    public long position() throws IOException;
    
    /**
     * Setzt Stream auf neue Position
     */
    public void position(long p) throws IOException;
    
    /**
     *  Gibt Stream der Ressource zurück
     */
    public CheckedInputStream getInputStream();
    
    /**
     *  Gibt Stream der Ressource zurück
     */
    public CheckedOutputStream getOutputStream();   
}
