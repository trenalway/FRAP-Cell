


import ij.gui.GenericDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;




public class Button_Click1 implements ActionListener {
    /**
     * Class which only function is to handle the button clicks
     */

    public void actionPerformed(ActionEvent event) {

        // Check from where the event comes
        Object source = event.getSource(); // returns the Button object
        String s = source.toString();

        // Do an action depending on the button clicked
        if (s.contains("label=Help")) {
        GenericDialog G1 = new GenericDialog("Help");
        G1.addMessage("for the thresholding techniques, you can look up the details of what they do on this website : ");
        G1.addMessage("https://imagej.net/plugins/auto-threshold");
        G1.addMessage("Overall I've found that: ");
        G1.addMessage("- For situations with 1 high intensity that's not of interest, Huang, Mean and Li tend to work best, with Triangle working also as long as they aren't close together");
        G1.addMessage("- For situations with overall low intensity, Default, Li, Otsu,");
        G1.addMessage("- For situations with extremely low fluorescence, Default, Huang & Triangle");
        G1.addMessage("Overall it's quite likely that multiple variations & rounds of 'Dilate', 'Erode' and 'Despeckle' will have to be used for better detection.");
        G1.showDialog();
        }
    }
}