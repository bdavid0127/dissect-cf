package iot.sme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cloudprovider.ResourceDependentProvider;

import org.w3c.dom.Node;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import iot.sme.Application.VmCollector;
import iot.sme.CityStation.Stationdata;
import iotprovider.sme.CloudsProvider;
import iotprovider.sme.Provider;

import iot.sme.Sensor;

/**
  *  Main task of this class to run the whole simulation based on XML files.
  *  Az osztaly fo feladata, hogy lefuttassa a teljes szimulaciot XML fajlok alapjan.
  */
public class Scenario {
	
	private static int filesize;
	private static long simulatedTime;
	private static boolean randommetering; //random sensorinditasi kesleltetes (0-20 perc mint korabban)
	

	private void loging() throws FileNotFoundException, UnsupportedEncodingException{
			int i=0;
			int j=0;
			for(Application a : Application.getApp()){
				System.out.println(a.getName()+ " stations: "+ a.stations.size());
				for(VmCollector vmcl : a.vmlist){
					if(vmcl.worked){
						j++;
						i+=vmcl.tasknumber;
					}
				}
				
				for(VmCollector vmcl : a.vmlist){						
						if(vmcl.isWorked()){
							//System.out.println(vmcl.vm+" : "+vmcl.tasknumber+" : "+vmcl.workingTime);
						}
						System.out.println(vmcl.vm+" : "+vmcl.tasknumber+" : "+vmcl.workingTime+" : "+vmcl.letehozva+ " : "+ vmcl.pm);
				}
				
				
				/*PrintWriter writer = new PrintWriter("src/main/java/iot/extension/experiments/scenario3/task-5L4"+".csv", "UTF-8");	
				for( Long s : a.tmap.keySet() )
				{
					writer.println(s + "," + a.tmap.get(s));

				}
				writer.close();*/
			}
			
			System.out.println("~~~~~~~~~~~~");
			System.out.println("VM: "+j + " tasks: "+i);
			System.out.println("~~~~~~~~~~~~");
			System.out.println("All filesize: "+CityStation.allstationsize);
			System.out.println("~~~~~~~~~~~~");
			System.out.println("Scneario finished at: "+Application.getFinishedTime());
			System.out.println("~~~~~~~~~~~~");
			for(Cloud c : Cloud.getClouds()){
				System.out.println(c.getIaas().repositories.toString());
				for (PhysicalMachine p : c.getIaas().machines) {
					if ( p.localDisk.getFreeStorageCapacity() != p.localDisk.getMaxStorageCapacity()) {
						System.out.println(p);
					}
				}
				System.out.println("~~~~~~~~~~~~");
		}
	}
	
	
	private static void readStationXml(String stationfile,String cloudfile,String providerfile,String cproviderfile, int cloudcount,int print,long appfreq) throws SAXException, IOException, ParserConfigurationException, NetworkException{
		long tasksize=-1; // TODO: ez miert kell?!

		CityStation.setStationvalue(new long[cloudcount]); 
		if(cloudcount<1){
			System.out.println("Cloudcount ertekenek legalabb 1-nek kell lennie!");
			System.exit(0);
		}
		
		if (stationfile.isEmpty()) {
			System.out.println("Datafile nem lehet null");
			System.exit(0);
		} else {
			File fXmlFile = new File(stationfile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList NL = doc.getElementsByTagName("application");
			tasksize = Long.parseLong(NL.item(0).getAttributes().item(0).getNodeValue());
			 if(tasksize<=0){
				 System.out.println("rossz tasksize ertek! ");
					System.exit(0);
			 }
			 
			NodeList nList = doc.getElementsByTagName("CityStation");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					final long freq = Long.parseLong(eElement.getElementsByTagName("freq").item(0).getTextContent());
					if (freq <= 0) {
						System.out.println("rossz freq ertek! ");
						System.exit(0);
					}
					final long time = Long.parseLong(eElement.getElementsByTagName("time").item(0).getTextContent());
					if (time < -1) {
						System.out.println("rossz time ertek! ");
						System.exit(0);
					}
					
					final long starttime = Long.parseLong(
							eElement.getElementsByTagName("time").item(0).getAttributes().item(0).getNodeValue());
					final long stoptime = Long.parseLong(
							eElement.getElementsByTagName("time").item(0).getAttributes().item(1).getNodeValue());

					if (starttime < 0 || (starttime > time && starttime < -1) || starttime > stoptime) {
						System.out.println("rossz starttime/stoptime ertek! ");
						System.exit(0);
					}
					
					
	/*
					final int snumber = Integer
							.parseInt(eElement.getElementsByTagName("snumber").item(0).getTextContent());
					if (snumber <= 0) {
						System.out.println("rossz snumber ertek! ");
						System.exit(0);
					}
					final int filesize=Integer.parseInt(eElement.getElementsByTagName("snumber").item(0)
							.getAttributes().item(0).getNodeValue());
					Scenario.filesize=filesize;
					
	
					if (filesize < 1) {
						System.out.println("rossz filesize ertek! ");
						System.exit(0);
					}
					
	*/
					
					final long maxinbw = Long
							.parseLong(eElement.getElementsByTagName("maxinbw").item(0).getTextContent());
					if (maxinbw <= 0) {
						System.out.println("rossz maxinbw ertek! ");
						System.exit(0);
					}
					final long maxoutbw = Long
							.parseLong(eElement.getElementsByTagName("maxoutbw").item(0).getTextContent());
					if (maxoutbw <= 0) {
						System.out.println("rossz maxoutbw ertek! ");
						System.exit(0);
					}
					final long diskbw = Long
							.parseLong(eElement.getElementsByTagName("diskbw").item(0).getTextContent());
					if (diskbw <= 0) {
						System.out.println("rossz diskbw ertek! ");
						System.exit(0);
					}
					final long reposize = Long
							.parseLong(eElement.getElementsByTagName("reposize").item(0).getTextContent());
					if (reposize <= 0) {
						System.out.println("rossz reposize ertek! ");
						System.exit(0);
					}
					final int ratio = Integer.parseInt(eElement.getElementsByTagName("ratio").item(0).getTextContent());
					if (ratio < 1) {
						System.out.println("rossz ratio ertek! ");
						System.exit(0);
					}
					
					final int stationnumber=Integer.parseInt(eElement.getElementsByTagName("name")
								.item(0).getAttributes().item(0).getNodeValue());
					if (stationnumber < 1) {
						System.out.println("rossz stationnumber ertek! ");
						System.exit(0);
					}
					
					
					//szenzorok szama is kimentve
					final int sensornumber=Integer.parseInt(eElement.getElementsByTagName("sensors")
							.item(0).getAttributes().item(0).getNodeValue());
					
					//adott statation random indljon-e
					final boolean rndstart = Boolean.parseBoolean(eElement.getElementsByTagName("rndstart").item(0).getTextContent());
					//random indulas max kesleltetese
					final int rndstartvalue=Integer.parseInt(eElement.getElementsByTagName("rndstart")
							.item(0).getAttributes().item(0).getNodeValue());
					
					
					//fajlmeret és szenzorok szama innen kikerultek
					
					for(int i=0;i<stationnumber;i++){
						Stationdata sd = new Stationdata(time, starttime, stoptime, freq,
								eElement.getElementsByTagName("name").item(0).getTextContent()+" "+i,
								eElement.getElementsByTagName("torepo").item(0).getTextContent(), ratio, rndstart , rndstartvalue);
						CityStation citystation = new CityStation(maxinbw, maxoutbw, diskbw, reposize, sd, Scenario.randommetering); //utolso parameter a random kesleltetes volt,most ez a szenzorinditashoz kapcsolodo
						CityStation.getStations().add(citystation);
						Sensor itsensors = new Sensor();
						itsensors.readSensorData(stationfile,citystation,sensornumber);
						if(i!=(stationnumber-1)) {Sensor.counter--;}  // ez a sor biztositja a tobb ugyanolyan station megfelelo kezeleset
					}
					Scenario.simulatedTime=(Scenario.simulatedTime<time)?time:Scenario.simulatedTime;
					
					/*
					 * Letrejon a station, aztan a Sensor osztaly letrehozza a hozza tartozo szenzorokat.
					 * 
					 * */
				

					}
				}
			}
			
			if(tasksize!=-1){
				CloudsProvider cp = new CloudsProvider(Scenario.simulatedTime);
				Provider.readProviderXml(cp, providerfile,cproviderfile,filesize);
				int maxstation = CityStation.getStations().size() / cloudcount;
				for(int i=0;i<cloudcount;i++){
					CityStation.getStationvalue()[i]=0;
					Cloud cloud = new Cloud(cloudfile,null,null,null);
					Cloud.getClouds().add(cloud);
					ArrayList<CityStation> stations = new ArrayList<CityStation>();
					int stationcounter=CityStation.getStations().size()-1;
					while(stationcounter>=0){
						CityStation.getStations().get(stationcounter).setCloud(cloud);
						CityStation.getStations().get(stationcounter).setCloudnumber(i);
						stations.add(CityStation.getStations().get(stationcounter));
						CityStation.getStations().remove(stationcounter);
						stationcounter--;
						if(stations.size()>maxstation){
							break;
						}
					}
					//kesleltetest 3. parameter adta korabban, stationok kesleltetett indulasaert felelt (mar felesleges,de attekinthetoseg vegett egyenlore maradt)
					Application.getApp().add(new Application(appfreq,tasksize,true,print,cloud,stations,(i+1)+". app:",Provider.getProviderList().get(0)));
				}
			}
		
	}

	/**
	 * Feldolgozza a Station-oket leiro XML fajlt es elinditja a szimulaciot.
	 * @param va a virtualis kepfajl, default ertek hasznalathoz null-kent keruljon atadasra
	 * @param arc a VM eroforrasigenye, default ertek hasznalathoz null-kent keruljon atadasra
	 * @param datafile a Station-okat definialo XML fajl 
	 * @param cloudfile az IaaS felhot definialo XML fajl
	 * @param print logolasi funkcio, 1 - igen, 2 - nem
	 */
	public Scenario(String datafile,String cloudfile,String providerfile,String cproviderfile,int print,int cloudcount,long appfreq) throws Exception {
			Scenario.readStationXml(datafile, cloudfile, providerfile, cproviderfile, cloudcount, print, appfreq);	
			Timed.simulateUntilLastEvent();
			if(print==1) this.loging();
		}

		/**
		 * @param args Az elso argumentumkent adhato meg a Station-okat leiro XML eleresi utvonala
		 * 			masodik argumentumkent az IaaS-t leiro XML eleresi utvonala
		 * 			harmadik argumentumkent a provider-eket leiro XML fajl eleresi utvonala 
		 * 			negyedik argumentum egy cproviderfile
		 * 			ötödik egy szam, ami ha 1-es, akkor a logolasi funkcio be van kapcsolva
		 * 			hatodik argumentum adja meg hogy legyen-e random kesleltetett (0-20) szenzorindulas
		 * 			(jelenleg beegetve)
		 */
		public static void main(String[] args) throws Exception {
			
			/*
			String datafile=args[0];
			String cloudfile=args[1];
			String providerfile=args[2];
			String cproviderfile=args[3];
			int print=Integer.parseInt(args[4]);
			*/

			String datafile="C:\\Users\\David\\Desktop\\dissect-cf-pricing\\src\\main\\resources\\CityStation.xml";
			String cloudfile="C:\\Users\\David\\Desktop\\dissect-cf-pricing\\src\\main\\resources\\LPDSCloud.xml";
			String providerfile="C:\\Users\\David\\Desktop\\dissect-cf-pricing\\src\\main\\resources\\Provider.xml";
			String cproviderfile="C:\\Users\\David\\Desktop\\dissect-cf-pricing\\src\\main\\resources\\CProvider.xml";
			int print = 1;
			Scenario.randommetering=false;

			//int print=Integer.parseInt(args[4]);

			new Scenario(datafile,cloudfile,providerfile,cproviderfile,print,1,5*60000);	
			
			Timed.simulateUntilLastEvent();
		}
}