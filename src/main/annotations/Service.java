package main.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any method marked as a service method will start its own thread tree, and
 * have the Service method being the base node. Now a service method can either
 * be marked as daemon or not. This will result in all threads spawned from the
 * annotation to be marked as daemon.
 * 
 * @author Nathaniel Cotton
 * 
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

	boolean daemon() default false;

}
