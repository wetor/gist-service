/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import top.wetor.gist.model.GistCommentResponse;

import java.io.File;
import java.util.List;

public interface CommentStore {

	List<GistCommentResponse> load(File store);

	List<GistCommentResponse> save(File store, List<GistCommentResponse> comments);

}
