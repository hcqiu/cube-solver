import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.util.Arrays;
import java.lang.String;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RConsole;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;
import lejos.robotics.Color;




public class CubeSolver {
	//Define Sensors
		static UltrasonicSensor distance=new UltrasonicSensor(SensorPort.S4);//examine if a cube is on the platform
		static LightSensor light = new LightSensor(SensorPort.S1);
		static ColorSensor color = new ColorSensor(SensorPort.S3);
		
		//Define Motors
		static NXTRegulatedMotor paw=Motor.C; //Control the claw, which can hold the cube or turn it over.
		static NXTRegulatedMotor bottom=Motor.A; //Control the platform
		static NXTRegulatedMotor monitor=Motor.B; //Control the color sensor
		
		
		//the motor angle for paw to wait
		static byte PawWaitPosition = 60;
		//the motor angle for paw to lift
		static byte PawLiftPosition = 50;
		//the motor angle for paw to turn over the cube
		static byte PawTurnOverPosition = -66;
		//the motor angle for platform to rotate 90 degree
		static int BaseOneQuarter = 315;
		//the log for platform to correct biases
		static int BaseQuarters = 0;//3 ;
		//the fix angle for base
		static byte BaseRotateFix = 30;
		//the motor angle for monitor to wait
		static byte MonitorWaitPosition = 56;
		//the init position of color sensor motor(this will be set automatically)
		static byte ColorMotorBaseAngle = 0;
		//add offset positions for color sensor motor
		static int ColorMotorOffset1 = 118;
		static byte ColorMotorOffset2 = 85;
		static byte ColorMotorOffset3 = 71;
		static int ColorReadPostion1 = 157;
		static int ColorReadPostion2 = 158;
		//A flag to check if the cube is on the base
		static boolean hasError = false;
		//a log to record state when error occur
		static byte state = 0 ;
		
		//Record the positions of all center colors of the cube, with respect to the robot coordinate.
		public static byte[] CenterColor = new byte[] { 0, 1, 2, 3, 4, 5 };//{ 'B', 'F', 'D', 'U', 'L', 'R' };
		public static String[] CenterColor2 = new String[] { "F", "B", "R", "L", "U", "D" };
		
		public static byte[][] AllColor = {{2,2,2,2,2,2,0,0,0,} , 
				{1,1,7,5,0,1,1,1,1,} , 
				{5,3,2,5,5,0,1,1,3,} , 
				{0,5,5,2,1,0,0,0,2,} , 
				{7,5,5,7,3,3,3,7,7,} , 
				{3,3,3,3,7,7,5,7,7,} };//new byte[6][9] ;
		public static int[] ColorComparePosition = new int[18] ;
		public static float[] ColorCompareValue = new float[18] ;
		public static float[] ColorCompareValue2 = new float[18] ;
		public static byte x = 0 ;
		public static String[] sInput =new String[20] ;
		static byte[] bInput = new byte[48] ;
		
		
		
		/*static String faces = "RLFBUD" ;
		static char[] order = "AECGBFDHIJKLMSNTROQP".toCharArray();
		static char[] bithash = "TdXhQaRbEFIJUZfijeYV".toCharArray();
		static char[] perm = "AIBJTMROCLDKSNQPEKFIMSPRGJHLNTOQAGCEMTNSBFDHORPQ".toCharArray();
		static char[] pos = new char[20];
		static char[] ori = new char[20];
		static char[] val = new char[20];
		static char[][] tables = new char[8][];
		static int[] move = new int[20];
		static int[] moveamount = new int[20];
		static int phase = 0 ;
		static int[] tablesize = { 1 , 4096 , 6561 , 4096 , 256 , 1536 , 13824 , 576 } ;
		static int CHAROFFSET = 65 ;
		static char[] tmpStr = new char[20];*/
		
		
		
        //Display the color when color sensor scans the cube
		static void displayColor(String name, int raw, int calibrated, int line)
	    {
	        LCD.drawString(name, 0, line);
	        LCD.drawInt(raw, 5, 6, line);
	        LCD.drawInt(calibrated, 5, 11, line);
	    }
		
		


		public static void main (String[] arg) throws Exception
	    {
			//Terminate the process by pressing down the escape button in the case of emergency
			Button.ESCAPE.addButtonListener(new ButtonListener() {
			      @Override
				public void buttonPressed(Button b) 
			    {
			    	  
				        monitor.rotateTo(0);				        
						paw.setSpeed(150);
						paw.rotateTo(0);
						LCD.clear();
				        LCD.drawString("ESCAPE pressed", 0, 0);
						LCD.drawString(String.valueOf(state), 0, 1);						
				        System.exit(0);
			    }

			      @Override
				public void buttonReleased(Button b) 
			    {
			        LCD.clear();
			    }
			    });

			
			
            
	    	Robot.LiftPaw();//Allow someone to put the cube onto the platform
	    	
	    	//Thread.sleep(3000);
	    	//paw.rotateTo(0);
	    	light.setFloodlight(true);
	    	color.setFloodlight(Color.WHITE);
	    	
	    	//Sound.playSample(new File("yesmaster.wav"));
	    	//Sound.playSample(new File("cometoyuri.wav"));
	    	
	    	boolean isChaotic = true; //flag used to record a chaotic cube  
	    	
	    	while(!Button.ESCAPE.isDown())
	    	{
	    		//Wait for the distance being in the correct range: 12~16
	    		
				if(!Robot.hasCube())
				{
					//if the cube is taken away, we consume it is been upset
					isChaotic = true;
				}
	    		
		    	if(Robot.hasCube() && isChaotic)
		    	{
		    		//Initiate the error status
		   			hasError = false;
		  					
					Thread.sleep(1000);
					
					//Play some sound to notice the "Start"
					//Sound.playSample(new File("belongtous.wav"));

								
					Robot.LiftPaw();//Ensure that the paw is lifted at the beginning of the scanning process 	
					
					
		   			//Read Colors
					RConsole.open();					
		   			Robot.ReadAllSide();
		   			LCD.clear();
		   			
		   			
		   		    //Display the results on the screen                
		   			for(byte i=0;i<6;i++)
		   			{
		   				
		   			    for(byte j=0;j<9;j++)
		   				    {
		   			    	if (j==0)
		   			    	  RConsole.print("{"+AllColor[i][j]+",");
		   			    	else if (j==8)
		   			    	 RConsole.print(AllColor[i][j]+"," + "} ,");
		   			    	else
		   			    		RConsole.print(AllColor[i][j]+",");
		   				    }
		   			    RConsole.println(" ");
		   			}
		   			/*for(int i=0;i<18;i++)
		   			{
		   			    
		   				RConsole.print(ColorComparePosition[i]+"   ");
		   			    
		   			}
		   			RConsole.println(" ");
		   			for(int i=0;i<18;i++)
		   			{
		   			    
		   				RConsole.print(ColorCompareValue[i]+"   ");
		   			    
		   			}
		   			RConsole.println(" ");
		   			for(int i=0;i<18;i++)
		   			{
		   			    
		   				RConsole.print(ColorCompareValue2[i]+"   ");
		   			    
		   			}
		   			RConsole.println(" "); */
		   			
		   			
		   			
		   			Robot.SolveReadColors() ;
		   			for(byte i = 0 ; i < 20 ; i ++ )
		   			{
		   				if(i < 6)
		   					LCD.drawString(sInput[i] , 0 , i);
		   				else
		   				{
		   					if(i < 12)
		   						LCD.drawString(sInput[i] + " " , 3 , i - 6);
		   					else
		   				   {	if(i < 16)
		   							LCD.drawString(sInput[i] + " " , 6 , i - 12);
		   				        else
		   				        	LCD.drawString(sInput[i] + " " , 10 , i - 16);
		   				   }	
		   				}   				
		   			} 
		   			/*for(byte i=0;i<20;i++)
		   			{
		   			    
		   				RConsole.print(sInput[i]);
		   				RConsole.print(" ");
		   			    
		   			}*/
		   			
		   			
		   			byte step = 0 ;
		   			
		   			LCD.drawString("waiting", 0, 7);
		   			USBConnection conn = USB.waitForConnection();
					DataOutputStream dos = conn.openDataOutputStream() ;
					DataInputStream dis = conn.openDataInputStream();
					LCD.clear();
		            
                    //transport the data of the cube to PCCubeSolver
					dos.write(bInput);
					dos.flush();
                     
					
					//Receive a solution
					try
		              {
						step = dis.readByte() ;
		              }
		            catch (EOFException e) 
		              {
		                break;
		              }   
					
					LCD.drawString( String.valueOf(step)+"steps:" ,0,0);
					byte[] moveamount =new byte[step] ;//{2,2,2,2,2,2,2,2,} ;//
		   			byte[] move = new byte[step] ;//{3,0,5,1,5,2,5,1};
		   			byte[] movement = new byte[2 * step] ;
					for (byte i = 0 ; i < 2*step ; i ++)
					{
		   			  try
		                {
		   				movement[i] = dis.readByte() ;
		   				//LCD.drawString(String.valueOf(movement[i]) + " ",i,2);
		   				//Button.ENTER.waitForPress();
		                }
		              catch (EOFException e) 
		                {
		                  break;
		                }   
					}
					
					/*try
		              {
						dis.read(move) ;
		              }
		            catch (EOFException e) 
		              {
		                break;
		              } */
					for (byte i = 0 ; i < step ; i ++)
					{
						move[i] = movement[2*i] ; 
						moveamount[i] = movement[2*i + 1] ; 
					}
					
						
					dos.close();
					dis.close();
					conn.close();
					
					String steps = "" ;
					for (byte i=0; i<step; i++)
					{
						//RConsole.print(move[i] + " ");
						
						steps += "FBRLUD".charAt(move[i]) + "" + moveamount[i] ;
						steps += " " ;	}
					LCD.drawString(steps,0,1);
					//for (byte i = 0 ; i < step ; i ++)
					    //LCD.drawString(String.valueOf(move[i]), i, 1);
					//for (byte i = 0 ; i < step ; i ++)
					    //LCD.drawString(String.valueOf(moveamount[i]), i, 2);
					
					Button.ENTER.waitForPress();
					
					//Each step is a rotation of one side
					for ( byte i = 0 ; i < step ; i ++)
					{
						byte k = 0 ;
						
						//Find the side we want to rotate
						for (byte j = 0 ; j < 6 ; j ++)
						{
							if (CenterColor[j] == move[i])
							{
							    k = j ;
								break ;
							}
						}
						
						
						//turn the side downwards
						Robot.FindSidePosition(k) ;
						
						//To turn three quarters round clockwise, is equal to turn one quarter round counterclockwise
						if (moveamount[i] == 3)
							moveamount[i] = -1 ;
						Robot.RotateBottomSide(moveamount[i]);
						
						
						state += 1 ;// This value would be showed on the screen in case of error.
						/*if (BaseQuarters == 4)
							{
							paw.setSpeed(150);
							paw.rotateTo(PawLiftPosition);
							bottom.setSpeed(1000);
							bottom.rotateTo(0);
							paw.setSpeed(150);
							paw.rotateTo(0);
							BaseQuarters = 0;
							}
						if (BaseQuarters == -4)
							{
							paw.setSpeed(150);
							paw.rotateTo(PawLiftPosition);
							bottom.setSpeed(1000);
							bottom.rotateTo(0);
							paw.setSpeed(150);
							paw.rotateTo(0);
							BaseQuarters = 0;
							}*/
					}
					LCD.clear();
		   			//RConsole.print(cube.GetResult(sInput)) ;
		   			
		   			
			   			//Send 255 to start calculate
			   			//BlueTooth.WriteBytes(new byte[]{(byte)255});
			   			
			   			//The first return byte is the length of steps
		   				
			   			
			   			
			   			//int stepCount = readStepCount[0];
			   			//LCD.drawString("steps=" + stepCount, 1, 1);
			   			
			   			//Start to action
			   			//Sound.playSample(new File("watchandlearn.wav"));
		   				//RConsole.print(cube.GetResult(sInput)) ;
			   			//String result = cube.GetResult(sInput) ;
			   			//LCD.drawString(result , 0 , 0);
			   			//Send 254 to reset the pc data
			   			//BlueTooth.WriteBytes(new byte[]{(byte)254});
					
					
			   			//RConsole.close();
		   			
		   			if(!hasError)
		   			{
			   			//The cube has been solved
			   			isChaotic = false;
			   			
			   			Robot.LiftPaw();
						//Sound.playSample(new File("yes.wav"));
						Thread.sleep(1000);
						Robot.RotateBottom(8);
						Robot.LiftPaw();
						Thread.sleep(5000);
						
		   			}
		    	}
	    		Thread.sleep(500);
	    	}
	    	Thread.sleep(5000);
	    	paw.setSpeed(150);
			paw.rotateTo(0);
	    	
	    }

		
        //This subclass includes all methods controlling motion of the robot.
		public static class Robot
	    {			
			
			
			//Rotate the downward side with the claw holding the upper two layers.
			//This method would change the state of the cube, without changing the position of its center colors.
			/**
			 * @throws Exception  
			 */
			public static void RotateBottomSide(byte nQuarter) throws Exception 
			{
				//turn the platform a little more, since there are gaps between the cube and the railing of the platform
				//Without this modification, the downward side cannot be rotated to the perfect position.
				int nFixAngle = BaseRotateFix * ( nQuarter > 0 ? 1 : -1); 
				bottom.setSpeed(1000);
				paw.setSpeed(170);
				paw.rotateTo(-28);
				Thread.sleep(200);
				bottom.rotate(nQuarter * BaseOneQuarter + nFixAngle);
				bottom.rotate(-nFixAngle);//After turning the platform a little more, it need to be turned back.	
				BaseQuarters += nQuarter;
			}
			
			
			
			//Rotate the platform without the claw holding the upper two layers
			/**This method would change the position of the center colors, thus the global variable CenterColor
			need to be altered.*/
			//This method would not change the state of the cube.
			/**
			 * @throws Exception  
			 */
			public static void RotateBottom(int j)throws Exception 
			{
				int nPawLiftPosition = PawLiftPosition;
				paw.setSpeed(150);
				paw.rotateTo(nPawLiftPosition);
				bottom.setSpeed(1000);
				bottom.rotate(j * BaseOneQuarter);
				paw.setSpeed(170);//150
				paw.rotateTo(-28);
				BaseQuarters += j;
				int i = j % 4 ;
				switch(i)
				{
				case 1 :
					byte n = CenterColor[0] ;
					CenterColor[0] = CenterColor[3] ;
					CenterColor[3] = CenterColor[1] ;
					CenterColor[1] = CenterColor[2] ;
					CenterColor[2] = n ;
					break ;
				case -1 :
					byte m = CenterColor[0] ;
					CenterColor[0] = CenterColor[2] ;
					CenterColor[2] = CenterColor[1] ;
					CenterColor[1] = CenterColor[3] ;
					CenterColor[3] = m ;
					break ;
				case 2 :
					byte l = CenterColor[0] ;
					CenterColor[0] = CenterColor[1] ;
					CenterColor[1] = l ;
					l = CenterColor[2] ;
					CenterColor[2] = CenterColor[3] ;
					CenterColor[3] = l ;
					break ;
					
				}
			}
			
			
			//Turn the cube over, resulting in the modification of the center colors' positions.
			public static void RotatePaw() throws InterruptedException 
			{
				int nPawTurnOverPosition = PawTurnOverPosition;
				
				paw.setSpeed(170);//150
				paw.rotateTo(0);
				Thread.sleep(300);//300
				paw.setSpeed(180);//160
				paw.rotateTo(nPawTurnOverPosition);
				paw.setSpeed(320);//300
				paw.rotateTo(0);
				
				byte n = CenterColor[4] ;
				CenterColor[4] = CenterColor[0] ;
				CenterColor[0] = CenterColor[5] ;
				CenterColor[5] = CenterColor[1] ;
				CenterColor[1] = n ;
				
			}
			
			
			//Lift the paw, allowing the platform to rotate or someone to move the cube.
			public static void LiftPaw()
			{
			    paw.setSpeed(130);
	    	    int nPawWaitPosition = PawWaitPosition;
			    paw.rotateTo(nPawWaitPosition);				
			}
			
			
			
			//Scan colors of each side of the cube 
			public static void ReadAllSide() throws Exception
			{
				
				int nSideIndex=0;//Record the side by the scanning order.
				
				//upturn the 6 sides in sequence
				monitor.setSpeed(170);
				bottom.setSpeed(1000);
				ReadOneSide(nSideIndex++);
				//bottom.rotateTo(0);

				RotatePaw();
				ReadOneSide(nSideIndex++);
				//bottom.rotateTo(0);
				
				RotatePaw();
				ReadOneSide(nSideIndex++);
				//bottom.rotateTo(0);
				
				
				RotatePaw();
				ReadOneSide(nSideIndex++);
				//bottom.rotateTo(0);
						
				
				RotateBottom(-1);
				RotatePaw();
				ReadOneSide(nSideIndex++);
                
				
				RotatePaw();
				RotatePaw();
				ReadOneSide(nSideIndex);
				//bottom.rotate(-4);
				Robot.ColorItermCompare() ;
				monitor.rotateTo(0) ;
			}
			
			//Read one side by the index
			public static void ReadOneSide(int nSideIndex) throws Exception
			{
				
				//Add a delay time for the motor to be stable
				byte delay=120; 
				
				//The order for the scanner to read the nine colors within one side of the cube
				byte[] idx={0,5,6,7,8,1,2,3,4} ;
				//The order for the scanner to read six sides of the cube, chosen at the claw's convenience.
				byte[] idx2={3,1,2,0,5,4};
				
				byte i=0;
				
				Robot.LiftPaw();
				
				//Scan center color
				monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset1 - (0 * nSideIndex));
				Thread.sleep(delay);				
				ReadColor(idx2[nSideIndex], idx[i++]);
				
				//Scan borders				
				for(byte jj=0;jj<4;jj++)
				{
					
					monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset2 - (0 * nSideIndex ));
					Thread.sleep(delay);
					ReadColor(idx2[nSideIndex], idx[i++]);
				
					monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset3 - (0 * nSideIndex ) , true);				
					bottom.rotate(ColorReadPostion1);
					
					Thread.sleep(delay);
					ReadColor(idx2[nSideIndex], idx[i++]);
					bottom.rotate(ColorReadPostion2 , true);
				}
				
			    monitor.rotateTo(ColorMotorBaseAngle + 20 - (0 * nSideIndex) );
			    
			    
			}
			
			public static void ReadColor(byte center, byte n)
			{
				//Setting in leJOS packages
				String modes[] = {"Full", "Red", "Green", "Blue", "White", "None"};
				String colorNames[] = {"None", "Red", "Green", "Blue", "Yellow",
                        "Megenta", "Orange", "White", "Black", "Pink",
                        "Grey", "Light Grey", "Dark Grey", "Cyan"};
				LCD.drawString("Mode: " + modes[0], 0, 0);
                LCD.drawString("Color   Raw  Cal", 0, 1);
				ColorSensor.Color vals = color.getColor();
				ColorSensor.Color rawVals = color.getRawColor();
				displayColor("Red", rawVals.getRed(), vals.getRed(), 2);
                displayColor("Green", rawVals.getGreen(), vals.getGreen(), 3);
                displayColor("Blue", rawVals.getBlue(), vals.getBlue(), 4);
                displayColor("None", rawVals.getBackground(), vals.getBackground(), 5);
                LCD.drawString("Color:          ", 0, 6);
                LCD.drawString(colorNames[vals.getColor() + 1], 7, 6);
                LCD.drawString("Color val:          ", 0, 7);
                LCD.drawInt(vals.getColor(), 3, 11, 7);
                
				if(vals.getColor() != 3)
					AllColor[center][n] = (byte) vals.getColor() ;//record a number representing the color in AllColor
				else
				{
					/*float rr = rawVals.getRed() ;
					float rg = rawVals.getGreen();
					float rb = rawVals.getBlue() ;
					float r = vals.getRed() ;
					float g = vals.getGreen();
					float b = vals.getBlue() ;*/
					/*double[][] input = new double[][] {{rawVals.getRed()},{vals.getRed()},{rawVals.getGreen()},
						                               {vals.getGreen()},{rawVals.getBlue()},{vals.getBlue()},
						                               {rawVals.getBackground()},{vals.getBackground()}};
					double[][][] weights = new double[][][] {{{ -1.18452599e+00,   4.95145483e-01,  -1.06663329e+00,
				          2.42492834e+00,  -7.43844416e-01,   1.71450281e+00,
				          -2.98326149e-01,  -6.89658000e-01},
				        { -1.82344473e+00,  -3.49320103e-03,   8.00327013e-02,
				          -3.10553914e+00,  -1.59781334e+00,  -6.74079038e-01,
				          -1.00256119e+00,  -3.16862826e-01},
				        {  1.03770987e+00,   1.67211822e+00,  -5.82149753e-02,
				          -3.32865988e-01,  -2.50010499e-01,   5.37743717e-01,
				           8.34966792e-01,   6.39069908e-01},
				        { -5.81569363e-02,  -1.08198850e+00,   1.59940227e-01,
				           1.18060766e-03,   2.23981514e-01,  -1.26807992e+00,
				           2.63187396e+00,   1.29324115e+00},
				        {  1.57792148e-01,  -5.97269357e-01,   1.39791337e+00,
				           8.66910595e-01,  -6.96299569e-01,   6.03611958e-01,
				           1.71570417e-01,   1.91045450e+00},
				        {  4.69580707e-01,   9.90975862e-01,   9.78546769e-01,
				          -7.42698946e-01,  -1.41928317e+00,  -3.76374402e-01,
				           1.05499327e+00,   4.42593166e-01},
				        { -1.78971598e-01,   2.88941019e-01,  -2.43176561e-01,
				           9.48076199e-01,   2.41329469e-02,  -1.04289272e+00,
				          -1.03904125e+00,  -1.53368164e-01},
				        { -4.85878650e-01,   3.60407016e-01,  -2.40035816e-01,
				           9.45778746e-01,  -2.32919285e+00,   5.12182523e-02,
				          -1.24441665e+00,  -3.86742139e-01},
				        { -3.72092112e-01,  -8.63976935e-02,   2.96016519e+00,
				          -2.36299254e-01,  -4.12210113e-03,  -1.53586345e+00,
				          -5.40172574e-01,   3.48267274e-01},
				        {  1.39995085e+00,  -8.20164587e-02,  -3.22856607e-01,
				           6.72505645e-01,  -1.24585109e+00,   8.63331561e-01,
				           1.53796350e-01,   9.96154015e-03}}, {{ 0.81559845,  0.72431866,  0.82500247, -2.49082272,  1.12455635,
				         -0.937228  ,  1.22980066, -0.06772444,  0.97651422,  0.82798062},
				        {-0.33683079, -0.26247679, -1.82933353,  2.71249011,  2.1990736 ,
				         -0.10822724, -0.24064902, -1.32566865, -0.99074143, -0.21983158}}};
				         
				         double[][][] biases = new double[][][]  {{{ 0.48792263},
				             { 0.04282902},
				             {-1.84644067},
				             { 0.36362997},
				             { 0.8794715 },
				             {-0.42133958},
				             {-0.97011682},
				             { 1.41240353},
				             {-0.3487816 },
				             {-0.61318014}}, {{-0.37067989},
				             {-1.70408579}}};
                         
				         for (byte i = 0 ; i < 2 ; i++)
				         {
				        	 input = matrix.sigmoid( matrix.plusMatrix( matrix.multiMatrix( weights[i] , input ), biases[i] ) );
				         }
				         
				         if (input[0][0] < input[1][0])
				        	 AllColor[center][n] = 0 ;
				         else
				        	 AllColor[center][n] = 5 ;*/
					
					ColorCompareValue[x] = vals.getRed() + vals.getBlue() - 2*vals.getGreen();
				    ColorComparePosition[x] = 10*center + n ; 
				    x += 1 ;
					
					
	                
				}
					
					
					
					
					//ColorIterm c1 = new ColorIterm(vals.getRed() , vals.getGreen() , vals.getBlue() , rawVals.getRed() , rawVals.getGreen() , rawVals.getGreen() , center , n) ;
					

				//send to PC by bluetooth
				
				
				 
				
			}
			
			
			
			
			//check if the cube is still on the platform
			public static boolean CheckCubeReady() throws Exception
			{
				//if already error, return directly to avoid play *.wav again
				if(hasError) return false;
				
			    int d = distance.getDistance();
				int errorCount = 0;
				while((d<0 || d>13) && errorCount < 10)
				{
					errorCount++;
					Thread.sleep(20);
				}
				if(errorCount >= 10)
				{
					//The cube is break out;
					hasError = true;
					Sound.playSample(new File("sensetrouble.wav"));
				}
				return !hasError;
			}
			
			
			//Decide whether the cube is on the platform or not
            public static boolean hasCube() throws InterruptedException 
            {
            	int CheckStatusTimes=0;
            	LCD.clear();
            	boolean previousStatus = true;
            	boolean currentStatus = true;
            	while(CheckStatusTimes++ < 5)
            	{
    				int n =  distance.getDistance();
    				LCD.drawString("distance=" + n + "   ", 0, 0);
    				currentStatus = (n>=0 && n<=14); //If true, there is a cube on the platform
    				if(currentStatus != previousStatus)
    				{
    					CheckStatusTimes = 0;
    					previousStatus = currentStatus;
    				}
    				Thread.sleep(100);
    			}
            	return currentStatus;
            	
            }
            
            
            //This method would turn the desirable side downward
            public static void FindSidePosition(byte j) throws Exception
            {
            	
            	switch(j)
            	{
            	case 0 :
            		Robot.RotateBottom(2);
            		Robot.RotatePaw();
            		break ;
            	case 1 :
            		Robot.RotatePaw();
            		break ;
            	case 2 :
            		Robot.RotateBottom(1);
            		Robot.RotatePaw();
            		break ;
            	case 3 :
            		Robot.RotateBottom(-1);
            		Robot.RotatePaw();
            		break ;
            	case 4 :
            		Robot.RotatePaw();
            		Robot.RotatePaw();
            	case 5 :
            		break ;
            	}
            }
            
            public static void ColorItermCompare()
            {
            	
            	for(int i = 0 ; i < 18 ; i ++ )
            		ColorCompareValue2[i] = ColorCompareValue[i] ;
            	Arrays.sort(ColorCompareValue) ;
            	for(int i = 0 ; i < 18 ; i ++ )
            	{
            		int j = Arrays.binarySearch( ColorCompareValue , ColorCompareValue2[i]) ;
            		RConsole.print(j+" ");
	   			    RConsole.println(" ");
            		int n = ColorComparePosition[i] % 10 ;
            		int c = ( ColorComparePosition[i] - n ) / 10 ;
            		if (j < 9)
            			AllColor[c][n] = 3 ;
            		else
            			AllColor[c][n] = 5 ;           			
            	}
            }
            
            
            
            /**Convert the data of the cube in the coordinate of the robot's platform, 
            to a standard representation of Rubik's cube.*/
			public static void SolveReadColors()
			{
				
				
				//This part aims to 
				byte[] Center = new byte[6] ;		
				for (byte c = 0 ; c < 6 ; c ++)
				{
					Center[c] = AllColor[c][0] ;
				}
				
				
				for (byte c = 0 ; c < 6 ; c ++)
				{
					for (byte n = 0 ; n < 9 ; n ++)
					{
						for (byte j = 0 ; j < 6 ; j ++)
						{
							if (Center[j] == AllColor[c][n])
							{
								AllColor[c][n] = j ;
								break ;
							}
						}		
					}
				}
				
				
				
				
				
				sInput[0] = CenterColor2[AllColor[4][5]] + CenterColor2[AllColor[0][7]] ;//UF
				sInput[1] = CenterColor2[AllColor[4][3]] + CenterColor2[AllColor[2][7]] ;//UR
				sInput[2] = CenterColor2[AllColor[4][1]] + CenterColor2[AllColor[1][7]] ;//UB
				sInput[3] = CenterColor2[AllColor[4][7]] + CenterColor2[AllColor[3][7]] ;//UL
				sInput[4] = CenterColor2[AllColor[5][1]] + CenterColor2[AllColor[0][3]] ;//DF
				sInput[5] = CenterColor2[AllColor[5][3]] + CenterColor2[AllColor[2][3]] ;//DR
				sInput[6] = CenterColor2[AllColor[5][5]] + CenterColor2[AllColor[1][3]] ;//DB
				sInput[7] = CenterColor2[AllColor[5][7]] + CenterColor2[AllColor[3][3]] ;//DL
				sInput[8] = CenterColor2[AllColor[0][1]] + CenterColor2[AllColor[2][5]] ;//FR
				sInput[9] = CenterColor2[AllColor[0][5]] + CenterColor2[AllColor[3][1]] ;//FL
				sInput[10] = CenterColor2[AllColor[1][5]] + CenterColor2[AllColor[2][1]] ;//BR
				sInput[11] = CenterColor2[AllColor[1][1]] + CenterColor2[AllColor[3][5]] ;//BL
				
				sInput[12] = CenterColor2[AllColor[4][4]] + CenterColor2[AllColor[0][8]] + CenterColor2[AllColor[2][6]] ;//UFR
				sInput[13] = CenterColor2[AllColor[4][2]] + CenterColor2[AllColor[2][8]] + CenterColor2[AllColor[1][6]] ;//URB
				sInput[14] = CenterColor2[AllColor[4][8]] + CenterColor2[AllColor[1][8]] + CenterColor2[AllColor[3][6]] ;//UBL
				sInput[15] = CenterColor2[AllColor[4][6]] + CenterColor2[AllColor[3][8]] + CenterColor2[AllColor[0][6]] ;//ULF
				
				sInput[16] = CenterColor2[AllColor[5][2]] + CenterColor2[AllColor[2][4]] + CenterColor2[AllColor[0][2]] ;//DRF
				sInput[17] = CenterColor2[AllColor[5][8]] + CenterColor2[AllColor[0][4]] + CenterColor2[AllColor[3][2]] ;//DFL
				sInput[18] = CenterColor2[AllColor[5][6]] + CenterColor2[AllColor[3][4]] + CenterColor2[AllColor[1][2]] ;//DLB
				sInput[19] = CenterColor2[AllColor[5][4]] + CenterColor2[AllColor[1][4]] + CenterColor2[AllColor[2][2]] ;//DBR
				
				
				bInput[0] =  AllColor[4][5] ;
				bInput[1] =  AllColor[0][7] ;//UF
				bInput[2] =  AllColor[4][3] ;
				bInput[3] =  AllColor[2][7] ;//UR
				bInput[4] =  AllColor[4][1] ;
				bInput[5] =  AllColor[1][7] ;//UB
				bInput[6] =  AllColor[4][7] ;
				bInput[7] =  AllColor[3][7] ;//UL
				bInput[8] =  AllColor[5][1] ;
				bInput[9] = AllColor[0][3] ;//DF
				bInput[10] = AllColor[5][3] ;
				bInput[11] = AllColor[2][3] ;//DR
				bInput[12] = AllColor[5][5] ;
				bInput[13] = AllColor[1][3] ;//DB
				bInput[14] = AllColor[5][7] ;
				bInput[15] = AllColor[3][3] ;//DL
				bInput[16] = AllColor[0][1] ;
				bInput[17] = AllColor[2][5] ;//FR
				bInput[18] = AllColor[0][5] ;
				bInput[19] = AllColor[3][1] ;//FL
				bInput[20] = AllColor[1][5] ; 
				bInput[21] = AllColor[2][1];//BR 
				bInput[22] = AllColor[1][1] ;
				bInput[23] = AllColor[3][5] ;//BL
				
				bInput[24] = AllColor[4][4] ;
				bInput[25] = AllColor[0][8] ;
				bInput[26] = AllColor[2][6] ;//UFR
				bInput[27] = AllColor[4][2] ;
				bInput[28] = AllColor[2][8]  ;
				bInput[29] = AllColor[1][6] ;//URB
				bInput[30] = AllColor[4][8] ;
				bInput[31] = AllColor[1][8] ;
				bInput[32] = AllColor[3][6] ;//UBL
				bInput[33] = AllColor[4][6]  ;
				bInput[34] = AllColor[3][8] ;
				bInput[35] = AllColor[0][6] ;//ULF
				
				bInput[36] = AllColor[5][2] ;
				bInput[37] = AllColor[2][4] ;
				bInput[38] = AllColor[0][2] ;//DRF
                bInput[39] = AllColor[5][8] ;
                bInput[40] = AllColor[0][4] ;
                bInput[41] = AllColor[3][2] ;//DFL
				bInput[42] = AllColor[5][6] ;
				bInput[43] = AllColor[3][4] ;
				bInput[44] = AllColor[1][2] ;//DLB
				bInput[45] = AllColor[5][4] ;
				bInput[46] = AllColor[1][4] ;
				bInput[47] = AllColor[2][2] ;//DBR
				
			}
			
			
	    }
		
		
		public static class matrix
		{
			
			public static double[][] multiMatrix(double[][] MA , double[][] MB)
			{
				double[][] multimatrix = new double[ MA.length ][ MB[0].length ];
				for (byte i = 0 ; i < MA.length ; i ++)
				{
					for (byte j = 0 ; j < MB[0].length ; j ++)
					{
						multimatrix[i][j] = 0 ;
						for (byte k = 0 ; k < MB.length ; k ++)
						{
							multimatrix[i][j] += MA[i][k]*MB[k][j] ;
						}
					}
				}
				return multimatrix ;
			}
			
			public static double[][] plusMatrix(double[][] MA , double[][] MB)
			{
				double[][] plusmatrix = new double[ MA.length ][ MA[0].length ];
				for (byte i = 0 ; i < MA.length ; i ++)
				{
					for (byte j = 0 ; j < MA[0].length ; j ++)
					{
						plusmatrix[i][j] = MA[i][j] + MB[i][j] ;
					}
				}
				return plusmatrix ;
			}
			
			public static double[][] sigmoid(double[][] z)
			{
				double[][] a = new double[ z.length ][ z[0].length ];
				for (byte i = 0 ; i < z.length ; i ++)
				{
					for (byte j = 0 ; j < z[0].length ; j ++)
					{
						a[i][j] = 1.0/(1.0 + Math.pow( Math.E , -z[i][j])) ;
					}
				}
				return a ;
			}
			
		}
		
		
		//This class would be moved into PCCubeSolver, since my robot cannot handle this.
		/*public static class cube
		{
			
			public static String GetResult(String[] Input) throws Exception
			{
				
				
				phase = 0;
				
				
				String sOutput = "" ;
				
				
				
				int f , i = 0 , j = 0 , k = 0 , pc , mor ;
				
				for ( ; k< 20 ; k ++ )
				{	
					tmpStr = Character.toChars(k < 12 ? 2 : 3) ;
				    val[k] = tmpStr[0];
				}
				
				for ( ; j < 8 ; j ++ )
					filltable(j) ;
				
				for ( ; i < 20 ; i ++ )
				{
					f = pc = k = mor = 0 ;
					for ( ; f < val[i] ; f ++ )
					{
						j = faces.indexOf(Input[i].charAt(f)) ;
						if (j>k)
						{
							k = j ;
							mor = f ;
						}
						pc += 1 << j ;
					}
					for (f = 0 ; f < 20 ; f ++)
						if (pc == bithash[f]-64)
							break ;
					
					tmpStr = Character.toChars(f) ;
					pos[order[i] - CHAROFFSET] = tmpStr[0] ;
					tmpStr = Character.toChars(mor % val[i]) ;
					ori[order[i] - CHAROFFSET] = tmpStr[0];
				}
				for ( ; phase < 8 ; phase += 2)
				{
					for (j=0; ! searchphase(j , 0 , 9) ; j++ )
						;
					for (i=0; i<j; i++)
					{
						RConsole.print(move[i] + " ");
						sOutput += "FBRLUD".charAt(move[i]) + "" + moveamount[i] ;
						sOutput += " " ;	
						//Robot.FindSidePosition(move[i]) ;
						//if (moveamount[i] == 3)
						//	moveamount[i] = -1 ;
						//Robot.RotateBottomSide(moveamount[i]);
						
					}	
				}
				
				return sOutput ;
			}
			
			public static int Char2Num(char c)
			{
				return (int) c - CHAROFFSET ;
			}
			
			public static void cycle(char[] p , char[] a , int offset)
			{
				char temp = p[Char2Num(a[0 + offset])];
				p[Char2Num(a[0 + offset])] = p[Char2Num(a[1 + offset])] ;
				p[Char2Num(a[1 + offset])] = temp ;
				temp = p[Char2Num(a[0 + offset])] ;
				p[Char2Num(a[0 + offset])] = p[Char2Num(a[2 + offset])] ;
				p[Char2Num(a[2 + offset])] = temp ;
				temp = p[Char2Num(a[0 + offset])] ;
				p[Char2Num(a[0 + offset])] = p[Char2Num(a[3 + offset])] ;
				p[Char2Num(a[3 + offset])] = temp ;			
			}
			
			public static void twist(int i , int a)
			{
				i -= CHAROFFSET ;
				tmpStr = Character.toChars(((int) ori[i] + a + 1)%val[i]) ;
				ori[i] = tmpStr[0] ;
			}
			
			public static void reset()
			{
				for (int i = 0 ; i < 20 ;)
				{
					tmpStr = Character.toChars(i) ;
					pos[i] = tmpStr[0] ;
					ori[i ++ ] = '\0' ;	
				}
			}
			
			public static int permtonum(char[] p , int offset)
			{
				int n = 0 ;
				for(int a = 0 ; a < 4 ; a ++ )
				{
					n *= 4-a ;
					for(int b = a ; ++ b < 4 ;)
						if(p[b + offset] < p[a + offset])
							n ++ ;
				}
				return n ;
			}
			
			public static void numtoperm(char[] p , int n , int o)
			{
				tmpStr = Character.toChars(o) ;
				p[3 + o] = tmpStr[0] ;
				for (int a = 3 ; a -- > 0 ;)
				{
					tmpStr = Character.toChars(n % (4 - a) + o) ;
					p[a + o] = tmpStr[0] ;
					n /= 4 - a ;
					for (int b=a ; ++ b < 4 ;)
						if (p[b + o] >= p[a + o])
							p[b + o] ++ ;			
				}
			}
			
			public static int getposition(int t)
			{
				int i = -1 , n = 0 ;
				switch (t)
				{
				    case 1 :
					    for ( ; ++ i < 12 ;)
					    	n += ((int) ori[i]) << i ;
					    break ;
				    case 2 :
				    	for (i = 20 ; -- i > 11 ;)
				    		n = n *3 + (int) ori[i] ;
				    	break ;
				    case 3 :
				    	for ( ; ++ i < 12 ;)
				    		n += ((((int) pos[i]) & 8) > 0) ? (1 << i) : 0 ;
				    	break ;
				    case 4 :
				    	for ( ; ++ i < 8 ;)
				    		n += ((((int) pos[i]) & 4) > 0) ? (1 << i) : 0 ;
				        break ;
				    case 5 :
				    	int[] corn = new int[8] ;
				    	int[] corn2 = new int[4] ;
				    	int j ,
				    	k ,
				    	l ;
				    	k = j = 0 ;
				    	for ( ; ++ i <8 ;)
				    		if (((l = pos[i + 12] - 12) & 4) > 0)
				    		{
				    			corn[l] = k ++ ;
				    			n += 1 << i ;		    			
				    		}
				    		else
				    			corn[j ++ ] = l ;
				    	for(i = 0 ; i < 4 ; i ++)
				    		corn2[i] = corn[4 + corn[i]] ;
				    	for( ; -- i > 0 ;)
				    		corn2[i] ^= corn2[0] ;
				    	
				    	n = n * 6 + corn2[1] * 2 - 2 ;
				    	if (corn2[3] < corn2[2])
				    		n ++ ;
				    	break ;
				    case 6 :
				    	n = permtonum(pos , 0) * 576 + permtonum(pos , 4) * 24 + permtonum(pos , 12) ;
				    	break ;
				    case 7 :
				    	n = permtonum(pos , 8) * 24 + permtonum(pos , 16) ;
				    	break ;
				    			    	
				}
				return n ;
			}
			
			public static void setposition(int t , int n)
			{
				int i = 0 , j = 12 , k = 0 ;
				char[] corn = "QRSTQRTSQSRTQTRSQSTRQTSR".toCharArray() ;
				reset() ;
				switch (t)
				{
				
				case 1 :
					for ( ; i < 12 ; i ++ , n >>= 1)
					{
						tmpStr = Character.toChars(n & 1) ;
						ori[i] = tmpStr[0] ;
					}
					break ;
				case 2 :
					for (i = 12 ; i < 20 ; i ++ , n /= 3)
					{
						tmpStr = Character.toChars(n % 3) ;
						ori[i] = tmpStr[0] ;
					}
					break ;
				case 3 :
					for ( ; i < 12 ; i ++ , n >>= 1)
					{
						tmpStr = Character.toChars(8 * n & 8) ;
						pos[i] = tmpStr[0] ;
					}
					break ;
				case 4 :
					for ( ; i < 8 ; i ++ , n >>= 1)
					{
						tmpStr = Character.toChars(4 * n & 4) ;
						pos[i] = tmpStr[0] ;
					}
					break ;
				case 5 :
					int offset = n % 6 * 4 ;
					n /= 6 ;
					for ( ; i < 8 ; i ++ , n >>=1)
					{
						tmpStr = Character.toChars(((n & 1) > 0) ? corn[offset + k ++] -CHAROFFSET : j ++ ) ;
						pos[i + 12] = tmpStr[0] ;				
					}
					break ;
				case 6 :
					numtoperm(pos , n % 24 , 12) ;
					n /= 24 ;
					numtoperm(pos , n % 24 , 4) ;
					n /= 24 ;
					numtoperm(pos , n , 0) ;
					break ;
				case 7 :
					numtoperm(pos , n / 24 , 8) ;
					numtoperm(pos , n % 24 , 16) ;
					break ;
				} 
				
			}
			public static void domove(int m)
			{
				
				int offset = 8 * m ;
				int i = 8 ;
				
				cycle(pos , perm , offset) ;
				cycle(ori , perm , offset) ;
				
				cycle(pos , perm , offset + 4) ;
				cycle(ori , perm , offset + 4) ;
				
				if (m < 4)
					for ( ; -- i > 3 ;)
						twist(perm[i + offset] , i & 1) ;
				
				if (m < 2)
					for (i = 4 ; i -- > 0 ;)
						twist(perm[i + offset] , 0) ; 			
			}
			
			public static void filltable(int ti)
			{
				int n = 1 , l = 1 , tl = tablesize[ti] ;
				char[] tb = new char[tl] ;
				tables[ti] = tb ;
				for (int i = 0 ; i < tb.length ; i ++ )
					tb[i] = '\0' ;
				
				reset() ;
				tmpStr = Character.toChars(1) ;
				tb[getposition(ti)] = tmpStr[0] ;
				
				while (n > 0) 
				{
					n = 0 ;
					 
					for (int i = 0 ; i < tl ; i ++ )
					{
						if (tb[i] == l)
						{
							setposition(ti , i) ;
							for (int f = 0 ; f < 6 ; f ++ )
							{
								for (int q = 1 ; q < 4 ; q ++ )
								{
									domove(f) ;
									
									int r = getposition(ti) ;
									
									if ((q == 2 || f >= (ti & 6)) && tb[r] == '\0')
									{
										tmpStr = Character.toChars(l + 1) ;
										tb[r] = tmpStr[0] ;
										n ++ ;
									}
								}
								domove(f) ;
							}
						}
					}
					l ++ ;
				}
				
			} 
			
			public static boolean searchphase(int movesleft , int movesdone , int lastmove)
			{
				if (tables[phase][getposition(phase)] - 1 > movesleft || tables[phase + 1][getposition(phase + 1)] - 1 > movesleft)
					return false ;
				
				if (movesleft == 0)
					return true ;
				
				for (int i = 6 ; i -- > 0 ;)
				{
					if ((i - lastmove != 0) && ((i - lastmove + 1) != 0 || ((i | 1) != 0)))
					{
						move[movesdone] = i ;
						for (int j = 0 ; ++ j < 4 ;)
						{
							domove(i) ;
							moveamount[movesdone] = j ;
							if ((j == 2 || i >= phase) && searchphase(movesleft - 1 , movesdone + 1 , i))
								return true ;
						}
						domove(i) ;
					}
				}
				return false ;
			}
		}*/


		
		
		
		
}

