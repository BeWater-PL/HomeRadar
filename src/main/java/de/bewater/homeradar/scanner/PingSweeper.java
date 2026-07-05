package de.bewater.homeradar.scanner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

@Component
public class PingSweeper {

	/** Timeout pro Host in Millisekunden. */
	private static final int TIMEOUT_MS = 500;

	/** Anzahl paralleler Threads fuer den Sweep. */
	private static final int THREAD_POOL_SIZE = 50;

	/**
	 * Durchsucht ein /24-Subnetz (Hosts .1 bis .254) und liefert alle
	 * erreichbaren IP-Adressen zurueck. Die Hosts werden parallel gepingt.
	 *
	 * @param subnet die ersten drei Oktette, z.B. "192.168.178"
	 * @return Liste der erreichbaren IP-Adressen als Strings
	 */
	public List<String> sweep(String subnet) {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		try {
			List<Future<String>> futures = new ArrayList<>();
			for (int host = 1; host <= 254; host++) {
				String ip = subnet + "." + host;
				Callable<String> task = () -> {
					InetAddress address = InetAddress.getByName(ip);
					return address.isReachable(TIMEOUT_MS) ? ip : null;
				};
				futures.add(executor.submit(task));
			}

			List<String> reachable = new ArrayList<>();
			for (Future<String> future : futures) {
				try {
					String ip = future.get();
					if (ip != null) {
						reachable.add(ip);
					}
				} catch (ExecutionException e) {
					// Host nicht erreichbar oder Netzwerkfehler -> ueberspringen
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			return reachable;
		} finally {
			executor.shutdown();
		}
	}
}
