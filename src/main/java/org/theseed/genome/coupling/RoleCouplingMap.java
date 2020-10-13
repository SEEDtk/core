/**
 *
 */
package org.theseed.genome.coupling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.theseed.genome.Coupling;
import org.theseed.genome.Feature;
import org.theseed.io.TabbedLineReader;
import org.theseed.proteins.Role;
import org.theseed.proteins.RoleMap;

/**
 * This object represents a role-coupling map.  It can be read and written to disk, constructed in memory, and
 * used to extract coupled features from genomes.
 *
 * This object includes a role definition map, and a map of role IDs to coupling objects.  The coupling objects
 * are organized into lists, which is not the fastest structure, but is acceptable since the mean list size is
 * 3.
 *
 * @author Bruce Parrello
 *
 */
public class RoleCouplingMap implements Serializable {

    // FIELDS
    /** role definition map */
    private RoleMap roleMap;
    /** hash of role IDs to couplings */
    private Map<String, SortedSet<Coupling>> couplingMap;
    /** end-of-stream mark */
    private static final String MARKER = "//";
    /** empty set of couplings */
    private static final SortedSet<Coupling> NO_COUPLINGS = new TreeSet<Coupling>();
    /** serialization class ID */
    private static final long serialVersionUID = -6953186280381142675L;

    /**
     * Construct a blank, empty role-coupling map.
     */
    public RoleCouplingMap() {
        this.init();
    }

    /**
     * Construct a role-coupling map from a kmers.reps coupling report.
     *
     * @param inFile	coupling report containing the role couplings (file)
     *
     * @throws IOException
     */
    public RoleCouplingMap(File inFile) throws IOException {
        this.init();
        try (TabbedLineReader inStream = new TabbedLineReader(inFile)) {
            readFromStream(inStream);
        }
    }

    /**
     * Construct a role-coupling map from a kmers.reps coupling report.
     *
     * @param input	coupling report containing the role couplings (stream)
     *
     * @throws IOException
     */
    public RoleCouplingMap(InputStream input) throws IOException {
        this.init();
        try (TabbedLineReader inStream = new TabbedLineReader(input)) {
            readFromStream(inStream);
        }
    }

    /**
     * Read this coupling map from an input stream.
     *
     * @param inStream		input stream containing a coupling report
     *
     * @throws IOException
     */
    protected void readFromStream(TabbedLineReader inStream) throws IOException {
        int role1Idx = inStream.findField("role1");
        int role2Idx = inStream.findField("role2");
        int sizeIdx = inStream.findField("size");
        int strengthIdx = inStream.findField("sim_distance");
        for (TabbedLineReader.Line line : inStream) {
            String role1Desc = line.get(role1Idx);
            String role2Desc = line.get(role2Idx);
            Role role1 = this.roleMap.findOrInsert(role1Desc);
            Role role2 = this.roleMap.findOrInsert(role2Desc);
            this.couple(role1.getId(), role2.getId(), line.getInt(sizeIdx), line.getDouble(strengthIdx));
        }
    }

    /**
     * Load a role-coupling map from a serialized file.
     *
     * @param inFile	file containing the serialized coupling map
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static RoleCouplingMap load(File inFile) throws IOException, ClassNotFoundException {
        RoleCouplingMap retVal = null;
        FileInputStream fileStream = new FileInputStream(inFile);
        try (ObjectInputStream oStream = new ObjectInputStream(fileStream)) {
            retVal = (RoleCouplingMap) oStream.readObject();
        } finally {
            fileStream.close();
        }
        return retVal;
    }

    /**
     * Initialize this coupling map to an empty state.
     */
    private void init() {
        this.roleMap = new RoleMap();
        this.couplingMap = new HashMap<String, SortedSet<Coupling>>();
    }

    /**
     * Initialize this object from an input stream.
     *
     * @param is	object input stream
     *
     * @throws IOException
     */
    private void readObject(ObjectInputStream is) throws IOException {
        // Initialize the object.
        this.init();
        // Loop through the role definitions.
        String role = is.readUTF();
        while (! role.contentEquals(MARKER)) {
            Role roleO = new Role(role, is.readUTF());
            this.roleMap.put(roleO);
            role = is.readUTF();
        }
        // Loop through the coupling definitions.  For each coupling, we add both directions.
        role = is.readUTF();
        while (! role.contentEquals(MARKER)) {
            // Get the coupling information.
            String target = is.readUTF();
            int size = is.readInt();
            double strength = is.readDouble();
            this.couple(role, target, size, strength);
            role = is.readUTF();
        }
    }

    /**
     * Establish a coupling of the given size and strength between two roles.
     *
     * @param role1		ID of the first role
     * @param role2		ID of the second role
     * @param size		size of the coupled set
     * @param strength	strength of the coupling
     */
    public void couple(String role1, String role2, int size, double strength) {
        // Attach the coupling in both directions.
        this.coupleTo(role1, role2, size, strength);
        this.coupleTo(role2, role1, size, strength);
    }

    /**
     * Connect role2 to role1 as a coupling.
     *
     * @param role1		ID of the first role
     * @param role2		ID of the second role
     * @param size		size of the coupled set
     * @param strength	strength of the coupling
     */
    private void coupleTo(String role1, String role2, int size, double strength) {
        SortedSet<Coupling> couplings = this.couplingMap.computeIfAbsent(role1, x -> new TreeSet<Coupling>());
        couplings.add(new Coupling(role2, size, strength));
    }

    /**
     * Write this object to an output stream.
     *
     * @param os	object output stream
     *
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream os) throws IOException {
        // Loop through the role definitions.
        for (Role roleDef : this.roleMap) {
            os.writeUTF(roleDef.getId());
            os.writeUTF(roleDef.getName());
        }
        os.writeUTF(MARKER);
        // Now we write out the couplings.  To save space, we only write the couplings where the first
        // role is lexically less than the second role.  The coupling is duplicated when it is read back in.
        for (Map.Entry<String, SortedSet<Coupling>> coupleEntry : this.couplingMap.entrySet()) {
            String role1 = coupleEntry.getKey();
            for (Coupling couple : coupleEntry.getValue()) {
                String role2 = couple.getTarget();
                if (role1.compareTo(role2) < 0) {
                    os.writeUTF(role1);
                    os.writeUTF(role2);
                    os.writeInt(couple.getSize());
                    os.writeDouble(couple.getStrength());
                }
            }
        }
        os.writeUTF(MARKER);
    }

    /**
     * Save this role-coupling map to a file.
     *
     * @param outFile	file to use for storing the map
     *
     * @throws IOException
     */
    public void save(File outFile) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(outFile);
        try (ObjectOutputStream oStream = new ObjectOutputStream(fileStream)) {
            oStream.writeObject(this);
        } finally {
            fileStream.close();
        }
    }

    /**
     * @return the set of couplings for the specified role ID
     *
     * This will never return NULL, but it may return an empty set.
     *
     * @param roleId	ID of the role of interest
     */
    public SortedSet<Coupling> getCouplings(String roleId) {
        return this.couplingMap.getOrDefault(roleId, NO_COUPLINGS);
    }

    /**
     * @return the set of role IDs with couplings
     */
    public Set<String> getRoles() {
        return this.couplingMap.keySet();
    }

    /**
     * @return the description of a role
     *
     * @param ID of the role of interest
     */
    public String getName(String roleId) {
        return this.roleMap.getName(roleId);
    }

    /**
     * @return the ID of the role having a description, or NULL if the role is not in the map
     *
     * @param roleDesc	description of interest
     */
    public String getRole(String roleDesc) {
        String retVal = null;
        Role role = this.roleMap.getByName(roleDesc);
        if (role != null) {
            retVal = role.getId();
        }
        return retVal;
    }
    /**
     * @return the highest-strength coupling in the specified coupling list relevant to the specified feature, or NULL if there is none
     *
     * @param couplings		set of desired couplings, sorted by strength
     * @param feat			feature of interest
     */
    public Coupling getBestCoupling(SortedSet<Coupling> couplings, Feature feat) {
        // Get the set roles in this feature.
        Set<String> roles = feat.getUsefulRoles(this.roleMap).stream().map(r -> r.getId()).collect(Collectors.toSet());
        Coupling retVal = null;
        if (roles.size() > 0) {
            // Loop through the couplings.  The first match will be the strongest, because the set is ordered.
            Iterator<Coupling> iter = couplings.iterator();
            while (retVal == null && iter.hasNext()) {
                Coupling curr = iter.next();
                if (roles.contains(curr.getTarget()))
                    retVal = curr;
            }
        }
        return retVal;
    }

    /**
     * @return the underlying role map
     */
    public RoleMap getMap() {
        return this.roleMap;
    }

}
