package priism_art.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MultiExec<T> {
	private Consumer<T> action;
	private BiConsumer<T, Exception> exceptionHandler;
	private BiConsumer<T, Integer> reportAction;
	private Collection<T> items;
	private String queueName;
	private int progInterval;
	private int tCount;
	private long startTime;

	private MultiExec(Collection<T> items) {
		this.items = items;
		this.queueName = "Queue";
		this.reportAction = (i, l) -> {
			String rate = calculateRate(items.size() - l, startTime);
			System.out.printf("%s size: %,d Rate: %s\n", getQueueName(), l, rate);
		};
		this.tCount = 3;
		this.progInterval = 150;

		this.exceptionHandler = (i, e) -> {
			e.printStackTrace();
		};
	}

	private int getThreadCount() {
		return Math.min(tCount, items.size());
	}

	public String getQueueName() {
		return queueName;
	}

	public static <T> MultiExec<T> create(Collection<T> inputItems) {
		return new MultiExec<T>(inputItems);
	}

	public static String calculateRate(long total, long startTime) {
		long diff = System.currentTimeMillis() - startTime;
		double rate = total / ((double) diff / 1000);
		return String.format("%,.1f/s", rate);
	}

	/**
	 * @param progressInterval the progressInterval to set
	 */
	public MultiExec<T> setProgressInterval(int progressInterval) {
		this.progInterval = progressInterval;
		return this;
	}

	/**
	 * @param threadCount the threadCount to set
	 */
	public MultiExec<T> setThreadCount(int threadCount) {
		this.tCount = threadCount;
		return this;
	}

	/**
	 * @param action the action to set
	 */
	public MultiExec<T> setAction(Consumer<T> action) {
		this.action = action;
		return this;
	}

	/**
	 * @param items the items to set
	 */
	public MultiExec<T> setItems(Collection<T> items) {
		this.items = items;
		return this;
	}

	public MultiExec<T> setReportAction(BiConsumer<T, Integer> reportAction) {
		this.reportAction = reportAction;
		return this;
	}

	public MultiExec<T> setReportAction(Consumer<Integer> reportAction) {
		this.reportAction = (a, b) -> reportAction.accept(b);
		return this;
	}

	public MultiExec<T> setExceptionHandler(BiConsumer<T, Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}

	public MultiExec<T> setQueueName(String queueName) {
		this.queueName = queueName;
		return this;
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
					if (left % progInterval == 0 && left != 0 && reportAction != null) {
						reportAction.accept(item, left);
					}
					if (item == null) {
						return;
					}
					try {
						action.accept(item);
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
		System.out.println("Waiting for everyone (" + getQueueName() + ")");
		threads.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("done " + getQueueName());

	}

}
