import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.io.FilenameUtils

def site = null
def profilePath = null
def filename = null
def file = null
def elasticTranscoderService = applicationContext["studioElasticTranscoderService"]

if (ServletFileUpload.isMultipartContent(request)) {
    def factory = new DiskFileItemFactory()
    def upload = new ServletFileUpload(factory)
    def items = upload.parseRequest(request)

    items.each { item ->
        if (item.formField) {
            switch (item.fieldName) {
                case "site":
                    site = item.string
                    break
                case "profile_path":
                    profilePath = item.string
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

    def job = elasticTranscoderService.transcodeFile(site, profilePath, filename, file)

    response.setHeader("Content-Type", "text/html")

    def writer = response.writer
    writer.println("<script>document.domain = \"${request.serverName}\";</script>")
    writer.println("[{\"job_id\":\"${job.id}\",\"output_bucket\":\"${job.outputBucket}\",\"base_key\":\"${job.baseKey}\"}]")
    writer.flush()
}