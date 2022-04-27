/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

/**
 * Transformer that calls ImageMagick from the command line with the options specified in the parameters.
 *
 * @author avasquez
 * @see <a href="https://www.imagemagick.org/script/index.php">ImageMagick</a>
 */
public class ImageMagickTransformer implements ImageTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ImageMagickTransformer.class);

    private static final String DEFAULT_IMG_MGK_PATH = "convert";
    private static final int DEFAULT_PROCESS_TIMEOUT_SECS = 30;

    private static final String PARAM_OPTIONS = "options";

    private String imgMgkPath;
    private int processTimeoutSecs;

    public ImageMagickTransformer() {
        imgMgkPath = DEFAULT_IMG_MGK_PATH;
        processTimeoutSecs = DEFAULT_PROCESS_TIMEOUT_SECS;
    }

    public void setImgMgkPath(String imgMgkPath) {
        this.imgMgkPath = imgMgkPath;
    }

    public void setProcessTimeoutSecs(int processTimeoutSecs) {
        this.processTimeoutSecs = processTimeoutSecs;
    }

    public void transform(Path sourcePath, Path targetPath, Map<String, String> parameters) throws ImageTransformationException {
        String cmdLine = createCmdLine(sourcePath, targetPath, parameters);

        try {
            logger.info("Executing command: {}", cmdLine);

            Process proc = Runtime.getRuntime().exec(cmdLine);
            // TODO: This might hang in certain OS. It's better to first read the stout and stderr before calling waitFor()
            proc.waitFor(processTimeoutSecs, TimeUnit.SECONDS);

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
