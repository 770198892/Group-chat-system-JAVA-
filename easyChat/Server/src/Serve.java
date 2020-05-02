

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;




/*
 * �ļ����������
 * �˿�7788�����ļ�����˿�
 */
class Server extends Thread{
	 
    private int listenPort;
    //�ļ������̼߳���
    private static List<HandleThread>handle_list=new ArrayList<HandleThread>();
    //�ļ�����
    private static LinkedList<Filemessages> message_list = new LinkedList<Filemessages>();
    private String savePath="c://save//";
    private boolean isPrint=false; 
    /**
     * ���췽��
     *
     * @param listenPort �����˿�
     * @param savePath   ���յ��ļ�Ҫ�����·��
     *
     * @throws IOException �����������·��ʧ��
     */
    Server(int listenPort) throws IOException {
        this.listenPort = listenPort;
        new PrintOutThread();
        start();
    }
 
 
    // ���ϳ����ģ����ֽ�ת�� int��b ���Ȳ���С�� 4����ֻ��ȡǰ 4 λ��
    public static int b2i(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
    
    /**
     * �����߳�
     */
  
 
        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(listenPort);
                System.out.println("���������ļ����Ӵ���"+listenPort);
                // ��ʼѭ��
                int i=0;
                while (true) {
                    Socket socket = server.accept();
                    //���̷߳����̼߳���
                    handle_list.add(new HandleThread(socket,i));
                    i++;
                    System.out.println("�ͻ���"+i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    /**
     * ��ȡ���������ļ����߳�
     */
    private class HandleThread extends Thread {
 
        private Socket socket;
        int id;
        private HandleThread(Socket socket,int i) {
            this.socket = socket;
            id=i;
            start();
        }
 
        @Override
        public void run() {
            try {
            	//��ȡ���Կͻ��˵�������
                InputStream is = socket.getInputStream();
                //message_list.add(new Filemessages(id,is));
                
                while(true)
                {
                	for(int i=0;i<handle_list.size();i++)
                	{
                		if(i==id)
                			continue;
                		handle_list.get(i).sendFile(is);
                	}
                	
                	
                    //isPrint=true;//��������
                	
                    //readAndSave(is);
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                	if(isPrint==false)
                	{
                		socket.close();
                	}                  
                } catch (IOException e) {
                    // nothing to do
                }
            }
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
        
        /*
         * ��ͻ��˷����ļ�
         */
        
        public byte[] i2b(int i) {
            return new byte[]{
                    (byte) ((i >> 24) & 0xFF),
                    (byte) ((i >> 16) & 0xFF),
                    (byte) ((i >> 8) & 0xFF),
                    (byte) (i & 0xFF)
            };
           
        }
        public void sendFile(InputStream iso) throws IOException {
            OutputStream os = socket.getOutputStream();
            byte[] buffer = new byte[4096];
            int size;
            while ((size = iso.read(buffer)) != -1) {
                os.write(buffer, 0, size);
          }
        }
    }
            
        
    /*
     * �����������ļ��߳�
     */
    class PrintOutThread extends Thread {
		 
		public PrintOutThread() {
			start();
		}
 
		public void run() {
		System.out.println("�ļ������߳̿���"+isPrint);
		while (true) {
			//�����Ϣ����û����Ϣ����ͣ��ǰ�̣߳���cpuƬ���ó��������߳�,�������
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
			System.out.println("�ļ������߳̿���"+isPrint);
			// �������ڶ����е���Ϣ��˳���͵����ͻ��ˣ����Ӷ����������
			Filemessages f = (Filemessages)message_list.getFirst();
			// �����е��û����̱߳�������������Լ�������Ϣ�͹㲥��������
			for (int i = 0; i < handle_list.size(); i++) {
				// ��������̺߳��û���һ��ģ�����i����Ӧ���û�����i����Ӧ���̣߳����Ը�������ж��ǲ����Լ����߳�
				HandleThread thread = handle_list.get(i);
					try {
						//JOptionPane.showMessageDialog(null,"�ļ����ͣ�!");
						if(i!=f.getID())
						{
							thread.sendFile(f.getFile());
						}
						//thread.sendMessage(msg.getMsg());
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
    
   
   

