

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.measure.CurveFitter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class FRAP_Cell30 implements PlugIn {
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
//new WaitForUserDialog ("fe", "define exclusion ROI").show();
IJ.run(impFRAP2, "Analyze Particles...", "size=2.4-30 exclude clear overlay add stack");
IJ.selectWindow(impFRAPID);
CheckLastRoi();
CheckRoiLocation();
CheckRoiSize();
//new WaitForUserDialog ("Delete wrong ROI", "have a quick look & delete the ROI that aren't right").show();
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
	rm.runCommand(impFRAP,"Update");

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
String regex = "[a-z]";
Pattern pattern = Pattern.compile(regex);
ResultsTable rt = ResultsTable.getResultsTable();
double[] X= rt.getColumn("X");
double[] Y= rt.getColumn("Y");
double[] Slice= rt.getColumn("Slice");
CurveFitter cX = new CurveFitter(Slice,X);
CurveFitter cY = new CurveFitter(Slice,Y);
cX.doFit(CurveFitter.POLY3);
cY.doFit(CurveFitter.POLY3);

// this fetches the values of the curve fitting for the X values 
String x =cX.getResultString();
int a1 = x.indexOf("a =");
int b1 = x.indexOf("b =");
int c1 = x.indexOf("c =");
int d1 = x.indexOf("d =");
int t1 = x.length();

CharSequence ax = pattern.matcher(x.subSequence(a1+4,a1+15)).replaceAll(""); // this creates Charsequence that have has all non-capitalised letters taken out
CharSequence bx = pattern.matcher(x.subSequence(b1+4,b1+15)).replaceAll("");
CharSequence cx = pattern.matcher(x.subSequence(c1+4,c1+15)).replaceAll("");
CharSequence dx = pattern.matcher(x.subSequence(d1+4,t1)).replaceAll("");

double a12 = Double.parseDouble(String.valueOf(ax));
double b12 = Double.parseDouble(String.valueOf(bx));
double c12 = Double.parseDouble(String.valueOf(cx));
double d12 = Double.parseDouble(String.valueOf(dx));


// this fetches the values of the curve fitting for the Y values 
String y =cY.getResultString();
int a2 = y.indexOf("a =");
int b2 = y.indexOf("b =");
int c2 = y.indexOf("c =");
int d2 = y.indexOf("d =");
int t2 = y.length();// I have no idea why the +1 is necessary here, especially when the part above works perfectly without, maybe its not counted something here which should've been ?
CharSequence ay = pattern.matcher(y.subSequence(a2+4,a2+15)).replaceAll(""); // this creates Charsequence that have has all non-capitalised letters taken out
CharSequence by = pattern.matcher(y.subSequence(b2+4,b2+15)).replaceAll("");
CharSequence cy = pattern.matcher(y.subSequence(c2+4,c2+15)).replaceAll("");
CharSequence dy = pattern.matcher(y.subSequence(d2+4,t2)).replaceAll("");
new WaitForUserDialog ("t", " Results string of X is:"+ x + "  "
		+ "Results String of Y is : "+ y).show();
double a22 = Double.parseDouble(String.valueOf(ay));
double b22 = Double.parseDouble(String.valueOf(by));
double c22 = Double.parseDouble(String.valueOf(cy));
double d22 = Double.parseDouble(String.valueOf(dy));

new WaitForUserDialog ("t", "a = "+a22+" b= "+b22+" c= "+c22+ " d= "+ d22).show();
for (int i=1;i<=impFRAP.getNFrames();i++ ) {
double Xx = a12 + b12*i + (Math.pow(c12*i,2)) + (Math.pow(d12*i,3));
double Yy = a22 + b22*i + (Math.pow(c22*i,2)) + (Math.pow(d22*i,3));
// DON'T CREATE THEM, MOVE THEM!!!!!!!!!
}
}}