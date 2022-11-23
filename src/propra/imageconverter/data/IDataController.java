package propra.imageconverter.data;

/**
 *
 * @author pg
 */
public interface IDataController {
    public void setup(IDataCodec input, IDataCodec output);
    public void process();
}
