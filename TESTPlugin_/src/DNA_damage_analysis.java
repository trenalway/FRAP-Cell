

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.measure.CurveFitter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileWriter; 
import java.io.IOException;
import java.awt.event.ActionListener;

public class DNA_damage_analysis implements PlugIn {
	ImagePlus impDamage; // creates ImagePlus object called impDamage which is used to identify the channel where the measurement
	//is needed
	ImagePlus impAnchor;// creates ImagePlus object called impDamage which is used to identify the channel used to anchor the 
	//bleach area
	ImagePlus impAnchor2;
	// creates ImagePlus object called impAnchor2 which is a copy of impAnchor. This will become the binary object on which 
	//the AnalyseParticle tool will work.
	ImagePlus impAnchor1;
	//creates ImagePlus object impAnchor1, a copy of impAnchor 
	int impDamageID;
	int impAnchorID;
	int b = 0;
	int c = 1;
	int FrameN = 0;
	double u = 11;
	int RightRoi = 0;
	int ROINum =0;

    ArrayList<Integer> v = new ArrayList<Integer>(); // Creates an ArrayList object

public void run(String arg) {

//new WaitForUserDialog ("Open Image", "choose image to open and split channels").show();
IJ.run("Bio-Formats Importer");
new WaitForUserDialog ("Select Channels", "Click the window needing measuring then press OK ").show();//select measured channel
impDamage = WindowManager.getCurrentImage();
impDamageID = impDamage.getID();
new WaitForUserDialog ("Select other Channel", "Click the window with the Anchor channel then press OK").show();//select channel which guid
impAnchor = WindowManager.getCurrentImage();
impAnchorID = impAnchor.getID();
impAnchor2=impAnchor.duplicate();
impAnchor2.show();
ArrayList<String>AP = UserInterface();

int Bl = (int)Double.parseDouble(AP.get(0));
IJ.setSlice(Bl-1);
IJ.run(impAnchor2,"Auto Threshold","method=[Try all] white");
impAnchor2.show();
IJ.run("","Duplicate...");
}
public ArrayList <String> UserInterface() {
	ActionListener clickRecorder = new Button_Click1();
	ArrayList<String> t = new ArrayList<String>(); // Creates an ArrayList object
	GenericDialog g = new GenericDialog("Variables");
	g.addMessage("bleach event");
	g.addNumericField ("Bleach frame", 6,0);
	g.showDialog();
	t.add(String.valueOf(g.getNextNumber()));
	return t;
}}