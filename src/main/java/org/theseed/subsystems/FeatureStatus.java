/**
 *
 */
package org.theseed.subsystems;

import j2html.tags.DomContent;

/**
 * This object describes the status of a feature in a subsystem.  It contains the state of the feature,
 * and if the peg is in the bad-role state, its assigned function.
 *
 * @author Bruce Parrello
 */
public class FeatureStatus {

    // FIELDS
    /** state of this feature */
    private PegState state;
    /** function of this feature (or NULL if it has the correct role) */
    private String function;

    /**
     * Create a blank feature status.  The initial state is always MISSING, with a correct function.
     */
    public FeatureStatus() {
        this.state = PegState.MISSING;
        this.function = null;
    }

    // Create a feature status with the specified state.
    public FeatureStatus(PegState newState) {
        this.state = newState;
        this.function = null;
    }

    /**
     * Set the state of this feature (if not bad-role).
     *
     * @param newState	new state
     * @param function	actual functional assignment of the feature
     */
    public void setState(PegState newState, String function) {
        this.state = newState;
        if (newState == PegState.BAD_ROLE)
            this.function = function;
        else
            this.function = null;
    }

    /**
     * @return the state
     */
    public PegState getState() {
        return this.state;
    }

    /**
     * @return the functional assignment, or NULL if the function is correct for this role
     */
    public String getFunction() {
        return this.function;
    }

    /**
     * @return the HTML display entity for the feature with this status
     *
     * @param pegId		ID of the feature having this status
     */
    public DomContent display(String pegId) {
        return this.state.display(pegId);
    }


}
