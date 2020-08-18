/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import top.wetor.gist.model.GistHistory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultHistoryCache implements HistoryCache {

	@Override
	@Cacheable(value = "historystore", key = "#commitId")
	public List<GistHistory> load(String commitId) {
		return new ArrayList<GistHistory>();
	}

	@Override
	@CachePut(cacheNames = "historystore", key = "#commitId")
	public List<GistHistory> save(String commitId, List<GistHistory> history) {
		return history;
	}

}
