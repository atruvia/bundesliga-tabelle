package de.atruvia.ase.samman.buli.springframework;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * Prevents confusion when annotation the primary port with @Service coming from
 * jmolecules and the implementing class with @Service coming from
 * springframework.
 * 
 * @author Peter Fichtner
 */
@Service
@Retention(RUNTIME)
@Target(TYPE)
public @interface ServiceImplementation {

	/**
	 * The implemented DDD-Service.
	 * 
	 * @return implemented DDD-Service
	 */
	Class<?> value();

}
