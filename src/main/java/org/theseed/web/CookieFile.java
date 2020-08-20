/**
 *
 */
package org.theseed.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.theseed.io.LineReader;

/**
 * This object reads named values from a file when it is constructed.  When it is closed, it writes the values
 * back.  Internally, the values are stored as strings.  The client is expected to pull values out once and
 * store them back once, and the number of values is expected to be small, so this is not a performance
 * issue.  The goal is to have something that acts like cookies but stays on the server.
 *
 * Field names cannot contain spaces.  Values cannot contain line-end characters.
 *
 * @author Bruce Parrello
 *
 */
public class CookieFile implements AutoCloseable {

    // FIELDS
    /** map of named values to strings */
    private Map<String, String> varMap;
    /** file containing the data map */
    private File mapFile;
    /** TRUE if the file has changed */
    private boolean changed;
    /** format for file name */
    private static String FILE_NAME = "_%s.cookie.tbl";

    /**
     * Load the cookie file into memory.
     *
     * @param workSpaceDir	workspace directory
     * @param typeName		object type whose cookie file is desired
     *
     * @throws IOException
     */
    public CookieFile(File workSpaceDir, String typeName) throws IOException {
        this.mapFile = new File(workSpaceDir, String.format(FILE_NAME, typeName));
        // Create the map.
        this.varMap = new HashMap<String, String>();
        // Only try to read the file if it exists.  Otherwise, we have an empty map.
        if (this.mapFile.exists()) {
            // Each line of the file consists of a field name, a space, and the field value.
            try (LineReader mapStream = new LineReader(this.mapFile)) {
                for (String line : mapStream) {
                    String key = StringUtils.substringBefore(line, " ");
                    String value = StringUtils.substring(line, key.length() + 1);
                    this.varMap.put(key, value);
                }
            }
        }
        // Denote we're unchanged.
        this.changed = false;
    }

    /**
     * @return the value of a key (string)
     *
     * @param key			key whose value is desired
     * @param defaultVal	default value to use if the key is not present
     */
    public String get(String key, String defaultVal) {
        return this.varMap.computeIfAbsent(key, x -> defaultVal);
    }

    /**
     * @return the value of a key (integer)
     *
     * @param key			key whose value is desired
     * @param defaultVal	default value to use if the key is not present
     */
    public int get(String key, int defaultVal) {
        int retVal;
        String stringVal = this.varMap.get(key);
        if (stringVal == null) {
            this.put(key, defaultVal);
            retVal = defaultVal;
        } else {
            retVal = Integer.parseInt(stringVal);
        }
        return retVal;
    }

    /**
     * @return the value of a key (boolean)
     *
     * @param key			key whose value is desired
     * @param defaultVal	default value to use if the key is not present
     */
    public boolean get(String key, boolean defaultVal) {
        boolean retVal = false;
        String stringVal = this.varMap.get(key);
        if (stringVal == null) {
            retVal = defaultVal;
            this.put(key, defaultVal);
        } else {
            retVal = stringVal.contentEquals("Y");
        }
        return retVal;
    }

    /**
     * Store a new key value (boolean).
     *
     * @param key			key to update
     * @param newVal		value to store
     *
     * @return this object, for chaining
     */
    public CookieFile put(String key, boolean newVal) {
        String flagValue = (newVal ? "Y" : " ");
        return this.put(key, flagValue);
    }

    /**
     * @return the value of a key (double)
     *
     * @param key			key whose value is desired
     * @param defaultVal	default value to use if the key is not present
     */
    public double get(String key, double defaultVal) {
        String stringVal = this.varMap.get(key);
        double retVal;
        if (stringVal == null) {
            retVal = defaultVal;
            this.put(key, defaultVal);
        } else {
            retVal = Double.parseDouble(stringVal);
        }
        return retVal;
    }

    /**
     * Store a new key value (double).
     *
     * @param key			key to update
     * @param newVal		value to store
     *
     * @return this object, for chaining
     */
    public CookieFile put(String key, double newVal) {
        return this.put(key, Double.toString(newVal));
    }

    /**
     * Store a new key value (string).
     *
     * @param key			key to update
     * @param newVal		value to store
     *
     * @return this object, for chaining
     */
    public CookieFile put(String key, String newVal) {
        this.varMap.put(key, newVal);
        this.changed = true;
        return this;
    }

    /**
     * Store a new key value (int).
     *
     * @param key			key to update
     * @param newVal		value to store
     *
     * @return this object, for chaining
     */
    public CookieFile put(String key, int newVal) {
        return this.put(key, Integer.toString(newVal));
    }


    @Override
    public void close() throws IOException {
        if (this.changed) {
            // Write the values to the output file.
            try (PrintWriter outStream = new PrintWriter(this.mapFile)) {
                for (Map.Entry<String, String> keyValue : this.varMap.entrySet()) {
                    outStream.println(keyValue.getKey() + " " + keyValue.getValue());
                }
            }
        }
    }

}
