/**
 *
 */
package org.theseed.reports;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.theseed.sequence.CharCounter;
import org.theseed.sequence.FastaInputStream;
import org.theseed.sequence.Sequence;

/**
 * @author Bruce Parrello
 *
 */
public class TestAlignColoring extends TestCase {

    public void testColoring() throws IOException {
        File alignFile = new File("data", "alignCounts.fa");
        List<Sequence> alignment = FastaInputStream.readAll(alignFile);
        CharCounter.prepare(alignment);
        CharCounter counter = new CharCounter();
        CharCounter.Count[] counts = counter.countSequences(alignment, 0);
        AlignColoring coloring = new AlignColoring.Consensus();
        String checkA = coloring.colorLetter('A', counts).render();
        String checkC = coloring.colorLetter('C', counts).render();
        String checkM = coloring.colorLetter('-', counts).render();
        String checkG = coloring.colorLetter('G', counts).render();
        String checkT = coloring.colorLetter('T', counts).render();
        String allCheck = checkA + checkC + checkM + checkG + checkT;
        assertThat(allCheck, equalTo("<mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #FFBA80\">-</mark>GT"));
    }

    public void testAlignmentTable() throws IOException {
        File alignFile = new File("data", "alignCounts.fa");
        List<Sequence> alignment = FastaInputStream.readAll(alignFile);
        AlignColoring coloring = new AlignColoring.Consensus();
        String table = CoreHtmlUtilities.alignmentTable(alignment, coloring).render();
        assertThat(table, equalTo(""));
    }

}
