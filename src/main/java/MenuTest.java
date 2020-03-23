/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     https://creativecommons.org/publicdomain/zero/1.0/
 */

import net.imagej.ImageJ;

import org.scijava.log.LogService;
import org.scijava.app.StatusService;
import org.scijava.menu.MenuService;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Managed to make this output the menu as text. It's a start. Click OK --> get current menu.
 * Want to associate each root with a HIDE option. Thinking three panels, heirarchy like file folders.
 * Want to be able to hide an move around plugins etc at will.
 * Will have to override the current menu building code... autopopulated. Tricky.
 */
@Plugin(type = Command.class, headless = true, menuPath = "Other>Menu Test")
public class MenuTest implements Command {
	@Parameter
	private LogService log;

	@Parameter
	private StatusService statusService;

	@Parameter
	private MenuService menuService;

	@Parameter(label = "Want to see the menu structure as it is right now?")
	private Boolean pboolean;

	// This part makes it so when the user clicks OK, it brings up an output window with the menu.
	// Need to figure out how to get it into an active window!
	@Parameter(label = "Results", type = ItemIO.OUTPUT)
	private String result;

	@Override
	public void run() {


		final StringBuilder sb = new StringBuilder();
		append(sb, "Menu root:");
		append(sb, String.valueOf(menuService.getMenu()));

		result = sb.toString();

	}


	private void append(final StringBuilder sb, final String s) {
		sb.append(s + "\n");
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		// Launch our "Hello World" command right away.
		ij.command().run(MenuTest.class, true);
	}

}
