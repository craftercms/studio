/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils

def elasticTranscoderService = applicationContext["studioElasticTranscoderService"]

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
                case "site_id":
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
                def job = elasticTranscoderService.transcodeFile(site, profileId, filename, stream)
                def writer = response.writer
                writer.println("<script>document.domain = \"${request.serverName}\";</script>")
                writer.println("[{\"job_id\":\"${job.id}\",\"output_bucket\":\"${job.outputBucket}\",\"base_key\":\"${job.baseKey}\"}]")
                writer.flush()
            } catch (e) {
                logger.error("Transcoding of file ${filename} failed", e)

                sendError(500, "Transcoding of file failed: ${e.message}")
            }
        }
    }
} else {
    sendError(400, "Request is not of type multi-part")
}