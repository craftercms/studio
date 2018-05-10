import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils
import scripts.api.ContentServices

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
            contentType = item.getContentType()
            result = ContentServices.writeContentAsset(context, site, path, fileName, stream,
                    isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
        }
    }
} else {
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
return result;
