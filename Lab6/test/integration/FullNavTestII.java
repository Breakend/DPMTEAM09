package integration;

import bluetoothclient.BluetoothReceiver;
import movement.Driver;
import navigation.Map;
import navigation.PathTraveller;
import lejos.nxt.Sound;
import lejos.robotics.navigation.Waypoint;
import localization.Localization;
import localization.LocalizationI;
import objectdetection.ObstacleDetector;
import odometry.Odometer;
import odometry.OdometerCorrection;
import robotcore.Configuration;
import robotcore.Coordinate;
import robotcore.LCDWriter;
import sensors.LineReader;
import sensors.UltrasonicPoller;

/**
 * Basic test to scan a tile in front of the robot
 * Place obstacle at different points in the tile and see if the scanner picks up
 * the obstacle correctly. This is an extension of the fullNaveTest with inteegrated Localization 
 * 
 * NOTE: This is without odometry correction and pretty slow.
 * 
 * @author Peter Henderson
 *
 */
public class FullNavTestII {

	private static boolean followPath(){
		PathTraveller t = PathTraveller.getInstance();
		Driver driver = Driver.getInstance();
		ObstacleDetector detector = ObstacleDetector.getInstance();
		Map map = Map.getInstance();
		Configuration conf = Configuration.getInstance();
		
		while(!t.pathIsEmpty()){
			Waypoint next = t.popNextWaypoint();
			//Turn to the next tile
			driver.turnTo(Coordinate.calculateRotationAngle(
												Odometer.getInstance().getCurrentCoordinate(), 
												new Coordinate(next)));
			
			//scan the next area
			if(detector.scanTile()){
				//block next tile
				map.blockNodeAt(next.x, next.y);
				return false;
			}
			
			driver.travelTo(new Coordinate(next));
		}
		
		return true;
			
	}
	
	public static void main(String[] args){
		//===============get info from blue tooth ========\
		
//		BluetoothReceiver br = new BluetoothReceiver(); 
//		br.listenForStartCommand();// info in Config should be set 
		
		
		
		//=================INIT THREADS==========================
		LCDWriter lcd = LCDWriter.getInstance();
		Configuration conf = Configuration.getInstance();		
		UltrasonicPoller up = UltrasonicPoller.getInstance();
		PathTraveller traveller = PathTraveller.getInstance();
		LineReader llr = LineReader.getLeftSensor();	//left + right line reader
		LineReader rlr = LineReader.getRightSensor();
		Odometer odo = Odometer.getInstance();
		OdometerCorrection oc = OdometerCorrection.getInstance();
		Driver dr = Driver.getInstance();

		up.start();
		odo.start();
		llr.start();
		rlr.start();
		lcd.start();
		
		//=================INIT var ==========================
		Coordinate [] flagZone = conf.getFlagZone();
		//=====================INIT END=========================
		
		//do localization 
		LocalizationI.localizeAndMoveToStartLoc();
		
		
		//====end of localization 
		LineReader.subscribeToAll(oc);
		
		try {Thread.sleep(1000);}catch(Exception e){};
		traveller.recalculatePathToCoords(105,105);
//		traveller.recalculatePathToCoords((int)flagZone[0].getX(),(int) flagZone[0].getY());

		boolean done  = false;
		while(!done){
			done = followPath();
			try{
			if(!done)
				traveller.recalculatePathToCoords(105,105);
//				traveller.recalculatePathToCoords((int)flagZone[0].getX(),(int) flagZone[0].getY());
			else break;
			}
			catch(Exception e){
				lcd.writeToScreen("E: "+ e.toString(), 1);
			}
		}
		
		
		//indicate finish
		Sound.beepSequenceUp();		
		
	}
	
}
