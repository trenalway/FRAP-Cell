

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.plugin.StackWriter;
import java.io.File;


public class Cell_treatment_8h implements PlugIn {
	ImagePlus impComp0;
	ImagePlus impComp;
	ImagePlus impGFP;
	ImagePlus impRFP;
	int impCID;
	int impGID;
	int impRID;
	String DirectoryPath;
	public void run (String arg) {
		IJ.run("Bio-Formats Importer");
		impComp0 = WindowManager.getCurrentImage();
		impComp0.getChannel();
		impComp0.setC(1);
		IJ.run(impComp0,"Enhance Contrast", "saturated = 0.35");
		impComp0.setC(2);
		IJ.run(impComp0,"Enhance Contrast", "saturated = 0.35");
		new WaitForUserDialog ("slice","choose best Z position").show();
		IJ.run(impComp0,"Reduce Dimensionality...", "channels frames");
		ImagePlus impComp2 = WindowManager.getCurrentImage().duplicate();
		impComp2.show();
		ImagePlus[] channels = ChannelSplitter.split(impComp2);
		channels[0].show();
		channels[1].show();
		new WaitForUserDialog ("select Channels","Click GFP").show();
		impGFP = WindowManager.getCurrentImage();
		new WaitForUserDialog("select Channels","Click RFP").show();
		impRFP = WindowManager.getCurrentImage();
		DirectoryPath = impComp0.getOriginalFileInfo().directory;
		IJ.run(impGFP,"Enhance Contrast", "saturated = 0.35");
		IJ.run(impRFP,"Enhance Contrast", "saturated = 0.35");
		impCID = impComp0.getID();
		impGID = impGFP.getID();
		impRID = impRFP.getID();
		String CName = impComp0.getTitle().substring(34,35);
		File image = new File(DirectoryPath+"/image"+CName);
		image.mkdir();
		CName=image+"/";
		File b1 = new File (CName+"Block_1");
		File b2 = new File (CName+"Block_2");
		b1.mkdir();
		b2.mkdir();
		CName=b1+"/";
		File comp = new File(CName+"composit");
		File GFP = new File (CName +"GFP");
		File RFP = new File (CName + "RFP");
		comp.mkdir();
		GFP.mkdir();
		RFP.mkdir();
		CName=b2+"/";
		comp = new File(CName+"composit");
		GFP = new File (CName +"GFP");
		RFP = new File (CName + "RFP");
		comp.mkdir();
		GFP.mkdir();
		RFP.mkdir();
		
		IJ.setTool("rectangle");
		new WaitForUserDialog ("crop", "crop, also check when metaphase is ").show();
		RoiManager rm = RoiManager.getRoiManager();
		rm.addRoi(null);
		Roi[] roi = rm.getSelectedRoisAsArray();
		WindowManager.getImage(impCID);
		impComp2.setRoi(roi[0]);
		impComp = impComp2.crop("stack");
		WindowManager.getImage(impGID);
		impGFP.setRoi(roi[0]);
		impGFP = impGFP.crop("stack");
		WindowManager.getImage(impRID);
		impRFP.setRoi(roi[0]);
		impRFP = impRFP.crop("stack");
		
		IJ.run(impComp,"RGB Color", "frames keep");
		IJ.run(impRFP,"RGB Color", "frames keep");
		IJ.run(impGFP,"RGB Color", "frames keep");
		IJ.run(impComp, "Scale Bar...", "width=10 height=10 thickness=10 font=40 color=White background=None location=[Lower Right] horizontal bold overlay label");
		IJ.run(impGFP, "Scale Bar...", "width=10 height=10 thickness=10 font=40 color=White background=None location=[Lower Right] horizontal bold overlay label");
		IJ.run(impRFP, "Scale Bar...", "width=10 height=10 thickness=10 font=40 color=White background=None location=[Lower Right] horizontal bold overlay label");
		double FrameInterval = impComp.getCalibration().frameInterval;
		GenericDialog g = new GenericDialog("metaphase frame");
		g.addMessage("put the frame where metaphase happens (if this is block 2 and metaphase is in block 1, add the difference between and then add 27)");
		g.addNumericField("frame ", 7, 0);
		g.addMessage("which block is this?");
		String [] arr = new String[] {"1","2"};
		g.addChoice("block :", arr,"1" );
		g.showDialog();
		double t = g.getNextNumber();
		String b = g.getNextChoice();
		IJ.run(impComp,"Label...","format=00:00:00 starting="+(-FrameInterval*(t-1)) +" interval="+FrameInterval+" x=60 y=60 font=60 text=[] range=1-27 use");
		IJ.run(impGFP,"Label...","format=00:00:00 starting="+(-FrameInterval*(t-1)) +" interval="+FrameInterval+" x=60 y=60 font=60 text=[] range=1-27 use");
		IJ.run(impRFP,"Label...","format=00:00:00 starting="+(-FrameInterval*(t-1)) +" interval="+FrameInterval+" x=60 y=60 font=60 text=[] range=1-27 use");
		
		impComp.show();
		impRFP.show();
		impGFP.show();
		StackWriter.save(impComp,   "C:/Users/hugov/OneDrive - University of Edinburgh/university/year 5/assays/8h experiment/12.5.23/airyscan treated/H2B/"
		+"image"+impComp0.getTitle().substring(34,35)+"/Block_"+b+"/composit/", "format=jpeg");
		StackWriter.save(impGFP,   "C:/Users/hugov/OneDrive - University of Edinburgh/university/year 5/assays/8h experiment/12.5.23/airyscan treated/H2B/"
		+"image"+impComp0.getTitle().substring(34,35)+"/Block_"+b+"/GFP/", "format=jpeg");
		StackWriter.save(impRFP,   "C:/Users/hugov/OneDrive - University of Edinburgh/university/year 5/assays/8h experiment/12.5.23/airyscan treated/H2B/"
		+"image"+impComp0.getTitle().substring(34,35)+"/Block_"+b+"/RFP/", "format=jpeg");
		
		
	}
}