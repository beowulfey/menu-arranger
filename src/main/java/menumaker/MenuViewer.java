package menumaker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;

public class MenuViewer {
    protected JPanel rootPanel;
    protected JTree menuTree;
    protected JButton cancelButton;
    protected JButton okButton;
    protected JButton hideButton;
    protected JButton showButton;
    protected JPanel BottomPanel;
    protected JPanel adjustPanel;
    protected JSplitPane adjustSplitPane;
    protected JScrollPane hiddenPane;
    protected JScrollPane visiblePane;
    protected JTree hiddenTree;

    private DefaultMutableTreeNode tempTreeRoot = new DefaultMutableTreeNode("FAILED");
    private DefaultMutableTreeNode newTreeRoot = new DefaultMutableTreeNode("Menu");
    private DefaultTreeModel sysTreeModel = new DefaultTreeModel(tempTreeRoot);
    private DefaultTreeModel newTreeModel = new DefaultTreeModel(newTreeRoot);

    public void setMenuTree(DefaultTreeModel menuTreeModel) {
        sysTreeModel = menuTreeModel;
        menuTree.setModel(sysTreeModel);
        menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
    }

    public void setupHiddenTree() {
        hiddenTree.setModel(newTreeModel);
        hiddenTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        hiddenTree.setDropMode(DropMode.ON);
        hiddenTree.setTransferHandler(new TransferHandler() {

            public boolean canImport(TransferSupport info) {
                // THIS STUFF HAPPENS WHILE THE MOUSE BUTTON IS STILL HELD!!

                info.setShowDropLocation(true);
                TreePath item = menuTree.getSelectionPath();
                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();

                // DEBUG
                System.out.println(item);
                System.out.println("Drop location is " + dl.getPath());

                return true;
            }

            public boolean importData(TransferSupport info) {
                // TO DO: Set it to do something if drop location is null!?

                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath path = dl.getPath();
                TreePath item = menuTree.getSelectionPath();

                System.out.println("You chose to drop it at " + path);

                int childIndex = dl.getChildIndex();
                System.out.println("The index here is " + childIndex);

                // if child index is -1, the drop was on top of the path, so we'll
                // treat it as inserting at the end of that path's list of children
                if (childIndex == -1) {
                    childIndex = hiddenTree.getModel().getChildCount(path.getLastPathComponent());
                }

                // create a new node to represent the data and insert it into the model
                DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) menuTree.getLastSelectedPathComponent();
                System.out.println(newNode);
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                newTreeModel.insertNodeInto(newNode, parentNode, childIndex);

                // make the new node visible and scroll so that it's visible
                hiddenTree.makeVisible(path.pathByAddingChild(newNode));
                hiddenTree.scrollRectToVisible(hiddenTree.getPathBounds(path.pathByAddingChild(newNode)));

                return true;
            }
        });


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
        BottomPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(BottomPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        BottomPanel.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        okButton = new JButton();
        okButton.setText("OK");
        BottomPanel.add(okButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        BottomPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 1, false));
        final JSeparator separator1 = new JSeparator();
        BottomPanel.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        adjustPanel = new JPanel();
        adjustPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(adjustPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        adjustSplitPane = new JSplitPane();
        adjustSplitPane.setDividerLocation(200);
        adjustPanel.add(adjustSplitPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 200), null, 1, false));
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
        hiddenTree = new JTree();
        hiddenTree.setDragEnabled(true);
        hiddenTree.setDropMode(DropMode.ON_OR_INSERT);
        hiddenTree.setEditable(true);
        hiddenTree.setScrollsOnExpand(false);
        hiddenTree.setToggleClickCount(2);
        hiddenTree.setVerifyInputWhenFocusTarget(false);
        hiddenPane.setViewportView(hiddenTree);
        hideButton = new JButton();
        hideButton.setText("Make hidden");
        adjustPanel.add(hideButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        showButton = new JButton();
        showButton.setText("Make visible");
        adjustPanel.add(showButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 3, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        rootPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 2, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        rootPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(0, -1), null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}