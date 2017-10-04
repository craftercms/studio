package org.craftercms.studio.api.v1.aws.elastictranscoder;

import java.io.InputStream;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.craftercms.studio.api.v1.exception.TranscoderConfigurationException;

/**
 * Implementations of this interface should be able to parse and generate a {@link TranscoderProfile} from the given input stream.
 *
 * @author avasquez
 */
public interface TranscoderProfileReader {

    /**
     * Reads the specified input stream and parses the content to generate a {@link TranscoderProfile}
     *
     * @param input the input stream
     *
     * @return the {@link TranscoderProfile}
     *
     * @throws TranscoderConfigurationException if the input stream couldn't be read/parsed correctly or a required property is missing
     */
    TranscoderProfile readProfile(InputStream input) throws TranscoderConfigurationException;

}
