package org.craftercms.studio.impl.v1.image.transformation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ImageTransformationException;
import org.craftercms.studio.api.v1.image.transformation.ImageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageMagickTransformer implements ImageTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ImageMagickTransformer.class);

    private static final String DEFAULT_IMG_MGK_PATH = "convert";
    private static final int DEFAULT_PROCESS_TIME_TO_WAIT_SECS = 15;

    private static final String PARAM_OPTIONS = "options";

    private String imgMgkPath;
    private int processTimeToWaitSecs;

    public ImageMagickTransformer() {
        imgMgkPath = DEFAULT_IMG_MGK_PATH;
        processTimeToWaitSecs = DEFAULT_PROCESS_TIME_TO_WAIT_SECS;
    }

    public void setImgMgkPath(String imgMgkPath) {
        this.imgMgkPath = imgMgkPath;
    }

    public void setProcessTimeToWaitSecs(int processTimeToWaitSecs) {
        this.processTimeToWaitSecs = processTimeToWaitSecs;
    }

    public void transform(Path sourcePath, Path targetPath, Map<String, String> parameters) throws ImageTransformationException {
        String cmdLine = createCmdLine(sourcePath, targetPath, parameters);

        try {
            logger.info("Executing command: {}", cmdLine);

            Process proc = Runtime.getRuntime().exec(cmdLine);
            proc.waitFor(processTimeToWaitSecs, TimeUnit.SECONDS);

            String stdOut = getProcessStdOut(proc);
            String stdErr = getProcessStdErr(proc);

            if (StringUtils.isNotEmpty(stdOut)) {
                logger.info("Img Mgk stdout for [{}]: {}", cmdLine, stdOut);
            }
            if (StringUtils.isNotEmpty(stdErr)) {
                logger.info("Img Mgk stderr for [{}]: {}", cmdLine, stdErr);
            }
        } catch (Exception e) {
            throw new ImageTransformationException("Error while running Image Magick process from the command line", e);
        }
    }


    private String createCmdLine(Path sourcePath, Path targetPath, Map<String, String> parameters) {
        StringBuilder cmdLine = new StringBuilder(imgMgkPath);

        cmdLine.append(" ").append(sourcePath.toAbsolutePath().toString());

        String options = MapUtils.getString(parameters, PARAM_OPTIONS);
        if (StringUtils.isNotEmpty(options)) {
            cmdLine.append(" ").append(options);
        }

        cmdLine.append(" ").append(targetPath.toAbsolutePath().toString());

        return cmdLine.toString();
    }

    private String getProcessStdOut(Process proc) throws IOException {
        return IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
    }

    private String getProcessStdErr(Process proc) throws IOException {
        return IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
    }

}
