package org.alfresco.repo.content;

public interface ContentServicePolicies {

    public interface OnContentUpdatePolicy extends org.alfresco.repo.policy.ClassPolicy {
        org.alfresco.service.namespace.QName QNAME = null;

        void onContentUpdate(org.alfresco.service.cmr.repository.NodeRef var1, boolean var2);
    }
}
