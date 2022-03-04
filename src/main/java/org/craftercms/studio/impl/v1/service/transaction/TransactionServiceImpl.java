/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.service.transaction;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.transaction.TransactionService;

import javax.transaction.UserTransaction;


/**
 * Provide different types of transactions to the site 
 * @author russdanner
 */
public class TransactionServiceImpl implements TransactionService {

	/**
	 * get a standard user transaction
	 */
	public UserTransaction getTransaction() {
 		return null;
	}
	
	/** getter for content repository */
	public ContentRepository getContentRepository() { return _contentRepository; }
	/** setter for content repository */
	public void setContentRepository(ContentRepository repo) { _contentRepository = repo; }
	
	protected ContentRepository _contentRepository;
}
