/**
 *
 */
package org.theseed.web.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.io.TabbedLineReader;
import org.theseed.reports.Color;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A scatter graph is an SVG object that draws a scatter graph.  The client passes in labels, predicted values,
 * and actual values.  These are converted into dots on the scatter graph.
 *
 * @author Bruce Parrello
 *
 */
public class ScatterGraph implements IGraph {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(ScatterGraph.class);
    /** SVG tag */
    private ContainerTag container;
    /** coordinate width */
    private int width;
    /** coordinate height */
    private int height;
    /** top margin */
    private int topMargin;
    /** right margin */
    private int rightMargin;
    /** bottom margin */
    private int botMargin;
    /** left margin */
    private int leftMargin;
    /** circle radius */
    private int radius;
    /** circle fill */
    private Color circleFill;
    /** x-axis parameters */
    private Axis.X xAxis;
    /** y-axis parameters */
    private Axis.Y yAxis;
    /** collection of data points */
    private List<Point> points;

    /**
     * This is a tiny class to represent a data point.
     */
    private static class Point {
        private double x;
        private double y;
        String label;

        /**
         * Create a point.
         *
         * @param label		label of the point
         * @param x			x-value of the point
         * @param y			y-value of the point
         */
        public Point(String label, double x, double y) {
            this.label = label;
            this.x = x;
            this.y = y;
        }

        /**
         * @return the x-value
         */
        protected double getX() {
            return this.x;
        }

        /**
         * @return the y-value
         */
        protected double getY() {
            return this.y;
        }

        /**
         * @return the point label
         */
        protected String getLabel() {
            return this.label;
        }

    }

    /**
     * Create the canvas.
     */
    public ScatterGraph(int width, int height, String style, int topMargin, int rightMargin, int botMargin, int leftMargin) {
        this.width = width;
        this.height = height;
        this.topMargin = topMargin;
        this.rightMargin = rightMargin;
        this.botMargin = botMargin;
        this.leftMargin = leftMargin;
        // Set the circle defaults;
        this.radius = 4;
        this.circleFill = Color.BLUE;
        // Create the canvas.
        this.container = new ContainerTag("svg").withClass(style).attr("viewBox", String.format("0 0 %d %d", width, height));
        // Draw the axes.
        int xAxisY = this.height - this.botMargin;
        this.drawLine("axis", this.leftMargin, xAxisY, this.width - this.rightMargin, xAxisY);
        this.drawLine("axis", this.leftMargin, xAxisY, this.leftMargin, this.topMargin);
        this.points = new LinkedList<Point>();
    }

    /**
     * Draw a line on the canvas.
     *
     * @param style		style of the line
     * @param x1		x-coordinate of origin
     * @oaram y1		y-coordinate of origin
     * @param x2		x-coordinate of terminus
     * @oaram y2		y-coordinate of terminus
     */
    public void drawLine(String style, int x1, int y1, int x2, int y2) {
        this.container.with(new ContainerTag("line").withClass(style)
                .attr("x1", x1).attr("y1", y1).attr("x2", x2).attr("y2", y2));
    }

    /**
     * @return the container tag for the graph.
     */
    public ContainerTag getHtml() {
        return this.container;
    }

    @Override
    public void showHCText(String style, int x, int y, String text) {
        showText(x, y, text, "middle", "hanging");
    }

    /**
     * Display horizontal text with the specified anchor type.
     *
     * @param x			x-anchor of text
     * @param y			y-origin of text
     * @param text		text to display
     * @param anchor	type of anchor
     * @param baseline	type of baseline
     */
    private void showText(int x, int y, String text, String anchor, String baseline) {
        ContainerTag textTag = new ContainerTag("text").attr("x", x).attr("y", y)
                .attr("text-anchor", anchor).attr("alignment-baseline", baseline)
                .with(text(text));
        this.container.with(textTag);
    }

    @Override
    public void showVText(String style, int x, int y, String text) {
        ContainerTag textTag = new ContainerTag("text").attr("text-anchor", "middle")
                .attr("alignment-baseline", "middle")
                .attr("transform", String.format("translate(%d, %d) rotate(270)", x, y))
                .with(text(text));
        this.container.with(textTag);
    }

    @Override
    public void showRText(String style, int x, int y, String text) {
        showText(x, y, text, "end", "middle");
    }

    /**
     * Define the axes.
     *
     * @param xLabel	x-axis label
     * @param yLabel	y-axis label
     */
    public void defineAxes(String xLabel, String yLabel) {
        this.xAxis = new Axis.X(xLabel, this.leftMargin, this.width - this.rightMargin);
        this.yAxis = new Axis.Y(yLabel, this.height - this.botMargin, this.topMargin);
    }

    /**
     * Add a point to the graph.
     *
     * @param label		label for the point
     * @param x			x-value of the point
     * @param y			y-value of the point
     */
    public void add(String label, double x, double y) {
        Point point = new Point(label, x, y);
        this.xAxis.point(x);
        this.yAxis.point(y);
        this.points.add(point);
    }

    /**
     * Draw an x-value bound.
     *
     * @param x		x-value of bound
     */
    public void drawXBound(double x) {
        int p = this.xAxis.getPoint(x);
        this.drawLine("bound", p, this.yAxis.getOrigin(), p, this.yAxis.getTerminus());
    }

    /**
     * Draw a y-value bound.
     *
     * @param x		x-value of bound
     */
    public void drawYBound(double y) {
        int p = this.yAxis.getPoint(y);
        this.drawLine("bound", this.xAxis.getOrigin(), p, this.xAxis.getTerminus(), p);
    }

    /**
     * Read the points from a tab-delimited file.
     *
     * @param inFile	input file
     * @param xCol		column/axis label for x-axis
     * @param yCol		column/axis label for y-axis
     */
    public void readPoints(File inFile, String labelCol, String xCol, String yCol) throws IOException {
        this.defineAxes(xCol, yCol);
        log.info("Reading points from {}.", inFile);
        try (TabbedLineReader inStream = new TabbedLineReader(inFile)) {
            int labelColIdx = inStream.findField(labelCol);
            int xColIdx = inStream.findField(xCol);
            int yColIdx = inStream.findField(yCol);
            for (TabbedLineReader.Line line : inStream) {
                String label = line.get(labelColIdx);
                double x = line.getDouble(xColIdx);
                double y = line.getDouble(yColIdx);
                this.add(label, x, y);;
            }
        }
        log.info("{} points read.", this.points.size());
    }

    /**
     * @return the number of points graphed
     */
    public int size() {
        return this.points.size();
    }

    /**
     * Plot the points.
     */
    public void plot() {
        // Fix up the axes.
        this.xAxis.initialize(this.yAxis, this);
        this.yAxis.initialize(this.xAxis, this);
        // Plot the points.
        for (Point point : this.points) {
            int xPos = this.xAxis.getPoint(point.getX());
            int yPos = this.yAxis.getPoint(point.getY());
            ContainerTag dot = new ContainerTag("circle").attr("cx", xPos).attr("cy", yPos)
                    .attr("fill", this.circleFill.html()).attr("r", this.radius)
                    .with(title(point.getLabel()));
            this.container.with(dot);
        }
    }

}
