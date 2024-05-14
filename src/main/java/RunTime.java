import analysis.SnmpAnalysing;
import com.beust.jcommander.JCommander;

import java.rmi.server.UID;

public class RunTime {


    public static void main(String [] args){

        try {
            SnmpAnalysing TCPAnalysis = new SnmpAnalysing(false);
            SnmpAnalysing UDPAnalysis = new SnmpAnalysing(true);
            SnmpAnalysing prober = new SnmpAnalysing(true);
            JCommander.newBuilder()
                    .addObject(TCPAnalysis)
                    .build()
                    .parse(args);

            JCommander.newBuilder()
                    .addObject(UDPAnalysis)
                    .build()
                    .parse(args);
            JCommander.newBuilder()
                    .addObject(prober)
                    .build()
                    .parse(args);

            if(prober.isAgentUp()){

                UDPAnalysis.runBenchmark();
                Thread.sleep(1000);
                TCPAnalysis.runBenchmark();
            }
            else {
                System.out.println("Agent is down");
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
