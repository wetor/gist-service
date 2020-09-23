/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git.Service;

import top.wetor.gist.CustomLock;
import top.wetor.gist.model.*;
import top.wetor.gist.repository.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wetor.gist.repository.git.*;
import top.wetor.gist.repository.git.StorageLocator.AsymetricFourFolderRepositoryStorageLocator;
import top.wetor.gist.repository.git.StorageLocator.RepositoryStorageLocator;
import top.wetor.gist.repository.git.StorageLocator.SymetricFourPartRepositoryStorageLocator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class GitGistRepositoryService implements GistRepositoryService {

    private static final int DEFAULT_LOCK_TIMEOUT = 30;

    private int lockTimeout = DEFAULT_LOCK_TIMEOUT;

    private static final String RECYCLE_FOLDER_NAME = ".recycle";

    private Logger logger = LoggerFactory.getLogger(GitGistRepositoryService.class);

    private File repositoryRoot;
    private File recycleRoot;
    private GistIdGenerator idGenerator;
    private GistSecurityManager securityManager;
    private GistRepositoryFactory repositoryFactory;
    private List<RepositoryStorageLocator> locators;

    public GitGistRepositoryService(String repositoryRoot, GistIdGenerator idGenerator) throws IOException {
        this.repositoryRoot = new File(repositoryRoot);
        if (!this.repositoryRoot.exists()) {
            FileUtils.forceMkdir(this.repositoryRoot);
        }
        recycleRoot = new File(repositoryRoot, RECYCLE_FOLDER_NAME);
        if (!this.recycleRoot.exists()) {
            FileUtils.forceMkdir(this.recycleRoot);
        }
        this.idGenerator = idGenerator;

        locators = Arrays.asList(new AsymetricFourFolderRepositoryStorageLocator(this.repositoryRoot),
                new SymetricFourPartRepositoryStorageLocator(this.repositoryRoot));
    }

    public void setGistRepositoryFactory(GistRepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public void setLockTimeout(int timeout) {
        this.lockTimeout = timeout;
    }

    public GistSecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(GistSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public List<GistResponse> listGists(User user) {
        List<GistResponse> gists = new ArrayList<GistResponse>();
        for (File file : FileUtils.listFiles(repositoryRoot,
                FileFilterUtils.and(FileFileFilter.FILE, new NameFileFilter(RepositoryLayout.GIST_META_FILE)),
                TrueFileFilter.INSTANCE)) {
            GistRepository repository = repositoryFactory.getRepository(file.getParentFile());
            if (this.securityManager.isOwner(repository, user)) {
                gists.add(repository.readGist(user));
            }
        }
        return gists;
    }
    @Override
    public List<GistResponse> listPublicGists(User user) {
        List<GistResponse> gists = new ArrayList<GistResponse>();
        for (File file : FileUtils.listFiles(repositoryRoot,
                FileFilterUtils.and(FileFileFilter.FILE, new NameFileFilter(RepositoryLayout.GIST_META_FILE)),
                TrueFileFilter.INSTANCE)) {
            GistRepository repository = repositoryFactory.getRepository(file.getParentFile());
            if (this.securityManager.canRead(repository, user)) {
                gists.add(repository.readGist(user));
            }
        }
        return gists;
    }
    @Override
    public GistResponse getGist(String gistId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(repository, user);
            return repository.readGist(user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistResponse getGist(String gistId, String commitId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(repository, user);
            return repository.readGist(commitId, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistResponse createGist(GistRequest request, User user) {
        this.ensureCreateable(request, user);
        String gistId = idGenerator.generateId();
        File repositoryFolder = getRepositoryFolder(gistId);
        GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
        return repository.createGist(request, gistId, user);
    }

    @Override
    public GistResponse forkGist(String gistToForkId, User user) {
        Lock lock = acquireGistLock(gistToForkId);
        try {
            File gistToForkRepositoryFolder = getAndValidateRepositoryFolder(gistToForkId);
            GistRepository gistToForkRepository = repositoryFactory.getRepository(gistToForkRepositoryFolder);
            this.ensureReadable(gistToForkRepository, user);
            String gistId = idGenerator.generateId();
            File repositoryFolder = getRepositoryFolder(gistId);
            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
            return repository.forkGist(gistToForkRepository, gistId, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistResponse editGist(String gistId, GistRequest request, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureWritable(repository, user);
            return repository.updateGist(request, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteGist(String gistId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureWritable(repository, user);
            FileUtils.moveDirectoryToDirectory(repositoryFolder, new File(recycleRoot, gistId), true);
            FileUtils.forceDelete(repositoryFolder);
        } catch (IOException e) {
            GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
                    "Could not delete gist {}, an internal error has occurred", gistId);
            logger.error(error.getFormattedMessage());
            throw new GistRepositoryError(error, e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Fork> getForks(String gistId, User activeUser) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(repository, activeUser);
            GistMetadata metadata = repository.getMetadata();
            List<Fork> forks = metadata.getForks();
            if (forks == null) {
                forks = Collections.emptyList();
            }
            return forks;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<GistCommentResponse> getComments(String gistId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(gistRepository, user);
            GistCommentRepository repository = gistRepository.getCommentRepository();
            return repository.getComments(user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistCommentResponse getComment(String gistId, long commentId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(gistRepository, user);
            GistCommentRepository repository = gistRepository.getCommentRepository();
            return repository.getComment(commentId, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistCommentResponse createComment(String gistId, GistComment comment, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureWritable(gistRepository, user);
            GistCommentRepository repository = gistRepository.getCommentRepository();
            return repository.createComment(comment, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistCommentResponse editComment(String gistId, long commentId, GistComment comment, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureWritable(gistRepository, user);
            GistCommentRepository repository = gistRepository.getCommentRepository();
            return repository.editComment(commentId, comment, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteComment(String gistId, long commentId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureWritable(gistRepository, user);
            GistCommentRepository repository = gistRepository.getCommentRepository();
            repository.deleteComment(commentId, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<GistHistory> getCommits(String gistId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(gistRepository, user);
            GistCommitRepository repository = gistRepository.getCommitRepository();
            return repository.getCommits(gistId, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GistHistory getCommit(String gistId, String commitId, User user) {
        Lock lock = acquireGistLock(gistId);
        try {
            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
            GistRepository gistRepository = repositoryFactory.getRepository(repositoryFolder);
            this.ensureReadable(gistRepository, user);
            GistCommitRepository repository = gistRepository.getCommitRepository();
            return repository.getCommit(gistId, commitId, user);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getDiff(String gistId,String oldCommitId,String newCommitId){
        return "";
//        try {
//            File repositoryFolder = getAndValidateRepositoryFolder(gistId);
//            GistRepository repository = repositoryFactory.getRepository(repositoryFolder);
//            Git gitCommand = new Git(repository);
//            List<DiffEntry> diffEntries = listDiff(repository, gitCommand, oldCommitId, newCommitId);
//            StringBuilder sb = new StringBuilder();
//            for (DiffEntry entry : diffEntries) {
//                sb.append(entry.getChangeType().toString())
//                        .append(" : ")
//                        .append(
//                                entry.getOldPath().equals(entry.getNewPath()) ? entry.getNewPath() : entry.getOldPath()
//                                        + " -> " + entry.getNewPath()
//                        );
//
//                OutputStream output = new OutputStream() {
//                    StringBuilder builder = new StringBuilder();
//
//                    @Override
//                    public void write(int b) throws IOException {
//                        builder.append((char) b);
//                    }
//
//                    public String toString() {
//                        return this.builder.toString();
//                    }
//                };
//
//                try (DiffFormatter formatter = new DiffFormatter(output)) {
//                    formatter.setRepository(repository);
//                    formatter.format(entry);
//                }
//                sb.append("\n").append(output.toString());
//            }
//            return sb.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "";
//        } catch (GitAPIException e) {
//            e.printStackTrace();
//            return "";
//        }
    }

    private Lock acquireGistLock(String gistId) {
        Lock lock = new CustomLock();
        try {
            if (!lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
                GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
                        "Could not access gist {}, it is currently being updated", gistId);
                logger.error(error.getFormattedMessage());
                throw new GistRepositoryException(error);
            }
        } catch (InterruptedException e) {
            GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
                    "Could not acess gist {}, it is currently being updated", gistId);
            logger.error(error.getFormattedMessage());
            throw new GistRepositoryException(error);
        }
        return lock;
    }

    private File getAndValidateRepositoryFolder(String id) {
        for (RepositoryStorageLocator locator : this.locators) {
            File repositoryFolder = locator.getStoragePath(id);
            if (repositoryFolder.exists()) {
                return repositoryFolder;
            }
        }
        GistError error = new GistError(GistErrorCode.ERR_GIST_NOT_EXIST, "Gist with id {} does not exist", id);
        logger.error(error.getFormattedMessage());
        throw new GistRepositoryException(error);

    }

    private File getRepositoryFolder(String id) {
        return this.locators.get(0).getStoragePath(id);
    }

    private void ensureReadable(GistRepository repository, User user) {
        if (!this.securityManager.canRead(repository, user)) {
            GistError error = new GistError(GistErrorCode.ERR_ACL_READ_DENIED,
                    "You do not have permission to read the gist with id {}.", repository.getId());
            logger.error(error.getFormattedMessage());
            throw new GistAccessDeniedException(error);
        }
    }

    private void ensureWritable(GistRepository repository, User user) {
        if (!this.securityManager.canWrite(repository, user)) {
            GistError error = new GistError(GistErrorCode.ERR_ACL_WRITE_DENIED,
                    "You do not have permission to alter the gist with id {}.", repository.getId());
            logger.error(error.getFormattedMessage());
            throw new GistAccessDeniedException(error);
        }
    }
    
    private void ensureCreateable(GistRequest request, User user) {
        String owner = request.getOwner();
        if(StringUtils.isNotBlank(owner)) {
            if(!this.securityManager.canCreateAs(user, owner)) {
                GistError error = new GistError(GistErrorCode.ERR_ACL_CREATE_DENIED,
                        "Your user {} does not have permission to create a gist as {}.", user.getUsername(), owner);
                logger.error(error.getFormattedMessage());
                throw new GistAccessDeniedException(error);
            }
        } else {
            if(!this.securityManager.canCreate(user)) {
                GistError error = new GistError(GistErrorCode.ERR_ACL_CREATE_DENIED,
                        "Your user {} does not have permission to create a gist.", user.getUsername());
                logger.error(error.getFormattedMessage());
                throw new GistAccessDeniedException(error);
            }
        }
    }
    
}
