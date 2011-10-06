package net.dev.jcd.ant.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * 
 * 
 * @author jcdwyer
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
	 * Get file from file set
	 * 
	 * 
	 * @return file, throw exception if more or less than 1 file
	 */
	public File getFile() {

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

		if (fileNames.size() > 1) {
			throw new BuildException("Module fileset should only contain one file (" + moduleDescription + ")");
		}
		if (fileNames.size() == 0) {
			throw new BuildException("Module fileset should contain one file (" + moduleDescription + ")");
		}
		return fileNames.get(0).getFile();
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
