/**
 *
 */
package org.theseed.genome.coupling;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.theseed.genome.Coupling;
import org.theseed.io.TabbedLineReader;

/**
 * @author Bruce Parrello
 *
 */
public class TestRoleCouplingMap  {

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        File origFile = new File("data", "roles.coupling.tbl");
        RoleCouplingMap map1 = new RoleCouplingMap(origFile);
        // This will track all the roles we find in the input file.
        Set<String> fileSet = new HashSet<String>(30);
        // Verify that everything in the file is in the map.
        try (TabbedLineReader origStream = new TabbedLineReader(origFile)) {
            for (TabbedLineReader.Line line : origStream) {
                String role1 = map1.getRole(line.get(0));
                String role2 = map1.getRole(line.get(1));
                assertThat(role1, not(nullValue()));
                assertThat(role2, not(nullValue()));
                Set<Coupling> couplings = map1.getCouplings(role1);
                assertThat(couplings.stream().anyMatch(x -> x.getTarget().contentEquals(role2)), equalTo(true));
                fileSet.add(role1);
                fileSet.add(role2);
            }
        }
        // Verify that only the roles in the file are in the map.
        Set<String> roleSet = map1.getRoles();
        assertThat(roleSet.size(), equalTo(fileSet.size()));
        // Verify that each role in the file is in the map list.
        for (String role : fileSet)
            assertThat(role, roleSet.contains(role), equalTo(true));
        // Spot check some sizes and strengths.
        SortedSet<Coupling> couplings = map1.getCouplings("DnaDireRnaPolySubu");
        Coupling couple = couplings.first();
        assertThat(couple.getTarget(), equalTo("SsuRiboProtS5e"));
        assertThat(couple.getSize(), equalTo(34));
        assertThat(couple.getStrength(), closeTo(319.0955, 0.0001));
        couplings = map1.getCouplings("3SulfDehy");
        assertThat(couplings.size(), equalTo(3));
        Iterator<Coupling> iter = couplings.iterator();
        couple = iter.next();
        assertThat(couple.getTarget(), equalTo("SulfSulfLyasSubu2"));
        assertThat(couple.getSize(), equalTo(20));
        assertThat(couple.getStrength(), closeTo(48.7050, 0.0001));
        couple = iter.next();
        assertThat(couple.getTarget(), equalTo("SulfSulfLyasSubu"));
        assertThat(couple.getSize(), equalTo(18));
        assertThat(couple.getStrength(), closeTo(41.7053, 0.0001));
        couple = iter.next();
        assertThat(couple.getTarget(), equalTo("SulfDehy"));
        assertThat(couple.getSize(), equalTo(16));
        assertThat(couple.getStrength(), closeTo(32.7726, 0.0001));
        // Write the map and read it back.
        File saveFile = new File("data", "map.ser");
        map1.save(saveFile);
        RoleCouplingMap map2 = RoleCouplingMap.load(saveFile);
        Set<String> loadSet = map2.getRoles();
        assertThat(loadSet.size(), equalTo(roleSet.size()));
        for (String role : loadSet) {
            assertThat(role, roleSet.contains(role), equalTo(true));
            SortedSet<Coupling> oldCouplings = map1.getCouplings(role);
            SortedSet<Coupling> newCouplings = map2.getCouplings(role);
            assertThat(role, oldCouplings.equals(newCouplings), equalTo(true));
        }
    }
}
