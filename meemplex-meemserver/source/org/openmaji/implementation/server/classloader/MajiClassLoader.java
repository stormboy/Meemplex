/*
 * @(#)MajiClassLoader.java
 *
 * Copyright 2004 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */
package org.openmaji.implementation.server.classloader;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openmaji.implementation.server.Common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author mg
 * @author stormboy
 */
public class MajiClassLoader extends URLClassLoader {

	public static final String CLASSLOADER_DEBUG = "org.openmaji.server.classloader.debug";

	public static final String CLASSPATH_FILE = "org.openmaji.server.classpath";

	private static final String DEFAULT_CLASSPATH_FILE = "conf/maji-classpath.xml";

	private static final String MAJI_CLASSLOADER_PACKAGE = "org.openmaji.implementation.server.classloader";

	private static final String MAJI_ECLIPSE_PACKAGE = "org.openmaji.implementation.tool.eclipse";

	private boolean debug = false;

	private String majitekDirectoryBaseURL;

	public MajiClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);

		String debug = System.getProperty(CLASSLOADER_DEBUG);
		if (debug != null && debug.equalsIgnoreCase("true")) {
			this.debug = true;
			System.err.println("MajiClassLoader debug on");
		}

		if (this.debug) {
			System.err.println("+++ Creating server MajiClassLoader");
		}

		SystemExportList.getInstance().setMajiClassLoader(this);

		loadClassPaths();
	}

	public void loadClassPaths() {

		String cp = System.getProperty(CLASSPATH_FILE);
		if (cp == null) {
			System.setProperty(CLASSPATH_FILE, DEFAULT_CLASSPATH_FILE);
			cp = DEFAULT_CLASSPATH_FILE;
			// String msg = "Classpath file property \"" + CLASSPATH_FILE +
			// "\" not specified";
			// throw new RuntimeException(msg);
		}

		File classpathFile = new File(cp);
		if (!classpathFile.isAbsolute()) {
			classpathFile = new File(System.getProperty(Common.PROPERTY_MAJI_HOME) + System.getProperty("file.separator") + cp);
			cp = classpathFile.getPath();
		}

		if (!classpathFile.exists()) {
			throw new RuntimeException("Classpath file doesn't exist: " + cp);
		}

		majitekDirectoryBaseURL = classpathFile.getParentFile().getPath().replace('\\', '/');
		if (debug) {
			System.err.println("MajiClassLoader majitekDirectoryBaseURL : " + majitekDirectoryBaseURL);
		}

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document doc = builder.parse(classpathFile);

			NodeList jarsList = doc.getDocumentElement().getElementsByTagName("path");

			for (int i = 0; i < jarsList.getLength(); i++) {
				Element path = (Element) jarsList.item(i);

				String filePath = path.getAttribute("value");

				if (filePath.startsWith("http://") == false) {
					File file = new File(path.getAttribute("value"));
					if (!file.exists()) {
						file = new File(majitekDirectoryBaseURL + "/" + path.getAttribute("value"));
						if (debug)
							System.err.println("MajiClassLoader, trying path : " + majitekDirectoryBaseURL + "/" + path.getAttribute("value"));
					}

					if (!file.exists()) {
						System.err.println("Error parsing classpath file: Incorrect path value: " + path.getAttribute("value"));
						continue;
					}

					filePath = file.getCanonicalPath().replace('\\', '/');
				}

				boolean isJar = true;
				if (!(filePath.endsWith(".zip") || filePath.endsWith(".jar"))) {
					isJar = false;
					if (!filePath.endsWith("/")) {
						filePath += "/";
					}
				}

				URL url = makeURL(filePath);

				URLConnection c = url.openConnection();

				if (c == null) {
					System.err.println("Incorrect path value: " + url);
					continue;
				}
				else {
					try {
						c.connect();
					}
					catch (IOException ex) {
						System.err.println("Incorrect path value: " + url);
						continue;
					}
				}
				if (debug) {
					System.err.println("MajiClassLoader addURL: " + url);
				}

				// add urls
				// System.err.println("server addURL(" + url + ")");
				addURL(url);

				if (isJar) {
					parseJarFile(url);
				}
				else {
					parsePath(filePath);
				}
			}
		}
		catch (Exception e) {
			System.err.println("Exception parsing classpath file: " + e.getMessage());
		}
	}

	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (name.startsWith(MAJI_CLASSLOADER_PACKAGE) || name.startsWith(MAJI_ECLIPSE_PACKAGE)) {
			return super.loadClass(name, resolve);
		}

		if (debug) {
			System.err.println("MajiClassLoader.loadClass: " + name + " : " + resolve + " : " + this);
		}

		Class<?> c = findLoadedClass(name);	// must do this outside of synchronization

		if (c == null) {
			// TODO this synch has been commented out by Warren 5/5/2011 - was causing deadlocks
//			synchronized (name.intern()) {
				c = findClass(name);
//			}
		}

		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	/**
	 * @see java.net.URLClassLoader#findClass(java.lang.String)
	 */
	protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
		if (debug) {
			System.err.println("MajiClassLoader.findClass: " + name + " : " + this);
		}

//		Class<?> clazz = findLoadedClass(name);
//		if (clazz != null) {
//			return clazz;
//		}
	
		Class<?> clazz = null;
		try {
			// check system
			clazz = findSystemClass(name);
		}
		catch (ClassNotFoundException e) {
			try {
				clazz = super.findClass(name);
			}
			catch (ClassNotFoundException ex) {
				ClassLoader classLoader = SystemExportList.getInstance().getClassLoaderFor(name);
				if (classLoader != null && classLoader != this) {
					clazz = ((MeemkitClassLoader) classLoader).findClass(name);
				}
			}
		}
		if (clazz == null) {
			throw new ClassNotFoundException(name);
		}
		
		return clazz;
	}

	public Package getPackage(String packageName) {
		return super.getPackage(packageName);
	}

	public Package[] getPackages(String packageName) {
		return super.getPackages();
	}
	
	private URL makeURL(String location) {

		try {
			if (location.startsWith("http://")) {
				//return (new URL("http", "zig", 80, location.substring(10)));
				return (new URL(location));
			}

			return new URL("file", null, 0, location);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return (null);
	}
	
	private void parseJarFile(URL jarUrl) {

		URL url = null;
		try {
			url = new URL("jar:" + jarUrl + "!/");
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		try {
			JarURLConnection jarConnection = (JarURLConnection) url.openConnection();

			JarFile jarFile = jarConnection.getJarFile();

			Set<String> exportSet = new HashSet<String>();

			Enumeration<JarEntry> e = jarFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();

				if (!ze.isDirectory()) {
					String name = ze.getName().replace('/', '.');
					if (name.endsWith(".class")) {
						name = name.substring(0, name.length() - 6);
						exportSet.add(name);
						if (debug)
							System.err.println("MajiClassLoader.parseJarFile: " + name);
					}
				}
			}

			SystemExportList.getInstance().addMajiClassLoaderExport(exportSet);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parsePath(String path) {

		File file = new File(path);
		Collection<File> files = getFileListing(file);
		Set<String> exportSet = new HashSet<String>();

		Iterator<File> i = files.iterator();
		while (i.hasNext()) {
			File f = i.next();
			if (f.getName().endsWith(".class")) {
				String className = f.getPath().substring(file.getPath().length() + 1, f.getPath().length() - 6);
				className = className.replace(File.separatorChar, '.');
				exportSet.add(className);
				if (debug) {
					System.err.println("MajiClassLoader.parsePath: " + className);
				}
			}
		}

		SystemExportList.getInstance().addMajiClassLoaderExport(exportSet);
	}

	static public Collection<File> getFileListing(File startingDir) {

		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		Iterator<File> filesIter = filesDirs.iterator();
		File file = null;
		while (filesIter.hasNext()) {
			file = filesIter.next();
			result.add(file);
			if (file.isDirectory()) {
				Collection<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			}

		}
		return result;
	}
}
