/**
 *
 */
package org.theseed.reports;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

import org.apache.commons.lang3.StringUtils;

/**
 * This class specifies a column in an HTML table.  A column specification defines how to format a table
 * cell in the column.  This is not an abstract class, but we expect it to be overridden for special
 * cases.  Some simple cases are provided as nested classes.
 *
 * @author Bruce Parrello
 *
 */
public class ColSpec {

    // FIELDS
    /** column title */
    private String title;
    /** floating-point format */
    private String format;
    /** styles to use for all cells */
    private String[] styles;
    /** tooltip text (if any) */
    private String tooltip;

    /**
     * Construct a column specification with a different floating-point format.
     * Most column specifications only need to call some version of this constructor.
     *
     * @param col_title		title of this column
     * @param fp_format		special format for floating-point values
     * @param baseStyles	permanent styles for this column
     */
    protected ColSpec(String col_title, String fp_format, String... baseStyles) {
        this.title = col_title;
        this.format = fp_format;
        this.styles = baseStyles;
        this.tooltip = null;
    }

    /**
     * Specify a tooltip for this column.  This is a fluent method, as it is expected to be used during definition.
     *
     * @param tip	text of the tooltip
     */
    public ColSpec setTip(String tip) {
        this.tooltip = tip;
        return this;
    }

    /**
     * Store an integer in a cell. This can be overridden to provide special styling.
     *
     * @param cell	target cell
     * @param num	integer to format
     */
    protected void store(CellContent cell, int num) {
       this.store(cell, rawHtml(Integer.toString(num)));
    }

    /**
     * Store a floating-point number in a cell.  This can be overridden to provide special styling.
     *
     * @param cell	target cell
     * @param num	floating-point nubmer to format
     */
    protected void store(CellContent cell, double num) {
        this.store(cell, text(String.format(this.format, num)));
    }

    /**
     * Store a string in a cell.  This can be overridden to provide special styling.
     *
     * @param cell	target cell
     * @param text	string to format
     */
    protected void store(CellContent cell, String text) {
        this.store(cell, text(text));
    }

    /**
     * Store html content in a cell.  This is called by all the other methods, so
     * cannot be overridden.
     *
     * @param html	content to format
     */
    protected final void store(CellContent cell, DomContent html) {
        cell.store(html);
        for (String style : this.styles) cell.addStyle(style);
    }

    /**
     * @return the title of this column
     */
    protected DomContent getTitle() {
        DomContent retVal = text(this.title);
        if (this.tooltip != null)
            retVal = CoreHtmlUtilities.toolTip(retVal, this.tooltip, "btip");
        return retVal;
    }

    /**
     * Apply the default styles of this column to a tag.  Existing styles will
     * be erased.
     *
     * @param html	the tag to which the styles should be applied
     *
     * @return the tag, for chaining
     */
    protected ContainerTag applyStyles(ContainerTag html) {
        if (this.styles.length > 0)
            html.withClass(StringUtils.join(this.styles, ' '));
        return html;
    }

    // SIMPLE SUBCLASSES

    /**
     * Simple numeric column.
     */
    public static class Num extends ColSpec {

        public Num(String title) {
            super(title, "%6.2f", "num");
        }

    }

    /**
     * Numeric column with fractional numbers.
     */
    public static class Fraction extends ColSpec {

        public Fraction(String title) {
            super(title, "%8.4f", "num");
        }

    }

    /**
     * Error count column.  Nonzero values are highlighted.
     */
    public static class ErrorCount extends Num {

        public ErrorCount(String title) {
            super(title);
        }

        @Override
        protected void store(CellContent cell, int num) {
            super.store(cell, num);
            cell.highlight(num > 0);
        }
    }

    /**
     * Required count column.  Zero values are highlighted.
     */
    public static class RequiredCount extends Num {

        public RequiredCount(String title) {
            super(title);
        }

        @Override
        protected void store(CellContent cell, int num) {
            super.store(cell, num);
            cell.highlight(num == 0);
        }
    }

    /**
     * String column.  Basically vanilla but with good defaults.
     */
    public static class Normal extends ColSpec {

        public Normal(String title) {
            super(title, "%6.2f");
        }

    }

    /**
     * String column with a fixed-width font.
     */
    public static class Aligned extends ColSpec {

        public Aligned(String title) {
            super(title, "%6.2f", "align");
        }
    }

    /**
     * Flag column.  Centered text.
     */
    public static class Flag extends ColSpec {

        public Flag(String title) {
            super(title, "%6.2f", "flag");
        }

    }

    /**
     * @return the tooltip text
     */
    protected String getTooltip() {
        return this.tooltip;
    }

}
