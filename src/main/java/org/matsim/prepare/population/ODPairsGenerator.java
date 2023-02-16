package org.matsim.prepare.population;

import org.locationtech.jts.geom.Coordinates;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ODPairsGenerator {



    public static void main(String[] args) throws IOException {
        String plansInputFile="scenarios/basicInputFile/15kmCentroid.csv";
        String plansOutputFile="scenarios/basicInputFile/plans.csv";
        Map<String, String> origin=new HashMap<>();
        Map<String, String> destination=new HashMap<>();
        BufferedReader csvReader=new BufferedReader(new FileReader(plansInputFile));
        String line;
        while ((line=csvReader.readLine())!=null){
            String str[] = line.split(",");
            origin.put(str[0],str[1]+";"+str[2]);
            destination.put(str[0],str[1]+";"+str[2]);
            }
        File outPutFIle=new File(plansOutputFile);
        BufferedWriter out = new BufferedWriter(new FileWriter(outPutFIle));
        out.write("from_x;from_y;to_x;to_y;start_time"+";origin_zone;destination_zone"+"\n");
        for (Map.Entry<String, String> pairs1 : origin.entrySet()) {
            for(Map.Entry<String,String> pairs2:destination.entrySet()){
                out.write(pairs1.getValue()+";"+pairs2.getValue()+";"+"07:30"+";"+pairs1.getKey()+";"+pairs2.getKey()+"\n");
            }
        }
        out.close();
        }
    }



