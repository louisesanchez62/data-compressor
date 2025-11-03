package domain;

import domain.exception.CompressionException;
import domain.exception.InvalidDataException;
import domain.value.object.BitPackingRegistry;
import domain.value.object.CompressionTypeEnum;
import domain.value.object.PackedData;
import domain.value.object.UnpackedData;

import java.util.Objects;

public interface BitPacking {
    int WORD_BITS = 32;
    int MAX_SIZE = 1_000_000;

    void compress(UnpackedData fromUnpackedData, PackedData toPackedData);
    void decompress(PackedData fromPackedData, UnpackedData toUnpackedData);
    int get(int i);

    CompressionTypeEnum getType();

    /**
     * Should return a header with the size in the first 16 bits and the nb of needed bits in the 16 others
     **/
    default int[] createHeader(UnpackedData array) {
        int header = (array.getSize() << 16) | array.getMaxValue();
        return new int[header];
    }

    default void validateCompression(UnpackedData from) { // Domain level validation
        Objects.requireNonNull(from, "UnpackedData cannot be null");

        if (from.getSize() == 0) throw new InvalidDataException("Cannot compress empty data");
        if (from.getSize() > MAX_SIZE) throw new CompressionException("Data size exceeds maximum: " + MAX_SIZE);
    }

    default void validateDecompression(PackedData from) { // Domain level validation
        Objects.requireNonNull(from, "PackedData cannot be null");

        if (from.getCompressedSize() == 0) throw new InvalidDataException("Cannot decompress empty data");
    }

    default int calculateRequiredBits(int maxValue) {
        return Integer.toBinaryString(maxValue).length();
    }

    default int calculateRequiredWords(int elements, int bitsPerElement) {
        long totalBits = (long) elements * bitsPerElement;
        return (int) ((totalBits + WORD_BITS - 1) / WORD_BITS);
    }
}

