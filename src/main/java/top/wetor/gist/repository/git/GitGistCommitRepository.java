package top.wetor.gist.repository.git;


import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import top.wetor.gist.model.FileContent;
import top.wetor.gist.model.GistHistory;
import top.wetor.gist.model.GistResponse;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistCommitRepository;
import top.wetor.gist.repository.GistError;
import top.wetor.gist.repository.GistErrorCode;
import top.wetor.gist.repository.GistRepositoryError;
import top.wetor.gist.repository.git.Cache.HistoryCache;
import top.wetor.gist.repository.git.Operation.GitHistoryOperation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GitGistCommitRepository implements GistCommitRepository, Serializable {
    private static final long serialVersionUID = -6521001365366018269L;

    private static final Logger logger = LoggerFactory.getLogger(GitGistCommitRepository.class);

    public static final String REF_HEAD_MASTER = "refs/heads/master";

    private RepositoryLayout layout;

    private String gistId;

    private String commitId = null;

    private Grgit git;

    private HistoryCache historyCache = new HistoryCache() {

        @Override
        public List<GistHistory> load(String commitId) {
            return new LinkedList<>();
        }

        @Override
        public List<GistHistory> save(String commitId, List<GistHistory> history) {
            return history;
        }

    };

    public GitGistCommitRepository(RepositoryLayout layout, String gistId){
        this.layout = layout;
        this.gistId = gistId;
        this.git = openRepository();
    }

    @Override
    public List<GistHistory> getCommits(String gistId, User userDetails) {
        try {
            Repository repository = git.getRepository().getJgit().getRepository();
            RevCommit revCommit = resolveCommit(repository);

            List<GistHistory> history = Collections.emptyList();
            if(revCommit != null) {
                history = getHistory(git, revCommit);
            }
            return history;
        } catch (IOException e) {
            GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_READABLE,
                    "Could not read content of gist {}", gistId);
            logger.error(error.getFormattedMessage() + " with path {}", this.layout.getRootFolder(), e);
            throw new GistRepositoryError(error, e);
        }
    }

    @Override
    public GistHistory getCommit(String gistId, String commitId, User userDetails) {
        return null;
    }


    private Grgit openRepository() {
        OpenOp openOp = new OpenOp();
        openOp.setDir(this.layout.getBareFolder());
        Grgit git = openOp.call();
        return git;
    }
    private List<GistHistory> getHistory(Grgit git, RevCommit commit) {
        GitHistoryOperation historyOperation = new GitHistoryOperation(git, commit.getName());
        historyOperation.setHistoryCache(historyCache);
        return historyOperation.call();
    }
    private RevCommit resolveCommit(Repository repository) throws IOException {
        try (RevWalk revWalk = new RevWalk(repository)) {
            if(StringUtils.isEmpty(commitId)) {
                Ref head = repository.exactRef(REF_HEAD_MASTER);
                if(head != null) {
                    return revWalk.parseCommit(head.getObjectId());
                }
                return null;
            } else {
                return revWalk.parseCommit(ObjectId.fromString(this.commitId));
            }
        }
    }
}
