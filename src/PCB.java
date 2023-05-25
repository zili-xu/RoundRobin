import java.util.ArrayDeque;
import java.util.Queue;

public class PCB {
    private String pName;//进程名称
//    private List pInstructions = new ArrayList<Instructions>();//进程中的指令列表
    private Queue<Instructions> pInstructions = new ArrayDeque<>(); // 进程中的指令列表
    private int CurrentInstruction;        //当前运行指令索引

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public int getCurrentInstruction() {
        return CurrentInstruction;
    }

    public void setCurrentInstruction(int currentInstruction) {
        CurrentInstruction = currentInstruction;
    }

    public Queue<Instructions> getpInstructions() {
        return pInstructions;
    }

    public void setpInstructions(Queue<Instructions> pInstructions) {
        this.pInstructions = pInstructions;
    }
}
