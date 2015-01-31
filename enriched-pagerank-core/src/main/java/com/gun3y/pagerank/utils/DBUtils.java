package com.gun3y.pagerank.utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class DBUtils {

    public static Environment newEnvironment(String dbPath) {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        envConfig.setLocking(true);

        if (StringUtils.isBlank(dbPath)) {
            throw new IllegalArgumentException("DBPath is empty");
        }

        File envHome = new File(dbPath + "/frontier");
        if (!envHome.exists()) {
            if (!envHome.mkdirs()) {
                throw new RuntimeException("Couldn't create this folder: " + envHome.getAbsolutePath());
            }
        }

        return new Environment(envHome, envConfig);
    }

    public static boolean deleteFolderContents(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (!file.delete()) {
                    return false;
                }
            }
            else {
                if (!deleteFolder(file)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean deleteFolder(File folder) {
        return deleteFolderContents(folder) && folder.delete();
    }

    public static void main(String[] args) {
        newEnvironment("data2");
    }

}
