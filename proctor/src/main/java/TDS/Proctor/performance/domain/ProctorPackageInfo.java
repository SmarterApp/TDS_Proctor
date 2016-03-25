package TDS.Proctor.performance.domain;

public class ProctorPackageInfo {
    private byte[] _package;
    private String _testType;

    public byte[] getPackage() {
        return _package;
    }

    public void setPackage(byte[] _package) {
        this._package = _package;
    }

    public String getTestType() {
        return _testType;
    }

    public void setTestType(String _testType) {
        this._testType = _testType;
    }
}
