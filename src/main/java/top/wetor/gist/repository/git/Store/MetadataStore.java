/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git.Store;

import top.wetor.gist.model.GistMetadata;

import java.io.File;

public interface MetadataStore {

	GistMetadata load(File store);

	GistMetadata save(File store, GistMetadata metadata);

}
