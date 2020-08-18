/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository;

import top.wetor.gist.model.User;


public interface GistSecurityManager {

	public enum GistAccessRight {
		NONE, READ, WRITE
	}

	public enum GistRole {
		NONE, OWNER, COLLABORATOR
	}

	boolean canCreate(User userDetails);
	
	boolean canCreateAs(User userDetails, String otherUser);
	
	boolean canRead(GistRepository repository, User userDetails);

	boolean canWrite(GistRepository repository, User userDetails);

	GistAccessRight getAccessRight(GistRepository repository, User userDetails);

	boolean isOwner(GistRepository repository, User userDetails);

	GistRole getRole(GistRepository repository, User userDetails);
	
}
