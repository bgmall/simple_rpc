package simple.net;

public class NetServerOptions {

    /**
     * listen port of the server
     */
    private int listenPort;

    /**
     * backlog.
     */
    private int backlog;

    /**
     * The connect timeout in milliseconds
     */
    private int connectTimeout;

    /**
     * The send buffer size.
     */
    private int sendBufferSize;

    /**
     * The receive buffer size.
     */
    private int receiveBufferSize;

    /**
     * The length need compress
     */
    private int requiredCompressLength;

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

    /**
     * The acceptor threads.
     */
    private int acceptorThreads = 0; // acceptor threads. default use Netty default value

    /**
     * The event loop thread pool size.
     */
    private int workThreads = 20;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    public void setAcceptorThreads(int acceptorThreads) {
        this.acceptorThreads = acceptorThreads;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public int getRequiredCompressLength() {
        return requiredCompressLength;
    }

    public void setRequiredCompressLength(int requiredCompressLength) {
        this.requiredCompressLength = requiredCompressLength;
    }
}
