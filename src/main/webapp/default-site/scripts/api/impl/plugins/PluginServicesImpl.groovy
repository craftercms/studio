/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripts.api.impl.plugins

import scripts.api.ContentServices
import scripts.api.SiteServices

import groovy.json.JsonSlurper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.List
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.jar.JarInputStream
import java.util.jar.Manifest
import java.util.jar.Attributes

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import groovy.io.FileType

import scripts.api.ContentServices
import scripts.api.SiteServices

import groovy.xml.XmlUtil

/**
 * Plugin Service allows studio to install plugins, get and manipulate plugin states
 */
public class PluginServicesImpl {

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
   	def PluginServicesImpl(context) {
        this.context = context
    }

	/**
	 * Install a plugin from romote source.  
	 * @param siteId, the site where the plugin will be installed
	 * @param pluginDownloadUrl, url where plugin can be acquired
	 * @return true if success, false otherise
	 */
	def installPlugin(siteId, pluginDownloadUrl) {
		def downloadUrl = pluginDownloadUrl
		def filename = downloadUrl.substring(downloadUrl.lastIndexOf("/")+1)

		download(downloadUrl, filename)

		def props = readManifest(filename)

		logManifestDetails(props)

		if(props.id == null) {
			props.id = filename.substring(0,filename.lastIndexOf("."))
		}

		def unzipPath = "./plugin-install/"+props.id

		unzip(unzipPath, filename)

		importPlugin(unzipPath, props, siteId)

		return true
	}

	/** 
	 * given a siteId, return a list of installed plugins
	 * @param siteId, the site where the plugin will be installed
     * @return an array of installed plugin items
	 */
	def getInstalledPlugins(siteId) {
		def plugins = [:]

		def pluginItems = ContentServices.getContentItemTree(siteId, "/site/plugins", 2, context).children

		if(pluginItems) {
			pluginItems.each {  pluginItem ->
				def plugin = [:] 
				plugin.descriptorId = pluginItem.uri
				plugin.id = pluginItem.name.replace(".xml", "")
				plugin.name = pluginItem.internalName

				plugins.put(plugin.id, plugin)
			}
		}

		return plugins
	}

	/**
	 * register that a plugin has been installed
	 */
	def registerPlugin(siteId, props) {
		// note the plugin in the plugin registry
		def registryPluginDescriptorPath = "/site/plugins/"

			String registryDecriptor = "" + \
			 "<component>" + \
				"<filename>" + props.id + ".xml</filename>" + \
				"<internal-name>" + props.name + "</internal-name>" + \
				"<iplugin-id>" + props.id + "</iplugin-id>" + \
				"<plugin-name>" + props.name + "</plugin-name>" + \
				"<plugin-version>" + props.version + "</plugin-version>" + \
				"<plugin-developer>" + props.developer + "</plugin-developer>" + \
				"<plugin-url>" + props.url + "</plugin-url>" + \
				"<plugin-license>" + props.license + "</plugin-license>" + \
				"<plugin-license-url>" + props.license + "</plugin-license-url>" + \
				"<plugin-cost>" + props.cost + "</plugin-cost>" + \
				"<plugin-type>" + props.type + "</plugin-type>" + \
				"<plugin-compatibility>" + props.compatibility + "</plugin-compatibility>" + \
				"<plugin-dependencies>" + props.dependencies + "</plugin-dependencies>" + \
			"</component>"

			try {
				def registryDecriptorStream = new ByteArrayInputStream(registryDecriptor.getBytes("UTF-8"))
				ContentServices.writeContentAsset(context, siteId, registryPluginDescriptorPath, (props.id+".xml"), registryDecriptorStream,  "false", "", "", "", "false", "true", null)
			}
			catch(err) {
				System.out.println("error writing plugin registry item  "+ registryPluginDescriptorPath +" :" + err)
			}
	}


	/** ============= **/
	// Helper methods
	/** ============= **/	

	/** 
	 * Read a manifest file for a plugin
     * Available properties (MAP KEY  
	 *		id  
	 *		name 
	 *		version 
	 *		developer 
	 *		version  
	 *		url  
	 *		license  
	 *		licenseUrl 
	 *		cost 
	 *		type  
	 *		compatibility  
	 *		dependencies 
	 * @return a map of manifest properties
 	 */
	def readManifest(path) {
		def props = [:]

		JarInputStream jarStream = new JarInputStream(new FileInputStream(path))
		Manifest mf = jarStream.getManifest()

		if(mf != null) {
			Attributes attrs = mf.getMainAttributes()

			props.id = attrs.getValue("plugin-id")
			props.name = attrs.getValue("plugin-name")
			props.version = attrs.getValue("plugin-version")
			props.developer = attrs.getValue("plugin-developer")
			props.version = attrs.getValue("plugin-version")
			props.url = attrs.getValue("plugin-url")
			props.license = attrs.getValue("plugin-license")
			props.licenseUrl = attrs.getValue("plugin-license-url")
			props.cost = attrs.getValue("plugin-cost")
			props.type = attrs.getValue("plugin-type")
			props.compatibility = attrs.getValue("plugin-compatibility")
			props.dependencies = attrs.getValue("plugin-dependencies")

			if(props.id) { props.id = props.id.toLowerCase() }
			if(props.type) { props.type = props.type.toLowerCase() }

			if(props.dependencies) {
				props.dependencies = props.dependencies.toLowerCase()
				props.dependencies = props.dependencies.split(",")
			}

		}
		else {
			throw new Exception("Unable to read manifest from file: ${path}")
		}

		if(props == null || props.id  == null || props.type == null) {
			throw new Exception("Key manifest (${path}) properties id ${props.id} and type ${props.type} missing")
		}

		return props
	}

	/**
	 * dump manfiest propeties to the log
	 */
	def logManifestDetails(props) {
		System.out.println("Name:" + props.name)
		System.out.println("Version:" + props.version)
		System.out.println("Developer:" + props.developer)
		System.out.println("Version:" + props.version)
		System.out.println("Type:" + props.type)
	}

	/**
	 * download a plugin from remote URL
	 */
	def download(url, filename) {

		def file = new File(filename).newOutputStream()
		file << new URL(url).openStream()
		file.close()
	}

	/**
	 * unzip a plugin
	 */
	def unzip(String unzipPath, String zipFile) {

		List<String> fileList
		String OUTPUT_FOLDER = unzipPath
		byte[] buffer = new byte[1024]

		try{

			//create output directory is not exists
			def outputFolder = OUTPUT_FOLDER
			File folder = new File(OUTPUT_FOLDER)
			if(!folder.exists()){
				folder.mkdir()
			}

			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))

			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry()

			while(ze!=null){

				String fileName = ze.getName()
				def isDirectory = ze.isDirectory()
				if(!fileName.contains(".DS_Store") && !fileName.contains("__MACOSX") ) {
					File newFile = new File(outputFolder + File.separator + fileName)

					if(isDirectory) {
						newFile.mkdir()
					}
					else {
						new File(newFile.getParent()).mkdirs()

						try {
							FileOutputStream fos = new FileOutputStream(newFile)

							int len
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len)
							}

							fos.close()
						}
						catch(err) {
							System.out.println("ERR: "+ err)
						}
					}
				}

				ze = zis.getNextEntry()
			}

			zis.closeEntry()
			zis.close()

		}
		catch(IOException ex){
			ex.printStackTrace()
		}
	}

	/**
	 * import a plugin in to the system
	 * This is a top level method that looks at the type of the plugin and calls the appropriate install method
	 */
	def importPlugin(unzipPath, props, installToSite) {
		def state = [:]
		state.status = false

		if(props.type) {
			if(props.type == "site-component") {
				state = importSitePlugin(unzipPath, props, installToSite)
			}
			else if(props.type == "studio") {
				state = importStudioPlugin(unzipPath, props, installToSite)
			}
			else {
				throw new Exception("unknown plugin type ${props.type}")
			}
		}
		else {
			throw new Exception("missing manifest properties")
		}

		return state
	}

	/**
	 * install a SITE plugin
	 */
	def importSitePlugin(unzipPath, props, installToSite) {

		def state = [:]
		state.status = true

		def applicationContext = context.applicationContext
		def request = context.request

		def dir = new File(unzipPath)
		
		dir.eachFileRecurse (FileType.FILES) { file ->

			def absolutePath = file.getAbsolutePath()
			def relativePath = absolutePath.substring(absolutePath.indexOf(unzipPath)+unzipPath.length())

			System.out.println("PROCESSING :" + relativePath)

			if(relativePath.startsWith("/templates")
					|| relativePath.startsWith("/scripts")
					|| relativePath.startsWith("/static-assets")
          			        || relativePath.startsWith("/config")
					|| relativePath.startsWith("/site")) {

				try {
					def writePath = relativePath
					def writePathOnly = writePath.substring(0, writePath.lastIndexOf("/")+1)
					def writeFileName = cleanPath(writePath.substring(writePath.lastIndexOf("/")+1))

					def content = new FileInputStream(file)

					def context = ContentServices.createContext(applicationContext, request)
					ContentServices.writeContentAsset(context, installToSite, cleanPath(writePathOnly), writeFileName, content, "false", "", "", "", "false", "true", null)

				}
				catch(err) {
					System.out.println("error writing asset to site: ${relativePath} :" + err)
				}
			}
			else if(relativePath.startsWith("/content-types")) {

				def writePath = relativePath
				def writePathOnly = "/cstudio/config/sites/"+installToSite+"/"+writePath.substring(0, writePath.lastIndexOf("/")+1)
				def writeFileName = writePath.substring(writePath.lastIndexOf("/")+1)

				try {
					def content = new FileInputStream(file)

					def context = SiteServices.createContext(applicationContext, request)
					SiteServices.writeConfiguration(context, cleanPath(joinPaths(writePathOnly, writeFileName)), content)

				}
				catch(err) {
					System.out.println("error writing config to site: ${relativePath} :" + err)
				}
			}
			else if(relativePath.startsWith("/preview-tools/components-config.xml")) {

				def writePath = relativePath
				def writePathOnly = "/cstudio/config/sites/"+installToSite+"/"+writePath.substring(0, writePath.lastIndexOf("/")+1)
				def writeFileName = writePath.substring(writePath.lastIndexOf("/")+1)
				def repoPath = cleanPath(joinPaths(writePathOnly, writeFileName))

				try {
					def content = new FileInputStream(file)

					def contextA = ContentServices.createContext(applicationContext, request)
					def repoContent = ContentServices.getContentAtPath(contextA, repoPath)

					String repoContentStr =  IOUtils.toString(repoContent, "UTF-8")
					String componentConfigStr = IOUtils.toString(content, "UTF-8")

					def fromxml = new XmlSlurper().parseText(componentConfigStr)
					def toxml = new XmlSlurper().parseText(repoContentStr)

					/* iterate over inbound items and add/merge them */
					fromxml.children().each { child ->
						def xpathResult = toxml.category.find { it.label == child.label }

						if(xpathResult != null) {
							// merge in to existing category
							child.label[0].replaceNode { }
							xpathResult << child.children()

							// REALLY NEEDS LOGIC HERE THAT ITERATES COMPONENTS AND DOES SAME THING
						}
						else {
							// add a new category
							toxml[0].children() << child
						}
					}

					java.io.StringWriter o = new java.io.StringWriter()
					println XmlUtil.serialize( toxml, o )
					String mergedXML = o.toString()


					def mergedXMLStream = new ByteArrayInputStream(mergedXML.getBytes("UTF-8"))

					def contextB = SiteServices.createContext(applicationContext, request)
					// FOR DEBUGGING println groovy.xml.XmlUtil.serialize( toxml )
					SiteServices.writeConfiguration(contextB, repoPath, mergedXMLStream)

				}
				catch(err) {
					System.out.println("error writing config to site: ${relativePath} :" + err)
				}
			}
		}

		if(state.status == true) {
			registerPlugin(installToSite, props)
		}

		return state
	}

	/**
	 * Install a STUDIO plugin (MODIFY THE EXPLODED WAR FILE)
	 */
	def importStudioPlugin(unzipPath, props, installToSite) {

		def state = [:]
		state.status = false

		def applicationContext = context.applicationContext
		def request = context.request
		def servletContext = request.getSession().getServletContext()
		def studioInstallBasePath = servletContext.getRealPath(File.separator)

		def dir = new File(unzipPath)
		dir.eachFileRecurse (FileType.FILES) { file ->

			def absolutePath = file.getAbsolutePath()
			def relativePath = absolutePath.substring(absolutePath.indexOf(unzipPath)+unzipPath.length())

			System.out.println("PROCESSING :" + relativePath)

			if(relativePath.startsWith("/templates")
					|| relativePath.startsWith("/scripts")
					|| relativePath.startsWith("/static-assets")) {

				def destPath = cleanPath(studioInstallBasePath + "default-site/" + relativePath)

				try {
					File destFile = new File(destPath)
					FileUtils.copyFile(file, destFile)
				}
				catch(err) {
					System.out.println("error writing file to studio: ${destPath} :" + err)
				}
			}
		}

		if(state.status == true) {
			registerPlugin(installToSite, props)
		}

		return state
	}

	/**
	 * path helper, join two file paths
	 */
	def joinPaths(pathA, pathB) {
		def joinedPath = (pathA + pathB).replace("//", "/")

		return joinedPath
	}

	/**
	 * path helper, clean up bad paths
	 */
	def cleanPath(path) {
		def cleanPath = path.replaceAll("//", "/")
		if(cleanPath.endsWith("/")) {
			cleanPath = cleanPath.substring(0, cleanPath.length()-1)
		}

		return cleanPath
	}
}	
