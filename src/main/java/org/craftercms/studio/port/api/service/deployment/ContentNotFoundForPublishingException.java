package org.craftercms.cstudio.api.service.deployment;

public class ContentNotFoundForPublishingException extends Exception {

    public ContentNotFoundForPublishingException(String site, String target, String path) {
        super();
        this._site = site;
        this._target = target;
        this._path = path;
    }

    public String getSite() { return _site; }
    public String getTarget() { return _target; }
    public String getPath() { return _path; }

    protected String _site;
    protected String _target;
    protected String _path;
}
