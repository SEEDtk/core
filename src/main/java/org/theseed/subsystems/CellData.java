/**
 *
 */
package org.theseed.subsystems;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.theseed.reports.HtmlUtilities;

import static j2html.TagCreator.br;
import static j2html.TagCreator.rawHtml;
import j2html.tags.DomContent;

/**
 * This object represents a single subsystem cell.  The cell contains one or more features, all belonging
 * to the same genome, and each possessing a particular state.  The features should implement all of the
 * roles in the function defining the cell's column.
 *
 * @author Bruce Parrello
 *
 */
public class CellData {

    // FIELDS
    /** map of features in this cell */
    private final Map<String, FeatureStatus> features;


    /**
     * Construct a cell from a spreadsheet cell string.
     *
     * @param rowData		row descriptor for this cell
     * @param cellString	cell definition string
     */
    public CellData(RowData rowData, String cellString) {
        // We use a tree map because the number of entries is generally one or two.
        this.features = new TreeMap<>();
        // Compute the feature prefix.
        String prefix = "fig|" + rowData.getGenomeId() + ".";
        // Split on commas.
        for (String pegNum : StringUtils.split(cellString, ',')) {
            int idx = pegNum.indexOf('.');
            if (idx >= 0) {
                this.features.put(prefix + pegNum, new FeatureStatus());
                rowData.addType(pegNum.substring(0, idx));
            } else {
                this.features.put(prefix + "peg." + pegNum, new FeatureStatus());
                rowData.addType("peg");
            }
        }
    }

    /**
     * @return TRUE if there are no features in this cell
     */
    public boolean isEmpty() {
        return this.features.isEmpty();
    }

    /**
     * @return the status of a feature in this cell
     *
     * @param the ID of the feature of interest
     */
    public FeatureStatus getStatus(String fid) {
        return this.features.get(fid);
    }

    /**
     * Compute the status of a feature for this cell.
     *
     * @param fid		feature ID
     * @param function	function of the feature
     * @param column	column containing this cell
     */
    public void setState(String fid, String function, ColumnData column) {
        FeatureStatus status = this.getStatus(fid);
        if (column.matches(function)) {
            // Here the feature belongs in the column.
            if (status == null) {
                // This means we are disconnected.  The feature belongs, but is not present.
                this.features.put(fid, new FeatureStatus(PegState.DISCONNECTED));
            } else {
                // This means we are good.
                status.setState(PegState.GOOD, null);
            }
        } else if (status != null) {
            // This means we have a bad role.  The feature is present, but does not belong.
            status.setState(PegState.BAD_ROLE, function);
        }
    }

    /**
     * Display this cell's features.
     */
    public DomContent display() {
        DomContent retVal;
        if (this.isEmpty())
            retVal = rawHtml("&nbsp;");
        else {
            List<DomContent> list = this.features.entrySet().stream().map(k -> k.getValue().display(k.getKey()))
                    .collect(Collectors.toList());
            retVal = HtmlUtilities.joinDelimited(list, br());
        }
        return retVal;
    }

    /**
     * @return TRUE if this cell contains the specified feature
     *
     * @param fid	ID of the feature of interest
     */
    public boolean contains(String fid) {
        return this.features.containsKey(fid);
    }

    /**
     * @return the number of features in this cell
     */
    public int size() {
        return this.features.size();
    }

    /**
     * @return the feature information from this cell
     */
    public Set<Map.Entry<String, FeatureStatus>> getFeatures() {
        return this.features.entrySet();
    }


}
