package propra.imageconverter.image;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 *
 * @author pg
 */
public class ImageContentWriter extends BufferedInputStream {

    public ImageContentWriter(InputStream in) {
        super(in);
    }
    
}
