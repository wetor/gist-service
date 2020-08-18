/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.wetor.gist.model.GistCommentResponse;
import top.wetor.gist.repository.GistError;
import top.wetor.gist.repository.GistErrorCode;
import top.wetor.gist.repository.GistRepositoryError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GistCommentStore implements CommentStore {

	private static final Logger logger = LoggerFactory.getLogger(GistCommentStore.class);

	@Autowired
	private ObjectMapper objectMapper;

	public GistCommentStore() {
		this.objectMapper = new ObjectMapper();
	}

	@Autowired
	public GistCommentStore(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	@Cacheable(value = "commentstore", key = "#store.getAbsolutePath()")
	public List<GistCommentResponse> load(File store) {
		List<GistCommentResponse> comments = new ArrayList<>();
		if (store.exists()) {
			try {
				comments = objectMapper.readValue(store, new TypeReference<List<GistCommentResponse>>() {
				});
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_COMMENTS_NOT_READABLE, "Could not read comments");
				logger.error(error.getFormattedMessage() + " with path {}", store);
				throw new GistRepositoryError(error, e);
			}
		}
		return comments == null? new ArrayList<GistCommentResponse>(): comments;
	}

	@Override
	@CachePut(cacheNames = "commentstore", key = "#store.getAbsolutePath()")
	public List<GistCommentResponse> save(File store, List<GistCommentResponse> comments) {
		if(comments != null) {
			try {
				objectMapper.writeValue(store, comments);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_COMMENTS_NOT_WRITEABLE, "Could not save comments");
				logger.error(error.getFormattedMessage() + " with path {}", store);
				throw new GistRepositoryError(error, e);
			}
		}
		return comments;
	}



}
