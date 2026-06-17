package uy.edu.um.doors;

import uy.edu.um.doors.model.*;
import uy.edu.um.tad.heap.EmptyHeapException;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import java.io.BufferedReader;
import java.io.FileReader;


import uy.edu.um.tad.list.MyList;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;
import uy.edu.um.tad.stack.EmptyStackException;
import uy.edu.um.doors.model.FinishState;



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
    private final MyHeap<DoorProcess> pendingProcesses = new MyHeapImpl<>(false);
    private DoorProcess runningProcess;
    private final MyHash<Integer, DoorUser> users = new MyHashImpl<>();
    private final MyStack<DoorProcess> finishedProcesses = new MyStackImpl<>();
    private final MyHash<Integer, DoorProcess> processesByPid = new MyHashImpl<>();

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        loadUsers(usersCsvPath);
        System.out.println("Usuarios cargados: " + users.size());
        loadProcesses(processCsvPath);
        System.out.println("Procesos cargados: " + newProcesses.size());
    }


    @Override
    public void prepareProcesses() {
        int preparedProcesses = 0;
        while (!newProcesses.isEmpty()) {
            DoorProcess process = dequeueNewProcess();
            process.calculatePriority();
            process.setState(ProcessState.PENDING);
            pendingProcesses.insert(process);
            appendLog(formatNewPendingProcessLog(process));
            preparedProcesses++;
        }
        System.out.println("Procesos preparados: " + preparedProcesses);
    }

    @Override
    public void executeNextProcess() {
        if (runningProcess != null) {
            System.out.println("Ya hay un proceso en ejecucion (PID=" + runningProcess.getPid() + ").");
            return;
        }
        if (pendingProcesses.isEmpty()) {
            System.out.println("No hay procesos pendientes para ejecutar.");
            return;
        }
        try {
            runningProcess = pendingProcesses.remove();
        } catch (EmptyHeapException e) {
            return;
        }
        runningProcess.setState(ProcessState.RUNNING);
        appendLog(formatExecutingProcessLog(runningProcess));
    }


    @Override
    public void finishProcessOk() {
        finishRunningProcess(FinishState.OK, null);
    }

    @Override
    public void finishProcessError() {
        finishRunningProcess(FinishState.ERROR, null);
    }

    @Override
    public void terminateProcess(int uid) {
        DoorUser terminatedBy = users.get(uid);
        if (terminatedBy == null) {
            System.out.println("No existe el usuario con UID:" + uid);
            return;
        }
        finishRunningProcess(FinishState.TERMINATED, terminatedBy);
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

    private void loadUsers(String usersCsvPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(usersCsvPath))) {
            reader.readLine(); // saltea el encabezado uid;alias;type
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(";");
                    int uid = Integer.parseInt(parts[0].trim());
                    String alias = parts[1].trim();
                    UserType type = UserType.valueOf(parts[2].trim());
                    users.put(uid, new DoorUser(uid, alias, type));
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("ERROR leyendo usuarios: " + e.getMessage());
        }
    }

    private void loadProcesses(String processCsvPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(processCsvPath))) {
            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(";");
                    int pid = Integer.parseInt(parts[0].trim());
                    int uid = Integer.parseInt(parts[1].trim());
                    String name = parts[2].trim();

                    DoorUser user = users.get(uid);

                    MyList<ProcessEvent> events = parseEvents(parts[3]);


                    DoorProcess process = new DoorProcess(pid, name, user, events);
                    newProcesses.enqueue(process);
                    processesByPid.put(pid, process);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("ERROR leyendo procesos: " + e.getMessage());
        }
    }
    private MyList<ProcessEvent> parseEvents(String eventsBlock) {
        MyList<ProcessEvent> events = new MyLinkedListImpl<>();
        String content = eventsBlock.trim();

        // 1 Sacar las llaves { } de los bordes
        content = content.substring(1, content.length() - 1);

        // 2 Cortar por '#' para separar los eventos
        String[] rawEvents = content.split("#");
        for (int i = 0; i < rawEvents.length; i++) {
            String rawEvent = rawEvents[i].trim();
            if (rawEvent.isEmpty()) {
                continue;
            }

            // 3
            int colonIndex = rawEvent.indexOf(":");
            String typeText = rawEvent.substring(0, colonIndex).trim();
            String instructionsText = rawEvent.substring(colonIndex + 1).trim();

            EventType type = EventType.valueOf(typeText);
            // 4
            instructionsText = instructionsText.substring(1, instructionsText.length() - 1);
            MyList<String> instructions = new MyLinkedListImpl<>();
            String[] rawInstructions = instructionsText.split(",");
            for (int j = 0; j < rawInstructions.length; j++) {
                String instruction = rawInstructions[j].trim();
                if (!instruction.isEmpty()) {
                    instructions.add(instruction);
                }
            }

            events.add(new ProcessEvent(type, instructions));
        }

        return events;
    }

    private DoorProcess dequeueNewProcess() {
        try {
            return newProcesses.dequeue();
        } catch (EmptyQueueException e) {
            throw new IllegalStateException("No hay procesos nuevos para preparar", e);
        }
    }

    private void finishRunningProcess(FinishState finishState, DoorUser terminatedBy) {
        if (runningProcess == null) {
            System.out.println("No hay ningun proceso en ejecucion.");
            return;
        }
        DoorProcess process = runningProcess;
        process.finish(finishState, terminatedBy);
        appendLog(formatEndingProcessLog(process));

        if (finishedProcesses.size() == ProcessManager.MAX_FINISHED_PROCESS_ON_RAM) {
            appendLog(formatStackOverflowLog());
            while (!finishedProcesses.isEmpty()) {
                DoorProcess removed;
                try {
                    removed = finishedProcesses.pop();
                } catch (EmptyStackException e) {
                    break;
                }
                appendLog(formatFinishedInStackLog(removed));
                processesByPid.remove(removed.getPid());
            }
        }

        finishedProcesses.push(process);
        runningProcess = null;
    }


    private String formatNewPendingProcessLog(DoorProcess process) {
        return "[" + LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT) + "]: "
                + "NEW PENDING PROCESS: PID=" + process.getPid()
                + " | " + process.getName()
                + " | USER:" + process.getUser().getAlias()
                + " UID:" + process.getUser().getUid()
                + " | P=" + process.getPriority();
    }

    private String formatExecutingProcessLog(DoorProcess process) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT)).append("]: ");
        sb.append("EXECUTING PROCESS: PID=").append(process.getPid());
        sb.append(" | USER:").append(process.getUser().getAlias());
        sb.append(" UID:").append(process.getUser().getUid());
        MyList<ProcessEvent> events = process.getEvents();
        for (int i = 0; i < events.size(); i++) {
            ProcessEvent event = events.get(i);
            sb.append(System.lineSeparator());
            sb.append("EVENT: ").append(event.getType());
            sb.append(" | Instructions ").append(instructionsToString(event.getInstructions()));
        }
        return sb.toString();
    }

    private String formatEndingProcessLog(DoorProcess process) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT)).append("]: ");
        sb.append("ENDING PROCESS: PID=").append(process.getPid());
        sb.append(" | STATE: ").append(process.getFinishState());
        if (process.getFinishState() == FinishState.TERMINATED) {
            sb.append(" by USER:").append(process.getTerminatedBy().getAlias());
            sb.append(" UID:").append(process.getTerminatedBy().getUid());
        }
        return sb.toString();
    }

    private String formatStackOverflowLog() {
        return "[" + LocalDateTime.now().format(LOG_TIMESTAMP_FORMAT) + "]: "
                + "Finished process stack overflow";
    }

    private String formatFinishedInStackLog(DoorProcess process) {
        return "PID=" + process.getPid() + " " + process.getName()
                + " | STATE: " + process.getFinishState()
                + " | USER:" + process.getUser().getAlias()
                + " UID:" + process.getUser().getUid();
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
    private String instructionsToString(MyList<String> instructions) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(instructions.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

}
