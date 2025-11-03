package domain.value.object;
import domain.exception.PackedDataException;

import java.util.Arrays;

public class PackedData {
    private int[] data;
    private int originalSize;
    private int compressedSize;
    private int bitsPerValue;

    private PackedData(int[] intArray) {
        this.data = Arrays.copyOf(intArray, intArray.length);
        this.originalSize = intArray.length;
        this.compressedSize = intArray.length;
        this.bitsPerValue = 32;
    }

    private PackedData(int size) {
        this.data = new int[size];
        this.originalSize = 0;
        this.compressedSize = 0;
        this.bitsPerValue = 0;
    }

    public static PackedData from(int[] intArray) {
        if (intArray == null || intArray.length == 0) {
            throw new PackedDataException("intArray must not be null or empty");
        }
        return new PackedData(intArray);
    }

    public static PackedData empty() {
        return new PackedData(0);
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] newData) {
        this.data = newData;
    }

    public void setOriginalSize(int size) {
        this.originalSize = size;
    }

    public void setCompressedSize(int size) {
        this.compressedSize = size;
    }

    public void setBitsPerValue(int bits) {
        this.bitsPerValue = bits;
    }

    public int getOriginalSize() {
        return originalSize;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public int getBitsPerValue() {
        return bitsPerValue;
    }
}
