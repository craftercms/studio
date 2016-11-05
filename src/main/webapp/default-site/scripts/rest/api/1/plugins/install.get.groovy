
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

def downloadUrl = params.pluginUrl
def installToSite = params.site

System.out.println("Installing PlugIn ${downloadUrl} to site ${installToSite}")

def filename = downloadUrl.substring(downloadUrl.lastIndexOf("/")+1)

download(downloadUrl, filename)

props = readManifest(filename)
logManifestDetails(props)

if(props.id == null) {
	props.id = filename.substring(0,filename.lastIndexOf("."))
}

def unzipPath = "./plugin-install/"+props.id

unzip(unzipPath, filename)

importPlugin(unzipPath, props, installToSite, applicationContext, request)

return true



def download(url, filename) {

	def file = new File(filename).newOutputStream()
	file << new URL(url).openStream()
	file.close()
}

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

def logManifestDetails(props) {
	System.out.println("Name:" + props.name)
	System.out.println("Version:" + props.version)
	System.out.println("Developer:" + props.developer)
	System.out.println("Version:" + props.version)
	System.out.println("Type:" + props.type)
}

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

def importPlugin(unzipPath, props, installToSite, applicationContext, request) {
	def state = [:]
	state.status = false

	if(props.type) {
		if(props.type == "site-component") {
			state = importSitePlugin(unzipPath, props, installToSite, applicationContext, request)
		}
		else if(props.type == "studio") {
			state = importStudioPlugin(unzipPath, props, installToSite, applicationContext, request)
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

def importSitePlugin(unzipPath, props, installToSite, applicationContext, request) {

	def state = [:]
	state.status = false

	def dir = new File(unzipPath)
	dir.eachFileRecurse (FileType.FILES) { file ->

		def absolutePath = file.getAbsolutePath()
		def relativePath = absolutePath.substring(absolutePath.indexOf(unzipPath)+unzipPath.length())

		System.out.println("PROCESSING :" + relativePath)

		if(relativePath.startsWith("/templates")
				|| relativePath.startsWith("/scripts")
				|| relativePath.startsWith("/static-assets")) {

			try {
				def writePath = relativePath //.replace("/templates/web", "/templates/web/p/"+props.id)
				def writePathOnly = writePath.substring(0, writePath.lastIndexOf("/")+1)
				def writeFileName = writePath.substring(writePath.lastIndexOf("/")+1)

				def content = new FileInputStream(file)

				def context = ContentServices.createContext(applicationContext, request)
				ContentServices.writeContentAsset(context, installToSite, writePathOnly, writeFileName, content, "false", "", "", "", "false", "true", null)
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
				SiteServices.writeConfiguration(context, writePathOnly+"/"+writeFileName, content)
			}
			catch(err) {
				System.out.println("error writing config to site: ${relativePath} :" + err)
			}
		}
		else if(relativePath.startsWith("/preview-tools/components-config.xml")) {

			def writePath = relativePath
			def writePathOnly = "/cstudio/config/sites/"+installToSite+"/"+writePath.substring(0, writePath.lastIndexOf("/")+1)
			def writeFileName = writePath.substring(writePath.lastIndexOf("/")+1)

			try {
				def content = new FileInputStream(file)

				def contextA = ContentServices.createContext(applicationContext, request)
				def repoContent = ContentServices.getContentAtPath(contextA, writePathOnly+"/"+writeFileName)

				String repoContentStr =  IOUtils.toString(repoContent, "UTF-8")
				String componentConfigStr = IOUtils.toString(content, "UTF-8")
				def fromxml = new XmlSlurper().parseText(componentConfigStr)
				def toxml = new XmlSlurper().parseText(repoContentStr)

				toxml[0].children() << fromxml.children()

				java.io.StringWriter o = new java.io.StringWriter()
				XmlUtil xmlUtil = new XmlUtil()
				xmlUtil.serialize(toxml, o)​
				String mergedXML = o.toString()
				def mergedXMLStream = new ByteArrayInputStream(mergedXML.getBytes("UTF-8"))

				def contextB = SiteServices.createContext(applicationContext, request)
				SiteServices.writeConfiguration(contextB, writePathOnly+"/"+writeFileName, mergedXMLStream)
			}
			catch(err) {
				System.out.println("error writing config to site: ${relativePath} :" + err)
			}
		}
	}

	return state
}

def importStudioPlugin(unzipPath, props, installToSite, applicationContext, request) {

	def state = [:]
	state.status = false

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

			def destPath = studioInstallBasePath + "default-site/" + relativePath

			try {
				File destFile = new File(destPath)
				FileUtils.copyFile(file, destFile)
			}
			catch(err) {
				System.out.println("error writing file to studio: ${destPath} :" + err)
			}
		}
	}

	return state
}