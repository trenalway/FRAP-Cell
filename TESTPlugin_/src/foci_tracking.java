

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.measure.CurveFitter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileWriter; 
import java.io.IOException;
import java.awt.event.ActionListener;



public class foci_tracking implements PlugIn {
	ImagePlus impFRAP;
	ImagePlus impOther;
	ImagePlus impFRAP2;
	ImagePlus impFRAP1;
	ImagePlus impFRAP3;
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
RoiManager rm = RoiManager.getRoiManager();
//new WaitForUserDialog ("Open Image", "choose image to open and split channels").show();
IJ.run("Bio-Formats Importer");
new WaitForUserDialog ("crop image","select an area to crop").show();
rm.addRoi(null);
new WaitForUserDialog ("crop","please crop both channels with the ROI in the ROI manager").show();
rm.runCommand(impFRAP,"Delete");
new WaitForUserDialog ("Select Channels", "Click GFP").show();
impFRAP = WindowManager.getCurrentImage();
impFRAPID = impFRAP.getID();
new WaitForUserDialog ("Select other Channel", "Click H2B").show();
impOther = WindowManager.getCurrentImage();
impOtherID = impOther.getID();
IJ.run(impFRAP, "Reduce Dimensionality...", "frames keep");
impFRAP = WindowManager.getCurrentImage();
IJ.run(impOther, "Reduce Dimensionality...", "frames keep");
impOther=WindowManager.getCurrentImage();
impFRAP2=impOther.duplicate();
impFRAP2.show();
impFRAP3 = impFRAP2.duplicate();
impFRAP3.show();
IJ.selectWindow(impFRAP2.getID());
IJ.run(impFRAP2, "Make Binary", "method="+"Triangle"+" background=Default calculate black");
//IJ.setAutoThreshold(impFRAP2, "Triangle dark no-reset");
IJ.run(impFRAP2, "Erode", "stack");
IJ.run(impFRAP2, "Dilate", "stack");
IJ.setTool("Freehand");
new WaitForUserDialog ("select right area","please cut out any extra areas with the freehand selection tool" + "\n"
+ "also tale the opportunity to do anything you can to make detection quality a little better").show();
IJ.run(impFRAP2, "Analyze Particles...", "size=40-400 clear include overlay add composite stack");
new WaitForUserDialog ("right ROI selection", "please select the first right ROI").show();
Roi RightRo = WindowManager.getCurrentImage().getRoi();
String m = RightRo.getName();
int RightRoi = rm.getIndex(m);
new WaitForUserDialog ("delete useless","delete the useless ROI").show();
rm.select(RightRoi);

//double[] RightRoCoords = RightRo.getContourCentroid();
//IJ.run(impFRAP2,"Measure","");
//
//for (int a=0;a<rm.getCount();a++) {
//	int b=a-1;
//	int c=a-2;
//	if (a<2) {
//		b=RightRoi;
//		c=RightRoi;
//	}
//	Roi br = rm.getRoi(b);
//	int Name1 = Integer.parseInt(br.getName().substring(0,4));
//	Roi cr = rm.getRoi(a);
//	int Name2 = Integer.parseInt(cr.getName().substring(0,4));
//	if (Name1==Name2) {
//		rm.select(c);
//		IJ.run(impFRAP2,"Measure","");
//		rm.select(b);
//		IJ.run(impFRAP2,"Measure","");
//		rm.select(a);
//		IJ.run(impFRAP2,"Measure","");
//		double a2X = rt.getValue("X",1);
//		double a1X = rt.getValue("X", 2);
//		double aX = rt.getValue("X", 3);
//		double a2Y = rt.getValue("Y",1);
//		double a1Y = rt.getValue("Y", 2);
//		double aY = rt.getValue("Y", 3);
//		double a1LengthX = Math.abs (a1X-a2X);
//		double aLengthX = Math.abs (aX-a2X);
//		double a1LengthY = Math.abs(a1Y-a2Y);
//		double aLengthY = Math.abs(aY-a2Y);
//		double Difa1 = Math.sqrt(Math.pow(a1LengthX,2)+Math.pow(a1LengthY, 2));
//		double Difa =Math.sqrt(Math.pow(aLengthX,2)+Math.pow(aLengthY, 2));
//		
//		if (Difa > Difa1) {
//			rm.select(a);
//			rm.runCommand(impFRAP2,"Delete");
//			rt.deleteRow(1);
//			rt.deleteRow(1);
//			rt.deleteRow(1);
//			a=a-2;
//		}
//		else {
//			rm.select(a-1);
//			rm.runCommand(impFRAP2,"Delete");
//			rt.deleteRow(1);
//			rt.deleteRow(1);
//			rt.deleteRow(1);
//			a=a-2;
//		}
//	
//	}
//
//}


ArrayList<String>AP = UserInterface();
double Percentage = Double.parseDouble(AP.get(0));
for ( int a=0;a<rm.getCount(); a++) {
	Roi roi = rm.getRoi(a);
setROIThreshold(Percentage,roi,impFRAP);
}}
public void setROIThreshold(double percentage, Roi roi, ImagePlus imp){
    imp.setRoi(roi);
    int slice = roi.getPosition(); //Returns the stack position (image number) of this ROI, orzero if the ROI is not associated with a particular stack image.
    ImageProcessor ip = imp.getProcessor();// gets the information on a pixel level of the image
    ip.setSliceNumber(slice);// Can loop through slices if required
    int X = imp.getWidth();
    int Y = imp.getHeight();
    float[] pixels = new float[X * Y];//Array to hold all the pixels
    int count = 0;
    for (int x = 0; x < X; x++) {//Filling the array
        for (int y = 0; y < Y; y++) {
            pixels[count] = ip.getPixelValue(x, y);
            count++;
        }
    }
    Arrays.sort(pixels);//Sort the Array ascending
    int threshold = (int) (pixels.length * percentage / 100);//How many pixels below the threshold
    float value = pixels[threshold];//Find the value of the threshold
    imp.setSlice(slice);
    IJ.setRawThreshold(imp, value, pixels[pixels.length-1]);//Set the threshold
    IJ.run(imp, "Analyze Particles...", "size=0-Infinity pixel show=Masks summarize slice");
}

public ArrayList <String> UserInterface() {
	ArrayList<String> t = new ArrayList<String>(); // Creates an ArrayList object
	GenericDialog g = new GenericDialog("Variables");
	g.addMessage("Thresholding percentage");
	g.addNumericField("percentage",97.75,5);

	g.showDialog();

	t.add(String.valueOf(g.getNextNumber()));
	return t;
}
}
