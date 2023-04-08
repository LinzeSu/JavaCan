package com.linzesu.javacan.definitions;

public class SignalDefinition {
    private long messageID;
    private String name;
    private int bitStart;
    private int bitLength;
    private double factor;
    private double offset;
    private double minValue;
    private double maxValue;
    private String unit;
    private String receiver;
    private boolean isBigEndian;
    private boolean isUnsigned;


    public SignalDefinition(long messageID, String name, int bitStart, int bitLength, double factor,
                            double offset, double minValue, double maxValue, String unit, String receiver,
                            boolean isBigEndian, boolean isUnsigned) {
        this.messageID = messageID;
        this.name = name;
        this.bitStart = bitStart;
        this.bitLength = bitLength;
        this.factor = factor;
        this.offset = offset;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.receiver = receiver;
        this.isBigEndian = isBigEndian;
        this.isUnsigned=isUnsigned;
    }

    public long getMessageID() {
        return messageID;
    }

    public String getName() {
        return name;
    }

    public int getBitStart() {
        return bitStart;
    }

    public int getBitLength() {
        return bitLength;
    }

    public double getFactor() {
        return factor;
    }

    public double getOffset() {
        return offset;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getReceiver() {
        return receiver;
    }

    public Boolean getEndianness() {
        return isBigEndian;
    }

    public boolean isUnsigned() {
        return isUnsigned;
    }
}
