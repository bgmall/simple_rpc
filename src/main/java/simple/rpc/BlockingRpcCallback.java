package simple.rpc;

public class BlockingRpcCallback implements RpcCallback {

    private RpcMessage response;

    private RuntimeException runtimeException;

    private boolean done = false; // 会话完成标识

    private boolean isDone() {
        return done;
    }

    private void complete() {
        synchronized (this) {
            this.done = true;
            this.notifyAll();
        }
    }

    public void waitFor(long timeoutMills) {
        if (!isDone()) {
            synchronized (this) {
                while (!isDone()) {
                    try {
                        this.wait(timeoutMills);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    @Override
    public void result(RpcMessage response) {
        this.response = response;
        complete();
    }

    @Override
    public void exceptionCaught(RuntimeException ex) {
        this.runtimeException = ex;
        complete();
    }

    public RpcMessage getResponse() {
        return response;
    }

    public RuntimeException getRuntimeException() {
        return runtimeException;
    }
}
