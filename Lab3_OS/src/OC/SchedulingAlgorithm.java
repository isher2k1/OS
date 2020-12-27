package OC;// Run() is called from OC.Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

public class SchedulingAlgorithm {

    private static final String REGISTERED = "registered";
    private static final String COMPLETED = "completed";
    private static final String BLOCKED = "I/O blocked";
    private static final String PAUSED = "paused";
    private static final String RESUMED = "resumed";
    private static final String RESULTS_FILE_ROOT = "res/Summary-Processes";

    public static Results Run(int runtime, Vector processVector, Results result) {
        int currentProcessNum = 0;
        int compTime = 0;
        result.schedulingType = "Batch (Nonpreemptive)";
        result.schedulingName = "Shortest remaining time first";
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(RESULTS_FILE_ROOT));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (compTime < runtime) {
            compTime++;
            int newCurrentProccesNum = getMinLeftTimeProcessIndex(processVector);
            if (newCurrentProccesNum == -1) break;
            sProcess prevProcess = (sProcess) processVector.get(currentProcessNum);
            sProcess newProcess = (sProcess) processVector.get(newCurrentProccesNum);
            if (newCurrentProccesNum != currentProcessNum) {
                if (prevProcess.ionext > 0 && prevProcess.cpudone != prevProcess.cputime)
                    printProcessStat(prevProcess, currentProcessNum, PAUSED, out);
                if (newProcess.ionext > 0)
                    printProcessStat(newProcess, newCurrentProccesNum, RESUMED, out);
            }
            if (newProcess.ionext == 0) printProcessStat(newProcess, newCurrentProccesNum, REGISTERED, out);
            currentProcessNum = getMinLeftTimeProcessIndex(processVector);

            sProcess process = (sProcess) processVector.get(currentProcessNum);
            process.ionext++;
            process.cpudone++;

            if (process.cpudone == process.cputime) {
                printProcessStat(process, currentProcessNum, COMPLETED, out);
            }

            if (process.ionext == process.ioblocking) {
                process.numblocked++;
                process.ionext = 0;
                printProcessStat(process, currentProcessNum, BLOCKED, out);
            }
        }
        result.compuTime = compTime;
        return result;
    }

    private static int getMinLeftTimeProcessIndex(Vector<sProcess> processes) {
        int minLeftTime = Integer.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < processes.size(); i++) {
            sProcess process = processes.get(i);
            if (process.cpudone == process.cputime)
                continue;
            int leftTime = process.ioblocking - process.ionext;
            if (leftTime < minLeftTime) {
                minLeftTime = leftTime;
                minIndex = i;
            } else if (leftTime == minLeftTime) {
                minIndex = process.priority > processes.get(minIndex).priority ? i : minIndex;
            }

        }
        return minIndex;
    }

    private static void printProcessStat(sProcess process, int ProcessNum, String state, PrintStream out) {
        out.println("OC.Process: " + ProcessNum + " " + state + "... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + " " + process.priority + ")");
    }
}
