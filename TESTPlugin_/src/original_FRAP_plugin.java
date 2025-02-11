
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.io.FileInfo;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;

public class original_FRAP_plugin implements PlugIn{
	String filename;
	ImagePlus impFRAP;
	int impFRAPID;
	ImagePlus impOther;
	int impOtherID;
	String DirectoryPath;
	String ImageFilename;
	int howOftenShouldPluginPause;
	
	public void run(String arg) {
		
		IJ.run("Set Measurements...", "area mean min centroid center perimeter bounding shape redirect=None decimal=3");
		FileOpener();
		MeasureFRAP();
		IJ.run("Close All", "");
		ClearRoi();
		new WaitForUserDialog("Finished", "Plugin Finished").show();
		
	}

	public void FileOpener() {
		new WaitForUserDialog("Open Image", "Open the image and split channels").show();
		IJ.run("Bio-Formats Importer");
		
	//	String theAnswer = JOptionPane.showInputDialog("How many frames between pauses?","0");
	//	howOftenShouldPluginPause =  Integer.parseInt(theAnswer);
		
		
		
		//GET THE FILE NAME AND FILEPATH
		new WaitForUserDialog("Select Channels", "Click FRAP channel image and then OK").show();
		impFRAP =  WindowManager.getCurrentImage();
		FileInfo filedata = impFRAP.getOriginalFileInfo();
		String dirstr = filedata.directory; //Get File Path
		String fts = impFRAP.getTitle();
		DirectoryPath = dirstr;
		ImageFilename = fts.split("\\.",2)[0];
		impFRAPID = impFRAP.getID();
		IJ.run(impFRAP, "Enhance Contrast", "saturated=0.35"); //Autoscale image
		filename = impFRAP.getShortTitle();
		new WaitForUserDialog("Select Channels", "Click Non_FRAP channel image and then OK").show();
		impOther =  WindowManager.getCurrentImage();
		impOtherID = impOther.getID();
		IJ.run(impOther, "Enhance Contrast", "saturated=0.35"); //Autoscale image
		
	}
	
	public void MeasureFRAP() {
		String theMeasuredImage;
		IJ.selectWindow(impFRAPID);
		IJ.setTool("oval");
		ClearRoi();
		
		new WaitForUserDialog("Draw Circle", "Place a circle over the bleached region").show();
		RoiManager rm = RoiManager.getRoiManager();
		rm.addRoi(impFRAP.getRoi());
		int tempID = impFRAPID;
		theMeasuredImage = "frap";
		double Mean[] = new double [impFRAP.getNFrames()];
		Mean = MeasureROI(tempID,theMeasuredImage);
		ClearRoi();
		
		new WaitForUserDialog("Draw Circle", "Place a circle over a non bleached cell").show();
		rm = RoiManager.getRoiManager();
		rm.addRoi(impFRAP.getRoi());
		theMeasuredImage = "bleachcheck";
		double MeanBleach[] = new double [impFRAP.getNFrames()];
		MeanBleach = MeasureROI(tempID,theMeasuredImage);
		ClearRoi();
		
		new WaitForUserDialog("Draw Circle", "Place a circle over background area").show();
		rm = RoiManager.getRoiManager();
		rm.addRoi(impFRAP.getRoi());
		theMeasuredImage = "bkgrdCheck";
		double MeanBkGrd[] = new double [impFRAP.getNFrames()];
		MeanBkGrd = MeasureROI(tempID,theMeasuredImage);
		
		OutputText(Mean,MeanBleach,MeanBkGrd);
		
	}
	
	public double[] MeasureROI(int tempID, String theMeasuredImage) {
		RoiManager rm = RoiManager.getRoiManager();
		IJ.selectWindow(tempID);
		ImagePlus tempImp = WindowManager.getCurrentImage();
		double Mean[] = new double [tempImp.getNFrames()];
		int frameCounter=1;
		
		for(int a=1;a<impFRAP.getNFrames();a++) {
			rm.select(0);
	//		if(theMeasuredImage.equals("frap") && frameCounter==howOftenShouldPluginPause) {
	//			tempImp.setSlice(a);
	//			new WaitForUserDialog("Pause", "Move the ROI if required").show();
	//			frameCounter=1;
	//			rm.runCommand(impFRAP,"Update");
	//		}
			tempImp.setSlice(a);
			IJ.run(tempImp, "Measure", "");
			ResultsTable dt = Analyzer.getResultsTable();
			Mean[a] = dt.getValueAsDouble(1, 0);
			ClearResults();
			frameCounter++;
		}
		
		return Mean;
	}
	
	public void OutputText(double[] Mean, double[] MeanBleach, double [] MeanBkGrd) {
		String CreateName = DirectoryPath + ImageFilename + ".txt";
		//String CreateName = "C:/Temp/Results.txt";
		String FILE_NAME = CreateName;
    				
		try{
			FileWriter fileWriter = new FileWriter(FILE_NAME,true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(filename);
			bufferedWriter.newLine();
			for(int x=0;x<Mean.length;x++) {
				bufferedWriter.write("Cell Bleach Check = " + MeanBleach[x] + " Bleach = " + Mean[x] + " " + " Background = " + MeanBkGrd[x]);
				bufferedWriter.newLine();
			}
			
			bufferedWriter.close();

		}
		catch(IOException ex) {
            System.out.println(
                "Error writing to file '"
                + FILE_NAME + "'");
        }
		
	}
	
	
	private void ClearRoi(){
		ImagePlus imp=WindowManager.getCurrentImage();
		RoiManager rm = new RoiManager();    
		rm = RoiManager.getInstance();
		int numroi = rm.getCount();
		if (numroi>0){
			rm.runCommand(imp,"Deselect");
			rm.runCommand(imp,"Delete");
		}
	}
	
	private void ClearResults(){
		ResultsTable emptyrt = new ResultsTable();	
		emptyrt = Analyzer.getResultsTable();
		int valnums = emptyrt.getCounter();
		for(int a=0;a<valnums;a++){
			IJ.deleteRows(0, a);
		}
	}
	
}
