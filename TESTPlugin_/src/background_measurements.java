

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter; 
import java.io.IOException;


public class background_measurements implements PlugIn {
	ImagePlus impFRAP;
	ImagePlus impOther;
	ImagePlus impFRAP2;
	ImagePlus impFRAP1;
	int impFRAPID;
	int impOtherID;
	int b = 0;
	int c = 1;
	int FrameN = 0;
	double u = 11;

	ArrayList<Integer> v = new ArrayList<Integer>(); // Creates an ArrayList object

public void run(String arg) {
	IJ.run("Bio-Formats Importer");
	new WaitForUserDialog ("Select Channels", "Click GFP").show();
	impFRAP = WindowManager.getCurrentImage();
	impFRAPID = impFRAP.getID();
		ResultsTable rt = ResultsTable.getResultsTable();
		IJ.setTool("Oval");
		IJ.run("Clear Results", "");
		IJ.run(impFRAP,"Enhance Contrast", "saturated=0.35");
		new WaitForUserDialog ("background", "Please use the oval tool to select an area as large as possible of the background, without cells").show();;
		GetOtherMeasurements();
		double [] Background = rt.getColumn("Mean");
		WriteToTxt(Background);
		
	}
	public void GetOtherMeasurements() {
		RoiManager rm = RoiManager.getRoiManager();
		rm.addRoi(impFRAP.getRoi());
		for (int a=1;a<=impFRAP.getNFrames(); a++) {
		rm.select(0);
		impFRAP.setSlice(a);
		IJ.run(impFRAP,"Measure","");
		}}

	public void WriteToTxt( double [] Background) {
		String DirectoryPath = impFRAP.getOriginalFileInfo().directory;
		String FileName = impFRAP.getOriginalFileInfo().fileName;
		String Name = DirectoryPath + "Background "+ FileName +".tsv";
		try {	
			FileWriter fw = new FileWriter (Name,true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(impFRAP.getShortTitle());
			bw.newLine();
			for (int x=0; x<Background.length;x++){
				bw.write("	 Background = 	 "+ Background[x] );
				bw.newLine();
			}
			bw.newLine();
			bw.write("v = " + v.toString());
			bw.close();
		}
		catch (IOException ex) {
		System.out.println("Error writing to file '" + Name +"'");	
		}
	}}