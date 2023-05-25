public class Instructions {
    private char IName;           //指令类型
    private double IRuntime;   //指令运行时间
    private double IRemainTime;  //指令剩余运行时间

    public char getIName() {
        return IName;
    }

    public void setIName(char IName) {
        this.IName = IName;
    }

    public void setIRuntime(double IRuntime) {
        this.IRuntime = IRuntime;
    }

    public double getIRuntime() {
        return IRuntime;
    }

    public void setIRemainTime(double IRemainTime) {
        this.IRemainTime = IRemainTime;
    }

    public double getIRemainTime() {
        return IRemainTime;
    }
    public void subIRemainTime(){
        //剩余时间减1
        this.setIRemainTime(this.getIRemainTime()-1);

    }
}
