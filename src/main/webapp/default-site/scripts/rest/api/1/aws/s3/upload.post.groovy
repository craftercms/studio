import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils

def s3Service = applicationContext["studioS3Service"]

response.setHeader("Content-Type", "text/html")

def sendError = { code, msg ->
    response.status = code
    def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("{\"hasError\":true,\"errors\":[\"${msg}\"]}")
        writer.flush()
}

if (ServletFileUpload.isMultipartContent(request)) {
    def upload = new ServletFileUpload()
    def iterator = upload.getItemIterator(request)
    def site = null
    def profileId = null
    while(iterator.hasNext()) {
        def item = iterator.next()
        def name = item.getFieldName()
        def stream = item.openStream()
        if(item.isFormField()) {
            switch(name) {
                case "site":
                    site = Streams.asString(stream)
                    break
                case "profile":
                    profileId = Streams.asString(stream)
                    break
            }
        } else {
            def filename = item.getName()
            if (filename != null) {
                filename = FilenameUtils.getName(filename)
            }
            try {
                def output = s3Service.uploadFile(site, profileId, filename, stream)

                def writer = response.writer
                writer.println("<script>document.domain = \"${request.serverName}\";</script>")
                writer.println("[{\"bucket\":\"${output.bucket}\",\"key\":\"${output.key}\"}]")
                writer.flush()
            } catch (e) {
                logger.error("Upload of file ${filename} failed", e)

                sendError(500, "Upload of file failed: ${e.message}")
            }
        }
    }
} else {
    sendError(400, "Request is not of type multi-part")
}