package com.github.igotyou.FactoryMod.utility;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskScheduler {
    private static final List<ScheduledTask> tasks = new ArrayList<>();
    private static final Queue<ScheduledTask> pendingAdd = new ConcurrentLinkedQueue<>();
    private static final Queue<Integer> pendingRemove = new ConcurrentLinkedQueue<>();
    private static int nextTaskId = 0;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick();
        });
    }

    private static void tick() {
        while (!pendingAdd.isEmpty()) {
            tasks.add(pendingAdd.poll());
        }
        
        while (!pendingRemove.isEmpty()) {
            int toRemove = pendingRemove.poll();
            for (ScheduledTask task : tasks) {
                if (task.id == toRemove) {
                    task.cancelled = true;
                }
            }
        }

        Iterator<ScheduledTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            if (task.cancelled) {
                iterator.remove();
                continue;
            }
            if (task.ticksRemaining <= 0) {
                try {
                    task.runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                iterator.remove();
            } else {
                task.ticksRemaining--;
            }
        }
    }

    public static int scheduleSyncDelayedTask(Runnable runnable, int delayTicks) {
        ScheduledTask task = new ScheduledTask(runnable, delayTicks, nextTaskId++);
        pendingAdd.add(task);
        return task.id;
    }
    
    public static void cancelTask(int id) {
        pendingRemove.add(id);
    }

    private static class ScheduledTask {
        Runnable runnable;
        int ticksRemaining;
        int id;
        boolean cancelled;

        ScheduledTask(Runnable runnable, int ticksRemaining, int id) {
            this.runnable = runnable;
            this.ticksRemaining = ticksRemaining;
            this.id = id;
            this.cancelled = false;
        }
    }
}