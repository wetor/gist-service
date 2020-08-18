/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository;

import top.wetor.gist.model.GistRequest;
import top.wetor.gist.model.GistResponse;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.git.GistMetadata;

import java.io.File;

public interface GistRepository {

	File getGistRepositoryFolder(User userDetails);
	
	File getGistGitRepositoryFolder(User userDetails);

	GistResponse readGist(User userDetails);

	GistResponse readGist(String commitId, User userDetails);

	GistResponse createGist(GistRequest request, String gistId, User userDetails);

	GistResponse updateGist(GistRequest request, User userDetails);

	GistResponse forkGist(GistRepository forkedRepository, String gistId, User userDetails);

	String getId();

	void registerFork(GistRepository forkedRepository);
	
	GistMetadata getMetadata();
	
	GistCommentRepository getCommentRepository();
	
	
}
