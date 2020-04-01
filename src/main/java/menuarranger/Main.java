package menuarranger;

import net.imagej.ImageJ;


public class Main {

    public static void main( String... args ) {


        ImageJ ij = new net.imagej.ImageJ();
        ij.launch(args);

        ij.command().run(MenuArranger.class, true);
    }
}
