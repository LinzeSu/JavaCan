import utils.CanToJSONProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static utils.CanToJSONProcessor.hexStringToByteArray;


// TODO: ADD LOGGER
// TODO: consider some performance issue
public class JavaCan {

    public static void main(String[] args) throws IOException {

        CanToJSONProcessor c2j = new CanToJSONProcessor("src/main/resources/V2.0.1_HAV0.2_PTCAN_20230110.dbc");

        byte[] byteMessage = hexStringToByteArray("3d00f00cf00cf10c");
        byte[] byteId = hexStringToByteArray("18c1eff3");
        System.out.println(bytesToHex(byteId));
        boolean isExtended = true;

        String CanMessageInJson = c2j.processCanMessageToJSON(byteId, byteMessage, isExtended);
        System.out.println(CanMessageInJson);

    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

}
