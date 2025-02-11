

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.plugin.PlugIn;



public class Merge_channels implements PlugIn {
	ImagePlus impFRAP;
	ImagePlus impOther;

public void run(String arg) {

new WaitForUserDialog("Open image", "open the 2 images you want to merge").show();
IJ.run("Bio-Formats Importer");
impFRAP = WindowManager.getCurrentImage();
String FileName = impFRAP.getOriginalFileInfo().fileName;
IJ.run("Bio-Formats Importer");
//new WaitForUserDialog ("select image 2", "select the second image").show();
impOther = WindowManager.getCurrentImage();
String FileName2 = impOther.getOriginalFileInfo().fileName;

IJ.run(impFRAP,"Merge Channels...", "c1=["+ FileName +"] c3=["+FileName2+"] create");
    }
}