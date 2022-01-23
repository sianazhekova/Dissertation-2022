package analyzers.baseline_analyzer.unit_tests;

import analyzers.baseline_analyzer.DataDependence;
import analyzers.baseline_analyzer.MemoryAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

public class DataDependenceTest {

    @ParameterizedTest
    @EnumSource(names = {"READ", "WRITE"})
    void testDependenceWithValueSource(MemoryAccess unitAccess) {
        MemoryAccess baseAccess = MemoryAccess.READ;
        System.out.println("Case1: Base access is " + baseAccess.name()+ ", and the unit access is " + unitAccess.name());
        DataDependence dataDep = DataDependence.getDependence(baseAccess, unitAccess);
        System.out.println(dataDep.name() + " which is equivalent to " + DataDependence.getStringDepType(dataDep));
        //Assertions.assertTrue(EnumSet.of(DataDependence.WR).contains(dataDep));

        baseAccess = MemoryAccess.WRITE;
        System.out.println("Case 2: Base access is " + baseAccess.name()+ ", and the unit access is " + unitAccess.name());
        dataDep = DataDependence.getDependence(baseAccess, unitAccess);
        System.out.println(dataDep.name() + " which is equivalent to " + DataDependence.getStringDepType(dataDep));
        //Assertions.assertTrue(EnumSet.of(DataDependence.RW, DataDependence.WW).contains(dataDep));
    }

}
