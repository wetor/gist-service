/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import top.wetor.gist.repository.GistRepository;
import top.wetor.gist.repository.GistRepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class GitGistRepositoryFactory implements GistRepositoryFactory {

	@Autowired
	private GistOperationFactory gistOperationFactory;

	public GistRepository getRepository(File folder) {
		return new GitGistRepository(folder, gistOperationFactory);
	}

}
