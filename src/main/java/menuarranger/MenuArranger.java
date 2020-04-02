// TO DO:
// Build a shadow menu.

package menuarranger;

import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;

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
    ThreadService threadService;
    @Parameter
    private CustomMenuService customMenuService;

    private DefaultMutableTreeNode treeRoot;
    private DefaultTreeModel treeModel;
    private HashMap<String, ModuleInfo> menuMap;
    private HashMap<String, ModuleInfo> dupeMap;

    protected DefaultTreeModel customTreeModel;
    protected ShadowMenu customMenu;

    private int prevDepthIn;


    public MenuArranger(){
        // Constructor time!
        treeRoot = new DefaultMutableTreeNode("Menu");
        treeModel = new DefaultTreeModel(treeRoot);
        customTreeModel = null;
        customMenu = null;

        menuMap = new HashMap<>();
        dupeMap = new HashMap<>();

        prevDepthIn = -1;
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
            List<String> pathListString = new ArrayList<String>();
            for (TreeNode treeNode : pathList) {
                pathListString.add(treeNode.toString());
            }
            pathListString.remove(0);

            if (dupeMap.containsKey(key)){
                logService.info("Duplicate item detected! Deferring to user input");
                ModuleInfo[] options = {dupeMap.get(key), menuMap.get(key)};
                MenuMatcher newDialog = new MenuMatcher(options, pathListString);
                JDialog dialog = new JDialog(newDialog);
                ModuleInfo selection =  newDialog.getSelection();
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

    private synchronized DefaultTreeModel getNewMenu(){

        return customTreeModel;
    }

    @Override
    public void run() {
        // Get menu context and build a treeModel from it.
        final ShadowMenu orig = menuService.getMenu();

        MenuViewer menuViewer = new MenuViewer(treeModel);
        Thread thread = threadService.newThread(() -> {
            parseMenu(orig, treeRoot);
            treeModel.setRoot(treeRoot);
            menuViewer.setupGUI();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        customTreeModel = menuViewer.getModel();
        if (customTreeModel != null){
            notify();
            System.out.println("Made it!");
        }


        System.out.println("after Gui");
        //if (threadTree != null){
        //    System.out.println("CustomTreeModel returned "+customTreeModel.getChildCount(customTreeModel.getRoot()));
        //    makeNewMenu(customTreeModel);
        //}



    }
}