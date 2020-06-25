import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;

public class Relay extends AbstractProc {

    private ServerSocket serv_socket;

    private String config;

    public Relay(String[] args) {
        super(args);
        try {
            this.serv_socket = new ServerSocket(TCP_PORT);

        }catch (Exception ex){ex.printStackTrace();}

    }



    public void runRelay(){
        System.out.println("Running relay on: " + ip_relay);

        System.out.println("Start listenig TCP...");

        try {
            tcp_socket = serv_socket.accept();
        }catch (Exception ex){ex.printStackTrace();}

        System.out.println("Connection from: " + tcp_socket.getRemoteSocketAddress());

        es.submit(() -> { //input thread
            try {

                establishConnection();

                while (true){

                  String command = instream.readLine();

                  //message~portUDP(port where we should to send data)

                      System.out.println("Recieved packet from agent...");

                      String[] data = command.split("~");

                      DatagramSocket ds = new DatagramSocket((int)(10000 + Math.random() * 55000));

                      System.out.println("Send this packet to client on: " + config + ":\u001B[36m" + data[1] + "\u001B[0m");

                      ds.send(new DatagramPacket(data[0].getBytes(),data[0].getBytes().length,
                                                new InetSocketAddress(config,Integer.parseInt(data[1]))));

                      ds.close();

                  }

            }catch (Exception ex){ ex.printStackTrace();}
        });

    }

    public void establishConnection() throws Exception{

        instream = new BufferedReader(new InputStreamReader(tcp_socket.getInputStream()));

        System.out.println("Waiting for config...");

        config = instream.readLine();

        System.out.println("Recieved config: " + config + "\n"
                + "Open UDP port...");

        //UDP PORT LISTENING THREAD  relay->TCP->agent
        es.submit(() -> {


            try {
               DatagramSocket ds = new DatagramSocket(udp_port_relay);

                outstream = new PrintWriter(new OutputStreamWriter(tcp_socket.getOutputStream()),true);

                System.out.println("UDP port: \u001B[36m" + udp_port_relay + "\n\u001B[0m" +
                    "Waiting for messages from client...");

            while (true) {

                DatagramPacket inpacket = new DatagramPacket(buf,buf.length);

                ds.receive(inpacket);

                System.out.println("Recieved packet from: " + inpacket.getAddress().getHostAddress() + ":" + inpacket.getPort());

                System.out.println("Send this packet to agent...");

                String aa = new String(inpacket.getData(),0,inpacket.getLength()) + "~" + inpacket.getPort();

                aa = aa.replaceAll("\n","");

                System.out.println(aa);

                outstream.println(aa);

                System.out.println("Sent, waiting for new messages...");
            }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

    }
}
