/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.api;

import top.wetor.gist.model.GistIdentity;
import top.wetor.gist.model.GistResponse;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping(value = "/user", produces = {
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
	
	@RequestMapping(value = "/{username}/gists", method = RequestMethod.GET)
	public List<GistResponse> getUsersPublicGists(@PathVariable("username") String username) {

		return repository.listGists(new User());
	}

}
