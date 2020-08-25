/**
 *
 */
package org.theseed.reports;

import static j2html.TagCreator.div;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.util.List;
import java.util.Set;

import org.theseed.genome.Feature;
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
     * The sequences go in a table, with the label as the row header with a hover title of the comment, and the
     * sequence in the main column.  The alignment div is used to insure the table behaves properly.
     *
     * A summary row is placed along the bottom.  This row contains the consensus letter and displays statistics when clicked.
     * The most common non-hyphen letter is given a colored background.
     *
     * @param aligned	list of sequences to display
     */
    // TODO coloring and counting
    public static ContainerTag alignmentTable(List<Sequence> aligned) {
        ContainerTag alignTable = table();
        for (Sequence seq : aligned) {
            ContainerTag row = tr().with(th(seq.getLabel()).withTitle(seq.getComment())).with(td(seq.getSequence()));
            alignTable.with(row);
        }
        ContainerTag alignDiv = div(alignTable).withId("Aligned");
        return alignDiv;
    }

}
