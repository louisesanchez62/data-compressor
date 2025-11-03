package domain.factory.products;

import domain.BitPacking;
import domain.factory.BitPackingFactory;
import domain.factory.CompressionTypeEnum;
import domain.entities.PackedData;
import domain.entities.UnpackedData;

// modulo 32 equivaut a & 31
public class BitpackingAligned implements BitPacking {
    private final CompressionTypeEnum TYPE = CompressionTypeEnum.ALIGNED;
    private PackedData lastPackedData;

    /**
     * This method compresses data with aligned bit packing (values never cross word boundaries)
     * Format: [Header: 32 bits] [Compressed Data]
     * Header: 16 bits = originalSize, 16 bits = bitsPerValue
     * @param fromUnpackedData the data to compress
     * @param toPackedData the compressed output
     **/
    @Override
    public void compress(UnpackedData fromUnpackedData, PackedData toPackedData) {
        int originalArrayLength = fromUnpackedData.getSize();
        int maxValueInArray = fromUnpackedData.getMaxValue();
        int maxBitsNeeded = this.calculateRequiredBits(maxValueInArray);

        int valuesPerWord = 32 / maxBitsNeeded;
        int requiredWords = (originalArrayLength + valuesPerWord - 1) / valuesPerWord;

        int[] compressedData = new int[requiredWords + 1];

        int header = (originalArrayLength << 16) | maxBitsNeeded;
        compressedData[0] = header;

        int currentWord = 0;
        int outputIndex = 1;
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
        toPackedData.setCompressedSize(requiredWords + 1);
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
        // Lire l'en-tête
        int header = fromPackedData.getData()[0];
        int originalArrayLength = (header >>> 16) & 0xFFFF;
        int maxBitsNeeded = header & 0xFFFF;

        int[] result = new int[originalArrayLength];

        if (fromPackedData.getData().length <= 1 || originalArrayLength == 0) {
            toUnpackedData.setData(result);
            return;
        }

        int mask = (1 << maxBitsNeeded) - 1;
        int currentWord = fromPackedData.getData()[1];
        int outputIndex = 0;
        int bitUsedInCurrentWord = 0;
        int inputIndex = 1;

        for (int i = 0; i < originalArrayLength; i++) {
            if (bitUsedInCurrentWord + maxBitsNeeded > 32) {
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
        int[] compressedArray = lastPackedData.getData();

        // Lire l'en-tête
        int header = compressedArray[0];
        int bitsPerValue = header & 0xFFFF;

        int valuesPerWord = 32 / bitsPerValue;
        int wordIndex = (index / valuesPerWord) + 1;
        int bitOffset = (index % valuesPerWord) * bitsPerValue;
        int mask = (1 << bitsPerValue) - 1;

        return (compressedArray[wordIndex] >>> bitOffset) & mask;
    }

    static {
        BitPackingFactory.getRegistry().register(
                CompressionTypeEnum.ALIGNED, BitpackingAligned::new
        );
    }
}
