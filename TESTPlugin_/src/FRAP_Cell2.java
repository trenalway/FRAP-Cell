

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class FRAP_Cell2 implements PlugIn {
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
public void run(String arg) {

	//new WaitForUserDialog ("Open Image", "choose image to open").show();
	IJ.run("Bio-Formats Importer");
	new WaitForUserDialog ("Select Channels", "Click GFP").show();
impFRAP = WindowManager.getCurrentImage();
impFRAPID = impFRAP.getID();
//IJ.run(impFRAP,"Enhance Contrast", "saturated = 0.35");
	new WaitForUserDialog ("Select other Channel", "Click H2B").show();
impOther = WindowManager.getCurrentImage();
impOtherID = impOther.getID();

impFRAP2=impOther.duplicate();
impFRAP2.show();
IJ.run(impFRAP2, "Make Binary", "method=Default background=Default calculate black");
IJ.run(impFRAP2, "Dilate", "stack");
IJ.run(impFRAP2, "Erode", "stack");
IJ.selectWindow(impOtherID);
IJ.run(impFRAP2, "Invert", "");
new WaitForUserDialog ("fe", "define exclusion ROI").show();
IJ.run(impFRAP2, "Analyze Particles...", "size=5-30 exclude clear overlay add stack");
IJ.selectWindow(impFRAPID);
CheckLastRoi();
CheckParticles();
CheckRoiSize();
}


public void CheckLastRoi() {
	RoiManager rm = RoiManager.getRoiManager();
if (Integer.parseInt(RoiManager.getRoiManager().getName(rm.getCount()-1).substring(2,4)) != impFRAP.getNFrames()){
	int t =rm.getCount()-1;
	rm.select(t);
	String RoiframEE = rm.getName(t).substring(4,14);
	rm.addRoi(null);
	rm.rename(t, "0090" + RoiframEE);
	rm.runCommand(impFRAP, "Sort");
}}

public void CheckParticles() {
	RoiManager rm = RoiManager.getRoiManager();
for (int a=1; a<=impFRAP.getNFrames(); a++) {
	WindowManager.getCurrentImage().setSlice(a);
	String Roiname = rm.getName(b);
	String Roiframe = Roiname.substring(2,4);
	String RoiframeE = Roiname.substring(4,14);
	int RoiframeNo = Integer.parseInt(Roiframe);
	String RoiframeP = ("");
	
	if (RoiframeNo == a) { 
		rm.select(b);
		WindowManager.getCurrentImage().setSlice(a);
		IJ.run(impFRAP, "Measure", "");
		b=b+1;
		c = 1;
		}
	
	if (a < 6) {
	rm.select(0);
	WindowManager.getCurrentImage().setSlice(a);
	IJ.run(impFRAP, "Measure", "");}
	
	// if there are multiple Roi in a single frame or no Roi for multiple frames 
	if ( a != RoiframeNo & a > 6) {
		// prep data
	rm.select(b-1);
	int d = b-1;
	if (d==0){
		int FrameN = (Integer.parseInt(rm.getName(1).substring(2,4)))-1;
		new WaitForUserDialog ("late detection", "this Roi is the first").show();
		int i = FrameN + 1; 
		// to account for less tha  10 values of i 
		if (i <10) {
			RoiframeP = ("0"+String.valueOf(i));
		}
		else 	{RoiframeP = String.valueOf(i);}
		
		rm.addRoi(null);
		rm.rename(b-1, "00"+ RoiframeP + RoiframeE);
		rm.runCommand(impFRAP, "Sort");
		rm.select(b);
		if (Integer.parseInt(rm.getName(b).substring(2,4)) != a) {
			new WaitForUserDialog ("double Roi in 1 frame", "there should be 2 Rois at frame "+a).show();
			rm.runCommand(impFRAP, "Sort");
			IJ.deleteRows(a-2, a-1);
			a = a-2;
			b=b-1;
		}
		else {
		WindowManager.getCurrentImage().setSlice(a);
		IJ.run(impFRAP, "Measure", "");
		c=c+1;
		b=b+1;
		}}
	else {
	int FrameN = Integer.parseInt(rm.getName(d).substring(2,4));
	int i = FrameN + 1; 
	// to account for less than  10 values of i 
	if (i <10) {
		RoiframeP = ("0"+String.valueOf(i));
	}
	else 	{RoiframeP = String.valueOf(i);}
	
	rm.addRoi(null);
	rm.rename(b-1, "00"+ RoiframeP + RoiframeE);
	rm.runCommand(impFRAP, "Sort");
	rm.select(b);
	if (Integer.parseInt(rm.getName(b).substring(2,4)) != a) {
		new WaitForUserDialog ("double Roi in 1 frame", "there should be 2 Rois at frame "+a).show();
		rm.runCommand(impFRAP, "Sort");
		IJ.deleteRows(a-2, a-1);
		a = a-2;
		b=b-1;
	}
	else {
	WindowManager.getCurrentImage().setSlice(a);
	IJ.run(impFRAP, "Measure", "");
	c=c+1;
	b=b+1;
	}}}
	
	// if the auto correction has failed, reset to earlier and try again with manual adjustment of error 
//	if (c > 1) { 
//		new WaitForUserDialog ("warning", "please correct the issue between frame "+ a + " and Roi " + RoiframeNo).show();
//	c=0;
//	rm.runCommand(impFRAP, "Sort");
//	IJ.deleteRows(a-3, a-1);
//	a= a-3;
//	b = b-4;
//	WindowManager.getCurrentImage().setSlice(a);
//	IJ.run(impFRAP, "Measure","");
//	b=b+1;
	}}


public void CheckRoiSize() {
	RoiManager rm = RoiManager.getRoiManager();
	// to check for Roi size
	for (int a=0; a <= impFRAP.getNFrames()-1; a++) {
		int z = a+1;
		if (ResultsTable.getResultsTable().getValue("Area", a)> 10  & a<6) {
				IJ.deleteRows (0,5);
				for (int x=1; x <=6; x++) {
//					if (ResultsTable.getResultsTable().getValue("Area", x)>10) {
//					rm.select(1);
//					IJ.run(impFRAP, "Scale...", "x=0.95 y=0.95 centered");
//					rm.runCommand(impFRAP, "Update");
					while (u>10) {
						rm.select(0);
						IJ.run(impFRAP, "Scale... ", "x=0.95 y=0.95 centered");
						rm.runCommand(impFRAP, "Update");
						rm.runCommand(impFRAP, "Sort");
						rm.select(0);
						IJ.run (impFRAP,"Measure","");
						u = ResultsTable.getResultsTable().getValue("Area", 84);
						IJ.deleteRows(84, 84);
					}
					
				rm.select(0);
				WindowManager.getCurrentImage().setSlice(x);
				IJ.run(impFRAP,"Measure", "");
				}	ResultsTable.getResultsTable().sort("Slice");}
				ResultsTable.getResultsTable().updateResults();
		if (ResultsTable.getResultsTable().getValue("Area", a)>10 & a>5) {	
		IJ.deleteRows(a, a);
		rm.select(z-6);
		IJ.run(impFRAP, "Scale... ", "x=0.95 y=0.95 centered");
		rm.runCommand(impFRAP, "Update");
		IJ.run(impFRAP, "Measure","");
		ResultsTable.getResultsTable().sort("Slice");
			
		if (ResultsTable.getResultsTable().getValue("Area", a) > 10) {
			a = a-1;
		}}}

		ResultsTable.getResultsTable().sort("Slice");
	}}
//		IJ.deleteRows((a-1),(a-1));
//		rm.select(b-1);
//		rm.runCommand(impFRAP,"Delete");
//		rm.select(b-1);
//		rm.addRoi(null);
//		rm.rename(b-1, "00"+ (RoiframeY) + RoiframeE);
//		rm.select(b-1);
//		WindowManager.getCurrentImage().setSlice(a);
//		IJ.run(impFRAP, "Measure", "");
//		rm.runCommand(impFRAP, "Sort");}
