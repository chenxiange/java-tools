import org.apache.commons.net.ftp.*;

import java.io.*;
import java.net.SocketException;

public class FTPManager {

    public static final String FTP_SERVER = "127.0.0.1";
    public static final int FTP_PORT = 2121;
    public static final String FTP_USER = "admin";
    public static final String FTP_PASSWORD = "admin";

    public static void main(String[] args) {

//        FTPManager.uploadFile("/Users/admin/Downloads/image.png", "/test", "789.png");
//        FTPManager.renameFile("/test/789.png", "/123.png");
        FTPManager.renameAllFiles("/test");

    }

    /**
     * 上传本地文件到 FTP 服务器
     * @param localFilePath 本地文件绝对路径
     * @param remoteDir FTP 服务器目的目录
     * @param fileName 存在到 FTP 服务器到文件名
     */
    public static void uploadFile(String localFilePath, String remoteDir, String fileName) {
        // 初始化 FTPClient 对象
        FTPClient ftpClient = new FTPClient();
        try {
            // 连接到 FTP 服务器，如果端口是默认（21）的，可以去掉此参数，或者写默认端口
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            // 用户登录
            ftpClient.login(FTP_USER, FTP_PASSWORD);

            String msg = "连接到 FTP 服务器 (%s:%s) 开始";
            System.out.println(String.format(msg, FTP_SERVER, FTP_PORT));

            //a. 主动模式传送数据时是“服务器”连接到“客户端”的端口；被动模式传送数据是“客户端”连接到“服务器”的端口。
            //b. 主动模式需要客户端必须开放端口给服务器，很多客户端都是在防火墙内，开放端口给FTP服务器访问比较困难；被动模式只需要服务器端开放端口给客户端连接就行了。
            ftpClient.enterLocalPassiveMode();   // 进入被动模式
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 需要指定文件传输类型，否则默认是ASCII类型，会导致二进制文件传输损坏

            msg = "连接到 FTP 服务器 (%s:%s) 成功, 返回信息：%s";
            System.out.println(String.format(msg, FTP_SERVER, FTP_PORT, ftpClient.getReplyString()));

            // 切换工作目录
            ftpClient.changeWorkingDirectory(remoteDir);

            // 创建文件对象
            File file = new File(localFilePath);

            // 创建文件输入流，这里用 try 就不用手动关闭文件输入流了
            try (InputStream inputStream = new FileInputStream(file)) {
                // 保存文件保存到 FTP 服务器上
                ftpClient.storeFile(fileName, inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 退出登录
            ftpClient.logout();

            msg = "上传文件到服务器成功, 文件目录：%s/%s";
            System.out.println(String.format(msg, remoteDir, fileName));

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭 FTP 连接
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 重命名 FTP 服务器上文件 (其实包含移动文件和重命名文件两个功能），如果目录不同，则相当于移动文件
     * @param fromFileName 目标文件绝对路径 ex：/test/123.png
     * @param toFileName 目的文件绝对路径 ex: /456.png
     */
    public static void renameFile(String fromFileName, String toFileName) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(FTP_USER, FTP_PASSWORD);

            String msg = "连接到 FTP 服务器 (%s:%s) 开始";
            System.out.println(String.format(msg, FTP_SERVER, FTP_PORT));

            //a. 主动模式传送数据时是“服务器”连接到“客户端”的端口；被动模式传送数据是“客户端”连接到“服务器”的端口。
            //b. 主动模式需要客户端必须开放端口给服务器，很多客户端都是在防火墙内，开放端口给FTP服务器访问比较困难；被动模式只需要服务器端开放端口给客户端连接就行了。
            ftpClient.enterLocalPassiveMode();   // 进入被动模式
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 需要指定文件传输类型，否则默认是ASCII类型，会导致二进制文件传输损坏

            msg = "连接到 FTP 服务器 (%s:%s) 成功, 返回信息：%s";
            System.out.println(String.format(msg, FTP_SERVER, FTP_PORT, ftpClient.getReplyString()));

            // 重命名文件
            boolean ok = ftpClient.rename(fromFileName, toFileName);
            msg = "重命名 %s 到 %s %s";
            if(ok) {
                System.out.println(String.format(msg, fromFileName, toFileName, "成功"));
            } else {
                System.out.println(String.format(msg, fromFileName, toFileName, "失败"));
            }

            ftpClient.logout();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 重命名一个目录下到所有文件
     * @param parentDir 文件目录
     * EX：去掉文件名后的下划线(_)
     * 文件 /test/README.txt 不符合规则，跳过
     * 重命名 /test/img01_3.png 到 /test/img01.png 成功
     * 重命名 /test/img02_3.png 到 /test/img02.png 成功
     * 文件 /test/img03.png 不符合规则，跳过
     */
    public static void renameAllFiles(String parentDir) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(FTP_USER, FTP_PASSWORD);

            String msg = "连接到 FTP 服务器 (%s:%s) 开始";
            System.out.println(String.format(msg, FTP_SERVER, FTP_PORT));

            //a. 主动模式传送数据时是“服务器”连接到“客户端”的端口；被动模式传送数据是“客户端”连接到“服务器”的端口。
            //b. 主动模式需要客户端必须开放端口给服务器，很多客户端都是在防火墙内，开放端口给FTP服务器访问比较困难；被动模式只需要服务器端开放端口给客户端连接就行了。
            ftpClient.enterLocalPassiveMode();   // 进入被动模式
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 需要指定文件传输类型，否则默认是ASCII类型，会导致二进制文件传输损坏

            msg = "连接到 FTP 服务器 (%s:%s) 成功, 返回信息：%s";
            System.out.println(String.format(msg, FTP_SERVER, FTP_PORT, ftpClient.getReplyString()));

            // 不同到主要是这个地方，这里是获取目录下到所有文件，但是不会递归获取，即如果是文件夹，则不会再获取文件夹下的文件。
            FTPFile[] ftpFiles = ftpClient.listFiles(parentDir);
            // 遍历文件（夹）
            for (FTPFile ftpFile: ftpFiles) {
                // 这里只处理文件，不处理文件夹
                if(ftpFile.isFile()) {
                    // 这里只是文件名，不包含路径
                    String fileName = ftpFile.getName();
                    String suffix = "";
                    // 获取文件后缀符号的文职
                    int dotIdx = fileName.lastIndexOf(".");
                    if(dotIdx != -1) { //可能没有扩展名
                        // 文件的扩展名
                        suffix = fileName.substring(dotIdx);
                    }
                    // 源文件的绝对路径
                    String originFile = parentDir + "/" + fileName;
                    // 文件名称, 到这里就不包含扩展名了，跟 fileName 就差扩展名
                    String name = fileName.substring(0, dotIdx);
                    // 要处理到分隔符的索引（这里是要把 a_1 的 _ 后（包含）的字符全部去掉；如果是其他字符则修改这里。
                    int splitIdx = name.lastIndexOf("_");
                    if(splitIdx == -1) { // 等于 -1 则表示文件名没有此字符
                        msg = "文件 %s 不符合规则，跳过";
                        System.out.println(String.format(msg, originFile));
                    } else {
                        // 处理后的名字
                        name = name.substring(0, splitIdx);
                        // 组装目标文件的绝对路径
                        String targetFile = parentDir + "/" + name + suffix;
                        // 重命名
                        boolean ok = ftpClient.rename(originFile, targetFile);
                        msg = "重命名 %s 到 %s %s";
                        if(ok) {
                            System.out.println(String.format(msg, originFile, targetFile, "成功"));
                        } else {
                            System.out.println(String.format(msg, originFile, targetFile, "失败"));
                        }
                    }
                }
            }
            ftpClient.logout();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
