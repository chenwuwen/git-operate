package cn.kanyun;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 源Gitlab
 */
public class GitLabSource {

    /**
     * 源gitlab域名
     */
    private static final String SOURCE_GITLAB_DOMAIN = "";

    /**
     * 源gitlab用户对应的token。需要从gitlab上设置获取
     */
    private static final String SOURCE_GITLAB_TOKEN = "";

    private static Logger logger = LoggerFactory.getLogger(GitLabSource.class);

    /**
     * 获取源Gitlab的仓库列表,注意返回的总项目数量
     * @return
     */
    public static List<GitlabProject> getProject() {
        
//         此变量最终未使用
        ArrayList<Map<String, String>> pros = new ArrayList<>();
        GitlabAPI api = GitlabAPI.connect(SOURCE_GITLAB_DOMAIN, SOURCE_GITLAB_TOKEN);
        List<GitlabProject> allProjects = api.getAllProjects();
        logger.info("仓库数量：{}", allProjects.size());
        for (GitlabProject project : allProjects) {
//           得到项目的 NameSpace
            String nameSpace = project.getNamespace().getPath();
            logger.info("{} -> 所属空间 {}",project.getName(),nameSpace);
            HashMap<String, String> result = new HashMap<String, String>();
//            浏览器地址
            logger.info(project.getWebUrl());
            logger.info(project.getHttpUrl());
            logger.info(project.getNameWithNamespace());
            logger.info(String.valueOf(project.getNamespace()));
            logger.info(project.getName());
            result.put("desc", project.getDescription());
            result.put("name", project.getName());
            result.put("url", project.getHttpUrl());
            result.put("path", project.getPath());
            pros.add(result);
        }
        System.out.println(pros);
        return allProjects;
    }
}
