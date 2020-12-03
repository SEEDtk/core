/**
 *
 */
package org.theseed.web;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This class is used to create HTML tables.  The intent is to provide a framework that can
 * be converted at some future point to create a sortable, filterable table with javascript
 * support.  For now it creates static HTML.
 *
 * The table is built by adding cell objects in columns.  A column can be numeric or text,
 * which determines its alignment.  Cells can be individually highlighted, though error
 * counts are highlighted automatically.  Each cell can be filled with an integer, an error
 * count, a floating-point number, a boolean ("Y" or blank), text, or full-blown HTML.
 *
 * The column specification indicates the type of content in the column.  This must be constant
 * to allow for filtering and sorting in the future.
 *
 * The type parameter indicates the object to be used as table keys.
 *
 * @author Bruce Parrello
 *
 */
public class HtmlTable<K extends Key & Comparable<K>> {

    // FIELDS
    /** array of column specifiers defining the table */
    private ColSpec[] columns;
    /** list of table rows */
    private SortedSet<BaseRow<K>> rows;
    /** HTML for an empty cell */
    protected static final DomContent EMPTY = rawHtml("&nbsp;");

    /**
     * Construct a new table.
     *
     * @param cols	column specifications
     */
    public HtmlTable(ColSpec... cols) {
        this.columns = cols;
        this.rows = new TreeSet<BaseRow<K>>();
    }

    /**
     * @return the specified column specification
     *
     * @param colIdx	index of the desired column
     */
    protected ColSpec getColumn(int colIdx) {
        return this.columns[colIdx];
    }

    /**
     * Change the key of a row.
     *
     * @param row	row of interest
     * @param key	new key value
     */
    public void moveRow(BaseRow<K> row, K key) {
        this.rows.remove(row);
        row.setKey(key);
        this.rows.add(row);
    }

    /**
     * Add a row to this table.
     *
     * @param row	new row to add
     */
    protected void addRow(BaseRow<K> row) {
        this.rows.add(row);
    }


    /**
     * @return the number of columns in this table
     */
    public int getWidth() {
        return this.columns.length;
    }

    /**
     * @return the html for the entire table
     */
    public ContainerTag output() {
        ContainerTag retVal = table();
        // Create the headers.
        retVal.with(tr().with(Arrays.stream(this.columns).map(c -> c.applyStyles(th(c.getTitle())))));
        // Add the rows.
        retVal.with(this.rows.stream().map(r -> r.output()));
        // Return the table.
        return retVal;
    }

    /**
     * @return the number of rows in the table (excluding the header)
     */
    public int getHeight() {
        return this.rows.size();
    }

    /**
     * Put row numbers in the specified column of the table.  Only standard rows are numbered.
     *
     * @param i		column to use for numbering (usually 0)
     */
    public void setIndexColumn(int i) {
        int r = 1;
        for (BaseRow<K> row : rows)
            if (row instanceof Row<?>)
                ((Row<K>) row).store(i, r++);
    }

}
