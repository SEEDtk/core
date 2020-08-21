/**
 *
 */
package org.theseed.reports;

import java.util.Set;

import org.theseed.genome.Feature;

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
}
