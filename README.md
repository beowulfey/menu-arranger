# menu-arranger
A menu reorganizer for FIJI and ImageJ

Consists of two parts: a menu service and a menu arranger command. 

The CustomMenuService is intended to take the place of DefaultMenuService, serving up the menu by adding all the Modules and creating the ShadowMenu for the root. 

The Arrange Menus command opens up a window with two JTrees; it allows you to drag the folders and modules you want into the new JTree, rearranging into the structure you desire. 

Your custom menu is saved as a file, and the next time you load ImageJ, it will use that to build the menus instead. 

It almost works, but not with Legacy mode. Absolutely riddled with bugs. Not recommended for use yet, but I'm going to keep trying to improve it. 
