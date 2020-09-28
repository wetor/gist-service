/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.security;

import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistRepository;
import top.wetor.gist.repository.GistSecurityManager;
import top.wetor.gist.model.GistMetadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SimpleGistSecurityManager implements GistSecurityManager {

    private static final Set<GistAccessRight> READ_RIGHTS = new HashSet<>(
            Arrays.asList(GistAccessRight.READ, GistAccessRight.WRITE));
    private static final Set<GistAccessRight> WRITE_RIGHTS = new HashSet<>(Arrays.asList(GistAccessRight.WRITE));

    @Override
    public boolean canRead(GistRepository repository, User userDetails) {
        return READ_RIGHTS.contains(this.getAccessRight(repository, userDetails));
    }

    @Override
    public boolean canWrite(GistRepository repository, User userDetails) {
        return WRITE_RIGHTS.contains(this.getAccessRight(repository, userDetails));
    }

    @Override
    public boolean isOwner(GistRepository repository, User userDetails) {
        return GistRole.OWNER.equals(this.getRole(repository, userDetails));
    }

    @Override
    public GistRole getRole(GistRepository repository, User userDetails) {
        return this.isOwner(this.getMetaData(repository), userDetails) ? GistRole.OWNER : GistRole.COLLABORATOR;
    }

    @Override
    public GistAccessRight getAccessRight(GistRepository repository, User userDetails) {
        GistMetadata metadata = getMetaData(repository);

        if (this.canWrite(metadata, userDetails)) {
            return GistAccessRight.WRITE;
        }
        if (this.canRead(metadata, userDetails)) {
            return GistAccessRight.READ;
        }
        return GistAccessRight.NONE;
    }

    private boolean canRead(GistMetadata metadata, User userDetails) {
        return true;
    }

    private boolean canWrite(GistMetadata metadata, User userDetails) {
        return this.isOwner(metadata, userDetails);
    }

    private GistMetadata getMetaData(GistRepository repository) {
        return repository.getMetadata();
    }

    private boolean isOwner(GistMetadata metadata, User userDetails) {
        return userDetails.getUsername().equals(metadata.getOwner());
    }

    @Override
    public boolean canCreate(User userDetails) {
        return true;
    }

    @Override
    public boolean canCreateAs(User userDetails, String otherUser) {
        return false;
    }

}
