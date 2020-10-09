/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package top.wetor.gist.api;

import top.wetor.gist.model.*;
import top.wetor.gist.repository.GistRepositoryService;
import top.wetor.gist.repository.git.Store.CollaborationDataStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@CrossOrigin(origins = "http://0.0.0.0:8080")
@RestController()
@RequestMapping(value = "/gists", produces = { MediaType.APPLICATION_JSON_VALUE, "application/vnd.github.beta+json",
        "application/vnd.github.v3+json" })
public class GistRestController {

    private final Logger logger = LoggerFactory.getLogger(GistRestController.class);
    public static final String USER_ROLE_AUTHORITY = "hasRole('USER')";

    @Autowired
    private GistRepositoryService repository;

    @Autowired
    private ControllerUrlResolver resolver;

    @Autowired
    private CollaborationDataStore collaborationDataStore;

    /**
     * 通过user获取gist列表
     * @param activeUser user
     * @return gist列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<GistResponse> listAllGistsForUser(User activeUser) {
        List<GistResponse> responses = repository.listGists(activeUser);
        decorateGistResponse(responses, activeUser);
        return responses;
    }

    /**
     * 获取user的公开gist
     * @param activeUser user
     * @return gist列表
     */
    @RequestMapping(value = "/public", method = RequestMethod.GET)
    public List<GistResponse> listAllPublicGists(User activeUser) {
        List<GistResponse> responses = repository.listPublicGists(activeUser);
        decorateGistResponse(responses, activeUser);
        return responses;
    }

    /**
     * 通过gistId获取gist
     * @param gistId gistId
     * @param activeUser user
     * @return gist
     */
    @RequestMapping(value = "/{gistId}", method = RequestMethod.GET)
    @Cacheable(value = "gists", key = "#gistId")
    public GistResponse getGist(@PathVariable("gistId") String gistId, User activeUser) {
        GistResponse response = repository.getGist(gistId, activeUser);
        decorateGistResponse(response, activeUser);
        return response;
    }

    /**
     * 获取gits的指定commit
     * @param gistId gistId
     * @param commitId commitId
     * @param activeUser user
     * @return commit
     */
    @RequestMapping(value = "/{gistId}/{commitId}", method = RequestMethod.GET)
    @Cacheable(value = "gists", key = "{ #gistId, #commitId }")
    public GistResponse getGistAtCommit(@PathVariable("gistId") String gistId,
                                        @PathVariable("commitId") String commitId, User activeUser) {

        GistResponse response = repository.getGist(gistId, commitId, activeUser);
        decorateGistResponse(response, activeUser);
        return response;
    }


    /**
     * 获取gist的commit列表
     * @param gistId gistId
     * @param activeUser user
     * @return commit列表
     */
    @RequestMapping(value = "/{gistId}/commits", method = RequestMethod.GET)
    @Cacheable(value = "commits", key = "{ #gistId}")
    public List<GistHistory> getCommits(@PathVariable("gistId") String gistId, User activeUser){

        return repository.getCommits(gistId, activeUser);
    }
    /**
     * 获取两个commit的diff
     * @param gistId gistId
     * @param newCommitId commit1
     * @param oldCommitId commit2
     * @return diff
     */
    @RequestMapping(value = "/{gistId}/diff", method = RequestMethod.GET)
    @Cacheable(value = "commit", key = "{ #gistId, #newCommitId, #oldCommitId}")
    public GistDiff getCommitDiff(@PathVariable("gistId") String gistId,String newCommitId,String oldCommitId){
        return repository.getDiff(gistId,newCommitId,oldCommitId);
    }
    /**
     * 获取当前commit的内容
     * @param gistId gistId
     * @param commitId commitId
     * @return diff
     */
    @RequestMapping(value = "/{gistId}/commit/{commitId}", method = RequestMethod.GET)
    @Cacheable(value = "commit", key = "{ #gistId, #commitId}")
    public GistDiff getCommitDiff(@PathVariable("gistId") String gistId,@PathVariable("commitId") String commitId){
        return repository.getDiff(gistId, commitId, null);
    }
    /**
     * 需要登录。创建gist
     * @param request gist数据
     * @param httpRequest request
     * @param activeUser user
     * @return gist
     */
    @RequestMapping(method = RequestMethod.POST)
    //@PreAuthorize(USER_ROLE_AUTHORITY)
    @ResponseStatus(HttpStatus.CREATED)
    public GistResponse createGist(@RequestBody GistRequest request, HttpServletRequest httpRequest, User activeUser) {
        GistResponse response = repository.createGist(request, activeUser);
        decorateGistResponse(response, activeUser);
        return response;
    }

    /**
     * 获取fork列表
     * @param gistId gistId
     * @param activeUser user
     * @return fork列表
     */
    @RequestMapping(value = "/{gistId}/forks", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Fork> getForks(@PathVariable("gistId") String gistId, User activeUser) {
        List<Fork> forks = repository.getForks(gistId, activeUser);
        decorateForksResponse(forks, activeUser);
        return forks;
    }

    /*
     * Legacy github mapping
     */

    /**
     * 映射github 获取fork列表
     * @param gistId gistId
     * @param activeUser user
     * @return fork列表
     */
    @RequestMapping(value = "/{gistId}/fork", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Deprecated
    public List<Fork> legacyGetForks(@PathVariable("gistId") String gistId, User activeUser) {
        return this.getForks(gistId, activeUser);
    }

    /**
     * 需要登录。fork指定gist
     * @param gistId gistId
     * @param activeUser user
     * @return fork后的gist
     */
    @RequestMapping(value = "/{gistId}/forks", method = RequestMethod.POST)
    //@PreAuthorize(USER_ROLE_AUTHORITY)
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(cacheNames = "gists", key = "#gistId")
    public ResponseEntity<GistResponse> forkGist(@PathVariable("gistId") String gistId, User activeUser) {
        // TODO need to add Location header to response for the new Gist
        GistResponse response = repository.forkGist(gistId, activeUser);
        String location = resolver.getGistUrl(response.getId(), activeUser);
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setLocation(new URI(location));
        } catch (URISyntaxException e) {
            logger.warn("Unable to set the location header with value {} for fork with id {} with error {}.", location,
                    gistId, e.getMessage());
        }
        decorateGistResponse(response, activeUser);
        ResponseEntity<GistResponse> responseEntity = new ResponseEntity<>(response, headers, HttpStatus.CREATED);

        return responseEntity;
    }

    /*
     * Legacy github mapping
     */

    /**
     * 映射github 需要登录。fork指定gist
     * @param gistId gistId
     * @param activeUser user
     * @return fork后的gist
     */
    @RequestMapping(value = "/{gistId}/fork", method = RequestMethod.POST)
    //@PreAuthorize(USER_ROLE_AUTHORITY)
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(cacheNames = "gists", key = "#gistId")
    @Deprecated
    public ResponseEntity<GistResponse> legacyForkGist(@PathVariable("gistId") String gistId, User activeUser) {
        return this.forkGist(gistId, activeUser);
    }

    /**
     * 需要登录。修改gist
     * @param gistId gistId
     * @param request 修改内容
     * @param activeUser user
     * @return 修改后的gist
     */
    @RequestMapping(value = "/{gistId}", method = RequestMethod.PATCH)
    //@PreAuthorize(USER_ROLE_AUTHORITY)
    @CachePut(cacheNames = "gists", key = "#gistId")
    public GistResponse editGist(@PathVariable("gistId") String gistId, @RequestBody GistRequest request, User activeUser) {
        GistResponse response = repository.editGist(gistId, request, activeUser);
        decorateGistResponse(response, activeUser);
        return response;
    }

    /**
     * 需要登录。删除gist
     * @param gistId gistId
     * @param activeUser user
     */
    @RequestMapping(value = "/{gistId}", method = RequestMethod.DELETE)
    //@PreAuthorize(USER_ROLE_AUTHORITY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(cacheNames = "gists", key = "#gistId")
    public void deleteGist(@PathVariable("gistId") String gistId, User activeUser) {
        repository.deleteGist(gistId, activeUser);
    }

    /**
     * 处理多个gistResponse，添加url、comment、fork等信息
     * @param gistResponses gist列表
     * @param activeUser user
     */
    private void decorateGistResponse(Collection<GistResponse> gistResponses, User activeUser) {
        if (gistResponses != null) {
            for (GistResponse gistResponse : gistResponses) {
                this.decorateGistResponse(gistResponse, activeUser);
            }
        }
    }

    /**
     * 处理单个gistResponse，添加url、comment、fork等信息
     * @param gistResponse gist
     * @param activeUser user
     */
    private void decorateGistResponse(GistResponse gistResponse, User activeUser) {
        if (gistResponse != null) {
            gistResponse.setUrl(resolver.getGistUrl(gistResponse.getId(), activeUser));
            gistResponse.setCommentsUrl(resolver.getCommentsUrl(gistResponse.getId(), activeUser));
            gistResponse.setForksUrl(resolver.getForksUrl(gistResponse.getId(), activeUser));
            gistResponse.setCommitsUrl(resolver.getCommitsUrl(gistResponse.getId(), activeUser));
            if (gistResponse.getForkOf() != null) {
                Fork forkOf = gistResponse.getForkOf();
                String url = resolver.getGistUrl(forkOf.getId(), activeUser);
                forkOf.setUrl(url);
            }
            this.decorateCollaborators(gistResponse);
        }
    }

    /**
     * 再次处理gistResponse，添加所有者以及登录信息
     * @param gistResponse gist
     */
    private void decorateCollaborators(GistResponse gistResponse) {
        if (gistResponse.getOwner() != null && StringUtils.isNotBlank(gistResponse.getOwner().getLogin())) {
            Collection<String> collaboratorNames = collaborationDataStore
                    .getCollaborators(gistResponse.getOwner().getLogin());
            Collection<GistIdentity> collaboratorIdentities = new LinkedList<>();
            for (String collaboratorName : collaboratorNames) {
                GistIdentity collaboratorIdentity = new GistIdentity();
                collaboratorIdentity.setLogin(collaboratorName);
                collaboratorIdentities.add(collaboratorIdentity);
            }
            gistResponse.setCollaborators(collaboratorIdentities);
        }
    }

    /**
     * 处理fork列表，添加url等信息
     * @param forks fork列表
     * @param activeUser user
     */
    private void decorateForksResponse(List<Fork> forks, User activeUser) {
        for (Fork fork : forks) {
            String forkUrl = resolver.getGistUrl(fork.getId(), activeUser);
            fork.setUrl(forkUrl);
        }
    }

}
