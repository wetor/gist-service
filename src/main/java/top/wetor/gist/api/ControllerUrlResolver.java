/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.api;

import top.wetor.gist.model.User;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class ControllerUrlResolver {

	/**
	 * 获取gist的url
	 * @param gistId gistId
	 * @param activeUser user
	 * @return url
	 */
	public String getGistUrl(String gistId, User activeUser) {
		String url = null;
		if(gistId != null) {
			url = linkTo(
					methodOn(GistRestController.class)
					.getGist(gistId, activeUser))
					.withSelfRel()
					.getHref();
			}
		return url;
	}

	/**
	 * 获取comment的url列表
	 * @param gistId gistId
	 * @param activeUser user
	 * @return url列表
	 */
	public String getCommentsUrl(String gistId, User activeUser) {
		String url = null;
		if(gistId != null) {
			url = linkTo(
					methodOn(GistCommentRestController.class)
					.getComments(gistId, activeUser))
					.withSelfRel()
					.getHref();
			}
		return url;
	}

	/**
	 * 获取指定comment的url
	 * @param gistId gistId
	 * @param commentId comment
	 * @param activeUser user
	 * @return url
	 */
	public String getCommentUrl(String gistId, Long commentId, User activeUser) {
		String url = null;
		if(gistId != null && commentId != null) {
			url = linkTo(
					methodOn(GistCommentRestController.class)
					.getComment(gistId, commentId, activeUser))
					.withSelfRel()
					.getHref();
			}
		return url;
	}

	/**
	 * 获取fork的url
	 * @param gistId gistId
	 * @param activeUser user
	 * @return url
	 */
	public String getForksUrl(String gistId, User activeUser) {
		String url = null;
		if(gistId != null) {
			url = linkTo(
					methodOn(GistRestController.class)
					.forkGist(gistId, activeUser))
					.withSelfRel()
					.getHref();
			}
		return url;
	}

	/**
	 * 获取commits的url
	 * @param gistId
	 * @param activeUser
	 * @return
	 */
	public String getCommitsUrl(String gistId, User activeUser) {
		String url = null;
		if(gistId != null) {
			url = linkTo(
					methodOn(GistRestController.class)
							.getCommits(gistId, activeUser))
					.withSelfRel()
					.getHref();
		}
		return url;
	}
}
