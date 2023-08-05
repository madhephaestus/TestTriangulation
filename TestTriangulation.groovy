import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.FileUtil
import eu.mihosoft.vrl.v3d.Text3d
import eu.mihosoft.vrl.v3d.TextExtrude
import eu.mihosoft.vrl.v3d.Vector3d
import eu.mihosoft.vrl.v3d.svg.SVGLoad
import javafx.scene.text.Font;
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Polygon
double computeGearPitch(double diameterAtCrown,double numberOfTeeth){
	return ((diameterAtCrown/2)*((360.0)/numberOfTeeth)*Math.PI/180)
}
// call a script from another library
def bevelGears = ScriptingEngine.gitScriptRun(
            "https://github.com/madhephaestus/GearGenerator.git", // git location of the library
            "bevelGear.groovy" , // file to load
            // Parameters passed to the funcetion
            [	  34,// Number of teeth gear a
	            24,// Number of teeth gear b
	            6,// thickness of gear A
	            computeGearPitch(26.15,30),// gear pitch in arc length mm
	           60,// shaft angle, can be from 0 to 100 degrees
	            0// helical angle, only used for 0 degree bevels
            ]
            )
File f = ScriptingEngine
	.fileFromGit(
		"https://github.com/madhephaestus/SVGBowlerExtrude.git",//git repo URL
		"master",//branch
		"drawing.svg"// File from within the Git repo
	)
println "Extruding SVG "+f.getAbsolutePath()
SVGLoad s = new SVGLoad(f.toURI())
println "Layers= "+s.getLayers()
// A map of layers to polygons
HashMap<String,List<Polygon>> polygonsByLayer = s.toPolygons()
// extrude all layers to a map to 10mm thick
HashMap<String,ArrayList<CSG>> csgByLayers = s.extrudeLayers(10)
// extrude just one layer to 10mm
// The string "1-holes" represents the layer name in Inkscape
def holeParts = s.extrudeLayerToCSG(10,"1-holes")
// seperate holes and outsides using layers to differentiate
// The string "2-outsides" represents the layer name in Inkscape
def outsideParts = s.extrudeLayerToCSG(10,"2-outsides")
					.difference(holeParts)
// layers can be extruded at different depths
// The string "3-boarder" represents the layer name in Inkscape					
def boarderParts = s.extrudeLayerToCSG(5,"3-boarder")

double size=50;
TextExtrude.text(10.0, "Hello", new Font("Helvedica",  18));
TextExtrude.text(10.0, "Hello World!", new Font("Times New Roman", 18));
CSG text = new Text3d("Hello world", 10).toCSG();
text.triangulate()
CSG polygon = Extrude.points(new Vector3d(0, 0, size),// This is the  extrusion depth
		                new Vector3d(0,0),// All values after this are the points in the polygon
		                new Vector3d(size*2,0),// Bottom right corner
		                new Vector3d(size*1.5,size),// upper right corner
		                new Vector3d(size/2,size)// upper left corner
		        ).movey(-100);
CSG block = new Cube(40).toCSG().toZMin()
		        .difference(bevelGears.get(0))
		        .movey(100)
		        //.difference(bevelGears.get(1))
		        
return [block,bevelGears.get(0),text,polygon,CSG.unionAll([boarderParts,outsideParts]),s.extrudeLayerToCSG(2,"4-star")]