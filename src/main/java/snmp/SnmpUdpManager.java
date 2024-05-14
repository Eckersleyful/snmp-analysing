package snmp;

import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SnmpUdpManager extends SnmpBaseManager {


    public SnmpUdpManager() throws IOException {
        super(new DefaultUdpTransportMapping());
        this.getCommunityTarget().setAddress(new UdpAddress(LOCALHOST + "/161"));
    }

    @Override
    public String toString(){
        return "UDP " + super.toString();
    }

}