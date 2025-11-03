package domain.factory.products;

import domain.BitPacking;
import domain.factory.BitPackingFactory;
import domain.factory.CompressionTypeEnum;
import domain.entities.PackedData;
import domain.entities.UnpackedData;

public class BitpackingOverlapped implements BitPacking {
    private final CompressionTypeEnum TYPE = CompressionTypeEnum.OVERLAPPED;
    private PackedData lastPackedData;

    /**
     * This method compresses data with unaligned bit packing (values can span multiple words)
     * Format: [Header: 32 bits] [Compressed Data]
     * Header: 16 bits = originalSize, 16 bits = bitsPerValue
     * @param fromUnpackedData
     * @param toPackedData
     **/
    @Override
    public void compress(UnpackedData fromUnpackedData, PackedData toPackedData) {
        int originalArrayLength = fromUnpackedData.getSize();
        int maxValueInArray = fromUnpackedData.getMaxValue();
        int maxBitsNeeded = this.calculateRequiredBits(maxValueInArray);
        int totalBits = originalArrayLength * maxBitsNeeded;
        int requiredWords = (totalBits + 31) / 32;

        int[] compressedData = new int[requiredWords + 1];

        int header = (originalArrayLength << 16) | maxBitsNeeded;
        compressedData[0] = header;

        int bitPosition = 0;

        for (int i = 0; i < originalArrayLength; i++) {
            int value = fromUnpackedData.getData()[i] & ((1 << maxBitsNeeded) - 1);
            int wordIndex = (bitPosition / 32) + 1;
            int bitOffset = bitPosition & 31;

            int bitsRemainingInWord = 32 - bitOffset;

            if(bitsRemainingInWord >= maxBitsNeeded) {
                compressedData[wordIndex] |= (value << bitOffset);
            } else {
                compressedData[wordIndex] |= (value << bitOffset);
                compressedData[wordIndex + 1] |= (value >>> bitsRemainingInWord);
            }

            bitPosition += maxBitsNeeded;
        }

        toPackedData.setData(compressedData);
        toPackedData.setOriginalSize(originalArrayLength);
        toPackedData.setCompressedSize(requiredWords + 1);
        toPackedData.setBitsPerValue(maxBitsNeeded);
        this.lastPackedData = toPackedData;
    }

    @Override
    public void decompress(PackedData fromPackedData, UnpackedData toUnpackedData) {
        // Lire l'en-tête
        int header = fromPackedData.getData()[0];
        int originalArrayLength = (header >>> 16) & 0xFFFF;
        int maxBitsNeeded = header & 0xFFFF;

        int[] result = new int[originalArrayLength];

        int mask = (1 << maxBitsNeeded) - 1;
        int bitPosition = 0;

        for (int i = 0; i < originalArrayLength; i++) {
            int wordIndex = (bitPosition / 32) + 1;
            int bitOffset = bitPosition & 31;
            int bitsRemainingInWord = 32 - bitOffset;

            if (bitsRemainingInWord >= maxBitsNeeded) {
                result[i] = (fromPackedData.getData()[wordIndex] >>> bitOffset) & mask;
            } else {
                int lowBits = fromPackedData.getData()[wordIndex] >>> bitOffset;
                int highBits = fromPackedData.getData()[wordIndex + 1] << bitsRemainingInWord;
                result[i] = (lowBits | highBits) & mask;
            }

            bitPosition += maxBitsNeeded;
        }

        toUnpackedData.setData(result);
        this.lastPackedData = fromPackedData;
    }

    @Override
    public int get(int index) {
        // Lire l'en-tête
        int header = lastPackedData.getData()[0];
        int bits = header & 0xFFFF;

        int bitPosition = index * bits;
        int wordIndex = (bitPosition / 32) + 1;
        int bitOffset = bitPosition & 31;
        int mask = (1 << bits) - 1;
        int[] words = lastPackedData.getData();

        if (bitOffset + bits <= 32) {
            return (words[wordIndex] >>> bitOffset) & mask;
        } else {
            int low = words[wordIndex] >>> bitOffset;
            int high = words[wordIndex + 1] << (32 - bitOffset);
            return (low | high) & mask;
        }
    }

    static {
        BitPackingFactory.getRegistry().register(
                CompressionTypeEnum.OVERLAPPED, BitpackingOverlapped::new
        );
    }
}
