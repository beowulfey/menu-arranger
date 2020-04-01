// TO DO:
// Show a pop up when there is a duplicate menu item.
// Build a shadow menu.

package menuarranger;

import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.menu.SwingJMenuBarCreator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Plugin(type = Command.class, menuPath = "File>Arrange Menus")
public class MenuArranger extends ContextCommand implements Runnable {

    @Parameter
    private MenuService menuService;
    @Parameter
    private ModuleService moduleService;
    @Parameter
    private LogService logService;
    @Parameter
    EventService eventService;
    @Parameter
    private CustomMenuService customMenuService;

    private JFrame frame;
    private JDialog dialog;
    private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Menu");
    private DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    public DefaultTreeModel customTreeModel = new DefaultTreeModel(treeRoot);
    public ShadowMenu customMenu = null;
    private JMenuBar swingMenuBar = new JMenuBar();
    private HashMap<String, ModuleInfo> menuMap = new HashMap<>();
    private HashMap<String, ModuleInfo> dupeMap = new HashMap<>();

    private int prevDepthIn = -1;
    private boolean init = false;


    ///////////////////////////////////////////////////////////////////////////////////
    // Reads through the shadow menu currently in use within the context.
    // Creates a node for each parent. Applies those nodes to the local root.
    // Does this at each layer.
    // Appears to be working properly, which caught me off guard.
    private void parseMenu(final ShadowMenu root, DefaultMutableTreeNode treeParent) {
        DefaultMutableTreeNode node;
        for (ShadowMenu child : root.getChildren()) {
            final double depth = child.getMenuDepth();
            if (depth > prevDepthIn) {
                if (child.getChildren() == null) {
                    node = new DefaultMutableTreeNode(child.getMenuEntry());
                    treeParent.setAllowsChildren(false);
                    treeParent.add(node);
                }
                else if (child.getChildren() != null) {
                    node = new DefaultMutableTreeNode(child.getMenuEntry());
                    treeParent.add(node);
                    prevDepthIn += 1;
                    parseMenu(child, node);
                }

            }
        }
        prevDepthIn -= 1;
    }

    private void makeNewMenu (DefaultTreeModel newTreeModel) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) newTreeModel.getRoot();

        // I think this will need to come from the CustomMenuService
        //ShadowMenu newMenu = null;

        // Start by building a Hashmap of all current menu modules.
        // This will allow me to associate the menu item with its original ModuleInfo metadata.
        List<ModuleInfo> modList = moduleService.getModules();
        for (ModuleInfo mod : modList) {
            if (!mod.getMenuPath().isEmpty() && mod.getLocation() != null) {    // had a weird issue with this plugin being installed twice, hack to avoid
                String key = mod.getMenuPath().getLeaf().toString();            // Take the leaf node of each path for a key
                ModuleInfo oldValue = menuMap.put(key, mod);
                if (oldValue != null && oldValue.getMenuPath() != mod.getMenuPath()) {
                    logService.warn("Multiple options detected!" + oldValue.getMenuPath() + " vs. " + mod.getMenuPath());
                    ModuleInfo olderValue = dupeMap.put(key, oldValue);
                    if (olderValue != null && olderValue.getMenuPath() != oldValue.getMenuPath()) {
                        logService.warn("Wow! There are three of this menu entry! You suck at naming plugins. The third one " +
                                "is located at " + olderValue.getMenuPath());
                    }
                }
            }
        }
        // DEBUGGING
        //List<PluginInfo<?>> plugList = pluginService.getPlugins();
        //for (PluginInfo plug : plugList) {
        //    if (plug.getTitle().contains("DefaultMenuService")){
        //        logService.info(plug.getTitle());
        //        logService.info(plug.getPriority());
        //    }
        //}



        // Populate the dupe key HashMap with all values
        //for (String dupeKey : dupeMap.keySet()) {
        //    dupeMapExtended.put(dupeKey, new ModuleInfo[]{menuMap.get(dupeKey),dupeMap.get(dupeKey)});
        //}

        // Now iterate through the tree
        traverseChildren(rootNode);

    }

    private void traverseChildren (DefaultMutableTreeNode root) {
        // needs to use CustomMenuService ShadowMenu !!
        int children = root.getChildCount();
        if (root.isLeaf()) {
            TreeNode[] childPath = root.getPath(); // Get the path as an array.
            List<TreeNode> pathList = new ArrayList<>(Arrays.asList(childPath)); //Array to list is immutable so have to make a copy.
            String key = pathList.get(pathList.size() - 1).toString();
            if (dupeMap.containsKey(key)){
                ModuleInfo[] options = {dupeMap.get(key), menuMap.get(key)};
                MenuMatcher newDialog = new MenuMatcher(options);
                dialog = new JDialog(newDialog);
                // NEED TO PAUSE HERE FOR RETURN!
                ModuleInfo selection =  newDialog.getSelection();
                logService.info("You chose "+selection);

            }
            else if (menuMap.containsKey(key)){
                logService.debug("Found a match for "+key);
                //logService.info(menuMap.get(key));
            }
            else {
                logService.warn("Derp! Unable to find a menu item for "+key);
            }
        }
        else {
            for (int i = 0; i < children; i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i); // get child node
                traverseChildren(child);
            }

        }
    }

    @Override
    public void run() {

        if (frame == null && !init) {
            frame = new JFrame("Menu Arranger");
            frame.addWindowListener(new WindowAdapter(){
                public void windowClosed(WindowEvent wC) {
                    frame = null;
                    makeNewMenu(treeModel);
                }
            });
            MenuViewer menuViewer = new MenuViewer();



            // Get menu context and build a treeModel from it.
            final ShadowMenu orig = menuService.getMenu();
            parseMenu(orig, treeRoot);
            treeModel.setRoot(treeRoot);

            customTreeModel = menuViewer.setupUI(treeModel);

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
}