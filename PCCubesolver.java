import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

/*This is a PC program.
* It receives the data got by NXT and come up with
* the steps to solve the cube, for NXT in short of
* computation capability.
* There is possible to shorten this code, focusing 
* on the interface of class cube.
* 
* 
* 
*/
public class PCCubesolver {
	
	public static String[] CenterColor2 = new String[] { "F", "B", "R", "L", "U", "D" };
	
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
	static String bOutput = "";
	static int phase = 0 ;
	static int[] tablesize = { 1 , 4096 , 6561 , 4096 , 256 , 1536 , 13824 , 576 } ;
	static int CHAROFFSET = 65 ;
	static char[] tmpStr = new char[20];
	static int step = 0;
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

        NXTConnector conn = new NXTConnector();
		
		conn.addLogListener(new NXTCommLogListener(){

			public void logEvent(String message) {
				System.out.println("USBSend Log.listener: "+message);
				
			}

			public void logEvent(Throwable throwable) {
				System.out.println("USBSend Log.listener - stack trace: ");
				 throwable.printStackTrace();
				
			}
			
		} 
		);
		
		if (!conn.connectTo("usb://")){
			System.err.println("No NXT found using USB");
			System.exit(1);
		}
		
		DataInputStream inDat = new DataInputStream(conn.getInputStream());
		DataOutputStream outDat = new DataOutputStream(conn.getOutputStream());
		
		//Receive the state of chaotic cube
		byte[] bInput = new byte[48];
		
		try {
       	  inDat.read(bInput);//receive a 48-length byte array representing the color blocks versus the right position
       } catch (IOException ioe) {
          System.err.println("IO Exception reading reply");
       }     
		//double[][] input = new double[1][8] ;
		
				
	   //change to standard representation 
	   String[] sInput = new String[20]; 
	    sInput[0] = CenterColor2[bInput[0]] + CenterColor2[bInput[1]] ;//UF
		sInput[1] = CenterColor2[bInput[2]] + CenterColor2[bInput[3]] ;//UR
		sInput[2] = CenterColor2[bInput[4]] + CenterColor2[bInput[5]] ;//UB
		sInput[3] = CenterColor2[bInput[6]] + CenterColor2[bInput[7]] ;//UL
		sInput[4] = CenterColor2[bInput[8]] + CenterColor2[bInput[9]] ;//DF
		sInput[5] = CenterColor2[bInput[10]] + CenterColor2[bInput[11]] ;//DR
		sInput[6] = CenterColor2[bInput[12]] + CenterColor2[bInput[13]] ;//DB
		sInput[7] = CenterColor2[bInput[14]] + CenterColor2[bInput[15]] ;//DL
		sInput[8] = CenterColor2[bInput[16]] + CenterColor2[bInput[17]] ;//FR
		sInput[9] = CenterColor2[bInput[18]] + CenterColor2[bInput[19]] ;//FL
		sInput[10] = CenterColor2[bInput[20]] + CenterColor2[bInput[21]] ;//BR
		sInput[11] = CenterColor2[bInput[22]] + CenterColor2[bInput[23]] ;//BL
		
		sInput[12] = CenterColor2[bInput[24]] + CenterColor2[bInput[25]] + CenterColor2[bInput[26]] ;//UFR
		sInput[13] = CenterColor2[bInput[27]] + CenterColor2[bInput[28]] + CenterColor2[bInput[29]] ;//URB
		sInput[14] = CenterColor2[bInput[30]] + CenterColor2[bInput[31]] + CenterColor2[bInput[32]] ;//UBL
		sInput[15] = CenterColor2[bInput[33]] + CenterColor2[bInput[34]] + CenterColor2[bInput[35]] ;//ULF
	
		sInput[16] = CenterColor2[bInput[36]] + CenterColor2[bInput[37]] + CenterColor2[bInput[38]] ;//DRF
		sInput[17] = CenterColor2[bInput[39]] + CenterColor2[bInput[40]] + CenterColor2[bInput[41]] ;//DFL
		sInput[18] = CenterColor2[bInput[42]] + CenterColor2[bInput[43]] + CenterColor2[bInput[44]] ;//DLB
		sInput[19] = CenterColor2[bInput[45]] + CenterColor2[bInput[46]] + CenterColor2[bInput[47]] ;//DBR
		
	   //get result
	   String sOutput = new String();
	   try {
		sOutput = cube.GetResult(sInput);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   System.out.println(String.valueOf(step)+"steps:"+sOutput);
	   
	   
	   //send steps to NXT
			try {
			   outDat.write(step);
			   outDat.flush();
	
			} catch (IOException ioe) {
				System.err.println("IO Exception writing bytes");
			}
	        
			
			
			for (int i = 0 ; i < 2*step ; i ++) 
		    {
			try {
				//System.out.println(bOutput.charAt(i));
				      String s = String.valueOf(bOutput.charAt(i));
				      int b = Integer.parseInt(s);
					  outDat.writeByte((byte) b);
					  outDat.flush();
					  //outDat.writeByte((byte) moveamount[i]);
				      //outDat.flush();
				      //System.out.println(move[i]);
				      //System.out.println(moveamount[i]);
				  
				} catch (IOException ioe) {
					System.err.println("IO Exception writing bytes");
				}
		    }
			
		
				
	        
		
		
		try {
			inDat.close();
			outDat.close();
			System.out.println("Closed data streams");
		} catch (IOException ioe) {
			System.err.println("IO Exception Closing connection");
		}
		
		try {
			conn.close();
			System.out.println("Closed connection");
		} catch (IOException ioe) {
			System.err.println("IO Exception Closing connection");
		}
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
				step += j ;
				for (i=0; i<j; i++)
				{
					//RConsole.print(move[i] + " ");
					sOutput += "FBRLUD".charAt(move[i]) + "" + moveamount[i] ;
					sOutput += " " ;	
					bOutput += String.valueOf(move[i]) + String.valueOf(moveamount[i]);
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
}
