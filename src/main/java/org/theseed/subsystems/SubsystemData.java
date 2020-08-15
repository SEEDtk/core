/**
 *
 */
package org.theseed.subsystems;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.io.LineReader;
import org.theseed.io.MarkerFile;

/**
 * This object contains data about a subsystem.  This includes an array of role descriptors and an array of rows.
 *
 * @author Bruce Parrello
 *
 */
public class SubsystemData {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(SubsystemData.class);
    /** list of columns */
    private ColumnData[] columns;
    /** loaded rows */
    private SortedSet<RowData> rows;
    /** name of this subsystem */
    private String name;
    /** ID of this subsystem */
    private String id;
    /** list of missing genomes */
    private SortedSet<String> missingGenomes;
    /** coreSEED directory for this subsystem */
    private File coreDir;
    /** subsystem error count */
    private int errorCount;
    /** spreadsheet section marker */
    private static final String MARKER = "//";

    /**
     * Create an empty subsystem.
     *
     * @param coreDir	SEED data directory
     * @param id		ID of the subsystem
     */
    private SubsystemData(File coreDir, String ssId) {
        this.id = ssId;
        this.name = StringUtils.replaceChars(ssId, '_', ' ');
        this.coreDir = coreDir;
        this.rows = new TreeSet<RowData>();
        this.missingGenomes = new TreeSet<String>();
    }

    /**
     * Load a subsystem from the SEED.
     *
     * @param coreDir	SEED data directory
     * @param id		ID of the subsystem
     *
     * @return the fully-populated subsystem, or NULL if the subsystem does not exist
     */
    public static SubsystemData load(File coreDir, String ssId) {
        SubsystemData retVal = null;
        File ssFile = new File(coreDir, "Subsystems/" + ssId + "/spreadsheet");
        if (! ssFile.exists()) {
            log.warn("Subsystem {} not found in {}.", ssId, coreDir);
        } else {
            retVal = new SubsystemData(coreDir, ssId);
            // Open the spreadsheet and start reading sections.
            try (LineReader ssStream = new LineReader(ssFile)) {
                log.info("Reading spreadsheet file for {} subsystem \"{}\".",
                        coreDir, retVal.name);
                // Read the roles.
                List<ColumnData> cols = new ArrayList<ColumnData>(50);
                for (String[] roleParts : ssStream.new Section(MARKER))
                    cols.add(new ColumnData(cols.size(), roleParts[0], roleParts[1]));
                // Store the roles as an array.
                retVal.columns = new ColumnData[cols.size()];
                retVal.columns = cols.toArray(retVal.columns);
                // Skip the notes section.
                ssStream.skipSection(MARKER);
                // Now we loop through the rows.
                for (String ssRow : ssStream) {
                    RowData row = RowData.load(coreDir, ssRow, cols.size());
                    // Empty rows are ignored.
                    if (row != null) {
                        if (row.isMissing())
                            // Here the genome does not exist in the SEED.
                            retVal.missingGenomes.add(row.getGenomeId());
                        else
                            retVal.rows.add(row);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return retVal;
    }

    /**
     * @return the error-count file name for the specified subsystem
     *
     * @param coreDir	coreSEED data directory
     * @param id		ID of the subsystem of interest
     */
    public static File errorCountFile(File coreDir, String id) {
        return new File(coreDir, "Subsystems/" + id + "/ERRORCOUNT");
    }
    /**
     * @return the array of columns
     */
    public ColumnData[] getColumns() {
        return this.columns;
    }

    /**
     * @return the number of columns in this subsystem
     */
    public int getWidth() {
        return this.columns.length;
    }

    /**
     * @return the number of rows in this subsystem
     */
    public int size() {
        return this.rows.size();
    }

    /**
     * @return the set of rows, ordered by genome name
     */
    public SortedSet<RowData> getRows() {
        return this.rows;
    }

    /**
     * @return the subsystem name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the subsystem ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the set of genomes missing from the SEED
     */
    public SortedSet<String> getMissingGenomes() {
        return this.missingGenomes;
    }

    /**
     * @return the number of missing genomes in this subsystem
     */
    public int numGenomesMissing() {
        return this.missingGenomes.size();
    }

    /**
     * Run validation on all rows of this subsystem and tally the
     * results.
     *
     * @param coreDir	SEED data directory
     *
     * @throws IOException
     */
    public void validateRows(File coreDir) throws IOException {
        log.info("Validating subsystem {}.", this.name);
        // Loop through the rows, placing each feature.
        for (RowData row : this.rows) {
            log.info("Scanning {}.", row.toString());
            Map<String, String> funMap = row.getFunctions();
            for (Map.Entry<String, String> feature : funMap.entrySet()) {
                for (int i = 0; i < this.getWidth(); i++)
                    row.getCell(i).setState(feature.getKey(), feature.getValue(), this.columns[i]);
            }
        }
        // Now summarize the columns and compute the error count.
        log.info("Summarizing columns in {}.", this.name);
        this.errorCount = this.numGenomesMissing();
        for (ColumnData col : this.columns) {
            int idx = col.getColIdx();
            for (RowData row : this.rows)
                col.countCell(row.getCell(idx));
            this.errorCount += col.getCount(PegState.MISSING) + col.getCount(PegState.BAD_ROLE) +
                    col.getCount(PegState.DISCONNECTED);
        }
        // Save the error count.
        File errorCountFile = SubsystemData.errorCountFile(this.coreDir, this.id);
        MarkerFile.write(errorCountFile, this.errorCount);
    }

    /**
     * @return the number of errors in this subsystem
     */
    public int getErrorCount() {
        return this.errorCount;
    }

    /**
     * @return the subsystem health rating (1.00 is perfect)
     */
    public double getHealth() {
        double total = (this.getWidth() + 1) * this.size();
        double retVal = 0.0;
        if (total > 0)
            retVal = (total - this.errorCount) / total;
        return retVal;
    }


}
