package com.linzesu.javacan.utils;

import com.linzesu.javacan.definitions.MessageDefinition;
import com.linzesu.javacan.definitions.SignalDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageParser {

    private static final Logger LOG = LogManager.getLogger(MessageParser.class);

    public static JSONObject processCanMessageToJSON(LinkedHashMap<Long, MessageDefinition> messageDefinitions,
                                                     byte[] canID,
                                                     byte[] canMessage,
                                                     boolean isExtended,
                                                     ArrayList<String> signalName) {

        long messageId;
        // Check if the canID is 8 bytes long to make ByteBuffer.getLong to work.
        byte[] newArray = new byte[8];
        if(canID.length < 8){
            System.arraycopy(canID, 0, newArray, 8-canID.length, canID.length);
            canID = newArray;
        }

        if (isExtended) {
            LOG.info("Using extended CAN format");
            ByteBuffer idBuffer = ByteBuffer.wrap(canID);
            // Add 0x8000000 if using extended CAN
            messageId = idBuffer.getLong(0) + 2147483648L;
            LOG.info("the message id is: " + messageId);
        }else{
            ByteBuffer idBuffer = ByteBuffer.wrap(canID);
            messageId = idBuffer.getLong(0);
            LOG.info("the message id is: " + messageId);
        }

        ByteBuffer buffer = ByteBuffer.wrap(canMessage);

        MessageDefinition message = messageDefinitions.get(messageId);
        if (message == null) {
            LOG.info("Cannot find message definition for ID " + messageId);
            return null;
        }
        JSONObject json = new JSONObject();
        if (signalName == null){
            for (SignalDefinition signal : message.getSignals()) {
                double signalValue = getSignalValue(buffer, signal);
                json.put(signal.getName(), signalValue);
            }
        }else{
            for (String name: signalName) {
                SignalDefinition signal = message.getSignal(name);
                double signalValue = getSignalValue(buffer, signal);
                json.put(signal.getName(), signalValue);
            }
        }

        return json;
    }

    public static byte[] processJSONToCanMessage(Map<Long, MessageDefinition> messageDefinitions, JSONObject json, long canId) {
        MessageDefinition messageDefinition = messageDefinitions.get(canId);
        if (messageDefinition == null) {
            throw new IllegalArgumentException("Cannot find message definition for CAN ID: " + canId);
        }

        int dataLength = 8;
        ByteBuffer buffer = ByteBuffer.allocate(dataLength);

        for (String key : json.keySet()) {
            SignalDefinition signalDefinition = messageDefinition.getSignal(key);
            if (signalDefinition == null) {
                throw new IllegalArgumentException("Cannot find signal definition for signal name: " + key);
            }

            double rawValue = json.getDouble(key);
            long signalValue = (long) ((rawValue - signalDefinition.getOffset()) / signalDefinition.getFactor());
            int startBit = signalDefinition.getBitStart();
            int length = signalDefinition.getBitLength();
            boolean isBigEndian = signalDefinition.getEndianness();

            if (isBigEndian) {
                int bitPos = startBit;
                int bitValue;
                for (int i = 0; i < length; i++) {
                    int byteIndex = bitPos / 8;
                    int bitIndex = bitPos % 8;
                    bitValue = (int)(signalValue >> (length - i - 1)) & 1;
                    buffer.put(byteIndex, (byte) (buffer.get(byteIndex) | (bitValue << bitIndex)));

                    if (bitIndex == 0) {
                        bitPos += 15;
                    } else {
                        bitPos--;
                    }
                }
            } else {
                for (int i = 0; i < length; i++) {
                    int byteIndex = (startBit + i) / 8;
                    int bitIndex = (startBit + i) % 8;
                    buffer.put(byteIndex, (byte) (buffer.get(byteIndex) | (((signalValue >> i) & 1) << bitIndex)));
                }
            }
        }

        return buffer.array();
    }
    public static double getSignalValue(ByteBuffer buffer, SignalDefinition signal) {

        boolean isBigEndian = signal.getEndianness();

        int bitStart = signal.getBitStart();
        int bitLength = signal.getBitLength();
        double factor = signal.getFactor();
        double offset = signal.getOffset();

        long rawValue = 0;
        int numBitsRead = 0;

        if(isBigEndian){
            while (numBitsRead < bitLength) {

                int remainingBits = bitLength - numBitsRead;
                // The number of bits to read in this byte.
                int bitsToRead = Math.min(remainingBits, bitStart % 8 +1);

                int byteIndex = bitStart / 8;
                int bitOffset = Math.max(8 - remainingBits, 0);

                // Get the value in the current reading byte.
                byte b = buffer.get(byteIndex);
                int mask = (1 << bitsToRead) - 1;
                int value = (b >> bitOffset) & mask;

                // Adding value from the previous loop.
                int shift = (remainingBits - bitsToRead);
                LOG.debug("the shift of signal {} is {}.", signal.getName(), shift);
                rawValue |= (long) value << shift;

                // Shifting relevant bits.
                numBitsRead += bitsToRead;
                // The bitStart for big endian signal can be very confusing. It's recommended to use tools like
                // canoe to get clearer view of how the big endian data is aligned.
                bitStart = (byteIndex+2)*8 - 1;
            }
        }else{
            while (numBitsRead < bitLength) {

                int remainingBits = bitLength - numBitsRead;
                // The number of bits to read in this byte.
                int bitsToRead = Math.min(remainingBits, 8 - (bitStart % 8));

                int byteIndex = bitStart / 8;
                int bitOffset = bitStart % 8;

                // Get the value in the current reading byte.
                byte b = buffer.get(byteIndex);
                int mask = (1 << bitsToRead) - 1;
                LOG.debug("the bit offset of signal " + signal.getName() + " is: " + bitOffset);
                int value = (b >> bitOffset) & mask;

                // Adding value from the previous loop.
                rawValue |= (long) value << numBitsRead;

                // Shifting relevant bits.
                numBitsRead += bitsToRead;
                bitStart += bitsToRead;
            }
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
