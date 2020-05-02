import java.io.InputStream;

public class Filemessages {
	private int id;
	private InputStream is;
	
	public Filemessages(int m,InputStream n)
	{
		id=m;
		is=n;
	}
	
	public int getID()
	{
		return id;
	}
	
	public InputStream getFile()
	{
		return is;
	}
}
