/**
 *
 */
package org.theseed.genome;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.genome.core.OrganismDirectories;
import org.theseed.reports.NaturalSort;
import org.theseed.subsystems.RowData;

/**
 * This is a utility class to search the entire coreSEED for features with particular assignments.
 * The client specifies a particular coreSEED in the constructor, and then a member method is
 * called to get a list of feature IDs.
 *
 * @author Bruce Parrello
 *
 */
public class FeatureSearch {

    // FIELDS
    /** logging facility */
    private static final Logger log = LoggerFactory.getLogger(FeatureSearch.class);
    /** organism directory object */
    private OrganismDirectories genomes;
    /** organism root directory */
    private File orgRoot;
    /** relevant feature types */
    private static Set<String> TYPES = Stream.of("peg", "rna").collect(Collectors.toSet());

    /**
     * Construct a feature search object.
     *
     * @param coreDir	coreSEED data directory
     */
    public FeatureSearch(File coreDir) {
        this.orgRoot = new File(coreDir, "Organisms");
        log.info("Scanning {} for genomes.", orgRoot);
        this.genomes = new OrganismDirectories(orgRoot);
    }

    /**
     * Search for all features with a specified functional assignment.
     *
     * @param regex		regular expression string describing the desired assignments
     *
     * @return a map from feature IDs to functional assignments
     *
     * @throws IOException
     */
    public Map<String, String> findFeatures(String regex) throws IOException {
        // We want the results to go back sorted by feature ID, so we use a tree map.
        Map<String, String> retVal = new TreeMap<String, String>(new NaturalSort());
        // Compile the search pattern.
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        // Loop through the genomes.
        for (String genomeId : genomes) {
            File genomeDir = new File(this.orgRoot, genomeId);
            log.info("Searching {} in directory {}.", genomeId, genomeDir);
            Map<String, String> funMap = RowData.readFunctions(genomeDir, genomeId, TYPES);
            for (Map.Entry<String, String> funEntry : funMap.entrySet()) {
                String function = funEntry.getValue();
                if (pattern.matcher(function).find())
                    retVal.put(funEntry.getKey(), function);
            }
        }
        log.info("{} features found with search pattern.", retVal.size());
        return retVal;
    }

}
