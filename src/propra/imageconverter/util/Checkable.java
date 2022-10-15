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
    public Checksum getChecksumObj();
    
    /**
     *
     * @param bytes
     * @return
     */
    public long check(byte[] bytes);
}
