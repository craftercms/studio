import scripts.api.ContentServices
import org.springframework.web.multipart.MultipartRequest

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

if(request instanceof MultipartRequest) {
    site = params.site
    path = params.path
    isImage = params.isImage
    allowedWidth = params.allowedWidth
    allowedHeight = params.allowedHeight
    allowLessSize = params.allowLessSize
    changeCase = params.changeCase
    def file = request.getFile("file")
    fileName = file.getOriginalFilename()
    contentType = file.getContentType()
    content = file.getInputStream()

    result = ContentServices.writeContentAsset(context, site, path, fileName, content,
            isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)
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