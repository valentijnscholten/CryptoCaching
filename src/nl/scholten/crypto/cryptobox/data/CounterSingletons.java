package nl.scholten.crypto.cryptobox.data;

import java.util.concurrent.atomic.AtomicLong;

public enum CounterSingletons {
	TRIES, MAXSCORE;
	
	public volatile AtomicLong counter = new AtomicLong();

	public static void reset() {
		System.out.println("Resetting counters");
		TRIES.counter.set(0);
		MAXSCORE.counter.set(0);
	}
	
}
