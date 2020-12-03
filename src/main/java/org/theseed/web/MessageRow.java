/**
 *
 */
package org.theseed.web;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This is a special table row that spans the entire width of the table.  It is used in forms for messages and to break up
 * the form, but it can conceivably be used for other purposes.
 *
 * @author Bruce Parrello
 *
 */
public class MessageRow<K extends Key & Comparable<K>> extends BaseRow<K> {

    // FIELDS
    /** Html content of the row */
    private DomContent content;

    /**
     * Create the message row.
     *
     * @param htmlTable		parent table
     * @param key			key for this row
     */
    public MessageRow(HtmlTable<K> htmlTable, K key) {
        super(htmlTable, key);
        this.content = rawHtml("&nbsp;");
    }

    /**
     * Specify the content of the message row.
     *
     * @param html		html to display in the message row
     */
    public MessageRow<K> store(DomContent html) {
        this.content = html;
        return this;
    }

    @Override
    protected ContainerTag output() {
        return tr(td(this.content).attr("colspan", this.getParent().getWidth()));
    }

}
