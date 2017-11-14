package iot.sme;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

public class Sensor {
	
	/**
	 * A szenzorok adatait foglalja ossze.
	 *
	 */
	
	public static class Sensordata {
		
		public int id;
		public String type;
		public long sensorfreq;
		public int size;
		
		public int getSize() {
			return this.size;
		}
		
		public void setSize(int size) {
			this.size = size;
		}
		
		public int getId() {
			return this.id;
		}
		
		public void setId(int id) {
			this.id=id;
		}
		
		public String getType() {
			return this.type;
		}
		
		public void setId(String type) {
			this.type=type;
		}
		
		public long getSensorfreq() {
			return this.sensorfreq;
		}
		
		public void setSensorfreq(long sensorfreq) {
			this.sensorfreq=sensorfreq;
		}
		
		
		public Sensordata(int id,String type,long sensorfreq,int size) {
			this.id = id;
			this.type = type;
			this.sensorfreq = sensorfreq;
			this.size = size;
		}
		
	}
	
		
/*
 * Ez a metodus olvassa be a sensor adatokat,Scenario hivja, Citystation sensors lista tarolja a peldanyokat.
 * XML feldolgozast javitani!
 * 
 */
	
public void readSensorData(String stationfile,CityStation citystation)throws SAXException, IOException, ParserConfigurationException, NetworkException {
	
	if (stationfile.isEmpty()) {
		System.out.println("Datafile nem lehet null");
		System.exit(0);
	} else {
		File fXmlFile = new File(stationfile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		 
		NodeList nList = doc.getElementsByTagName("sensor");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				int id = Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent());
				String type = eElement.getElementsByTagName("type").item(0).getTextContent();
				long sensorfreq = Long.parseLong(eElement.getElementsByTagName("sensorfreq").item(0).getTextContent());
				int size = Integer.parseInt(eElement.getElementsByTagName("size").item(0).getTextContent());
				citystation.getSensors().add(new Sensor.Sensordata(id,type,sensorfreq,size));
				
				System.out.println("SensorAdatok: " + id + " , " + type + " , " + sensorfreq + " , " + size + "!");
			}
		}
	}
}
		
}
