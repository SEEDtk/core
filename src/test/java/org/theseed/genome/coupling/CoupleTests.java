/**
 *
 */
package org.theseed.genome.coupling;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.theseed.test.Matchers.*;

/**
 * Test for coupling-related functions
 *
 * @author Bruce Parrello
 *
 */
public class CoupleTests {

    @Test
    public void testFidPairs() {
        CouplingSet.FidPair test1 = new CouplingSet.FidPair("abc:def");
        CouplingSet.FidPair test1a = new CouplingSet.FidPair("abc:def");
        CouplingSet.FidPair test2 = new CouplingSet.FidPair("123:456");
        assertThat(test1, equalTo(test1a));
        assertThat(test1a, equalTo(test1));
        assertThat(test2, not(equalTo(test1)));
        assertThat(test2, not(equalTo(test1a)));
        assertThat(test1.getFid(0), equalTo("abc"));
        assertThat(test1.getFid(1), equalTo("def"));
    }

    @Test
    public void testCouplingSet() throws IOException {
        CouplingSet couples = new CouplingSet(new File("data", "couples.tbl"), "PGF_00006351", "PGF_00884706");
        assertThat(couples.getFamily(0), equalTo("PGF_00006351"));
        assertThat(couples.getFamily(1), equalTo("PGF_00884706"));
        assertThat(couples.getFunction(0), equalTo("Efflux ABC transporter, permease protein"));
        assertThat(couples.getFunction(1), equalTo("Efflux ABC transporter, ATP-binding protein"));
        assertThat(couples.size(), equalTo(5));
        Iterator<CouplingSet.FidPair> iter = couples.iterator();
        assertThat(iter.hasNext(), isTrue());
        CouplingSet.FidPair curr = iter.next();
        assertThat(curr.getFid(0),equalTo("fig|100226.1.peg.1224")); assertThat(curr.getFid(1), equalTo("fig|100226.1.peg.1225"));
        assertThat(curr.getGenomeId(), equalTo("100226.1"));
        assertThat(iter.hasNext(), isTrue());
        curr = iter.next();
        assertThat(curr.getFid(0),equalTo("fig|100226.1.peg.1686")); assertThat(curr.getFid(1), equalTo("fig|100226.1.peg.1687"));
        assertThat(iter.hasNext(), isTrue());
        curr = iter.next();
        assertThat(curr.getFid(0),equalTo("fig|100226.1.peg.1895")); assertThat(curr.getFid(1), equalTo("fig|100226.1.peg.1896"));
        assertThat(iter.hasNext(), isTrue());
        curr = iter.next();
        assertThat(curr.getFid(0),equalTo("fig|100226.1.peg.2856")); assertThat(curr.getFid(1), equalTo("fig|100226.1.peg.2857"));
        assertThat(iter.hasNext(), isTrue());
        curr = iter.next();
        assertThat(curr.getFid(0),equalTo("fig|83333.1.peg.3183")); assertThat(curr.getFid(1), equalTo("fig|83333.1.peg.3184"));
        assertThat(curr.getGenomeId(), equalTo("83333.1"));
        assertThat(iter.hasNext(), isFalse());
    }

}
