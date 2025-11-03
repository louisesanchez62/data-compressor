package domain.value.object;

import domain.BitPacking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BitPackingRegistry {
    private final Map<CompressionTypeEnum, Supplier<BitPacking>> registry = new HashMap<>();

    public void register(CompressionTypeEnum type, Supplier<BitPacking> supplier) {
        if (type == null || supplier == null)
            throw new IllegalArgumentException("Type and supplier must not be null");
        registry.put(type, supplier);
    }

    public BitPacking create(CompressionTypeEnum type) {
        Supplier<BitPacking> supplier = registry.get(type);
        if (supplier == null)
            throw new IllegalArgumentException("No implementation registered for type: " + type);
        return supplier.get();
    }

    public boolean isRegistered(CompressionTypeEnum type) {
        return registry.containsKey(type);
    }
}