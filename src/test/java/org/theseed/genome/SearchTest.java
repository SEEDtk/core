/**
 *
 */
package org.theseed.genome;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Bruce Parrello
 *
 */
public class SearchTest extends TestCase {

    public void testSearch() throws IOException {
        FeatureSearch searcher = new FeatureSearch(new File("data"));
        Map<String, String> found = searcher.findFeatures("\\bcysteine\\b");
        for (Map.Entry<String, String> foundEntry : found.entrySet()) {
            String function = foundEntry.getValue();
            String fid = foundEntry.getKey();
            assertThat(fid, function.toLowerCase(), containsString("cysteine"));
        }
    }
}
