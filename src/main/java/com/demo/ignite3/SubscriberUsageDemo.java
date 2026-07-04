package com.demo.ignite3;

import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.table.RecordView;
import org.apache.ignite.table.Table;
import org.apache.ignite.table.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubscriberUsageDemo {

    private static final String[] CUSTOMER_IDS = {
            "CUST-001", "CUST-002", "CUST-003", "CUST-004", "CUST-005"
    };

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {

        String[] addresses = { "localhost:10800" };

        try (IgniteClient client = IgniteClient.builder()
                .addresses(addresses)
                .build()) {

            System.out.println("Connected to the cluster: " + client.connections());

            createTable(client);
            clearTable(client);

            Table subscriberTable = client.tables().table("SUBSCRIBER");
            RecordView<Tuple> recordView = subscriberTable.recordView();

            insertBaselineSubscribers(recordView);
            simulateUsage(recordView);
            printFinalState(recordView);
        }
    }

    private static void createTable(IgniteClient client) {
        System.out.println("\n--- Creating Subscriber table ---");
        client.sql().execute(null,
                "CREATE TABLE IF NOT EXISTS Subscriber (" +
                "customer_id VARCHAR PRIMARY KEY, " +
                "data_usage DOUBLE, " +
                "sms_usage INT, " +
                "call_usage INT)");
        System.out.println("Subscriber table is ready.");
    }

    private static void clearTable(IgniteClient client) {
        System.out.println("\n--- Clearing Subscriber table ---");
        long deleted = client.sql().execute(null, "DELETE FROM Subscriber").affectedRows();
        System.out.println("Deleted " + deleted + " existing row(s).");
    }

    private static void insertBaselineSubscribers(RecordView<Tuple> recordView) {
        System.out.println("\n--- Inserting 5 baseline subscribers ---");
        for (String customerId : CUSTOMER_IDS) {
            Subscriber subscriber = new Subscriber(customerId, 0.0, 0, 0);
            recordView.insert(null, subscriber.toTuple());
            System.out.println("Inserted: " + subscriber);
        }
    }

    private static void simulateUsage(RecordView<Tuple> recordView) {
        System.out.println("\n--- Simulating usage updates ---");
        for (String customerId : CUSTOMER_IDS) {
            Tuple key = Subscriber.keyTuple(customerId);
            Subscriber current = Subscriber.fromTuple(recordView.get(null, key));

            double dataDelta = 50 + RANDOM.nextDouble() * 450;
            int smsDelta = 1 + RANDOM.nextInt(20);
            int callDelta = 1 + RANDOM.nextInt(60);

            Subscriber updated = new Subscriber(
                    customerId,
                    current.getDataUsage() + dataDelta,
                    current.getSmsUsage() + smsDelta,
                    current.getCallUsage() + callDelta);

            recordView.upsert(null, updated.toTuple());

            System.out.printf(
                    "%s: +%.2f MB, +%d SMS, +%d min -> %s%n",
                    customerId, dataDelta, smsDelta, callDelta, updated);
        }
    }

    private static void printFinalState(RecordView<Tuple> recordView) {
        System.out.println("\n--- Final subscriber state ---");
        List<Tuple> keys = new ArrayList<>();
        for (String customerId : CUSTOMER_IDS) {
            keys.add(Subscriber.keyTuple(customerId));
        }
        List<Tuple> results = recordView.getAll(null, keys);
        for (Tuple tuple : results) {
            System.out.println(Subscriber.fromTuple(tuple));
        }
    }
}
