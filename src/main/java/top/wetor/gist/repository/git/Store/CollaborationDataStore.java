package top.wetor.gist.repository.git.Store;

import java.util.*;

public class CollaborationDataStore {

    private Map<String, List<String>> collaborators = new HashMap<>();

    public CollaborationDataStore() {

    }

    public CollaborationDataStore(Map<String, List<String>> collaborators) {
        this.collaborators = collaborators;
    }

    public Collection<String> getCollaborators(String user) {
        if (collaborators.containsKey(user) && collaborators.get(user) != null) {
            return new LinkedHashSet<>(collaborators.get(user));
        } else {
            return Collections.emptySet();
        }
    }
    
    public void updateCollaborators(Map<String, List<String>> collaborators) {
        this.collaborators.clear();
        for(Map.Entry<String, List<String>> entry: collaborators.entrySet()) {
            this.collaborators.put(entry.getKey(), entry.getValue());
        }
    }

}
