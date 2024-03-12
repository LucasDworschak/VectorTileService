package com.vectortileextractor;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.ZoomFunction;
import com.onthegomap.planetiler.geo.GeometryException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Coordinate;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/*
 * Code adapted from planetiler example:
 * https://github.com/onthegomap/planetiler
*/

public class Extractor implements Profile {

	// IF YOU GOT THE MESSAGE TO ADD CHARS TO THE LIST
	// OVERRIDE charArray with the list you got
	private static int[] charArray = {32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 45, 46, 47, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 167, 176, 180, 196, 214, 220, 223, 225, 226, 228, 233, 236, 237, 243, 246, 250, 252, 253, 268, 269, 283, 328, 337, 345, 352, 353, 381, 382, 8364};
	public static List<Integer> allChars = Arrays.stream(charArray).boxed().toList();
	@Override
	public void processFeature(SourceFeature sourceFeature, FeatureCollector features)
	{
		// only allow peaks that at least have a name tag
		if (sourceFeature.isPoint() 												// GIVE US ONLY POINT DATA
			&& sourceFeature.hasTag("natural", "peak") 								// GIVE US ALL PEAKS
			&& (sourceFeature.hasTag("name") || sourceFeature.hasTag("name:de"))	// REQUIRED NAME OR GERMAN NAME
			&& sourceFeature.hasTag("ele")  										// REQUIRED ELEVATION
		)
		{
			String name;
			if(sourceFeature.hasTag("name:de"))
			{// prefer german names (if available)
					name = (String)sourceFeature.getTag("name:de");
			}
			else
			{
					name = (String)sourceFeature.getTag("name");
			}

			// check if any name contains chars that are not present in our list
			for (int i = 0; i < name.length(); i++) {
				if(!allChars.contains(name.codePointAt(i)))
				{

					allChars.add(name.codePointAt(i));

					Collections.sort(allChars);
					
					// !! IF THIS MESSAGE APPEARS YOU HAVE TO DO SOMETHING !!
					// 1. add the list of integers to this java file as the new allChars variable above
					// 2. add the list of integers to the alpinemaporgs project so that the font atlas contains all necessary chars
					System.out.println("Features contain unfamiliar chars: !!! ADD THEM TO THE LIST !!!:");
					System.out.println(allChars);
				}
			}

			Coordinate latlon;
			try{
				latlon = sourceFeature.latLonGeometry().getCoordinate();
			}
			catch(GeometryException e)
			{
				System.out.println("Coordinate not found: " + name);
				return;
			}
			

			int elevation = 0;
			// some features do not have elevations -> use 0 (If is always true currently -> but if we also allow no elevation data -> than using this is necessary)
			if(sourceFeature.hasTag("ele"))
			{
				// ele tag is encoded as a string that contains double precision values
				// we therfore have to convert the "object" to a string -> then the string to a double -> and the double to the desired int
				elevation = (int)Double.parseDouble((String)sourceFeature.getTag("ele"));
			}

			// planetiler only supports up to zoom level 14
			// https://github.com/onthegomap/planetiler/discussions/506
			features.point("Peak")
				.setZoomRange(9, 14)
				.setSortKeyDescending(elevation) // sort features according to elevation -> prefers higher peaks if zoom level does not allow more
				.setAttr("name", name)
				.setAttr("elevation", elevation)
				.setAttr("long", latlon.x)
				.setAttr("lat", latlon.y)
				.setId(sourceFeature.id())

				// FINE TUNING PROBABLY NECESSARY
				.setPointLabelGridSizeAndLimit(
					12, // only limit at z12 and below (z13+ -> shows all)
					64, // break the tile up into NxN px squares
					1 // any only keep the x nodes with lowest sort-key in each 32px square
				)
			 
				.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 64))

				.setMinPixelSize(0)
				;
		}
	}


	@Override
	public String name() {
		return "Peak Extractor";
	}

	@Override
	public String description() {
		return "extracts mountain peaks";
	}

	@Override
	public boolean isOverlay() {
		return true;
	}

	/*
	 * Any time you use OpenStreetMap data, you must ensure clients display the following copyright. Most clients will
	 * display this automatically if you populate it in the attribution metadata in the mbtiles file:
	 */
	@Override
	public String attribution() {
		return """
			<a href="https://www.openstreetmap.org/copyright" target="_blank">&copy; OpenStreetMap contributors</a>
			""".trim();
	}

	/*
	 * Main entrypoint for the example program
	 */
	public static void main(String[] args) throws Exception {
		run(Arguments.fromArgsOrConfigFile(args));
	}

	static void run(Arguments args) throws Exception {
		Planetiler.create(args)
			.setProfile(new Extractor())
			.addOsmSource("osm", Path.of("../input", "austria-latest.osm.pbf"), "")
			.overwriteOutput(Path.of("../output", "austria.peaks.pmtiles"))
			.run();
	}
}
