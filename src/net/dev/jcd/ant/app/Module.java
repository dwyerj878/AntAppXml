package net.dev.jcd.ant.app;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Module defines an application module.
 * <p>
 * An Application modukle may be of type ejb, war, or java {@link ModuleType}
 * </p>
 * 
 * <p>
 * A Java Module has a reference to a fileset containing one or more files. For each file in the fileset a new Application Module
 * stanza will be created in the applicaiton.xml file
 * </p>
 * &lt;module type="java"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;fileset dir="modules"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;include name="java-module*.jar" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;/fileset&gt;<br>
 * &lt;/module&gt;<br>
 * 
 * <p>
 * An ejb Module has a reference to a fileset containing one or more files. For each file in the fileset a new Application Module
 * stanza will be created in the applicaiton.xml file
 * </p>
 * &lt;module type="ejb"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;fileset dir="modules"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;include name="ejb-module*.jar" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;/fileset&gt;<br>
 * &lt;/module&gt;<br>
 * 
 * <p>
 * A War Module has a reference to a fileset containing one and only one file. A War Module must have a context root
 * </p>
 * &lt;module type="war" contextroot="appRoot"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;fileset dir="modules"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;include name="web-app.war" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;/fileset&gt;<br>
 * &lt;/module&gt;<br>
 * 
 * 
 * 
 */
public class Module extends DataType {

	private ModuleType type;

	private String contextRoott;
	private FileSet fileSet;

	/**
	 * Set the type (WAR/JAR)
	 * 
	 * @param type
	 */
	public void setType(final ModuleType type) {
		this.type = type;
	}

	/**
	 * Set the base context if war
	 * 
	 * @param contextRoot
	 */
	public void setContextRoot(final String contextRoot) {
		this.contextRoott = contextRoot;
	}

	/**
	 * Check that all values are set correctly
	 */
	public void validate() {
		if (type == null) {
			throw new BuildException("Type is not set");
		}
		if (contextRoott == null && isWar()) {
			throw new BuildException("basecontext is required for war modules");
		}
	}

	/**
	 * Add a new {@link FileSet}
	 * 
	 * @return created {@link FileSet}
	 */
	public FileSet createFileSet() {

		if (fileSet != null) {
			throw new BuildException("Only one fileset per module");
		}
		this.fileSet = new FileSet();
		return fileSet;
	}

	/**
	 * 
	 *
	 */
	public static class ModuleType extends EnumeratedAttribute {
		@Override
		public String[] getValues() {
			return new String[] { "war", "ejb", "java" };
		}
	}

	/**
	 * Get files from file set<br>
	 * <p>
	 * if module is war, then fileset must only contain 1 file
	 * </p>
	 * <p>
	 * java and ejb modules should contain at least one file
	 * </p>
	 * 
	 * @return list {@link FileResource}, throw exception if 0 incorrect number of files
	 */
	public ArrayList<FileResource> getFiles() {

		ArrayList<FileResource> fileNames = new ArrayList<FileResource>();

		@SuppressWarnings("unchecked")
		Iterator<FileResource> it = fileSet.iterator();
		while (it.hasNext()) {
			FileResource next = it.next();
			fileNames.add(next);

			getProject().log(next.toString());

		}

		String moduleDescription = "No Description";
		if (fileSet.getDescription() != null) {
			moduleDescription = fileSet.getDescription();
		}

		if (fileNames.size() > 1 && isWar()) {
			throw new BuildException("War Module fileset should only contain one file (" + moduleDescription + ")");
		} else if (fileNames.size() == 0 && isWar()) {
			throw new BuildException("War Module fileset should contain exactly one file (" + moduleDescription + ")");
		} else if (fileNames.size() == 0) {
			throw new BuildException("Module fileset should contain at least one file (" + moduleDescription + ")");
		}

		return fileNames;
	}

	/**
	 * @return the root context for a war
	 */
	public String getContextRoot() {
		return contextRoott;
	}

	/**
	 * @return true if java module
	 */
	public boolean isJava() {
		return "java".equals(type.getValue());
	}

	/**
	 * @return true if ejb module
	 */
	public boolean isEjb() {
		return "ejb".equals(type.getValue());
	}

	/**
	 * Check if war module
	 * 
	 * @return true if war
	 */
	public boolean isWar() {
		return "war".equals(type.getValue());
	}

}
