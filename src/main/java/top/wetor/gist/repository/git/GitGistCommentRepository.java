/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import top.wetor.gist.model.GistComment;
import top.wetor.gist.model.GistCommentResponse;
import top.wetor.gist.model.GistIdentity;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistCommentRepository;
import top.wetor.gist.repository.GistError;
import top.wetor.gist.repository.GistErrorCode;
import top.wetor.gist.repository.GistRepositoryException;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class GitGistCommentRepository implements GistCommentRepository, Serializable {

	private static final long serialVersionUID = 414766810805325462L;

	private static final Logger logger = LoggerFactory.getLogger(GitGistCommentRepository.class);

	private File commentsFile;

	private CommentStore commentStore;

	public GitGistCommentRepository(File commentsFile, CommentStore commentStore) {
		this.commentsFile = commentsFile;
		this.commentStore = commentStore;
	}

	/* (non-Javadoc)
	 * @see top.wetor.gist.repository.IGistCommentRepository#getComments()
	 */
	@Override
	public List<GistCommentResponse> getComments(User activeUser) {
		return loadComments();
	}

	/* (non-Javadoc)
	 * @see top.wetor.gist.repository.IGistCommentRepository#getComment(long)
	 */
	@Override
	public GistCommentResponse getComment(long commentId, User activeUser) {
		List<GistCommentResponse> comments = this.loadComments();
		return this.findComment(comments, commentId);
	}

	/* (non-Javadoc)
	 * @see top.wetor.gist.repository.IGistCommentRepository#createComment(top.wetor.gist.model.GistComment, org.springframework.security.core.userdetails.User)
	 */
	@Override
	public GistCommentResponse createComment(GistComment comment, User user) {
		GistCommentResponse response = new GistCommentResponse();
		DateTime now = DateTime.now();
		response.setCreatedAt(now);
		response.setUpdatedAt(now);
		response.setBody(comment.getBody());
		GistIdentity userIdentity = new GistIdentity();
		userIdentity.setLogin(user.getUsername());
		response.setUser(userIdentity);
		List<GistCommentResponse> comments = loadComments();
		long id = 1L;
		if(!comments.isEmpty()) {
			id = comments.get(comments.size() - 1).getId() + 1;
		}
		response.setId(id);
		comments.add(response);
		this.saveComments(comments);
		return response;
	}

	/* (non-Javadoc)
	 * @see top.wetor.gist.repository.IGistCommentRepository#editComment(long, top.wetor.gist.model.GistComment, org.springframework.security.core.userdetails.User)
	 */
	@Override
	public GistCommentResponse editComment(long commentId, GistComment comment, User user) {
		List<GistCommentResponse> comments = this.loadComments();
		GistCommentResponse commentResponse = this.findComment(comments, commentId);
		if(commentResponse != null) {
			commentResponse.setBody(comment.getBody());
			commentResponse.setUpdatedAt(new DateTime());
		}
		this.saveComments(comments);
		return commentResponse;
	}

	/* (non-Javadoc)
	 * @see top.wetor.gist.repository.IGistCommentRepository#deleteComment(long)
	 */
	@Override
	public void deleteComment(long commentId, User activeUser) {
		List<GistCommentResponse> comments = this.loadComments();
		GistCommentResponse comment = this.findComment(comments, commentId);
		if(comment != null) {
			comments.remove(comment);
		}
		this.saveComments(comments);
	}

	private GistCommentResponse findComment(List<GistCommentResponse> comments, long id) {
		final long commentId = id;
		if (comments != null) {
			return IterableUtils.find(comments, new Predicate<GistCommentResponse>() {
				public boolean evaluate(GistCommentResponse comment) {
					return comment.getId().equals(commentId);
				}
			});
		}

		GistError error = new GistError(GistErrorCode.ERR_COMMENT_NOT_EXIST, "Comment with id {} does not exist", id);
		logger.warn(error.getFormattedMessage());
		throw new GistRepositoryException(error);

	}

	private List<GistCommentResponse> loadComments() {
		return this.commentStore.load(this.commentsFile);
	}

	private void saveComments(List<GistCommentResponse> comments) {
		this.commentStore.save(this.commentsFile, comments);
	}

}
