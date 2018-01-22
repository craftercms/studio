import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartRequest

def elasticTranscoderService = applicationContext["studioElasticTranscoderService"]

response.setHeader("Content-Type", "text/html")

def sendError = { code, msg ->
    response.status = code
    def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("{\"hasError\":true,\"errors\":[\"${msg}\"]}")
        writer.flush()
}

if (request instanceof MultipartRequest) {
    def site = params.site
    def profileId = params.profile
    def uploadedFile = request.getFile("file")
    def filename = uploadedFile.getOriginalFilename()
    try {
        def filenameNoExt = FilenameUtils.removeExtension(filename)
        def ext = FilenameUtils.getExtension(filename)
        def tmpFile = File.createTempFile(filenameNoExt, "." + ext)
        uploadedFile.transferTo(tmpFile)

        def job = elasticTranscoderService.transcodeFile(site, profileId, filename, tmpFile)
        def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("[{\"job_id\":\"${job.id}\",\"output_bucket\":\"${job.outputBucket}\",\"base_key\":\"${job.baseKey}\"}]")
        writer.flush()
    } catch (e) {
        logger.error("Transcoding of file ${filename} failed", e)

        sendError(500, "Transcoding of file failed: ${e.message}")
    }

} else {
    sendError(400, "Request is not of type multi-part")
}