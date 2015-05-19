package fi.helsinki.cs.tmc.langs;

public class CompileResult {

    private int statusCode;
    private byte[] stdout;
    private byte[] stderr;

    public CompileResult(int statusCode, byte[] stdout, byte[] stderr) {
        this.statusCode = statusCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getStdout() {
        return stdout;
    }

    public void setStdout(byte[] stdout) {
        this.stdout = stdout;
    }

    public byte[] getStderr() {
        return stderr;
    }

    public void setStderr(byte[] stderr) {
        this.stderr = stderr;
    }

}
