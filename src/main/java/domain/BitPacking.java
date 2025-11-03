package domain;

import domain.exception.CompressionException;
import domain.exception.InvalidDataException;
import domain.factory.CompressionTypeEnum;
import domain.entities.PackedData;
import domain.entities.UnpackedData;

import java.util.Objects;

public interface BitPacking {

    void compress(UnpackedData fromUnpackedData, PackedData toPackedData);
    void decompress(PackedData fromPackedData, UnpackedData toUnpackedData);
    int get(int i);

    default int calculateRequiredBits(int maxValue) {
        return Integer.toBinaryString(maxValue).length();
    }
}

