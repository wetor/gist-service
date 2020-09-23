package top.wetor.gist.repository.git;


import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wetor.gist.model.GistHistory;
import top.wetor.gist.model.User;
import top.wetor.gist.repository.GistCommitRepository;

import java.io.Serializable;
import java.util.List;

public class GitGistCommitRepository implements GistCommitRepository, Serializable {
    private static final long serialVersionUID = -6521001365366018269L;

    private static final Logger logger = LoggerFactory.getLogger(GitGistCommitRepository.class);

    private RepositoryLayout layout;

    private String gistId;

    public GitGistCommitRepository(RepositoryLayout layout, String gistId){
        this.layout = layout;
        this.gistId = gistId;
    }

    @Override
    public List<GistHistory> getCommits(String gistId, User userDetails) {
        return null;
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
}
