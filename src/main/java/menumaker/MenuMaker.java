package menumaker;

import net.imagej.ImageJ;
import org.scijava.MenuEntry;
import org.scijava.command.Command;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;
import org.scijava.ui.swing.menu.SwingJMenuBarCreator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ListIterator;

@Plugin(type = Command.class, menuPath = "File>Arrange Menus")
public class MenuMaker implements Command {

    @Parameter
    private MenuService menuService;
    @Parameter
    private UIService uiService;
    @Parameter
    private ModuleService moduleService;
    @Parameter
    PluginService pluginService;
    @Parameter
    private EventService eventService;
    @Parameter
    private LogService logService;

    protected JFrame frame = null;
    private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Menu");
    private DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    public DefaultTreeModel customTreeModel = new DefaultTreeModel(treeRoot);
    public ShadowMenu customMenu = null;
    private JMenuBar swingMenuBar = new JMenuBar();

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
                if (child.getChildren() == null) {
                    node = new DefaultMutableTreeNode(child.getMenuEntry());
                    treeParent.setAllowsChildren(false);
                    treeParent.add(node);
                }
                else if (child.getChildren() != null) {
                    node = new DefaultMutableTreeNode(child.getMenuEntry());
                    treeParent.add(node);
                    prevDepth += 1;
                    parseMenu(child, node);
                }

            }
        }
        prevDepth -= 1;
    }

    private ShadowMenu makeNewMenu (DefaultTreeModel newTreeModel){
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) newTreeModel.getRoot();
        ShadowMenu newMenu = null;

        int children = rootNode.getChildCount();
        int i = 0;
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
        MenuEntry menu0 = (MenuEntry) child.getUserObject();
        System.out.println(menu0);

        // access all modules currently in service.
        List modList = moduleService.getModules();
        for (ListIterator it = modList.listIterator(); it.hasNext(); ) {
            ModuleInfo mod = (ModuleInfo) it.next();
            if (mod.isVisible() && !mod.getMenuPath().isEmpty()) {
                logService.info(mod.getName());
                logService.info(mod.getMenuPath());
                logService.info(mod.getLocation());
            }
        }

        // access all plugins currently in service.
        List plugList = pluginService.getPlugins();
        for (ListIterator it2 = plugList.listIterator(); it2.hasNext(); ){
            PluginInfo plug = (PluginInfo) it2.next();
            if (plug.isVisible() && !plug.getMenuPath().isEmpty()) {
                logService.info(plug.getName());
                logService.info(plug.getMenuPath());
                logService.info(plug.getLocation());
            }
        }

            return newMenu;
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

            customTreeModel = menuViewer.setupUI(treeModel);
            System.out.println(customTreeModel.getChildCount(customTreeModel.getRoot()));
            System.out.println(treeModel.getChildCount(treeModel.getRoot()));
            customMenu = makeNewMenu(treeModel);

            // THIS SECTION WILL ALLOW ME TO ADJUST THE MENU
            // Right now I'm just reusing the default menu.
            if (customMenu != null) {
                System.out.println("SETTING MENU!!!!");
                new SwingJMenuBarCreator().createMenus(customMenu, swingMenuBar);
                frame.setJMenuBar(swingMenuBar);
            }

            // UI SETUP AND APPEARANCE.


            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(menuViewer.rootPanel);
            frame.pack();
            frame.setVisible(true);
            init = true;
        }
        else {
            logService.info("Made it to the else");
            // I can't figure out how to only allow one window at a time.
            // This did not work...
            //uiService.showDialog("Only one window can be opened at once!");
        }
    }

    public static void main(final String... args) {
        // Launch ImageJ as usual.
        ImageJ ij = new ImageJ();
        ij.launch(args);

        ij.command().run(MenuMaker.class, false);

    }
}