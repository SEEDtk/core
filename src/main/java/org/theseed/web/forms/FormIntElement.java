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
 * This annotation represents a form integer element.  Both int and double fields can use this
 * annotation.  As with FormElement, the name and display label are taken from the associated
 * @Option annotation.
 *
 * @author Bruce Parrello
 *
 */
public @interface FormIntElement {


    /** minimum value for the element */
    int min();

    /** maximum value for the element */
    int max();
}
