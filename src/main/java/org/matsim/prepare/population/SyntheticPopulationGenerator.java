package org.matsim.prepare.population;

import org.apache.commons.math3.distribution.GammaDistribution;
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

import java.util.*;
import java.util.Map;

public class SyntheticPopulationGenerator {
    private static final String OUTPUT_FILE = "scenarios/basicInputFile/newGeneratedWorker.xml";
    private static final String SHAPE_FILE = "E:\\Project seminar\\matsim-munich-uam0118\\scenarios\\shapeFile\\MunichZoneSystem.shp";
    private static final Scenario scenario= ScenarioUtils.createScenario(ConfigUtils.createConfig());
    private static final CoordinateTransformation ct = TransformationFactory.
            getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);
    private static Map<Long, Geometry> shapeMap;




    public static void main(String[] args) {
shapeMap=readShapeFile(SHAPE_FILE);
int commuter=459;
for(int i2=1;i2<=commuter;i2++){
    Random rand=new Random();
    int randomInt1=rand.nextInt(25);
    int randomInt2=rand.nextInt(4592)+1;
    double randomDouble=rand.nextDouble();
    if(randomDouble<0.2828){
        createLowSpeedOD((long)2573+randomInt1, (long)2598, "Employee"+i2);
    }
else{
        createHighSpeedOD((long)randomInt2,(long)2598,"Employee"+i2);
    }
}
        PopulationWriter pw = new PopulationWriter(scenario.getPopulation(), scenario.getNetwork());
        pw.write(OUTPUT_FILE);
    }
    public static Map<Long, Geometry> readShapeFile(String filename) {
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(filename);
        Map<Long, Geometry> shapeMap = new HashMap<>();
        for (SimpleFeature feature : features) {
            shapeMap.put((Long) feature.getAttributes().get(1), (Geometry) feature.getDefaultGeometry());
        }

        return shapeMap;
    }
    private static void createLowSpeedOD(Long origin, Long destination, String odPrefix) {
        Geometry home = shapeMap.get(origin);
        Geometry work = shapeMap.get(destination);
        String mode;
            double randomNumber=Math.random();
           if(randomNumber<0.16){
               mode="walk";
           }
           else {
               mode="bike";
           }
            Coord homeCoord = drawRandomPointFromGeometry(home);
            homeCoord = ct.transform(homeCoord);

            Coord workCoord = drawRandomPointFromGeometry(work);
            workCoord = ct.transform(workCoord);

            createOneCommuter(homeCoord, workCoord, mode, odPrefix);
    }
    private static void createHighSpeedOD(Long origin, Long destination, String odPrefix) {
        Geometry home = shapeMap.get(origin);
        Geometry work = shapeMap.get(destination);
        String mode;
            double randomNumber=Math.random();
            if (randomNumber<0.6705){
                if(randomNumber<0.6286){
                    mode = "car";
                }
                else
                    mode="ride";
            }
            else {
                mode="pt";}
            Coord homeCoord = drawRandomPointFromGeometry(home);
            homeCoord = ct.transform(homeCoord);

            Coord workCoord = drawRandomPointFromGeometry(work);
            workCoord = ct.transform(workCoord);

            createOneCommuter(homeCoord, workCoord, mode, odPrefix);

    }

    private static void createOneCommuter(Coord homeCoord, Coord workCoord, String mode, String  odPrefix) {
        double commuteTimeVaraible=new GammaDistribution(2.69,13.958).sample();
        double durationTimeVariance=Math.random()*60*60*2;

        Id<Person> personId = Id.createPersonId(odPrefix);
        Person person = scenario.getPopulation().getFactory().createPerson(personId);
        scenario.getPopulation().addPerson(person);

        Plan plan = scenario.getPopulation().getFactory().createPlan();
        person.addPlan(plan);

        Activity home = scenario.getPopulation().getFactory().createActivityFromCoord("home", homeCoord);
        home.setEndTime(9 * 60 * 60 - (int)commuteTimeVaraible*60);
        plan.addActivity(home);

        Leg legToWork = scenario.getPopulation().getFactory().createLeg(mode);
        plan.addLeg(legToWork);

        Activity work = scenario.getPopulation().getFactory().createActivityFromCoord("work", workCoord);
        work.setMaximumDuration(7 * 60 * 60 + durationTimeVariance);
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
//        do {
            x = g.getEnvelopeInternal().getMinX()
                    + rmd.nextDouble() * (g.getEnvelopeInternal().getMaxX() - g.getEnvelopeInternal().getMinX());
            y = g.getEnvelopeInternal().getMinY()
                    + rmd.nextDouble() * (g.getEnvelopeInternal().getMaxY() - g.getEnvelopeInternal().getMinY());
            p = MGC.xy2Point(x, y);
//        } while (g.contains(p));
        return new Coord(p.getX(), p.getY());
    }


}
