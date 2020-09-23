/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git.Cache;

import top.wetor.gist.model.GistHistory;

import java.util.List;

public interface HistoryCache {

	public List<GistHistory> load(String commitId);

	public List<GistHistory> save(String commitId, List<GistHistory> history);

}
