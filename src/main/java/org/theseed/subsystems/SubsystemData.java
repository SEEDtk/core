/**
 *
 */
package org.theseed.subsystems;

import static j2html.TagCreator.a;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.io.LineReader;
import org.theseed.io.MarkerFile;

import j2html.tags.ContainerTag;

/**
 * This object contains data about a subsystem.  This includes an array of role descriptors and an array of rows.
 * The row array is implemented as a hashmap so that the last duplicate is the one kept.
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
    private Map<String, RowData> rows;
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
    /** TRUE if the subsystem's error count is unknown */
    private boolean ambiguousCount;
    /** spreadsheet section marker */
    private static final String MARKER = "//";
    /** number of milliseconds it takes for an error check to go stale (7 days of 24 hours of 3600 seconds
     * of 1000 milliseconds) */
    private static final long STALE_TIME = 7 * 24 * 3600 * 1000;
    /** coreSEED subsystem page link format */
    private static final String SUBSYSTEM_LINK = "https://core.theseed.org/FIG/seedviewer.cgi?page=Subsystems;subsystem=%s";
    /** format for subsystem page URLs */
    private static String HEALTH_LINK = "subsystems.cgi?health=%s";


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
        this.rows = new HashMap<String, RowData>();
        this.missingGenomes = new TreeSet<String>();
    }

    /**
     * Get the name and health of a subsystem without loading it.
     *
     * @param coreDir	SEED data directory
     * @param id		ID of the subsystem
     */
    public static SubsystemData survey(File coreDir, String ssId) {
        SubsystemData retVal = null;
        File ssFile = getSpreadsheet(coreDir, ssId);
        if (! ssFile.exists()) {
            log.warn("Subsystem {} not found in {}.", ssId, coreDir);
        } else {
            retVal = new SubsystemData(coreDir, ssId);
            // Now check the error file.
            File errorCountFile = SubsystemData.errorCountFile(coreDir, ssId);
            if (! errorCountFile.exists()) {
                // Here there it no error file.  The error count is 0 and ambiguous.
                retVal.errorCount = 0;
                retVal.ambiguousCount = true;
            } else {
                // Read the error count.
                retVal.errorCount = MarkerFile.readInt(errorCountFile);
                // It is ambiguous if it is older than the last change to the spreadsheet or
                // if it has not been checked in a week.  This is a very crude test, since
                // the truth requires checking all the genomes, which is worse than reloading
                // the subsystem.
                retVal.ambiguousCount = (errorCountFile.lastModified() < ssFile.lastModified() ||
                        errorCountFile.lastModified() < (System.currentTimeMillis() - STALE_TIME));
            }
        }
        return retVal;
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
        File ssFile = getSpreadsheet(coreDir, ssId);
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
                            retVal.rows.put(row.getGenomeId(), row);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            // Denote the error count is unknown.
            retVal.errorCount = 0;
            retVal.ambiguousCount = true;
        }
        return retVal;
    }

    /**
     * Return the name of a subsystem's spreadsheet file.
     *
     * @param coreDir	SEED data directory
     * @param ssId		ID of the subsystem
     *
     * @return a file object for the spreadsheet, or NULL if the subsystem does not exist
     */
    private static File getSpreadsheet(File coreDir, String ssId) {
        File retVal = new File(coreDir, "Subsystems/" + ssId + "/spreadsheet");
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
    public Collection<RowData> getRows() {
        return this.rows.values();
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
     * @throws IOException
     */
    public void validateRows() throws IOException {
        log.info("Validating subsystem {}.", this.name);
        Collection<RowData> allRows = this.getRows();
        // Loop through the rows, placing each feature.
        for (RowData row : allRows) {
            log.debug("Scanning {}.", row.toString());
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
            for (RowData row : allRows)
                col.countCell(row.getCell(idx));
            this.errorCount += col.getCount(PegState.MISSING) + col.getCount(PegState.BAD_ROLE) +
                    col.getCount(PegState.DISCONNECTED);
        }
        // Denote that error count is known and save it.
        this.ambiguousCount = false;
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
     * @return TRUE if the error count is suspect, else FALSE
     */
    public boolean isSuspectErrorCount() {
        return this.ambiguousCount;
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

    /**
     * @return a link to the health page for the specified subsystem
     *
     * @param subsystem		subsystem of interest
     */
    public ContainerTag getHealthLink() {
        return a(this.getName()).withHref(String.format(HEALTH_LINK, this.getUrlId()));
    }

    /**
     * @return the subsystem ID formatted for a URL
     */
    private String getUrlId() {
        String retVal = null;
        try {
            retVal = URLEncoder.encode(this.getId(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return retVal;
    }
    /**
     * @return the CoreSEED URL for this subsystem
     */
    public String getLink() {
        return String.format(SUBSYSTEM_LINK, this.getUrlId());
    }


}
