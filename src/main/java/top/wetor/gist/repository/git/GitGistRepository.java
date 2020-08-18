/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import top.wetor.gist.model.Fork;
import top.wetor.gist.model.GistRequest;
import top.wetor.gist.model.GistResponse;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistCommentRepository;
import top.wetor.gist.repository.GistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;

public class GitGistRepository implements GistRepository, Serializable {

    private static final long serialVersionUID = -8235501365399798269L;

    private static final Logger logger = LoggerFactory.getLogger(GitGistRepository.class);

    static final String B64_BINARY_EXTENSION = "b64";

    private GistOperationFactory gistOperationFactory;

    private RepositoryLayout layout;

    private MetadataStore metadataStore;

    private CommentStore commentStore;

    public GitGistRepository(File repositoryFolder) {
        this(repositoryFolder, new GistOperationFactory());
    }

    public GitGistRepository(File repositoryFolder, GistOperationFactory gistOperationFactory) {
        this.gistOperationFactory = gistOperationFactory;
        this.metadataStore = gistOperationFactory.getMetadataStore();
        this.commentStore = gistOperationFactory.getCommentStore();
        InitRepositoryLayoutOperation op = new InitRepositoryLayoutOperation(repositoryFolder);
        this.layout = op.call();
    }

    @Override
    public String getId() {
        return this.getMetadata().getId();
    }

    @Override
    public File getGistRepositoryFolder(User owner) {
        return layout.getRootFolder();
    }

    public File getGistGitRepositoryFolder(User owner) {
        return layout.getBareFolder();
    }

    @Override
    public GistResponse readGist(User userDetails) {
        return readGistInternal(userDetails);
    }

    @Override
    public GistResponse readGist(String commitId, User userDetails) {
        return readGistInternal(commitId, userDetails);
    }

    @Override
    public GistResponse createGist(GistRequest request, String gistId, User userDetails) {
        CreateOrUpdateGistOperation op = gistOperationFactory.getCreateOrUpdateOperation(layout, gistId, request,
                userDetails);
        return op.call();
    }

    @Override
    public GistResponse forkGist(GistRepository originalRepository, String gistId, User userDetails) {
        ForkGistOperation op = gistOperationFactory.getForkOperation(layout, gistId, originalRepository, this,
                userDetails);
        return op.call();
    }

    @Override
    public GistResponse updateGist(GistRequest request, User userDetails) {
        CreateOrUpdateGistOperation op = gistOperationFactory.getCreateOrUpdateOperation(layout, this.getId(), request,
                userDetails);
        return op.call();
    }

    @Override
    public GistMetadata getMetadata() {
        return metadataStore.load(layout.getMetadataFile());
    }

    @Override
    public void registerFork(GistRepository forkedRepository) {
        this.updateForkInformation(forkedRepository);
    }

    @Override
    public GistCommentRepository getCommentRepository() {
        return new GitGistCommentRepository(this.layout.getCommentsFile(), this.commentStore);
    }

    private void saveMetadata(GistMetadata metadata) {
        metadataStore.save(this.layout.getMetadataFile(), metadata);
    }

    private GistResponse readGistInternal(String commitId, User activeUser) {
        ReadGistOperation op = gistOperationFactory.getReadOperation(layout, this.getId(), activeUser, commitId);
        return op.call();
    }

    private GistResponse readGistInternal(User activeUser) {
        return this.readGistInternal(null, activeUser);
    }

    private void updateForkInformation(GistRepository forkedRepository) {
        GistMetadata metadata = this.getMetadata();
        GistMetadata forksMetadata = forkedRepository.getMetadata();
        Fork fork = new Fork();
        fork.setId(forksMetadata.getId());
        metadata.addOrUpdateFork(fork);
        this.saveMetadata(metadata);
    }

}
