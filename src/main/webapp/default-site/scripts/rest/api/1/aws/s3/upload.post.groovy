import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartRequest

def s3Service = applicationContext["studioS3Service"]

response.setHeader("Content-Type", "text/html")

def sendError = { msg ->

    def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("{\"hasError\":true,\"errors\":[\"${msg}\"]}")
        writer.flush()
}

if (request instanceof MultipartRequest) {
    def site = params.site
    def profileId = params.profile
    def uploadedFile = request.getFile("file")
    filename = uploadedFile.getOriginalFilename()

    def filenameNoExt = FilenameUtils.removeExtension(filename)
    def ext = FilenameUtils.getExtension(filename)
    def tmpFile = File.createTempFile(filenameNoExt, "." + ext)
    uploadedFile.transferTo(tmpFile)

    def output
    try {
        output = s3Service.uploadFile(site, profileId, filename, tmpFile)
    } catch (e) {
        logger.error("Upload of file ${tmpFile} failed", e)

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