package top.wetor.gist.repository;

import top.wetor.gist.model.GistHistory;
import top.wetor.gist.model.User;

import java.util.List;

public interface GistCommitRepository {

    List<GistHistory> getCommits(String gistId, User userDetails);

    GistHistory getCommit(String gistId, String commitId, User userDetails);


}
