package domain.entities;

import domain.exception.UnpackedDataException;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

public class UnpackedData {
    private int[] data;
    private int size;

    private UnpackedData(int[] intArray) {
        this.data = Arrays.copyOf(intArray, intArray.length);
        this.size = intArray.length;
    }

    public static UnpackedData from(int[] intArray) {
        if (intArray == null || intArray.length == 0){
            throw new UnpackedDataException("intArray must not be null or empty");
        }
        return new UnpackedData(intArray);
    }

    public static UnpackedData empty() {
        return new UnpackedData(new int[0]);
    }

    public int getMaxValue() {
        IntSummaryStatistics stat = Arrays.stream(this.getData()).summaryStatistics();
        return stat.getMax();
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
        this.size = data.length;
    }

    public int getSize() {
        return size;
    }
}
