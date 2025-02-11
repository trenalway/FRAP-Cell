

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class FRAP_Cell26 implements PlugIn {
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
CheckParticles();
CheckMoveDistance();
CheckRoiSize();
}

public void CheckLastRoi() {
	RoiManager rm = RoiManager.getRoiManager();
	int fn = impFRAP.getNFrames();
	String frameNo = Integer.toString(fn);
if (Integer.parseInt(rm.getName(rm.getCount()-1).substring(2,4)) != impFRAP.getNFrames()){
	int t =rm.getCount()-1;
	rm.select(t);
	String RoiframEE = rm.getName(t).substring(4,14);
	rm.addRoi(null);
	rm.rename(t, "00" + frameNo + RoiframEE);
	rm.setPosition (fn);
	rm.runCommand(impFRAP, "Sort");
}
for (int v=0; v<=1;v++) {
	b=0;
if ((Integer.parseInt(rm.getName(b).substring(2,4)))<6) {
	rm.select(0);
	rm.runCommand(impFRAP,"Delete");
	v=v-1;}
else {v++;}
}}

public void CheckParticles() {
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
	
	if (RoiframeNo == a & a >6) { //for rare situation where all goes well
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
		double ThisX = rt.getValue("X",a-1);
		double ThisY = rt.getValue("Y",a-1);
		double LastX = rt.getValue("X",a-3);
		double LastY = rt.getValue("Y",a-3);
		double DifThisX = Math.abs(ThisX-LastX); 
		double DifThisY = Math.abs(ThisY-LastY);
		
	if (Integer.parseInt(rm.getName(b).substring(2,4))==Integer.parseInt(rm.getName(b-1).substring(2,4))) { //tests to see if there are 2 ROI in the same frame and deletes the wrong ROI
		rm.select(b);
		IJ.run(impFRAP,"Measure", "");
	rt.deleteRows(a-1, a-2);
	if (DifThisX>=2 || DifThisY>=2) { //tests to see if this b is far from the last trustworth X & Y values
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

public void CheckRoiSize() {
	RoiManager rm = RoiManager.getRoiManager();
	ResultsTable rt = ResultsTable.getResultsTable();
	// to check for Roi size
	for (int a=0; a <= impFRAP.getNFrames()-1; a++) {
		if (rt.getValue("Area", a)> 10  & a<=5) {

					while (u>10) { // to fix the size of the initial ROI 
						IJ.deleteRows (0,5);
						rm.select(0);
						IJ.run(impFRAP, "Scale... ", "x=0.95 y=0.95 centered");
						rm.runCommand(impFRAP, "Update");
						rm.runCommand(impFRAP, "Sort");
						rm.select(0);
						IJ.run (impFRAP,"Measure","");
						u = rt.getValue("Area", rm.getCount()-1); //here u is supposed to be the area of the most recent measurement  
						IJ.deleteRows(rm.getCount()-1,rm.getCount()-1); // here we want to delete the last measurement because it's just to determine the new area
						rt.sort("Slice");
					}
for (int n=1; n<=6; n++) {
				rm.select(0);
				WindowManager.getCurrentImage().setSlice(n);
				IJ.run(impFRAP,"Measure", "");
				rt.sort("Slice");
	}}
		
		if (rt.getValue("Area", a)>10 & a>5) {	
		IJ.deleteRows(a, a);
		rm.select(a-5);
		IJ.run(impFRAP, "Scale... ", "x=0.95 y=0.95 centered");
		rm.runCommand(impFRAP, "Update");
		IJ.run(impFRAP, "Measure","");
		rt.sort("Slice");
			
		if (rt.getValue("Area", a) > 10) {
			a = a-1;
		}}}

		rt.sort("Slice");
	}

public void CheckMoveDistance() {
RoiManager rm = RoiManager.getRoiManager();
ResultsTable rt = ResultsTable.getResultsTable();
int e = 2; // the value of acceptable movement
for (int a = 0; a<=impFRAP.getNFrames()-1; a++) { //IMPORTANT, here the results table starts at 0, not at 1!!!!!!!
	int b = a-1; // the previous value in the results table
	int c = a-5; // the ROI corresponding to a (-6 because of the 6 frames it takes to start FRAP) 
	int d = a-6; // the ROI corresponding to b (-7 because of the 6 frames it takess to start FRAP) 
	double ax = rt.getValue("X",a); // the value at row a for X
	double ay = rt.getValue("Y",a); // the value at row a for Y 
	double bx = ax; //the value at row b(a-1) for X, but for now is the same as "a" because otherwise returns error when a=0
	double by = ay;//the value at row b(a-1) for Y, but for now is the same as "a" because otherwise returns error when a=0
	if (a>0) {	// if a = 0 then b which is a-1 doesn't exist
		bx = rt.getValue("X", b);// the value at row b (a-1) for X
		by = rt.getValue ("Y",b); //the value at row b for Y 
}
	double difx = Math.abs(ax-bx); //absolute value of the difference between ax and bx
	double dify = Math.abs(ay-by); // absolute value of the difference between ay & by 
	String ca =""; //for now the name of c is null because it the bleach spot hasn't appeared in frames 1-6
	String cb =""; //for now the name of c is null because it the bleach spot hasn't appeared in frames 1-6
	if (c>=0) {
	ca = rm.getName(c).substring(2,4); // the name of c
	cb = rm.getName(c).substring(4,14); // the name of c
}	
	if (difx >= 2 || dify>=2) {
		rt.deleteRow(a);
		rm.select(c);
		rm.runCommand(impFRAP,"Delete");
		rm.select(d);
		rm.addRoi(null);
		rm.rename(d,"00" + ca + cb);
		rm.setPosition(c);
		rm.runCommand(impFRAP,"Sort");
		rm.select(c);
		IJ.run(impFRAP,"Measure","");
		rt.sort("Slice");
		e = e+2;
	}
	else {e=2;}
}}}