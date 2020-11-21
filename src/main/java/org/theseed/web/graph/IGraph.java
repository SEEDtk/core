/**
 *
 */
package org.theseed.web.graph;

/**
 * Interface for graphing objects.  It contains methods useful for type-agnostic objects such as Axis.
 *
 * @author Bruce Parrello
 *
 */
public interface IGraph {

    /**
     * Draw a line.
     *
     * @param style		line style
     * @param x1		x-point of origin
     * @param y1		y-point of origin
     * @param x2		x-point of terminus
     * @param y2		y-point of terminus
     */
    public void drawLine(String style, int x1, int y1, int x2, int y2);

    /**
     * Display a line of text horizontally.
     *
     * @param style		text style
     * @param x			x-point of text center
     * @param y			y-point of text top
     * @param text		text to display
     */
    public void showHCText(String style, int x, int y, String text);

    /**
     * Display a line of text vertically
     *
     * @param style		text style
     * @param x			x-point of text origin
     * @param y			y-point of text center
     * @param text		text to display
     */
    public void showVText(String style, int x, int y, String text);

    /**
     * Display a line of text to the right of a point
     *
     * @param style		text style
     * @param x			x-point of text origin
     * @param y			y-point of text center
     * @param text		text to display
     */
    public void showRText(String style, int x, int y, String text);

}
