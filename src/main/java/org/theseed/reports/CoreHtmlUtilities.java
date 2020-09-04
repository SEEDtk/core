/**
 *
 */
package org.theseed.reports;

import static j2html.TagCreator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.theseed.genome.Feature;
import org.theseed.sequence.CharCounter;
import org.theseed.sequence.Sequence;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

/**
 * This is a static class that contains useful methods for generating web pages relating to CoreSEED genomes.
 *
 * @author Bruce Parrello
 *
 */
public class CoreHtmlUtilities {

    /**
     * @return the HTML for a list of a feature's subsystems
     *
     * @param feat		feature of interest
     */
    public static DomContent subsystemList(Feature feat) {
        Set<String> subsystems = feat.getSubsystems();
        DomContent retVal = HtmlUtilities.joinDelimited(subsystems.stream().map(x -> LinkObject.Core.subsystemLink(x)), " | ");
        return retVal;
    }

    /**
     * @return an alignment display table for a set of aligned sequences
     *
     * The sequences go in a table. The list is a row header with a hover title of the comment, and the
     * sequence is in the main column.  The alignment div is used to insure the table behaves properly.
     *
     * A summary row is placed along the bottom.  This row contains the consensus letter and displays statistics when clicked.
     * The two most common non-hyphen letters are given a colored background.
     *
     * @param aligned	list of sequences to display
     * @param scheme	coloring scheme to use
     */
    public static ContainerTag alignmentTable(List<Sequence> aligned, AlignColoring scheme) {
        // Compute the alignment width.
        int width = aligned.get(0).getSequence().length();
        // Prepare the sequences for counting.
        CharCounter.prepare(aligned);
        // Get counts and a consensus for every column.
        List<CharCounter.Count[]> allCounts = new ArrayList<CharCounter.Count[]>(width);
        StringBuilder consensus = new StringBuilder(width);
        CharCounter counter = new CharCounter();
        for (int i = 0; i < width; i++) {
            CharCounter.Count[] counts = counter.countSequences(aligned, i);
            consensus.append(counts[0].getTarget());
            allCounts.add(counts);
        }
        // We will add the consensus sequence at the end.  It requires a boatload of special processing.
        // In the meantime, we put the real sequences in the table.  These must be built into rendered
        // strings so we can mark the colored letters.  We use a huge buffer.
        StringBuilder buffer = new StringBuilder(width * 5);
        ContainerTag alignTable = table();
        for (Sequence seq : aligned) {
            String sequenceIn = seq.getSequence();
            buffer.setLength(0);
            for (int i = 0; i < width; i++)
                buffer.append(scheme.colorLetter(sequenceIn.charAt(i), allCounts.get(i)).render());
            // We make the sequence label the row header with the comment as a tooltip.  The HTML string is the content of the table cell.
            ContainerTag row = tr().with(th(seq.getLabel()).withTitle(seq.getComment())).with(td(rawHtml(buffer.toString())));
            alignTable.with(row);
        }
        // Now we add the consensus row.  This involves tooltips.
        buffer.setLength(0);
        for (int i = 0; i < width; i++) {
            // Here we compute the tooltip.  We describe the percentage for each character (up to three) and join them
            // together.
            String analysis = Arrays.stream(allCounts.get(i)).limit(3)
                    .map(x -> String.format("%c = %d%%", x.getTarget(), (x.getCount() * 100 + 50) / aligned.size()))
                    .collect(Collectors.joining(", "));
            // Embed the consensus character in the tooltip and add it to the buffer.
            DomContent conChar = span(rawHtml(Character.toString(consensus.charAt(i)) + span(analysis).withClass("tip").render())).withClass("tt");
            buffer.append(conChar.render());
        }
        ContainerTag consensusRow = tr().with(th("CONSENSUS")).with(td(rawHtml(buffer.toString())));
        alignTable.with(consensusRow);
        // Wrap it all up in a div.
        ContainerTag alignDiv = div(alignTable).withId("Aligned");
        return alignDiv;
    }

}
