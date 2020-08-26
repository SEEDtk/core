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
        assertThat(table, equalTo("<div id=\"Aligned\"><table><tr><th title=\"\">seq1</th><td><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #FFBA80\">A</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #FFBA80\">T</mark><mark style=\"background-color: #80FFBA\">T</mark><mark style=\"background-color: #80FFBA\">T</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80BAFF\">G</mark>G<mark style=\"background-color: #FFBA80\">G</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #FFBA80\">-</mark></td></tr><tr><th title=\"\">seq2</th><td><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #80BAFF\">T</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #80FFBA\">G</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #FFBA80\">-</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80FFBA\">T</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #FFBA80\">G</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #80BAFF\">A</mark></td></tr><tr><th title=\"\">seq3</th><td><mark style=\"background-color: #FFBA80\">-</mark><mark style=\"background-color: #80FFBA\">-</mark><mark style=\"background-color: #80FFBA\">A</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">T</mark><mark style=\"background-color: #FFBA80\">G</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">T</mark>G<mark style=\"background-color: #FFBA80\">-</mark><mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">T</mark></td></tr><tr><th title=\"\">seq4</th><td><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">A</mark>G<mark style=\"background-color: #FFBA80\">A</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">G</mark>T<mark style=\"background-color: #80FFBA\">G</mark>G<mark style=\"background-color: #FFBA80\">A</mark><mark style=\"background-color: #80FFBA\">A</mark>A<mark style=\"background-color: #FFBA80\">A</mark><mark style=\"background-color: #80BAFF\">A</mark><mark style=\"background-color: #FFBA80\">A</mark><mark style=\"background-color: #80BAFF\">A</mark></td></tr><tr><th title=\"\">seq5</th><td>GT<mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #FFBA80\">A</mark>GT<mark style=\"background-color: #80BAFF\">-</mark><mark style=\"background-color: #FFBA80\">A</mark><mark style=\"background-color: #80FFBA\">G</mark><mark style=\"background-color: #80FFBA\">T</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">A</mark><mark style=\"background-color: #80BAFF\">G</mark>T<mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #FFBA80\">A</mark>G</td></tr><tr><th title=\"\">seq6</th><td><mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #FFBA80\">C</mark><mark style=\"background-color: #80FFBA\">C</mark>C<mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #FFBA80\">C</mark>C<mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #80BAFF\">C</mark>C</td></tr><tr><th title=\"\">seq7</th><td><mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #FFBA80\">T</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80BAFF\">T</mark><mark style=\"background-color: #80BAFF\">C</mark>T<mark style=\"background-color: #80FFBA\">C</mark><mark style=\"background-color: #FFBA80\">T</mark><mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">T</mark><mark style=\"background-color: #FFBA80\">C</mark><mark style=\"background-color: #80FFBA\">T</mark><mark style=\"background-color: #80FFBA\">C</mark>T<mark style=\"background-color: #80BAFF\">C</mark><mark style=\"background-color: #80FFBA\">T</mark></td></tr><tr><th>CONSENSUS</th><td><span class=\"tt\">A<span class=\"tip\">A = 50%, C = 35%, - = 21%</span></span><span class=\"tt\">C<span class=\"tip\">C = 50%, - = 35%, A = 21%</span></span><span class=\"tt\">C<span class=\"tip\">C = 64%, A = 35%, T = 21%</span></span><span class=\"tt\">C<span class=\"tip\">C = 50%, - = 35%, A = 21%</span></span><span class=\"tt\">T<span class=\"tip\">T = 50%, C = 35%, A = 21%</span></span><span class=\"tt\">C<span class=\"tip\">C = 64%, - = 21%, G = 21%</span></span><span class=\"tt\">-<span class=\"tip\">- = 50%, G = 35%, C = 21%</span></span><span class=\"tt\">-<span class=\"tip\">- = 50%, C = 35%, A = 21%</span></span><span class=\"tt\">A<span class=\"tip\">A = 35%, G = 35%, T = 35%</span></span><span class=\"tt\">C<span class=\"tip\">C = 50%, T = 35%, - = 21%</span></span><span class=\"tt\">C<span class=\"tip\">C = 50%, T = 50%, A = 21%</span></span><span class=\"tt\">-<span class=\"tip\">- = 35%, A = 35%, C = 35%</span></span><span class=\"tt\">G<span class=\"tip\">G = 35%, T = 35%, - = 21%</span></span><span class=\"tt\">-<span class=\"tip\">- = 35%, C = 35%, A = 21%</span></span><span class=\"tt\">A<span class=\"tip\">A = 35%, C = 35%, G = 35%</span></span><span class=\"tt\">C<span class=\"tip\">C = 50%, - = 35%, A = 35%</span></span><span class=\"tt\">A<span class=\"tip\">A = 35%, T = 35%, - = 21%</span></span></td></tr></table></div>"));
    }

}
