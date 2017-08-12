package com.app.albertstudio.vt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

//import com.asus.control.ControlAPI;
//import java.util.logging.Handler;
//import android.leds.LedsWheelManager;


class SocketThread extends Thread
{
	Context mContext;
	boolean isEnd = true;

	public SocketThread(Context ct)
	{
		mContext = ct;

	}

	public boolean isConnected()
	{
		return  (!isEnd);
	}

	public void Disconnected()
	{
		isEnd = true;
	}
	
	public void run()
	{

		String tName = Thread.currentThread().getName();
		try
		{

			Log.d("ConversationResponse","Waitting to connect......");
			String server = "192.168.43.93";
            //String server = "172.20.10.4";
            //String server = "192.168.43.7";
			int servPort = 8787;
			Socket socket = new Socket();
			while(socket.isConnected() == false)
			{
                Log.d("ConversationResponse","Trying to connect......");
				SocketAddress socketAddress = new InetSocketAddress(server,servPort);
				try
				{
					socket.connect(socketAddress);
					isEnd = false;
				}
				catch(Exception e)
		        {
					Log.d("ConversationResponse","Error: "+e.getMessage());

		        }
				this.sleep(1000);
			}
            MainActivity.in2PKG = socket.getInputStream();
            MainActivity.out2PKG = socket.getOutputStream();
            MainActivity.pw2PKG = new PrintWriter(MainActivity.out2PKG,true);
            MainActivity.br2PKG = new BufferedReader(new InputStreamReader(MainActivity.in2PKG));

			String str2 = "Toby Connected!!";
            MainActivity.pw2PKG.write(str2 + "\n");
            MainActivity.pw2PKG.flush();
            Log.i("ConversationResponse", "Connected!!");

            while(isEnd == false)
			{
				if(MainActivity.br2PKG.ready())
				{
					String msg_received = MainActivity.br2PKG.readLine();
                    String[] cmd = msg_received.split(":");
					String str ="Received : "+ msg_received;
					Log.d("ConversationResponse",str);
					if(msg_received.equals("GETSOURCE_BROADCAST") == true)
					{
						Intent i = new Intent(MainActivity.GETSOURCE_BROADCAST);
						mContext.sendBroadcast(i);
					}
                    else if(cmd[0].equals("GETTARGET_BROADCAST") == true)
                    {
                        Intent i = new Intent(MainActivity.GETTARGET_BROADCAST);
                        mContext.sendBroadcast(i);
                    }
				}
			}
            MainActivity.pw2PKG.close();
            MainActivity.br2PKG.close();
			socket.close();

		}
		catch(Exception e)
        {
			Log.d("ConversationResponse","Error: "+e.getMessage());
        }
  	} 
}
