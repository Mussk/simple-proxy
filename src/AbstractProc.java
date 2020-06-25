import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractProc {

    private String[] args;

    final static int TCP_PORT = 50128;

    final static  int BUFF_SIZE = 256;

    byte[] buf = new byte[BUFF_SIZE];

     String ip_relay;

     String ip_client, ip_back; //behind relay /behind agent

    List<Integer> udp_ports;

    ExecutorService es;

    Socket tcp_socket;

    List<DatagramSocket> udp_sockets;

    Integer udp_port_relay;

    BufferedReader instream;

    PrintWriter outstream;

    public AbstractProc(String[] args) {
        this.args = args;
        this.es = Executors.newFixedThreadPool(5);
        this.tcp_socket = new Socket();
        this.udp_ports = new ArrayList<>();
        this.udp_sockets = new ArrayList<>();

        if (args[0].equals("relay")){
            this.udp_port_relay = (int)(10000 + Math.random() * 50000);
            try {
                this.ip_relay = InetAddress.getLocalHost().getHostAddress();
            }catch (Exception ex){ex.printStackTrace();}
        }


        else  if (args[0].equals("agent")) {
            this.ip_relay = args[1];
            this.ip_client = args[2];

            for (int i = 3; i < args.length; i++) {
                udp_ports.add(Integer.parseInt(args[i])); // agent listens this ports

                try {
                udp_sockets.add(new DatagramSocket(udp_ports.get(i-3)));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        }

    }
}
