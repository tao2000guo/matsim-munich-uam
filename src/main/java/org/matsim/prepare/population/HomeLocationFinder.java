package org.matsim.prepare.population;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dziemke
 */
public class HomeLocationFinder {

    public static void main(String[] args) throws IOException {
        // Input and output files
        String plansInputFile = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\basicInputFile\\matsimPlans_5_percent_merged.xml";
        String plansOutputFile = "scenarios/basicInputFile/home_Location_Population.xml";
        String areaShapeFile = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\shapeFile\\MunichZoneSystem.shp";
        // Store relevant area of city as geometry
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(areaShapeFile);
        Map<Long, Geometry> zoneGeometries = new HashMap<>();
        Map<String, String> homeLocation = new HashMap<>();
        for (SimpleFeature feature : features) {
            zoneGeometries.put((Long) feature.getAttributes().get(1), (Geometry) feature.getDefaultGeometry());
        }

        // Get population
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Scenario newScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Population subPopulation = newScenario.getPopulation();
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(plansInputFile);

        // Substitute car mode by carInternal mode for people inside relevant area
        for (Person person : scenario.getPopulation().getPersons().values()) {
            Activity homeActivity = null;
            Activity dummyActivity;
            for (PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
                if (planElement instanceof Leg) {
                    continue;
                }
                dummyActivity = (Activity) planElement;
                if (dummyActivity.getType().equals("home")) {
                    homeActivity = dummyActivity;
                    break;
                }
            }
//
            if (homeActivity == null) {
                continue;
            }

            Point homeActAsPoint = MGC.xy2Point(homeActivity.getCoord().getX(), homeActivity.getCoord().getY());
            homeLocation.put(person.getId().toString(), homeActAsPoint.getCoordinate().toString());
            subPopulation.addPerson(person);
        }


        // Write modified population to file
        FileWriter fstream;
        BufferedWriter out;

// create your filewriter and bufferedreader
        fstream = new FileWriter(plansOutputFile);
        out = new BufferedWriter(fstream);




// create your iterator for your map
        Iterator<Map.Entry<String, String>> it = homeLocation.entrySet().iterator();

// then use the iterator to loop through the map, stopping when we reach the

// last record in the map or when we have printed enough records
        while (it.hasNext()) {
// the key/value pair is stored here in pairs
            Map.Entry<String, String> pairs =  it.next();
// since you only want the value, we only care about pairs.getValue(), which is written to out
            out.write(pairs.getKey()+";"+ pairs.getValue() + "\n");
        }
    }
}
