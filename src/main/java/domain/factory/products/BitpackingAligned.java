package domain.value.object.factory.products;

import domain.BitPacking;
import domain.entity.BitPackingFactory;
import domain.value.object.CompressionTypeEnum;
import domain.value.object.PackedData;
import domain.value.object.UnpackedData;

// modulo 32 equivaut a & 31
public class BitpackingAligned implements BitPacking {
    private final CompressionTypeEnum TYPE = CompressionTypeEnum.ALIGNED;
    private PackedData lastPackedData;

    /**
     * This method compresses data with aligned bit packing (values never cross word boundaries)
     * @param fromUnpackedData the data to compress
     * @param toPackedData the compressed output
     **/
    @Override
    public void compress(UnpackedData fromUnpackedData, PackedData toPackedData) {
        int originalArrayLength = fromUnpackedData.getSize();
        int maxValueInArray = fromUnpackedData.getMaxValue();
        int maxBitsNeeded = this.calculateRequiredBits(maxValueInArray);

        int valuesPerWord = 32 / maxBitsNeeded; // Calculate required size for compressed data
        int requiredWords = (originalArrayLength + valuesPerWord - 1) / valuesPerWord;

        int[] compressedData = new int[requiredWords];
        int currentWord = 0;
        int outputIndex = 0;
        int bitUsedInCurrentWord = 0;

        for (int i = 0; i < originalArrayLength; i++) {
            int value = fromUnpackedData.getData()[i];

            if(bitUsedInCurrentWord + maxBitsNeeded > 32) {
                if (outputIndex < compressedData.length) {
                    compressedData[outputIndex++] = currentWord;
                }
                currentWord = 0;
                bitUsedInCurrentWord = 0;
            }
            currentWord |= (value << bitUsedInCurrentWord);
            bitUsedInCurrentWord += maxBitsNeeded;
        }
        if (bitUsedInCurrentWord > 0 && outputIndex < compressedData.length) {
            compressedData[outputIndex] = currentWord;
        }

        toPackedData.setData(compressedData);
        toPackedData.setOriginalSize(originalArrayLength);
        toPackedData.setCompressedSize(requiredWords);
        toPackedData.setBitsPerValue(maxBitsNeeded);
        this.lastPackedData = toPackedData;
    }

    /**
     * This method decompresses aligned bit-packed data back to original integers
     * @param fromPackedData the compressed data
     * @param toUnpackedData the decompressed output
     **/
    @Override
    public void decompress(PackedData fromPackedData, UnpackedData toUnpackedData) {
        int maxBitsNeeded = fromPackedData.getBitsPerValue();
        int originalArrayLength = fromPackedData.getOriginalSize();
        int[] result = new int[originalArrayLength];

        if (fromPackedData.getData().length == 0 || originalArrayLength == 0) {
            toUnpackedData.setData(result);
            return;
        }

        int mask = (1 << maxBitsNeeded) - 1;
        int currentWord = fromPackedData.getData()[0];
        int outputIndex = 0;
        int bitUsedInCurrentWord = 0;
        int inputIndex = 0;

        for (int i = 0; i < originalArrayLength; i++) {
            if (bitUsedInCurrentWord + maxBitsNeeded > 32) { // Move to next word
                inputIndex++;
                if (inputIndex < fromPackedData.getData().length) {
                    currentWord = fromPackedData.getData()[inputIndex];
                    bitUsedInCurrentWord = 0;
                } else {
                    break;
                }
            }
            result[outputIndex++] = (currentWord >>> bitUsedInCurrentWord) & mask;
            bitUsedInCurrentWord += maxBitsNeeded;
        }

        toUnpackedData.setData(result);
        this.lastPackedData = fromPackedData;
    }

    @Override
    public int get(int index) {
        int[] data = lastPackedData.getData();
        int bits = lastPackedData.getBitsPerValue();
        int valuesPerWord = 32 / bits;
        int wordIndex = index / valuesPerWord;
        int bitOffset = (index % valuesPerWord) * bits;
        int mask = (1 << bits) - 1;
        return (data[wordIndex] >>> bitOffset) & mask;
    }

    static {
        BitPackingFactory.getRegistry().register(
                CompressionTypeEnum.ALIGNED, BitpackingAligned::new
        );
    }

    @Override
    public CompressionTypeEnum getType() {
        return this.TYPE;
    }
}
