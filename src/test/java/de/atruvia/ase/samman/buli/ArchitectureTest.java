package de.atruvia.ase.samman.buli;

import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static java.lang.String.format;
import static org.jmolecules.archunit.JMoleculesArchitectureRules.ensureHexagonal;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;

import de.atruvia.ase.samman.buli.springframework.PrimaryPortImplementation;

@AnalyzeClasses(packages = ArchitectureTest.BASE, importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

	static final String BASE = "de.atruvia.ase.samman.buli";

	@ArchTest
	ArchRule noCycles = slices().matching(BASE + ".(*)..").should().beFreeOfCycles();

	@ArchTest
	ArchRule hexagonalArchitecture = ensureHexagonal();

	@ArchTest
	ArchRule primaryPortImplementationHasToImplementThePrimaryPort = classes().that()
			.areAnnotatedWith(PrimaryPortImplementation.class).should(implementTheSpecifiedInterface());

	private static ArchCondition<? super JavaClass> implementTheSpecifiedInterface() {
		return new ArchCondition<>("implement the specified interface") {

			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				PrimaryPortImplementation annotation = javaClass.getAnnotationOfType(PrimaryPortImplementation.class);
				if (annotation != null) {
					String specifiedInterfaceName = annotation.value().getName();
					if (!implementsInterface(javaClass, specifiedInterfaceName)) {
						events.add(violated(javaClass, format("%s does not implement the specified interface %s",
								javaClass.getName(), specifiedInterfaceName)));
					}
				}
			}

			private boolean implementsInterface(JavaClass javaClass, String specifiedInterfaceName) {
				return javaClass.getInterfaces().stream().map(JavaType::getName)
						.anyMatch(specifiedInterfaceName::equals);
			}

		};
	}

}
