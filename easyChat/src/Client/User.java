package Client;

import javax.swing.ImageIcon;

public class User {
	private String name;
	private ImageIcon face;
	
	public User(String name,ImageIcon face)
	{
		this.name=name;
		this.face=face;
	}
	public User()
	{
		name="ะกร๗";
		face=new ImageIcon("");
	}
	public boolean setNmame(String m)
	{
		if(m!=null)
		{
			name=m;
			return true;
		}else
		{
			return false;
		}
			
	}
	public String getName()
	{
		return name;
	}
	public ImageIcon getface()
	{
		return face;
	}
}
