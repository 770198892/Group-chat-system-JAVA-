package Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ReciveFile {
	private int port;
	private String hostname;
	private Socket socket;
	private static String savePath="D:\\eclipse\\easyChat\\file";
	public ReciveFile(String host,int port)
	{
		hostname=host;
		this.port=port;
		
        
		try {
			socket = new Socket(hostname, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// 网上抄来的，将 int 转成字节
    public static byte[] i2b(int i) {
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
            System.out.println("发送文件：" + file.getName() + "，长度：" + length);
 
            // 发送文件名和文件内容
            writeFileName(file, os);
            writeFileContent(is, os, length);
        } finally {
            os.close();
            is.close();
        }
    }
 
    // 输出文件内容
    private void writeFileContent(InputStream is, OutputStream os, int length) throws IOException {
        // 输出文件长度
        os.write(i2b(length));
 
        // 输出文件内容
        byte[] buffer = new byte[4096];
        int size;
        while ((size = is.read(buffer)) != -1) {
            os.write(buffer, 0, size);
        }
    }
 
    // 输出文件名
    private void writeFileName(File file, OutputStream os) throws IOException {
        byte[] fn_bytes = file.getName().getBytes();
 
        os.write(i2b(fn_bytes.length));         // 输出文件名长度
        os.write(fn_bytes);    // 输出文件名
    }
    /*
     * 获取服务器文件
     * 并且保存在本地文件夹下
     */
 // 网上抄来的，将字节转成 int。b 长度不得小于 4，且只会取前 4 位。
    public static int b2i(byte[] b) {
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
	  if(!getFileName(is).equals("")) {
		   while(true)
		   {
			   readAndSave(is);
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
}
