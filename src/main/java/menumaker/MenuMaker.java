package menumaker;

import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Plugin(type = Command.class, menuPath = "File>Arrange Menus")
public class MenuMaker implements Command {

    @Parameter
    private MenuService menuService;
    @Parameter
    private UIService uiService;
    @Parameter
    private LogService logService;

    private JFrame frame = null;
    private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Menu");
    private DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    // private JMenuBar swingMenuBar = new JMenuBar();

    private int prevDepth = -1;
    private boolean init = false;


    ///////////////////////////////////////////////////////////////////////////////////
    // Reads through the shadow menu currently in use within the context.
    // Creates a node for each parent. Applies those nodes to the local root.
    // Does this at each layer.
    // Appears to be working properly, which caught me off guard.
    private void parseMenu(final ShadowMenu root, DefaultMutableTreeNode treeParent) {
        DefaultMutableTreeNode node = null;
        for (ShadowMenu child : root.getChildren()) {
            final double depth = child.getMenuDepth();
            if (depth > prevDepth) {
                node = new DefaultMutableTreeNode(child.getMenuEntry());
                treeParent.add(node);
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
        if (frame == null && !init) {
            frame = new JFrame("Menu Arranger");
            frame.addWindowListener(new WindowAdapter(){
                public void windowClosed(WindowEvent wC) {
                    frame = null;
                }
            });
            MenuViewer menuViewer = new MenuViewer();

            // Get menu context and build a treeModel from it.
            final ShadowMenu orig = menuService.getMenu();
            parseMenu(orig, treeRoot);
            treeModel.setRoot(treeRoot);

            // THIS SECTION WILL ALLOW ME TO ADJUST THE MENU
            // Right now I'm just reusing the default menu.
            // new SwingJMenuBarCreator().createMenus(orig, swingMenuBar);
            // frame.setJMenuBar(swingMenuBar);

            // UI SETUP AND APPEARANCE.
            menuViewer.setupUI(treeModel);

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(menuViewer.rootPanel);
            frame.pack();
            frame.setVisible(true);
            init = true;
            System.out.println(frame);
            logService.info(init);
        }
        else {
            logService.info("Made it to the else");
            // I can't figure out how to only allow one window at a time.
            // uiService.showDialog("Only one window can be opened at once!");
        }
    }

    public static void main(final String... args) {
        // Launch ImageJ as usual.
        ImageJ ij = new ImageJ();
        ij.launch(args);

        ij.command().run(MenuMaker.class, false);

    }
}