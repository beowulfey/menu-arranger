// TO DO:
// Build a shadow menu.

package menuarranger;

import org.scijava.MenuPath;
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
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
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
    PluginService pluginService;
    @Parameter
    UIService uiService;
    @Parameter
    EventService eventService;
    //@Parameter
    //private CustomMenuService customMenuService;

    private DefaultMutableTreeNode treeRoot;
    private DefaultTreeModel treeModel;
    private List<ModuleInfo> modList;
    private List<ModuleInfo> newModList;
    private HashMap<String, ModuleInfo> menuMap;
    private HashMap<String, ModuleInfo> dupeMap;

    protected DefaultTreeModel customTreeModel;
    protected ShadowMenu customMenu;

    private int prevDepthIn;


    public MenuArranger(){
        treeRoot = new DefaultMutableTreeNode("Menu");
        treeModel = new DefaultTreeModel(treeRoot);
    }

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
                    node.setAllowsChildren(false); // doesn't do shit.
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
        modList = moduleService.getModules();
        for (ModuleInfo mod : modList) {
            if (!mod.getMenuPath().isEmpty() && mod.getLocation() != null) {    // had a weird issue with this plugin being installed twice, hack to avoid
                logService.info("Item: "+mod);
                logService.info("Root "+mod.getMenuRoot());
                String key = mod.getMenuPath().getLeaf().toString();            // Take the leaf node of each path for a key
                ModuleInfo oldValue = menuMap.put(key, mod);
                if (oldValue != null && oldValue.getMenuPath() != mod.getMenuPath()) {
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
        //    if (plug.getTitle().contains("MenuService")){
        //        logService.info(plug.getTitle());
        //        logService.info(plug.getPriority());
        //    }
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
            List<String> pathListString = new ArrayList<String>();
            for (TreeNode treeNode : pathList) {
                pathListString.add(treeNode.toString());
            }
            pathListString.remove(0);

            if (dupeMap.containsKey(key)){
                logService.info("Duplicate item detected! Deferring to user input");
                ModuleInfo[] options = {menuMap.get(key), dupeMap.get(key)};
                MenuMatcher menuMatcher = new MenuMatcher(options, pathListString);

                // Set up user selection dialog
                JDialog dialog = new JDialog(menuMatcher);
                dialog.setLocation(200, 200); // THIS IS SO HACKY!
                dialog.setAlwaysOnTop(true);
                dialog.setContentPane(menuMatcher.contentPane);
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);

                ModuleInfo selection =  menuMatcher.getSelection();
                logService.info("Previous menu is: "+ selection.getMenuPath());
                String cleanedPath = cleanPath(pathListString);
                MenuPath newPath = new MenuPath(cleanedPath,",");
                selection.setMenuPath(newPath);
                logService.info("--->New menu is: "+ selection.getMenuPath());

            }
            else if (menuMap.containsKey(key)){
                logService.debug("Found a match for "+key);
                ModuleInfo entry = menuMap.get(key);
                logService.info("Previous menu is: "+ entry.getMenuPath());
                String cleanedPath = cleanPath(pathListString);
                MenuPath newPath = new MenuPath(cleanedPath,",");
                entry.setMenuPath(newPath);
                logService.info("--->New menu is: "+ entry.getMenuPath());

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

    // Why is everything in Java so complicated?
    // Removing brackets from the string for Menu Path
    private String cleanPath(List<String> list){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<list.size(); i++){
            sb.append(list.get(i));
            if (i<list.size()-1) sb.append(",");
        }
        return sb.toString();
    }


    @Override
    public void run() {
        // These must be reset each time it is run
        customTreeModel = null;
        customMenu = null;
        prevDepthIn = -1;
        menuMap = new HashMap<>();
        dupeMap = new HashMap<>();

        // Get menu context and build a treeModel from it.
        final ShadowMenu orig = menuService.getMenu();
        parseMenu(orig, treeRoot);
        treeModel.setRoot(treeRoot);
        MenuViewer menuViewer = new MenuViewer(treeModel);

        // Set up dialog window
        JDialog selector = new JDialog(menuViewer);
        selector.setContentPane(menuViewer.rootPanel);
        selector.pack();
        selector.setModal(true);
        selector.setVisible(true);
        customTreeModel = menuViewer.getTreeModel();


        if (customTreeModel != null) {
            //menuService.getMenu().removeAll(moduleService.getModules());
            //if (customMenu != null) {
            //    System.out.println("SETTING MENU!!!!");
            //    new SwingJMenuBarCreator().createMenus(customMenu, swingMenuBar);
            //    frame.setJMenuBar(swingMenuBar);
            //}
            orig.remove(treeModel.getRoot());
            makeNewMenu(customTreeModel);
        }
    }



}