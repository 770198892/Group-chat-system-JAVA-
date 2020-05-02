

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
 * 文件传输服务器
 * 端口7788代表文件传输端口
 */
class Server extends Thread{
	 
    private int listenPort;
    //文件传输线程集合
    private static List<HandleThread>handle_list=new ArrayList<HandleThread>();
    //文件集合
    private static LinkedList<Filemessages> message_list = new LinkedList<Filemessages>();
    private String savePath="c://save//";
    private boolean isPrint=false; 
    /**
     * 构造方法
     *
     * @param listenPort 侦听端口
     * @param savePath   接收的文件要保存的路径
     *
     * @throws IOException 如果创建保存路径失败
     */
    Server(int listenPort) throws IOException {
        this.listenPort = listenPort;
        new PrintOutThread();
        start();
    }
 
 
    // 网上抄来的，将字节转成 int。b 长度不得小于 4，且只会取前 4 位。
    public static int b2i(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
    
    /**
     * 侦听线程
     */
  
 
        @Override
        public void run() {
            try {
                ServerSocket server = new ServerSocket(listenPort);
                System.out.println("服务器端文件链接创建"+listenPort);
                // 开始循环
                int i=0;
                while (true) {
                    Socket socket = server.accept();
                    //将线程放入线程集合
                    handle_list.add(new HandleThread(socket,i));
                    i++;
                    System.out.println("客户端"+i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    /**
     * 读取流并保存文件的线程
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
            	//获取来自客户端的数据流
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
                	
                	
                    //isPrint=true;//存在问题
                	
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
 
        // 从流中读取内容并保存
        private void readAndSave(InputStream is) throws IOException {
            String filename = getFileName(is);
            int file_len = readInteger(is);
            System.out.println("接收文件：" + filename + "，长度：" + file_len);
 
            readAndSave0(is, savePath + filename, file_len);
 
            System.out.println("文件保存成功（" + file_len + "字节）。");
        }
 
        private void readAndSave0(InputStream is, String path, int file_len) throws IOException {
            FileOutputStream os = getFileOS(path);
            readAndWrite(is, os, file_len);
            os.close();
        }
 
        // 边读边写，直到读取 size 个字节
        private void readAndWrite(InputStream is, FileOutputStream os, int size) throws IOException {
            byte[] buffer = new byte[4096];
            int count = 0;
            while (count < size) {
                int n = is.read(buffer);
                // 这里没有考虑 n = -1 的情况
                os.write(buffer, 0, n);
                count += n;
            }
        }
 
        // 读取文件名
        private String getFileName(InputStream is) throws IOException {
            int name_len = readInteger(is);
            byte[] result = new byte[name_len];
            is.read(result);
            return new String(result);
        }
 
        // 读取一个数字
        private int readInteger(InputStream is) throws IOException {
            byte[] bytes = new byte[4];
            is.read(bytes);
            return b2i(bytes);
        }
 
        // 创建文件并返回输出流
        private FileOutputStream getFileOS(String path) throws IOException {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
 
            return new FileOutputStream(file);
        }
        
        /*
         * 向客户端发送文件
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
     * 服务器传输文件线程
     */
    class PrintOutThread extends Thread {
		 
		public PrintOutThread() {
			start();
		}
 
		public void run() {
		System.out.println("文件发送线程开启"+isPrint);
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
			System.out.println("文件发送线程开启"+isPrint);
			// 将缓存在队列中的消息按顺序发送到各客户端，并从队列中清除。
			Filemessages f = (Filemessages)message_list.getFirst();
			// 对所有的用户的线程遍历，如果不是自己发的消息就广播给其他人
			for (int i = 0; i < handle_list.size(); i++) {
				// 由于添加线程和用户是一起的，所以i所对应的用户就是i所对应的线程，可以根据这个判断是不是自己的线程
				HandleThread thread = handle_list.get(i);
					try {
						//JOptionPane.showMessageDialog(null,"文件发送！!");
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
    
   
   

