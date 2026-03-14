import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class SystemMethods {

    public static File findDirectoryByName(File root, String targetName){
        if (root == null || !root.exists()) {
            return null;
        }

        // Если текущая директория совпадает с именем цели, возвращаем её
        if (root.isDirectory() && root.getName().equals(targetName)) {
            return root;
        }

        // Рекурсивно проверяем все поддиректории
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    File result = findDirectoryByName(file, targetName);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    public static void updateMainMenuState(JMenuItem copyFile, JMenuItem pasteFile, JMenuItem delete, JMenuItem copypasteFile, JMenuItem createItem)
    {
        TreePath selectedPath = FileManager.fileTree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        String nodeName = node.getUserObject().toString();
        File selectedFile = new File(SystemMethods.getPathFromNode(node));
        if (selectedPath == null || nodeName.equals("System")){
            copyFile.setEnabled(false);
            pasteFile.setEnabled(false);
            delete.setEnabled(false);
            copypasteFile.setEnabled(false);
            createItem.setEnabled(false);

            return;
        }
        if (selectedFile.isDirectory()){
            if(nodeName.equals("Корзина") || nodeName.equals("Изображения") || nodeName.equals("Документы")){
                copyFile.setEnabled(false);
                pasteFile.setEnabled(true);
                delete.setEnabled(false);
                copypasteFile.setEnabled(false);
                createItem.setEnabled(true);

            } else {
                copyFile.setEnabled(false);
                pasteFile.setEnabled(true);
                delete.setEnabled(true);
                copypasteFile.setEnabled(true);
            }
        } else {
            copyFile.setEnabled(true);
            pasteFile.setEnabled(true);
            delete.setEnabled(true);
            copypasteFile.setEnabled(false);
        }
    }

    public static void copyDirectory(File sourceFolder, File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdir(); // Создаем целевую директорию, если её нет
        }

        File[] files = sourceFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                File targetFile = new File(targetDir, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, targetFile); // Рекурсивно копируем поддиректории
                } else {
                    try {
                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING); // Копируем файлы
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static File findFileInAllDirectories(String fileName) {
        File rootDir = new File(Main.systemPath);
        return findFileRecursive(rootDir, fileName);
    }

    private static File findFileRecursive(File dir, String fileName) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(fileName)) {
                    return file;
                }
                if (file.isDirectory()) {
                    File found = findFileRecursive(file, fileName);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    public JTree createFileTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("SuperAppRoot");

        // Загружаем структуру файловой системы
        File rootDir = new File(Main.systemPath);
        buildFileTree(root, rootDir);

        FileManager.treeModel = new DefaultTreeModel(root); // Создаем модель дерева
        JTree tree = new JTree(FileManager.treeModel);
        tree.setRootVisible(false); // Скрываем корневой элемент

        return tree;
    }

    private static void buildFileTree(DefaultMutableTreeNode parent, File dir) {
        File[] files = dir.listFiles((_, name) ->
                !name.equals("out") &&
                        !name.equals(".idea") &&
                        !name.equals(".gitignore") &&
                        !name.equals("SupperApp.iml") &&
                        !name.equals(".hiddenFolder")
        );

        if (files != null) {
            for (File file : files) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
                parent.add(node);
                if (file.isDirectory()) {
                    buildFileTree(node, file);
                }
            }
        }
    }

    public static void refreshDirectoryNode(File directory) {
        // Сохраняем состояние развернутых узлов
        List<TreePath> expandedPaths = saveExpandedPaths(FileManager.fileTree);

        // Находим узел, соответствующий заданной директории
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) FileManager.treeModel.getRoot();
        TreePath path = findTreePathForDirectory(root, directory);

        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            // Очищаем дочерние узлы
            node.removeAllChildren();

            // Добавляем новые файлы и поддиректории
            addFilesToNode(node, directory);

            // Обновляем модель дерева для конкретного узла
            FileManager.treeModel.reload(node);
        }

        // Восстанавливаем состояние развернутых узлов
        restoreExpandedPaths(FileManager.fileTree, expandedPaths);
    }

    /**
     * Сохраняем состояние развернутых узлов дерева
     */
    private static List<TreePath> saveExpandedPaths(JTree tree) {
        List<TreePath> expandedPaths = new ArrayList<>();
        for (int i = 0; i < tree.getRowCount(); i++) {
            if (tree.isExpanded(i)) {
                expandedPaths.add(tree.getPathForRow(i));
            }
        }
        return expandedPaths;
    }

    /**
     * Восстанавливает состояние развернутых узлов дерева
     */
    private static void restoreExpandedPaths(JTree tree, List<TreePath> expandedPaths) {
        for (TreePath path : expandedPaths) {
            tree.expandPath(path);
        }
    }

    /**
     * Находит путь в дереве для указанной директории
     */
    private static TreePath findTreePathForDirectory(DefaultMutableTreeNode root, File directory) {
        return findTreePathForDirectoryRecursive(root, directory.getAbsolutePath());
    }


    private static TreePath findTreePathForDirectoryRecursive(DefaultMutableTreeNode node, String directoryPath) {
        String nodePath = getPathFromNode(node);
        if (nodePath.equals(directoryPath)) {
            return new TreePath(node.getPath());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath result = findTreePathForDirectoryRecursive(child, directoryPath);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Добавляет файлы и поддиректории в узел
     */
    public static void addFilesToNode(DefaultMutableTreeNode parent, File dir) {
        File[] files = dir.listFiles((_, name) ->
                !name.equals("out") &&
                        !name.equals(".idea") &&
                        !name.equals(".gitignore") &&
                        !name.equals("SupperApp.iml")
        );

        if (files != null) {
            for (File file : files) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
                parent.add(node);
                if (file.isDirectory()) {
                    addFilesToNode(node, file); // Рекурсивно добавляем содержимое поддиректорий
                }
            }
        }
    }

    /**
     * Добавляет файлы и поддиректории в узел
     */
     public static String getPathFromNode(DefaultMutableTreeNode node) {
        StringBuilder path = new StringBuilder(node.getUserObject().toString());
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        while (parent != null && !parent.isRoot()) {
            path.insert(0, parent.getUserObject().toString() + "/");
            parent = (DefaultMutableTreeNode) parent.getParent();
        }
        return Main.systemPath + "/" + path.toString();
    }
}
