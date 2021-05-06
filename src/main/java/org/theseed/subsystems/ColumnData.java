/**
 *
 */
package org.theseed.subsystems;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.theseed.genome.Feature;

/**
 * This object represents a subsystem column.  Each subsystem column contains data relating to a single
 * function.  The function is almost always a single role, but may contain multiple roles.
 *
 *
 * @author Bruce Parrello
 *
 */
public class ColumnData {


    /**
     * This is a capsule class to make it easier to deal with entries in the bad-role map.
     * It sorts the entries from largest set size to smallest, and defines equality by the
     * function.
     */
    public static class BadRole implements Comparable<BadRole> {

        // FIELDS
        String function;
        Set<String> fids;

        protected BadRole(Map.Entry<String, SortedSet<String>> entry) {
            this.function = entry.getKey();
            this.fids = entry.getValue();
        }

        /**
         * @return the function
         */
        public String getFunction() {
            return this.function;
        }

        /**
         * @return the features having this bad role
         */
        public Set<String> getFids() {
            return this.fids;
        }

        @Override
        public int compareTo(BadRole o) {
            int retVal = o.fids.size() - this.fids.size();
            if (retVal == 0)
                retVal = this.function.compareTo(o.function);
            return retVal;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.function == null) ? 0 : this.function.hashCode());
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
            BadRole other = (BadRole) obj;
            if (this.function == null) {
                if (other.function != null)
                    return false;
            } else if (!this.function.equals(other.function))
                return false;
            return true;
        }

    }

    // FIELDS
    /** abbreviation */
    private String abbr;
    /** canonical assigned function for this column */
    private String function;
    /** column index in the subsystem */
    private int colIdx;
    /** set of roles in the function */
    private String[] roles;
    /** array of counts, indexed by PegState */
    private int[] counts;
    /** hash of bad-role functions to feature sets */
    private Map<String, SortedSet<String>> badRoleFeatures;
    /** TRUE if this is an auxiliary role */
    private boolean aux;

    /**
     * Construct a new column descriptor.
     *
     * @param idx		column index (0-based)
     * @param abbr		role abbreviation
     * @param roleText	role description
     */
    public ColumnData(int idx, String abbr, String roleText) {
        this.colIdx = idx;
        this.abbr = abbr;
        this.function = roleText;
        this.roles = Feature.rolesOfFunction(roleText);
        this.aux = false;
        // Clear the counter-related stuff.
        this.counts = new int[PegState.values().length];
        this.badRoleFeatures = new TreeMap<String, SortedSet<String>>();
    }

    /**
     * @return the abbreviation for this column's function
     */
    public String getAbbr() {
        return this.abbr;
    }

    /**
     * @return the function assigned to this column
     */
    public String getFunction() {
        return this.function;
    }

    /**
     * @return TRUE if this is an auxiliary role
     */
    public boolean isAux() {
        return this.aux;
    }

    /**
     * Specify whether or not this is an auxiliary role.
     *
     * @param aux 	TRUE if this is an auxiliary role, else FALSE
     */
    public void setAux(boolean aux) {
        this.aux = aux;
    }

    /**
     * @return the index (0-based) of this column
     */
    public int getColIdx() {
        return this.colIdx;
    }

    /**
     * @return TRUE if the specified function matches this column, else FALSE
     *
     * @param oFunction	function of a feature being considered for the role
     */
    public boolean matches(String oFunction) {
        int idx = oFunction.indexOf(this.function);
        boolean retVal = false;
        if (idx == 0 && this.function.length() == oFunction.length()) {
            // Here the other function is exactly the same.
            retVal = true;
        } else if (idx >= 0) {
            // Here we are a substring of the other function.  Verify that this is a role match.
            String[] oRoles = Feature.rolesOfFunction(oFunction);
            // oRoles must contain everything in roles.  These are usually very small arrays,
            // at most 3, and almost always length 1, so we use brute force.
            retVal = true;
            for (String role : this.roles) {
                int count = 0;
                for (String oRole : oRoles)
                    if (oRole.contentEquals(role)) count++;
                if (count == 0) retVal = false;
            }
        }
        return retVal;
    }

    /**
     * @return the count for the specified state
     */
    public int getCount(PegState state) {
        return this.counts[state.ordinal()];
    }

    /**
     * @return TRUE if this column has errors, else FALSE
     */
    public boolean hasErrors() {
        return (this.badRoleFeatures.size() > 0 || this.getCount(PegState.DISCONNECTED) > 0 ||
                this.getCount(PegState.MISSING) > 0);
    }

    /**
     * Count the features in a cell.
     */
    public void countCell(CellData cell) {
        // Loop through the features here.
        for (Map.Entry<String, FeatureStatus> featData : cell.getFeatures()) {
            FeatureStatus status = featData.getValue();
            // Bad roles are organized by feature within improper functional assignment.
            if (status.getState() == PegState.BAD_ROLE) {
                String function = status.getFunction();
                SortedSet<String> badFidSet = this.badRoleFeatures.computeIfAbsent(function, k -> new TreeSet<String>());
                badFidSet.add(featData.getKey());
                this.counts[PegState.BAD_ROLE.ordinal()]++;
            } else {
                // Everything else is simply counted.
                this.counts[status.getState().ordinal()]++;
            }
        }
    }

    /**
     * @return the bad role entries, sorted by number of features
     */
    public SortedSet<BadRole> getBadRoles() {
        SortedSet<BadRole> retVal = new TreeSet<BadRole>();
        for (Map.Entry<String, SortedSet<String>> entry : this.badRoleFeatures.entrySet())
            retVal.add(new BadRole(entry));
        return retVal;
    }

}
