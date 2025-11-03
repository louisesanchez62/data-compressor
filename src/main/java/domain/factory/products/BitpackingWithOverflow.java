package domain.value.object.factory.products;

import domain.BitPacking;
import domain.entity.BitPackingFactory;
import domain.value.object.CompressionTypeEnum;
import domain.value.object.PackedData;
import domain.value.object.UnpackedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitpackingWithOverflow implements BitPacking {
    private static final CompressionTypeEnum TYPE = CompressionTypeEnum.OVERFLOW;
    private static final int WORD_BITS = 32;
    private PackedData lastPackedData;

    private int payloadBits;
    private int elementBits;
    private int maxPayloadValue;
    private int payloadMask;

    private List<Integer> overflowTable;

    @Override
    public void compress(UnpackedData fromUnpackedData, PackedData toPackedData) {
        int[] originalArray = fromUnpackedData.getData();
        int originalArrayLength = fromUnpackedData.getSize();
        int[] bitLengths = Arrays.stream(originalArray).map(value -> Integer.toBinaryString(value).length()).toArray();

        payloadBits = findBitSize(bitLengths);
        elementBits = payloadBits + 1;
        maxPayloadValue = (1 << payloadBits) - 1;
        payloadMask = maxPayloadValue;

        overflowTable = new ArrayList<>();

        int totalBits = originalArrayLength * elementBits;
        int requiredWords = (totalBits + WORD_BITS - 1) / WORD_BITS;
        int[] compressed = new int[requiredWords];

        int bitPosition = 0;

        for (int value : originalArray) {
            int encoded;
            if (value <= maxPayloadValue) { // flag = 0
                encoded = (0 << payloadBits) | (value & payloadMask);
            } else {
                int index = overflowTable.size(); // flag = 1 -> value in overflow
                overflowTable.add(value);
                encoded = (1 << payloadBits) | (index & payloadMask);
            }

            int wordIndex = bitPosition / WORD_BITS;
            int bitOffset = bitPosition % WORD_BITS;
            int bitsLeft = WORD_BITS - bitOffset;

            if (bitsLeft >= elementBits) {
                compressed[wordIndex] |= (encoded << bitOffset);
            } else {
                compressed[wordIndex] |= (encoded << bitOffset);
                compressed[wordIndex + 1] |= (encoded >>> bitsLeft);
            }

            bitPosition += elementBits;
        }

        int[] header = new int[3 + overflowTable.size()];
        header[0] = payloadBits;
        header[1] = originalArrayLength;
        header[2] = overflowTable.size();
        for (int i = 0; i < overflowTable.size(); i++) {
            header[3 + i] = overflowTable.get(i);
        }

        int[] finalData = new int[header.length + compressed.length];
        System.arraycopy(header, 0, finalData, 0, header.length);
        System.arraycopy(compressed, 0, finalData, header.length, compressed.length);

        toPackedData.setData(finalData);
        toPackedData.setOriginalSize(originalArrayLength);
        toPackedData.setCompressedSize(finalData.length);
        toPackedData.setBitsPerValue(elementBits);
        this.lastPackedData = toPackedData;
    }

    private int findBitSize(int[] bitLengths) {
        Arrays.sort(bitLengths);
        int total = bitLengths.length, best = bitLengths[total - 1];
        long minSize = Long.MAX_VALUE;

        for (int i = 0; i < total; ) {
            int bits = bitLengths[i];
            int j = i;
            while (j < total && bitLengths[j] == bits) j++;
            int overflow = total - j;
            if (overflow == 0 || overflow <= (1 << bits)) {
                long size = (long) total * (1 + bits) + (long) overflow * 32;
                if (size < minSize) { minSize = size; best = bits; }
            }
            i = j;
        }
        return best;
    }

    @Override
    public void decompress(PackedData fromPackedData, UnpackedData toUnpackedData) {
        int[] packed = fromPackedData.getData();

        payloadBits = packed[0];
        int originalSize = packed[1];
        int overflowCount = packed[2];
        elementBits = payloadBits + 1;
        maxPayloadValue = (1 << payloadBits) - 1;
        payloadMask = maxPayloadValue;

        overflowTable = new ArrayList<>(overflowCount);
        for (int i = 0; i < overflowCount; i++) {
            overflowTable.add(packed[3 + i]);
        }

        int[] data = new int[originalSize];
        int[] bitstream = new int[packed.length - (3 + overflowCount)];
        System.arraycopy(packed, 3 + overflowCount, bitstream, 0, bitstream.length);

        int bitPosition = 0;
        int encodedMask = (1 << elementBits) - 1;

        for (int i = 0; i < originalSize; i++) {
            int wordIndex = bitPosition / WORD_BITS;
            int bitOffset = bitPosition % WORD_BITS;
            int bitsLeft = WORD_BITS - bitOffset;

            int encoded;
            if (bitsLeft >= elementBits) {
                encoded = (bitstream[wordIndex] >>> bitOffset) & encodedMask;
            } else {
                int lowBits = bitstream[wordIndex] >>> bitOffset;
                int highBits = bitstream[wordIndex + 1] << bitsLeft;
                encoded = (lowBits | highBits) & encodedMask;
            }

            int flag = (encoded >>> payloadBits) & 1;
            int payload = encoded & payloadMask;

            if (flag == 0) {
                data[i] = payload;
            } else {
                data[i] = overflowTable.get(payload);
            }

            bitPosition += elementBits;
        }

        toUnpackedData.setData(data);
        this.lastPackedData = fromPackedData;
    }

    @Override
    public int get(int index) {
        int[] packed = lastPackedData.getData();
        int storedPayloadBits = packed[0];
        int overflowCount = packed[2];

        int headerSize = 3 + overflowCount;
        int[] bitstream = new int[packed.length - headerSize];
        System.arraycopy(packed, headerSize, bitstream, 0, bitstream.length);

        List<Integer> overflow = new ArrayList<>(overflowCount);
        for (int i = 0; i < overflowCount; i++) {
            overflow.add(packed[3 + i]);
        }

        int bits = storedPayloadBits + 1;
        int bitPosition = index * bits;
        int wordIndex = bitPosition / WORD_BITS;
        int bitOffset = bitPosition % WORD_BITS;
        int mask = (1 << bits) - 1;

        int encoded;
        if (bitOffset + bits <= WORD_BITS) {
            encoded = (bitstream[wordIndex] >>> bitOffset) & mask;
        } else {
            int low = bitstream[wordIndex] >>> bitOffset;
            int high = bitstream[wordIndex + 1] << (WORD_BITS - bitOffset);
            encoded = (low | high) & mask;
        }

        int flag = (encoded >>> storedPayloadBits) & 1;
        int payload = encoded & ((1 << storedPayloadBits) - 1);
        return (flag == 0) ? payload : overflow.get(payload);
    }

    static {
        BitPackingFactory.getRegistry().register(
                CompressionTypeEnum.OVERFLOW, BitpackingWithOverflow::new
        );
    }

    @Override
    public CompressionTypeEnum getType() {
        return TYPE;
    }
}
