package com.bing.utils;

import java.io.File;
import java.io.IOException;
/**
 * 文件和目录操作工具类
 * 提供目录创建、文件创建、文件删除、目录删除、存在性检查等通用文件操作功能
 * 所有方法均为静态方法，无需实例化即可使用
 *
 * @author shizhongtao
 */
public class FileCreateUtils {
    /**
     * 创建目录路径
     * 如果路径中的父目录不存在，会递归创建所有不存在的父目录
     * 如果目录已存在，则直接返回该目录对象
     *
     * @param path 要创建的目录路径
     * @return 创建好的目录File对象
     */
    public static File createFolderPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 在指定父目录下创建子目录
     * 父目录不存在时会先自动创建父目录
     *
     * @param parent 父目录路径
     * @param childName 子目录名称
     * @return 创建好的子目录File对象
     */
    public static File createchildFolder(String parent, String childName) {
        File file = new File(createFolderPath(parent), childName);
        file.mkdir();
        return file;
    }

    /**
     * 在指定父目录File对象下创建子目录
     * 父目录不存在时会先自动创建父目录
     *
     * @param parent 父目录File对象
     * @param childName 子目录名称
     * @return 创建好的子目录File对象
     */
    public static File createchildFolder(File parent, String childName) {
        File file = new File(parent, childName);
        file.mkdirs();
        return file;
    }

    /**
     * 根据完整路径创建文件
     * 自动创建文件所在的父目录（如果不存在）
     * 如果路径没有父目录（如相对路径只有文件名），则直接在当前目录创建
     *
     * @param path 要创建的文件完整路径
     * @return 创建好的文件File对象，如果创建失败返回null
     */
    public static File createFile(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        String fileName = file.getName();
        if (parent == null) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // 静默处理异常，返回文件对象
            }
            return file;
        }
        return createFile(parent, fileName);
    }

    /**
     * 检查指定路径是否存在
     * 可以是文件或目录
     *
     * @param path 要检查的路径
     * @return 存在返回true，不存在返回false
     */
    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 删除指定路径的文件
     * 如果路径是目录或不存在，则删除失败
     *
     * @param fullPath 要删除的文件完整路径
     * @return 删除成功返回true，失败返回false
     */
    public static boolean deleteFile(String fullPath) {
        File file = new File(fullPath);
        if (file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 在指定父目录下创建文件
     * 父目录不存在时会先自动创建父目录
     *
     * @param parent 父目录路径
     * @param fileName 要创建的文件名
     * @return 创建好的文件File对象，如果创建失败返回null
     */
    public static File createFile(String parent, String fileName) {
        File file = new File(parent);
        return createFile(file, fileName);
    }

    /**
     * 在指定父目录File对象下创建文件
     * 父目录不存在时会先自动创建父目录
     *
     * @param parent 父目录File对象
     * @param fileName 要创建的文件名
     * @return 创建好的文件File对象，如果创建失败返回null
     */
    public static File createFile(File parent, String fileName) {
        File file = new File(parent, fileName);
        try {
            if (!parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    /**
     * 递归删除目录及其所有子内容
     * 先删除目录下的所有文件和子目录，再删除目录本身
     * 如果传入的是文件而不是目录，则直接删除该文件
     *
     * @param dir 要删除的目录File对象
     * @return 删除成功返回true，失败返回false
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children == null) {
                return false;
            }
            // 递归删除目录中的所有子文件和子目录
            for (File child : children) {
                boolean success = deleteDir(child);
                if (!success) {
                    return false;
                }
            }
        }
        // 删除空目录或文件
        return dir.delete();
    }
}