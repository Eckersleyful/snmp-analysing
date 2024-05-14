package analysis;

import com.beust.jcommander.Parameter;
import snmp.SnmpBaseManager;
import snmp.SnmpManagerThread;
import snmp.SnmpTcpManager;
import snmp.SnmpUdpManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SnmpAnalysing {


    private static final int RUNS = 2;
    @Parameter(names = {"-t", "--threads"}, description = "How many threads call SNMP Agent in parallel")
    private int parallelThreads;


    @Parameter(names = {"-f", "--pollFrequency"}, description = "How often the SNMP agent is polled")
    private int pollFrequency;

    @Parameter(names = {"-p", "--pollAmount"}, description = "How many times each manager polls the SNMP agent")
    private int pollAmount;


    Set<SnmpManagerThread> managerThreads = Collections.synchronizedSet(new HashSet<SnmpManagerThread>());
    Set<Thread> javaThreads = new HashSet<>();
    ArrayList<List<Double>> allThreadResultLists = new ArrayList<>();

    boolean isUDP;

    public SnmpAnalysing(boolean isUDP) {
        this.isUDP = isUDP;

    }

    public boolean isAgentUp() {
        try {

            System.out.println(this.parallelThreads + " manager threads");
            System.out.println(this.pollAmount + " poll times");
            System.out.println(this.pollFrequency + " poll frequency");
            SnmpTcpManager tcpAgent = new SnmpTcpManager();


            SnmpUdpManager udpAgent = new SnmpUdpManager();

            return udpAgent.sendSnmpRequest().getResponse() != null && tcpAgent.sendSnmpRequest().getResponse() != null;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private void createThreads() throws IOException {

        for (int i = 0; i < parallelThreads; i++) {
            SnmpBaseManager baseManager = null;
            if (this.isUDP) {
                baseManager = new SnmpUdpManager();
            } else {
                baseManager = new SnmpTcpManager();
            }

            SnmpManagerThread snmpManagerThread = new SnmpManagerThread(baseManager, pollFrequency, pollAmount);
            Thread thread = new Thread(snmpManagerThread);
            javaThreads.add(thread);
            managerThreads.add(snmpManagerThread);
            thread.start();
        }
    }

    public void runParallelSnmpGets() throws IOException, InterruptedException {

        createThreads();

        for (Thread thread : javaThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (SnmpManagerThread thread : managerThreads) {
            allThreadResultLists.add(new ArrayList(thread.getResultList()));
        }

        managerThreads.clear();
        javaThreads.clear();
    }


    private void writeResultsToFile(String protocol, double firstPollMean, double allPollsmean, double fastestPoll, double slowestPoll, double standardDeviation, int missedPolls) {

        String fileName =  protocol + "_t_" + this.parallelThreads + "_p_" + this.pollAmount + "_f_" + this.pollFrequency + ".csv";

        File file = new File(fileName);

        try (FileWriter fileWriter = new FileWriter(fileName, true)) {

            //Only write the header if it does not exist
            if (file.length() == 0 || !file.exists()) {
                fileWriter.append("Mean of first polls;Mean of all polls;Fastest poll;Slowest poll;Standard deviation of all polls;Failed polls\n");
            }
            fileWriter.append(String.format("%.3f;%.3f;%.3f;%.3f;%.3f;%d\n", firstPollMean, allPollsmean,  fastestPoll, slowestPoll, standardDeviation, missedPolls));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private double standardDeviation(List<Double> values, double mean) {
        double squaredDifferences = 0;

        for (double value : values) {
            double difference = value - mean;
            squaredDifferences += difference * difference;
        }

        double meanOfSquaredDifferences = squaredDifferences / values.size();

        return Math.sqrt(meanOfSquaredDifferences);
    }


    public void runBenchmark() throws IOException, InterruptedException {


        System.out.println("----------------------------------------------------------------------------\n");
        System.out.println("Spinning up manager threads\n");


        System.out.println("Waiting for threads to finish their polls\n");

        for(int i = 0; i < RUNS; i++){

            runParallelSnmpGets();
        }

        processResults();


    }


    private void processResults() {

        List<Double> allFirstPolls = allThreadResultLists.stream()
                .map(list -> (list.get(0) / 1_000_000.0))
                .collect(Collectors.toList());

        List<Double> mergedList = allThreadResultLists.stream()
                .flatMap(list -> list.stream().skip(1))
                .map(value -> (value / 1_000_000.0))
                .collect(Collectors.toList());



        long expectedResultsSize = (pollAmount * RUNS * parallelThreads);

        double sumOfAllPolls =  mergedList.stream().mapToDouble(Double::doubleValue).sum();
        double sumOfFirstPolls = allFirstPolls.stream().mapToDouble(Double::doubleValue).sum();

        int missedPolls = (int) ((expectedResultsSize - (RUNS * parallelThreads)) - mergedList.size());

        double firstPollMean = sumOfFirstPolls / (parallelThreads * RUNS);
        double allPollsMean = sumOfAllPolls / expectedResultsSize;

        double fastestPoll = Collections.min(mergedList);
        double slowestPoll = Collections.max(mergedList);

        double standardDeviation = standardDeviation(mergedList, allPollsMean);

        String protocol = isUDP ? "UDP" : "TCP";

        System.out.println(String.format("%s results after %s runs with %s threads and %s polls per thread: \n" +
                "First poll on average took %.3f ms \n" +
                "Poll on average took %.3f ms\n" +
                "Standard deviation of all polls was %.3f ms\n" +
                "Fastest poll was %.3f ms\n" +
                "Slowest poll %.3f ms\n" +
                "%d polls failed to return results", protocol, RUNS, parallelThreads, pollAmount, firstPollMean, allPollsMean, standardDeviation, fastestPoll, slowestPoll, missedPolls));
        System.out.println("----------------------------------------------------------------------------\n");

        writeResultsToFile(protocol, firstPollMean, allPollsMean, fastestPoll, slowestPoll, standardDeviation, missedPolls);
    }

}
