package com.example.ale.medidas;

/**
 * Created by ale on 24/04/2017.
 */

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    //public static final String SERVERIP = "10.1.18.121";
    //public static final int SERVERPORT = 23;
    //public static final String SERVERIP = "192.168.173.1";
    public static final String SERVERIP = "10.1.18.121";
    public static final int SERVERPORT = 5023; //test con servidor echo
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;
    Socket socket;
    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {

        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */

    public void sendMessage(String message) {
        //Envía comandos sin espera de una respuesta: p.e comandos de configuración del VNA
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }


    public void stopClient(){
        mRun = false;
    }

    public void run(String txt) {
        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            //create a socket to make the connection with the server
            //Socket socket = new Socket(serverAddr, SERVERPORT);
            this.socket = new Socket(serverAddr, SERVERPORT);
            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.e("TCPClient", "alej: Socket con el servidor Abierto!");
                sendMessage(txt);

                //in this while the client listens for the messages sent by the server
                //while (mRun) {
                    serverMessage = in.readLine(); //comprobamos si ha llegado algo al servidor

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                //}
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                socket.close();

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }
            //finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                //socket.close();
            //}

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}