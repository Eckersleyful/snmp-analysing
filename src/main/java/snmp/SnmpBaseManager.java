package snmp;

import communication.SnmpRequest;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import java.io.IOException;

public abstract class SnmpBaseManager implements SnmpRequest {


    protected final String LOCALHOST = "localhost";
    private final String SNMP_COMMUNITY = "public";
    private final int SNMP_VERSION = SnmpConstants.version2c;
    private final String TIMESTAMP_OID = "1.3.6.1.2.1.1.1.0";

    private PDU pdu;

    private CommunityTarget communityTarget;

    private TransportMapping transportMapping;

    private Snmp snmp;

    public SnmpBaseManager(TransportMapping transportMapping) throws IOException {
        this.transportMapping = transportMapping;
        init();
    }

    private void init() throws IOException {

        this.transportMapping.listen();
        this.snmp = new Snmp(this.transportMapping);

        communityTarget = new CommunityTarget();
        communityTarget.setCommunity(new OctetString(SNMP_COMMUNITY));
        communityTarget.setRetries(2);
        communityTarget.setTimeout(500);
        communityTarget.setVersion(SNMP_VERSION);

        this.pdu = new PDU();
        pdu.add(new VariableBinding(new OID(TIMESTAMP_OID)));
        pdu.setType(PDU.GET);

    }


    @Override
    public ResponseEvent sendSnmpRequest() throws IOException {
        return snmp.send(this.pdu, this.communityTarget);
    }

    public CommunityTarget getCommunityTarget() {
        return communityTarget;
    }

    public Snmp getSnmp(){
        return this.snmp;
    }

    public TransportMapping getTransportMapping(){
        return this.transportMapping;
    }

}
