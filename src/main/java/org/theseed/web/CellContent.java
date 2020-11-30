/**
 *
 */
package org.theseed.web;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This object represents the content of a table cell.  It specifies the HTML content of the cell and has a set
 * of styles for rendering.
 *
 * @author Bruce Parrello
 *
 */
public class CellContent {

    // FIELDS
    /** styles for this cell */
    private Set<String> styles;
    /** content of the cell */
    private DomContent content;

    /**
     * Construct new cell content.
     *
     * @param html	content for the cell
     */
    public CellContent(DomContent html) {
        this.content = html;
        // We don't care about sorting, but for small sets, trees perform better
        this.styles = new TreeSet<String>();
    }

    /**
     * Add a style to this cell.
     *
     * @param style		style to add
     */
    public CellContent addStyle(String style) {
        this.styles.add(style);
        return this;
    }

    /**
     * Clear the styles on this cell.
     */
    public CellContent clear() {
        this.styles.clear();
        return this;
    }

    /**
     * Highlight this cell.
     */
    public CellContent highlight() {
        this.styles.add("highlight");
        return this;
    }

    /**
     * Highlight this cell or de-highlight it according to a boolean condition.
     *
     * @param flag	condition under which to highlight the cell
     */
    public CellContent highlight(boolean flag) {
        if (flag) {
            this.highlight();
        } else {
            this.styles.remove("highlight");
        }
        return this;
    }

    /**
     * Store content in this cell.
     *
     * @param html	content to store
     */
    protected void store(DomContent html) {
        this.content = html;
    }

    /**
     * Render this cell as a row cell.
     */
    protected ContainerTag output() {
        ContainerTag cell = td(this.content);
        if (! this.styles.isEmpty())
            cell.withClass(StringUtils.join(this.styles, ' '));
        return cell;
    }


}
