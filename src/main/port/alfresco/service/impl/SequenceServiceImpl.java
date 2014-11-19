/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.service.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.sequence.SequenceDaoService;
import org.craftercms.cstudio.alfresco.service.api.SequenceService;
import org.craftercms.cstudio.alfresco.service.exception.SequenceException;
import org.craftercms.cstudio.alfresco.to.SequenceTO;

public class SequenceServiceImpl extends AbstractRegistrableService implements SequenceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceServiceImpl.class);

	private static final String DEFAULT_NAMESPACE = "default";
	private Map<String, SequenceTO> sequences = new HashMap<String, SequenceTO>();

	private SequenceDaoService _sequenceDaoService;

    @Override
    public void register() {
        getServicesManager().registerService(SequenceService.class, this);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.craftercms.crafter.alfresco.service.api.SequenceService#createSequence
      * (java.lang.String)
      */
	public boolean createSequence(String namespace) {
		try {
			namespace = toLowerCase(namespace);
			if (sequenceExists(namespace)) {
				return true;
			} else {
				SequenceTO sequence = _sequenceDaoService.createSequence(namespace);
				if (sequence != null) {
					sequences.put(namespace, sequence);
					return true;
				}
			}
		} catch (SequenceException e) {
			LOGGER.error("Failed to create a sequence for " + namespace, e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.craftercms.crafter.alfresco.service.api.SequenceService#next()
	 */
	public long next() throws SequenceException {
		return next(DEFAULT_NAMESPACE, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.api.SequenceService#next(java.lang
	 * .String)
	 */
	public long next(String namespace) throws SequenceException {
		return next(namespace, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.api.SequenceService#next(java.lang
	 * .String, boolean)
	 */
	public synchronized long next(String namespace, boolean create) throws SequenceException {
		namespace = toLowerCase(namespace);
		SequenceTO sequence = sequences.get(namespace);
		if (sequence == null) {
			// look for the sequence in DB
			try {
				sequence = _sequenceDaoService.getIdSpace(namespace, create);
				sequences.put(namespace, sequence);
			} catch (SQLException e) {
				throw new SequenceException(e);
			}
		}
		if (sequence != null) {
			Long next = sequence.getNext();
			// if the ID space is filled, get the next sequence generator
			if (next == sequence.getLast()) {
				try {
					sequence = _sequenceDaoService.getIdSpace(namespace, false);
					sequences.put(namespace, sequence);
				} catch (SQLException e) {
					throw new SequenceException(e);
				}
			} else {
				sequence.setNext(next + 1);
			}
			return next;
		} else {
			throw new SequenceException("No sequence found for namespace: " + namespace);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.crafter.alfresco.service.api.SequenceService#deleteSequence(java.lang.String)
	 */
	public void deleteSequence(String namespace) throws SequenceException {
		namespace = toLowerCase(namespace);
		_sequenceDaoService.deleteSequence(namespace);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.api.SequenceService#sequenceExists
	 * (java.lang.String)
	 */
	public boolean sequenceExists(String namespace) {
		try {
			namespace = toLowerCase(namespace);
			// check if the sequence is already loaded
			SequenceTO sequence = sequences.get(namespace);
			if (sequence != null) {
				return true;
			} else {
				sequence = _sequenceDaoService.getSequence(namespace);
				return sequence != null;
			}
		} catch (SequenceException e) {
			LOGGER.error("Failed to check if a sequence exists for " + namespace, e);
			return false;
		}
	}



	/**
	 * change the given string to lower case
	 * 
	 * @param namespace
	 * @return namespace in lower case
	 * @throws SequenceException
	 */
	private String toLowerCase(String namespace) throws SequenceException {
		if (namespace != null && namespace.trim().length() > 0) {
			return namespace.toLowerCase();
		} else {
			throw new SequenceException("Namespace must not be empty");
		}
	}

	/**
	 * @param sequenceDaoService the sequenceDaoService to set
	 */
	public void setSequenceDaoService(SequenceDaoService sequenceDaoService) {
		this._sequenceDaoService = sequenceDaoService;
	}

}
