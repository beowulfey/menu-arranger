// TO DO:
// Build a shadow menu.

package menuarranger;

import org.scijava.MenuPath;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.event.EventService;
import org.scijava.io.IOService;
import org.scijava.log.LogService;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Plugin(type = Command.class, menuPath = "File>Arrange Menus")
public class MenuArranger extends ContextCommand implements Runnable {

    @Parameter
    private CustomMenuService menuService;
    @Parameter
    private ModuleService moduleService;
    @Parameter
    private LogService logService;
    @Parameter
    EventService eventService;
    @Parameter
    IOService ioService;
    @Parameter
    PluginService pluginService;

    private DefaultMutableTreeNode treeRoot;
    private DefaultTreeModel treeModel;
    private List<ModuleInfo> modList;
    private List<ModuleInfo> debugList;
    private List<ArrayList<Object>> newModList;
    private HashMap<String, ModuleInfo> menuMap;
    private HashMap<String, ModuleInfo> dupeMap;

    protected DefaultMutableTreeNode newRoot;
    protected DefaultTreeModel customTreeModel;
    protected ShadowMenu customMenu;

    private int prevDepthIn;

    // Basic constructor.
    public MenuArranger(){
        debugList = new ArrayList<ModuleInfo>();
        newModList = new ArrayList<ArrayList<Object>>();
        treeRoot = new DefaultMutableTreeNode("Menu");
        treeModel = new DefaultTreeModel(treeRoot);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Reads through the shadow menu currently in use within the context.
    // Creates a node for each parent. Applies those nodes to the local root.
    // Does this at each layer.
    // Appears to be working properly, which caught me off guard.
    // The function of this is to convert the ShadowMenu format into a JTree format
    // by making a DefaultMutableTreeNode for the root that contains everything below
    private void parseMenu(final ShadowMenu root, DefaultMutableTreeNode treeParent) {
        DefaultMutableTreeNode node;
        for (ShadowMenu child : root.getChildren()) {
            final double depth = child.getMenuDepth();
            if (depth > prevDepthIn) {
                if (child.isLeaf()) {
                    node = new DefaultMutableTreeNode(child.getMenuEntry());
                    node.setAllowsChildren(false); // doesn't do shit.
                    treeParent.add(node);
                }
                else if (!child.isLeaf()) {
                    node = new DefaultMutableTreeNode(child.getMenuEntry());
                    treeParent.add(node);
                    prevDepthIn += 1;
                    parseMenu(child, node);
                }
            }
        }
        prevDepthIn -= 1;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Start by building a HashMap of all current menu modules.
    // Builds a second map for if a command appears twice. This seemed most simple.
    // If there are more than two, it will break!
    // This will allow me to associate the menu item direct with its original ModuleInfo metadata.
    private void buildMenuIndex() {
        modList = moduleService.getModules();
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
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // This reads through the root node and
    // Unfortunately, because CommandInfo is not serializable, I have to do it again on load.
    // But, I can still save the new path this way.
    private void traverseChildren (DefaultMutableTreeNode root) {
        TreeNode[] childPath = root.getPath(); // Get the path as an array.
        List<TreeNode> pathList = new ArrayList<>(Arrays.asList(childPath)); //Array to list is immutable so have to make a copy.
        String key = pathList.get(pathList.size() - 1).toString();
        List<String> pathListString = new ArrayList<String>();
        for (TreeNode treeNode : pathList) pathListString.add(treeNode.toString());
        pathListString.remove(0);

        int children = root.getChildCount();
        try {
            if (root.isLeaf()) {
                // get the current index in the parent node
                int pri = root.getParent().getIndex(root);
                if (dupeMap.containsKey(key)) {
                    // Opens user dialog to confirm the correct original menu path
                    logService.debug("Duplicate item detected! Deferring to user input");
                    ModuleInfo[] options = {menuMap.get(key), dupeMap.get(key)};
                    MenuMatcher menuMatcher = new MenuMatcher(options, pathListString);
                    JDialog dialog = new JDialog(menuMatcher);
                    dialog.setLocation(200, 200); // THIS IS SO HACKY!
                    dialog.setAlwaysOnTop(true);
                    dialog.setContentPane(menuMatcher.contentPane);
                    dialog.pack();
                    dialog.setModal(true);
                    dialog.setVisible(true);

                    ModuleInfo selection = menuMatcher.getSelection();
                    String cleanedPath = cleanPath(pathListString);
                    ArrayList<Object> modInfo = new ArrayList();
                    logService.warn(selection.getDelegateClassName());
                    modInfo.add(selection.getDelegateClassName());
                    modInfo.add(cleanedPath);
                    modInfo.add(pri);
                    newModList.add(modInfo);

                    // DEBUGGING
                    MenuPath newPath = new MenuPath(cleanedPath, ",");
                    newPath.getLeaf().setWeight(pri);
                    selection.setMenuPath(newPath);
                    debugList.add(selection);

                } else if (menuMap.containsKey(key)) {
                    ModuleInfo entry = menuMap.get(key);
                    String cleanedPath = cleanPath(pathListString);
                    ArrayList<Object> modInfo = new ArrayList();
                    logService.warn(entry.getDelegateClassName());
                    modInfo.add(entry.getDelegateClassName());
                    modInfo.add(cleanedPath);
                    modInfo.add(pri);
                    //logService.info(modInfo);
                    newModList.add(modInfo);

                    // DEBUGGING
                    MenuPath newPath = new MenuPath(cleanedPath, ",");
                    newPath.getLeaf().setWeight(pri);
                    entry.setMenuPath(newPath);
                    debugList.add(entry);

                } else {
                    logService.warn("Derp! Unable to find a menu item for " + key);
                }
            } else {
                for (int i = 0; i < children; i++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i); // get child node
                    traverseChildren(child);
                }
            }
        }
        catch (NullPointerException err){
            logService.info("Nothing selected");
            err.getStackTrace();
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

    public void writeObject(List<ArrayList<Object>> object){
        try{
            //logService.info(object);
            FileOutputStream f = new FileOutputStream(new File("customMenuList.map"));
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(object);
            o.close();
            f.close();
        } catch (FileNotFoundException err) {
            logService.info("Oops!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void debug(){
        logService.info("Current directory: "+System.getProperty("user.dir"));
        //DEBUGGING
        List<PluginInfo<?>> plugList = pluginService.getPlugins();
        for (PluginInfo plug : plugList) {
            if (plug.getClassName().contains("Menu")){
                logService.warn(plug.getTitle());
                logService.warn(plug.getLocation());
                logService.warn(plug.getPriority());
            }
        }
    }

    @Override
    public void run() {
        // These must be reset each time it is run
        customTreeModel = null;
        customMenu = null;
        prevDepthIn = -1;
        menuMap = new HashMap<>();
        dupeMap = new HashMap<>();

        // Get current context and build a TreeModel and list of current modules
        buildMenuIndex();
        if (menuService.getDefaultMenu() != null) {
            final ShadowMenu orig = menuService.getDefaultMenu();
            parseMenu(orig, treeRoot);
        } else{
            final ShadowMenu orig = menuService.getMenu();
            parseMenu(orig, treeRoot);
        }
        treeModel.setRoot(treeRoot);
        MenuViewer menuViewer = new MenuViewer(treeRoot);

        // Set up dialog window
        JDialog selector = new JDialog(menuViewer);
        selector.setContentPane(menuViewer.rootPanel);
        selector.pack();
        selector.setModal(true);
        selector.setVisible(true);
        newRoot = menuViewer.getCustomRoot();

        //debug();

        if (newRoot != null) {
            //menuService.getMenu().removeAll(moduleService.getModules());
            // if (customMenu != null) {
            //    System.out.println("SETTING MENU!!!!");
            //    new SwingJMenuBarCreator().createMenus(customMenu, swingMenuBar);
            //    frame.setJMenuBar(swingMenuBar);
            //}
            traverseChildren(newRoot);
            System.out.println(new ShadowMenu(getContext(), debugList));
            writeObject(newModList);
        }
    }
}