package simple.net;

public class NetClientOptions {

    /**
     * The io eventLoop thread count.
     */
    private int eventLoopThreads = 0;

    /**
     * The connect timeout in milliseconds
     */
    private int connectTimeout;

    /**
     * The reconnect interval in milliseconds
     */
    private int reconnectIntervalMills;


    private int heartBeatIntervalMills;

    /**
     * The send buffer size.
     */
    private int sendBufferSize;

    /**
     * The receive buffer size.
     */
    private int receiveBufferSize;

    /**
     * The max length of package
     */
    private int maxFrameLength;

    /**
     * The tcp no delay.
     */
    private boolean tcpNoDelay;

    /**
     * The keep alive.
     */
    private boolean keepAlive;

    /**
     * The reuse address.
     */
    private boolean reuseAddress;

    /**
     * The idle timeout.
     */
    private int idleTimeoutSeconds;

    public int getEventLoopThreads() {
        return eventLoopThreads;
    }

    public void setEventLoopThreads(int eventLoopThreads) {
        this.eventLoopThreads = eventLoopThreads;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReconnectIntervalMills() {
        return reconnectIntervalMills;
    }

    public void setReconnectIntervalMills(int reconnectIntervalMills) {
        this.reconnectIntervalMills = reconnectIntervalMills;
    }

    public int getHeartBeatIntervalMills() {
        return heartBeatIntervalMills;
    }

    public void setHeartBeatIntervalMills(int heartBeatIntervalMills) {
        this.heartBeatIntervalMills = heartBeatIntervalMills;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public int getIdleTimeoutSeconds() {
        return idleTimeoutSeconds;
    }

    public void setIdleTimeoutSeconds(int idleTimeoutSeconds) {
        this.idleTimeoutSeconds = idleTimeoutSeconds;
    }
}
