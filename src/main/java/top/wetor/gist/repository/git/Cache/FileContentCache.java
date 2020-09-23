/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git.Cache;

import top.wetor.gist.model.FileContent;

public interface FileContentCache {

	FileContent load(String contentId, String path);

	FileContent save(String contentId, String path, FileContent content);

}
