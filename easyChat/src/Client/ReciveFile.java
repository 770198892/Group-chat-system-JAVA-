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
	// ���ϳ����ģ��� int ת���ֽ�
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
            System.out.println("�����ļ���" + file.getName() + "�����ȣ�" + length);
 
            // �����ļ������ļ�����
            writeFileName(file, os);
            writeFileContent(is, os, length);
        } finally {
            os.close();
            is.close();
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
}
