package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Classes annotated with the @Shell annotation will have a new field injected
 * into the class definition that will help with the return problem.
 * 
 * @author Nathaniel Cotton
 * 
 */
@Target(value = { ElementType.TYPE })
public @interface Shell {

}
