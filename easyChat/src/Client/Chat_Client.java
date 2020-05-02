package Client;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import sun.net.www.URLConnection;


public class Chat_Client extends JFrame
{
	private ObjectInputStream m_input;
	private ObjectOutputStream m_output;
	JFileChooser fileChooser;
	private JMenuBar m_Bar;
	private JTextField m_enter;//��������
	private JTextArea m_display;//��ʾ����
	private JButton m_send;
	private DataOutputStream dos;
	private FileInputStream fis;
	private int length = 0;
    private byte[] sendBytes = null;
    private Socket s;
    private int state=0;//0Ϊ��ͨ��Ϣ��1λ�ļ���Ϣ
    private ReciveFile file;
    private ToFile tofile;
    private Socket socket;
	/*
	 *�����˵���
	 */
	private User user;
	
	public Chat_Client()
	{
		super("�������ͻ���");
		Container c= getContentPane();
		/*
		 *�����˵���
		 */
		m_Bar=new JMenuBar();
		setJMenuBar(m_Bar);
		JMenu[]m= {new JMenu("��¼"),new JMenu("�ļ�"),new JMenu("�༭")};
		JMenuItem []item={new JMenuItem("�޸���������"),new JMenuItem("Exit"),new JMenuItem("�����ļ�"),
				new JMenuItem("���������¼")};
		m[0].add(item[0]);
		m[1].add(item[2]);
		m[2].add(item[3]);
		m[2].add(item[1]);
		m_Bar.add(m[0]);
		m_Bar.add(m[1]);
		m_Bar.add(m[2]);
		m_enter =new JTextField("",20);
		m_display=new JTextArea();
		
		m_display.setEditable(false);
		m_send=new JButton("send");
		JPanel p=new JPanel();
		p.setLayout(new FlowLayout());
		p.add(m_enter);
		p.add(m_send);
		
		c.add(new JScrollPane(m_display),BorderLayout.CENTER);
		c.add(p,BorderLayout.SOUTH);
		
		/*
		 * �û�����
		 */
		user = new User();
		
		/*
		 * �¼�����
		 */
		m_send.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
				{
					String messages=m_enter.getText().toString();
					sendMessage(messages);	
				}
			}
		);
		
		m_enter.addActionListener(new ActionListener(
				) {
				public void actionPerformed(ActionEvent event)
				{
					//������Ϣ
					String messages=m_enter.getText().toString();
					sendMessage(messages);
					
				}
		}
		);
		item[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				user.setNmame(JOptionPane.showInputDialog(null,"����������������֣�"));	
			}
		});
		
		item[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try
				{
					m_output.writeObject("!exit!");
					m_output.flush();
					System.exit(1);
				}
				catch(Exception ei)
				{
					ei.printStackTrace();
				}	
				
			}
		});
		
		item[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//���ļ�
				fileChooser=new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.showDialog(new JLabel(), "ѡ���ļ�");  
		        File getfile=fileChooser.getSelectedFile();
		        
		        /**
		         * �����ļ���������
		         * 7788�ļ�����˿�
		         */
		       try {
					tofile.sendFile(getfile, getfile.getPath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}      
		});
		item[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//���������¼
				fileChooser=new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setDialogType(fileChooser.SAVE_DIALOG);
				fileChooser.showDialog(new JLabel(), "���������¼");
		          
			}
		});
	}
	
				

	/*
	 * ��ȡʱ��
	 */
	public static String getTime(String url)
	{
		try {
			URL turl=new URL(url);
			java.net.URLConnection conn=turl.openConnection();
			conn.connect();
			long datel=conn.getDate();
			Date date=new Date(datel);
			SimpleDateFormat dateFormat=new SimpleDateFormat("YYYY--MM--dd HH:mm:ss");
			return dateFormat.format(date);
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	/*
	 * ���ӷ�����
	 */
	public void connectServer(String host,int port)
	{
		
		
		try {
			socket=new Socket(host,7789);//�ļ��˿�
			s=new Socket(host,port);//��ͨ��Ϣ�˿�
			tofile=new ToFile();//�����ļ��߳�
			tofile.start();
			String messages;
			//��ʾ�����ͻ��˵���Ϣ
			m_output=new ObjectOutputStream(s.getOutputStream());
			m_input=new ObjectInputStream(s.getInputStream());
			
			while(true) {
				messages=m_input.readObject().toString();
				System.out.println("erro");
				String time=getTime("https://www.baidu.com").toString();
				m_display.append(time+"\n");
				m_display.append(messages+"\n");
				m_display.setCaretPosition(m_display.getText().length());
			}
			
			//???
			/*
			 * m_input.close();
			 * s.close();
			 */
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/*public void connectSercerFile()
	{
	
			file=new ReciveFile("localhost",7788);
			try {
				file.getFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}*/
	//������Ϣ
	public void sendMessage(String messages)
	{
		/*
		 * �������������Ϣ
		 */
		if(!m_enter.getText().isEmpty())
		{
			display(messages);
			m_enter.requestFocusInWindow();//��ȡ����
			m_enter.setText("");
			
			try
			{
				m_output.writeObject(user.getName()+":"+messages);
				m_output.flush();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}	
		}
	}
	
	public void display(String messages)
	{
		messages=m_enter.getText().toString();
		String time=getTime("https://www.baidu.com").toString();
		m_display.append(time+"\n");
		m_display.append(user.getName()+"��"+messages+"\n");
		m_display.setCaretPosition(m_display.getText().length());
	}
	public class ToFile extends Thread
	{
		private int port;
		
		private String hostname;
		
		private static final String savePath="c://save//";
		/*public ToFile()
		{
			
		}*/
		/*
		 * 
		 */
		// ���ϳ����ģ��� int ת���ֽ�
	    public byte[] i2b(int i) {
	        return new byte[]{
	                (byte) ((i >> 24) & 0xFF),
	                (byte) ((i >> 16) & 0xFF),
	                (byte) ((i >> 8) & 0xFF),
	                (byte) (i & 0xFF)
	        };
	       }
	    public void sendFile(File file,String filepath) throws IOException {
	    	
	        FileInputStream is = new FileInputStream(filepath);
	        OutputStream os = socket.getOutputStream();
	        try {
	            int length = (int) file.length();
	            System.out.println("�����ļ���" + file.getName() + "�����ȣ�" + length);
	 
	            // �����ļ������ļ�����
	            writeFileName(file, os);
	            writeFileContent(is, os, length);
	            JOptionPane.showMessageDialog(null,"�ļ����ͳɹ�!");
	        } finally {
	            //os.close();
	            //is.close();
	        }
	    }
	 
	    // ����ļ�����
	    private void writeFileContent(InputStream is, OutputStream os, int length) throws IOException {
	        // ����ļ�����
	        os.write(i2b(length));
	 
	        // ����ļ�����
	        byte[] buffer = new byte[4096];
	        int size;
	        while ((size = is.read(buffer)) != -1) {
	            os.write(buffer, 0, size);
	        }
	    }
	 
	    // ����ļ���
	    private void writeFileName(File file, OutputStream os) throws IOException {
	        byte[] fn_bytes = file.getName().getBytes();
	 
	        os.write(i2b(fn_bytes.length));         // ����ļ�������
	        os.write(fn_bytes);    // ����ļ���
	    }
	    /*
	     * ��ȡ�������ļ�
	     * ���ұ����ڱ����ļ�����
	     */
	 // ���ϳ����ģ����ֽ�ת�� int��b ���Ȳ���С�� 4����ֻ��ȡǰ 4 λ��
	    public int b2i(byte[] b) {
	        int value = 0;
	        for (int i = 0; i < 4; i++) {
	            int shift = (4 - 1 - i) * 8;
	            value += (b[i] & 0x000000FF) << shift;
	        }
	        return value;
	    }
	   public void getFile() throws IOException
	   {
		   InputStream is = socket.getInputStream();
		  //if(!getFileName(is).equals("")) {
			   while(true)
			   {
				   readAndSave(is);
			   }
		  //}   
	   }
	   // �����ж�ȡ���ݲ�����
	   private void readAndSave(InputStream is) throws IOException {
	       String filename = getFileName(is);
	       int file_len = readInteger(is);
	       System.out.println("�����ļ���" + filename + "�����ȣ�" + file_len);

	       readAndSave0(is, savePath + filename, file_len);

	       System.out.println("�ļ�����ɹ���" + file_len + "�ֽڣ���");
	   }

	   private void readAndSave0(InputStream is, String path, int file_len) throws IOException {
	       FileOutputStream os = getFileOS(path);
	       readAndWrite(is, os, file_len);
	       os.close();
	   }

	   // �߶���д��ֱ����ȡ size ���ֽ�
	   private void readAndWrite(InputStream is, FileOutputStream os, int size) throws IOException {
	       byte[] buffer = new byte[4096];
	       int count = 0;
	       while (count < size) {
	           int n = is.read(buffer);
	           // ����û�п��� n = -1 �����
	           os.write(buffer, 0, n);
	           count += n;
	       }
	   }

	   // ��ȡ�ļ���
	   private String getFileName(InputStream is) throws IOException {
	       int name_len = readInteger(is);
	       byte[] result = new byte[name_len];
	       is.read(result);
	       return new String(result);
	   }

	   // ��ȡһ������
	   private int readInteger(InputStream is) throws IOException {
	       byte[] bytes = new byte[4];
	       is.read(bytes);
	       return b2i(bytes);
	   }

	   // �����ļ������������
	   private FileOutputStream getFileOS(String path) throws IOException {
	       File file = new File(path);
	       if (!file.exists()) {
	           file.createNewFile();
	       }

	       return new FileOutputStream(file);
	}
	@Override
	public void run()
	{
		try {
			getFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	    
}
	
	
}

