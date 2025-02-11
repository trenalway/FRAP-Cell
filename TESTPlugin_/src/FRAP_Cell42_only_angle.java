

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



public class FRAP_Cell42_only_angle implements PlugIn {
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
	int RightRoi = 0;
	int ROINum =0;

	ArrayList<Integer> v = new ArrayList<Integer>(); // Creates an ArrayList object

public void run(String arg) {

//new WaitForUserDialog ("Open Image", "choose image to open and split channels").show();
IJ.run("Bio-Formats Importer");
new WaitForUserDialog ("Select Channels", "Click GFP").show();
impFRAP = WindowManager.getCurrentImage();
impFRAPID = impFRAP.getID();
new WaitForUserDialog ("Select other Channel", "Click H2B").show();
impOther = WindowManager.getCurrentImage();
impOtherID = impOther.getID();
impFRAP2=impOther.duplicate();
impFRAP2.show();

ArrayList<String>AP = UserInterface();
String Thr = AP.get(0);
String APs =AP.get(1);
String APh = AP.get(2);
int Bl = (int)Double.parseDouble(AP.get(3));
String Sc = AP.get(4);
double Dis = Double.parseDouble(AP.get(5));
//double FerDif = Double.parseDouble(AP.get(6));
double FerAngDif = Double.parseDouble(AP.get(7));
double AcceptLostFrames = Double.parseDouble(AP.get(8));
double Scal = Double.parseDouble(AP.get(9));

//new WaitForUserDialog("Threshold technique is " + Thr).show();
IJ.run(impFRAP2, "Make Binary", "method="+Thr+" background=Default calculate black");
IJ.run(impFRAP2, "Dilate", "stack");
IJ.run(impFRAP2, "Erode", "stack");
IJ.selectWindow(impOtherID);
IJ.run(impFRAP2, "Invert", "");
impFRAP2.setSlice(Bl);
IJ.setTool("Freehand");

new WaitForUserDialog (null, "If necessary, define ROI in which the Analyse particle tool should be run,"+ "\n"
		+ " the rest of the image will be deleted from the b&w frame,"+"\n"
		+ " feel free to adjust image using despeckle, erode or dilate tools").show();
IJ.setBackgroundColor(0,0, 0);
//RoiManager rm = RoiManager.getRoiManager();
Roi roi = impFRAP2.getRoi();

if (roi == null) {
	IJ.run (impFRAP2,"Select All","");
	IJ.run(impFRAP2, "Scale... ", "x="+Sc+" y="+Sc+" centered");
	}
IJ.run(impFRAP2,"Clear Outside", "stack");
IJ.run(impFRAP2, "Select None", "");
IJ.run(impFRAP2, "Analyze Particles...", "size="+APs+"-"+APh+" exclude clear overlay add stack");
IJ.selectWindow(impFRAPID);
CheckLastRoi(Bl);
new WaitForUserDialog("right ROI selection", "please select the first ROI that is in the right place").show();
CheckRoiLocation(Dis);
CheckRoiSize(Dis, Scal, FerAngDif, AcceptLostFrames);
new WaitForUserDialog ("Delete wrong ROI", "have a quick look & delete the ROI that aren't right").show();
CheckLastRoi(Bl); //does not report the last ROI as missing in the V array list, need to change that. 
GetRoiMeasurements(Bl);
GetFinalMeasurements(Bl);
ExtractData();
IJ.run("Close All","");
}

public void CheckLastRoi(int Bl) { // this will add a final ROI to the file if there isn't one. This is done so that down the line plugin works 
	// the second part of this function checks that there are no ROIs before frame 6, if there are, it gets rid of them
	RoiManager rm = RoiManager.getRoiManager();
	int fn = impFRAP.getNFrames();
	String frameNo = Integer.toString(fn);
if (Integer.parseInt(rm.getName(rm.getCount()-1).substring(1,4)) != impFRAP.getNFrames()){ // if the last ROI doesn't have the same frame as the number of frames in the file
	int t =rm.getCount()-1; // get the last ROI 
	rm.select(t);// get the last ROI (which will then become the before last roi)
	String RoiframEE = rm.getName(t).substring(4,14); //get the name of that ROI 
	rm.addRoi(null);
	if (fn<100) {
		rm.rename(t, "00" + frameNo + RoiframEE); // create a new ROI that is at the last frame of the file 
	}
	if (fn>100) {
		rm.rename(t,"0" + frameNo + RoiframEE);
	}
	rm.setPosition (fn);
	rm.runCommand(impFRAP, "Sort");
}
for (int v=0; v<=1;v++) {
	b=0;
if ((Integer.parseInt(rm.getName(b).substring(2,4)))<Bl) {
	rm.select(0);
	rm.runCommand(impFRAP,"Delete");
	v=v-1;}
else {v++;} //gets rid of any ROIs sooner than frame 6
rm.runCommand(impFRAP,"Update");}}

public void CheckRoiLocation(double Dis) { // eliminates all ROI which are too far from the reference ROI
	ResultsTable rt = ResultsTable.getResultsTable();
	RoiManager rm = RoiManager.getRoiManager();
	Roi RightRo = WindowManager.getCurrentImage().getRoi();
	String m = RightRo.getName();
	int RightRoi = rm.getIndex(m);
	rm.select(RightRoi);
	IJ.run(impFRAP,"Measure","");
	int q =0;
	int t =0;
for (int a =0; a<rm.getCount(); a++) {	
	rm.select(a-1);
	String AN1Name = rm.getName().substring(0,4);
	rm.select(a);
	String AName = rm.getName();
	IJ.run(impFRAP,"Measure","");
	double ThatX = rt.getValue("X",q);
	double ThatY = rt.getValue("Y",q);
	double NewX = rt.getValue("X", a+1);
	double NewY = rt.getValue("Y", a+1);
	double LengthX = Math.abs(NewX-ThatX);
	double LengthY = Math.abs(NewY-ThatY);
	double Dif = Math.sqrt(Math.pow(LengthX,2)+Math.pow(LengthY, 2));

	if (Dif > Dis) {
	rm.select(a);
	rm.runCommand(impFRAP,"Delete");
	rt.deleteRow(a+1);
	a = a-1;
	q=q-1;
	t=1;
	}
	if (AN1Name == AName && t==0) {//triggers if Dif > Dis and it's in the same frame as the previous frame, essentially checks which of the last two frames is most trustworthy 
		// deletes the furthest ROI if it's in the present ROI 
	double LastX = rt.getValue("X",q-1);
	double LastY = rt.getValue("Y",q-1);
	double LengthX1 = Math.abs(ThatX-LastX);
	double LengthY1 = Math.abs(ThatY-LastY);
	double Dif2 = Math.sqrt(Math.pow(LengthX1,2)+Math.pow(LengthY1,2));
	boolean Difs = Dif>Dif2;
	if (Difs==true) {
		rm.runCommand(impFRAP,"Delete");
		rt.deleteRow(a+1);
		a=a-1;
		q=q-1;
	}
	}
	if(a>=1){
		q=q+1;
		}
	if (q<0){
		q=0;
		}
	} // the prior part gets rid of the Roi which are further than 2.5 microns from the previous Roi (which is seen as being a correct Roi) 
IJ.run("Clear Results", "");
for (int b =0; b<rm.getCount();b++) { // tries to get all ROIs to be elliptical 
	rm.select(b); // get ROI of interest
	IJ.run(impFRAP,"Fit Ellipse", ""); // fit this region to an ellipse 
	impFRAP.getRoi().setPosition(Integer.parseInt(rm.getName(b).substring(1,4))); // potential area why I've got my ROI going to 1 & smaller numbers, in theoryn this takes the selected ROI and 
	rm.addRoi(impFRAP.getRoi()); // make new ROI, (potentially elliptical
	rm.select(b);
	rm.runCommand(impFRAP,"Delete");
	rm.runCommand(impFRAP,"Sort");}
q =0;
for (int a =0; a<=rm.getCount(); a++) {	// check again to take out the Roi which have moved too much since 
	rm.select(0);
	IJ.run(impFRAP, "Measure", "");
	rm.select(a);
	IJ.run(impFRAP,"Measure","");
	double ThatX = rt.getValue("X",q);
	double ThatY = rt.getValue("Y",q);
	double NewX = rt.getValue("X", a);
	double NewY = rt.getValue("Y", a+1);
	double LengthX = Math.abs(NewX-ThatX);
	double LengthY = Math.abs(NewY-ThatY);
	double Dif = Math.sqrt(Math.pow(LengthX,2)+Math.pow(LengthY, 2));


	if (Dif>Dis) {
	rm.select(a);
	rm.runCommand(impFRAP,"Delete");
	rt.deleteRow(a+1);
	a = a-1;
	q=q-1;}
	if(a>=1){q=q+1;}
	}

IJ.run("Clear Results", "");
}
public void CheckRoiSize(double Dis,  double Scal, double FerAngDif, double AcceptLostFrames) {
	double w=0;
	ResultsTable rt = ResultsTable.getResultsTable();
	RoiManager rm = RoiManager.getRoiManager();
		rm.select(RightRoi);
		rm.scale(Scal, Scal, true);
		rm.select(1);
		rm.select(RightRoi);
		IJ.run(impFRAP,"Measure","");
	
	for (int z=0; z<=rm.getCount();z++) {
	int t = 0;
	rm.select(z);
	IJ.run(impFRAP,"Measure","");
	double e = rt.getValue("Area",0);
	double r = rt.getValue("Area", 1);
	
		while (r>e+0.2||r<e-0.2 ) { // microns squared 
			rt.deleteRows(1,1);
			rm.select(z);
			w = Math.round((Math.sqrt(e/r))*100); // this rounds to the closest hundreth which is the best the scale tool can do
			w = w/100; // still need to figure out a way to stay away from O.99 & 1.01 because they don't work in the scale tool 
			if (w==0.99 ) {w=w-0.01;}
			if (w==1.01) {w=w+0.01;}
			if (t<=5) {
				IJ.run(impFRAP, "Scale... ", "x="+w+" y="+w+" centered");
				rm.runCommand(impFRAP, "Update");
				IJ.run(impFRAP, "Measure","");
				rt.sort("Slice");
	
				r=rt.getValue("Area", 1);
				e=rt.getValue("Area", 0);
				rt.deleteRows(1,1);
				t = t+1;
			}
			if (t > 5) {
					new WaitForUserDialog("problem with scaling", "there is an issue with scaling, please scale it manually, the last scaling factor was "+w).show();
					rm.runCommand(impFRAP,"Update");
					IJ.run(impFRAP,"Measure","");
					rt.sort("Slice");
					
				}
			if (w <=1.02 && w>=0.98) {r=e;}
		}
		t=0;
		if (r>e-0.2 & r<e+0.2) {
			rt.deleteRows(1, 1);	
		}
	}
	int q=0;
	int t=0;

	IJ.run("Clear Results","");
	rm.select(0);
	IJ.run(impFRAP, "Measure","");
	ROINum = rm.getCount();
	int p = 0;
	for (int m = 0; m<ROINum;m++) {	// checks again to delete Roi which are now too far from the others 

		//if (m<=ROINum) {
			
		rm.select(m);
		IJ.run(impFRAP,"Measure","");
		double ThatX = rt.getValue("X",q);
		double ThatY = rt.getValue("Y",q);
		double NewX = rt.getValue("X", m+1);
		double NewY = rt.getValue("Y", m+1);
		//double Feret = rt.getValue("Feret", q);
		double FeretAng = rt.getValue("FeretAngle",q);
		//double Feret2 = rt.getValue("Feret",m+1);
		double FeretAng2 =rt.getValue("FeretAngle", m+1);		
		double LengthX = Math.abs(NewX-ThatX);
		double LengthY = Math.abs(NewY-ThatY);
		double Dif = Math.sqrt(Math.pow(LengthX,2)+Math.pow(LengthY, 2));
		if (Dif > Dis) {
			rm.select(m);
			rm.runCommand(impFRAP,"Delete");
			rt.deleteRow(m+1);
			m = m-1;
			q=q-1;
			}
		if(m>=1){q=q+1;}
		if (q<0){q=0;}
//		if (Math.abs(Feret-Feret2)>(Feret*FerDif)) {
//			rm.select(m);
//			rm.runCommand(impFRAP,"Delete");
//			rt.deleteRow(m+1);
//			m=m-1;
//			q=q-1;
//			t++;
//			p++;
//			}
		if (Math.abs(FeretAng-FeretAng2)>FerAngDif ) {	
			rm.select(m);
			rm.runCommand(impFRAP,"Delete");
			rt.deleteRow(m+1);
			m=m-1;
			q=q-1;
			t++;
			p++;
			}
		if ( Math.abs(FeretAng-FeretAng2)<FerAngDif) {
			p=0;
			}
		if (p>=AcceptLostFrames) {
			new WaitForUserDialog ("There's a gap of 5 frames here which has no ROI because the feret value is too long, it's causing an issue, please adjust feret distance or angle ").show();
			//System.exit(1);		
		}//}
		ROINum = rm.getCount();
		}
	String T1 = String.valueOf(t);
	new WaitForUserDialog ("report", T1).show();
	}

public void GetRoiMeasurements(int Bl){
		ResultsTable rt = ResultsTable.getResultsTable();
		RoiManager rm = RoiManager.getRoiManager();
		b=0;
		IJ.run("Clear Results","");
	
	for (int a=1; a<=impFRAP.getNFrames(); a++) { // for all frames 
		WindowManager.getCurrentImage().setSlice(a);
		String Roiname = rm.getName(b);
		String Roiframe = Roiname.substring(1,4);
		String RoiframeE = Roiname.substring(4,14);
		int RoiframeNo = Integer.parseInt(Roiframe);
		String RoiframeP = ("");
		if (RoiframeNo == a & a >=Bl) { //for rare situation where all goes well
			rm.select(b);
			WindowManager.getCurrentImage().setSlice(a);
			IJ.run(impFRAP, "Measure", "");
			b=b+1;
			c = 1;
			}
		
		if (a < Bl) { // to measure intensity at original bleach point 
		rm.select(0);
		WindowManager.getCurrentImage().setSlice(a);
		IJ.run(impFRAP, "Measure", "");
		}
		
		if (a==Bl & a!= RoiframeNo) {
			new WaitForUserDialog ("choose first ROI", "it looks like your first ROI isn't on your selected first frame" + "\r\n" + "this could be due to a detection issue or the bleach frame value being inserted wrong"+ "\r\n" + "please select a ROI you think is in the right place").show();
			Roi t = WindowManager.getCurrentImage().getRoi();
			String t1 = t.getName();
			int m = rm.getIndex(t1);
			rm.addRoi(null);
			if (Bl<10) {
				rm.rename(m, "000" + String.valueOf(Bl) + RoiframeE);
			}
			if (Bl>10 & Bl<100) {
				rm.rename(m,"00" + String.valueOf(Bl) + RoiframeE);	
			}
			if (Bl>100) {
				rm.rename(m,"0"+String.valueOf(Bl)+ RoiframeE);
			}
			rm.setPosition(Bl);
			rm.runCommand(impFRAP, "Sort");
			rm.select(b);
			rt.deleteRow(a-1);
			a=a-1;
		}
	
		if (a>Bl & a!=RoiframeNo & b<1) {
			new WaitForUserDialog ("choose first ROI", "it appears the first ROI might be wrong, please select the ROI you want to duplicate to begin again").show();
			Roi t = WindowManager.getCurrentImage().getRoi();
			String t1 = t.getName();
			int m = rm.getIndex(t1);
			rm.addRoi(null);
			if (Bl<10) {
				rm.rename(m, "000" + String.valueOf(Bl) + RoiframeE);
			}
			if (Bl>10 & Bl<100) {
				rm.rename(m,"00" + String.valueOf(Bl) + RoiframeE);	
			}
			if (Bl>100) {
				rm.rename(m,"0"+String.valueOf(Bl)+ RoiframeE);
			}
			rm.setPosition(Bl);
			rm.runCommand(impFRAP, "Sort");
			rm.select(b);
		 rt.deleteRow(a-1);
		 a=a-1;
		}
		
		if (a>Bl & a != RoiframeNo & b>=1) {
			double ThisX = rt.getValue("X",a-2);
			double ThisY = rt.getValue("Y",a-2);
			double LastX = rt.getValue("X",a-4);
			double LastY = rt.getValue("Y",a-4);
			double DifThisX = Math.abs(ThisX-LastX); 
			double DifThisY = Math.abs(ThisY-LastY);
			
			if (Integer.parseInt(rm.getName(b).substring(1,4))==Integer.parseInt(rm.getName(b-1).substring(1,4))) { //tests to see if there are 2 ROI in the same frame and deletes the wrong ROI
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
		
		if (RoiframeNo!=Integer.parseInt(rm.getName(b-1).substring(1,4))) { // should be able to deal with a situation where there are frames missing
		int FrameN = Integer.parseInt(rm.getName(b-1).substring(1,4));
		int i = FrameN + 1; 
		// to account for less than  10 values of i 
		if (i <10) {RoiframeP = ("000"+String.valueOf(i));}
		if (i>=10 & i<100 ) {RoiframeP = ("00" + String.valueOf(i));}
		if (i>=100 & i<1000) {RoiframeP = ("0"+ String.valueOf(i));}
		rm.select(b-1);
		rm.addRoi(null);
		rm.rename(b-1,  RoiframeP + RoiframeE);
		rm.setPosition(i);
		rm.runCommand(impFRAP, "Sort");
		rm.select(b);
		IJ.run(impFRAP,"Measure","");
		v.add(rm.getSliceNumber(rm.getName(b))); 
		// issue here "java.lang.NumberFormatException: For input string: "00-"
		//at java.base/jdk.internal.math.FloatingDecimal.readJavaFormatString(FloatingDecimal.java:2054)
		//at java.base/jdk.internal.math.FloatingDecimal.parseDouble(FloatingDecimal.java:110)
		//at java.base/java.lang.Double.parseDouble(Double.java:651)
		//at FRAP_Cell33.GetRoiMeasurements(FRAP_Cell33.java:303)
		// at FRAP_Cell33.run(FRAP_Cell33.java:56)"
		
		//Resolved issue by changing  v.add( Double.parseDouble(rm.getName(b).substring(1,4))); to a simple rm.getSliceNumber
		// Not sure if the tool is right but seems to be working. 
		// As I understand, the issue was that the parseDouble function was having issues working with a string starting with 00
		b=b+1;
		// this creates an array of all the ROI which have been filled in where a frame had no ROI 
		}}}
	PlotRoiCoordinates (v);
	
	}	
public void PlotRoiCoordinates(ArrayList<Integer> v) {
String regex = "[a-z]";
Pattern pattern = Pattern.compile(regex);
ResultsTable rt = ResultsTable.getResultsTable();
RoiManager rm = RoiManager.getRoiManager();
double[] X= rt.getColumn("X");
double[] Y= rt.getColumn("Y");
double[] Slice= rt.getColumn("Slice");
CurveFitter cX = new CurveFitter(Slice,X);
CurveFitter cY = new CurveFitter(Slice,Y);
cX.doFit(CurveFitter.POLY3);
cY.doFit(CurveFitter.POLY3);
//cX.getPlot().show();
//cY.getPlot().show();

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
int t2 = y.length();
CharSequence ay = pattern.matcher(y.subSequence(a2+4,a2+15)).replaceAll(""); // this creates CharSequences that have has all non-capitalised letters taken out
CharSequence by = pattern.matcher(y.subSequence(b2+4,b2+15)).replaceAll("");
CharSequence cy = pattern.matcher(y.subSequence(c2+4,c2+15)).replaceAll("");
CharSequence dy = pattern.matcher(y.subSequence(d2+4,t2)).replaceAll("");

double a22 = Double.parseDouble(String.valueOf(ay));
double b22 = Double.parseDouble(String.valueOf(by));
double c22 = Double.parseDouble(String.valueOf(cy));
double d22 = Double.parseDouble(String.valueOf(dy));
// then I want to generate the points where the Rois SHOULD be based on the curve and the equation that describes it, which is what I do here. 
for (int i=0;i<v.size();i++ ) { // this uses the ROI array generated above of all the newly created ROI, which were added to compensate for a ROI missing in a particular frame 
	// to move these ROI to where they should be based on the curve fitting 
double Xx = a12 + b12*v.get(i) + (Math.pow(c12*v.get(i),2)) + (Math.pow(d12*v.get(i),3));
double Yy = a22 + b22*v.get(i) + (Math.pow(c22*v.get(i),2)) + (Math.pow(d22*v.get(i),3));
 double X1 = rt.getValue("X", i);
 double Y1 = rt.getValue("Y", i);
 double TVX = Xx-X1;
 double TVY = Yy-Y1;
 rm.select(i);
 rm.translate(TVX, TVY);
 rm.runCommand("Sort");
 
}}
public void ExtractData() {
	RoiManager rm = RoiManager.getRoiManager();
	ResultsTable rt = ResultsTable.getResultsTable();
	double[] BleachSpot= rt.getColumn("Mean");
	IJ.setTool("Oval");
	IJ.run("Clear Results", "");
	rm.runCommand(impFRAP,"Deselect");
	rm.runCommand(impFRAP ,"Delete");
	IJ.run(impFRAP,"Enhance Contrast", "saturated=0.35");
	new WaitForUserDialog ("background", "Please use the oval tool to select an area as large as possible of the background, without cells").show();;
	GetOtherMeasurements();
	double [] Background = rt.getColumn("Mean");
	IJ.run("Clear Results", "");
	rm.runCommand(impFRAP,"Deselect");
	rm.runCommand(impFRAP ,"Delete");
	
	new WaitForUserDialog ("PhotoBleaching", "Please use the oval tool to select an area as large as possible of a non-bleached cell").show();
	GetOtherMeasurements();
	double [] Photobleaching = rt.getColumn ("Mean");
	WriteToTxt(BleachSpot, Background, Photobleaching);
	
}

public void GetOtherMeasurements() {
	RoiManager rm = RoiManager.getRoiManager();
	rm.addRoi(impFRAP.getRoi());
	for (int a=1;a<=impFRAP.getNFrames(); a++) {
	rm.select(0);
	impFRAP.setSlice(a);
	IJ.run(impFRAP,"Measure","");
	}}

public void GetFinalMeasurements (int Bl) {
	RoiManager rm = RoiManager.getRoiManager();
	b=0;
	//IJ.saveAs("Results", "C:/Users/hugov/OneDrive - University of Edinburgh/university/year 5/Results0.csv");
	IJ.run("Clear Results","");
for (int a=1; a<=impFRAP.getNFrames(); a++) { // for all frames 
	impFRAP.setSlice(a);
	String Roiname = rm.getName(b);
	String Roiframe = Roiname.substring(1,4);
	int RoiframeNo = Integer.parseInt(Roiframe);
	if (RoiframeNo == a & a >=Bl) { //for rare situation where all goes well
		rm.select(b);
		impFRAP.setSlice(a);
		IJ.run(impFRAP, "Measure", "");
		b=b+1;
		c = 1;
		}
	
	if (a < Bl) { // to measure intensity at original bleach point 
	rm.select(0);
	impFRAP.setSlice(a);
	IJ.run(impFRAP, "Measure", "");}}
}

public void WriteToTxt(double [] BleachSpot, double [] Background, double[] PhotoBleaching) {
	String DirectoryPath = impFRAP.getOriginalFileInfo().directory;
	String FileName = impFRAP.getOriginalFileInfo().fileName;
	String Name = DirectoryPath + FileName +".tsv";
	try {	
		Name = DirectoryPath + FileName +".tsv";
		FileWriter fw = new FileWriter (Name,true);
		BufferedWriter bw = new BufferedWriter (fw);
		bw.write(impFRAP.getShortTitle());
		bw.newLine();
		for (int x=0; x<BleachSpot.length;x++){
			bw.write("	Bleach spot  = 	" + BleachSpot[x] + "	 Background = 	 "+ Background[x] + " 	 Photo-Bleaching = 	 "+ PhotoBleaching[x] );
			bw.newLine();
		}
		bw.newLine();
		bw.write("v = " + v.toString());
		bw.close();
	}
	catch (IOException ex) {
	System.out.println("Error writing to file '" + Name +"'");	
	}
}

//public boolean makeYesNoDialog (String title, String message) {
//	GenericDialog s = new GenericDialog (title);
//	s.addMessage(message);
//	s.enableYesNoCancel();
//	s.hideCancelButton();
//	s.showDialog();
//	return s.wasOKed();
//}
public ArrayList <String> UserInterface() {
	ActionListener clickRecorder = new Button_Click1();
	ArrayList<String> t = new ArrayList<String>(); // Creates an ArrayList object
	GenericDialog g = new GenericDialog("Variables");
	g.addMessage("Thresholding technique");
	String [] arr0 = new String[] {"Default","Huang","Intermodes","IsoData","IJ_IsoData","Li","MaxEntropy","Mean","MinError","Minimum","Moments","Otsu","Percentile",
			"RenyiEntropy","Shanbhag","Triangle","Yen"};
	g.addChoice("choose a thresholding technique (generally use default)", arr0, "Default");
	g.addMessage("Analyse Particle detection range (default is 2.4 - 30 µm^2)");
	g.addNumericField("Low bracket",2.4,2);
	g.addNumericField("High bracket", 30,1);
	g.addMessage ("Bleach Frame (default is 6)");
	g.addNumericField("Bleach frame", 6,0);
	g.addMessage("cropping of field if no ROI is selected upon particle analysis");
	g.addNumericField("percentage of scaling", 0.95,2);
	g.addMessage("acceptable distance moved frame by frame (microns)");
	g.addNumericField("distance", 2.5,1);
	g.addMessage("decide what percentage of the feret value you want to have as the cutoff point (Feret value is the longest distance"
			+ "between any 2 points of the elipse)");
	g.addNumericField("cutoff point ", 0.25,2);
	g.addMessage("decide how much of a change in angle you find acceptable from frame to frame");
	g.addNumericField ("acceptable angle",90,2);
	g.addMessage("How many sequential deleted frames are acceptable when filtering with Feret values?");
	g.addNumericField("acceptable number of lost frames", 5,1);
	g.addMessage("what scaling factor do you want to apply to the ROI to ensure that the bleach spot is detected in the right way?");
	g.addNumericField("scaling factor",0.8,2);
	g.addButton("Help", clickRecorder);
	g.showDialog();

	t.add(g.getNextChoice());
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	t.add(String.valueOf(g.getNextNumber()));
	return t;
}
}

//	g2.addCheckbox("Tis is a tickbox --> ACtivate some option", true);
//	g2.addToSameRow();
//	String [] arr0 = new String[] {"Choice1", "Choice2"};
//	g2.addChoice("Choose one option amont a list", arr0, "Choice1");
//	g2.addNumericField("Some double", 10,0);
//	g2.addHelp("https://imagej.net/scripting/generic-dialog");
//	g2.showDialog();
//	if (g2.wasOKed()){
//		String inString = g2.getNextString();
//		boolean inBool = g2.getNextBoolean();
//		String inChoice = g2.getNextChoice();
//		double inNum = g2.getNextNumber();}
