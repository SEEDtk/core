/**
 *
 */
package org.theseed.reports;

import java.util.Collection;


import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;
import j2html.Config;

/**
 * This is the base class for writing web pages.  It contains the basic utilities for web output,
 * and can be subclassed to produced a custom prefix and suffix.
 *
 * @author Bruce Parrello
 *
 */
public abstract class PageWriter {

    public PageWriter() {
        // Insure the slash is in empty tags.
        Config.closeEmptyTags = true;
    }
    /**
     * This method writes the output.  The client passes in the body of the page, and the
     * subclass sends it to the standard output with the proper formatting.
     *
     * @param title		title to use if this is a standalone web page
     * @param content	array of content items to put on the page
     */
    public abstract void writePage(String title, DomContent... content);

    /**
     * This enum indicates the types of output.
     */
    public static enum Type {
        INTERNAL, SEEDTK;

        public PageWriter create() {
            PageWriter retVal = null;
            switch (this) {
            case INTERNAL :
                retVal = new InternalPageWriter();
                break;
            case SEEDTK :
                retVal = new SeedTkPageWriter();
                break;
            }
            return retVal;
        }
    }

    /**
     * Create a highlight block.  This is a large foreground area in the web page.
     *
     * @param items		items to be included
     *
     * @return the highlight block container
     */
    public ContainerTag highlightBlock(DomContent... items) {
        return div().withId("Pod").with(items);
    }

    /**
     * Create a scroll block.  This is a confined scrollable area in the web page.
     *
     * @param items		items to be included
     *
     * @return the scroll block container
     */
    public ContainerTag scrollBlock(DomContent... items) {
        return div().withClass("wide").with(items);
    }

    /**
     * Create a highlight block.  This is a large foreground area in the web page.
     *
     * @param items		items to be included
     *
     * @return the highlight block container
     */
    public ContainerTag highlightBlock(Collection<DomContent> items) {
        return div().withId("Pod").with(items);
    }

    /**
     * Create a document block with a linkable heading.
     *
     * @param linkName		linkable name for the section
     * @param title			title for the section
     * @param content		content for the section
     *
     * @return the document subsection
     */
    public ContainerTag subSection(String linkName, String title, DomContent... content) {
        ContainerTag retVal = div().with(h2(a(title).withName(linkName))).with(content);
        return retVal;
    }

}
