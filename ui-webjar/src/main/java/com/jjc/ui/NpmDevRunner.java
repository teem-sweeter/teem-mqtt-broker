package com.jjc.ui;

/**
 * @author sweeter
 * @date 2025/12/10
 * @description
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class NpmDevRunner {

    public static void main(String[] args) {
        // 从当前工作目录开始查找 package.json
        File projectRoot = findProjectRoot(new File(System.getProperty("user.dir")+"/ui-webjar/"));

        if (projectRoot == null) {
            System.err.println("找不到包含 package.json 的目录");
            return;
        }

        System.out.println("项目根目录: " + projectRoot.getAbsolutePath());

        // 构建命令
        ProcessBuilder processBuilder;
        if (isWindows()) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", "npm", "run", "dev");
        } else {
            processBuilder = new ProcessBuilder("/bin/bash", "-c", "npm run dev");
        }
        processBuilder.directory(projectRoot);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            // 读取输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("\n命令结束，退出码: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 向上查找包含 package.json 的目录
    private static File findProjectRoot(File dir) {
        File packageJson = new File(dir, "package.json");
        if (packageJson.exists() && packageJson.isFile()) {
            return dir;
        }

        File parentDir = dir.getParentFile();
        if (parentDir == null || !parentDir.isDirectory()) {
            return null;
        }

        return findProjectRoot(parentDir);
    }

    // 判断操作系统是否为 Windows
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
}
