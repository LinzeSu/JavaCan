import org.json.JSONObject;
import org.junit.*;
import com.linzesu.javacan.utils.DBCParser;

import java.io.IOException;

import static com.linzesu.javacan.utils.MessageParser.hexStringToByteArray;
import static com.linzesu.javacan.utils.MessageParser.processCanMessageToJSON;
import static org.junit.Assert.assertEquals;

public class JavaCanTest {

    private static DBCParser dbcParserCanExtended;
    private static DBCParser dbcParser;

    @BeforeClass
    public static void beforeClass() throws IOException {
        dbcParser = new DBCParser("src/test/resources/example-can.dbc");
        dbcParserCanExtended = new DBCParser("src/test/resources/example-can-extended.dbc");
    }

    @Test
    public void getSignalAttributes(){
        // Notice that the message id has to be type long
        assertEquals(dbcParser.getMessageDefinitions()
                .get(1l).getSignal("Dog").getEndianness(), false);
    }

    @Test
    public void testLittleEndianMessageSpanMultipleByte(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        byte[] byteMessage = new byte[]{0x01, 0x20, 0x00, 0x03, 0x40, 0x00, 0x05, 0x00};
        boolean isExtended = false;

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParser.getMessageDefinitions(),byteId, byteMessage,
                isExtended);
        System.out.println(CanMessageInJson);

    }

    @Test
    public void testLittleEndianMessageSpanMultipleByte2(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02};
        byte[] byteMessage = new byte[]{0x10, 0x00, 0x21, 0x00, 0x32, 0x00, 0x43, 0x00};
        boolean isExtended = true;

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId, byteMessage
                , isExtended);
        System.out.println(CanMessageInJson);

    }

    @Test
    public void testBigEndianMessage1(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03};
        byte[] byteMessage = new byte[]{0x00, 0x10, 0x02, 0x00, 0x30, 0x04, 0x00, 0x05};
        boolean isExtended = true;

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId, byteMessage
                , isExtended);
        System.out.println(CanMessageInJson);

    }

    @Test
    public void testBigEndianMessage2(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04};
        byte[] byteMessage = new byte[]{0x01, 0x00, 0x22, 0x00, 0x33, 0x00, 0x40, 0x05};
        boolean isExtended = true;

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId, byteMessage, isExtended);
        System.out.println(CanMessageInJson);

    }

}
