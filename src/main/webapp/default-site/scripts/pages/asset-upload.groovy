import scripts.api.ContentServices
import org.apache.commons.io.IOUtils
import java.util.List
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory

model.cookieDomain = request.getServerName()

def result = [:]
def site = ""
def path = ""
def oldPath = ""
def fileName = ""
def contentType = ""
def draft = "false"
def createFolders = "true"
def edit = "false"
def unlock = "true"
def content = null

def isImage = "false";
def allowedWidth = "";
def allowedHeight = "";
def allowLessSize = "";
def changeCase = "";
def systemAsset = null;

def context = ContentServices.createContext(applicationContext, request)

if(ServletFileUpload.isMultipartContent(request)) {
    DiskFileItemFactory factory = new DiskFileItemFactory()
    //factory.setSizeThreshold(yourMaxMemorySize)
    //factory.setRepository(yourTempDirectory)
    ServletFileUpload upload = new ServletFileUpload(factory)
    //upload.setSizeMax(yourMaxRequestSize);
    List<FileItem> items = upload.parseRequest(request)

    Iterator<FileItem> iter = items.iterator()

    while (iter.hasNext()) {
        FileItem item = iter.next()

        if (item.isFormField()) {
            if(item.getFieldName()=="site") {
                site = item.getString()
            }
            else if(item.getFieldName()=="path") {
                path = item.getString()
            } else if (item.getFieldName() == "fileName") {
                fileName = item.getString();
            } else if (item.getFieldName() == "isImage") {
                isImage = item.getString();
            } else if (item.getFieldName() == "allowedWith") {
                allowedWidth = item.getString();
            } else if (item.getFieldName() == "allowedHeight") {
                allowedHeight = item.getString();
            } else if (item.getFieldName() == "allowLessSize") {
                allowLessSize = item.getString();
            } else if (item.getFieldName() == "changeCase") {
                changeCase = item.getString();
            }
        } 
        else {
            fileName = item.getName()
            fileName = fileName.toLowerCase()
            contentType = item.getContentType()
            content = item.getInputStream()
        }
    }

    def dotIdx = fileName.lastIndexOf(".")
    def cleanFileName = fileName.substring(0, dotIdx)
    def cleanFileExt = fileName.substring(dotIdx + 1)

    cleanFileName = cleanFileName.replaceAll("[\\s]", "-")
    cleanFileName = cleanFileName.replaceAll("[.]", "-")
    cleanFileName = cleanFileName.replaceAll("[^\\w\\-]","")

    cleanFileExt = cleanFileExt.replaceAll("[\\s]", "-")
    cleanFileExt = cleanFileExt.replaceAll("[^\\w\\-]","")

    fileName = cleanFileName + "." + cleanFileExt

    result = ContentServices.writeContentAsset(context, site, path, fileName, content,
            isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
}
else {
    site = params.site;
    path = params.path;
    oldPath = params.oldContentPath;
    fileName = (params.fileName) ? params.fileName : params.filename;
    contentType = params.contentType;
    createFolders = params.createFolders;
    edit = params.edit;
    draft = params.draft;
    unlock = params.unlock;
    content = request.getInputStream()

    if (!site || site == '') {
        result.code = 400;
        result.message = "Site must be provided."+site
        return result
    }
    else if (!path || path == '') {
        result.code = 400
        result.message = "Path must be provided."
        return result
    }
    else if (!fileName || fileName == '') {
        result.code = 400;
        result.message = "fileName must be provided."
        return result
    }

    if (oldPath != null && oldPath != "" && (draft==null || draft!=true)) {
        fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length());
        result.result = ContentServices.writeContentAndRename(context, site, oldPath, path, fileName, contentType, content, "true", edit, unlock, true);

    } else {
        if(path.startsWith("/site")){
            result.result = ContentServices.writeContent(context, site, path, fileName, contentType, content, "true", edit, unlock);
        }
        else {
            result.result = ContentServices.writeContentAsset(context, site, path, fileName, content,
                isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
        }
    }
}

//model.fileName = fileName
model.fileName = result.message.name

def dotPos = fileName.indexOf(".")
model.fileExtension = (dotPos != -1) ? fileName.substring(dotPos+1) : ""

model.size = result.message.size
model.sizeUnit = result.message.sizeUnit
