package propra.imageconverter.util;

/**
 *
 * @author pg
 */
public class ICException extends Exception {
    
    /**
     *
     * @param arg
     */
    public static void checkArg(boolean arg) {
        if(!arg) {
            throw new IllegalArgumentException();
        }
    }
}
