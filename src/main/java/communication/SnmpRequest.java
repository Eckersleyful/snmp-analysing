package communication;

import org.snmp4j.event.ResponseEvent;

import java.io.IOException;

public interface SnmpRequest {


     ResponseEvent sendSnmpRequest() throws IOException;

}