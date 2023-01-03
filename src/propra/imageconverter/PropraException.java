package propra.imageconverter;

/**
 *  Propra spezifische Exception und allgemeine Fehlerbehandlung
 */
public class PropraException extends RuntimeException {
        
    // Fehlercode
    private static final int ERROR_EXIT_CODE = 123;
    
    public PropraException(String msg) {
        super(msg);
    }
    
    /**
     * Schreibt msg in System.err und beendet das Programm mit EXIT_CODE
     */
    public static void printErrorAndQuit(String msg, Exception e) {
        String s = msg;
        if(e != null) {
            if(e.getMessage() != null) {
                s = s.concat(e.getMessage());
            }
        }
        System.err.println(s);
        System.exit(ERROR_EXIT_CODE);
    }
    
    /**
     * Wirft eine Exception Wenn obj == null ist 
     */
    public static void assertArgument(Object obj) {
        if(obj == null) {
            throw new IllegalArgumentException("Argument ist null!");
        }    
    }
    
    /**
     * Gibt msg in System.out aus
     */
    public static void printMessage(String msg) {
        System.out.println(msg); 
    }
}
