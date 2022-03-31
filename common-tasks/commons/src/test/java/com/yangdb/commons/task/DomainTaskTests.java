package com.yangdb.commons.task;

import com.yangdb.commons.function.consumer.ThrowingConsumer;
import com.yangdb.commons.task.domain.DomainTask;
import com.yangdb.commons.task.domain.ExecutorServiceExecution;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DomainTaskTests {
    @Test
    public void start_await_null_result() throws Exception {
        Task<?, ?> task = new DomainTask.Builder<>().type("task").result(null).build();

        task.start().await();
        Assert.assertEquals(Task.State.success, task.getState());
        Assert.assertNull(task.getResult());
    }

    @Test
    public void start_await_result() throws Exception {
        Task<?, ?> task = new DomainTask.Builder<>().type("task").result("ok").build();
        task.start().await();
        Assert.assertEquals(Task.State.success, task.getState());
        Assert.assertEquals("ok", task.getResult());
    }

    @Test
    public void start_await_exception() throws Exception {
        Exception notOk = new Exception("not ok");
        Task<?, ?> task = new DomainTask.Builder<>().type("task").executor(executor -> { executor.thenFail(notOk); }).build();
        task.start().await();
        Assert.assertEquals(Task.State.failed, task.getState());
        Assert.assertEquals(notOk, task.getException());
        try {
            task.getResult();
        } catch (Exception ex) {
            Assert.assertEquals(notOk, ex);
        }
    }

    @Test
    public void start_pause() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signal3 = new CompletableFuture<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Task<?, ?> task = new DomainTask.Builder<>().type("task")
                    .executor(execution -> {
                        Assert.assertEquals(Task.State.running, execution.getTask().getState());
                        signal1.complete(null);
                        signal2.join();
                        Assert.assertEquals(Task.State.pausing, execution.getTask().getState());
                    }).build();

            executorService.submit(() -> {
                executorService.submit(() -> {
                    task.start();
                    signal3.complete(null);
                });
                signal1.join();
                task.pause();
                signal2.complete(null);
            });

            signal3.join();
            Assert.assertEquals(Task.State.paused, task.getState());
            Assert.assertNull(null, task.getResult());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void start_pause_resume() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signal3 = new CompletableFuture<>();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        final AtomicBoolean wasPaused = new AtomicBoolean(false);

        try {
            Task<?, ?> task = new DomainTask.Builder<>().type("task")
            .executor(execution -> {
                Assert.assertEquals(Task.State.running, execution.getTask().getState());
                if (!wasPaused.get()) {
                    signal1.complete(null);
                    signal2.join();
                    Assert.assertEquals(Task.State.pausing, execution.getTask().getState());
                } else {
                    execution.thenComplete(() -> "ok");
                }
            }).build();

            executorService.submit(() -> {
                executorService.submit(() -> {
                    task.start();
                    signal3.complete(null);
                });
                signal1.join();
                task.pause();
                signal2.complete(null);
            });

            signal3.join();
            Assert.assertEquals(Task.State.paused, task.getState());
            Assert.assertNull(null, task.getResult());

            wasPaused.set(true);
            task.resume().await();
            Assert.assertEquals(Task.State.success, task.getState());
            Assert.assertEquals("ok", task.getResult());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void start_cancel() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signal3 = new CompletableFuture<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Task<?, ?> task = new DomainTask.Builder<>().type("task")
            .executor(execution -> {
                Assert.assertEquals(Task.State.running, execution.getTask().getState());
                signal1.complete(null);
                signal2.join();
                Assert.assertEquals(Task.State.canceling, execution.getTask().getState());
            }).build();

            executorService.submit(() -> {
                executorService.submit(() -> {
                    task.start();
                    signal3.complete(null);
                });
                signal1.join();
                task.cancel();
                signal2.complete(null);
            });

            signal3.join();
            Assert.assertEquals(Task.State.canceled, task.getState());
            Assert.assertNull(null, task.getResult());
            Assert.assertEquals(0, task.getProgress());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void start_cancel_progress() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signal3 = new CompletableFuture<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Task<?, ?> task = new DomainTask.Builder<>().type("task")
            .executor(execution -> {
                Assert.assertEquals(Task.State.running, execution.getTask().getState());
                signal1.complete(null);
                signal2.join();
                Assert.assertEquals(Task.State.canceling, execution.getTask().getState());
                execution.progress(50);
            }).build();

            executorService.submit(() -> {
                executorService.submit(() -> {
                    task.start();
                    signal3.complete(null);
                });
                signal1.join();
                task.cancel();
                signal2.complete(null);
            });

            signal3.join();
            Assert.assertEquals(Task.State.canceled, task.getState());
            Assert.assertNull(null, task.getResult());
            Assert.assertEquals(50, task.getProgress());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void two_phase_start_await_result() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Task<?, ?> task = new DomainTask.Builder<>().type("task")
                .executor(execution -> {
                        executorService.submit(() -> {
                            try {
                                execution.thenComplete(() -> "ok");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    })
                .build();
        task.start().await();

        Assert.assertEquals(Task.State.success, task.getState());
        Assert.assertEquals("ok", task.getResult());
        executorService.shutdownNow();
    }

    @Test
    public void two_phase_start_pause_resume() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signalPaused = new CompletableFuture<>();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicInteger executor1Counter = new AtomicInteger(0);
        AtomicInteger executor2Counter = new AtomicInteger(0);

        Task<?, ?> task = new DomainTask.Builder<>().type("task")
                .executor(execution -> {
                        executor1Counter.incrementAndGet();
                        executorService.submit(() -> {
                            signal1.complete(null);
                            signal2.join();

                            try {
                                execution.thenComplete(() -> {
                                    executor2Counter.incrementAndGet();
                                    return "ok";
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }).build();

        task.onStateChange(task1 -> {
            if (task1.getState() == Task.State.paused) {
                signalPaused.complete(null);
            }
        });

        task.start();
        signal1.join();
        task.pause();
        signal2.complete(null);

        signalPaused.join();
        Assert.assertEquals(Task.State.paused, task.getState());
        Assert.assertEquals(1, executor1Counter.get());
        Assert.assertEquals(0, executor2Counter.get());

        task.resume().await();

        Assert.assertEquals(Task.State.success, task.getState());
        Assert.assertEquals(1, executor1Counter.get());
        Assert.assertEquals(1, executor2Counter.get());
    }

    @Test
    public void executor_service_two_phase_start_pause_resume() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signalPaused = new CompletableFuture<>();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicInteger executor1Counter = new AtomicInteger(0);
        AtomicInteger executor2Counter = new AtomicInteger(0);

        Task<?, ?> task = new DomainTask.Builder<>().type("task")
                .execution(execution -> new ExecutorServiceExecution<>(execution, Executors.newFixedThreadPool(1)))
                .executor(execution -> {
                    executor1Counter.incrementAndGet();
                    executorService.submit(() -> {
                        signal1.complete(null);
                        signal2.join();

                        try {
                            execution.thenComplete(() -> {
                                executor2Counter.incrementAndGet();
                                return "ok";
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }).build();

        task.onStateChange(task1 -> {
            if (task1.getState() == Task.State.paused) {
                signalPaused.complete(null);
            }
        });

        task.start();
        signal1.join();
        task.pause();
        signal2.complete(null);

        signalPaused.join();
        Assert.assertEquals(Task.State.paused, task.getState());
        Assert.assertEquals(1, executor1Counter.get());
        Assert.assertEquals(0, executor2Counter.get());

        task.resume().await();

        Assert.assertEquals(Task.State.success, task.getState());
        Assert.assertEquals(1, executor1Counter.get());
        Assert.assertEquals(1, executor2Counter.get());

        executorService.shutdownNow();
    }

    @Test
    public void executor_service_two_phase_start_pause_resume_async_execution() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signalPaused = new CompletableFuture<>();

        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        Task<?, ?> task = new DomainTask.Builder<>().type("task")
                .execution(execution -> new ExecutorServiceExecution<>(execution, Executors.newFixedThreadPool(1)))
                .executor(execution -> {
                    CompletableFuture.completedFuture(null)
                            .thenCompose(avoid -> execution.thenAsync(() -> {
                                signal1.complete(null);
                                //should pause here
                                signal2.join();
                                counter1.getAndIncrement();
                            }))
                            .thenAccept(avoid -> counter2.getAndIncrement())
                            .thenCompose(avoid -> execution.thenCompleteAsync(() -> "ok"));
                }).build();

        task.onStateChange(task1 -> {
            if (task1.getState() == Task.State.paused) {
                signalPaused.complete(null);
            }
        });

        task.start();

        signal1.join();
        task.pause();
        signal2.complete(null);
        signalPaused.join();

        Assert.assertEquals(Task.State.paused, task.getState());
        Assert.assertEquals(1, counter1.get());
        Assert.assertEquals(0, counter2.get());

        task.resume().await();

        Assert.assertEquals(Task.State.success, task.getState());
        Assert.assertEquals("ok", task.getResult());
        Assert.assertEquals(1, counter1.get());
        Assert.assertEquals(1, counter2.get());
    }

    @Test
    public void executor_service_two_phase_start_pause_resume_fail_execution() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signalPaused = new CompletableFuture<>();

        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        Exception exception = new Exception("failed");

        Task<?, ?> task = new DomainTask.Builder<>().type("task")
                .execution(execution -> new ExecutorServiceExecution<>(execution, Executors.newFixedThreadPool(1)))
                .executor(execution -> {
                    CompletableFuture.completedFuture(null)
                            .thenCompose(avoid -> execution.thenAsync(() -> {
                                signal1.complete(null);
                                //should pause here
                                signal2.join();
                                counter1.getAndIncrement();
                            }))
                            .thenAccept(avoid -> counter2.getAndIncrement())
                            .thenCompose(avoid -> execution.thenFailAsync(exception));
                }).build();

        task.onStateChange(task1 -> {
            if (task1.getState() == Task.State.paused) {
                signalPaused.complete(null);
            }
        });

        task.start();

        signal1.join();
        task.pause();
        signal2.complete(null);
        signalPaused.join();

        Assert.assertEquals(Task.State.paused, task.getState());
        Assert.assertEquals(1, counter1.get());
        Assert.assertEquals(0, counter2.get());

        task.resume().await();

        Assert.assertEquals(Task.State.failed, task.getState());
        Assert.assertEquals(exception, task.getException());
        Assert.assertEquals(1, counter1.get());
        Assert.assertEquals(1, counter2.get());
    }

    @Test
    public void executor_service_two_phase_start_cancel_async_execution() throws Exception {
        CompletableFuture<Void> signal1 = new CompletableFuture<>();
        CompletableFuture<Void> signal2 = new CompletableFuture<>();
        CompletableFuture<Void> signalCanceled = new CompletableFuture<>();

        AtomicInteger counter1 = new AtomicInteger(0);

        Task<?, ?> task = new DomainTask.Builder<>().type("task")
                .execution(execution -> new ExecutorServiceExecution<>(execution, Executors.newFixedThreadPool(1)))
                .executor(execution -> {
                    CompletableFuture.completedFuture(null)
                            .thenCompose(avoid -> execution.thenAsync(() -> {
                                signal1.complete(null);
                                //should cancel here
                                signal2.join();
                            }))
                            .thenAccept(avoid -> counter1.incrementAndGet())
                            .thenCompose(avoid -> execution.thenCompleteAsync(() -> "ok"))
                            .exceptionally(ex -> {
                                Assert.assertTrue(ex instanceof CompletionException);
                                return null;
                            });
                }).build();

        task.onStateChange(task1 -> {
            if (task1.getState() == Task.State.canceled) {
                signalCanceled.complete(null);
            }
        });

        task.start();

        signal1.join();
        task.cancel();
        signal2.complete(null);
        signalCanceled.join();

        Assert.assertEquals(Task.State.canceled, task.getState());
        Assert.assertEquals(0, counter1.get());
    }

    @Test
    public void parallel_task_start() {
        AtomicInteger executorCounter = new AtomicInteger(0);
        AtomicInteger failedCounter = new AtomicInteger(0);

        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);


        Task<String, Void> task = new DomainTask.Builder<String, Void>().type("task")
                .executor(execution -> {
                    executorCounter.incrementAndGet();
                }).build();

        List<Future<?>> futures = new ArrayList<>();
        for(int i = 0 ; i < numThreads; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    task.start();
                } catch (Exception ex) {
                    failedCounter.incrementAndGet();
                }
            }));
        }

        futures.forEach(ThrowingConsumer.toConsumer((ThrowingConsumer<? super Future<?>>) Future::get));

        Assert.assertEquals(1, executorCounter.get());
        Assert.assertEquals(numThreads - 1, failedCounter.get());
    }

    @Test
    @Ignore
    public void test1() throws Exception {
        Task<String, Void> task = new DomainTask.Builder<String, Void>().type("task")
                .execution(execution -> new ExecutorServiceExecution<>(execution, Executors.newFixedThreadPool(1)))
                .executor(execution -> {
                    System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  phase 1");
                    Thread.sleep(5000);
                    //    CAN PAUSE HERE  //
                    execution.then(() -> {
                        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  phase 2");
                        Thread.sleep(5000);
                        //    CAN PAUSE HERE  //
                        execution.thenComplete(() -> {
                            System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  phase 3");
                            Thread.sleep(5000);

                            return "done";
                        });
                    });
                }).build();

        task.then(task1 -> System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  task is complete")).start();
        Thread.sleep(1000);

        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  pausing...");
        task.pause();
        Thread.sleep(6000);
        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  task state is: " + task.getState());

        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  resuming in 1 second...");
        Thread.sleep(1000);
        task.resume();
        Thread.sleep(1000);
        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  task state is: " + task.getState());

        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  pausing again...");
        task.pause();
        Thread.sleep(6000);
        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  task state is: " + task.getState());

        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  resuming in 1 second...");
        Thread.sleep(1000);
        task.resume();
        Thread.sleep(6000);

        System.out.println(new Date() + "  |  " + Thread.currentThread().getName() + "  |  task state is: " + task.getState());
    }
}
