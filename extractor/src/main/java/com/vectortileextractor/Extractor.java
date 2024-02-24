package com.vectortileextractor;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.ZoomFunction;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Code adapted from planetiler example:
 * https://github.com/onthegomap/planetiler
*/

public class Extractor implements Profile {


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

			int elevation = 0;
			// some features do not have elevations -> use 0 (If is always true currently -> but if we also allow no elevation data -> than using this is necessary)
			if(sourceFeature.hasTag("ele"))
			{
				// ele tag is encoded as a string that contains double precision values
				// we therfore have to convert the "object" to a string -> then the string to a double -> and the double to the desired int
				elevation = (int)Double.parseDouble((String)sourceFeature.getTag("ele"));
			}

			features.point("Peak")
				.setZoomRange(9, 18)
				.setSortKeyDescending(elevation) // sort features according to elevation -> prefers higher peaks if zoom level does not allow more
				.setAttr("name", name)
				.setAttr("elevation", elevation)
				.setId(sourceFeature.id())

				// FINE TUNING PROBABLY NECESSARY
				.setPointLabelGridSizeAndLimit(
					12, // only limit at z12 and below
					64, // break the tile up into NxN px squares
					1 // any only keep the x nodes with lowest sort-key in each 32px square
				)
			 
				.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 64))
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
