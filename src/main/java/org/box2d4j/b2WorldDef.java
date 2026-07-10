package org.box2d4j;

public class b2WorldDef {
    public b2Vec2 gravity = new b2Vec2();
    public float restitutionThreshold;
    public float hitEventThreshold;
    public float contactHertz;
    public float contactDampingRatio;
    public float maxContactPushSpeed;
    public float maximumLinearSpeed;
    public b2FrictionCallback frictionCallback;
    public b2RestitutionCallback restitutionCallback;
    public boolean enableSleep;
    public boolean enableContinuous;
    public int workerCount;
    public b2EnqueueTaskCallback enqueueTask;
    public b2FinishTaskCallback finishTask;
    public Object userTaskContext;
    public Object userData;
    public int internalValue;

    public void setTaskScheduler(B2TaskScheduler scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("scheduler");
        }
        int schedulerWorkerCount = scheduler.workerCount();
        if (schedulerWorkerCount < 1 || schedulerWorkerCount > 64) {
            throw new IllegalArgumentException("scheduler workerCount must be in [1, 64]");
        }
        workerCount = schedulerWorkerCount;
        enqueueTask = (task, itemCount, minRange, taskContext, userContext) ->
            ((B2TaskScheduler) userContext).enqueue(task, itemCount, minRange, taskContext);
        finishTask = (taskHandle, userContext) ->
            ((B2TaskScheduler) userContext).finish(taskHandle);
        userTaskContext = scheduler;
    }
}
