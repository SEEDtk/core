/**
 *
 */
package org.theseed.web.forms;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
/**
 * This form element represents a dropdown box that is initialized from a file.  The file must be in the CoreSEED data directory,
 * and the "file" option specifies the file name. As with FormElement, the name and display label are taken from the associated
 * @Option annotation.
 *
 * @author Bruce Parrello
 *
 */
public @interface FormMapElement {

    /** name of the map file */
    String file();

}
