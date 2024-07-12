package de.atruvia.ase.samman.buli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class Main {

	public static void main(String... args) {
		SpringApplication.run(Main.class, args);
	}

}
