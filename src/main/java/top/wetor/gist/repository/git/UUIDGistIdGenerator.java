/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import top.wetor.gist.repository.GistIdGenerator;

import java.util.UUID;

public class UUIDGistIdGenerator implements GistIdGenerator {

	/* (non-Javadoc)
	 * @see top.wetor.gist.api.GistIdGenerator#generateId()
	 */
	@Override
	public String generateId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
