/**
 *
 */
package org.theseed.reports;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    // FIELDS
    /** map of file patterns to datalist IDs */
    private Map<String, String> dataListMap;

    public PageWriter() {
        // Insure the slash is in empty tags.
        Config.closeEmptyTags = true;
        // Use our custom escaper.
        Config.textEscaper = PageWriter::escape;
        // Initialize the datalist map.
        this.dataListMap = new HashMap<String, String>();
    }

    /**
     * This method writes the output.  The client passes in the body of the page, and the
     * subclass sends it to the standard output with the proper formatting.
     *
     * @param title		title to use if this is a standalone web page
     * @param content	array of content items to put on the page
     */
    public void writePage(String title, DomContent... content) {
        this.writePage(title, Arrays.stream(content));
    }

    /**
     * This method writes the output.  The client passes in the body of the page, and the
     * subclass sends it to the standard output with the proper formatting.
     *
     * @param title		title to use if this is a standalone web page
     * @param content	array of content items to put on the page
     */
    public void writePage(String title, List<DomContent> content) {
        this.writePage(title, content.stream());
    }

    /**
     * This method writes the output.  The client passes in the body of the page, and the
     * subclass sends it to the standard output with the proper formatting.
     *
     * @param title		title to use if this is a standalone web page
     * @param content	stream of content items to put on the page
     */
    protected abstract void writePage(String title, Stream<DomContent> stream);


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

    /**
     * Convert a URL to its local format.  This usually involves prefixing "SEEDtk" or some other name.
     *
     * @param url		URL to convert
     *
     * @return the prefixed URL
     */
    public abstract String local_url(String url);

    /**
     * @return the ID of the data list corresponding to a type string, or NULL if there is none
     *
     * @param type string	type string to check
     */
    public String checkDataList(String type_string) {
        return this.dataListMap.get(type_string);
    }

    /**
     * Store a data list ID for a type string
     *
     * @param type_string	type string of interest
     * @param list_id		associated data list ID
     */
    public void putDataList(String type_string, String list_id) {
        this.dataListMap.put(type_string, list_id);
    }

    /**
     * @return the next available ID for a data list
     */
    public String getListID() {
        return String.format("_data_list_%04X", this.dataListMap.size());
    }

    /**
     * Custom method for escaping text that leaves single quotes alone.
     *
     * @param s		string to escape
     * @return		the HTML-escaped string
     */
    public static String escape(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder escapedText = new StringBuilder();
        char currentChar;
        for (int i = 0; i < s.length(); i++) {
            currentChar = s.charAt(i);
            switch (currentChar) {
                case '<':
                    escapedText.append("&lt;");
                    break;
                case '>':
                    escapedText.append("&gt;");
                    break;
                case '&':
                    escapedText.append("&amp;");
                    break;
                case '"':
                    escapedText.append("&quot;");
                    break;
                default:
                    escapedText.append(currentChar);
            }
        }
        return escapedText.toString();
    }

}
