package main.annotations;

/**
 * Any method marked as a service method will start its own thread tree, and
 * have the Service method being the base node. Now a service method can either
 * be marked as daemon or not. This will result in all threads spawned from the
 * annotation to be marked as daemon.
 * 
 * @author Nathaniel Cotton
 * 
 */
public @interface Service {

	boolean daemon() default false;

}
