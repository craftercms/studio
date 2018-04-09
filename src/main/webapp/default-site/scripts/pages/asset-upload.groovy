import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils
import scripts.api.ContentServices

model.cookieDomain = request.getServerName()

def result = [:]
def site = ""
def path = ""
def fileName = ""
def draft = "false"
def unlock = "true"
def content = null

def isImage = "false"
def allowedWidth = ""
def allowedHeight = ""
def allowLessSize = ""
def systemAsset = null

def context = ContentServices.createContext(applicationContext, request)

if(ServletFileUpload.isMultipartContent(request)) {
    def upload = new ServletFileUpload()
    def iterator = upload.getItemIterator(request)
    while(iterator.hasNext()) {
        def item = iterator.next()
        def name = item.getFieldName()
        def stream = item.openStream()
        if (item.isFormField()) {
            switch (name) {
                case "site":
                    site = Streams.asString(stream)
                    break
                case "path":
                    path = Streams.asString(stream)
                    break
                case "isImage":
                    isImage = Streams.asString(stream)
                    break
                case "allowedWidth":
                    allowedWidth = Streams.asString(stream)
                    break
                case "allowedHeight":
                    allowedHeight = Streams.asString(stream)
                    break
                case "allowLessSize":
                    allowLessSize = Streams.asString(stream)
                    break
                case "changeCase":
                    changeCase = Streams.asString(stream)
                    break
            }
        } else {
            fileName = item.getName()
            if (fileName != null) {
                fileName = FilenameUtils.getName(fileName)
            }
            result = ContentServices.writeContentAsset(context, site, path, fileName, stream,
                    isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)
        }
    }
} else {
    site = request.getParameter("site")
    path = request.getParameter("path")
    oldPath = request.getParameter("oldContentPath")
    fileName = (request.getParameter("fileName")) ? request.getParameter("fileName") : request.getParameter("filename")
    contentType = request.getParameter("contentType")
    createFolders = request.getParameter("createFolders")
    edit = request.getParameter("edit")
    draft = request.getParameter("draft")
    unlock = request.getParameter("unlock")
    content = request.getInputStream()

    if (!site || site == '') {
        result.code = 400
        result.message = "Site must be provided." + site
        return result
    }
    else if (!path || path == '') {
        result.code = 400
        result.message = "Path must be provided."
        return result
    }
    else if (!fileName || fileName == '') {
        result.code = 400
        result.message = "fileName must be provided."
        return result
    }

    if (oldPath != null && oldPath != "" && (draft==null || draft!=true)) {
        fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length())
        result.result = ContentServices.writeContentAndRename(context, site, oldPath, path, fileName, contentType,
                 content, "true", edit, unlock, true)

    } else {
        if(path.startsWith("/site")){
            result.result = ContentServices.writeContent(context, site, path, fileName, contentType, content,
                     "true", edit, unlock)
        }
        else {
            result.result = ContentServices.writeContentAsset(context, site, path, fileName, content,
                isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)
        }
    }
}

//model.fileName = fileName
model.fileName = result.message.name

def dotPos = fileName.indexOf(".")
model.fileExtension = (dotPos != -1) ? fileName.substring(dotPos+1) : ""

model.size = result.message.size
model.sizeUnit = result.message.sizeUnit