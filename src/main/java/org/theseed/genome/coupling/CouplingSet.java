/**
 *
 */
package org.theseed.genome.coupling;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.theseed.genome.Feature;
import org.theseed.genome.Genome;
import org.theseed.genome.coupling.CouplingSet.FidPair;
import org.theseed.io.TabbedLineReader;

/**
 * A coupling set represents a set of feature pairs that consistute a single functional coupling.
 *
 * @author Bruce Parrello
 *
 */
public class CouplingSet implements Iterable<FidPair> {

    /**
      * This is a utility class that represents a pair of feature IDs.
      */
    public static class FidPair {

        // FIELDS
        private String fids[];

        /**
         * Construct a feature ID pair from a coupling file pair string.
         *
         * @param pairing	two feature IDs separated by a column
         */
        protected FidPair(String pairing) {
            this.fids = StringUtils.split(pairing, ':');
        }

        /**
         * @return the indicated feature ID
         *
         * @param iidx		0 for the first feature ID and 1 for the second
         */
        public String getFid(int idx) {
            return this.fids[idx];
        }

        /**
         * @return the genome ID for this pair
         */
        public String getGenomeId() {
            return Feature.genomeOf(this.fids[0]);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result + this.fids[0].hashCode()) * prime + this.fids[1].hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FidPair)) {
                return false;
            }
            FidPair other = (FidPair) obj;
            return this.fids[0].equals(other.fids[0]) &&
                    this.fids[1].equals(other.fids[1]);
        }

        /**
         * @return the feature represented by the indicated pair member
         *
         * @param genome	genome containing these features
         * @param idx		0 for the first feature ID and 1 for the second
         */
        public Feature getFeature(Genome genome, int idx) {
            return genome.getFeature(this.fids[idx]);
        }

        /**
         * Determine the feature in this pairing that occurs first.  Both features are on the same strand
         * in the same contig.  If they are forward, we return the index of the leftmost.  If they are
         * backward, we return the index of the rightmost.
         *
         * @param genome	genome containing the features in this pair
         *
         * @return the index of the feature that starts the coupling
         */
        public int getFirstIdx(Genome genome) {
            Feature feat0 = this.getFeature(genome, 0);
            Feature feat1 = this.getFeature(genome, 1);
            int retVal;
            if (feat0.isUpstream(feat1))
                retVal = 0;
            else
                retVal = 1;
            return retVal;
        }

    }

    // FIELDS
    /** protein family IDs */
    private String[] families;
    /** protein family functional roles */
    private String[] functions;
    /** list of feature pairs */
    private List<FidPair> pairs;

    /**
     * Extract Search a coupling file for a specified family pair.
     *
     * @param p1			first family of pair
     * @param p2			second family of pair
     * @param coupleFile	couple file to search
     *
     * @return the data line for the specified family
     *
     * @throws IOException
     */
    private static TabbedLineReader.Line searchCoupleFile(String p1, String p2, File coupleFile) throws IOException {
        TabbedLineReader.Line retVal = null;
        try (TabbedLineReader coupleStream = new TabbedLineReader(coupleFile)) {
             Iterator<TabbedLineReader.Line> iter = coupleStream.iterator();
             // Loop until we run out of file or find the coupling.
             while (retVal == null && iter.hasNext()) {
                 TabbedLineReader.Line line = iter.next();
                 if (line.get(0).contentEquals(p1) && line.get(2).contentEquals(p2))
                     retVal = line;
             }
        }
        if (retVal == null)
            throw new IOException("No coupling data found for " + p1 + " and " + p2 + ".");
        return retVal;
    }

    /**
     * Construct a coupling set for the specified protein families.  If the given pairing is not found in
     * the coupling file, an error will be thrown.
     *
     * @param coupleFile	file of coupling-pair data
     * @param p1			ID of the first protein family
     * @param p2			ID of the second protein family
     *
     * @throws IOException
     */
    public CouplingSet(File coupleFile, String p1, String p2) throws IOException {
        TabbedLineReader.Line line = searchCoupleFile(p1, p2, coupleFile);
        this.families = new String[] { p1, p2 };
        this.functions = new String[] { line.get(1), line.get(3) };
        String[] pairings = StringUtils.split(line.get(4), ",");
        this.pairs = Arrays.stream(pairings).map(x -> new FidPair(x)).collect(Collectors.toList());
    }

    @Override
    public Iterator<FidPair> iterator() {
        return this.pairs.iterator();
    }

    /**
     * @return the specified family ID
     *
     * @param idx	0 for the first family, 1 for the second
     */
    public String getFamily(int idx) {
        return this.families[idx];
    }

    /**
     * @return the specified family's function
     *
     * @param idx	0 for the first family, 1 for the second
     */
    public String getFunction(int idx) {
        return this.functions[idx];
    }

    /**
     * @return the number of pairs in this set
     */
    public int size() {
        return this.pairs.size();
    }

}
