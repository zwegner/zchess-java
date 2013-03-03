import java.util.*;
import java.io.*;

public class board
{
	final int MAXPLY=128;
	final int MAXHPLY=4096;
	final int cstm[]=
	{
		14,15,15,12,15,15,15,13,0,0,0,0,0,0,0,0,
		15,15,15,15,15,15,15,15,0,0,0,0,0,0,0,0,
		15,15,15,15,15,15,15,15,0,0,0,0,0,0,0,0,
		15,15,15,15,15,15,15,15,0,0,0,0,0,0,0,0,
		15,15,15,15,15,15,15,15,0,0,0,0,0,0,0,0,
		15,15,15,15,15,15,15,15,0,0,0,0,0,0,0,0,
		15,15,15,15,15,15,15,15,0,0,0,0,0,0,0,0,
		11,15,15, 3,15,15,15, 7,0,0,0,0,0,0,0,0
	};
	final int dt[][]=
	{
		{0},
		{0},
		{-33,-31,-18,-14,14,18,31,33,0},
		{-17,-15,15,17,0},
		{-16,-1,1,16,0},
		{-17,-16,-15,-1,1,15,16,17,0},
		{-17,-16,-15,-1,1,15,16,17,0}
	};
	final boolean sl[]={false,false,false,true,true,true,false};
	final char pchar[]={'.','P','N','B','R','Q','K'};
	final int pm[][]=
	{
		{0},
		{0,1,4,8,16,32,64},
		{0,2,4,8,16,32,64}
	};
	int atk[];
	int step[];
	int p[];
	int c[];
	int pl[][];
	int pll[];
	int s;
	int f;
	int cst;
	int ep;
	move ms[];
	int msi[];
	int ss[];
	move pv[][];
	int pvl[];
	move kill[][];
	int hist[][];
	int eval[];
	move hmv[];
	int hf[];
	int hcst[];
	int hep[];
	int hply;
	int mn;
	int md;
	int cs;
	int beep;
	PrintWriter pw;
	int thinking;
	int nodes;
	int qnodes;
	board()
	{
		try
		{
			pw=new PrintWriter(new BufferedWriter(new FileWriter("log.txt")),true);
		}
		catch(Exception e)
		{
		}
		p=new int[128];
		c=new int[128];
		pl=new int[2][16];
		pll=new int[2];
		ms=new move[32*MAXPLY];
		msi=new int[MAXPLY];
		ss=new int[32*MAXPLY];
		pv=new move[MAXPLY][MAXPLY];
		pvl=new int[MAXPLY];
		kill=new move[MAXPLY][2];
		hist=new int[64][64];
		eval=new int[MAXPLY];
		hmv=new move[MAXHPLY];
		hf=new int[MAXHPLY];
		hcst=new int[MAXHPLY];
		hep=new int[MAXHPLY];
		atk=new int[256];
		step=new int[256];
		//hard-coded pawn attacks
		atk[15+128]|=pm[1][1];
		atk[17+128]|=pm[1][1];
		atk[-15+128]|=pm[2][1];
		atk[-17+128]|=pm[2][1];
		for(int pc=2;pc<=6;pc++)
		{
			for(int x=0;dt[pc][x]!=0;x++)
			{
				int sq=dt[pc][x];
				for(int y=0;y<8&&sq>=-128&&sq<128;y++)
				{
					atk[sq+128]|=pm[1][pc];
					step[sq+128]=dt[pc][x];
					if(!sl[pc])
						break;
					sq+=dt[pc][x];
				}
			}
		}
		beep=0;//7;
		md=4;
		cs=2;
		newgame();
	}
	void newgame()
	{
		int ip[]=
		{
			4,2,3,5,6,3,2,4,0,0,0,0,0,0,0,0,
			1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,
			4,2,3,5,6,3,2,4,0,0,0,0,0,0,0,0
		};
		int ic[]=
		{
			1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,
			1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,
			2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0
		};
		for(int x=0;x<128;x++)
		{
			p[x]=ip[x];
			c[x]=ic[x];
		}
		s=1;
		f=0;
		cst=15;
		ep=0;
		mn=1;
		msi[0]=msi[1]=0;
		genpl();
	}
	void genpl()
	{
		int x;
		pll[0]=pll[1]=1;
		for(x=0;x<128;x++)
			if(p[x]==6)
				pl[c[x]-1][0]=x;
		for(x=0;x<128;x++)
			if(p[x]!=6&&p[x]!=0)
				pl[c[x]-1][pll[c[x]-1]++]=x;
	}
	void gen(int ply)
	{
		msi[ply+1]=msi[ply];
		if(ep!=0)
		{
			switch(s)
			{
				case 1:
					if(((ep-15)&0x88)==0&&c[ep-15]==1&&p[ep-15]==1)
						ms[msi[ply+1]++]=new move(ep-15,ep);
					if(((ep-17)&0x88)==0&&c[ep-17]==1&&p[ep-17]==1)
						ms[msi[ply+1]++]=new move(ep-17,ep);
					break;
				case 2:
					if(((ep+15)&0x88)==0&&c[ep+15]==2&&p[ep+15]==1)
						ms[msi[ply+1]++]=new move(ep+15,ep);
					if(((ep+17)&0x88)==0&&c[ep+17]==2&&p[ep+17]==1)
						ms[msi[ply+1]++]=new move(ep+17,ep);
					break;
			}
		}
		switch(s)
		{
			case 1:
				if((cst&2)!=0&&c[5]==0&&c[6]==0)
					ms[msi[ply+1]++]=new move(4,6,6);
				if((cst&1)!=0&&c[1]==0&&c[2]==0&&c[3]==0)
					ms[msi[ply+1]++]=new move(4,2,6);
				break;
			case 2:
				if((cst&8)!=0&&c[117]==0&&c[118]==0)
					ms[msi[ply+1]++]=new move(116,118,6);
				if((cst&4)!=0&&c[113]==0&&c[114]==0&&c[115]==0)
					ms[msi[ply+1]++]=new move(116,114,6);
				break;
		}
		for(int x=0;x<pll[s-1];x++)
		{
			int sq=pl[s-1][x];
			switch(p[sq])
			{
				case 1:
					switch(s)
					{
						case 1:
							if((sq+16>>4)==7)//promotions
							{
								if(c[sq+16]==0)
								{
									ms[msi[ply+1]++]=new move(sq,sq+16,1,0,2);
									ms[msi[ply+1]++]=new move(sq,sq+16,1,0,3);
									ms[msi[ply+1]++]=new move(sq,sq+16,1,0,4);
									ms[msi[ply+1]++]=new move(sq,sq+16,1,0,5);
								}
								if((sq&15)>0&&c[sq+15]==2)
								{
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15],2);
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15],3);
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15],4);
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15],5);
								}
								if((sq&15)<7&&c[sq+17]==2)
								{
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17],2);
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17],3);
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17],4);
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17],5);
								}
							}
							else
							{
								if(c[sq+16]==0)
								{
									ms[msi[ply+1]++]=new move(sq,sq+16,1);//single pawn move
									if(sq>>4==1&&c[sq+32]==0)
										ms[msi[ply+1]++]=new move(sq,sq+32,1);
								}
								if((sq&15)>0&&c[sq+15]==2)//pawn caps
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15]);
								if((sq&15)<7&&c[sq+17]==2)
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17]);
							}
							break;
						case 2:
							if((sq-16>>4)==0)//promotions
							{
								if(c[sq-16]==0)
								{
									ms[msi[ply+1]++]=new move(sq,sq-16,1,0,2);
									ms[msi[ply+1]++]=new move(sq,sq-16,1,0,3);
									ms[msi[ply+1]++]=new move(sq,sq-16,1,0,4);
									ms[msi[ply+1]++]=new move(sq,sq-16,1,0,5);
								}
								if((sq&15)<7&&c[sq-15]==1)
								{
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15],2);
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15],3);
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15],4);
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15],5);
								}
								if((sq&15)>0&&c[sq-17]==1)
								{
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17],2);
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17],3);
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17],4);
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17],5);
								}
							}
							else
							{
								if(c[sq-16]==0)
								{
									ms[msi[ply+1]++]=new move(sq,sq-16,1);//single pawn move
									if(sq>>4==6&&c[sq-32]==0)
										ms[msi[ply+1]++]=new move(sq,sq-32,1);
								}
								if((sq&15)<7&&c[sq-15]==1)//pawn caps
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15]);
								if((sq&15)>0&&c[sq-17]==1)
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17]);
							}
							break;
					}
					break;
				case 2:
					for(int y=0;y<8;y++)
					{
						if((sq+dt[2][y]&0x88)==0&&c[sq+dt[2][y]]!=s)
							ms[msi[ply+1]++]=new move(sq,sq+dt[2][y],2,p[sq+dt[2][y]]);
					}
					break;
				case 3:
					for(int y=0;y<4;y++)
					{
						int z=sq+dt[3][y];
						while((z&0x88)==0&&c[z]!=s)
						{
							ms[msi[ply+1]++]=new move(sq,z,3,p[z]);
							if(c[z]!=0)
								break;
							z+=dt[3][y];
						}
					}
					break;
				case 4:
					for(int y=0;y<4;y++)
					{
						int z=sq+dt[4][y];
						while((z&0x88)==0&&c[z]!=s)
						{
							ms[msi[ply+1]++]=new move(sq,z,4,p[z]);
							if(c[z]!=0)
								break;
							z+=dt[4][y];
						}
					}
					break;
				case 5:
					for(int y=0;y<8;y++)
					{
						int z=sq+dt[5][y];
						while((z&0x88)==0&&c[z]!=s)
						{
							ms[msi[ply+1]++]=new move(sq,z,5,p[z]);
							if(c[z]!=0)
								break;
							z+=dt[5][y];
						}
					}
					break;
				case 6:
					for(int y=0;y<8;y++)
					{
						if((sq+dt[6][y]&0x88)==0&&c[sq+dt[6][y]]!=s)
							ms[msi[ply+1]++]=new move(sq,sq+dt[6][y],6,p[sq+dt[6][y]]);
					}
					break;
			}
		}
	}	
	void gencaps(int ply)
	{
		msi[ply+1]=msi[ply];
		if(ep!=0)
		{
			switch(s)
			{
				case 1:
					if(((ep-15)&0x88)==0&&c[ep-15]==1&&p[ep-15]==1)
						ms[msi[ply+1]++]=new move(ep-15,ep);
					if(((ep-17)&0x88)==0&&c[ep-17]==1&&p[ep-17]==1)
						ms[msi[ply+1]++]=new move(ep-17,ep);
					break;
				case 2:
					if(((ep+15)&0x88)==0&&c[ep+15]==2&&p[ep+15]==1)
						ms[msi[ply+1]++]=new move(ep+15,ep);
					if(((ep+17)&0x88)==0&&c[ep+17]==2&&p[ep+17]==1)
						ms[msi[ply+1]++]=new move(ep+17,ep);
					break;
			}
		}
		for(int x=0;x<pll[s-1];x++)
		{
			int sq=pl[s-1][x];
			switch(p[sq])
			{
				case 1:
					switch(s)
					{
						case 1:
							if((sq+16>>4)==7)//promotions
							{
								if((sq&15)>0&&c[sq+15]==2)
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15],5);
								if((sq&15)<7&&c[sq+17]==2)
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17],5);
							}
							else
							{
								if(((sq+15)&0x88)==0&&c[sq+15]==2)//pawn caps
									ms[msi[ply+1]++]=new move(sq,sq+15,1,p[sq+15]);
								if(((sq+17)&0x88)==0&&c[sq+17]==2)
									ms[msi[ply+1]++]=new move(sq,sq+17,1,p[sq+17]);
							}
							break;
						case 2:
							if((sq-16>>4)==0)//promotions
							{
								if((sq&15)<7&&c[sq-15]==1)
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15],5);
								if((sq&15)>0&&c[sq-17]==1)
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17],5);
							}
							else
							{
								if(((sq-15)&0x88)==0&&c[sq-15]==1)//pawn caps
									ms[msi[ply+1]++]=new move(sq,sq-15,1,p[sq-15]);
								if(((sq-17)&0x88)==0&&c[sq-17]==1)
									ms[msi[ply+1]++]=new move(sq,sq-17,1,p[sq-17]);
							}
							break;
					}
					break;
				case 2:
					for(int y=0;y<8;y++)
					{
						if((sq+dt[2][y]&0x88)==0&&c[sq+dt[2][y]]==(s^3))
							ms[msi[ply+1]++]=new move(sq,sq+dt[2][y],2,p[sq+dt[2][y]]);
					}
					break;
				case 3:
					for(int y=0;y<4;y++)
					{
						int z=sq+dt[3][y];
						while((z&0x88)==0&&c[z]!=s)
						{
							if(c[z]!=0)
							{
								ms[msi[ply+1]++]=new move(sq,z,3,p[z]);
								break;
							}
							z+=dt[3][y];
						}
					}
					break;
				case 4:
					for(int y=0;y<4;y++)
					{
						int z=sq+dt[4][y];
						while((z&0x88)==0&&c[z]!=s)
						{
							if(c[z]!=0)
							{
								ms[msi[ply+1]++]=new move(sq,z,4,p[z]);
								break;
							}
							z+=dt[4][y];
						}
					}
					break;
				case 5:
					for(int y=0;y<8;y++)
					{
						int z=sq+dt[5][y];
						while((z&0x88)==0&&c[z]!=s)
						{
							if(c[z]!=0)
							{
								ms[msi[ply+1]++]=new move(sq,z,5,p[z]);
								break;
							}
							z+=dt[5][y];
						}
					}
					break;
				case 6:
					for(int y=0;y<8;y++)
					{
						if((sq+dt[6][y]&0x88)==0&&c[sq+dt[6][y]]==(s^3))
							ms[msi[ply+1]++]=new move(sq,sq+dt[6][y],6,p[sq+dt[6][y]]);
					}
					break;
			}
		}
	}
	boolean incheck()
	{
		int sq=pl[s-1][0];
		int x;
		for(x=1;x<pll[s-1];x++)
			if(p[pl[s-1][x]]==6)
			{
				println(this+"");
				for(int i=0;i<hply;i++)
					print(hmv[i]+" ");
				print("\n");
				for(int i=0;i<10000000;i++);
				for(int j=0;j<10000000;j++)
				for(int k=0;k<10000000;k++);
			}
		for(x=0;x<pll[(s^3)-1];x++)
		{
			int t=pl[(s^3)-1][x];
			if((atk[sq-t+128]&pm[s^3][p[t]])!=0)
			{
				int st=step[sq-t+128];
				if(!sl[p[t]])
					return true;
				else
				{
					t+=st;
					while(t!=sq&&c[t]==0)
						t+=st;
					if(t==sq)
						return true;
				}	
			}
		}
		return false;
	}
	boolean make(move m)
	{
		hmv[hply]=m;
		hf[hply]=f;
		hcst[hply]=cst;
		hep[hply]=ep;
		hply++;
		ep=0;
		if(s==2)
			mn++;
		if(m.p==1||m.c!=0)
			f=0;
		else
			f++;
		for(int x=0;x<pll[s-1];x++)
			if(pl[s-1][x]==m.f)
			{
				pl[s-1][x]=m.t;
				break;
			}
		if(m.c!=0)	
			for(int x=0;x<pll[(s^3)-1];x++)
				if(pl[(s^3)-1][x]==m.t)
				{
					pl[(s^3)-1][x]=pl[(s^3)-1][--pll[(s^3)-1]];
					break;
				}
		cst&=cstm[m.f]&cstm[m.t];
		switch(m.p)
		{
			case 1:
				p[m.f]=c[m.f]=0;
				c[m.t]=s;
				if(m.pr!=0)			//promote
					p[m.t]=m.pr;
				else
				{
					p[m.t]=1;
					if(m.f-m.t==32||m.f-m.t==-32)	//two forward
						ep=(m.f+m.t)>>1;
					else if(m.e!=0)					//en passant
					{
						p[m.t+(s==1?-16:16)]=0;
						c[m.t+(s==1?-16:16)]=0;
						for(int x=0;x<pll[(s^3)-1];x++)
							if(pl[(s^3)-1][x]==m.t+(s==1?-16:16))
							{
								pl[(s^3)-1][x]=pl[(s^3)-1][--pll[(s^3)-1]];
								break;
							}
					}
				}
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				p[m.f]=c[m.f]=0;
				p[m.t]=m.p;
				c[m.t]=s;
				break;
			case 6:
				p[m.f]=c[m.f]=0;
				p[m.t]=6;
				c[m.t]=s;
				if(m.t-m.f==2)						//k-side castle
				{
					p[m.f+1]=4;
					c[m.f+1]=s;
					p[m.t+1]=c[m.t+1]=0;
					for(int x=0;x<pll[s-1];x++)
						if(pl[s-1][x]==m.t+1)
						{
							pl[s-1][x]=m.f+1;
							break;
						}
				}
				else if(m.t-m.f==-3)				//q-side castle
				{
					p[m.f-1]=4;
					c[m.f-1]=s;
					p[m.t-2]=c[m.t-2]=0;
					for(int x=0;x<pll[s-1];x++)
						if(pl[s-1][x]==m.t-2)
						{
							pl[s-1][x]=m.f-1;
							break;
						}
				}
				break;
		}
		if(incheck())
		{
			s^=3;
			unmake();
			return false;
		}
		s^=3;
		return true;
	}
	void unmake()
	{
		if(hply<=0)
			return;
		s^=3;
		hply--;
		move m=hmv[hply];
		f=hf[hply];
		cst=hcst[hply];
		ep=hep[hply];
		if(s==2)
			mn--;
		for(int x=0;x<pll[s-1];x++)
			if(pl[s-1][x]==m.t)
			{
				pl[s-1][x]=m.f;
				break;
			}
		if(m.c!=0)
			pl[(s^3)-1][pll[(s^3)-1]++]=m.t;
		switch(m.p)
		{
			case 1:
				p[m.t]=m.c;
				c[m.t]=m.c!=0?s^3:0;
				p[m.f]=1;
				c[m.f]=s;
				if(m.e!=0)							//en passant
				{
					p[m.t+(s==1?-16:16)]=1;
					c[m.t+(s==1?-16:16)]=s^3;
					pl[(s^3)-1][pll[(s^3)-1]++]=m.t+(s==1?-16:16);
				}
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				p[m.t]=m.c;
				c[m.t]=m.c!=0?s^3:0;
				p[m.f]=m.p;
				c[m.f]=s;
				break;
			case 6:
				p[m.t]=m.c;
				c[m.t]=m.c!=0?s^3:0;
				p[m.f]=6;
				c[m.f]=s;
				if(m.t-m.f==2)						//k-side castle
				{
					p[m.f+1]=c[m.f+1]=0;
					p[m.t+1]=4;
					c[m.t+1]=s;
					for(int x=0;x<pll[s-1];x++)
						if(pl[s-1][x]==m.f+1)
						{
							pl[s-1][x]=m.t+1;
							break;
						}
				}
				else if(m.t-m.f==-3)				//q-side castle
				{
					p[m.f-1]=c[m.f-1]=0;
					p[m.t-2]=4;
					c[m.t-2]=2;
					for(int x=0;x<pll[s-1];x++)
						if(pl[s-1][x]==m.f-1)
						{
							pl[s-1][x]=m.t-2;
							break;
						}
				}
				break;
		}
	}
	void checklegal(int ply)
	{
		for(int x=msi[ply];x<msi[ply+1];x++)
		{
			if(make(ms[x]))
				unmake();
			else
			{
				ms[x]=ms[--msi[ply+1]];
				x--;
			}
		}
	}
	void perft(int ply,int depth)
	{
		gen(ply);
		if(depth>1)
			for(int x=msi[ply];x<msi[ply+1];x++)
			{
				if(make(ms[x]))
				{
					perft(ply+1,depth-1);
					unmake();
				}
			}
		else
		{
			checklegal(ply);
			nodes+=msi[ply+1]-msi[ply];
		}
	}
	String hist()
	{
		String s="";
		for(int x=0;x<hply;x++)
			s+=hmv[x]+" ";
			return s;
	}
	boolean inputmove(String s,int user)
	{
		int f,t,pr=0;
		String prs="NBRQ";
		if(!((s.length()==4||(s.length()==5&&prs.indexOf(s.charAt(4))!=-1))&&
			s.charAt(0)>='a'&&s.charAt(0)<='h'&&
			s.charAt(1)>='1'&&s.charAt(1)<='8'&&
			s.charAt(2)>='a'&&s.charAt(2)<='h'&&
			s.charAt(3)>='1'&&s.charAt(3)<='8'))
		{
			if(user==1)
				println("Illegal move:"+s);
			return false;
		}
		f=(s.charAt(1)-'1')*16+(s.charAt(0)-'a');
		t=(s.charAt(3)-'1')*16+(s.charAt(2)-'a');
		if(s.length()==5)
			pr=2+prs.indexOf(s.charAt(4));
		if(pr==1)
		{
			if(user==1)
				println("Illegal move:"+s);
			return false;
		}
		for(int x=msi[0];x<msi[1];x++)
			if(ms[x].f==f&&ms[x].t==t&&ms[x].pr==pr)
			{
				if(user==1)
					make(ms[x]);
				return true;
			}
		if(user==1)
			println("Illegal move:"+s);
		return false;
	}
	int inputcmd(String str)
	{
		StringTokenizer t=new StringTokenizer(str);
		String c=t.nextToken();
		if(c.equals("help"))
		{
			println("beep.............toggle computer move alarm");
			println("cs x.............set computer side,1=white,2=black,3=both");
			println("d................display the chess board");
			println("divide x.........print perft values of x-1 for each root move");
			println("exit.............quits ZChess");
			println("go...............computer plays side to move");
			println("help.............display this");
			println("moves............display move list");
			println("new..............new game");
			println("perft x..........print number of chess games from root position x plies long");
			println("sd x.............set search depth to x");
			println("setboard.........setup board with FEN position");
			println("undo.............undo a move");
			println("quit.............quits ZChess");
			return 1;
		}
		else if(c.equals("beep"))
		{
			beep^=7;
			return 1;
		}
		else if(c.equals("cs"))
		{
			cs=Integer.parseInt(t.nextToken());
			return 1;
		}
		else if(c.equals("d"))
		{
			println(""+this);
			return 1;
		}
		else if(c.equals("divide"))
		{
			if(thinking==1)
				return 2;
			int d=Integer.parseInt(t.nextToken())-1;
			for(int x=msi[0];x<msi[1];x++)
			{
				nodes=0;
				make(ms[x]);
				perft(1,d);
				unmake();
				println(ms[x]+": "+nodes);
			}
			return 1;
		}
		else if(c.equals("go"))
		{
			cs=s;
			return 1;
		}
		else if(c.equals("moves"))
		{
			for(int x=msi[0];x<msi[1];x++)
				print(ms[x]+" ");
			println("");
			return 1;
		}
		else if(c.equals("new"))
		{
			if(thinking==1)
				return 2;
			newgame();
			return 1;
		}
		else if(c.equals("perft"))
		{
			if(thinking==1)
				return 2;
			Date d=new Date();
			nodes=0;
			perft(0,Integer.parseInt(t.nextToken()));
			println(nodes+" nodes, "+(float)(new Date().getTime()-d.getTime())/1000+" secs");
			return 1;
		}
		else if(c.equals("sd"))
		{
			md=Integer.parseInt(t.nextToken());
			return 1;
		}
		else if(c.equals("setboard"))
		{
			setboard(t.nextToken()+" "+t.nextToken()+" "+t.nextToken()+" "+t.nextToken());
			return 1;
		}
		else if(c.equals("undo"))
		{
			if(thinking==1)
				return 2;
			unmake();
			return 1;
		}
		else if(c.equals("quit")||c.equals("exit"))
		{
			System.exit(0);
		}
		return 0;
	}
	boolean result()
	{
		gen(0);
		checklegal(0);
		if(msi[1]==0)
		{
			println(incheck()?"mate":"stalemate");
			return true;
		}
		if(f==100)
		{
			println("draw by fifty moves");
			return true;
		}
		return false;
	}
	void think()
	{
		search s=new search(this);
		nodes=qnodes=0;
		int val,alpha=-10000,beta=10000;
		long l=(new Date()).getTime();
		println("depth score\tnodes\tpv (time)");
		for(int x=1;x<=md;x++)
		{	
			val=s.searchr(alpha,beta,x);
			if(val>beta)
			{
				println(x+"     ++\t"+((nodes+qnodes)/1000)+"k\t"+pv[0][0]+"! ("+((new Date()).getTime()-l)+")");
				beta=10000;
				x--;
			}
			else if(val<alpha)
			{
				println(x+"     --\t"+((nodes+qnodes)/1000)+"k\t"+pv[0][0]+"? ("+((new Date()).getTime()-l)+")");
				alpha=-10000;
				x--;
			}
			else
			{
				println(x+"     "+val+"\t"+((nodes+qnodes)/1000)+"k\t"+pvstr()+"("+((new Date()).getTime()-l)+")");
				alpha=val-100;
				beta=val+100;
			}
		}
		println((nodes+qnodes)+" nodes "+((float)100.0*qnodes/(nodes+qnodes))+"% q "+
			((float)((new Date()).getTime()-l)/1000)+" secs "+
			((float)(nodes+qnodes)/((float)((new Date()).getTime()-l)/1000))+" nps");
	}
	void setboard(String fen)
	{
		int x,sq=112;
		String w=" PNBRQK";
		String n="012345678";
		for(x=0;fen.charAt(x)!=' ';x++)
		{
			if(fen.charAt(x)=='/')
				sq=(((sq>>4)-1)<<4);
			else if(n.indexOf(fen.charAt(x))>=0)
				for(int y=0;y<n.indexOf(fen.charAt(x));y++)
				{
					c[sq]=p[sq]=0;
					sq++;
				}
			else
			{
				c[sq]=w.indexOf(fen.charAt(x))>0?1:2;
				p[sq]=c[sq]==1?w.indexOf(fen.charAt(x)):w.toLowerCase().indexOf(fen.charAt(x));
				sq++;
			}
		}
		x++;
		s=fen.charAt(x)=='w'?1:2;
		x++;
		cst=0;
		
		println(this+"");
		genpl();
	}
	String mlstr(int ply)
	{
		String s="";
		for(int x=msi[ply];x<msi[ply+1];x++)
			s+=ms[x]+" ";
		return s;
	}
	String pvstr()
	{
		String s="";
		for(int x=0;x<pvl[0];x++)
			s+=pv[0][x]+" ";
		return s;
	}
	public String toString()
	{
		String s="";
		for(int r=7;r>=0;r--)
		{
			s+=(r+1)+" ";
			for(int f=0;f<8;f++)
				s+=(char)(pchar[p[(r<<4)+f]]+(c[(r<<4)+f]==2?32:0))+" ";
			s+="\n";
		}
		s+="  a b c d e f g h\n";
		return s;
	}
	public void print(String s)
	{
		System.out.print(s);
		pw.print(s);
	}
	public void println(String s)
	{
		System.out.println(s);
		pw.println(s);
	}
	public void printlog(String s)
	{
		pw.print(s);
	}
}
