// TO DO:
// Show a pop up when there is a duplicate menu item.
// Build a shadow menu.

package menuarranger;

import net.imagej.ImageJ;
import org.scijava.command.Command;
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
import javax.swing.tree.TreeNode;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Plugin(type = Command.class, menuPath = "File>Arrange Menus")
public class MenuArranger implements Command {

    @Parameter
    private MenuService menuService;
    @Parameter
    private UIService uiService;
    @Parameter
    private ModuleService moduleService;
    @Parameter
    private PluginService pluginService;
    @Parameter
    private LogService logService;

    protected JFrame frame = null;
    private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Menu");
    private DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    public DefaultTreeModel customTreeModel = new DefaultTreeModel(treeRoot);
    public ShadowMenu customMenu = null;
    private JMenuBar swingMenuBar = new JMenuBar();
    private HashMap<String, org.scijava.UIDetails> menuMap = new HashMap<>();
    private HashMap<String, org.scijava.UIDetails> dupeMap = new HashMap<>();
    private HashMap<String, org.scijava.UIDetails> dupeMapExtended = new HashMap<>();

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

    private void traverseChildren (DefaultMutableTreeNode root) {
        // needs to use CustomMenuService ShadowMenu !!

        int children = root.getChildCount();
        if (root.isLeaf()) {
            TreeNode[] childPath = root.getPath(); // Get the path as an array.
            List<TreeNode> pathList;
            pathList = new ArrayList<>(Arrays.asList(childPath)); //Array to list is immutable so have to make a copy.
            //pathList.remove(0); // take out the "Menu" part so it matches the moduleService path formatting. REDUNDANT
            String key = pathList.get(pathList.size() - 1).toString();
            if (dupeMap.containsKey(key)){
                if (dupeMapExtended.containsKey(key)){
                    logService.info("Hey this one had three!");
                }
                else {
                    //options = {dupeMap.get(key).toString(), menuMap.get(key).toString()};
                    //uiService.showDialog("Error!", DialogPrompt.MessageType.QUESTION_MESSAGE);
                }

            }
            else {
                logService.info("Found a match for "+key);
                //logService.info(menuMap.get(key));
            }
        }
        else {
            for (int i = 0; i < children; i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i); // get child node
                traverseChildren(child);
            }

        }
    }

    private void makeNewMenu (DefaultTreeModel newTreeModel){
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) newTreeModel.getRoot();

        // I think this will need to come from the CustomMenuService
        //ShadowMenu newMenu = null;

        // Start by building a hashmap of current menuList
        List<ModuleInfo> modList = moduleService.getModules();
        List<PluginInfo<?>> plugList = pluginService.getPlugins();
         // Need a second map in case there are duplicate menu entry leaves

        // access all modules currently in service.
        for (ModuleInfo mod : modList) {
            if (mod.isVisible() && !mod.getMenuPath().isEmpty() && mod.getLocation() != null) {
                String key = mod.getMenuPath().getLeaf().toString();
                ModuleInfo oldValue = (ModuleInfo) menuMap.put(key, mod);
                if (oldValue != null && oldValue.getMenuPath() != mod.getMenuPath()) {
                    logService.warn("Map already contains this module key! At " + oldValue.getMenuPath());
                    logService.warn("New value is associated with " + mod.getMenuPath());
                    ModuleInfo olderValue = (ModuleInfo) dupeMap.put(key, oldValue);
                    if (olderValue != null && olderValue.getMenuPath() != oldValue.getMenuPath()) {
                        logService.warn("Wow! There are three of this menu entry! You suck at naming plugins.\n The third one " +
                                "is located at "+olderValue.getMenuPath());
                        dupeMapExtended.put(key,olderValue); // may have to capture this one too, if there are any with three!
                    }
                }
            }
        }
        // access all plugins currently in service.
        for (PluginInfo<?> pluginInfo : plugList) {
            if (pluginInfo.isVisible() && !pluginInfo.getMenuPath().isEmpty() && pluginInfo.getLocation() != null) {
                String key = pluginInfo.getMenuPath().getLeaf().toString();
                menuMap.put(pluginInfo.getMenuPath().getLeaf().toString(), pluginInfo);
                PluginInfo<?> oldValue = (PluginInfo<?>) menuMap.put(key, pluginInfo);
                if (oldValue != null && oldValue.getMenuPath() != pluginInfo.getMenuPath()) {
                    logService.warn("Map already contains this plugin key! At " + oldValue.getMenuPath());
                    logService.warn("New value is associated with " + pluginInfo.getMenuPath());
                    PluginInfo<?> olderValue = (PluginInfo<?>) dupeMap.put(key, oldValue);
                    if (olderValue != null && olderValue.getMenuPath() != oldValue.getMenuPath()) {
                        logService.warn("Wow! There are three of this menu entry! You suck at naming plugins.\n The third one " +
                                "is located at " + olderValue.getMenuPath());
                        dupeMapExtended.put(key, olderValue); // may have to capture this one too, if there are any with three!
                    }
                }
                //logService.info(plug.getMenuPath().getLeaf()); // This is a way to get a "name" for the command
                //logService.info(plug.getLocation()); // gives the raw .jar file, which is awesome.
            }
        }

        // Now iterate through the tree
        traverseChildren(rootNode);
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
            makeNewMenu(treeModel);

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

        ij.command().run(MenuArranger.class, false);

    }
}