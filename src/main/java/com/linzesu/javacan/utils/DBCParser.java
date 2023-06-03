package com.linzesu.javacan.utils;

import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.linzesu.javacan.definitions.MessageDefinition;
import com.linzesu.javacan.definitions.SignalDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBCParser {

    private static final Logger LOG = LoggerFactory.getLogger(DBCParser.class);

    private LinkedHashMap<Long, MessageDefinition> messageDefinitions;

    public DBCParser(String dbcFilePath) throws IOException {
        messageDefinitions = new LinkedHashMap<Long, MessageDefinition>();
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

                LOG.info("putting signal {} in message {}.", signalName, messageId);
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
                    // Extract minimum and maximum values
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

    public LinkedHashMap<Long, MessageDefinition> getMessageDefinitions(){
        return this.messageDefinitions;
    }
}
