package net;

import simple.net.protocol.ProtocolType;
import simple.net.protocol.annotation.NetProtocol;
import simple.net.protocol.impl.bigData.BigDataMessage;

@NetProtocol(msgId = 3, protocolType = ProtocolType.BIGDATA)
public class SimpleBigDataMessage extends BigDataMessage {


}
