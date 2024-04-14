package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OrderUtils {

    public static void writeOrderIDToFile(String orderID, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Order ID: " + orderID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
