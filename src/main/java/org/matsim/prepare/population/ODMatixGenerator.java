package org.matsim.prepare.population;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.geotools.util.MapEntry;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

//This class generator the ODMatrix from the trips file
public class ODMatixGenerator {
    private static final String OUTPUT_FILE = "scenarios/basicInputFile/ODMatrix.csv";
    private static final String TRIP_FILE="scenarios/basicInputFile/uam.10.trips.csv";
    private static final String SHAPE_FILE = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\shapeFile\\MunichZoneSystem.shp";

    public static void main(String[] args) throws IOException {
        Map<Long, Geometry> shapeMap = readShapeFile(SHAPE_FILE);
        Map<Integer, String> odMatrix;
        odMatrix = readTripFile(shapeMap, TRIP_FILE);

// create your filewriter and bufferedreader
        File outPutFIle=new File(OUTPUT_FILE);
        BufferedWriter out = new BufferedWriter(new FileWriter(outPutFIle));
// create your iterator for your map
        // last record in the map or when we have printed enough records
        for (Map.Entry<Integer, String> pairs : odMatrix.entrySet()) {
// the key/value pair is stored here in pairs
            // since you only want the value, we only care about pairs.getValue(), which is written to out
            out.write(pairs.getValue()+ "\n");
        }
        out.close();
    }
    public static Map<Long, Geometry> readShapeFile(String filename) {
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(filename);
        Map<Long, Geometry> shapeMap = new HashMap<>();
        for (SimpleFeature feature : features) {
            shapeMap.put((Long) feature.getAttributes().get(1), (Geometry) feature.getDefaultGeometry());
        }

        return shapeMap;
    }
    public static Map<Integer,String> readTripFile(Map<Long, Geometry> shapeMap, String filename) throws IOException {
        Map<Integer,String> odMatrix=new HashMap<>();
        Map<Integer, Long> origin = new HashMap<>();
        Map<Integer, Long> destination = new HashMap<>();
        Map<Integer, String> modes = new HashMap<>();
        Map<Integer, String> travelTimes = new HashMap<>();
        int index = 1;
        File file = new File(filename);
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            String[] array = line.split(";");
            double originX = Double.parseDouble(array[14]);
            double originY = Double.parseDouble(array[15]);
            double destinationX = Double.parseDouble(array[18]);
            double destinationY = Double.parseDouble(array[19]);
            String travelTime = array[4];
            travelTimes.put(index, travelTime);
            String mode = array[8];
            modes.put(index, mode);
            Point originPoint = MGC.xy2Point(originX, originY);
            Point destinationPoint = MGC.xy2Point(destinationX, destinationY);
            for (Map.Entry<Long, Geometry> entry : shapeMap.entrySet()) {
                if (entry.getValue().contains(originPoint) ) {
                    origin.put(index, entry.getKey());
                }
                if (entry.getValue().contains(destinationPoint) ) {
                    destination.put(index, entry.getKey());
                }
                if (origin.get(index) != null && destination.get(index) != null) {
                    odMatrix.put(index,origin.get(index)+";"+destination.get(index)+";"+travelTimes.get(index)+";"+modes.get(index));
                    index++;
                    break;
                }
            }

        }
return odMatrix;
    }

}
