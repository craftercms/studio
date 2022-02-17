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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class EmailMessageQueueTo implements Serializable {

    private static final long serialVersionUID = -8731745205175488556L;
    protected Queue<EmailMessageTO> pendingEmailMessages= new LinkedList<EmailMessageTO>();
	
	public synchronized EmailMessageTO getNext()
	{
		return pendingEmailMessages.poll();
	}
	
	public synchronized List<EmailMessageTO> getAll()
	{
		ArrayList<EmailMessageTO> list = new ArrayList<EmailMessageTO>();
		int size = pendingEmailMessages.size();
		for(int i=0;i<size;i++)
		{
			list.add(pendingEmailMessages.poll());
		}
		return list;
	}
	
	public synchronized void addEmailMessage(EmailMessageTO emailMessage)
	{
		pendingEmailMessages.add(emailMessage);
	}
	
	public synchronized int size()
	{
		return pendingEmailMessages.size();
	}
}
