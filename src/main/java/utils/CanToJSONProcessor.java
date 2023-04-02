package utils;

import java.nio.file.Files;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dbc.MessageDefinition;
import dbc.SignalDefinition;
import org.json.JSONObject;

public class CanToJSONProcessor {

    private static Map<Long, MessageDefinition> messageDefinitions;

    public CanToJSONProcessor(String dbcFilePath) throws IOException {
        messageDefinitions = new HashMap<Long, MessageDefinition>();
        String dbcFileContent = new String(Files.readAllBytes(Paths.get(dbcFilePath)));
        String[] lines = dbcFileContent.split("\r?\n|\r");
        MessageDefinition currentMessage = null;
        long messageId = 0;
        for (String line : lines) {
            if (line.startsWith("BO_ ")) {
                String[] parts = line.split(" ");
                messageId = Long.parseLong(parts[1]);
                currentMessage = new MessageDefinition(messageId);
                messageDefinitions.put(messageId, currentMessage);
            } else if (line.startsWith(" SG_ ")) {
                // Split the line into the signal name and signal definition parts
                String[] nameAndDefinition = line.split(":");
                String signalName = nameAndDefinition[0].trim().substring(4); // remove " SG_ " prefix and trim whitespace
                String signalDefinition = nameAndDefinition[1].trim();

                // Extract signal definition components using a regular expression
                Pattern pattern = Pattern.compile("^(\\d+)\\|(\\d+)@(\\d+)([+-])\\s+\\(([^,]+),([^\\)]+)\\)\\s+\\[([^\\]]*)\\]\\s+\"([^\"]*)\"\\s+(.*)$");
                Matcher matcher = pattern.matcher(signalDefinition);
                if (matcher.matches()) {
                    int bitStart = Integer.parseInt(matcher.group(1));
                    int bitLength = Integer.parseInt(matcher.group(2));
                    boolean isBigEndian = matcher.group(3).equals("0");
                    boolean isUnsigned = matcher.group(4).equals("+");
                    double factor = Double.parseDouble(matcher.group(5));
                    double offset = Double.parseDouble(matcher.group(6));
                    String minMaxString = matcher.group(7);
                    String unit = matcher.group(8);
                    String receiver = matcher.group(9);
                    // Extract minimum and maximum values (if present)
                    String[] minMax = minMaxString.split("\\|");
                    double minValue = Double.parseDouble(minMax[0]);
                    double maxValue = Double.parseDouble(minMax[1]);

                    SignalDefinition signal = new SignalDefinition(messageId, signalName, bitStart, bitLength, factor,
                            offset, minValue, maxValue, unit, receiver, isBigEndian, isUnsigned);
                    currentMessage.addSignal(signal);
                } else {
                    // Handle invalid signal definition format
                    System.err.println("Invalid signal definition format: " + signalDefinition);
                }
            }
        }
    }

    public static String processCanMessageToJSON(byte[] canID, byte[] canMessage, boolean isExtended) {

        long messageId;

        // Check if the canID is 8 bytes long to make ByteBuffer.getLong to work.
        byte[] newArray = new byte[8];
        if(canID.length < 8){
            System.arraycopy(canID, 0, newArray, 8-canID.length, canID.length);
            canID = newArray;
        }

        if (isExtended) {
            System.out.println("Using CAN extended format");
            ByteBuffer idBuffer = ByteBuffer.wrap(canID);
            // Add 0x8000000
            messageId = idBuffer.getLong(0) + 2147483648L;
            System.out.println("the message id is:" + messageId);
        }else{
            ByteBuffer idBuffer = ByteBuffer.wrap(canID);
            messageId = idBuffer.getLong(0);
            System.out.println("the message id is:" + messageId);
        }

        ByteBuffer buffer = ByteBuffer.wrap(canMessage);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // TODO: Change order if necessary

        MessageDefinition message = messageDefinitions.get(messageId);
        if (message == null) {
            return null; // Cannot find message definition for this ID
        }
        JSONObject json = new JSONObject();
        for (SignalDefinition signal : message.getSignals()) {
            double signalValue = getSignalValue(buffer, signal);
            json.put(signal.getName(), signalValue);
        }

        return json.toString();
    }

    public static double getSignalValue(ByteBuffer buffer, SignalDefinition signal) {

//        System.out.println("the isUnsigned is " + signal.isUnsigned());
//        System.out.println("the isBigEndian is " + signal.getEndianness());

        int bitStart = signal.getBitStart();
        int bitLength = signal.getBitLength();
        double factor = signal.getFactor();
        double offset = signal.getOffset();

        boolean isBigEndian = signal.getEndianness();
        long rawValue = 0;
        int numBitsRead = 0;

        while (numBitsRead < bitLength) {
            int remainingBits = bitLength - numBitsRead;
            int bitsToRead = Math.min(remainingBits, 8 - (bitStart % 8));

            int byteIndex = bitStart / 8;
            int bitOffset = bitStart % 8;

            byte b = buffer.get(byteIndex);
            int mask = (1 << bitsToRead) - 1;
            int value = (b >> bitOffset) & mask;

            if (isBigEndian) {
                rawValue = (rawValue << bitsToRead) | value;
            } else {
                rawValue |= (long) value << numBitsRead;
            }

            numBitsRead += bitsToRead;
            bitStart += bitsToRead;
        }

        double scaledValue = (rawValue * factor) + offset;
        return scaledValue;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }
}
