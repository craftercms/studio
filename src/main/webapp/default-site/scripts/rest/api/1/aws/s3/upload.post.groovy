import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.io.FilenameUtils

def site = null
def profileId = null
def filename = null
def file = null
def s3Service = applicationContext["studioS3Service"]

response.setHeader("Content-Type", "text/html")

def sendError = { msg ->

    def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("{\"hasError\":true,\"errors\":[\"${msg}\"]}")
        writer.flush()
}

if (ServletFileUpload.isMultipartContent(request)) {
    try {
        def factory = new DiskFileItemFactory()
        def upload = new ServletFileUpload(factory)
        def items = upload.parseRequest(request)

        items.each { item ->
            if (item.formField) {
                switch (item.fieldName) {
                    case "site":
                        site = item.string
                        break
                    case "profile":
                        profileId = item.string
                        break
                }
            } else {
                filename = item.name

                def filenameNoExt = FilenameUtils.removeExtension(filename)
                def ext = FilenameUtils.getExtension(filename)

                file = File.createTempFile(filenameNoExt, "." + ext)

                item.write(file)
            }
        }
    } catch (e) {
        logger.error("Parsing of multi-part request failed", e)

        sendError("Parsing of multi-part request failed")

        return
    }

    def output
    try {
        output = s3Service.uploadFile(site, profileId, filename, file)
    } catch (e) {
        logger.error("Upload of file ${file} failed", e)

        sendError("Upload of file failed")

        return
    }

    def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("[{\"bucket\":\"${output.bucket}\",\"key\":\"${output.key}\"}]")
        writer.flush()
} else {
    sendError("Request is not of type multi-part")
}