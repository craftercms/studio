package org.craftercms.studio.api.v2.dal;

import java.util.List;

public class PublishingPackageDetails extends PublishingPackage {

    private List<PublishingPackageItem> items;

    public List<PublishingPackageItem> getItems() {
        return items;
    }

    public void setItems(List<PublishingPackageItem> items) {
        this.items = items;
    }

    public static class PublishingPackageItem {

        private String path;
        private String contentTypeClass;
        private String mimeType;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getContentTypeClass() {
            return contentTypeClass;
        }

        public void setContentTypeClass(String contentTypeClass) {
            this.contentTypeClass = contentTypeClass;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
}
