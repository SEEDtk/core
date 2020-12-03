/**
 *
 */
package org.theseed.web;

import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.tr;

import java.util.Arrays;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

/**
 * This describes a standard table row.  It is the main workhorse for the HtmlTable object.
 */
public class Row<K extends Key & Comparable<K>> extends BaseRow<K> {

    /** array of column values */
    private CellContent[] cells;
    /** index of the next empty column in this cell */
    private int nextCol;

    /**
     * Create a new row for this table.
     *
     * @param key			key for the row
     * @param htmlTable 	parent table
     */
    public Row(HtmlTable<K> htmlTable, K key) {
        super(htmlTable, key);
        // Create the cells and denote they are empty.
        this.cells = new CellContent[this.getParent().getWidth()];
        Arrays.setAll(this.cells, i -> new CellContent(HtmlTable.EMPTY));
        this.nextCol = 0;
    }

    /**
     * Store an integer in a cell of this row.
     *
     * @param colIdx	target column index
     * @param num		integer to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> store(int colIdx, int num) {
        this.getParent().getColumn(colIdx).store(this.cells[colIdx], num);
        return this.adjust(colIdx);
    }

    /**
     * Insure the next-column index is updated past the specified column.
     *
     * @param colIdx	column just filled
     */
    private Row<K> adjust(int colIdx) {
        if (colIdx >= this.nextCol) this.nextCol = colIdx + 1;
        return this;
    }

    /**
     * Store a floating-point number in a cell of this row.
     *
     * @param colIdx	target column index
     * @param num		number to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> store(int colIdx, double num) {
        this.getParent().getColumn(colIdx).store(this.cells[colIdx], num);
        return this.adjust(colIdx);
    }

    /**
     * Store a string in a cell of this row.
     *
     * @param colIdx	target column index
     * @param text		string to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> store(int colIdx, String text) {
        // Note we have to insure that a blank cell is a non-breaking space.
        if (text == null || text.isEmpty()) {
            this.getParent().getColumn(colIdx).store(this.cells[colIdx], rawHtml("&nbsp;"));
        } else {
            this.getParent().getColumn(colIdx).store(this.cells[colIdx], text);
        }
        return this.adjust(colIdx);
    }

    /**
     * Store a boolean value in a cell of this row.  FALSE is an empty cell.
     * TRUE is a "Y".
     *
     * @param colIdx	target column index
     * @param flag		boolean flag to store
     */
    public Row<K> store(int colIdx, boolean flag) {
        return this.store(colIdx, (flag ? "Y" : ""));
    }

    /**
     * Store HTML content in a cell of this row.
     *
     * @param colIdx	target column index
     * @param html		content to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> store(int colIdx, DomContent html) {
        this.getParent().getColumn(colIdx).store(this.cells[colIdx], html);
        return this.adjust(colIdx);
    }

    /**
     * Store a floating-point number in the next cell of this row.
     *
     * @param num		number to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> add(double num) {
        return this.store(this.nextCol, num);
    }

    /**
     * Store an integer in the next cell of this row.
     *
     * @param num		number to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> add(int num) {
        return this.store(this.nextCol, num);
    }

    /**
     * Store a boolean value in the next cell of this row.
     *
     * @param flag		boolean to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> add(boolean flag) {
        return this.store(this.nextCol, flag);
    }

    /**
     * Store a string in the next cell of this row.
     *
     * @param text		string to store
     *
     * @return this row (to allow chaining)
     */
    public Row<K> add(String text) {
        return this.store(this.nextCol, text);
    }

    /**
     * Store HTML content in the next cell of this row.
     *
     * @param html		content to store
     *
     * @return this row (to allow chaining)
     */
     public Row<K> add(DomContent html) {
         return this.store(this.nextCol, html);
     }

     /**
      * Add the key to the next cell of this row.
      *
      * @return this row (to allow chaining)
      */
     public Row<K> addKey() {
         this.getKey().store(this.cells[this.nextCol], this.getParent().getColumn(this.nextCol));
         this.nextCol++;
         return this;
     }

     /**
      * @return the HTML for this row.  We simply output all the cells.
      */
     @Override
     public ContainerTag output() {
         return tr().with(Arrays.stream(this.cells).map(x -> x.output()));
     }

     /**
      * Highlight a cell.
      *
      * @param colIdx	column index of the cell to highlight
      */
     public void highlight(int colIdx) {
         this.cells[colIdx].highlight();
     }

}
