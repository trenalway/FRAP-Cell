

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.measure.CurveFitter;

public class FRAP_Cell28 implements PlugIn {
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
IJ.run(impFRAP2, "Analyze Particles...", "size=2.4-30 exclude clear overlay add stack");
IJ.selectWindow(impFRAPID);
CheckLastRoi();
CheckRoiLocation();
CheckRoiSize();
new WaitForUserDialog ("Delete wrong ROI", "have a quick look & delete the ROI that aren't right").show();
GetRoiMeasurements();
PlotRoiCoordinates();
}

public void CheckLastRoi() { // this will add a final ROI to the file if there isn't one. This is done so that down the line plugin works 
	// the second part of this function checks that there are no ROIs before frame 6, if there are, it gets rid of them
	RoiManager rm = RoiManager.getRoiManager();
	int fn = impFRAP.getNFrames();
	String frameNo = Integer.toString(fn);
if (Integer.parseInt(rm.getName(rm.getCount()-1).substring(2,4)) != impFRAP.getNFrames()){ // if the last ROI doesn't have the same frame as the number of frames in the file
	int t =rm.getCount()-1; // get the before last ROI 
	rm.select(t);// get the before last ROI 
	String RoiframEE = rm.getName(t).substring(4,14); //get the name of that ROI 
	rm.addRoi(null);
	rm.rename(t, "00" + frameNo + RoiframEE); // create a new ROI that is at the last frame of the file 
	rm.setPosition (fn);
	rm.runCommand(impFRAP, "Sort");
}
for (int v=0; v<=1;v++) {
	b=0;
if ((Integer.parseInt(rm.getName(b).substring(2,4)))<6) {
	rm.select(0);
	rm.runCommand(impFRAP,"Delete");
	v=v-1;}
else {v++;} //gets rid of any ROIs sooner than frame 6
rm.runCommand(impFRAP,"Update");}}

public void CheckRoiLocation() { // eliminates all ROI which are too far from the reference ROI
	ResultsTable rt = ResultsTable.getResultsTable();
	RoiManager rm = RoiManager.getRoiManager();

	new WaitForUserDialog("right ROI selection", "please select the first ROI that is in the right place").show();
	IJ.run(impFRAP,"Measure","");
	int q =0;
for (int a =0; a<=rm.getCount(); a++) {	
	rm.select(a);
	IJ.run(impFRAP,"Measure","");
	double ThatX = rt.getValue("X",q);
	double ThatY = rt.getValue("Y",q);
	double NewX = rt.getValue("X", a+1);
	double NewY = rt.getValue("Y", a+1);
	double DifX = Math.abs(NewX-ThatX);
	double DifY = Math.abs(NewY-ThatY);

	if (DifX>2.5 || DifY>2.5) {
	rm.select(a);
	rm.runCommand(impFRAP,"Delete");
	rt.deleteRow(a+1);
	a = a-1;
	q=q-1;}
	if(a>=1){q=q+1;}
	}
IJ.run("Clear Results", "");
}
public void CheckRoiSize() {
	ResultsTable rt = ResultsTable.getResultsTable();
	RoiManager rm = RoiManager.getRoiManager();
	rm.select(0);
	IJ.run(impFRAP,"Measure","");
	
	for (int z=1; z<=rm.getCount();z++) {
	int t = 0;
	rm.select(z);
	IJ.run(impFRAP,"Measure","");
	double e = rt.getValue("Area",0);
	double r = rt.getValue("Area", 1);
	
		while (r>e+0.2||r<e-0.2) {
			rt.deleteRows(1,1);
			rm.select(z);
			double w = Math.round((Math.sqrt(e/r))*100); // this rounds to the closest hundreth which is the best the scale tool can do
			w = w/100; // still need to figure out a way to stay away from O.99 & 1.01 because they don't work in the scale tool 
			if (w==0.99 ) {w=w-0.01;}
			if (w==1.01) {w=w+0.01;}
				if (t<5) {
				IJ.run(impFRAP, "Scale... ", "x="+w+" y="+w+" centered");
				rm.runCommand(impFRAP, "Update");
				IJ.run(impFRAP, "Measure","");
				rt.sort("Slice");
	
			r=rt.getValue("Area", 1);
			e=rt.getValue("Area", 0);
			rt.deleteRows(1,1);
			t = t+1;}
				if (t > 5) {
					new WaitForUserDialog("problem with scaling", "there is an issue with scaling, please scale it manually, the last scaling factor was "+w).show();
					rm.runCommand(impFRAP,"Update");
					IJ.run(impFRAP,"Measure","");
					rt.sort("Slice");
					
				}
		}
		t=0;
		if (r>e-0.2 & r<e+0.2) {
		rt.deleteRows(1, 1);	
		}
	}
	
	IJ.run("Clear Results","");}

public void GetRoiMeasurements(){
		ResultsTable rt = ResultsTable.getResultsTable();
		RoiManager rm = RoiManager.getRoiManager();
		b=0;
	for (int a=1; a<=impFRAP.getNFrames(); a++) { // for all frames 
		WindowManager.getCurrentImage().setSlice(a);
		String Roiname = rm.getName(b);
		String Roiframe = Roiname.substring(2,4);
		String RoiframeE = Roiname.substring(4,14);
		int RoiframeNo = Integer.parseInt(Roiframe);
		String RoiframeP = ("");

		for (int T=0; T<26; T++) {
			rm.select(T);
			if (Integer.parseInt(rm.getName(T).substring(2,4)) <6) {
			rm.runCommand(impFRAP,"Delete");}}
		
		if (RoiframeNo == a & a >=6) { //for rare situation where all goes well
			rm.select(b);
			WindowManager.getCurrentImage().setSlice(a);
			IJ.run(impFRAP, "Measure", "");
			b=b+1;
			c = 1;
			}
		
		if (a < 6) { // to measure intensity at original bleach point 
		rm.select(0);
		WindowManager.getCurrentImage().setSlice(a);
		IJ.run(impFRAP, "Measure", "");}
		
		
		if (a>6 & a!=RoiframeNo & b<1) {
		 new WaitForUserDialog ("choose first ROI", "it appears the first ROI might be wrong, please delete the wrong ROI then press OK").show();
		 rt.deleteRow(a-1);
		 a=a-1;
		}
		if (a>6 & a != RoiframeNo & b>=1) {
			double ThisX = rt.getValue("X",a-2);
			double ThisY = rt.getValue("Y",a-2);
			double LastX = rt.getValue("X",a-4);
			double LastY = rt.getValue("Y",a-4);
			double DifThisX = Math.abs(ThisX-LastX); 
			double DifThisY = Math.abs(ThisY-LastY);
			
		if (Integer.parseInt(rm.getName(b).substring(2,4))==Integer.parseInt(rm.getName(b-1).substring(2,4))) { //tests to see if there are 2 ROI in the same frame and deletes the wrong ROI
			rm.select(b);
			IJ.run(impFRAP,"Measure", "");
		rt.deleteRows(a-1, a-2);
		if (DifThisX>=2 || DifThisY>=2) { //tests to see if this b is far from the last trustworthy X & Y values
			rm.select(b);
			rm.runCommand(impFRAP,"Delete");
			b=b-2;
			a=a-3;
		}
			
		else {
			rt.deleteRow(a-3);
			rm.select(b-1);
			rm.runCommand(impFRAP, "Delete");
			b=b-3;
			a=a-4;
		}}
		
		if ( Integer.parseInt(rm.getName(b).substring(2,4))!=Integer.parseInt(rm.getName(b-1).substring(2,4))) { // should be able to deal with a situation where there are frames missing
		int FrameN = Integer.parseInt(rm.getName(b-1).substring(2,4));
		int i = FrameN + 1; 
		// to account for less than  10 values of i 
		if (i <10) {RoiframeP = ("0"+String.valueOf(i));}
		else 	{RoiframeP = String.valueOf(i);}
		rm.select(b-1);
		rm.addRoi(null);
		rm.rename(b-1, "00"+ RoiframeP + RoiframeE);
		rm.setPosition(i);
		rm.runCommand(impFRAP, "Sort");
		rm.select(b);
		IJ.run(impFRAP,"Measure","");
		b=b+1;
		}}}}	
public void PlotRoiCoordinates() {
ResultsTable rt = ResultsTable.getResultsTable();
double[] X= rt.getColumn("X");
double[] Y= rt.getColumn("Y");
double[] Slice= rt.getColumn("Slice");
Plot plot1 = new Plot ("X","X Axis", "Y Axis");
//plot1.add("X movement", Slice, X);
//plot1.show();
Plot plot2 = new Plot ("Y", "Slices", "Y Axis movement");
//plot2.add ("Y movement", Slice, Y);
//plot2.show();
CurveFitter cX = new CurveFitter(Slice,X);
CurveFitter cY = new CurveFitter(Slice,Y);
cX.doFit(CurveFitter.POLY3);
cY.doFit(CurveFitter.POLY3);
double[] pX = cX.getParams();
double[] pY = cY.getParams();

double[] XX= cX.getXPoints();
double[] XY= cX.getYPoints();
double[] YX= cY.getXPoints();
double[] YY= cY.getYPoints();
double rValueX = cX.getFitGoodness();
double rValueY = cY.getFitGoodness();
new WaitForUserDialog ("report","quality of fit for X "+rValueX+",quality of fit for Y "+rValueY).show();
plot1.add("X movement",XX,XY);
plot2.add("Y movement",YX,YY);
plot1.show();
plot2.show();
}}