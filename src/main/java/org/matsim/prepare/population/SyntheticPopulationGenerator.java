package org.matsim.prepare.population;


import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map;

public class SyntheticPopulationGenerator {
    private static final String OUTPUT_FILE = "scenarios/basicInputFile/newGeneratedEmployeeFromSurvey.xml";
    private static final String SHAPE_FILE_1 = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\shapeFile\\MunichZoneSystem.shp";
    private static final String SHAPE_FILE_2="plz-5stellig.shp/plz-5stellig.shp";
    private static final String SURVEY_RESULT="scenarios/basicInputFile/newCreatedPopulation.csv";
    private static final Scenario scenario= ScenarioUtils.createScenario(ConfigUtils.createConfig());
    private static final CoordinateTransformation ct = TransformationFactory.
            getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
    private static Map<Long, Geometry> shapeMap1;
    private static Map<String, Geometry> shapeMap2;


    public static void main(String[] args) throws IOException {
         Map<String, Double> departureTime=new HashMap<>();
       Map<String, Double> workEndTime=new HashMap<>();
         Map<String,String> commuteMode=new HashMap<>();
       Map<String,String> homeZipCode=new HashMap<>();

        shapeMap1= readMunichZoneSystemShapeFile(SHAPE_FILE_1);
        shapeMap2= readPlzShapeFile(SHAPE_FILE_2);
        File file = new File(SURVEY_RESULT);
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            String[] array = line.split(",");
            if(shapeMap2.containsKey(array[6])){
            departureTime.put(array[0],Double.parseDouble(array[4]));
            workEndTime.put(array[0],Double.parseDouble(array[2]));
            commuteMode.put(array[0],array[5]);
            homeZipCode.put(array[0],array[6]);}
        }
for(Map.Entry<String,Double> entry:departureTime.entrySet()){
    createOD(homeZipCode.get(entry.getKey()),(long)2598,commuteMode.get(entry.getKey()),entry.getValue(),workEndTime.get(entry.getKey()),"survey_"+entry.getKey());
}
        PopulationWriter pw = new PopulationWriter(scenario.getPopulation(), scenario.getNetwork());
        pw.write(OUTPUT_FILE);
    }
    public static Map<Long, Geometry> readMunichZoneSystemShapeFile(String filename) {
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(filename);
        Map<Long, Geometry> shapeMap = new HashMap<>();
        for (SimpleFeature feature : features) {
            shapeMap.put((Long) feature.getAttributes().get(1), (Geometry) feature.getDefaultGeometry());
        }

        return shapeMap;
    }
    public static Map<String, Geometry> readPlzShapeFile(String filename) {
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(filename);
        Map<String, Geometry> shapeMap = new HashMap<>();
        for (SimpleFeature feature : features) {
            shapeMap.put(feature.getAttributes().get(1).toString(), (Geometry) feature.getDefaultGeometry());
        }

        return shapeMap;
    }
    private static void createOD(String origin, Long destination,String mode,double departureTime, double workEndTIme, String personID) {
        Geometry home = shapeMap2.get(origin);
        Geometry work = shapeMap1.get(destination);

            Coord homeCoord = drawRandomPointFromGeometry(home);
            homeCoord = ct.transform(homeCoord);

            Coord workCoord = drawRandomPointFromGeometry(work);


            createOneCommuter(homeCoord, workCoord, mode, departureTime,workEndTIme,personID);
    }

    private static void createOneCommuter(Coord homeCoord, Coord workCoord, String mode, double departureTime, double workEndTIme, String personID) {

        Id<Person> personId = Id.createPersonId(personID);
        Person person = scenario.getPopulation().getFactory().createPerson(personId);
        scenario.getPopulation().addPerson(person);
        Plan plan = scenario.getPopulation().getFactory().createPlan();
        person.addPlan(plan);

        Activity home = scenario.getPopulation().getFactory().createActivityFromCoord("home", homeCoord);
        home.setEndTime(departureTime);
        plan.addActivity(home);

        Leg legToWork = scenario.getPopulation().getFactory().createLeg(mode);
        plan.addLeg(legToWork);

        Activity work = scenario.getPopulation().getFactory().createActivityFromCoord("work", workCoord);
        work.setEndTime(workEndTIme);

        plan.addActivity(work);

        Leg legToHome = scenario.getPopulation().getFactory().createLeg(mode);
        plan.addLeg(legToHome);

        Activity endAtHome = scenario.getPopulation().getFactory().createActivityFromCoord("home", homeCoord);
        plan.addActivity(endAtHome);
    }

    private static Coord drawRandomPointFromGeometry(Geometry g) {
        Random rmd = MatsimRandom.getLocalInstance();
        Point p;
        double x;
        double y;
        do {
            x = g.getEnvelopeInternal().getMinX()
                    + rmd.nextDouble() * (g.getEnvelopeInternal().getMaxX() - g.getEnvelopeInternal().getMinX());
            y = g.getEnvelopeInternal().getMinY()
                    + rmd.nextDouble() * (g.getEnvelopeInternal().getMaxY() - g.getEnvelopeInternal().getMinY());
            p = MGC.xy2Point(x, y);
        } while (g.contains(p));
        return new Coord(p.getX(), p.getY());
    }


}
