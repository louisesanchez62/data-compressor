package domain.value.object.factory.products;

import domain.BitPacking;
import domain.entity.BitPackingFactory;
import domain.value.object.CompressionTypeEnum;
import domain.value.object.PackedData;
import domain.value.object.UnpackedData;

public class BitpackingOverlapped implements BitPacking {
    private final CompressionTypeEnum TYPE = CompressionTypeEnum.OVERLAPPED;
    private PackedData lastPackedData;

    /**
     * This method compresses data with unaligned bit packing (values can span multiple words)
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

        int[] compressedData = new int[requiredWords];
        int bitPosition = 0;

        for (int i = 0; i < originalArrayLength; i++) {
            int value = fromUnpackedData.getData()[i] & ((1 << maxBitsNeeded) - 1);
            int wordIndex = bitPosition / 32;
            int bitOffset = bitPosition % 32;

            int bitsRemainingInWord = 32 - bitOffset;

            if(bitsRemainingInWord >= maxBitsNeeded) { // Value fits entirely in current word
                compressedData[wordIndex] |= (value << bitOffset);
            } else { // Value spans two words
                compressedData[wordIndex] |= (value << bitOffset);
                compressedData[wordIndex + 1] |= (value >>> bitsRemainingInWord);
            }

            bitPosition += maxBitsNeeded;
        }

        toPackedData.setData(compressedData);
        toPackedData.setOriginalSize(originalArrayLength);
        toPackedData.setCompressedSize(requiredWords);
        toPackedData.setBitsPerValue(maxBitsNeeded);
        this.lastPackedData = toPackedData;
    }

    @Override
    public void decompress(PackedData fromPackedData, UnpackedData toUnpackedData) {
        int maxBitsNeeded = fromPackedData.getBitsPerValue();
        int originalArrayLength = fromPackedData.getOriginalSize();
        int[] result = new int[originalArrayLength];

        int mask = (1 << maxBitsNeeded) - 1;
        int bitPosition = 0;

        for (int i = 0; i < originalArrayLength; i++) {
            int wordIndex = bitPosition / 32;
            int bitOffset = bitPosition % 32;
            int bitsRemainingInWord = 32 - bitOffset;

            if (bitsRemainingInWord >= maxBitsNeeded) { // Value fits entirely in current word
                result[i] = (fromPackedData.getData()[wordIndex] >>> bitOffset) & mask;
            } else { // Value spans two words
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
        int bits = lastPackedData.getBitsPerValue();
        int bitPosition = index * bits;
        int wordIndex = bitPosition / 32;
        int bitOffset = bitPosition % 32;
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

    @Override
    public CompressionTypeEnum getType() {
        return TYPE;
    }
}

