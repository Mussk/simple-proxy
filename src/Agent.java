import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Agent extends AbstractProc {

    public Agent(String[] args) {
        super(args);
    }


    public  void runAgent(){
        System.out.println("Running agent...");

        System.out.println("Make a TCP connection to " + ip_relay
                + " on port " + TCP_PORT + "...");

        try {
            tcp_socket.connect(new InetSocketAddress(ip_relay,TCP_PORT));
    }catch (Exception ex){ex.printStackTrace();}

        System.out.println("Succesfully connected!");

        runUDPagent();

    //CRATE I/O STREAMS
        es.execute(() -> { //input thread
            try {
                instream = new BufferedReader(new InputStreamReader(tcp_socket.getInputStream()));

                //realay -> TCP -> agent -> client

                while (true){

                    System.out.println("TCP: Start listening relay...");

                   String msg = instream.readLine();

                   System.out.println("TCP: New message from " + tcp_socket.getRemoteSocketAddress());

                    int recieved_port = Integer.parseInt(msg.split("~")[1]);

                    System.out.println("send this to client on  " + ip_client + ":\u001B[31m" + recieved_port + " \u001B[0m");

                    //find udp socket with port

                    int tmp_port = (int)(10000 + Math.random() * 55000);

                    System.out.println("UDP: Temporary port: " + tmp_port);

                    DatagramSocket ds = new DatagramSocket(tmp_port);

                    System.out.println(msg);

                    ds.send(new DatagramPacket(msg.split("~")[0].getBytes(),msg.split("~")[0].length(),new InetSocketAddress(ip_client,recieved_port)));

                    ds.close();

                    System.out.println("sended!");

                    System.out.println("start listening for new messsages...");

                }

            }catch (Exception ex){ ex.printStackTrace();}
        });

        //output TCP thread
        es.execute(() -> {
            try {
                outstream = new PrintWriter(new OutputStreamWriter(tcp_socket.getOutputStream()),true);

                System.out.println("Send config to relay...");

                outstream.println(ip_client);

                System.out.println("Config sent sucssessfully!");

            }catch (Exception ex){ex.printStackTrace();}

        });


    }

    public void runUDPagent(){

        System.out.println("Starting listenig UDP from clients on" +
                (udp_ports.size() == 1 ? "\u001B[36m port " + udp_ports.get(0) + " \u001B[0m"
                                        : "\u001B[36m ports: " + udp_ports.toString()) + " \u001B[0m");

       List<DatagramPacket> packets = new ArrayList<>();

       //listen packets from clients
        es.execute(() -> {

            for (int i = 0; i < udp_sockets.size(); i++) {

              int count = i;

                    packets.add(new DatagramPacket(new byte[BUFF_SIZE], BUFF_SIZE));

                    new Thread(() -> {

                        try {

                            DatagramSocket thread_socket = udp_sockets.get(count);

                            DatagramPacket packet = packets.get(count);

                            while (true) {

                                thread_socket.receive(packet);

                                System.out.println("Recieved packet on port: "
                                        + thread_socket.getLocalPort());

                                System.out.println("Send it through TCP tunnel...");

                                String aa = new String(packet.getData(),0,packet.getLength()) + "~" + packet.getPort();

                                aa = aa.replaceAll("\n","");

                                System.out.println(aa);

                                outstream.println(aa);

                                System.out.println("Listen for new packets...");

                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }).start();


                }
        });

    }


}
