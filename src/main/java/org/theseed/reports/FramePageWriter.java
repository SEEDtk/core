/**
 *
 */
package org.theseed.reports;

import static j2html.TagCreator.*;

import java.util.stream.Stream;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.EmptyTag;

/**
 * This is for a page being output to an iFrame.  We eschew all the heading and title stuff.
 *
 * @author Bruce Parrello
 */
public class FramePageWriter extends PageWriter {

    /** style sheet link */
    public static final EmptyTag FRAME_STYLES = link().withRel("stylesheet").withHref("/css/Frame.css");

    @Override
    protected void writePage(String title, DomContent heading, Stream<DomContent> content) {
        System.out.println(document().render());
        ContainerTag body = body().with(content);
        ContainerTag page = html(head(title(title), FRAME_STYLES),
                body);
        System.out.println(page.render());
        System.out.flush();
    }

    @Override
    public String local_url(String url) {
        return url;
    }

}
