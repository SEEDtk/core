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
 * This form element represents part of a BLAST source specification.  Each consists of a type selection box and a file box.
 * The form element must be on both of the target variables with the same id.
 * @author Bruce Parrello
 *
 */
public @interface FormBlastElement {

}
