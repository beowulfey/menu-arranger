package menuarranger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuViewer extends JDialog {
    protected JPanel rootPanel;
    protected JTree menuTree;
    protected JButton cancelButton;
    protected JButton OKButton;
    protected JButton deleteButton;
    protected JPanel BottomPanel;
    protected JPanel adjustPanel;
    protected JSplitPane adjustSplitPane;
    protected JScrollPane hiddenPane;
    protected JScrollPane visiblePane;
    protected JTree customTree;
    protected JButton addFolderButton;

    private DefaultMutableTreeNode newTreeRoot = new DefaultMutableTreeNode("Drop Here", true);
    private DefaultTreeModel newTreeModel = new DefaultTreeModel(newTreeRoot);
    public DefaultTreeModel finalTreeModel = null;

    public MenuViewer(DefaultTreeModel menuTreeModel) {
        setAlwaysOnTop(true);
        setContentPane(rootPanel);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        menuTree.setModel(menuTreeModel);
        menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); // I don't want to have to deal with moving multiple nodes, sorry!
        customTree.setModel(newTreeModel);
        customTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        customTree.setDragEnabled(true);
        customTree.setDropMode(DropMode.ON_OR_INSERT);

        // Setup button actions
        OKButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // NEED TO FIGURE OUT HOW TO MAKE THIS WORK SO IT DOESN'T DELETE THE BLANK NODE IF EMPTY!
                // ALSO NEED TO MAKE IT SO I CAN'T DO ON THE ROOT NODE!
                //DefaultMutableTreeNode temp = new DefaultMutableTreeNode(customTree.getLastSelectedPathComponent());
                //if (newTreeModel.getChildCount(temp.getParent()) < 1 && customTree.getLastSelectedPathComponent().toString() != "") {
                newTreeModel.removeNodeFromParent((DefaultMutableTreeNode) customTree.getLastSelectedPathComponent());
                //}
                //else {
                //    System.out.println("CAN'T DELETE THAT!");
                //}
            }
        });
        addFolderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode last = (DefaultMutableTreeNode) customTree.getLastSelectedPathComponent();
                DefaultMutableTreeNode insertNode = new DefaultMutableTreeNode("New Folder", true);
                // In order to get around my restriction in the canImport function below (can't drop on leaves)...
                DefaultMutableTreeNode fakeNode = new DefaultMutableTreeNode("", false);
                insertNode.add(fakeNode);
                newTreeModel.insertNodeInto(insertNode, last, last.getChildCount());
            }
        });

        // Code for setting up drag and drop from the menu tree to the custom tree
        customTree.setTransferHandler(new TransferHandler() {

            public boolean canImport(TransferSupport info) {
                // Currently preventing drops based on two criteria:
                // 1) If the path is null (anywhere in random space)
                // 2) If it is on a leaf that is not the root (assuming it is a command)
                // Exception to (2) is if it is a new folder... see below.

                info.setShowDropLocation(true);
                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath path = dl.getPath();
                int dropIndex = dl.getChildIndex();
                if (path == null) {
                    return false;
                } else {
                    //DefaultMutableTreeNode drop = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (dropIndex == -1 && path.getLastPathComponent() != newTreeModel.getRoot()) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        return !node.isLeaf();
                    }
                }
                return true;
            }

            public boolean importData(TransferSupport info) {
                // if we can't handle the import, say so
                if (!canImport(info)) {
                    return false;
                }

                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath path = dl.getPath();

                int childIndex = dl.getChildIndex();

                // if child index is -1, the drop was on top of the path, so we'll
                // treat it as inserting at the end of that path's list of children
                if (childIndex == -1) {
                    childIndex = customTree.getModel().getChildCount(path.getLastPathComponent());
                }

                // create a new node to represent the data and insert it into the model
                DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) menuTree.getLastSelectedPathComponent();

                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                newTreeModel.insertNodeInto(newNode, parentNode, childIndex);
                System.out.println(parentNode.getChildAt(0));
                System.out.println(childIndex);
                //THIS IS SUPER BUGGY!
                //if (parentNode.getChildAt(0).isLeaf() && parentNode.getChildAt(0).toString() == "") {
                //    parentNode.remove(0);}

                // make the new node visible and scroll so that it's visible
                customTree.makeVisible(path.pathByAddingChild(newNode));
                customTree.scrollRectToVisible(customTree.getPathBounds(path.pathByAddingChild(newNode)));

                return true;
            }
        });
    }

    public DefaultTreeModel getTreeModel() {
        return finalTreeModel;
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public void onOK() {
        setCustomTree();
        close();
    }

    public void onCancel() {
        close();
    }

    public void setCustomTree() {
        finalTreeModel = newTreeModel;
    }

    //////////////////////////////////////////////////////////
    // UI STUFF THAT I SHOULD NOT HAVE TO TOUCH
    //
    private void createUIComponents() {
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setAutoscrolls(false);
        rootPanel.setPreferredSize(new Dimension(600, 350));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        BottomPanel = new JPanel();
        BottomPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(BottomPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        BottomPanel.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JSeparator separator1 = new JSeparator();
        BottomPanel.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OKButton = new JButton();
        OKButton.setText("OK");
        BottomPanel.add(OKButton, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(102, 30), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        BottomPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        adjustPanel = new JPanel();
        adjustPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(adjustPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        adjustSplitPane = new JSplitPane();
        adjustSplitPane.setDividerLocation(200);
        adjustPanel.add(adjustSplitPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 200), null, 1, false));
        visiblePane = new JScrollPane();
        visiblePane.setAutoscrolls(false);
        adjustSplitPane.setLeftComponent(visiblePane);
        visiblePane.setBorder(BorderFactory.createTitledBorder(null, "System Menu", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP));
        menuTree = new JTree();
        menuTree.setDragEnabled(true);
        menuTree.setDropMode(DropMode.ON_OR_INSERT);
        menuTree.setShowsRootHandles(false);
        visiblePane.setViewportView(menuTree);
        hiddenPane = new JScrollPane();
        adjustSplitPane.setRightComponent(hiddenPane);
        hiddenPane.setBorder(BorderFactory.createTitledBorder(null, "Custom Menu", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP));
        customTree = new JTree();
        customTree.setDragEnabled(true);
        customTree.setDropMode(DropMode.ON_OR_INSERT);
        customTree.setEditable(true);
        customTree.setScrollsOnExpand(false);
        customTree.setToggleClickCount(2);
        customTree.setVerifyInputWhenFocusTarget(false);
        hiddenPane.setViewportView(customTree);
        deleteButton = new JButton();
        deleteButton.setText("Remove Selected");
        adjustPanel.add(deleteButton, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addFolderButton = new JButton();
        addFolderButton.setText("Add New Folder");
        adjustPanel.add(addFolderButton, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 2, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        adjustPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        rootPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 2, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        rootPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(0, -1), null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }


}