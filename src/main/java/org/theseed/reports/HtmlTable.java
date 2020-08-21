/**
 *
 */
package org.theseed.reports;

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
    private SortedSet<Row> rows;
    /** HTML for an empty cell */
    private static final DomContent EMPTY = rawHtml("&nbsp;");

    /**
     * This is a nested class describing a table row.  It is the main workhorse for this
     * object.
     */
    public class Row implements Comparable<Row> {

        // FIELDS
        /** original row index */
        private int idx;
        /** row key string (this cannot be changed after construction */
        private final K key;
        /** array of column values */
        private CellContent[] cells;
        /** index of the next empty column in this cell */
        private int nextCol;

        /**
         * Rows are compared by key followed by original index.
         */
        @Override
        public int compareTo(Row o) {
            int retVal = this.key.compareTo(o.key);
            if (retVal == 0)
                retVal = this.idx - o.idx;
            return retVal;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.idx;
            result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
            return result;
        }

        // The generic keys make this really creaky.  We only expect
        // to compare rows in the same table.
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof HtmlTable.Row)) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            HtmlTable.Row other = (HtmlTable.Row) obj;
            if (this.idx != other.idx) {
                return false;
            }
            if (this.key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!this.key.equals(other.key)) {
                return false;
            }
            return true;
        }

        /**
         * Create a new row for this table.
         *
         * @param key	key string for the row
         */
        public Row(K key) {
            this.key = key;
            // Note the row index is 1-based.
            this.idx = HtmlTable.this.rows.size() + 1;
            // Create the cells and denote they are empty.
            this.cells = new CellContent[HtmlTable.this.getWidth()];
            Arrays.setAll(this.cells, i -> new CellContent(EMPTY));
            this.nextCol = 0;
            // Add the row to the table.
            HtmlTable.this.rows.add(this);
        }

        /**
         * Store an integer in a cell of this row.
         *
         * @param colIdx	target column index
         * @param num		integer to store
         *
         * @return this row (to allow chaining)
         */
        public Row store(int colIdx, int num) {
            HtmlTable.this.columns[colIdx].store(this.cells[colIdx], num);
            return this.adjust(colIdx);
        }

        /**
         * Insure the next-column index is updated past the specified column.
         *
         * @param colIdx	column just filled
         */
        private Row adjust(int colIdx) {
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
        public Row store(int colIdx, double num) {
            HtmlTable.this.columns[colIdx].store(this.cells[colIdx], num);
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
        public Row store(int colIdx, String text) {
            // Note we have to insure that a blank cell is a non-breaking space.
            if (text == null || text.isEmpty()) {
                HtmlTable.this.columns[colIdx].store(this.cells[colIdx], rawHtml("&nbsp;"));
            } else {
                HtmlTable.this.columns[colIdx].store(this.cells[colIdx], text);
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
        public Row store(int colIdx, boolean flag) {
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
        public Row store(int colIdx, DomContent html) {
            HtmlTable.this.columns[colIdx].store(this.cells[colIdx], html);
            return this.adjust(colIdx);
        }

        /**
         * Store a floating-point number in the next cell of this row.
         *
         * @param num		number to store
         *
         * @return this row (to allow chaining)
         */
        public Row add(double num) {
            return this.store(this.nextCol, num);
        }

        /**
         * Store an integer in the next cell of this row.
         *
         * @param num		number to store
         *
         * @return this row (to allow chaining)
         */
        public Row add(int num) {
            return this.store(this.nextCol, num);
        }

        /**
         * Store a boolean value in the next cell of this row.
         *
         * @param flag		boolean to store
         *
         * @return this row (to allow chaining)
         */
        public Row add(boolean flag) {
            return this.store(this.nextCol, flag);
        }

        /**
         * Store a string in the next cell of this row.
         *
         * @param text		string to store
         *
         * @return this row (to allow chaining)
         */
        public Row add(String text) {
            return this.store(this.nextCol, text);
        }

        /**
         * Store HTML content in the next cell of this row.
         *
         * @param html		content to store
         *
         * @return this row (to allow chaining)
         */
         public Row add(DomContent html) {
             return this.store(this.nextCol, html);
         }

         /**
          * Add the key to the next cell of this row.
          *
          * @return this row (to allow chaining)
          */
         public Row addKey() {
             this.key.store(this.cells[this.nextCol], HtmlTable.this.columns[this.nextCol]);
             this.nextCol++;
             return this;
         }

         /**
          * @return the HTML for this row.  We simply output all the cells.
          */
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

    /**
     * Construct a new table.
     *
     * @param cols	column specifications
     */
    public HtmlTable(ColSpec... cols) {
        this.columns = cols;
        this.rows = new TreeSet<HtmlTable<K>.Row>();
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

}
