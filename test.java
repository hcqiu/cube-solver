
import java.io.DataOutputStream;
import java.io.File;












import java.io.OutputStream;


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
import lejos.robotics.Color;
import lejos.util.TextMenu;


/**
 * Test program for the Lego Color Sensor.
 * @author andy
 */

public class test
{   
	//Define Sensors
	static UltrasonicSensor distance=new UltrasonicSensor(SensorPort.S4);
	static LightSensor light = new LightSensor(SensorPort.S1);
	static ColorSensor color = new ColorSensor(SensorPort.S3);
	
	//Define Motors
	static NXTRegulatedMotor paw=Motor.C;
	static NXTRegulatedMotor bottom=Motor.A;
	static NXTRegulatedMotor monitor=Motor.B;
	
	
	//the motor angle for paw to wait
	static int PawWaitPosition = 60;
	//the motor angle for paw to lift
	static int PawLiftPosition = 50;
	//the motor angle for paw to turn over the cube
	static int PawTurnOverPosition = -66;
	//the motor angle for base to rotate 90 degree
	static int BaseOneQuarter = 315;
	//the fix angle for base
	static int BaseRotateFix = 40;
	//the motor angle for monitor to wait
	static int MonitorWaitPosition = 56;
	//the init position of color sensor motor(this will be set automatically)
	static int ColorMotorBaseAngle = 0;
	//add offset positions for color sensor motor
	static int ColorMotorOffset1 = 120;
	static int ColorMotorOffset2 = 85;
	static int ColorMotorOffset3 = 71;
	static int ColorReadPostion1 = 157;
	static int ColorReadPostion2 = 158;
	//A flag to check if the cube is on the base
	static boolean hasError = false;
	
	public static byte[] CenterColor = new byte[] { 0, 1, 2, 3, 4, 5 };//{ 'B', 'F', 'D', 'U', 'L', 'R' };
	public static String[] CenterColor2 = new String[] { "F", "B", "R", "L", "U", "D" };
	public static int[][] AllColor = {{3,6,6,3,3,1,1,2,6},
           {6,5,2,2,2,1,2,0,0},
           {1,3,3,6,2,2,5,6,6},
           {2,6,1,1,3,1,1,3,6},
           {5,3,5,5,1,5,0,2,3},
           {0,5,0,0,5,0,0,0,5}};
			//new int[6][9] ;
	public static int[] ColorComparePosition = new int[18] ;
	public static float[] ColorCompareValue = new float[18] ;
	public static float[] ColorCompareValue2 = new float[18] ;
	public static int x = 0 ;
	public static String[] sInput =new String[20] ;
	
	
	
	static String faces = "RLFBUD" ;
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
	static char[] tmpStr = new char[20];

	static void displayColor(String name, int raw, int calibrated, int line)
    {
        LCD.drawString(name, 0, line);
        LCD.drawInt(raw, 5, 6, line);
        LCD.drawInt(calibrated, 5, 11, line);
    }
	public static void RotateBottomSide(int i) throws Exception 
	{
		int nFixAngle = BaseRotateFix * ( i > 0 ? 1 : -1);
		bottom.setSpeed(1000);
		paw.setSpeed(170);
		paw.rotateTo(-30);
		Thread.sleep(200);
		bottom.rotate(i * BaseOneQuarter + nFixAngle);
		bottom.rotate(-nFixAngle);	
		
		//BaseQuarters += nQuarter;
	}
	
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
		paw.rotateTo(-30);
		
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
	
	public static void RotatePaw() throws InterruptedException 
	{
		int nPawTurnOverPosition = PawTurnOverPosition;
		
		paw.setSpeed(170);//150
		paw.rotateTo(0);
		Thread.sleep(150);//300
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
	public static void LiftPaw()
	{
	    paw.setSpeed(130);
	    int nPawWaitPosition = PawWaitPosition;
	    paw.rotateTo(nPawWaitPosition);
	    
		
	}
	public static void main(String [] args) 
    {
		
		Button.ESCAPE.addButtonListener(new ButtonListener() {
		      @Override
			public void buttonPressed(Button b) 
		    {
		    	LCD.clear();
		        LCD.drawString("ESCAPE pressed", 0, 0);
		        monitor.rotateTo(0);
		        bottom.setSpeed(1000);
				bottom.rotateTo(0);
				paw.setSpeed(150);
				paw.rotateTo(0);
				
		        System.exit(0);
		    }

		      @Override
			public void buttonReleased(Button b) 
		    {
		        LCD.clear();
		    }
		    });
		
		
		Button.ENTER.addButtonListener(new ButtonListener() {
		      @Override
			public void buttonPressed(Button b) 
		    {
		    	LCD.clear();
		        LCD.drawString("ENTER pressed", 0, 0);
		        
				
		        
		    }

		      @Override
			public void buttonReleased(Button b) 
		    {
		        LCD.clear();
		    }
		    });
		
		Button.LEFT.addButtonListener(new ButtonListener() {
		      @Override
			public void buttonPressed(Button b) 
		    {
		    	LCD.clear();
		        LCD.drawString("LEFT pressed", 0, 0);
		        
		        test.LiftPaw();
				
		        
		    }

		      @Override
			public void buttonReleased(Button b) 
		    {
		        LCD.clear();
		    }
		    });
		
		
		Button.RIGHT.addButtonListener(new ButtonListener() {
		      @Override
			public void buttonPressed(Button b) 
		    {
		    	LCD.clear();
		        LCD.drawString("RIGHT pressed", 0, 0);
		        
		        try {
					test.RotatePaw();
					try {
						test.RotateBottom(1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						test.RotateBottomSide(1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		    }

		      @Override
			public void buttonReleased(Button b) 
		    {
		        LCD.clear();
		    }
		    });
		RConsole.open();
	    /*test.SolveReadColors() ;
	    for(int i = 0 ; i < 20 ; i ++ )
			{
				if(i < 6)
					LCD.drawString(sInput[i] , 0 , i);
				else
				{
					if(i < 12)
						LCD.drawString(sInput[i] + "  " , 4 , i - 6);
					else
				   {	if(i < 16)
							LCD.drawString(sInput[i] + "  " , 7 , i - 12);
				        else
				        	LCD.drawString(sInput[i] + "  " , 11 , i - 16);
				   }	
				}   				
			} 
		    
			for(int i=0;i<20;i++)
			{
			    
				RConsole.print(sInput[i]);
				RConsole.print("  ");
			    
			    
			}*/
		try {
			Robot.ReadOneSide(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Button.ENTER.waitForPress();
		monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset1 - (0 ));
		monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset2 - (0  ));
		try {
			Thread.sleep(120);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset3 - (0 ));
		//bottom.rotate(ColorReadPostion1);
	   while(true)
	   {
		   for (int i = 0 ; i <10 ; i ++)
			  {try {
				Robot.ReadColor();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  }
		   Button.ENTER.waitForPress();
	   }
		   /*Thread.sleep(1000);
		   for (int i = 0 ; i < 50 ; i ++)
		   {
		    for (int j = 0 ; j < 10 ; j ++)
		       {
		    	test.ReadColor();
		    	Thread.sleep(200);
		       }
		    light.setFloodlight(true);
		    Thread.sleep(1000);
		    light.setFloodlight(false);
			Button.ENTER.waitForPress();
		   }
			NXTConnection connection = USB.waitForConnection();
			DataOutputStream dos = connection.openDataOutputStream() ;

			String sOutput = cube.GetResult(sInput) ;
			RConsole.print(sOutput) ;*/
		//paw.setSpeed(150);
		//paw.rotateTo(30);
			//Button.ENTER.waitForPress();
			//RConsole.close(); 
	 }
	public static class Robot
    {
		/**
		 * @throws Exception  
		 */
		public static void RotateBottomSide(byte nQuarter) throws Exception 
		{
			int nFixAngle = BaseRotateFix * ( nQuarter > 0 ? 1 : -1);
			bottom.setSpeed(1000);
			paw.setSpeed(170);
			paw.rotateTo(0);
			Thread.sleep(200);
			bottom.rotate(nQuarter * BaseOneQuarter + nFixAngle);
			bottom.rotate(-nFixAngle);	
			
			//BaseQuarters += nQuarter;
		}
		
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
			paw.rotateTo(0);
			
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
		
		public static void RotatePaw() throws InterruptedException 
		{
			int nPawTurnOverPosition = PawTurnOverPosition;
			
			paw.setSpeed(170);//150
			paw.rotateTo(0);
			Thread.sleep(150);//300
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
		public static void LiftPaw()
		{
		    paw.setSpeed(130);
    	    int nPawWaitPosition = PawWaitPosition;
		    paw.rotateTo(nPawWaitPosition);
		    
			
		}
	public static void ReadOneSide(int nSideIndex) throws Exception
	{
		
		//Add a delay time for the motor to be stable
		byte delay=120; 
		byte[] idx={0,5,6,7,8,1,2,3,4} ;
		byte[] idx2={3,1,2,0,5,4};
		
		byte i=0;
		
		Robot.LiftPaw();
		
		//Read Center Color
		monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset1 - (0 * nSideIndex));
		Thread.sleep(delay);
		
		ReadColor();
		
		//Read Borders
		
		for(byte jj=0;jj<4;jj++)
		{
			
			monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset2 - (0 * nSideIndex ));
			Thread.sleep(delay);
			ReadColor();
		
			monitor.rotateTo(ColorMotorBaseAngle + ColorMotorOffset3 - (0 * nSideIndex ));
			bottom.rotate(ColorReadPostion1);
			
			Thread.sleep(delay);
			ReadColor();
			bottom.rotate(ColorReadPostion2);
		}
		
	    monitor.rotateTo(ColorMotorBaseAngle + 20 - (0 * nSideIndex));
	    
	}
	public static void ReadColor()
	{
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
        
        RConsole.println("(numpy.array([["+rawVals.getRed()+"],["+vals.getRed()+"],["+rawVals.getGreen()+"],["+vals.getGreen()+"],["+rawVals.getBlue()+"],["+vals.getBlue()+"],["+rawVals.getBackground()+"],["+vals.getBackground()+"]], dtype=numpy.float32), numpy.array([[0],[1]])),");
		
	}  
        
	public static void SolveReadColors()
	{
		
		int[] Center = new int[6] ;
		for (int c = 0 ; c < 6 ; c ++)
		{
			Center[c] = AllColor[c][0] ;
		}
		
		
		for (int c = 0 ; c < 6 ; c ++)
		{
			for (int n = 0 ; n < 9 ; n ++)
			{
				for (int j = 0 ; j < 6 ; j ++)
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
		
	}
	public static class cube
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
	}
}}
