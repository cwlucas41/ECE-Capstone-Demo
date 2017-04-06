package edu.slu.iot.realdaq;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Reader{
  public static void main(String[] args){
   File file = new File("/home/deiban/ECE_Capstone_ADC/SAMPLE-SESSION");
    try{
      //System.out.println("Found File");
      Scanner sc = new Scanner(file);
      while(sc.hasNextLine()){
        //System.out.println("Reading Line");
        String line = sc.nextLine();
        System.out.println(line);
      }
      sc.close();
    }
    catch(FileNotFoundException e){
      System.out.println("could not find file");
      e.printStackTrace();
    }
  }

}
