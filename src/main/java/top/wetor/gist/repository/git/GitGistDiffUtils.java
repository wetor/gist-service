package top.wetor.gist.repository.git;

import org.ajoberstar.grgit.CommitDiff;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;

public class GitGistDiffUtils {

    public static String diffCommit(Repository repository,String oldCommitId,String newCommitId) {
        try {
            Git gitCommand = new Git(repository);
            List<DiffEntry> diffEntries = listDiff(repository, gitCommand, oldCommitId, newCommitId);
            StringBuilder sb = new StringBuilder();
            for (DiffEntry entry : diffEntries) {
                sb.append(entry.getChangeType().toString())
                        .append(" : ")
                        .append(
                                entry.getOldPath().equals(entry.getNewPath()) ? entry.getNewPath() : entry.getOldPath()
                                        + " -> " + entry.getNewPath()
                        );
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                try (DiffFormatter formatter = new DiffFormatter(output)) {
                    formatter.setRepository(repository);
                    formatter.format(entry);
                }
                sb.append("\n").append(output.toString("UTF-8"));
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (GitAPIException e) {
            e.printStackTrace();
            return "";
        }
    }
    private static List<DiffEntry> listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
        return diffs;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
}
