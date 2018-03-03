package iot.sme;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

/*
 * Osszegzes: A korabbiakban, a Stationok rogzitett szamu, ugyanakkora meretu adatot generaltak
 * azonos gyakorisaggal.
 * Itt a Stationok mar csak taroljak a szenzorokat, azok onalloan leteznek, egyedi tulajdonsagokkal
 * es sajat mukodessel.
 * Valtozo mennyiségu szenzora lehet a stationoknak, uj hozzadhato vagy elveheto.
 * A szenzoroknak sajat meretuk, tipusuk lehet es az adatgeneralisi gyakorisaguk szabadon modosithato egymastol teljesen fuggetlenul.
 * 
 * */

public class Sensor {
	
	/**
	 * A szenzorok adatait foglalja ossze.
	 * Az adatgeneralas a szenzor frekvenciaja szerint itt tortenik.
	 * A Citystation elindulasa utan elinditja a szenzorokat, a startSensor metodussal.
	 */
	
	public static class Sensordata extends Timed {
		
		public int id;    // a szenzor egyedi azonositoja
		public String type;   // a szenzor tipusa, jelenleg 5 fele
		public long sensorfreq;    //a szensor frekvenciaja, ezzel az idokozzel general adatot(merest)
		public int size;   // egy meres/adat merete
		public boolean randommetering;
		public int maxrandommeteringint;
		
		
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
		public boolean getRandomMetering() {
			return this.randommetering;
		}
		
		public void setRandomMetering(boolean randommetering) {
			this.randommetering=randommetering;
		}
		public int getMaxRandomMeteringInt() {
			return this.maxrandommeteringint;
		}
		
		public void setMaxRandomMeteringInt(int maxrandommeteringint) {
			this.maxrandommeteringint=maxrandommeteringint;
		}
		
		/*
		 * A szensor konstruktora.
		 * Az azonositot, tipust, frekvenciat es az adat meretet allitja be.
		 * A CityStation hivasara a readSensorData metodus hozza letre a megfelelo szamu szenzort xml fajl alapjan.
		 * */
		
		public Sensordata(int id,String type,long sensorfreq,int size, boolean randommetering, int maxrandommeteringint) {
			this.id = id;
			this.type = type;
			this.sensorfreq = sensorfreq;
			this.size = size;
			this.randommetering = randommetering;
			this.maxrandommeteringint = maxrandommeteringint;
		}
		
		CityStation citystation;  //hivatkozas a szenzort tarolo CityStationre
		
		/*
		 * A CityStation hívja minden tarolt szenzorara,az egyes szenzorok mereseit inditja el.
		 * 
		 * */
		public void startSensor(CityStation cs) {
			this.citystation = cs;
			
			subscribe(this.getSensorfreq());
			//System.out.println("Elindult a szenzor: " + Timed.getFireCount());
					
		}

		
		/*
		 * Megallitja a szensorok mukodeset.
		 * */
		public void stopSensor() {
			this.unsubscribe();
		}
		
		/*
		 * A szenzorok egyedi meretevel es azonositojaval dolgozik.
		 * A CityStation mar csak tarolja a szenzorokat, és az adattovabbitast kezeli.
		 * 
		 * */
		@Override
		public void tick(long fires) {
		
		//megallasi feltetel,ha a mukodesi idot tullepjuk, vagy a station valamiert leall
			if((citystation.getSd().getStoptime() + citystation.getTime()) < Timed.getFireCount() || citystation.getIsWorking() == false) {
				stopSensor();
				//System.out.println("Sensor megallt " + Timed.getFireCount()) ;
			}
			
			if (this.randommetering == true) {

				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(maxrandommeteringint) + 1;
				new Metering(citystation, this.getId(), this.size, 1000 * randomInt);
				//System.out.println("Ez a szenzor: " + randommetering + " randommetering ertekkel rendelkezik, max kesleltetese: " + maxrandommeteringint);
			} else {

				new Metering(citystation, this.getId(), this.size, 1);
			}
		}
		
		
	}
	
	
	public static int counter=0; //xml olvasashoz kell
	
		
/*
 * Ez a metodus olvassa be a sensor adatokat,Scenario hivja, Citystation sensors lista tarolja a peldanyokat.
 * 
 */
	
public void readSensorData(String stationfile,CityStation citystation,int sensornumber)throws SAXException, IOException, ParserConfigurationException, NetworkException {
	
	if (stationfile.isEmpty()) {
		System.out.println("Datafile nem lehet null");
		System.exit(0);
	} else {
		File fXmlFile = new File(stationfile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		//NodeList nListStation = doc.getElementsByTagName("CityStation");
		NodeList nList = doc.getElementsByTagName("sensor");

		
		for (int temp = counter*sensornumber ; temp < (counter*sensornumber) + sensornumber; temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				int id = Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent());
				String type = eElement.getElementsByTagName("type").item(0).getTextContent();
				long sensorfreq = Long.parseLong(eElement.getElementsByTagName("sensorfreq").item(0).getTextContent());
				int size = Integer.parseInt(eElement.getElementsByTagName("size").item(0).getTextContent());
				boolean randommetering = Boolean.parseBoolean(eElement.getElementsByTagName("rndmeter").item(0).getTextContent());
				int maxrandommeteringint=Integer.parseInt(eElement.getElementsByTagName("rndmeter")
						.item(0).getAttributes().item(0).getNodeValue());
				citystation.getSensors().add(new Sensor.Sensordata(id,type,sensorfreq,size,randommetering,maxrandommeteringint));
				
				System.out.println("SensorAdatok: " + id + " , " + type + " , " + sensorfreq + " , " + size + "!");
			}
		}
		
	counter++; //biztositja, hogy a megfelelo szensor adatait olvassuk be	
	}
}

}
