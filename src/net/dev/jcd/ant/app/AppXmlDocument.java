package net.dev.jcd.ant.app;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.types.resources.FileResource;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Wrapper for {@link Document}
 * 
 * @author jcdwyer
 * 
 */
public class AppXmlDocument {
	private static final String TAG_APPLICATION = "application"; //$NON-NLS-1$
	private static final String TAG_LIBRARY_DIRECTORY = "library-directory"; //$NON-NLS-1$
	private static final String TAG_JAVA = "java"; //$NON-NLS-1$
	private static final String TAG_EJB = "ejb"; //$NON-NLS-1$
	private static final String TAG_DISPLAY_NAME = "display-name"; //$NON-NLS-1$
	private static final String TAG_MODULE = "module"; //$NON-NLS-1$
	private static final String TAG_WEB = "web"; //$NON-NLS-1$
	private static final String TAG_WEB_URI = "web-uri"; //$NON-NLS-1$
	private static final String TAG_CONTEXT_ROOT = "context-root"; //$NON-NLS-1$

	private String displayName;
	private String libraryDirectory;
	private List<Module> modules;
	private int version;
	private Document doc;

	/**
	 * Construct new AppXmlDocument
	 * 
	 * @param displayName
	 * @param libraryDirectory
	 * @param modules
	 */
	public AppXmlDocument(String displayName, String libraryDirectory, List<Module> modules, int version) {
		super();
		this.displayName = displayName;
		this.libraryDirectory = libraryDirectory;
		this.modules = modules;
		this.version = version;
	}

	/**
	 * Create and return generated {@link Document}
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document createAppXmlDocument() throws ParserConfigurationException {
		// Create Document
		doc = createDocument();
		Comment createdBy = doc.createComment(Messages.getString("AppXml.msg.created.by")); //$NON-NLS-1$
		doc.appendChild(createdBy);

		Element app = doc.createElement(TAG_APPLICATION);
		setApplicationAttributes(app);
		doc.appendChild(app);

		// Add "display-name" element
		app.appendChild(createDisplayElement());

		for (Module module : modules) {
			module.validate();

			for (FileResource fileResource : module.getFiles()) {
				String fileName = fileResource.getFile().getName();

				Element moduleElement = doc.createElement(TAG_MODULE);
				Element childElement = null;
				if (module.isWar()) {
					childElement = createWebElement(fileName, module.getContextRoot());
				} else if (module.isEjb()) {
					childElement = createEjbElement(fileName);
				} else {
					childElement = createJavaElement(fileName);
				}

				// do not save empty module
				if (childElement != null) {
					moduleElement.appendChild(childElement);
					app.appendChild(moduleElement);
				}
			}
		}
		addLibraryDIrectory(app);
		return doc;
	}

	private Element createDisplayElement() {
		Element displayNameElement = doc.createElement(TAG_DISPLAY_NAME);
		displayNameElement.setTextContent(displayName);
		return displayNameElement;
	}

	private Element createJavaElement(String name) {
		Element javaElement = doc.createElement(TAG_JAVA);
		javaElement.setTextContent(name);
		return javaElement;
	}

	private Element createEjbElement(String name) {
		Element ejbElement = doc.createElement(TAG_EJB);
		ejbElement.setTextContent(name);
		return ejbElement;
	}

	private Element createWebElement(String name, String contextRoot) {
		Element webElement = doc.createElement(TAG_WEB);
		Element webUriElement = doc.createElement(TAG_WEB_URI);
		webUriElement.setTextContent(name);
		Element contextRootElement = doc.createElement(TAG_CONTEXT_ROOT);

		contextRootElement.setTextContent(contextRoot);
		webElement.appendChild(webUriElement);
		webElement.appendChild(contextRootElement);
		return webElement;
	}

	private void addLibraryDIrectory(Element app) {
		if (libraryDirectory != null) {
			Element libraryElement = doc.createElement(TAG_LIBRARY_DIRECTORY);
			libraryElement.setTextContent(libraryDirectory);
			app.appendChild(libraryElement);
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
	 * we use attributes instead of dom namespace methods because we need these values<br>
	 * and they are not "valid" namespaces
	 * 
	 * @param app
	 */
	private void setApplicationAttributes(Element app) {
		app.setAttribute("xmlns", "http://java.sun.com/xml/ns/javaee"); //$NON-NLS-1$ //$NON-NLS-2$
		if (version == 6) {
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
		app.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_5.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("version", "5"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Attributes for V6 (JBoss 7)
	 * 
	 * @param app
	 */
	private void setV6Attributes(Element app) {
		app.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_6.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
		app.setAttribute("version", "6"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
