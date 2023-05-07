import org.json.JSONObject;
import org.junit.*;
import com.linzesu.javacan.utils.DBCParser;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.linzesu.javacan.utils.MessageParser.processCanMessageToJSON;
import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

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
                .get(1L).getSignal("Dog").getEndianness(), false);
    }

    @Test
    public void testLittleEndianMessageSpanMultipleByte1(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        byte[] byteMessage = new byte[]{0x01, 0x20, 0x00, 0x03, 0x40, 0x00, 0x05, 0x00};
        boolean isExtended = false;

        // The message contained in the bytearray.
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("Dog",1);
        expectedResult.put("Cat",2);
        expectedResult.put("Bird",3);
        expectedResult.put("Fish",4);
        expectedResult.put("Other",5);

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParser.getMessageDefinitions(),byteId, byteMessage,
                isExtended, null);
        assertEquals(expectedResult, CanMessageInJson, true);

    }

    @Test
    public void testLittleEndianMessageSpanMultipleByte2(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02};
        byte[] byteMessage = new byte[]{0x10, 0x00, 0x21, 0x00, 0x32, 0x00, 0x43, 0x00};
        boolean isExtended = true;

        // The message contained in the bytearray.
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("Dog",4097);
        expectedResult.put("Cat",8194);
        expectedResult.put("Bird",12291);
        expectedResult.put("Fish",4);

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId, byteMessage
                , isExtended, null);
        assertEquals(expectedResult, CanMessageInJson, true);

    }

    @Test
    public void testBigEndianMessage1(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03};
        byte[] byteMessage = new byte[]{0x00, 0x10, 0x02, 0x00, 0x30, 0x04, 0x00, 0x05};
        boolean isExtended = true;

        // The message contained in the bytearray.
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("Dog",1);
        expectedResult.put("Cat",2);
        expectedResult.put("Bird",3);
        expectedResult.put("Fish",4);
        expectedResult.put("Other",5);

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId, byteMessage
                , isExtended, null);
        assertEquals(expectedResult, CanMessageInJson, true);

    }

    @Test
    public void testBigEndianMessage2(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04};
        byte[] byteMessage = new byte[]{0x01, 0x00, 0x22, 0x00, 0x33, 0x00, 0x40, 0x05};
        boolean isExtended = true;

        // The message contained in the bytearray.
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("Dog",4098);
        expectedResult.put("Cat",8195);
        expectedResult.put("Bird",12292);
        expectedResult.put("Fish",5);

        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId,
                byteMessage, isExtended, null);
        assertEquals(expectedResult, CanMessageInJson, true);

    }

    @Test
    public void testGetSpecificSignals(){

        byte[] byteId = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04};
        byte[] byteMessage = new byte[]{0x01, 0x00, 0x22, 0x00, 0x33, 0x00, 0x40, 0x05};
        boolean isExtended = true;

        // The message contained in the bytearray.
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("Dog",4098);
        expectedResult.put("Cat",8195);

        ArrayList<String> signalName = new ArrayList<>();
        signalName.add("Dog");
        signalName.add("Cat");
        JSONObject CanMessageInJson = processCanMessageToJSON(dbcParserCanExtended.getMessageDefinitions(),byteId,
                byteMessage, isExtended, signalName);
        assertEquals(expectedResult, CanMessageInJson, true);

    }
}
