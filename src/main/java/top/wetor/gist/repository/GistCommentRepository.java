/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository;

import top.wetor.gist.model.GistComment;
import top.wetor.gist.model.GistCommentResponse;
import top.wetor.gist.model.User;

import java.util.List;

public interface GistCommentRepository {

	List<GistCommentResponse> getComments(User userDetails);

	GistCommentResponse getComment(long commentId, User userDetails);

	GistCommentResponse createComment(GistComment comment, User user);

	GistCommentResponse editComment(long commentId, GistComment comment, User user);

	void deleteComment(long commentId, User userDetails);

}
