package cn.kanyun;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.gitlab.api.models.GitlabProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * 代码下载目录
     */
    private static final String RES_DIR = "E:\\code\\";

    /**
     * 源Git用户名
     */
    private static final String SOURCE_GIT_USERNAME = "";
    /**
     * 源Git密码
     */
    private static final String SOURCE_GIT_PASSWORD = "";
    /**
     * 目标Git用户名
     */
    private static final String TARGET_GIT_USERNAME = "";

    /**
     * 目标Git密码
     */
    private static final String TARGET_GIT_PASSWORD = "";

    public static void main(String[] args) throws IOException, InterruptedException {

//        从源Gitlab获取仓库列表(注意默认获取的列表是未过滤的列表)
        List<GitlabProject> projects = GitLabSource.getProject();
//        源Git认证
        CredentialsProvider sourceCredential = GitOps.createCredential(SOURCE_GIT_USERNAME, SOURCE_GIT_PASSWORD);
//        目标Git认证
        CredentialsProvider targetCredential = GitOps.createCredential(TARGET_GIT_USERNAME, TARGET_GIT_PASSWORD);

        for (GitlabProject project : projects) {

//            判断目标Gitlab是否存在此仓库
            if (GitLabTarget.isExits(project.getName())) {
                logger.info("项目[{}]已存在不用重复导入", project.getName());
                continue;
            }

//            在目标Gitlab上创建一个仓库(此时是空仓库)
            GitlabProject targetProject = GitLabTarget.createProject(project);

            try {
//                下载源Git仓库代码
                Git git = GitOps.fromCloneRepository(project.getHttpUrl(), RES_DIR + project.getName(), sourceCredential);
//                更改仓库URL
                GitOps.updateRemoteUrl(git, targetProject.getHttpUrl());
//                将分支切换到master分支(注意github无此分支)
                git.checkout().setName("master").call();
//                将代码上传到目标Gitlab
                GitOps.push(git, targetCredential);

//                创建新分支再提交
//                GitOps.newBranch(git, "xxxx");
//                GitOps.commit(git, "create branch:xxxx", targetCredential);
//                GitOps.push(git, targetCredential);
//                创建新分支,参数1:项目 参数2:新分支名 参数3:引用哪个分支

//                或者直接操作Gitlab来创建新分支
                GitLabTarget.createNewBranch(targetProject, "xxx", "master");

            } catch (GitAPIException | URISyntaxException | IOException e) {
                logger.info("git clone or update fail");
                e.printStackTrace();
            }
        }

        logger.info("导入完成");

    }

}
