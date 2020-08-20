/**
 *
 */
package org.theseed.web;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.theseed.test.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Test the cookie-file
 *
 * @author Bruce Parrello
 *
 */
public class CookieFileTest extends TestCase {

    public void testCookieFile() throws IOException {
        File wsDir = new File("src/test/resources", "Workspace");
        // Make sure we delete the cookie file.
        File cookieFileName = new File(wsDir, "_testCookies.cookie.tbl");
        if (cookieFileName.exists())
            FileUtils.forceDelete(cookieFileName);
        double dTest;
        try (CookieFile cookies = new CookieFile(wsDir, "testCookies")) {
            int iTest = cookies.get("iTest", 10);
            dTest = cookies.get("dTest", 1.0 / 3.0);
            String sTest = cookies.get("sTest", "string thing");
            boolean tTest = cookies.get("tTest", true);
            boolean fTest = cookies.get("fTest", false);
            assertThat(iTest, equalTo(10));
            assertThat(dTest, closeTo(0.333333, 0.000001));
            assertThat(sTest, equalTo("string thing"));
            assertThat(tTest, isTrue());
            assertThat(fTest, isFalse());
            cookies.put("sTest", "thing string");
            cookies.put("iTest", 20);
        }
        // Test reading it back in.
        try (CookieFile cookies = new CookieFile(wsDir, "testCookies")) {
            int iTest = cookies.get("iTest", 10);
            double dTest2 = cookies.get("dTest", 0.0);
            String sTest = cookies.get("sTest", "string thing");
            boolean tTest = cookies.get("tTest", false);
            boolean fTest = cookies.get("fTest", true);
            assertThat(iTest, equalTo(20));
            assertThat(dTest2, equalTo(dTest));
            assertThat(sTest, equalTo("thing string"));
            assertThat(tTest, isTrue());
            assertThat(fTest, isFalse());
            cookies.put("iTest", 99);
            assertThat(cookies.get("iTest", 10), equalTo(99));
            cookies.put("sTest", "Florida String");
            assertThat(cookies.get("sTest", ""), equalTo("Florida String"));
            cookies.put("dTest2", 12.6);
            assertThat(cookies.get("dTest2", 0.0), equalTo(12.6));
            cookies.put("tTest", false);
            assertThat(cookies.get("tTest", true), isFalse());
            assertThat(cookies.get("fTest", true), isFalse());
            assertThat(cookies.get("iTest", 10), equalTo(99));
            cookies.put("fTest", true);
            assertThat(cookies.get("fTest", false), isTrue());
        }
        // Final read-back.
        try (CookieFile cookies = new CookieFile(wsDir, "testCookies")) {
            assertThat(cookies.get("iTest", 10), equalTo(99));
            assertThat(cookies.get("sTest", ""), equalTo("Florida String"));
            assertThat(cookies.get("dTest2", 0.0), equalTo(12.6));
            assertThat(cookies.get("tTest", true), isFalse());
            assertThat(cookies.get("fTest", false), isTrue());
        }
    }
}
