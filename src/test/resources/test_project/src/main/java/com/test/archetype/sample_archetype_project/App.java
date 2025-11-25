package com.test.archetype.sample_archetype_project;

import org.sensoris.messages.data.DataMessage;

public class App {

  public static void main(String[] args) {
    Class<DataMessage> dataMessageClass = DataMessage.class;
    System.out.println("Class " + dataMessageClass.getName() + " was loaded!");
  }

}
