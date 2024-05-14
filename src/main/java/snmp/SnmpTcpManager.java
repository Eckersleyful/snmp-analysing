package snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultTcpTransportMapping;

import java.io.IOException;

public class SnmpTcpManager extends SnmpBaseManager {


    public SnmpTcpManager() throws IOException {
        super(new DefaultTcpTransportMapping());
        this.getCommunityTarget().setAddress(new TcpAddress(LOCALHOST + "/161"));

    }

    @Override
    public String toString(){
        return "TCP " + super.toString();
    }


}