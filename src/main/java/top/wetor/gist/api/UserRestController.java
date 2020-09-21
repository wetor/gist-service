/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.api;

import org.springframework.web.bind.annotation.*;
import top.wetor.gist.model.GistIdentity;
import top.wetor.gist.model.GistResponse;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

@CrossOrigin(origins = "http://0.0.0.0:8080")
@RestController()
@RequestMapping(value = "/users", produces = {
		MediaType.APPLICATION_JSON_VALUE,
		"application/vnd.github.beta+json",
		"application/vnd.github.v3+json"
		})
public class UserRestController {

	@Autowired
	private GistRepositoryService repository;

	@RequestMapping(method = RequestMethod.GET)
	public GistIdentity getUser() {
		GistIdentity response = new GistIdentity();
		String username = "admin";
		response.setLogin(username);
		return response;
	}

	/**
	 * 获取user的所有gist
	 * @param username username
	 * @return gist列表
	 */
	@RequestMapping(value = "/{username}/gists", method = RequestMethod.GET)
	public List<GistResponse> getUsersPublicGists(@PathVariable("username") String username) {

		return repository.listGists(new User());
	}

}
