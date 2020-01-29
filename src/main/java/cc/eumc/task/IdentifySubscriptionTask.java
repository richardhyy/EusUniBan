package cc.eumc.task;

import cc.eumc.controller.UniBanController;

public class IdentifySubscriptionTask implements Runnable {
    final UniBanController controller;
    public IdentifySubscriptionTask(UniBanController instance) {
        this.controller = instance;
    }

    @Override
    public void run() {
        // TODO IdentifySubscriptionTask
    }
}
