public class search
{
	board b;
	eval e;
	search(board x)
	{
		b=x;
		e=new eval(b);
		b.eval[0]=e.e();
	}
	int searchr(int alpha,int beta,int depth)
	{
		int next,best=-10000,val;
		move m;
		boolean pvfound=false;
		
		b.pvl[0]=1;
		b.nodes++;
		sortmoves(0);
		for(next=b.msi[0];next<b.msi[1];next++)
		{
			m=b.ms[next];
			b.print(depth+"     "+next+"/"+b.msi[1]+"\t"+(b.nodes/1000)+"k\t"+m+"\r");
			b.make(m);
			if(depth>1)
			{
				if(pvfound)
				{
					val=-search(-alpha-1,-alpha,depth-1,1,1);
					if(val>alpha&&val<beta)
						val=-search(-beta,-alpha,depth-1,1,1);
				}
				else
					val=-search(-beta,-alpha,depth-1,1,1);
			}
			else
				val=-e.e();
			b.unmake();
			b.ss[next]=val;
			if(val>best)
			{
				best=val;
				if(val>alpha)
				{
					pvfound=true;
					b.pv[0][0]=m;
					for(int x=1;x<b.pvl[1];x++)
						b.pv[0][x]=b.pv[1][x];
					b.pvl[0]=b.pvl[1];
					if(val>=beta)
					{
						if(m.c==0&&m.pr==0)
						{
							b.kill[0][1]=b.kill[0][0];
							b.kill[0][0]=m;
						}
						b.hist[(m.f&7)+((m.f>>1)&0x38)][(m.t&7)+((m.t>>1)&0x38)]+=depth*depth;
						return val;
					}
					alpha=val;
				}
			}
		}
		return alpha;
	}
	int search(int alpha,int beta,int depth,int ply,int donull)
	{
		int next,best=-10000,val,moves=0;
		move m;
		boolean check=b.incheck(),pvfound=false;
		
		b.pvl[ply]=ply;
		b.nodes++;
		b.eval[ply]=e.e();
		if(Math.abs(b.eval[ply]+b.eval[ply-1])<100)
			depth--;
		b.gen(ply);
		score(ply);
		sortmoves(ply);
		for(next=b.msi[ply];next<b.msi[ply+1];next++)
		{
			m=b.ms[next];
			if(b.make(m))
			{
				moves++;
				if(depth>1)
				{
					if(pvfound)
					{
						val=-search(-alpha-1,-alpha,depth,ply+1,1);
						if(val>alpha&&val<beta)
							val=-search(-beta,-alpha,depth,ply+1,1);
					}
					else
						val=-search(-beta,-alpha,depth,ply+1,1);
				}
				else
			//		val=-searchq(-beta,-alpha,ply+1);
					val=-e.e();
				b.unmake();
				if(val>best)
				{
					best=val;
					if(val>alpha)
					{
						b.pv[ply][ply]=m;
						for(int x=ply+1;x<b.pvl[ply+1];x++)
							b.pv[ply][x]=b.pv[ply+1][x];
						b.pvl[ply]=b.pvl[ply+1];
						pvfound=true;
						if(val>=beta)
						{
							if(m.c==0&&m.pr==0)
							{
								b.kill[ply][1]=b.kill[ply][0];
								b.kill[ply][0]=m;
							}
							b.hist[(m.f&7)+((m.f>>1)&0x38)][(m.t&7)+((m.t>>1)&0x38)]+=depth*depth;
							return val;
						}
						alpha=val;
					}
				}
			}
		}
		if(moves==0)
		{
			if(check)
				return ply-10000;
			return 0;
		}
		return best;
	}
/*	int search(int alpha,int beta,int depth,int ply,int donull)
	{
		int next,best=-10000,val,moves=0;
		move m;
		boolean check=b.incheck(),pvfound=false;
		
		b.pvl[ply]=ply;
		b.nodes++;
		if(check)
			depth++;
		b.gen(ply);
		score(ply);
		sortmoves(ply);
		for(next=b.msi[ply];next<b.msi[ply+1];next++)
		{
			m=b.ms[next];
			if(b.make(m))
			{
				moves++;
				if(depth>1)
				{
					if(pvfound)
					{
						val=-search(-alpha-1,-alpha,depth-1,ply+1,1);
						if(val>alpha&&val<beta)
							val=-search(-beta,-alpha,depth-1,ply+1,1);
					}
					else
						val=-search(-beta,-alpha,depth-1,ply+1,1);
				}
				else
					val=-searchq(-beta,-alpha,ply+1);
			//		val=-e.e();
				b.unmake();
				if(val>best)
				{
					best=val;
					if(val>alpha)
					{
						b.pv[ply][ply]=m;
						for(int x=ply+1;x<b.pvl[ply+1];x++)
							b.pv[ply][x]=b.pv[ply+1][x];
						b.pvl[ply]=b.pvl[ply+1];
						pvfound=true;
						if(val>=beta)
						{
							if(m.c==0&&m.pr==0)
							{
								b.kill[ply][1]=b.kill[ply][0];
								b.kill[ply][0]=m;
							}
							b.hist[(m.f&7)+((m.f>>1)&0x38)][(m.t&7)+((m.t>>1)&0x38)]+=depth*depth;
							return val;
						}
						alpha=val;
					}
				}
			}
		}
		if(moves==0)
		{
			if(check)
				return ply-10000;
			return 0;
		}
		return best;
	}
*/
	int searchq(int alpha,int beta,int ply)
	{
		int next,val;
		move m;
		
		b.pvl[ply]=ply;
		b.qnodes++;
		val=e.e();

		if(val>=beta)
			return beta;
		if(val>alpha)
			alpha=val;

		b.gencaps(ply);
		scoreq(ply);
		sortmoves(ply);
		for(next=b.msi[ply];next<b.msi[ply+1];next++)
		{
			if(b.ss[next]<=0)
				break;
			m=b.ms[next];
			if(b.make(m))
			{
				val=-searchq(-beta,-alpha,ply+1);
				b.unmake();
				if(val>alpha)
				{
			/*		b.pv[ply][ply]=m;
					for(int x=ply+1;x<b.pvl[ply+1];x++)
						b.pv[ply][x]=b.pv[ply+1][x];
					b.pvl[ply]=b.pvl[ply+1];
			*/		if(val>=beta)
						return val;
					alpha=val;
				}
			}
		}
		return alpha;
	}
	void score(int ply)
	{
		for(int x=b.msi[ply];x<b.msi[ply+1];x++)
		{
			b.ss[x]=b.hist[(b.ms[x].f&7)+((b.ms[x].f>>1)&0x38)][(b.ms[x].t&7)+((b.ms[x].t>>1)&0x38)];
			if(b.ms[x].c!=0)
				b.ss[x]+=5000+b.ms[x].c-b.ms[x].p;
			else if(b.kill[ply][0]!=null&&b.ms[x].equalTo(b.kill[ply][0]))
				b.ss[x]+=1000;
			else if(b.kill[ply][1]!=null&&b.ms[x].equalTo(b.kill[ply][0]))
				b.ss[x]+=800;
		}
	}
	void scoreq(int ply)
	{
		for(int x=b.msi[ply];x<b.msi[ply+1];x++)
			b.ss[x]+=b.ms[x].c-b.ms[x].p+b.ms[x].pr;
	}
	void sortmoves(int ply)
	{
		int best;
		for(int x=b.msi[ply];x<b.msi[ply+1];x++)
		{
			best=x;
			for(int y=x+1;y<b.msi[ply+1];y++)
				if(b.ss[y]>b.ss[best])
					best=y;
			if(best!=x)
			{
				move tm=b.ms[best];
				b.ms[best]=b.ms[x];
				b.ms[x]=tm;
				int ts=b.ss[best];
				b.ss[best]=b.ss[x];
				b.ss[x]=ts;
			}
		}
	}
}
