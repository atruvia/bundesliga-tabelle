package de.atruvia.ase.samman.buli;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.metaAnnotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.jmolecules.architecture.hexagonal.Adapter;
import org.jmolecules.architecture.hexagonal.Port;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = ArchitectureTest.BASE, importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

	static final String BASE = "de.atruvia.ase.samman.buli";

	@ArchTest
	ArchRule noCycles = slices().matching(BASE + ".(*)..") //
			.should().beFreeOfCycles() //
	;

	@ArchTest
	ArchRule portsAndAdapters = onionArchitecture() //
			.withOptionalLayers(true) //
			.domainModels("..domain..") //
			.adapter("primary-adapters", metaAnnotatedWith(PrimaryAdapter.class)) //
			.adapter("secondary-adapters", metaAnnotatedWith(SecondaryAdapter.class)) //
	;

	@ArchTest
	ArchRule noDomainToAdapterDependencies = noClasses().that().resideInAPackage("..domain..") //
			.should().dependOnClassesThat().areMetaAnnotatedWith(Adapter.class) //
	;

	@ArchTest
	ArchRule noPortToAdapterDependencies = noClasses().that().areMetaAnnotatedWith(Port.class) //
			.should().dependOnClassesThat().areMetaAnnotatedWith(Adapter.class) //
	;

}
