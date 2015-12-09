package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Any method or field annotated with @Sync will get a ContextSemaphore from the
 * ThreadingConstraints that will only allow one thread in at a time.
 * 
 * @author Nathaniel Cotton
 * 
 */
@Target(value = { ElementType.METHOD, ElementType.FIELD })
public @interface Sync {

}
