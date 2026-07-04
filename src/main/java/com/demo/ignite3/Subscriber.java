package com.demo.ignite3;

import org.apache.ignite.table.Tuple;

public class Subscriber {

    private String customerId;
    private double dataUsage;
    private int smsUsage;
    private int callUsage;

    public Subscriber() {
    }

    public Subscriber(String customerId, double dataUsage, int smsUsage, int callUsage) {
        this.customerId = customerId;
        this.dataUsage = dataUsage;
        this.smsUsage = smsUsage;
        this.callUsage = callUsage;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public double getDataUsage() { return dataUsage; }
    public void setDataUsage(double dataUsage) { this.dataUsage = dataUsage; }
    public int getSmsUsage() { return smsUsage; }
    public void setSmsUsage(int smsUsage) { this.smsUsage = smsUsage; }
    public int getCallUsage() { return callUsage; }
    public void setCallUsage(int callUsage) { this.callUsage = callUsage; }

    public Tuple toTuple() {
        return Tuple.create()
                .set("CUSTOMER_ID", customerId)
                .set("DATA_USAGE", dataUsage)
                .set("SMS_USAGE", smsUsage)
                .set("CALL_USAGE", callUsage);
    }

    public static Subscriber fromTuple(Tuple tuple) {
        return new Subscriber(
                tuple.stringValue("CUSTOMER_ID"),
                tuple.doubleValue("DATA_USAGE"),
                tuple.intValue("SMS_USAGE"),
                tuple.intValue("CALL_USAGE"));
    }

    public static Tuple keyTuple(String customerId) {
        return Tuple.create().set("CUSTOMER_ID", customerId);
    }

    @Override
    public String toString() {
        return String.format(
                "Subscriber{customerId='%s', dataUsage=%.2f MB, smsUsage=%d, callUsage=%d min}",
                customerId, dataUsage, smsUsage, callUsage);
    }
}
