package org.alfresco.service;

public class ServiceException extends RuntimeException {
	public ServiceException(String m, Exception e){}
	public ServiceException() {} 
	public ServiceException(Exception e) {}
	public ServiceException(String s) {}

}