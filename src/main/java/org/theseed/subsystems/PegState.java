/**
 *
 */
package org.theseed.subsystems;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

import org.theseed.utils.IDescribable;

/**
 * This enumeration represents the different possible states of a peg in a subsystem spreadsheet cell.  Our main
 * goal is to be able to display the peg in the proper format.
 *
 * @author Bruce Parrello
 *
 */
public enum PegState implements IDescribable {
    GOOD("normal", "Feature exists and has the correct role."),
    MISSING("missing", "Feature does not exist in the database."),
    DISCONNECTED("disconnected", "Feature has the correct role but is not in the subsystem."),
    BAD_ROLE("bad", "Feature is in the subsystem but has the incorrect role.");

    /** coreSEED URL format */
    private static final String CORE_PEG_URL = "https://core.theseed.org/FIG/seedviewer.cgi?page=Annotation;feature=%s";

    // FIELDS
    private String cssClass;
    private String description;

    private PegState(String cssClass, String description) {
        this.cssClass = cssClass;
        this.description = description;
    }
    /**
     * Create the display string for the specified feature appropriate to this type.
     *
     * @param pegId		ID of the peg to display
     *
     * @return the HTML to display the peg
     */
    public ContainerTag display(String pegId) {
        ContainerTag retVal;
        if (this == MISSING) {
            // Here we can't link to the peg.
            retVal = span(pegId);
        } else {
            retVal = fidLink(pegId);
        }
        return this.format(retVal);
    }
    /**
     * @return a feature link
     *
     * @param pegId		ID of the feature to form the link
     */
    public static ContainerTag fidLink(String pegId) {
        return a(pegId).withHref(String.format(CORE_PEG_URL, pegId)).withTarget("_blank");
    }

    /**
     * Format a container tab with this state's class.
     *
     * @param	container to format
     */
    public ContainerTag format(ContainerTag container) {
        return container.withClass(this.cssClass);
    }

    /**
     * Format a description of this state.
     */
    public ContainerTag describe() {
        ContainerTag retVal;
        if (this == MISSING)
            retVal = span(this.description);
        else
            retVal = a(this.description).withName("_state_" + this.name());
        return this.format(retVal);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

}
