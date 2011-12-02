package net.dev.jcd.ant.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Ant task to create a JBoss Application XML file
 * 
 * 
 * @author jcdwyer
 * 
 */
public class AppXml extends Task {
	private static final String INDENT_1 = "\t"; //$NON-NLS-1$
	private static final String INDENT_2 = "\t\t"; //$NON-NLS-1$
	private static final String INDENT_3 = "\t\t\t"; //$NON-NLS-1$
	private static final String INDENT_4 = "\t\t\t\t"; //$NON-NLS-1$
	private static final String J2EE_VERSION_5 = "5"; //$NON-NLS-1$
	private static final String J2EE_VERSION_6 = "6"; //$NON-NLS-1$
	

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
				throw new BuildException(Messages.getString("AppXml.msg.filename.not.set")); //$NON-NLS-1$
			}
			log("Creating app xml " + appXmlFilename); //$NON-NLS-1$
			File output = new File(appXmlFilename);
			PrintStream bus = new PrintStream(output);

			bus.println(Messages.getString("AppXml.xml.header")); //$NON-NLS-1$

			if (getVersion() == 6) {
				bus.println(Messages.getString("AppXml.tag.application.j2ee6")); // $NON-NLS-2$
			} else {
				bus.println(Messages.getString("AppXml.tag.application.j2ee5")); // $NON-NLS-1$
			}

			bus.println(INDENT_1 + Messages.getString("AppXml.tag.display.name")); //$NON-NLS-1$;
			bus.print(INDENT_2);
			bus.println(displayName);
			bus.println(INDENT_1 + Messages.getString("AppXml.tag.display.name.end")); //$NON-NLS-1$

			for (Module fs : modules) {
				fs.validate();

				for (FileResource fileResource : fs.getFiles()) {
					String name = fileResource.getFile().getName();
					bus.println(INDENT_1 + Messages.getString("AppXml.tag.module")); //$NON-NLS-1$;

					if (fs.isWar()) {
						bus.println(INDENT_2 + Messages.getString("AppXml.tag.web")); //$NON-NLS-1$
						bus.println(INDENT_3 + Messages.getString("AppXml.tag.web.uri")); //$NON-NLS-1$;
						bus.print(INDENT_4);
						bus.println(name);
						bus.println(INDENT_3 + Messages.getString("AppXml.tag.web.uri.end")); //$NON-NLS-1$

						bus.println(INDENT_3 + Messages.getString("AppXml.tag.context.root")); //$NON-NLS-1$;
						bus.print(INDENT_4);
						bus.println(fs.getContextRoot());
						bus.println(INDENT_3 + Messages.getString("AppXml.tag.context.root.end")); //$NON-NLS-1$
						bus.println(INDENT_2 + Messages.getString("AppXml.tag.web.end")); //$NON-NLS-1$;
					} else if (fs.isEjb()) {
						bus.println(INDENT_2 + Messages.getString("AppXml.tag.ejb")); //$NON-NLS-1$;
						bus.print(INDENT_3);
						bus.println(name);
						bus.println(INDENT_2 + Messages.getString("AppXml.tag.ejb.end")); // $NON-NLS-1$
					} else {
						bus.println(INDENT_2 + Messages.getString("AppXml.tag.java")); //$NON-NLS-1$
						bus.print(INDENT_3);
						bus.println(name);
						bus.println(INDENT_2 + Messages.getString("AppXml.tag.java.end")); //$NON-NLS-1$
					}
					bus.println(INDENT_1 + Messages.getString("AppXml.tag.module.end")); //$NON-NLS-1$;

				}
			}
			if (libraryDirectory != null) {
				bus.println(INDENT_1 + Messages.getString("AppXml.tag.library.dir")); //$NON-NLS-1$
				bus.print(INDENT_2);
				bus.println(libraryDirectory);
				bus.println(INDENT_1 + Messages.getString("AppXml.tag.library.dir.end")); //$NON-NLS-1$

			}
			bus.println(Messages.getString("AppXml.tag.application.end")); // $NON-NLS-1$
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
