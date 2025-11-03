package domain.entity;

import domain.BitPacking;
import domain.value.object.BitPackingRegistry;
import domain.value.object.factory.products.BitpackingOverlapped;
import domain.value.object.factory.products.BitpackingAligned;
import domain.value.object.factory.products.BitpackingWithOverflow;
import domain.value.object.CompressionTypeEnum;

//public class BitPackingFactory {
//
//    public static BitPacking createBitPacking(CompressionTypeEnum compressionType){
//        return switch (compressionType) {
//            case UNALIGNED -> new BitpackingUnaligned();
//            case ALIGNED -> new BitpackingAligned();
//            case OVERFLOW -> new BitpackingWithOverflow();
//        };
//    }
//}

public final class BitPackingFactory {

    private static final BitPackingRegistry registry = new BitPackingRegistry();

    static {
        try {
            Class.forName(BitpackingAligned.class.getName());
            Class.forName(BitpackingOverlapped.class.getName());
            Class.forName(BitpackingWithOverflow.class.getName());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private BitPackingFactory() {}

    public static BitPackingRegistry getRegistry() {
        return registry;
    }

    public static BitPacking createBitPacking(CompressionTypeEnum type) {
        if (!registry.isRegistered(type)) {
            throw new IllegalArgumentException("No implementation registered for type: " + type);
        }
        return registry.create(type);
    }
}