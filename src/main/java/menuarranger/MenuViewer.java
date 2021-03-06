package menuarranger;

import org.scijava.module.ModuleInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class MenuViewer extends JDialog {
    protected JPanel rootPanel;
    protected JTree menuTree;
    protected JButton cancelButton;
    protected JButton OKButton;
    protected JButton deleteButton;
    protected JPanel bottomPanel;
    protected JPanel adjustPanel;
    protected JSplitPane adjustSplitPane;
    protected JScrollPane hiddenPane;
    protected JScrollPane visiblePane;
    protected JTree customTree;
    protected JButton addFolderButton;
    protected JPanel topPanel;
    protected JTextField nodeName;
    protected JTextField statusField;

    private DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("Drop Here", true);
    private DefaultTreeModel newTreeModel = new DefaultTreeModel(newRoot);
    public DefaultTreeModel finalTreeModel = null;
    public DefaultMutableTreeNode finalTreeModelRoot;
    private DefaultTreeModel menuTreeModel;

    public MenuViewer(DefaultMutableTreeNode menuRoot) {

        DefaultMutableTreeNode rootCopy = copyRoot(menuRoot);
        menuTreeModel = new DefaultTreeModel(rootCopy);
        menuTree.setModel(menuTreeModel);
        customTree.setModel(newTreeModel);
        statusField.setForeground(new Color(200, 50, 50));

        // I don't want to have to deal with moving multiple nodes, sorry!
        menuTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        customTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // Dialog button and field functionality
        OKButton.addActionListener(e -> onOK());
        nodeName.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if (nodeName.getText().trim().equals("")) {
                    //nodeName.setEnabled(false);
                    nodeName.setText("New Folder Name");

                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (nodeName.getText().trim().equals("New Folder Name")) {
                    nodeName.setText("");
                }
            }
        });
        cancelButton.addActionListener(e -> onCancel());
        deleteButton.addActionListener(e -> {
            // Can I get this to delete multiple nodes at once? Low priority.
            try {
                newTreeModel.removeNodeFromParent((DefaultMutableTreeNode) customTree.getLastSelectedPathComponent());
            } catch (IllegalArgumentException err) {
                statusField.setText("Can't delete the root node!");
            } catch (NullPointerException err) {
                statusField.setText("Please select a site first!");
            }
        });
        addFolderButton.addActionListener(e -> {
            try {
                DefaultMutableTreeNode last = (DefaultMutableTreeNode) customTree.getLastSelectedPathComponent();
                TreePath path = customTree.getSelectionPath();
                DefaultMutableTreeNode insertNode = new DefaultMutableTreeNode(nodeName.getText(), true);
                newTreeModel.insertNodeInto(insertNode, last, last.getChildCount());
                customTree.makeVisible(path.pathByAddingChild(insertNode));
                customTree.scrollRectToVisible(customTree.getPathBounds(path.pathByAddingChild(insertNode)));
                nodeName.setText("New Folder Name");

            } catch (NullPointerException err) {
                statusField.setForeground(new Color(250, 0, 0));
                statusField.setText("Please select a site first!");


            }
        });

        // Code for setting up drag and drop from the menu tree to the custom tree
        customTree.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferSupport info) {
                // Currently preventing drops based on two criteria:
                // 1) If the path is null (anywhere in random space)
                // 2) If it is on a leaf that is not the root (assuming it is a command)
                info.setShowDropLocation(true);
                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath path = dl.getPath();
                if (path == null) {
                    return false;
                } else {
                    DefaultMutableTreeNode drop = (DefaultMutableTreeNode) path.getLastPathComponent();
                    return drop.getAllowsChildren();
                }
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

                // make the new node visible and scroll so that it's visible
                customTree.makeVisible(path.pathByAddingChild(newNode));
                customTree.scrollRectToVisible(customTree.getPathBounds(path.pathByAddingChild(newNode)));

                // Have to reload the tree with the copy because of weird bugs I can't explain when removing nodes.
                DefaultMutableTreeNode rootCopy = copyRoot(menuRoot);
                menuTreeModel.setRoot(rootCopy);
                menuTree.setModel(menuTreeModel);

                return true;
            }
        });
    }

    // Makes a copy of the root, so that when I make changes (such as dragging over an item)
    // it doesn't modify my original menu tree that is displayed.
    private DefaultMutableTreeNode copyRoot(final DefaultMutableTreeNode root) {
        DefaultMutableTreeNode copy = (DefaultMutableTreeNode) root.clone();
        if (!root.isLeaf()) {
            for (int i = 0; i < root.getChildCount(); i++) {
                DefaultMutableTreeNode child = copyRoot((DefaultMutableTreeNode) root.getChildAt(i));
                copy.add(child);
            }

        }
        return copy;
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public void onCancel() {
        close();
    }

    public void onOK() {
        finalizeChanges();
        close();
    }

    // Confirm your changes and set the final tree.
    public void finalizeChanges() {
        finalTreeModelRoot = (DefaultMutableTreeNode) newTreeModel.getRoot();
    }

    // Returns the root node for the custom tree.
    public DefaultMutableTreeNode getCustomRoot() {
        return finalTreeModelRoot;
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
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        rootPanel.setAutoscrolls(false);
        rootPanel.setPreferredSize(new Dimension(600, 350));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(bottomPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        bottomPanel.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        OKButton = new JButton();
        OKButton.setText("OK");
        bottomPanel.add(OKButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        statusField = new JTextField();
        statusField.setOpaque(false);
        bottomPanel.add(statusField, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        bottomPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        topPanel = new JPanel();
        topPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(topPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(258, 469), null, 0, false));
        adjustPanel = new JPanel();
        adjustPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(adjustPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        adjustSplitPane = new JSplitPane();
        adjustSplitPane.setDividerLocation(150);
        adjustPanel.add(adjustSplitPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 200), null, 0, false));
        hiddenPane = new JScrollPane();
        adjustSplitPane.setRightComponent(hiddenPane);
        hiddenPane.setBorder(BorderFactory.createTitledBorder(null, "Custom Menu", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, null, null));
        customTree = new JTree();
        customTree.setDragEnabled(true);
        customTree.setDropMode(DropMode.ON_OR_INSERT);
        customTree.setEditable(false);
        customTree.setInvokesStopCellEditing(false);
        customTree.setScrollsOnExpand(true);
        customTree.setToggleClickCount(2);
        customTree.setVerifyInputWhenFocusTarget(false);
        hiddenPane.setViewportView(customTree);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        adjustSplitPane.setLeftComponent(panel1);
        visiblePane = new JScrollPane();
        visiblePane.setAutoscrolls(false);
        panel1.add(visiblePane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        visiblePane.setBorder(BorderFactory.createTitledBorder(null, "System Menu", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, null, null));
        menuTree = new JTree();
        menuTree.setDragEnabled(true);
        menuTree.setDropMode(DropMode.ON_OR_INSERT);
        menuTree.setShowsRootHandles(false);
        visiblePane.setViewportView(menuTree);
        deleteButton = new JButton();
        deleteButton.setText("Remove");
        topPanel.add(deleteButton, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        topPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        addFolderButton = new JButton();
        addFolderButton.setText("Add");
        topPanel.add(addFolderButton, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(84, -1), null, 0, false));
        nodeName = new JTextField();
        nodeName.setText("New Folder Name");
        nodeName.setToolTipText("Enter name for new node");
        topPanel.add(nodeName, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JSeparator separator1 = new JSeparator();
        rootPanel.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(0, 0), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }


}

