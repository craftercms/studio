package org.alfresco.repo.node;

public interface NodeServicePolicies {

    public interface OnAddAspectPolicy extends org.alfresco.repo.policy.ClassPolicy {
        org.alfresco.service.namespace.QName QNAME = null;

        void onAddAspect(org.alfresco.service.cmr.repository.NodeRef var1, org.alfresco.service.namespace.QName var2);
    }

    public interface OnRemoveAspectPolicy extends org.alfresco.repo.policy.ClassPolicy {
        org.alfresco.service.namespace.QName QNAME = null;

        void onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef var1, org.alfresco.service.namespace.QName var2);
    }

    public interface OnDeleteNodePolicy extends org.alfresco.repo.policy.ClassPolicy {
        org.alfresco.service.namespace.QName QNAME = null;

        void onDeleteNode(org.alfresco.service.cmr.repository.ChildAssociationRef var1, boolean var2);
    }

    public interface OnMoveNodePolicy extends org.alfresco.repo.policy.ClassPolicy {
        org.alfresco.service.namespace.QName QNAME = null;

        void onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef var1, org.alfresco.service.cmr.repository.ChildAssociationRef var2);
    }
}
