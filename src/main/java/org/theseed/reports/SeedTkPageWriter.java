/**
 *
 */
package org.theseed.reports;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import static j2html.TagCreator.*;

import java.util.stream.Stream;

/**
 * Here the web page is expected to be output in a SEEDtk web environment.  We use standard styles.
 * @author Bruce Parrello
 *
 */
public class SeedTkPageWriter extends PageWriter {

    @Override
    public void writePage(String title, Stream<DomContent> content) {
        System.out.println(document().render());
        ContainerTag body = body().with(content);
        ContainerTag page = html(head(title(title), link().withRel("stylesheet").withHref("css/Basic.css")), body);
        System.out.println(page.render());

    }

}
