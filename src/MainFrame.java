import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

import static java.lang.Double.parseDouble;
import static java.lang.Thread.sleep;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class MainFrame {
    private JButton bFIle, bStart, bStop;//三个按钮
    private JTextField tTime, tCur;
    private JTextArea tAll, tReady, tInput, tOutput, tWait;//队列文本域显示
    private File file;
    private JLabel lInfo;
    private Deque<PCB> allQue = new ArrayDeque<>();
    private Deque<PCB> readyQue = new ArrayDeque<>();
    private Deque<PCB> inQue = new ArrayDeque<>();
    private Deque<PCB> outQue = new ArrayDeque<>();
    private Deque<PCB> waitQue = new ArrayDeque<>();
    private volatile Thread blinker;
    private long count = 0;

    private boolean flag = true;

    public MainFrame() {
        //初始化界面
        init();
        //选择文件
        bFIle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile();
                readFile();
                saveLog("读取文件成功!\r\n________________________\r\n");
                showAll(allQue);
            }
        });

        // 开始调度
        bStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (flag) {
                    if (allQue.size()==0) {
                        JOptionPane.showMessageDialog(null, "请重新选择文件!", "提示", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    if (boolTTime()) { // 判断时间片
                        initQue();
//                        if (flag) {
//                            initQue();
//                        }
                        startRun();
                    }
                }else {
                    resume();
                }
            }
        });

        // 暂停调度
        bStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bStart.setText("继续调度");

                lInfo.setText("调度已停止/(ㄒoㄒ)/~~");
                saveLog("调度已停止\n");

                // 暂停
                suspend();
//                blinker = null;
            }
        });
    }

    private void resume() {
        synchronized (blinker) {
//            blinker.notify();
            blinker.resume();
        }
    }

    private void suspend() {
        synchronized (blinker) {
//            try {
//                blinker.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            blinker.suspend();
        }
    }

    private void saveLog(String str) {
        FileWriter fw = null;
        try {
            //1.File实例化
            File file = new File("log.txt");
            //2.流的实例化 FileWriter
            fw = new FileWriter(file, true);
            //3.写出的操作
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //4.流的关闭
            try {
                if(fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean boolTTime() {
        //判断时间片
        if (tTime.getText().equals("")) {
            //时间片大小
            JOptionPane.showMessageDialog(null, "请输入时间片大小(数字)!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return false;
        } else {
            try {
                //是否为数值
                parseDouble(tTime.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "时间片大小为数字!请重新输入!", "提示", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public void startRun() {
        Runnable runnable = new Runnable() {
            public void run() {
                saveLog("------正在进行调度-------");
                lInfo.setText("------正在进行调度-------");
//                while (blinker != null) {
                while (true) {
                    try {
                        int a = runReady();
                        if (a != -1) {
                            runIn();
                            runOut();
                            runWait();
                        }

//                        showAll(allQue);
//                        saveLog("后备就绪队列:\t" + tReady.getText() + "\r\n");

                        showReady(readyQue);
                        saveLog("就绪队列:\t" + tReady.getText() + "\r\n");

                        showIn(inQue);
                        saveLog("输入队列:\t" + tInput.getText() + "\r\n");

                        showOut(outQue);
                        saveLog("输出队列:\t" + tOutput.getText() + "\r\n");

                        showWait(waitQue);
                        saveLog("等待队列:\t" + tWait.getText() + "\r\n");

                        saveLog("_______________________________\n");
                        count++;
                        System.out.println(count);

                        if (a == 0) {
                            try {
                                sleep(Long.parseLong(tTime.getText()));
                            }catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if ((readyQue.size() == 0) && (inQue.size() == 0) && (outQue.size() == 0) && (waitQue.size() == 0)) {
                            break;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                if (blinker != null) {
//                    bStart.setText("开始调度");
//                    tCur.setText("");
//                    lInfo.setText("调度已完成(*^_^*)");
//                    saveLog("调度已完成");
//                } else {
//                    lInfo.setText("调度已停止/(ㄒoㄒ)/~~");
//                    saveLog("调度已停止");
//                }

                bStart.setText("开始调度");
                tCur.setText("");
                lInfo.setText("调度已完成(*^_^*)");
                saveLog("调度已完成");
            }


        };
        blinker = new Thread(runnable);
        blinker.start();
    }

    private void showAll(Deque<PCB> allQue) {
        tAll.setText("");
        for (PCB p : allQue) {
            tAll.setText(tAll.getText() + "\r\n" + p.getpName());
        }
    }

    private void showWait(Deque<PCB> waitQue) {
        tWait.setText("");
        for (PCB p : waitQue) {
            tWait.setText(tWait.getText() + "\r\n" + p.getpName());
        }
    }

    private void showOut(Deque<PCB> outQue) {
        tOutput.setText("");
        for (PCB p : outQue) {
            tOutput.setText(tOutput.getText() + "\r\n" + p.getpName());
        }
    }

    private void showIn(Deque<PCB> inQue) {
        tInput.setText("");
        for (PCB p : inQue) {
            tInput.setText(tInput.getText() + "\r\n" + p.getpName());
        }
    }

    private void showReady(Deque<PCB> que) {
        tReady.setText("");
        for (PCB p : que) {
            tReady.setText(tReady.getText() + "\r\n" + p.getpName());
        }
    }

    private void initQue() {
        //把后备就绪进程排成一个就绪队列,即readyQue
        //I，O，W三条指令实际上是不占有CPU的，执行这三条指令就应该将进程放入对应的等待队列（输入等待队列，输出等待队列 ，其他等待队列）。
        //先清空队列
        readyQue.clear();
        inQue.clear();
        outQue.clear();
        waitQue.clear();

        // 把后备就绪队列里的进程全部复制到就绪队列里
        // 同时清空后备就绪队列
        readyQue = allQue;
//        allQue = null;
        tAll.setText("");

        flag = false;
    }

    // 就绪队列为空   返回0
    // 执行C指令    返回1
    // 其他指令     返回-1
    private int runReady() throws InterruptedException {
        if (readyQue.size() == 0) {
            return 0;
        }

        // 取出队头进程
        PCB pcb = readyQue.poll();
        // 显示当前运行进程
        tCur.setText(pcb.getpName());
        // 取队头进程的当前指令
        Instructions instruction = pcb.getpInstructions().peek();
        if (instruction.getIName() == 'C') {
            showReady(readyQue); // 显示

            // 运行
            try {
                sleep(Long.parseLong(tTime.getText()));
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 时间片减一
            instruction.subIRemainTime();

            if (instruction.getIRemainTime() == 0) { // 时间片减为0
                // 移除队首指令
                pcb.getpInstructions().poll();
            }
            // 重新加到就绪队列
            readyQue.offer(pcb);

            // 当前进程
            tCur.setText("");

            return 1;
        }else if (instruction.getIName() == 'I') {
            // 加到输入队列
            inQue.offer(pcb);
        }else if (instruction.getIName() == 'O') {
            // 加到输出队列
            outQue.offer(pcb);
        }else if (instruction.getIName() == 'W') {
            // 加到其他队列
            waitQue.offer(pcb);
        }else if (instruction.getIName() == 'H') {
            // 该进程已完成

        }
        tCur.setText("");

        return -1;
    }

    private void runIn() {
        if (inQue.size() == 0) {
            return;
        }
        PCB pcb = inQue.peek();
        Instructions instruction = pcb.getpInstructions().peek();
        // 时间片减一
        instruction.subIRemainTime();
        if (instruction.getIRemainTime() == 0) {
            // 时间片减为0，移除该条指令，并从输入队列中移除该进程，再加入到就绪队列
            pcb.getpInstructions().poll();
            inQue.poll();
            readyQue.offer(pcb);
        }
    }

    private void runOut() {
        if (outQue.size() == 0) {
            return;
        }
        PCB pcb = outQue.peek();
        Instructions instruction = pcb.getpInstructions().peek();
        // 时间片减一
        instruction.subIRemainTime();
        if (instruction.getIRemainTime() == 0) {
            // 时间片减为0，移除该指令，并从输出队列中移除该进程，再加入到就绪队列
            pcb.getpInstructions().poll();
            outQue.poll();
            readyQue.offer(pcb);
        }
    }

    private void runWait() {
        if (waitQue.size() == 0) {
            return;
        }
        PCB pcb = waitQue.peek();
        Instructions instruction = pcb.getpInstructions().peek();
        // 时间片减一
        instruction.subIRemainTime();
        if (instruction.getIRemainTime() == 0) {
            // 时间片减为0，移除该指令，并从等待队列中移除该进程，再加入到就绪队列
            pcb.getpInstructions().poll();
            waitQue.poll();
            readyQue.offer(pcb);
        }
    }

    private void readFile() {
        //读取文件
        if (file != null) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                String str;
                allQue.clear();
                PCB pcb = null;
                while ((str = in.readLine()) != null) {
                    if (str.charAt(0) == 'P') {
                        //创建新的进程
                        pcb = new PCB();
                        pcb.setpName(str);
                    } else {
                        //创建新的指令集
                        Instructions instructions = new Instructions();
                        instructions.setIName(str.charAt(0));
                        instructions.setIRuntime(parseDouble(str.substring(1)));
                        instructions.setIRemainTime(instructions.getIRuntime());//刚开始剩余时间与运行时间一致
                        assert pcb != null;

                        // 该条指令加入指令列表
                        pcb.getpInstructions().add(instructions);

                        if (instructions.getIName() == 'H') {
                            //H代表当前进程结束,添加到后备就绪队列
                            allQue.add(pcb);
                        }

                    }
                }
            } catch (IOException e) {
                System.out.println("文件读取失败!");
            }
        }
    }

    private void chooseFile() {
        //选择文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
        JFileChooser jfc = new JFileChooser(".");//当前目录下
        jfc.setFileFilter(filter);
        jfc.setMultiSelectionEnabled(false);//不允许多选
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = jfc.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            //JFileChooser.APPROVE_OPTION是个整型常量，代表0。
            // 就是说当返回0的值我们才执行相关操作，否则什么也不做。
            file = jfc.getSelectedFile();
        }

    }

    private void init() {
        //界面初始化
        JFrame jFrame = new JFrame();
        jFrame.setTitle("时间片轮转调度");
        Container con = jFrame.getContentPane();
        con.setLayout(null);
        bFIle = new JButton("打开文件");
        bFIle.setBounds(50, 50, 150, 30);

        bStart = new JButton("开始调度");
        bStart.setBounds(220, 50, 150, 30);

        bStop = new JButton("暂停调度");
        bStop.setBounds(390, 50, 150, 30);

        JLabel ltime = new JLabel("时间片大小:");
        ltime.setBounds(560, 50, 80, 30);
        tTime = new JTextField();
        tTime.setBounds(650, 50, 150, 30);
        tTime.setText("500");

        JLabel lCur = new JLabel("当前运行进程:");
        lCur.setBounds(50, 100, 150, 30);
        tCur = new JTextField();
        tCur.setBounds(50, 130, 150, 30);

        lInfo = new JLabel("");//提示信息
        lInfo.setBounds(350, 130, 300, 30);
        lInfo.setForeground(Color.red);

        JLabel lall = new JLabel("后备就绪队列:");
        lall.setBounds(50, 200, 150, 30);
        tAll = new JTextArea(6, 4);
        tAll.setBounds(50, 230, 150, 250);

        JLabel lr = new JLabel("就绪队列:");
        lr.setBounds(220, 200, 150, 30);
        tReady = new JTextArea(6, 4);
        tReady.setBounds(220, 230, 150, 250);

        JLabel lin = new JLabel("输入等待队列:");
        lin.setBounds(390, 200, 150, 30);
        tInput = new JTextArea(6, 4);
        tInput.setBounds(390, 230, 150, 250);

        JLabel lout = new JLabel("输出等待队列:");
        lout.setBounds(560, 200, 150, 30);
        tOutput = new JTextArea(6, 4);
        tOutput.setBounds(560, 230, 150, 250);

        JLabel lw = new JLabel("其他等待队列:");
        lw.setBounds(730, 200, 150, 30);
        tWait = new JTextArea(6, 4);
        tWait.setBounds(730, 230, 150, 250);

        con.add(bFIle);
        con.add(bStart);
        con.add(bStop);
        con.add(ltime);
        con.add(tTime);
        con.add(lCur);
        con.add(tCur);
        con.add(lInfo);

        con.add(lall);
        con.add(tAll);
        con.add(lr);
        con.add(tReady);
        con.add(lin);
        con.add(tInput);
        con.add(lout);
        con.add(tOutput);
        con.add(lw);
        con.add(tWait);
        jFrame.setBounds(200, 200, 1000, 600);  //设置窗口的属性 窗口位置以及窗口的大小
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }


    public static void main(String[] args) {

        MainFrame mainFrame = new MainFrame();
    }

}
