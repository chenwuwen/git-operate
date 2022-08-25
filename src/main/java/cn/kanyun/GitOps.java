package cn.kanyun;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Git操作工具类
 */
public class GitOps {

    private static Logger logger = LoggerFactory.getLogger(GitOps.class);

    /**
     * 创建Git认证
     * @param userName
     * @param password
     * @return
     */
    public static CredentialsProvider createCredential(String userName, String password) {
        return new UsernamePasswordCredentialsProvider(userName, password);
    }

    /**
     * 从仓库中克隆代码。克隆的代码是gitlab默认的分支代码
     * @param repoUrl
     * @param cloneDir
     * @param provider
     * @return
     * @throws GitAPIException
     * @throws GitAPIException
     */
    public static Git fromCloneRepository(String repoUrl, String cloneDir, CredentialsProvider provider) throws GitAPIException, GitAPIException {
        File file = new File(cloneDir);
        if (file.exists()) {
            try {
                Git git = Git.open(file);
                ListBranchCommand listBranchCommand = git.branchList();
                logger.info(listBranchCommand.getRepository().getBranch());
                git.pull();
                return git;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Git git = Git.cloneRepository()
                .setCredentialsProvider(provider)
                .setURI(repoUrl)
                .setDirectory(new File(cloneDir)).call();

        return git;
    }

    /**
     * 提交代码
     * @param git
     * @param message
     * @param provider
     * @throws GitAPIException
     */
    public static void commit(Git git, String message, CredentialsProvider provider) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit()
                .setMessage(message)
                .call();
    }


    /**
     * 创建新分支
     * @param git
     * @param branchName
     * @throws GitAPIException
     */
    public static void newBranch(Git git, String branchName) throws GitAPIException {

        ListBranchCommand listBranchCommand = git.branchList();
        List<Ref> branches = listBranchCommand.call();
        boolean flag = false;
        for (Ref branch : branches) {
            if (branch.getName().endsWith(branchName)) {
                logger.info("{} 分支已存在,无需重建", branchName);
                flag = true;
                break;
            }
        }
        if (!flag) {
            CreateBranchCommand createBranchCommand = git.branchCreate();
            createBranchCommand.setName(branchName);
            createBranchCommand.call();
            logger.info("{}创建新分支[{}]", git.getRepository(), branchName);
        }
        git.checkout().setName(branchName).call();
    }

    public static void push(Git git, CredentialsProvider provider) throws GitAPIException, IOException {
        logger.info("{} pushing......", git.getRepository());
        push(git, null, provider);
    }

    /**
     * 推送代码
     * @param git
     * @param branch
     * @param provider
     * @throws GitAPIException
     * @throws IOException
     * @throws IOException
     */
    public static void push(Git git, String branch, CredentialsProvider provider) throws GitAPIException, IOException, IOException {
        if (branch == null) {
            branch = git.getRepository().getBranch();
        }
        git.push()
                .setPushAll() // push所有分支？
                .setCredentialsProvider(provider)
                .setRemote("origin").setRefSpecs(new RefSpec(branch)).call();
    }

    /**
     * 更改远程仓库地址
     *
     * @param git
     * @param url 目标url
     * @throws GitAPIException
     * @throws IOException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void updateRemoteUrl(Git git, String url) throws GitAPIException, IOException, IOException, URISyntaxException {

        RemoteSetUrlCommand remoteSetUrlCommand = git.remoteSetUrl();
        remoteSetUrlCommand.setRemoteUri(new URIish(url));
        remoteSetUrlCommand.setRemoteName("origin");
        remoteSetUrlCommand.setUriType(RemoteSetUrlCommand.UriType.PUSH);
        remoteSetUrlCommand.call();
        remoteSetUrlCommand.setUriType(RemoteSetUrlCommand.UriType.FETCH);
        remoteSetUrlCommand.call();
        logger.info("{}更改remote url 为[{}]", git.getRepository(), url);
    }


    /**
     * git 迁移,虽然是git clone命令,但是下载的文件,并不是你能直接使用文件,而是需要push到新仓库才能用
     * https://blog.csdn.net/kanyun123/article/details/116749871
     * @param remoteRepoPath
     * @param localRepoPath
     * @param userName
     * @param passWord
     */
    public static void gitTransfer(String remoteRepoPath, String localRepoPath, String userName, String passWord) {

        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider
                = new UsernamePasswordCredentialsProvider(userName, passWord);

        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();
        Git git = null;
        try {
            git = cloneCommand.setURI(remoteRepoPath) //设置远程URI
                    .setBare(true)  //具体可以查询 git clone --bare
                    .setCredentialsProvider(usernamePasswordCredentialsProvider)
                    .setDirectory(new File(localRepoPath)) //设置下载存放路径
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

}
