/**
 *
 */
package org.theseed.reports;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.theseed.locations.Location;

import static j2html.TagCreator.*;

/**
 * This file provides useful key types for sorting HtmlTables.
 *
 * @author Bruce Parrello
 */
public abstract class Key {

    /**
     * Store this key in a cell.
     *
     * @param cell	target cell
     * @param col	relevant column specification
     */
    public abstract void store(CellContent cell, ColSpec col);

    /**
     * Null key, for unsorted tables
     */
    public static class Null extends Key implements Comparable<Null> {

        @Override
        public int compareTo(Null o) {
            return 0;
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, rawHtml("&nbsp;"));
        }

    }

    /** constant for all null keys */
    public static final Null NONE = new Null();

    /**
     * Integer key, natural sorting
     */
    public static class Int extends Key implements Comparable<Int> {

        // FIELDS
        private final int value;

        public Int(int val) {
            this.value = val;
        }

        @Override
        public int compareTo(Int o) {
            return this.value - o.value;
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.value);
        }

    }

    /**
     * Integer key, reverse sorting
     */
    public static class RevInt extends Key implements Comparable<RevInt> {

        // FIELDS
        private final int value;

        public RevInt(int val) {
            this.value = val;
        }

        @Override
        public int compareTo(RevInt o) {
            return o.value - this.value;
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.value);
        }

    }

    /**
     * Floating-point number, normal sorting
     */
    public static class Float extends Key implements Comparable<Float> {

        // FIELDS
        private final double value;

        public Float(double val) {
            this.value = val;
        }

        @Override
        public int compareTo(Float o) {
            return Double.compare(this.value, o.value);
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.value);
        }

    }

    /**
     * Floating-point number, reverse sorting
     */
    public static class RevFloat extends Key implements Comparable<RevFloat> {

        // FIELDS
        private final double value;

        public RevFloat(double val) {
            this.value = val;
        }

        @Override
        public int compareTo(RevFloat o) {
            return Double.compare(o.value, this.value);
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell,  this.value);
        }

    }

    /**
     * Floating-point ratio, reverse sorting, with zero denominators at the end.
     */
    public static class RevRatio extends Key implements Comparable<RevRatio> {

        // FIELDS
        private final double numerator;
        private final double denominator;
        private final double value;

        public RevRatio(double num, double dem) {
            this.numerator = num;
            this.denominator = dem;
            this.value = this.numerator / this.denominator;
        }

        @Override
        public int compareTo(RevRatio o) {
            int retVal;
            boolean thisNan = Double.isNaN(this.value);
            boolean oNan = Double.isNaN(o.value);
            if (thisNan)
                retVal = (oNan ? 0 : 1);
            else if (oNan)
                retVal = -1;
            else if (this.denominator == o.denominator) {
                retVal = Double.compare(o.numerator, this.numerator);
                if (this.denominator < 0.0) retVal = -retVal;
            } else if (this.denominator != 0 && o.denominator == 0)
                retVal = -1;
            else if (this.denominator == 0 && o.denominator != 0)
                retVal = 1;
            else
                retVal = Double.compare(o.value, this.value);
            return retVal;
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.value);
        }

    }


    /**
     * String, case-insensitive sorting
     */
    public static class Text extends Key implements Comparable<Text> {

        // FIELDS
        private final String value;

        public Text(String val) {
            this.value = val;
        }

        @Override
        public int compareTo(Text o) {
            int retVal = StringUtils.compareIgnoreCase(this.value, o.value);
            if (retVal == 0) {
                // Here the strings are the same, exempting case changes.  We compare
                // them in reverse so that lower case sorts first.
                retVal = o.value.compareTo(this.value);
            }
            return retVal;
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.value);

        }
    }

    /**
     * String, natural sorting
     */
    public static class Mixed extends Key implements Comparable<Mixed> {

        // FIELDS
        private final static Comparator<String> COMPARATOR = new NaturalSort();
        private final String value;

        public Mixed(String val) {
            this.value = val;
        }

        @Override
        public int compareTo(Mixed o) {
            return COMPARATOR.compare(this.value, o.value);
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.value);
        }

    }

    /**
     * Feature ID key, sort by location.
     */
    public static class Feature extends Key implements Comparable<Key.Feature> {

        // FIELDS
        private String fid;
        private Location loc;

        /**
         * Create a new feature key.
         *
         * @param feat		feature to use as the key
         */
        public Feature(org.theseed.genome.Feature feat) {
            this.fid = feat.getId();
            this.loc = feat.getLocation();
        }

        @Override
        public void store(CellContent cell, ColSpec col) {
            col.store(cell, this.fid);

        }

        @Override
        public int compareTo(Feature o) {
            int retVal = this.loc.compareTo(o.loc);
            if (retVal == 0)
                retVal = this.fid.compareTo(o.fid);
            return retVal;
        }

    }


}
