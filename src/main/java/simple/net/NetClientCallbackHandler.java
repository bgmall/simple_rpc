package simple.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import simple.net.callback.ClientCallState;
import simple.net.protocol.message.CallbackMessage;
import simple.net.protocol.message.NetMessage;

public class NetClientCallbackHandler extends SimpleChannelInboundHandler<NetMessage> {

    private NetClient netClient;

    public NetClientCallbackHandler(NetClient netClient) {
        this.netClient = netClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {
        if (msg instanceof CallbackMessage) {
            CallbackMessage callbackMessage = (CallbackMessage) msg;
            ClientCallState clientCallState = netClient.removePendingRequest(callbackMessage.getCallbackId());
            if (clientCallState != null) {
                clientCallState.handleResponse(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
