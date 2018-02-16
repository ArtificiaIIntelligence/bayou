package edu.rice.cs.caper.programming.thread;

import edu.rice.cs.caper.programming.numbers.NatNum32;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadSchedulerFifoInterrupt implements ThreadScheduler
{
    private final NatNum32 _concurrentLimit;

    private final Object _operationCompletedCondition = new Object();

    private NatNum32 _inActionOperationsCount = new NatNum32(0);

    private final LinkedList<Thread> _waitingThreads = new LinkedList<>();

    public ThreadSchedulerFifoInterrupt(NatNum32 concurrentLimit)
    {
        _concurrentLimit = concurrentLimit;
    }


    @Override
    public <T> T schedule(Supplier<T> operation)
    {
        synchronized (_operationCompletedCondition)
        {
            if(!_waitingThreads.isEmpty() || _inActionOperationsCount.AsInt == _concurrentLimit.AsInt)
            {
                _waitingThreads.add(Thread.currentThread());
                awaitTurn();
            }
            _inActionOperationsCount = _inActionOperationsCount.increment();
        }

        try
        {
            return operation.get();
        }
        finally
        {
            synchronized (_operationCompletedCondition)
            {
                _inActionOperationsCount = _inActionOperationsCount.decrement();
                if(!_waitingThreads.isEmpty())
                {
                    Thread nextToGo = _waitingThreads.remove();
                    nextToGo.interrupt();
                }
            }
        }
    }

    private void awaitTurn()
    {
        try
        {
            _operationCompletedCondition.wait();
        }
        catch (InterruptedException e)
        {
            // do nothing
        }
    }
}