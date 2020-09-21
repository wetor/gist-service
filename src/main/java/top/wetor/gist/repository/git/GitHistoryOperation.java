/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.repository.git;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.wetor.gist.model.GistHistory;
import top.wetor.gist.model.GistIdentity;
import top.wetor.gist.model.GitChangeStatus;
import org.ajoberstar.grgit.*;
import org.ajoberstar.grgit.operation.LogOp;
import org.ajoberstar.grgit.operation.ShowOp;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import top.wetor.gist.repository.GistDiff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Creates the history for a gist git repository.
 *
 * history contains:
 *
 * <pre>

  {
      "url": "https://api.github.com/gists/aa5a315d61ae9438b18d/57a7f021a713b1c5a6a199b54cc514735d2d462f",
      "version": "57a7f021a713b1c5a6a199b54cc514735d2d462f",
      "user": {
        "login": "octocat",
      },
      "change_status": {
        "deletions": 0,
        "additions": 180,
        "total": 180
      },
      "committed_at": "2010-04-14T02:15:15Z"
    }
 *
 * </pre>
 *
 */
public class GitHistoryOperation implements Callable<List<GistHistory>> {

	private static final Logger logger = LogManager.getLogger(GitHistoryOperation.class);

	private Repository repository;

	private String commitId;
	
	private HistoryCache historyStore = new HistoryCache() {

		@Override
		public List<GistHistory> load(String commitId) {
			return new LinkedList<>();
		}

		@Override
		public List<GistHistory> save(String commitId, List<GistHistory> history) {
			return history;
		}
		
	};

	public GitHistoryOperation(Grgit git, String commitId) {
		this.repository = git.getRepository();
		this.commitId = commitId;
	}
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public List<GistHistory> call() {
		LogOp logOp = new LogOp(repository);
		List<Commit> commits = logOp.call();
		return calculateHistory(repository, commits);
	}

	private List<GistHistory> calculateHistory(Repository repository, List<Commit> commits) {
		List<GistHistory> histories = new ArrayList<>();
		boolean recordHistory = commitId == null;
		Commit lastCommit = null;
		for (Commit logCommit : commits) {
			if(this.commitId == null) {
				this.commitId = logCommit.getId();
			}
			try {
				if(commitId != null && logCommit.getId().equals(commitId)) {
					recordHistory = true;
				}
				if(recordHistory) {
					List<GistHistory> cachedHistory = historyStore.load(logCommit.getId());
					if(!cachedHistory.isEmpty()) {
						histories.addAll(cachedHistory);
						break;
					}
					if(lastCommit!=null)
					{
						Test(repository,logCommit,lastCommit);
					}
					GistHistory history = create(repository, logCommit);
					histories.add(history);
					
				}
			} catch (GitAPIException | IOException e) {
				logger.error(String.format("Could not extract diff of commit %s.", logCommit.getId()), e);
			}
			lastCommit = logCommit;
		}
		this.historyStore.save(this.commitId, histories);
		return histories;
	}

	private void Test(Repository repository, Commit oldCommit,Commit newCommit) {
		System.out.println(GistDiff.diffCommit(repository.getJgit().getRepository(),oldCommit.getId(),newCommit.getId()));
	}

	private GistHistory create(Repository repository, Commit logCommit) throws GitAPIException, IOException {
		ShowOp showOp = new ShowOp(repository);
		showOp.setCommit(logCommit);
		CommitDiff diff = showOp.call();
		GistHistory history = new GistHistory();
		setVersion(history, logCommit);
		setUsername(history, logCommit);
		setCommitDate(history, logCommit);
		setChanges(history, diff);
		return history;
	}


	// TODO: diff
	private void setChanges(GistHistory history, CommitDiff diff) {
		GitChangeStatus status = new GitChangeStatus();
		status.setAdditions(diff.getAdded().size());
		status.setDeletions(diff.getRemoved().size());
		status.setTotal(diff.getAllChanges().size());
		history.setChangeStatus(status);
	}

	private void setCommitDate(GistHistory history, Commit commit) {
		long timeInSeconds = commit.getTime();
		DateTime dateTime = new DateTime(timeInSeconds * 1000, DateTimeZone.UTC);
		history.setCommittedAt(dateTime);
	}

	private void setUsername(GistHistory history, Commit commit) {
		Person author = commit.getAuthor();
		GistIdentity user = new GistIdentity();
		user.setLogin(author.getName());
		history.setUser(user);
	}

	private void setVersion(GistHistory history, Commit logCommit) {
		history.setVersion(logCommit.getId());
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public void setHistoryCache(HistoryCache historyStore) {
		this.historyStore = historyStore;
	}


}
