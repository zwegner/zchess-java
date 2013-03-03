public class move
{
	int f;
	int t;
	int p;
	int c;
	int e;
	int pr;
	public move(int from,int to,int piece)
	{
		f=from;
		t=to;
		p=piece;
		c=0;
		e=0;
		pr=0;
	}
	public move(int from,int to,int piece,int cap)
	{
		f=from;
		t=to;
		p=piece;
		c=cap;
		e=0;
		pr=0;
	}
	public move(int from,int to)//Work around for ep:two arguments only
	{
		f=from;
		t=to;
		p=1;
		c=0;
		e=1;
		pr=0;
	}
	public move(int from,int to,int piece,int cap,int promote)//another work around:cap can be 0,piece is redundant
	{
		f=from;
		t=to;
		p=piece;
		c=cap;
		e=0;
		pr=promote;
	}
	public String toString()
	{
		String prc[]={"","","N","B","R","Q"};
		return (char)((f&15)+'a')+""+((f>>4)+1)+(e!=0?"x":"")+(char)((t&15)+'a')+""+((t>>4)+1)+prc[pr];
	}
	public boolean equalTo(move m)
	{
		return f==m.f&&t==m.t&&pr==m.pr;
	}
}
