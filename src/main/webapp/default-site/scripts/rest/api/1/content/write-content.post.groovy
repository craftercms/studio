import scripts.api.ContentServices
import org.apache.commons.io.IOUtils
import java.util.List
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory

def result = [:]
def site = ""
def path = ""
def oldPath = ""
def fileName = ""
def contentType = ""
def draft = false
def createFolders = "true"
def edit = "false"
def unlock = "true"
def content = null

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
            }
        } 
        else {
            fileName = item.getName()
            contentType = item.getContentType()
            content = item.getInputStream()
        }
    }
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
}

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
    result.result = ContentServices.writeContent(context, site, path, fileName, contentType, content, "true", edit, unlock);
}
return result