/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.api;

import top.wetor.gist.model.GistComment;
import top.wetor.gist.model.GistCommentResponse;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@CrossOrigin(origins = "http://0.0.0.0:8080")
@RestController()
@RequestMapping(value = "/gists/{gistId}/comments", produces={
		MediaType.APPLICATION_JSON_VALUE,
		"application/vnd.github.beta+json",
		"application/vnd.github.v3+json" })
@CacheConfig(cacheNames="comments")
public class GistCommentRestController {

	@Autowired
	private GistRepositoryService repository;

	@Autowired
	private ControllerUrlResolver resolver;

	/**
	 * 获取gistId的所有comment
	 * @param gistId gistId
	 * @param activeUser user
	 * @return comment列表
	 */
	@RequestMapping(method= RequestMethod.GET)
	@Cacheable(key="#gistId")
	public List<GistCommentResponse> getComments(@PathVariable("gistId") String gistId, User activeUser) {
		List<GistCommentResponse> comments = repository.getComments(gistId, activeUser);
		this.decorateUrls(comments, gistId, activeUser);
		return comments;
	}

	/**
	 * 获取指定comment
	 * @param gistId gistId
	 * @param commentId commentId
	 * @param activeUser user
	 * @return comment
	 */
	@RequestMapping(value="/{commentId}", method= RequestMethod.GET)
	@Cacheable(key="{#gistId, #commentId}")
	public GistCommentResponse getComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, User activeUser) {
		GistCommentResponse response = repository.getComment(gistId, commentId, activeUser);
		this.decorateUrls(response, gistId, activeUser);
		return response;
	}

	/**
	 * 需要登录。发表comment
	 * @param gistId gistId
	 * @param comment comment内容
	 * @param activeUser user
	 * @return comment
	 */
	@RequestMapping(method= RequestMethod.POST)
	//@PreAuthorize(GistRestController.USER_ROLE_AUTHORITY)
	@ResponseStatus( HttpStatus.CREATED )
	@CacheEvict(key="#gistId")
	public GistCommentResponse createComment(@PathVariable("gistId") String gistId, @RequestBody GistComment comment, User activeUser) {
		GistCommentResponse response = repository.createComment(gistId, comment, activeUser);
		this.decorateUrls(response, gistId, activeUser);
		return response;
	}

	/**
	 * 需要登录。修改comment
	 * @param gistId gistId
	 * @param commentId commentId
	 * @param comment comment内容
	 * @param activeUser user
	 * @return comment
	 */
	@RequestMapping(value="/{commentId}", method= RequestMethod.PATCH)
	//@PreAuthorize(GistRestController.USER_ROLE_AUTHORITY)
	@CachePut(key="{#gistId, #commentId}")
	@Caching(evict = @CacheEvict(key="#gistId"), put = @CachePut(key="{#gistId, #commentId}"))
	public GistCommentResponse editComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, @RequestBody GistComment comment, User activeUser) {
		GistCommentResponse response = repository.editComment(gistId, commentId, comment, activeUser);
		this.decorateUrls(response, gistId, activeUser);
		return response;
	}

	/**
	 * 需要登录。删除comment
	 * @param gistId gistId
	 * @param commentId commentId
	 * @param activeUser user
	 */
	@RequestMapping(value="/{commentId}", method= RequestMethod.DELETE)
	//@PreAuthorize(GistRestController.USER_ROLE_AUTHORITY)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	@Caching(evict = { @CacheEvict(key="#gistId"), @CacheEvict(key="{#gistId, #commentId}") })
	public void deleteComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, User activeUser) {
		repository.deleteComment(gistId, commentId, activeUser);
	}

	/**
	 * 处理多个gistCommentResponse，添加url等信息
	 * @param gistCommentResponses gistCommentResponse列表
	 * @param gistId gistId
	 * @param activeUser user
	 */
	private void decorateUrls(Collection<GistCommentResponse> gistCommentResponses, String gistId, User activeUser) {
		if(gistCommentResponses != null) {
			for(GistCommentResponse gistResponse: gistCommentResponses) {
				this.decorateUrls(gistResponse, gistId, activeUser);
			}
		}
	}

	/**
	 * 处理单个gistCommentResponse，添加url等信息
	 * @param gistCommentResponse comment
	 * @param gistId gistId
	 * @param activeUser user
	 */
	private void decorateUrls(GistCommentResponse gistCommentResponse, String gistId, User activeUser) {
		if(gistCommentResponse != null) {
			gistCommentResponse.setUrl(resolver.getCommentUrl(gistId, gistCommentResponse.getId(), activeUser));
		}
	}

}
