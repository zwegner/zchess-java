import java.util.*;
import java.io.*;
import java.lang.*;
import java.awt.event.*;

/** zchess by Zach Wegner, 2004.
 *  All rights are reserved!!
 *  This software is distributed free of charge
 *  and without any warranty and so on. Zach Wegner
 *  claims no responsibilty for anything happening
 *  with this software, unless it wins something.
 *  Then I get the prize.
 **/  

public class chess
{
	public static void main(String[]a)throws Exception
	{
		String in;
		BufferedReader buf=new BufferedReader(new InputStreamReader(System.in));
		board b=new board();
		gui g=new gui();
		g.setSize(600, 400);
		g.addWindowListener(new WindowAdapter()
			{ 
				public void windowClosing(WindowEvent e)
				{ 
					System.exit(0);
				}
			});
		g.setb(b);
		g.show();
		
		b.println("ZChess 0.0 (c)2004 Zach Wegner");
		b.println("------------------------------");
		b.println((new Date())+"\n");
		while(true)
		{
			if(!b.result()&&(b.cs&b.s)!=0)
			{
				b.think();
				b.println((b.s==1?"white(":"black(")+b.mn+"):"+b.pv[0][0]+(char)b.beep);
				b.make(b.pv[0][0]);
				g.setb(b);
				g.show();
				g.repaint();
			}
			else
			{
				while(g.lm==null&&g.sc==null&&g.es.size()==0);
				if(g.lm!=null)
				{
					b.println((b.s==1?"white(":"black(")+b.mn+"):"+g.lm+(char)b.beep);
					b.make(g.lm);
					g.lm=null;
				}
				else if(g.sc!=null)
				{
					b.printlog(g.sc);
					b.inputcmd(g.sc);
					g.sc=null;
				}
				else
				{
					while(g.es.size()>0)
					{
						String s=(String)g.es.remove(0);
						b.printlog(s);
						b.inputcmd(s);
					}
				}
				g.setb(b);
				g.show();
				g.repaint();
			}
		}
	}
}
