package propra.imageconverter.data;

/**
 *
 * @author pg
 */
public interface DataFilter {
    
    public void begin();
    
    public DataBuffer apply(DataBuffer inOut);
    
    public DataBuffer apply(DataBuffer in, DataBuffer out);
    
    public void end();
}
