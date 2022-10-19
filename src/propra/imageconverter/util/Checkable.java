package propra.imageconverter.util;

/**
 * Interface für Klassen die Prüfsummenberechnung anbieten
 * 
 * @author pg
 */
public interface Checkable {

    /**
     *
     * @return
     */
    public boolean isCheckable();
    
    /**
     *
     * @return
     */
    public long getChecksum();
    
    /**
     *
     * @return
     */
    public Checksum getChecksumObj();
}
