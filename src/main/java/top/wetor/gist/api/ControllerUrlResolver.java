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

}
