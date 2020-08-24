/**
 *
 */
package org.theseed.reports;

import java.util.stream.Stream;
import static j2html.TagCreator.*;

import j2html.tags.DomContent;

/**
 * This is the page writer for the case where the web page is called from within another page.  In this
 * case, we expect a calling program to put out the prefix and suffix.
 *
 * @author Bruce Parrello
 *
 */
public class InternalPageWriter extends PageWriter {

    @Override
    public void writePage(String title, DomContent heading, Stream<DomContent> content) {
        // We are internal to another page, so the title is ignored.
        System.out.println(h1(heading));
        content.forEach(x -> System.out.println(x));
    }

    @Override
    public String local_url(String url) {
        return url;
    }


}
