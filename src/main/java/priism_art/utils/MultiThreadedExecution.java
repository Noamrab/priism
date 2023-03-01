package priism_art.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class MultiThreadedExecution<T, U> {
	private int progressInterval;
	private int threadCount;
	private BiConsumer<T, U> action;
	private BiConsumer<T, Exception> exceptionHandler;
	private U reference;
	private Collection<T> items;
	private BiConsumer<T, Integer> reportAction;
	private long startTime;
	private String queueName;
	
	public static <T, U> MultiThreadedExecution<T, U> create(Collection<T> inputItems, U reference) {
		return new MultiThreadedExecution<>(inputItems, reference);
	}
	
	public static <T> MultiThreadedExecution<T, Void> create(Collection<T> inputItems) {
		return new MultiThreadedExecution<T, Void>(inputItems, null);
	}
	public static String calculateRate(long total, long startTime) {
		long diff = System.currentTimeMillis() - startTime;
		double rate = total / ((double) diff / 1000);
		return String.format("%,.1f/s", rate);
	}
	private MultiThreadedExecution(Collection<T> items, U refrence) {
		this.items = items;
		this.reference = refrence;
		this.queueName = "Queue";
		this.reportAction = (i,l) -> {
			String rate = calculateRate(items.size() - l, startTime);
			System.out.printf("%s size: %,d Rate: %s\n", getQueueName(), l, rate);
		};
		this.threadCount = 3;
		this.progressInterval = 150;
		
		this.exceptionHandler = (i,e) -> {e.printStackTrace();};
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void start() {
		if (items == null || items.isEmpty()) {
			Reportable.updateStatusFormat("%s is empty, terminating. No threads will be created", getQueueName());
			return;
		}
		
		// Register start time 
		startTime = System.currentTimeMillis();
        // Init Queue
        Queue<T> queue = new LinkedList<T>(items);
        // Init Threads
        List<Thread> threads = new LinkedList<>();
        for (int i = 0; i < getThreadCount(); i++) {
            Thread t = new Thread(() -> {
                while (!Thread.interrupted()) {
                    T item;
                    int left;
                    synchronized (queue) {
                        item = queue.poll();
                        left = queue.size();
                    }
                    if (left % progressInterval == 0 && left != 0 && reportAction != null) {
                    	reportAction.accept(item, left);
                    }
                    if (item == null) {
                        return;
                    }
                    try {
                        action.accept(item, reference);
                    } catch (Exception e) {
                    	if (exceptionHandler != null) {
							exceptionHandler.accept(item, e);
						} else {
							e.printStackTrace();
						}
                    }
                }
            });
            threads.add(t);
            t.start();
        }
        System.out.println("Waiting for everyone (" + getQueueName() +")");
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("done " +  getQueueName());

    }



	/**
	 * @return the progressInterval
	 */
	public int getProgressInterval() {
		return progressInterval;
	}

	/**
	 * @param progressInterval the progressInterval to set
	 */
	public MultiThreadedExecution<T, U> setProgressInterval(int progressInterval) {
		this.progressInterval = progressInterval;
		return this;
	}

	/**
	 * @return the threadCount
	 */
	public int getThreadCount() {
		return Math.min(threadCount, items.size());
	}

	/**
	 * @param threadCount the threadCount to set
	 */
	public MultiThreadedExecution<T, U> setThreadCount(int threadCount) {
		this.threadCount = threadCount;
		return this;
	}

	/**
	 * @return the action
	 */
	public BiConsumer<T, U> getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public MultiThreadedExecution<T, U> setAction(BiConsumer<T, U> action) {
		this.action = action;
		return this;
	}
	
	public MultiThreadedExecution<T, U> setAction(Consumer<T> action) {
		this.action = (a,b) -> action.accept(a);
		return this;
	}

	/**
	 * @return the reference
	 */
	public U getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public MultiThreadedExecution<T, U> setReference(U reference) {
		this.reference = reference;
		return this;
	}

	/**
	 * @return the items
	 */
	public Collection<T> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public MultiThreadedExecution<T, U> setItems(Collection<T> items) {
		this.items = items;
		return this;
	}

	public BiConsumer<T, Integer> getReportAction() {
		return reportAction;
	}

	public MultiThreadedExecution<T,U> setReportAction(BiConsumer<T, Integer> reportAction) {
		this.reportAction = reportAction;
		return this;
	}
	public MultiThreadedExecution<T,U> setReportAction(Consumer<Integer> reportAction) {
		this.reportAction = (a,b) -> reportAction.accept(b);
		return this;
	}

	public BiConsumer<T, Exception> getExceptionHandler() {
		return exceptionHandler;
	}

	public MultiThreadedExecution<T,U> setExceptionHandler(BiConsumer<T, Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}
	public MultiThreadedExecution<T,U> setExceptionHandler(Consumer<Exception> exceptionHandler) {
		this.exceptionHandler = (a,b) -> exceptionHandler.accept(b);
		return this;
	}

	public String getQueueName() {
		return queueName;
	}

	public MultiThreadedExecution<T,U> setQueueName(String queueName) {
		this.queueName = queueName;
		return this;
	}
	
	public static <T> void executeAndWait(List<T> items, Function<T, Runnable> converter, ExecutorService service) {
		executeAndWait(items.stream().map(converter::apply).collect(Collectors.toList()), service);
	}
	
	public static void executeAndWait(List<Runnable> items, ExecutorService service) {
		List<Future<?>> futures = items.stream().map(service::submit).collect(Collectors.toList());
		waitForFuture(futures);
	}
	
	public static void waitForFuture(List<Future<?>> futures) {
		try {
			for (Future<?> future : futures) {
				future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
