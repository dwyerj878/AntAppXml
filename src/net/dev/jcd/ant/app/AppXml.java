package net.dev.jcd.ant.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.apache.tools.ant.types.resources.FileResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	@Override
	public void execute() {
		try {
			if (appXmlFilename == null) {
				throw new BuildException(Messages.getString("AppXml.msg.filename.not.set")); //$NON-NLS-1$
			}
			log("Creating app xml " + appXmlFilename); //$NON-NLS-1$
			File output = new File(appXmlFilename);
			
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            doc.setXmlVersion("1.0");

            // Using attributes to set namespace values because we care about the values not
            // about the validation
            Element app = doc.createElement("application");
            app.setAttribute("xmlns","http://java.sun.com/xml/ns/javaee");
            if (getVersion() == 6) {
        		app.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        		app.setAttribute("xsi:schemaLocation","http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_6.xsd");
        		app.setAttribute("version","6");
            } else {
	            app.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	            app.setAttribute("xmlns:application", "http://java.sun.com/xml/ns/javaee/application_5.xsd");
	            app.setAttribute("xsi:schemaLocation","http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_5.xsd");
				app.setAttribute("version", "5");
            }
			doc.appendChild(app);
			
			
			Element displayNameElement = doc.createElement("display-name");
			displayNameElement.setTextContent(displayName);
			app.appendChild(displayNameElement);

			for (Module fs : modules) {
				fs.validate();

				for (FileResource fileResource : fs.getFiles()) {
					String name = fileResource.getFile().getName();

					Element moduleElement = doc.createElement("module");
					if (fs.isWar()) {
						Element webElement = doc.createElement("web");
						Element webUriElement = doc.createElement("web-uri");
						webUriElement.setTextContent(name);
						Element contextRootElement = doc.createElement("context-root");
						contextRootElement.setTextContent(fs.getContextRoot());
						moduleElement.appendChild(webElement);
						webElement.appendChild(webUriElement);
						webElement.appendChild(contextRootElement);
						
					} else if (fs.isEjb()) {
						Element ejbElement = doc.createElement("ejb");
						ejbElement.setTextContent(name);
						moduleElement.appendChild(ejbElement);
					} else {
						Element javaElement = doc.createElement("java");
						javaElement.setTextContent(name);
						moduleElement.appendChild(javaElement);
					}
					app.appendChild(moduleElement);
				}
			}
			if (libraryDirectory != null) {
				Element libraryElement = doc.createElement("library-directory");
				libraryElement.setTextContent(libraryDirectory);
				app.appendChild(libraryElement);
			}

			
			// XML Out
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			trans.setOutputProperty(OutputKeys.ENCODING, "ASCII");
			
			//create string from xml tree
			
			StreamResult result = new StreamResult(output);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
