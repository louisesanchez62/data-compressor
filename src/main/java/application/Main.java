package application;

import application.utils.Statistics;
import application.utils.TestCases;
import domain.BitPacking;
import domain.entity.BitPackingFactory;
import domain.value.object.PackedData;
import domain.value.object.CompressionTypeEnum;
import domain.value.object.UnpackedData;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Tests de BitPacking ===\n");

        TestCases.execute();

        // Comparaison des performances
        System.out.println("\n\n=== Comparaison des performances ===");
        int[] largeArray = generateArray(1000, 1000);
        Statistics.compareAllMethods(largeArray);
    }

    private static int[] generateArray(int size, int maxValue) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = (int) (Math.random() * maxValue);
        }
        return array;
    }
}