package org.craftercms.studio.api.v1.asset.processing;

import java.util.Map;

public class ProcessorConfiguration {

    private String type;
    private Map<String, String> params;
    private String outputPathFormat;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getOutputPathFormat() {
        return outputPathFormat;
    }

    public void setOutputPathFormat(String outputPathFormat) {
        this.outputPathFormat = outputPathFormat;
    }

}
