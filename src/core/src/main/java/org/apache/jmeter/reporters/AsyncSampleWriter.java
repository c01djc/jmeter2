/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.reporters;

import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background writer that keeps sample-file I/O off sampler threads.
 * <p>
 * Enabled by default for JMeter2 via {@code jmeter.save.saveservice.async=true}.
 */
final class AsyncSampleWriter {

    private static final Logger log = LoggerFactory.getLogger(AsyncSampleWriter.class);

    private static final boolean ENABLED =
            JMeterUtils.getPropDefault("jmeter.save.saveservice.async", true); // $NON-NLS-1$

    private static final int QUEUE_SIZE =
            JMeterUtils.getPropDefault("jmeter.save.saveservice.async.queue.size", 10000); // $NON-NLS-1$

    private static final WriteItem POISON = new WriteItem(null, null, null, null);

    private static final Object LOCK = new Object();

    private static BlockingQueue<WriteItem> queue;
    private static Thread worker;
    private static volatile boolean running;

    private static LongAdder queueWaits = new LongAdder();
    private static LongAdder queueWaitNanos = new LongAdder();

    private AsyncSampleWriter() {
    }

    static boolean isEnabled() {
        return ENABLED;
    }

    static void start() {
        if (!ENABLED) {
            return;
        }
        synchronized (LOCK) {
            if (running) {
                return;
            }
            queue = new ArrayBlockingQueue<>(Math.max(100, QUEUE_SIZE));
            queueWaits = new LongAdder();
            queueWaitNanos = new LongAdder();
            worker = new Thread(AsyncSampleWriter::runWorker, "AsyncSampleWriter");
            worker.setDaemon(true);
            running = true;
            worker.start();
            log.info("Async sample writer started (queue size={})", QUEUE_SIZE);
        }
    }

    /**
     * Drain remaining samples, then stop the worker. Must be called before closing writers.
     */
    static void stopAndFlush() {
        if (!ENABLED) {
            return;
        }
        Thread toJoin;
        synchronized (LOCK) {
            if (!running) {
                return;
            }
            try {
                queue.put(POISON);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while signalling async writer shutdown");
            }
            toJoin = worker;
            running = false;
        }
        if (toJoin != null) {
            try {
                toJoin.join(TimeUnit.MINUTES.toMillis(2));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for async writer to finish");
            }
        }
        long waits = queueWaits.sum();
        if (waits > 0) {
            log.info("Async sample writer back-pressure: waits={}, waitTime={} ns",
                    waits, queueWaitNanos.sum());
        }
        synchronized (LOCK) {
            queue = null;
            worker = null;
        }
        log.info("Async sample writer stopped");
    }

    static void enqueue(SampleEvent event, PrintWriter out, SampleSaveConfiguration config) {
        WriteItem item = new WriteItem(event, out, config, null);
        BlockingQueue<WriteItem> q = queue;
        if (q == null) {
            // Writer not running (or already stopped) — fall back to sync write
            writeOne(item);
            return;
        }
        try {
            if (!q.offer(item)) {
                queueWaits.increment();
                long t0 = System.nanoTime();
                q.put(item);
                queueWaitNanos.add(System.nanoTime() - t0);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while queueing sample for async write; writing synchronously");
            writeOne(item);
        }
    }

    /**
     * Wait until previously queued items are written (does not close the writer).
     */
    static void flushPending() {
        BlockingQueue<WriteItem> q = queue;
        if (q == null) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        try {
            q.put(new WriteItem(null, null, null, latch));
            if (!latch.await(30, TimeUnit.SECONDS)) {
                log.warn("Timed out waiting for async sample writer flush");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while flushing async sample writer");
        }
    }

    private static void runWorker() {
        try {
            while (true) {
                WriteItem item = queue.take();
                if (item == POISON) {
                    // Drain anything still in the queue before exit
                    WriteItem leftover;
                    while ((leftover = queue.poll()) != null) {
                        if (leftover == POISON) {
                            continue;
                        }
                        if (leftover.flushLatch != null) {
                            leftover.flushLatch.countDown();
                        } else {
                            writeOne(leftover);
                        }
                    }
                    break;
                }
                if (item.flushLatch != null) {
                    item.flushLatch.countDown();
                    continue;
                }
                writeOne(item);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Async sample writer interrupted");
        } catch (RuntimeException e) {
            log.error("Async sample writer failed", e);
        }
    }

    private static void writeOne(WriteItem item) {
        if (item == null || item.event == null || item.out == null || item.config == null) {
            return;
        }
        try {
            if (item.config.saveAsXml()) {
                SaveService.saveSampleResult(item.event, item.out);
            } else {
                CSVSaveService.saveSampleResult(item.event, item.out);
            }
        } catch (Exception err) {
            log.error("Error trying to record a sample (async)", err);
        }
    }

    private static final class WriteItem {
        final SampleEvent event;
        final PrintWriter out;
        final SampleSaveConfiguration config;
        final CountDownLatch flushLatch;

        WriteItem(SampleEvent event, PrintWriter out, SampleSaveConfiguration config, CountDownLatch flushLatch) {
            this.event = event;
            this.out = out;
            this.config = config;
            this.flushLatch = flushLatch;
        }
    }
}
