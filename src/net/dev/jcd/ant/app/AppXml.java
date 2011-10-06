package net.dev.jcd.ant.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * Ant task to create a JBoss Application XML file
 * 
 * 
 * @author jcdwyer
 * 
 */
public class AppXml extends Task {
	private static final String APPLICATION_END = "</application>";
	private static final String APPLICATION_START_J2EE_5_HEADER = "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	        + "\n\t\txmlns=\"http://java.sun.com/xml/ns/javaee\" "
	        + "\n\t\txmlns:application=\"http://java.sun.com/xml/ns/javaee/application_5.xsd\" "
	        + "\n\t\txsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_5.xsd\" "
	        + "\n\t\tversion=\"5\">";

	private static final String APPLICATION_START_J2EE_6_HEADER = "<application xmlns=\"http://java.sun.com/xml/ns/javaee\" "
	        + "\n\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	        + "\n\t\tversion=\"6\" "
	        + "\n\t\txsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_6.xsd\">";

	private static final String CONTEXT_ROOT_END = "</context-root>";

	private static final String CONTEXT_ROOT_START = "<context-root>";

	private static final String DISPLAY_NAME_END = "</display-name>";

	private static final String DISPLAY_NAME_START = "<display-name>";

	private static final String EJB_END = "</ejb>";
	private static final String EJB_START = "<ejb>";
	private static final String INDENT_1 = "\t";
	private static final String INDENT_2 = "\t\t";
	private static final String INDENT_3 = "\t\t\t";
	private static final String INDENT_4 = "\t\t\t\t";
	private static final String J2EE_VERSION_5 = "5";
	private static final String J2EE_VERSION_6 = "6";
	private static final String JAVA_END = "</java>";
	private static final String JAVA_START = "<java>";
	private static final String LIBRARY_DIRECTORY_END = "</library-directory>";
	private static final String LIBRARY_DIRECTORY_START = "<library-directory>";
	private static final String MODULE_END = "</module>";
	private static final String MODULE_START = "<module>";
	private static final String WEB_END = "</web>";
	private static final String WEB_START = "<web>";
	private static final String WEB_URI_END = "</web-uri>";
	private static final String WEB_URI_START = "<web-uri>";
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ASCII\"?>";

	private String appXmlFilename;
	private String displayName;
	private String libraryDirectory;
	private List<Module> modules;

	private J2EEVersion version;

	/**
	 * Default constructor Called by ant
	 */
	public AppXml() {
		modules = new ArrayList<Module>();
	}

	/**
	 * Create and add a new {@link Module}
	 * 
	 * @return created file set
	 */
	public Module createModule() {
		Module module = new Module();
		modules.add(module);
		return module;
	}

	@Override
	public void execute() {
		try {
			if (appXmlFilename == null) {
				throw new BuildException("AppXmlFilename not Set");
			}
			log("Creating app xml " + appXmlFilename);
			File output = new File(appXmlFilename);
			PrintStream bus = new PrintStream(output);

			bus.println(XML_HEADER);

			if (getVersion() == 6) {
				bus.println(APPLICATION_START_J2EE_6_HEADER);
			} else {
				bus.println(APPLICATION_START_J2EE_5_HEADER);
			}

			bus.println(INDENT_1 + DISPLAY_NAME_START);
			bus.print(INDENT_2);
			bus.println(displayName);
			bus.println(INDENT_1 + DISPLAY_NAME_END);

			for (Module fs : modules) {
				fs.validate();

				bus.println(INDENT_1 + MODULE_START);
				String name = fs.getFile().getName();
				if (fs.isWar()) {
					bus.println(INDENT_2 + WEB_START);
					bus.println(INDENT_3 + WEB_URI_START);
					bus.print(INDENT_4);
					bus.println(name);
					bus.println(INDENT_3 + WEB_URI_END);

					bus.println(INDENT_3 + CONTEXT_ROOT_START);
					bus.print(INDENT_4);
					bus.println(fs.getContextRoot());
					bus.println(INDENT_3 + CONTEXT_ROOT_END);
					bus.println(INDENT_2 + WEB_END);
				} else if (fs.isEjb()) {
					bus.println(INDENT_2 + EJB_START);
					bus.print(INDENT_3);
					bus.println(name);
					bus.println(INDENT_2 + EJB_END);
				} else {
					bus.println(INDENT_2 + JAVA_START);
					bus.print(INDENT_3);
					bus.println(name);
					bus.println(INDENT_2 + JAVA_END);
				}
				bus.println(INDENT_1 + MODULE_END);

			}
			if (libraryDirectory != null) {
				bus.println(INDENT_1 + LIBRARY_DIRECTORY_START);
				bus.print(INDENT_2);
				bus.println(libraryDirectory);
				bus.println(INDENT_1 + LIBRARY_DIRECTORY_END);

			}
			bus.println(APPLICATION_END);
			bus.close();

		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
			throw new RuntimeException(exception);
		}
	}

	private int getVersion() {
		if (version != null && J2EE_VERSION_6.equalsIgnoreCase(version.getValue())) {
			return 6;
		}
		return 5;
	}

	/**
	 * Set the output file name
	 * 
	 * @param appXmlFilename
	 */
	public void setAppXml(final String appXmlFilename) {
		this.appXmlFilename = appXmlFilename;
	}

	/**
	 * Set the display name as it will appear in the applicaiton.xml file
	 * 
	 * @param displayName
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Set the j2ee version to 5 or 6 (changes the header)
	 * 
	 * @param version
	 */
	public void setJ2EEVersion(final J2EEVersion version) {
		this.version = version;
	}

	/**
	 * Set the library Directory
	 * 
	 * @param libraryDirectory
	 */
	public void setLibraryDirectory(final String libraryDirectory) {
		this.libraryDirectory = libraryDirectory;
	}

	/**
	 * Enumeration for J2EE Version
	 * 
	 * 
	 */
	public static class J2EEVersion extends EnumeratedAttribute {

		@Override
		public String[] getValues() {
			return new String[] { J2EE_VERSION_5, J2EE_VERSION_6 };
		}
	}

}
