package org.matsim.prepare.population;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author dziemke
 */
public class PopulationModifier {

    public static void main(String[] args) {
        // Input and output files
        String plansInputFile = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\basicInputFile\\matsimPlans_5_percent_merged.xml";
        String plansOutputFile = "scenarios/basicInputFile/plans_oberpfafenhofen_worker.xml";
        String areaShapeFile = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\shapeFile\\MunichZoneSystem.shp";
        // Store relevant area of city as geometry
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(areaShapeFile);
        Map<Long, Geometry> zoneGeometries = new HashMap<>();
        for (SimpleFeature feature : features) {
            zoneGeometries.put((Long) feature.getAttributes().get(1), (Geometry) feature.getDefaultGeometry());
        }
        Geometry areaGeometry = zoneGeometries.get((long) 2598);

        // Get population
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Scenario newScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Population subPopulation = newScenario.getPopulation();
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(plansInputFile);

        // Substitute car mode by carInternal mode for people inside relevant area
        for (Person person : scenario.getPopulation().getPersons().values()) {
            Activity workActivity = null;
            Activity dummyActivity;
            for (PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
             if (planElement instanceof Leg){
                 continue;
             }
                dummyActivity = (Activity) planElement;
                if (dummyActivity.getType().equals("work")) {
                    workActivity = dummyActivity;
                    break;
                }
            }
//
                if (workActivity == null) {
                    continue;
                }

                Point workActAsPoint = MGC.xy2Point(workActivity.getCoord().getX(), workActivity.getCoord().getY());


                if (areaGeometry.contains(workActAsPoint)) {
                    subPopulation.addPerson(person);
                }
            }


            // Write modified population to file
            PopulationWriter populationWriter = new PopulationWriter(subPopulation);
            populationWriter.write(plansOutputFile);
        }
    }
