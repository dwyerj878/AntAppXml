package net.dev.jcd.ant.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.LogLevel;

/**
 * Ant task to create a JBoss Application XML file
 * 
 * 
 * @author jcdwyer
 * 
 */
public class AppXml extends Task {
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

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() {
		try {
			if (appXmlFilename == null) {
				throw new BuildException(Messages.getString("AppXml.msg.filename.not.set")); //$NON-NLS-1$
			}
			log("Creating app xml " + appXmlFilename); //$NON-NLS-1$
			File output = new File(appXmlFilename);
			
			AppXmlDocument d = new AppXmlDocument(displayName, libraryDirectory, modules, getVersion());
			
			// XML Out
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); //$NON-NLS-1$
			trans.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			trans.setOutputProperty(OutputKeys.ENCODING, "ASCII"); //$NON-NLS-1$
			trans.setOutputProperty(OutputKeys.STANDALONE, "yes"); //$NON-NLS-1$
			
			// Save Result
			StreamResult result = new StreamResult(output);
			DOMSource source = new DOMSource(d.createAppXmlDocument());
			trans.transform(source, result);

		} catch (ParserConfigurationException e) {
			log(e, LogLevel.ERR.getLevel());
		} catch (TransformerException e) {
			log(e, LogLevel.ERR.getLevel());
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
