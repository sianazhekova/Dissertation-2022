package analyzers.baseline_analyzer.unit_tests;

import analyzers.baseline_analyzer.MemoryAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

public class MemoryAccessTest {

    @ParameterizedTest
    @EnumSource(names = {"WRITE", "READ"})
    void testDependenceWithValueSource(MemoryAccess unitAccess) {
        Assertions.assertTrue(EnumSet.of(MemoryAccess.WRITE, MemoryAccess.READ).contains(unitAccess));
    }

}
