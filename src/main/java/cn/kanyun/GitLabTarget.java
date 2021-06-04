package cn.kanyun;

import cn.hutool.core.bean.BeanUtil;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 目标Gitlab
 */
public class GitLabTarget {

    /**
     * 目标gitlab域名
     */
    private static final String TARGET_GITLAB_DOMAIN = "";

    /**
     * 目标gitlab用户对应的token。需要从gitlab上设置获取
     */
    private static final String TARGET_GITLAB_TOKEN = "";

    private static Logger logger = LoggerFactory.getLogger(GitLabTarget.class);

    public static GitlabProject createProject(GitlabProject msProject) throws IOException {
//        此token需要从gitlab的设置中获取
        GitlabAPI api = GitlabAPI.connect(TARGET_GITLAB_DOMAIN, TARGET_GITLAB_TOKEN);
        List<GitlabNamespace> namespaces = api.getNamespaces();
        List<GitlabGroup> git_groups = api.getGroups();

        try {
            GitlabProject gitlabProject = new GitlabProject();
            BeanUtil.copyProperties(msProject, gitlabProject);
            gitlabProject.setNamespace(new GitlabNamespace());

            ArrayList<GitlabProjectSharedGroup> groups = new ArrayList<>();

            GitlabProject project = api.createProject(gitlabProject);
            String expire = "2025-10-31";
            api.shareProjectWithGroup(GitlabAccessLevel.Master, expire, git_groups.get(0), project);
            return project;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void removeAllProject() throws IOException {
        GitlabAPI api = GitlabAPI.connect(TARGET_GITLAB_DOMAIN, TARGET_GITLAB_TOKEN);
        List<GitlabProject> allProjects = api.getAllProjects();
        for (GitlabProject project : allProjects) {
            logger.info("删除项目：[{}]", project.getName());
            api.deleteProject(project.getId());
        }

    }

    /**
     * @param gitlabProject 仓库
     * @param branchName    新分支名
     * @param ref           引用哪个分支
     * @throws IOException
     */
    public static void createNewBranch(GitlabProject gitlabProject, String branchName, String ref) throws IOException {
        logger.info("使用GitLab 创建新分支");
        GitlabAPI api = GitlabAPI.connect(TARGET_GITLAB_DOMAIN, TARGET_GITLAB_TOKEN);
        api.createBranch(gitlabProject,
                branchName,
                ref);
    }

    public static void renameBranch(GitlabProject gitlabProject, String branchName, String ref) throws IOException {
        logger.info("使用GitLab 创建新分支");
        GitlabAPI api = GitlabAPI.connect(TARGET_GITLAB_DOMAIN, TARGET_GITLAB_TOKEN);

        api.createBranch(gitlabProject,
                branchName,
                ref);
    }

    public static boolean isExits(String projectName) {
        GitlabAPI api = GitlabAPI.connect(TARGET_GITLAB_DOMAIN, TARGET_GITLAB_TOKEN);
        for (GitlabProject allProject : api.getAllProjects()) {
            if (allProject.getName().equals(projectName)) {
                return true;
            }
        }
        return false;
    }
}
