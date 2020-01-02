package org.craftercms.studio.impl.v1.asset.processing;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * {@link org.craftercms.studio.api.v1.asset.processing.AssetProcessor} that converts a file from an input
 * encoding to an output encoding.
 *
 * @author avasquez
 * @since 3.1.5
 */
public class FileEncodingConvertingAssetProcessor extends AbstractAssetProcessor {

    private static final String PARAM_INPUT_ENCODING = "inputEncoding";
    private static final String PARAM_OUTPUT_ENCODING = "outputEncoding";

    @Override
    protected void doProcessAsset(Path inputFile, Path outputFile,
                                  Map<String, String> params) throws AssetProcessingException {
        Charset inputCharset = getCharset(params, PARAM_INPUT_ENCODING);
        Charset outputCharset = getCharset(params, PARAM_OUTPUT_ENCODING);

        Reader input = null;
        Writer output = null;

        try {
            input = Files.newBufferedReader(inputFile, inputCharset);
            output = Files.newBufferedWriter(outputFile, outputCharset);

            IOUtils.copy(input, output);
        } catch (IOException e) {
            throw new AssetProcessingException("Unable to convert file from '" + inputCharset + "' to '" +
                                               outputCharset + "'", e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    private Charset getCharset(Map<String, String> params, String paramName) throws AssetProcessingException {
        String encoding = params.get(paramName);
        if (StringUtils.isNotEmpty(encoding)) {
            try {
                return Charset.forName(encoding);
            } catch (UnsupportedCharsetException e) {
                throw new AssetProcessingException("Parameter '" + paramName + "' is not a supported encoding");
            }
        } else {
            throw new AssetProcessingException("Parameter '" + paramName + "' not specified");
        }
    }

}
