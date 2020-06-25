

public class Starter {

    public static void main(String[] args) {

        try {
            if (args[0].equals("agent"))
                runAgent(args); //agent ip_relay ip_client <UDP ports>

            else if (args[0].equals("relay"))
                runRelay(args); // relay

        } catch (Exception ex) {
        }


    }

    public static void runAgent(String[] args) {

        new Agent(args).runAgent();
    }


    public static void runRelay(String[] args) {

        new Relay(args).runRelay();

    }


}

