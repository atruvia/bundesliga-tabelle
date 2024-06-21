package de.atruvia.ase.samman.buli;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.jmolecules.archunit.JMoleculesArchitectureRules.ensureHexagonal;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = ArchitectureTest.BASE, importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

	static final String BASE = "de.atruvia.ase.samman.buli";

	@ArchTest
	ArchRule noCycles = slices().matching(BASE + ".(*)..").should().beFreeOfCycles();

	@ArchTest
	ArchRule hexagonalArchitecture = ensureHexagonal();

}
