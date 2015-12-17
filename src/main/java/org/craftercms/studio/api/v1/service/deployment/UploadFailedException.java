package org.craftercms.studio.api.v1.service.deployment;

public class UploadFailedException extends Exception {

    private static final long serialVersionUID = 7407643924342258309L;

    public UploadFailedException(String site, String target, String url) {
        super();
        this._site = site;
        this._target = target;
        this._url = url;
    }

    public UploadFailedException(String site, String target, String url, Throwable throwable) {
        super(throwable);
        this._site = site;
        this._target = target;
        this._url = url;
    }

    public String getSite() { return _site; }
    public String getTarget() { return _target; }
    public String getUrl() { return _url; }

    protected String _site;
    protected String _target;
    protected String _url;
}
