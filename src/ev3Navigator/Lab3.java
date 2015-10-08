package ev3Navigator;

import java.util.LinkedList;
import java.util.Queue;

import ev3Odometer.Odometer;
import ev3Odometer.OdometryDisplay;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import ev3WallFollower.BangBangController;
import ev3WallFollower.UltrasonicPoller;
import ev3WallFollower.PController;

public class Lab3 {

	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor neckMotor= new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final Port usPort = LocalEV3.get().getPort("S1");


	// Constants
	public static final double WHEEL_RADIUS = 2.25;
	public static final double TRACK = 16.2;

	private static final int bandCenter = 25;			// Offset from the wall (cm)
	private static final int bandWidth = 3;				// Width of dead band (cm)
	private static final double  tileLength = 30.48;


	private static final double [][] coordinates1 = {	{2,1}, {1,1}, {1,2}, {2,0}	};
	private static final double [][] coordinates2 = {	{0,2}, {2,0}	};


	public static void main(String[] args) {
		int buttonChoice;

		//Create LCD display object
		final TextLCD t = LocalEV3.get().getTextLCD();

		//initiate odometer and odometer functions
		Odometer odometer = new Odometer(WHEEL_RADIUS, TRACK, leftMotor, rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t);



		//Set up controller
		PController pController = new PController(leftMotor, rightMotor, bandCenter, bandWidth);

		SensorModes usSensor = new EV3UltrasonicSensor(usPort);		// usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance");	// usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()];		// usData is the buffer in which data are returned
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData);

		//set up the navigator object
		Navigator navigator = new Navigator(odometer, usPoller, pController, leftMotor, rightMotor, neckMotor, WHEEL_RADIUS, TRACK);

		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("Drive  |Obstacle", 0, 2);
			t.drawString("pattern|Avoid   ", 0, 3);
			t.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice == 0 );

		switch(buttonChoice) {

		case Button.ID_LEFT :

			odometer.start();
			odometryDisplay.start();
			usPoller.start();
			navigator.setCoordinates(createCoordinatesQueue(coordinates1));
			navigator.start();
			break;

		case Button.ID_RIGHT:

			usPoller.start();
			odometer.start();
			odometryDisplay.start();
			navigator.setCoordinates(createCoordinatesQueue(coordinates2));
			navigator.start();
			break;

		default:

			System.exit(0);
			break;

		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);

	}

	public static Queue<Coordinate> createCoordinatesQueue( double coordinates[][])
	{
		Queue<Coordinate> coordinatesQueue = new LinkedList<Coordinate>();

		for (int x = 0 ; x < coordinates.length; x++)
			coordinatesQueue.add(new Coordinate(coordinates[x][0]*tileLength,coordinates[x][1]*tileLength));

		return coordinatesQueue;
	}

}
