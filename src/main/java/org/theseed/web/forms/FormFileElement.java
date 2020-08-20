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
 * This annotation specifies a file-input form element.  The parameter specifies a search pattern for the file in the form of a regular
 * expression.
 *
 * @author Bruce Parrello
 *
 */
public @interface FormFileElement {

    String pattern();
}
