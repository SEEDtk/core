/**
 *
 */
package org.theseed.subsystems;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import j2html.tags.ContainerTag;

/**
 *
 * Test suite for basic coreSEED classes.
 *
 * @author Bruce Parrello
 *
 */
public class StatusTest extends TestCase {

    /**
     * test the peg state
     */
    public void testPegState() {
        PegState s1 = PegState.BAD_ROLE;
        ContainerTag pegLink = s1.display("fig1");
        assertThat(pegLink.getTagName(), equalTo("a"));
        assertThat(pegLink.getNumChildren(), equalTo(1));
        String tagHtml = pegLink.render();
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("href", "core.theseed.org", "fig1")));
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("class=\"bad\"")));
        s1 = PegState.DISCONNECTED;
        pegLink = s1.display("fig1");
        tagHtml = pegLink.render();
        assertThat(pegLink.getTagName(), equalTo("a"));
        assertThat(pegLink.getNumChildren(), equalTo(1));
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("href", "core.theseed.org", "fig1")));
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("class=\"disconnected\"")));
        s1 = PegState.MISSING;
        pegLink = s1.display("fig1");
        tagHtml = pegLink.render();
        assertThat(pegLink.getTagName(), equalTo("span"));
        assertThat(pegLink.getNumChildren(), equalTo(1));
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("class=\"missing\"", "fig1")));
        s1 = PegState.GOOD;
        pegLink = s1.display("fig1");
        tagHtml = pegLink.render();
        assertThat(pegLink.getTagName(), equalTo("a"));
        assertThat(pegLink.getNumChildren(), equalTo(1));
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("href", "core.theseed.org", "fig1")));
        assertThat(tagHtml, stringContainsInOrder(Arrays.asList("class=\"normal\"")));
    }


    public void testFeatureStatus() {
        FeatureStatus stat = new FeatureStatus();
        assertThat(stat.getState(), equalTo(PegState.MISSING));
        assertThat(stat.getFunction(), nullValue());
        stat.setState(PegState.DISCONNECTED, "fun 1");
        assertThat(stat.getState(), equalTo(PegState.DISCONNECTED));
        assertThat(stat.getFunction(), nullValue());
        stat.setState(PegState.BAD_ROLE, "fun 2");
        assertThat(stat.getState(), equalTo(PegState.BAD_ROLE));
        assertThat(stat.getFunction(), equalTo("fun 2"));
        stat.setState(PegState.GOOD, "fun 3");
        assertThat(stat.getState(), equalTo(PegState.GOOD));
        assertThat(stat.getFunction(), nullValue());
        stat.setState(PegState.MISSING, "fun 4");
        assertThat(stat.getState(), equalTo(PegState.MISSING));
        assertThat(stat.getFunction(), nullValue());
        assertThat(stat.display("fid 1"), equalTo(PegState.MISSING.display("fid 1")));
    }

}
