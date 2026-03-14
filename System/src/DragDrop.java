import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class DragDrop {
    public static void fromManagerToSystem() {
        // Настройка DragSource для дерева файлов
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(FileManager.fileTree, DnDConstants.ACTION_MOVE, new DragGestureListener() {
            @Override
            public void dragGestureRecognized(DragGestureEvent dge) {
                TreePath selectionPath = FileManager.fileTree.getSelectionPath();
                if (selectionPath == null) return;

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

                // Получаем полный путь к файлу/папке
                String filePath = SystemMethods.getPathFromNode(node);
                File file = new File(filePath);
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(null, "Файл не найден: " + filePath, "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Transferable trans = new FileTransferable(file);

                dge.startDrag(null, trans);
            }
        });
    }

    public static void fromSystemToManager() {

        new DropTarget(FileManager.scrollPane, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        TreePath targetPath = FileManager.fileTree.getSelectionPath();
                        if (targetPath == null) {
                            dtde.dropComplete(false);
                            return;
                        }

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) targetPath.getLastPathComponent();
                        File targetDir = SystemMethods.findFileInAllDirectories(node.getUserObject().toString());

                        if (targetDir == null || !targetDir.isDirectory()) {
                            JOptionPane.showMessageDialog(null, "Выбранный элемент не является папкой.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            dtde.dropComplete(false);
                            return;
                        }

                        // Проверка: нельзя переносить в папку "System"
                        if ("System".equals(targetDir.getName())) {
                            JOptionPane.showMessageDialog(null, "Нельзя переносить файлы в защищённую папку 'System'.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                            dtde.dropComplete(false);
                            return;
                        }

                        // Перемещаем файлы в целевую директорию
                        for (File fileToMove : files) {
                            File targetFile = new File(targetDir, fileToMove.getName());
                            Files.move(fileToMove.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        // Обновляем дерево файлов
                        SystemMethods.refreshDirectoryNode(targetDir);
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                    ex.printStackTrace();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}
            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            @Override
            public void dragExit(DropTargetEvent dte) {}
        });
    }
}