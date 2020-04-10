/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package menuarranger;

import org.scijava.MenuPath;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.module.event.ModulesAddedEvent;
import org.scijava.module.event.ModulesRemovedEvent;
import org.scijava.module.event.ModulesUpdatedEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import java.io.*;
import java.util.*;

/**
 * Default service for keeping track of the application's menu structure.
 *
 * @author Curtis Rueden
 * @see ShadowMenu
 */
@Plugin(type = Service.class, priority = 10)
public class CustomMenuService extends AbstractService implements MenuService
{

    @Parameter
    private EventService eventService;

    @Parameter
    private ModuleService moduleService;

    /** Menu tree structures. There is one structure per menu root. */
    private HashMap<String, ShadowMenu> rootMenus;
    private HashMap<String, ShadowMenu> defaultMenu;
    private List<ArrayList<Object>> customMenuInfo;

    // -- MenuService methods --

    @Override
    public ShadowMenu getMenu(final String menuRoot) {
        return rootMenus().get(menuRoot);
    }

    public ShadowMenu getDefaultMenu() {
        try{
            ShadowMenu[] menu = defaultMenu.values().toArray(new ShadowMenu[0]);
            return menu[0];
        } catch (NullPointerException err){
            return null;
        }
    }

    // -- Event handlers --

    @EventHandler
    protected synchronized void onEvent(final ModulesAddedEvent event) {
        if (rootMenus == null) return; // menus not yet initialized
        System.out.println("onEvent Mods Added");
        addModules(event.getItems());
    }

    @EventHandler
    protected synchronized void onEvent(final ModulesRemovedEvent event) {
        if (rootMenus == null) return; // menus not yet initialized
        System.out.println("onEvent Mods Removed");
        for (final ShadowMenu menu : rootMenus().values()) {
            menu.removeAll(event.getItems());
        }
    }

    @EventHandler
    protected synchronized void onEvent(final ModulesUpdatedEvent event) {
        if (rootMenus == null) return; // menus not yet initialized
        for (final ShadowMenu menu : rootMenus().values()) {
            System.out.println("onEvent Mods Updated");
            menu.updateAll(event.getItems());
        }
    }

    // -- Helper methods --

    /**
     * Adds the given collection of modules to the menu data structure.
     * <p>
     * The menu data structure is created lazily via {@link #rootMenus()} if it
     * does not already exist. Note that this may result in a recursive call to
     * this method to populate the menus with the collection of modules currently
     * known by the {@link ModuleService}.
     * </p>
     */
    private synchronized void addModules(final Collection<ModuleInfo> items) {
        addModules(items, rootMenus());
    }

    /**
     * As {@link #addModules(Collection)} adding modules to the provided menu
     * root.
     */
    private synchronized void addModules(final Collection<ModuleInfo> items,
                                         final Map<String, ShadowMenu> rootMenu)
    {
        // categorize modules by menu root
        final HashMap<String, ArrayList<ModuleInfo>> modulesByMenuRoot =
                new HashMap<>();
        for (final ModuleInfo info : items) {
            final String menuRoot = info.getMenuRoot();
            ArrayList<ModuleInfo> modules = modulesByMenuRoot.get(menuRoot);
            if (modules == null) {
                modules = new ArrayList<>();
                modulesByMenuRoot.put(menuRoot, modules);
            }
            modules.add(info);
        }

        // process each menu root separately
        for (final String menuRoot : modulesByMenuRoot.keySet()) {
            final ArrayList<ModuleInfo> modules = modulesByMenuRoot.get(menuRoot);
            ShadowMenu menu = rootMenu.get(menuRoot);
            if (menu == null) {
                // new menu root: create new menu structure
                menu = new ShadowMenu(getContext(), modules);
                rootMenu.put(menuRoot, menu);
            }
            else {
                // existing menu root: add to menu structure
                menu.addAll(modules);
            }
        }

    }

    // Opens up a previous saved menu.
    private List<ArrayList<Object>> readSavedMenu(){
        try{
            FileInputStream f = new FileInputStream(new File("customMenuList.map"));
            ObjectInputStream o = new ObjectInputStream(f);
            customMenuInfo = (List<ArrayList<Object>>) o.readObject();
            System.out.println("Read in previous menu file");
            o.close();
            f.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return customMenuInfo;
    }

    private List<ModuleInfo> parseCustomMenu(List<ArrayList<Object>> input){
        List<ModuleInfo> cusMods = new ArrayList<>();
        HashMap<String, ModuleInfo> modMap = new HashMap<>();
        System.out.println("Compiling modules");
        final List<ModuleInfo> allModules = moduleService.getModules();
        for (ModuleInfo mod : allModules){
            modMap.put(mod.getDelegateClassName(),mod);
        }
        System.out.println("Successfully compiled");

        for (int i = 0; i<input.size();i++){
            ArrayList<Object> entry = input.get(i);
            String className = (String) entry.get(0);
            MenuPath path = new MenuPath((String) entry.get(1),",");
            path.getLeaf().setWeight((int) entry.get(2));
            if (modMap.containsKey(className)){
                ModuleInfo mod = modMap.get(className);
                mod.setMenuPath(path);
                cusMods.add(mod);
                System.out.println("Found entry for "+path);
            }
            else{
                System.out.println("Wasn't able to find for "+path);
            }
        }
        System.out.println("Rebuilt new menu!");
        return cusMods;
    }

    /**
     * Lazily creates the {@link #rootMenus} data structure.
     * <p>
     * Note that the data structure is initially populated with all modules
     * available from the {@link ModuleService}, which is accomplished via a call
     * to {@link #addModules(Collection)}, which calls {@link #rootMenus()}, which
     * can result in a level of recursion. This is intended.
     * </p>
     */
    private HashMap<String, ShadowMenu> rootMenus() {
        if (rootMenus == null) initRootMenus();
        System.out.println("INITIALIZING MENUS!!!!");
        return rootMenus;
    }

    private void initDefaultMenu() {
        final HashMap<String, ShadowMenu> map = new HashMap<>();
        final List<ModuleInfo> allModules = moduleService.getModules();
        addModules(allModules, map);
        defaultMenu = map;
        System.out.println("Default menu initialized");
    }



    /** Initializes {@link #rootMenus}. */
    private synchronized void initRootMenus() {
        if (rootMenus != null) return;
        final HashMap<String, ShadowMenu> map = new HashMap<>();
        final File customMenuList = new File("customMenuList.map");

        if (customMenuList.exists()){
            initDefaultMenu();
            customMenuInfo = readSavedMenu();
            List<ModuleInfo> customModules = parseCustomMenu(customMenuInfo);
            addModules(customModules, map);
        }
        else{
            final List<ModuleInfo> allModules = moduleService.getModules();
            addModules(allModules, map);
        }
        rootMenus = map;
        System.out.println("Loaded all modules");
    }

}
