package uy.edu.um.doors;

import uy.edu.um.doors.model.DoorProcess;
import uy.edu.um.doors.model.ProcessPriorityKey;
import uy.edu.um.doors.model.ProcessState;
import uy.edu.um.tad.binarytree.MySearchBinaryTree;
import uy.edu.um.tad.binarytree.MySearchBinaryTreeImpl;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessManagerImpl implements ProcessManager {

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MyQueue<DoorProcess> newProcesses = new MyQueueImpl<>();
    private final MySearchBinaryTree<ProcessPriorityKey, DoorProcess> pendingProcesses = new MySearchBinaryTreeImpl<>();
    private DoorProcess runningProcess;

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void prepareProcesses() {
        int preparedProcesses = 0;
        while (!newProcesses.isEmpty()) {
            DoorProcess process = dequeueNewProcess();
            process.calculatePriority();
            process.setState(ProcessState.PENDING);
            pendingProcesses.add(new ProcessPriorityKey(process.getPriority(), process.getPid()), process);
            appendLog(formatNewPendingProcessLog(process));
            preparedProcesses++;
        }
        System.out.println("Procesos preparados: " + preparedProcesses);
    }

    @Override
    public void executeNextProcess() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void finishProcessOk() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void finishProcessError() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void terminateProcess(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatus() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusVerbose() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByUser(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByProcess(int pid) {
        System.out.println("IMPLEMENTAR");
    }

    private DoorProcess dequeueNewProcess() {
        try {
            return newProcesses.dequeue();
        } catch (EmptyQueueException e) {
            throw new IllegalStateException("No hay procesos nuevos para preparar", e);
        }
    }

    private String formatNewPendingProcessLog(DoorProcess process) {
        return "[" + LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT) + "]: "
                + "NEW PENDING PROCESS: PID=" + process.getPid()
                + " | " + process.getName()
                + " | USER:" + process.getUser().getAlias()
                + " UID:" + process.getUser().getUid()
                + " | P=" + process.getPriority();
    }

    private void appendLog(String line) {
        Path logPath = Path.of("DOORS_PROCESS_LOG_" + LocalDate.now().format(LOG_DATE_FORMAT));
        try {
            Files.writeString(
                    logPath,
                    line + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir el log de procesos", e);
        }
    }
}
