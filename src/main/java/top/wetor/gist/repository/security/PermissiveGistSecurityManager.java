/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.security;



import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistRepository;

public class PermissiveGistSecurityManager extends SimpleGistSecurityManager {

	@Override
	public boolean canRead(GistRepository repository, User userDetails) {
		return true;
	}

	@Override
	public boolean canWrite(GistRepository repository, User userDetails) {
		return true;
	}

	@Override
	public GistAccessRight getAccessRight(GistRepository repository, User userDetails) {
		return GistAccessRight.WRITE;
	}

}
