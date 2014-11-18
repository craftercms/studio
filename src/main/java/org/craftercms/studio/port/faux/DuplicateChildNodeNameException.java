package org.alfresco.service.cmr.repository;

public class DuplicateChildNodeNameException extends RuntimeException implements org.alfresco.repo.transaction.DoNotRetryException {

    public DuplicateChildNodeNameException() { }
    public DuplicateChildNodeNameException(org.alfresco.service.cmr.repository.NodeRef parentNodeRef, org.alfresco.service.namespace.QName assocTypeQName, String name, Throwable e) { }
}