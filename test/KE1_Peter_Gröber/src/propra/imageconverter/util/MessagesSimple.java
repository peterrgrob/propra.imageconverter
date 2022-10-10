package propra.imageconverter.util;

import java.util.HashMap;

/**
 *
 * @author pg
 */
public class MessagesSimple implements Messages {
    protected HashMap<MessageID,String> msgMap = new HashMap();
    
    /**
     * 
     */
    public MessagesSimple() {
        msgMap.put(MessageID.MID_OK, "Ok");
    }
            
    /**
     * 
     * @param mID
     * @return 
     */
    @Override
    public String getMessage(MessageID mID) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
