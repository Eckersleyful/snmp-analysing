package snmp;

import java.util.ArrayList;
import java.util.List;


public class SnmpManagerThread implements Runnable{

    private SnmpBaseManager snmpBaseManager;

    private ArrayList<Double> executionTimes;

    private int pollFrequency;

    private int pollAmount;


    public SnmpManagerThread(SnmpBaseManager snmpBaseManager, int pollFrequency, int pollAmount){

        this.snmpBaseManager = snmpBaseManager;
        this.executionTimes = new ArrayList();
        this.pollFrequency = pollFrequency;
        this.pollAmount = pollAmount;
    }

    @Override
    public void run() {

        try {
            for(int i = 0; i < pollAmount; i++){
                long startTime = System.nanoTime();
                this.snmpBaseManager.sendSnmpRequest();
                long endTime = System.nanoTime();
                this.executionTimes.add((double) (endTime - startTime));
                Thread.sleep(pollFrequency);
            }
            this.snmpBaseManager.getSnmp().close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public List<Double> getResultList(){
        return this.executionTimes;
    }

}
