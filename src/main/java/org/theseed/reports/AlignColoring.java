/**
 *
 */
package org.theseed.reports;


import org.theseed.sequence.CharCounter;

import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This class represents a coloring scheme for an alignment.
 *
 * The main method takes as input the count results for the column and the current letter, and produces HTML.
 * The subclass determines the colors and the rules.
 *
 * @author Bruce Parrello
 *
 */
public abstract class AlignColoring {

    /**
     * @return the HTML for an alignment character with the proper coloring applied
     *
     * @param letter	letter to color
     * @param counts	character counts for this column
     */
    public DomContent colorLetter(char letter, CharCounter.Count[] counts) {
        DomContent retVal = text(Character.toString(letter));
        // Ask the subclass for the color.  NULL means no coloring is needed.
        Color rgb = this.computeColor(letter, counts);
        // Produce the colored letter.
        if (rgb != null)
            retVal = mark(retVal).withStyle("background-color: " + rgb.html());
        return retVal;
    }

    /**
     * @return the color for an alignment character with the proper coloring applied
     *
     * @param letter	letter to color
     * @param counts	character counts for this column
     */
    protected abstract Color computeColor(char letter, CharCounter.Count[] counts);

    /**
     * Color by consensus.  The most common character is light blue, the second most common
     * is light green.
     *
     * @author Bruce Parrello
     *
     */
    public static class Consensus extends AlignColoring {

        /** array of colors to use, by consensus position */
        private static Color[] COLORS = new Color[] { new Color(0.50, 0.73, 1.0),
                new Color(0.50, 1.0, 0.73), new Color(1.0, 0.73, 0.50) };

        @Override
        protected Color computeColor(char letter, CharCounter.Count[] counts) {
            Color retVal = null;
            for (int i = 0; retVal == null && i < COLORS.length; i++) {
                if (counts[i].getTarget() == letter)
                    retVal = COLORS[i];
            }
            return retVal;
        }

    }
}
