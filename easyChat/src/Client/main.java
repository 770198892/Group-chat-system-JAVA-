package Client;

import javax.swing.JFrame;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Chat_Client chat=new Chat_Client();
		chat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		chat.setSize(350,600);
		chat.setVisible(true);
		chat.connectServer("localhost", 2559);
		//chat.connectSercerFile();
	}

}
