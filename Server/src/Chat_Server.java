import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Chat_Server extends JFrame {

	private static boolean isPrint = false;// 是否输出消息标志
	private int m_onlineNumber =0;//聊天人数
	private boolean offLine=false;
	private JButton open;
	private JTextArea show;
	
	
	private static List<ServerThread> thread_list = new ArrayList<ServerThread>();
	private static LinkedList<Message> message_list = new LinkedList<Message>();// 存放用户消息的队列//同步
	
	/*
	 * 文件传输相关变量
	 */
	byte[] inputByte = null;
    int length = 0;
    DataInputStream dis = null;
    FileOutputStream fos = null;
	
	public Chat_Server()
	{
		super("EasyChat服务器总台");
		Container c=getContentPane();
		
		show=new JTextArea();
		show.setEditable(false);
		open=new JButton("启动服务器");
		c.add(show, BorderLayout.CENTER);
		c.add(open,BorderLayout.SOUTH);	
		
	}
	@SuppressWarnings("resource")
	public void getmessages()
	{	//启动文件传输服务器
		try {
			new Server(7789);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//启动普通信息传输器
		new PrintOutThread();
		try {
			ServerSocket server=null;
			server=new ServerSocket(2559);
			System.out.println("普通消息传输服务器"+2559);
			String messages;
			int i=0;
			Socket s;
			while(true)
			{
				s=server.accept();
				//receiveFile(s);
				thread_list.add(new ServerThread(s,i));
				i++;
			}
			
		}
		catch(Exception e)
		{
			System.err.println("发生异常:"+e);
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Chat_Server app=new Chat_Server();
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.setSize(350,150);
		app.setVisible(true);
		app.getmessages();
	}
	public class ServerThread extends Thread{
		
		private int id;
		Socket client;
		private ObjectOutputStream m_output;//输出流 
		private ObjectInputStream m_input;//输入流
		public ServerThread(Socket s,int i)
		{
			client = s;
			id=i;
			try
			{
				m_input=new ObjectInputStream(s.getInputStream());
				m_output=new ObjectOutputStream(s.getOutputStream());
				start();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try
			{
				String messages;
				offLine=true;
				while(offLine)
				{
					messages=m_input.readObject().toString();
					if(messages.equals("!exit!"))
						offLine=false;
					message_list.add(new Message(id,messages));
					isPrint=true;
				}
				m_output.flush();
				m_output.close();
				m_input.close();
				client.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		public void sendMessage(String msg) throws IOException
		{
			m_output.writeObject(msg);
		}
		public int getID()
		{
			return id;
		}
		
	}
	
	
	
	
	class PrintOutThread extends Thread {
		 
		public PrintOutThread() {
			start();
		}
 
		public void run() {
		while (true) {
			//如果消息队列没有消息则暂停当前线程，把cpu片段让出给其他线程,提高性能
			if (!isPrint) {
				try {
					Thread.sleep(500);
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			// 将缓存在队列中的消息按顺序发送到各客户端，并从队列中清除。
			Message msg = (Message)message_list.getFirst();
			// 对所有的用户的线程遍历，如果不是自己发的消息就广播给其他人
			for (int i = 0; i < thread_list.size(); i++) {
				// 由于添加线程和用户是一起的，所以i所对应的用户就是i所对应的线程，可以根据这个判断是不是自己的线程
				ServerThread thread = thread_list.get(i);
					try {
						if(i!=msg.getName())
						thread.sendMessage(msg.getMsg());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
			message_list.removeFirst();
			isPrint = message_list.size() > 0 ? true : false;
		}
	}
	}
}
