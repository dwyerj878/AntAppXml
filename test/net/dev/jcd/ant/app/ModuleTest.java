package net.dev.jcd.ant.app;

import static org.junit.Assert.fail;
import net.dev.jcd.ant.app.Module.ModuleType;

import org.apache.tools.ant.BuildException;
import org.junit.Before;
import org.junit.Test;

public class ModuleTest {

	private Module module;

	@Before
	public void setup() {
		module = new Module();
	}

	/**
	 * If we try and validate a module with no type {@link BuildException} should be thrown
	 */
	@Test(expected = BuildException.class)
	public void moduleIsNotValidIfTypeIsNull() {
		module.setType(null);
		module.validate();
	}

	/**
	 * Jar modules have no validation
	 */
	@Test
	public void moduleIsValidIfTypeIsJar() {
		ModuleType type = new ModuleType();
		type.setValue("java");
		module.setType(type);
		try {
			module.validate();
		} catch (Exception ex) {
			fail("Module should be valid");
		}
	}

	/**
	 * ejb modules have no validation
	 */
	@Test
	public void moduleIsValidIfTypeIsEJB() {
		ModuleType type = new ModuleType();
		type.setValue("ejb");
		module.setType(type);
		try {
			module.validate();
		} catch (Exception ex) {
			fail("Module should be valid");
		}
	}

	/**
	 * war modules must have a context root
	 */
	@Test(expected = BuildException.class)
	public void moduleIsInValidIfTypeIsWarButNoContextRoot() {
		ModuleType type = new ModuleType();
		type.setValue("war");
		module.setType(type);

		module.validate();
	}

	/**
	 * war modules must have a context root
	 */
	@Test
	public void moduleIsInValidIfTypeIsWarAndHasContextRoot() {
		ModuleType type = new ModuleType();
		type.setValue("war");
		module.setContextRoot("webRoot");
		module.setType(type);
		try {
			module.validate();
		} catch (Exception ex) {
			fail("Module should be valid");
		}
	}

	/**
	 * adding multiple file sets should fail
	 */
	@Test(expected = BuildException.class)
	public void moduleShouldFailIfMultipleFilesetsAreAdded() {
		module.createFileSet();
		module.createFileSet();
	}

	public void moduleShouldFailIf() {

		// http://ant.apache.org/antlibs/antunit/index.html

	}
}
