/**
 *
 */
package org.theseed.web;

import j2html.tags.ContainerTag;

/**
 * This is the base class for a table row.
 *
 * @author Bruce Parrello
 *
 * @param <K>
 */
public abstract class BaseRow<K extends Key & Comparable<K>> implements Comparable<BaseRow<K>> {

    /** parent HTML table */
    private final HtmlTable<K> parentTable;
    /** original row index */
    private int idx;
    /** row key string */
    private K key;

    /**
     * Create this row.
     *
     * @param htmlTable		parent table
     * @param key			initial row key
     */
    public BaseRow(HtmlTable<K> htmlTable, K key) {
        this.parentTable = htmlTable;
        this.key = key;
        // Note the row index is 1-based.
        this.idx = parentTable.getHeight() + 1;
        // Add the row to the table.
        this.getParent().addRow(this);
    }

    /**
     * @return the key
     */
    protected K getKey() {
        return this.key;
    }

    /**
     * Store a new key.
     *
     * @param key the key to set
     */
    protected void setKey(K key) {
        this.key = key;
    }

    /**
     * @return the parent table
     */
    protected HtmlTable<K> getParent() {
        return this.parentTable;
    }

    /**
     * Rows are compared by key followed by original index.
     */
    @Override
    public int compareTo(BaseRow<K> o) {
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
        if (!(obj instanceof Row)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        BaseRow other = (BaseRow) obj;
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
     * @return the HTML for this table row
     */
    protected abstract ContainerTag output();

}
