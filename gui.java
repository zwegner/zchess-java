import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class gui extends Frame implements MouseMotionListener,MouseListener,ActionListener,ItemListener
{
	board b;
	int x[]={0,0,20,20};
	int y[]={0,20,20,0};
	int n=4;
	int lsq=-1,dx,dy;
	move lm;
	String sc;
	ArrayList es;
	Image p[][]={{Toolkit.getDefaultToolkit().createImage("img/wp.gif"),
		Toolkit.getDefaultToolkit().createImage("img/wn.gif"),
		Toolkit.getDefaultToolkit().createImage("img/wb.gif"),
		Toolkit.getDefaultToolkit().createImage("img/wr.gif"),
		Toolkit.getDefaultToolkit().createImage("img/wq.gif"),
		Toolkit.getDefaultToolkit().createImage("img/wk.gif")},
		{Toolkit.getDefaultToolkit().createImage("img/bp.gif"),
		Toolkit.getDefaultToolkit().createImage("img/bn.gif"),
		Toolkit.getDefaultToolkit().createImage("img/bb.gif"),
		Toolkit.getDefaultToolkit().createImage("img/br.gif"),
		Toolkit.getDefaultToolkit().createImage("img/bq.gif"),
		Toolkit.getDefaultToolkit().createImage("img/bk.gif")}};
	Image bg;
	public gui()
	{
		lm=null;
		sc=null;
		es=new ArrayList();
		for(int x=0;x<6;x++)
		{
			prepareImage(p[0][x],this);
			prepareImage(p[1][x],this);
		}
		MenuBar mb=new MenuBar();
		Menu m[]={new Menu("File"),new Menu("Edit"),new Menu("Game")};
		Menu r;
		//File
		addmi(m[0],"New");
		addmi(m[0],"Quit");
		//Edit
		addmi(m[1],"Engine Settings...");
		//Game
		addmi(m[2],"Undo");
		r=new Menu("Computer plays...");
		addcmi(r,"White",false);
		addcmi(r,"Black",true);
		addcmi(r,"Both",false);
		addcmi(r,"Neither",false);
		m[2].add(r);
		for(int x=0;x<m.length;x++)
			mb.add(m[x]);
		setMenuBar(mb);
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	public void addmi(Menu m,String s)
	{
		MenuItem mi=new MenuItem(s);
		mi.addActionListener(this);
		m.add(mi);
	}
	public void addcmi(Menu m,String s,boolean b)
	{
		CheckboxMenuItem cmi=new CheckboxMenuItem(s,b);
		cmi.addItemListener(this);
		m.add(cmi);
	}
	public void mouseDragged(MouseEvent e)
	{
		if(lsq!=-1)
		{
			int rx=e.getX(),ry=e.getY();
			int sq=getsq(rx,ry);
			if((b.cs&b.s)==0)
			{
				Image i=p[b.c[lsq]-1][b.p[lsq]-1];
				rx-=dx;
				ry-=dy;
				getGraphics().drawImage(bg,0,0,this);
				getGraphics().drawImage(i,rx,ry,this);		
			}
		}
	}
	public void mouseReleased(MouseEvent e)
	{
		if(lsq!=-1)
		{
			show();
			int sq=getsq(e.getX(),e.getY());
			for(int x=0;x<b.msi[1];x++)
				if(b.ms[x].f==lsq&&b.ms[x].t==sq)
				{
					lm=b.ms[x];
					lsq=-1;
					break;
				}
		}
	}
	public void mousePressed(MouseEvent e)
	{
		int rx=e.getX(),ry=e.getY();
		lsq=getsq(rx,ry);
		if(lsq<0||lsq>127||b.c[lsq]!=b.s||(b.c[lsq]&b.cs)!=0)
			lsq=-1;
		else
		{
			createbg();
			dx=rx-((lsq&15)*35+78);
			dy=ry-((7-(lsq>>4))*35+78);
		}
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}
	public void itemStateChanged(ItemEvent i)
	{
		String s=((CheckboxMenuItem)i.getSource()).getLabel();
		for(int x=0;x<4;x++)
			((CheckboxMenuItem)((Menu)(getMenuBar().getMenu(2).getItem(1))).getItem(x)).setState(false);
		((CheckboxMenuItem)i.getSource()).setState(true);
		if(s.equals("Black"))
			sc="cs 2";
		else if(s.equals("Both"))
			sc="cs 3";
		else if(s.equals("Neither"))
			sc="cs 0";
		else if(s.equals("White"))
			sc="cs 1";
	}
	public void actionPerformed(ActionEvent a)
	{
		String s=a.getActionCommand();
		if(s.equals("Engine Settings..."))
		{
			settings st=new settings(b,es);
			st.setSize(400,300);
			st.toFront();
			st.show();
		}
		else if(s.equals("New"))
			sc="new";
		else if(s.equals("Quit"))
			sc="quit";
		else if(s.equals("Undo"))
			sc="undo";
	}
	int getsq(int x,int y)
	{
		return ((7-((y-78)/35))<<4)+((x-78)/35);
	}
	public void setb(board x)
	{
		b=x;
	}
	public void createbg()
	{
		bg=createImage(600,400);
		Graphics g=bg.getGraphics();
		if(lsq!=-1)
		{
			g.setColor(new Color(0,255,0));
			g.fillRect(0,0,600,400);
			Color s[]={new Color(255,255,255),new Color(0,0,0)};
			g.setColor(s[1]);
			g.fillRect(60,60,316,316);
			for(int r=7;r>=0;r--)
			{
				for(int f=0;f<8;f++)
				{
					g.setColor(s[((r&1)+(f&1))&1]);
					g.fillRect(f*35+78,(7-r)*35+78,35,35);
					if(r==0)
					{
						g.setColor(s[0]);
						g.drawString(new String(""+(char)(f+'a')),f*35+93,370);
					}
				}
				g.setColor(s[0]);
				g.drawString(new String(""+(char)(r+'1')),65,(7-r)*35+96);
			}
			for(int r=7;r>=0;r--)
			{
				for(int f=0;f<8;f++)
				{
					if(b.c[(r<<4)+f]!=0&&(r<<4)+f!=lsq)
						if(!g.drawImage(p[b.c[(r<<4)+f]-1][b.p[(r<<4)+f]-1],f*35+78,(7-r)*35+78,this))
							System.out.println("S");
				}
			}
		}
	}
	public void paint(Graphics g)
	{
		g.setColor(new Color(0,255,0));
		g.fillRect(0,0,600,400);
		Color s[]={new Color(255,255,255),new Color(0,0,0)};
		g.setColor(s[1]);
		g.fillRect(60,60,316,316);
		for(int r=7;r>=0;r--)
		{
			for(int f=0;f<8;f++)
			{
				g.setColor(s[((r&1)+(f&1))&1]);
				g.fillRect(f*35+78,(7-r)*35+78,35,35);
				if(r==0)
				{
					g.setColor(s[0]);
					g.drawString(new String(""+(char)(f+'a')),f*35+93,370);
				}
			}
			g.setColor(s[0]);
			g.drawString(new String(""+(char)(r+'1')),65,(7-r)*35+96);
		}
		for(int r=7;r>=0;r--)
		{
			for(int f=0;f<8;f++)
			{
				if(b.c[(r<<4)+f]!=0)
					if(!g.drawImage(p[b.c[(r<<4)+f]-1][b.p[(r<<4)+f]-1],f*35+78,(7-r)*35+78,this))
						System.out.println("S");
			}
		}
		Toolkit.getDefaultToolkit().sync();
	}
}

class settings extends JFrame implements ChangeListener
{
    private JButton jButton1;
    private JButton jButton2;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private ArrayList al;
    public settings(board b,ArrayList a)
    {
        jTextField1 = new JTextField();
        jTextField2 = new JTextField();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jSeparator1 = new JSeparator();
        jSeparator2 = new JSeparator();
        jButton1 = new JButton();
        jButton2 = new JButton();
        al=a;

        getContentPane().setLayout(null);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
            	dispose();
            }
        });

        jTextField1.setColumns(10);
        jTextField1.setText(b.md+"");
        getContentPane().add(jTextField1);
        jTextField1.setBounds(110, 50, 30, 20);

        jLabel2.setText("Max Depth:");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(30, 50, 70, 16);

        jLabel3.setText("Engine Settings");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(40, 20, 90, 16);

        jLabel4.setText("Setup FEN");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(190, 40, 80, 16);

        jTextField2.setColumns(100);
        jTextField2.setText("");
        getContentPane().add(jTextField2);
        jTextField2.setBounds(280, 40, 80, 20);

        getContentPane().add(jSeparator1);
        jSeparator1.setBounds(20, 40, 130, 10);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        getContentPane().add(jSeparator2);
        jSeparator2.setBounds(170, 10, 10, 250);

        jButton1.setText("OK");
        getContentPane().add(jButton1);
        jButton1.setBounds(350, 240, 51, 26);

        jButton2.setText("Cancel");
        getContentPane().add(jButton2);
        jButton2.setBounds(260, 240, 73, 26);

		jButton1.addChangeListener(this);
		jButton2.addChangeListener(this);
        pack();
    }
    public void stateChanged(ChangeEvent c)
    {
    	if(c.getSource()==jButton1)
    	{
    		al.add("sd "+jTextField1.getText());
		al.add("setboard "+jTextField2.getText());
    	}
    	dispose();
    }
}
