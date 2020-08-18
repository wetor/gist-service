/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository;

import java.io.File;

public interface GistRepositoryFactory {

	GistRepository getRepository(File folder);

}
