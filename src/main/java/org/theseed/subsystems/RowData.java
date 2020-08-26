/**
 *
 */
package org.theseed.subsystems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.theseed.io.LineReader;
import org.theseed.io.MarkerFile;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

/**
 * This object represents a single row in a subsystem.  It contains
 * the genome ID and name, the variant code, and all of the cells in
 * the row.
 *
 * @author Bruce Parrello
 *
 */
public class RowData implements Comparable<RowData> {

    // FIELDS
    /** ID of this row's genome */
    private String genomeId;
    /** name of this row's genome */
    private String name;
    /** variant code of this row */
    private String variantCode;
    /** feature types of interest */
    private Set<String> types;
    /** array of cells */
    private CellData[] cells;
    /** data directory for this row's genome */
    private File orgDir;
    /** genome URL format */
    private static String GENOME_URL = "https://core.theseed.org/FIG/seedviewer.cgi?page=Organism;organism=%s";

    /**
     * Construct a new row.
     */
    public RowData(String genomeId, String name, String variantCode) {
        this.genomeId = genomeId;
        this.name = name;
        this.variantCode = variantCode;
        // We use a tree set because we expect at most two types.
        this.types = new TreeSet<String>();
    }

    /**
     * Construct a row from a spreadsheet string.
     *
     * @param dataDir		coreSEED data directory
     * @param rowString		row definition
     * @param cols			expected number of columns
     *
     * @return the row created, else NULL
     */
    public static RowData load(File dataDir, String rowString, int cols) {
        RowData retVal = null;
        // Note we strip the empty cells at the end. This speeds a couple of things.
        String[] parts = StringUtils.splitPreserveAllTokens(StringUtils.stripEnd(rowString, "\t "), '\t');
        // Only proceed if the row has nonempty cells.
        if (parts.length > 2) {
            // Get the genome name. Don't fail if the genome is missing.
            String genomeId = parts[0];
            File orgDir = new File(dataDir, "Organisms/" + genomeId);
            File deleteFile = new File(orgDir, "DELETED");
            String name = "";
            if (orgDir.isDirectory() && ! deleteFile.exists())
                name = MarkerFile.read(new File(orgDir, "GENOME"));
            // Create the row.
            retVal = new RowData(genomeId, name, parts[1]);
            // Plug in the organism directory.
            retVal.orgDir = orgDir;
            // Fill in the cells.
            retVal.cells = new CellData[cols];
            for (int i = 0; i < cols; i++) {
                if (i + 2 >= parts.length)
                    // Here the cell was not specified, so we leave it blank.
                    retVal.cells[i] = new CellData(retVal, "");
                else
                    retVal.cells[i] = new CellData(retVal, parts[i + 2]);
            }
        }
        return retVal;
    }

    /**
     * Get all the functions for this genome's features of interest.
     *
     * @return a map from feature ID to functional assignment
     *
     * @throws IOException
     */
    public Map<String, String> getFunctions() throws IOException {
        Map<String, String> retVal = new HashMap<String, String>(3000);
        // Compute the deleted features.
        Set<String> deleted = new HashSet<String>(100);
        for (String type : this.types) {
            File deleteFile = new File(this.orgDir, "Features/" + type + "/deleted.features");
            if (deleteFile.exists())
                try (LineReader deleteStream = new LineReader(deleteFile)) {
                    for (String fid : deleteStream)
                        deleted.add(fid);
                }
        }
        // Now read the assigned functions.  We only keep the ones of interest that are NOT
        // deleted.
        File functionFile = new File(this.orgDir, "assigned_functions");
        try (LineReader funStream = new LineReader(functionFile)) {
            String prefix = "fig|" + this.genomeId + ".";
            // We use the section protocol to get the fields as an array.
            for (String[] parts : funStream.new Section(null)) {
                String type = StringUtils.substringBetween(parts[0], prefix, ".");
                // A null type means we have an invalid feature ID.  We just skip it.
                if (type != null && this.types.contains(type) && ! deleted.contains(parts[0]))
                    retVal.put(parts[0], parts[1]);
            }
        }
        return retVal;
    }

    /**
     * @return the ID of the row's genome
     */
    public String getGenomeId() {
        return this.genomeId;
    }

    /**
     * Add the specified feature type to this genome's types of interest.
     *
     * @param type	feature type to add
     */
    public void addType(String type) {
        this.types.add(type);
    }

    /**
     * @return the types of interest for this genome
     */
    public Set<String> getTypes() {
        return this.types;
    }

    /**
     * @return the display HTML for this genome
     */
    public ContainerTag displayGenome() {
        return a(this.name).withHref(String.format(GENOME_URL, this.genomeId));
    }

    /**
     * @return the variant code
     */
    public String getVariant() {
        return this.variantCode;
    }

    /**
     * @return the cell value in the specified column
     *
     * @param col	column of interest
     */
    public CellData getCell(int col) {
        return this.cells[col];
    }

    /**
     * @return TRUE if this genome is missing.
     */
    public boolean isMissing() {
        return this.name.isEmpty();
    }

    /**
     * Rows sort on name, then genome ID.  Genome ID must be unique.
     */
    @Override
    public int compareTo(RowData o) {
        int retVal = this.name.compareTo(o.name);
        if (retVal == 0)
            retVal = this.genomeId.compareTo(o.genomeId);
        return retVal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.genomeId == null) ? 0 : this.genomeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RowData other = (RowData) obj;
        if (this.genomeId == null) {
            if (other.genomeId != null)
                return false;
        } else if (!this.genomeId.equals(other.genomeId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.genomeId + " (" + this.name + ")";
    }

    /**
     * @return the genome name
     */
    public String getGenomeName() {
        return this.name;
    }

}
