import org.junit.*;
import utils.CanToJSONProcessor;

import java.io.IOException;

import static utils.CanToJSONProcessor.hexStringToByteArray;

public class JavaCanTest {

    private static CanToJSONProcessor c2j;

    @BeforeClass
    public static void beforeClass() throws IOException {
        c2j = new CanToJSONProcessor("src/main/resources/example-can-extended.dbc");
    }

    @Test
    public void testLittleEndianMessage(){

        byte[] byteId = hexStringToByteArray("00000001");
        byte[] byteMessage = hexStringToByteArray("1002004008");
        boolean isExtended = true;

        String CanMessageInJson = c2j.processCanMessageToJSON(byteId, byteMessage, isExtended);
        System.out.println(CanMessageInJson);

    }

    @Test
    public void testLittleEndianMessageSpanMultipleByte(){

        byte[] byteId = hexStringToByteArray("80BC614E");
        byte[] byteMessage = hexStringToByteArray("1002004008");
        boolean isExtended = true;

        String CanMessageInJson = c2j.processCanMessageToJSON(byteId, byteMessage, isExtended);
        System.out.println(CanMessageInJson);

    }

    @Test
    public void testBigEndianMessage(){

        byte[] byteMessage = new byte[]{0x01, 0x02, 0x00, 0x00, 0x34, 0x00, 0x00, 0x00};
        // 2565887440
        byte[] byteId = hexStringToByteArray("00000001");
        boolean isExtended = true;

        String CanMessageInJson = c2j.processCanMessageToJSON(byteId, byteMessage, isExtended);
        System.out.println(CanMessageInJson);

    }

}
