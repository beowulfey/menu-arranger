package menumaker;

import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.log.LogService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.swing.menu.SwingJMenuBarCreator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@Plugin(type = Command.class, menuPath = "File>Arrange Menus")
public class MenuMaker extends InteractiveCommand {



    @Parameter
    private MenuService menuService;
    @Parameter
    private UIService uiService;
    @Parameter
    private LogService logService;

    protected JFrame frame = null;
    protected DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Menu");
    protected DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    protected JMenuBar swingMenuBar = new JMenuBar();

    protected int prevDepth = -1;


    ///////////////////////////////////////////////////////////////////////////////////
    // Reads through the shadow menu currently in use as the context.
    // Creates nodes for each parent. Right now it only goes through the first layer.
    // Applies those nodes to the Root.
    // WORKS WELL! YES!
    private void parseMenu(final ShadowMenu root, DefaultMutableTreeNode treeParent) {
        DefaultMutableTreeNode node = null;
        for (ShadowMenu child : root.getChildren()) {
            final double depth = child.getMenuDepth();
            if (depth > prevDepth) {
                node = new DefaultMutableTreeNode(child.getMenuEntry());
                treeParent.add(node);
                logService.info(node);
            }
            if (child.getChildren() != null) {
                prevDepth += 1;
                parseMenu(child, node);
            }
        }
        prevDepth -= 1;
    }

    @Override
    public void run() {
        if (frame == null) {
            frame = new JFrame("Menu Arranger");
            MenuViewer menuViewer = new MenuViewer();
            //Get menu context
            final ShadowMenu orig = menuService.getMenu();
            parseMenu(orig, treeRoot);
            treeModel.setRoot(treeRoot);
            System.out.println(treeModel.getChildCount(treeRoot));

            //THIS SECTION WILL ALLOW ME TO ADJUST THE MENU!!
            // Right now I'm just reusing the default menu.
            new SwingJMenuBarCreator().createMenus(orig, swingMenuBar);
            frame.setJMenuBar(swingMenuBar);

            //THIS is supposed to update the tree. ain't working.
            //treeModel.reload(treeRoot);
            //treeModel.setRoot(treeRoot);


            // UI SETUP AND APPEARANCE.
            System.out.println("root from parent class:"+ treeModel.getRoot());
            menuViewer.setMenuTree(treeModel);

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(menuViewer.rootPanel);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public static void main(final String... args) {
        // Launch ImageJ as usual.
        ImageJ ij = new ImageJ();
        ij.launch(args);

        System.out.println("THIS IS BEFORE IT RUNS");

        // why does this have to be false? if I make it true, the command runs twice?!
        ij.command().run(MenuMaker.class, false);
    }
}