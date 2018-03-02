package org.craftercms.studio.api.v1.asset.processing;

import java.util.List;

public class ProcessorPipelineConfiguration {

    private String inputPathPattern;
    private boolean keepOriginal;
    private List<ProcessorConfiguration> processorsConfig;

    public String getInputPathPattern() {
        return inputPathPattern;
    }

    public void setInputPathPattern(String inputPathPattern) {
        this.inputPathPattern = inputPathPattern;
    }

    public boolean isKeepOriginal() {
        return keepOriginal;
    }

    public void setKeepOriginal(boolean keepOriginal) {
        this.keepOriginal = keepOriginal;
    }

    public List<ProcessorConfiguration> getProcessorsConfig() {
        return processorsConfig;
    }

    public void setProcessorsConfig(List<ProcessorConfiguration> processorsConfig) {
        this.processorsConfig = processorsConfig;
    }
}
