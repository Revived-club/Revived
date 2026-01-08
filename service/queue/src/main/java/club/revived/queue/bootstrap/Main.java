package club.revived.queue.bootstrap;

import club.revived.queue.QueueManager;

public final class Main {

    /**
     * <p>Application entry point that starts the queue manager.</p>
     *
     * Prints a startup message and constructs the {@code QueueManager}.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        System.out.println("Starting...");
        new QueueManager();
    }
}