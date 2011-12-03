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
import org.apache.tools.ant.types.LogLevel;
import org.apache.tools.ant.types.resources.FileResource;
import org.w3c.dom.Comment;
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
	private static final String TAG_APPLICATION = "application"; //$NON-NLS-1$
	private static final String TAG_LIBRARY_DIRECTORY = "library-directory"; //$NON-NLS-1$
	private static final String TAG_JAVA = "java"; //$NON-NLS-1$
	private static final String TAG_EJB = "ejb"; //$NON-NLS-1$
	private static final String TAG_DISPLAY_NAME = "display-name"; //$NON-NLS-1$
	private static final String TAG_MODULE = "module"; //$NON-NLS-1$
	private static final String TAG_WEB = "web"; //$NON-NLS-1$
	private static final String TAG_WEB_URI = "web-uri"; //$NON-NLS-1$
	private static final String TAG_CONTEXT_ROOT = "context-root"; //$NON-NLS-1$
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
			
			
			// Create Document
            Document doc = createDocument();
            Comment createdBy = doc.createComment(Messages.getString("AppXml.msg.created.by")); //$NON-NLS-1$
            doc.appendChild(createdBy);

            Element app = doc.createElement(TAG_APPLICATION); 
            setApplicationAttributes(app);
			doc.appendChild(app);
			
			// Add "display-name" element
			Element displayNameElement = doc.createElement(TAG_DISPLAY_NAME); 
			displayNameElement.setTextContent(displayName);
			app.appendChild(displayNameElement);

			for (Module fs : modules) {
				fs.validate();

				for (FileResource fileResource : fs.getFiles()) {
					String name = fileResource.getFile().getName();

					Element moduleElement = doc.createElement(TAG_MODULE); 
					if (fs.isWar()) {
						Element webElement = doc.createElement(TAG_WEB); 
						Element webUriElement = doc.createElement(TAG_WEB_URI); 
						webUriElement.setTextContent(name);
						Element contextRootElement = doc.createElement(TAG_CONTEXT_ROOT); 
						contextRootElement.setTextContent(fs.getContextRoot());
						moduleElement.appendChild(webElement);
						webElement.appendChild(webUriElement);
						webElement.appendChild(contextRootElement);
						
					} else if (fs.isEjb()) {
						Element ejbElement = doc.createElement(TAG_EJB); 
						ejbElement.setTextContent(name);
						moduleElement.appendChild(ejbElement);
					} else {
						Element javaElement = doc.createElement(TAG_JAVA); 
						javaElement.setTextContent(name);
						moduleElement.appendChild(javaElement);
					}
					app.appendChild(moduleElement);
				}
			}
			if (libraryDirectory != null) {
				Element libraryElement = doc.createElement(TAG_LIBRARY_DIRECTORY); 
				libraryElement.setTextContent(libraryDirectory);
				app.appendChild(libraryElement);
			}

			
			// XML Out
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); //$NON-NLS-1$
			trans.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			trans.setOutputProperty(OutputKeys.ENCODING, "ASCII"); //$NON-NLS-1$
			trans.setOutputProperty(OutputKeys.STANDALONE, "yes"); //$NON-NLS-1$
			
			// Save Result
			StreamResult result = new StreamResult(output);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

		} catch (ParserConfigurationException e) {
			log(e, LogLevel.ERR.getLevel());
		} catch (TransformerException e) {
			log(e, LogLevel.ERR.getLevel());
		}
	}

	/**
	 * Create and return a new {@link Document}
	 * 
	 * @return created {@link Document}
	 * @throws ParserConfigurationException
	 */
	private Document createDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		doc.setXmlVersion("1.0"); //$NON-NLS-1$
		return doc;
	}

	/**
	 * Set the application attributes for the name spaces<br>
	 * we use attributes instead of dom namespace methods because we need the values<br>
	 * as they are not "valid" namespaces
	 * 
	 * @param app
	 */
	private void setApplicationAttributes(Element app) {
		app.setAttribute("xmlns","http://java.sun.com/xml/ns/javaee"); //$NON-NLS-1$ //$NON-NLS-2$
		if (getVersion() == 6) {
			setV6Attributes(app);
		} else {
		    setV5Attributes(app);
		}
	}

	/**
	 * Attributes for V5 (JBoss 5/6)
	 * 
	 * @param app
	 */
	private void setV5Attributes(Element app) {
		app.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("xmlns:application", "http://java.sun.com/xml/ns/javaee/application_5.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("xsi:schemaLocation","http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_5.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("version", "5"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Attributes for V6 (JBoss 7)
	 * 
	 * @param app
	 */
	private void setV6Attributes(Element app) {
		app.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("xsi:schemaLocation","http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_6.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("version","6"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
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
