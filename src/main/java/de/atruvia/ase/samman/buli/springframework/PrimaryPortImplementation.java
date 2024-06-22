package de.atruvia.ase.samman.buli.springframework;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * Prevents confusion when annotation the primary port with
 * {@link org.jmolecules.ddd.annotation.Service} coming from jmolecules and the
 * implementing class with {@link org.springframework.stereotype.Service} coming
 * from springframework.
 * 
 * @author Peter Fichtner
 */
@Service
@Retention(RUNTIME)
@Target(TYPE)
public @interface PrimaryPortImplementation {

	/**
	 * The implemented DDD-Service.
	 * 
	 * @return implemented DDD-Service
	 */
	Class<?> value();

}
