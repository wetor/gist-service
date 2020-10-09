/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository;

import top.wetor.gist.model.*;

import java.util.List;

public interface GistRepositoryService {

	public List<GistResponse> listGists(User activeUser);

	public List<GistResponse> listPublicGists(User activeUser);

	public GistResponse getGist(String gistId, User activeUser);

	public GistResponse getGist(String gistId, String commitId, User activeUser);

	public GistResponse createGist(GistRequest request, User user);

	public GistResponse editGist(String gistId, GistRequest request, User activeUser);

	public void deleteGist(String gistId, User activeUser);

	public List<GistCommentResponse> getComments(String gistId, User activeUser);

	public GistCommentResponse getComment(String gistId, long commentId, User activeUser);

	public GistCommentResponse createComment(String gistId, GistComment comment, User activeUser);

	public GistCommentResponse  editComment(String gistId, long commentId, GistComment comment, User activeUser);

	public void deleteComment(String gistId, long commentId, User activeUser);

	public List<GistHistory> getCommits(String gistId, User activeUser);

	public GistHistory getCommit(String gistId, String commitId, User activeUser);

	public GistResponse forkGist(String gistId, User activeUser);
	
	public List<Fork> getForks(String gistId, User activeUser);

	public GistDiff getDiff(String gistId, String newCommitId, String oldCommitId);

}
