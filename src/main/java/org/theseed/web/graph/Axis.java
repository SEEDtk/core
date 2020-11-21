/**
 *
 */
package org.theseed.web.graph;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * This object represents the axis parameters for a scatter graph.  It will accumulate the
 * minimum and maximum input values, and it provides methods for converting point values to
 * canvas coordinates.
 *
 * @author Bruce Parrello
 *
 */
public abstract class Axis {

    // FIELDS
    /** minimum point value */
    private double min;
    /** maximum point value */
    private double max;
    /** coordinate value for axis origin */
    private int origin;
    /** coordinate value for axis maximum */
    private int terminus;
    /** axis label */
    private String label;
    /** scale factor for converting values to coordinates */
    private double scale;
    /** offset for converting values to coordinates */
    private double offset;
    /** format for tick labels */
    private String tickFormat;
    /** array of grid gap factors */
    private static final double[] GAP_FACTORS = new double[] { 50.0, 20.0, 10.0, 5.0, 2.0, 1.0 };
    /** pixel recommendation for grid gaps */
    private static final double GAP_PIXELS = 100.0;


    /**
     * Specify the parameters of an axis.
     *
     * @param label			label to put on the axis
     * @param origin		coordinate point for the axis origin
     * @param terminus		coordinate point for the axis terminus
     */
    public Axis(String label, int origin, int terminus) {
        this.label = label;
        this.origin = origin;
        this.terminus = terminus;
        this.min = Double.MAX_VALUE;
        this.max = -Double.MAX_VALUE;
    }

    /**
     * Record a point value.
     *
     * @param v		point value
     */
    public void point(double v) {
        if (v < this.min) this.min = v;
        if (v > this.max) this.max = v;
    }

    /**
     * Compute the scaling factor for this axis and plot the grid lines.
     *
     * @param other		Axis object for the other axis
     * @param graph		parent graph
     */
    public void initialize(Axis other, IGraph graph) {
        // First, we compute the average grid gap.  We need the axis length in coordinates (pixels).
        int axisLength = Math.abs(this.terminus - this.origin);
        double axisRange = this.max - this.min;
        // This is an initial stab at the unit value required to consume 100 pixels.
        double possibleGridGap = GAP_PIXELS * axisRange / (double) axisLength;
        // We need to round the gap to a reasonable number.
        double precision = Math.round(Math.log10(possibleGridGap));
        double p = Math.pow(10.0, precision - 1);
        OptionalDouble gridGapGuess = Arrays.stream(GAP_FACTORS).filter(x -> (p * x <= possibleGridGap)).findFirst();
        double gridGap = (gridGapGuess.isPresent() ? gridGapGuess.getAsDouble() : 1.0) * p;
        // Now round the min and max to a grid gap boundary.
        this.min = Math.floor(this.min / gridGap) * gridGap;
        this.max = Math.ceil(this.max / gridGap) * gridGap;
        axisRange = this.max - this.min;
        // Create the tick format.
        int iPrecision = (int) precision;
        if (iPrecision < -6 || iPrecision > 6)
            this.tickFormat = "%g";
        else if (iPrecision < 0)
            this.tickFormat = "%" + String.format("%d.%d", -iPrecision, -iPrecision) + "f";
        else
            this.tickFormat = "%f";
        // Compute the scale factor and offset.
        this.scale = (this.terminus - this.origin) / axisRange;
        this.offset = this.terminus - this.scale * this.max;
        // Now plot the grid lines.
        this.labelTick(this.min, graph, other.origin);
        for (double tick = this.min + gridGap; tick <= this.max; tick += gridGap) {
            this.drawGridLine(tick, graph, other);
            this.labelTick(tick, graph, other.origin);
        }
        // Finally, display the label.
        this.showLabel(other.origin, graph);
    }

    /**
     * @return the coordinate along this axis for a point
     *
     * @param v		point value
     */
    public int getPoint(double v) {
        return (int) Math.round(this.scale * v + this.offset);
    }

    /**
     * @return a formatted tick value
     *
     * @param tick		tick value to format
     */
    protected String getFormatted(double tick) {
        return String.format(this.tickFormat, tick);
    }

    /**
     * Display the label for this axis.
     *
     * @param origin2	origin point for the other axis
     * @param graph		graph object for rendering
     */
    protected abstract void showLabel(int origin2, IGraph graph);

    /**
     * Label a tick point on this axis.
     *
     * @param tick		tick point
     * @param graph		graph object for plotting the label
     * @param origin2	origin coordinate for the other axis
     */
    protected abstract void labelTick(double tick, IGraph graph, int origin2);

    /**
     * Draw a grid line from this axis.
     *
     * @param tick		tick point
     * @param graph		graph object for drawing the line
     * @param other		descriptor for the other axis
     */
    protected abstract void drawGridLine(double tick, IGraph graph, Axis other);

    /**
     * Subclass for X axis
     */
    public static class X extends Axis {

        public X(String label, int origin, int terminus) {
            super(label, origin, terminus);
        }

        @Override
        protected void labelTick(double tick, IGraph graph, int origin2) {
            int p = this.getPoint(tick);
            int r = origin2 + 5;
            graph.showHCText("label", p, r, this.getFormatted(tick));
        }

        @Override
        protected void drawGridLine(double tick, IGraph graph, Axis other) {
            int p = this.getPoint(tick);
            graph.drawLine("grid", p, other.getOrigin(), p, other.getTerminus());
        }

        @Override
        protected void showLabel(int origin2, IGraph graph) {
            int y = origin2 + 20;
            int x = (this.getTerminus() + this.getOrigin()) / 2;
            graph.showHCText("label", x, y, this.getLabel());
        }

    }

    /**
     * Subclass for Y axis
     */
    public static class Y extends Axis {

        public Y(String label, int origin, int terminus) {
            super(label, origin, terminus);
        }

        @Override
        protected void labelTick(double tick, IGraph graph, int origin2) {
            int p = this.getPoint(tick);
            int r = origin2 - 5;
            graph.showRText("label", r, p, this.getFormatted(tick));
        }

        @Override
        protected void drawGridLine(double tick, IGraph graph, Axis other) {
            int p = this.getPoint(tick);
            graph.drawLine("grid", other.getOrigin(), p, other.getTerminus(), p);
        }

        @Override
        protected void showLabel(int origin2, IGraph graph) {
            int x = origin2 - 50;
            int y = (this.getTerminus() + this.getOrigin()) / 2;
            graph.showVText("label", x, y, this.getLabel());
        }

    }

    /**
     * @return the origin
     */
    protected int getOrigin() {
        return this.origin;
    }

    /**
     * @return the terminus
     */
    protected int getTerminus() {
        return this.terminus;
    }

    /**
     * @return the label
     */
    protected String getLabel() {
        return this.label;
    }

}
