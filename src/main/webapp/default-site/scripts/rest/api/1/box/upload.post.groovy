import org.springframework.web.multipart.MultipartRequest

def boxService = applicationContext["studioBoxService"]

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
    def filename = uploadedFile.getOriginalFilename()
    try {
        def output = boxService.uploadFile(site, profileId, filename, uploadedFile.size, uploadedFile.inputStream)

        def writer = response.writer
        writer.println("<script>document.domain = \"${request.serverName}\";</script>")
        writer.println("[{\"name\":\"${output.name}\",\"id\":\"${output.id}\"}]")
        writer.flush()
    } catch (e) {
        logger.error("Upload of file ${filename} failed", e)

        sendError("Upload of file failed: ${e.message}")
    }
} else {
    sendError("Request is not of type multi-part")
}