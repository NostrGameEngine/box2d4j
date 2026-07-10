// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "determinism.h"

#include <algorithm>
#include <condition_variable>
#include <cstdint>
#include <cstdio>
#include <deque>
#include <mutex>
#include <thread>
#include <vector>

namespace
{
struct TaskHandle
{
	b2TaskCallback* task;
	void* taskContext;
	std::mutex mutex;
	std::condition_variable condition;
	int remaining;
};

struct Job
{
	TaskHandle* handle;
	int startIndex;
	int endIndex;
};

class TaskPool
{
public:
	explicit TaskPool( int workerCount ) : m_workerCount( workerCount )
	{
		for ( int workerIndex = 0; workerIndex < workerCount; ++workerIndex )
		{
			m_workers.emplace_back( [this, workerIndex] { WorkerLoop( workerIndex ); } );
		}
	}

	~TaskPool()
	{
		{
			std::lock_guard<std::mutex> lock( m_queueMutex );
			m_stopping = true;
		}
		m_queueCondition.notify_all();
		for ( std::thread& worker : m_workers )
		{
			worker.join();
		}
	}

	void* Enqueue( b2TaskCallback* task, int itemCount, int minRange, void* taskContext )
	{
		if ( itemCount <= 0 )
		{
			return nullptr;
		}
		if ( m_workerCount == 1 )
		{
			task( 0, itemCount, 0, taskContext );
			return nullptr;
		}

		int range = std::max( 1, minRange );
		int taskCount = std::min( m_workerCount, std::max( 1, itemCount / range ) );
		TaskHandle* handle = new TaskHandle{ task, taskContext, {}, {}, taskCount };
		int baseCount = itemCount / taskCount;
		int remainder = itemCount - baseCount * taskCount;
		int startIndex = 0;
		{
			std::lock_guard<std::mutex> lock( m_queueMutex );
			for ( int taskIndex = 0; taskIndex < taskCount; ++taskIndex )
			{
				int count = baseCount + ( taskIndex < remainder ? 1 : 0 );
				m_jobs.push_back( Job{ handle, startIndex, startIndex + count } );
				startIndex += count;
			}
		}
		m_queueCondition.notify_all();
		return handle;
	}

	void Finish( void* taskHandle )
	{
		TaskHandle* handle = static_cast<TaskHandle*>( taskHandle );
		std::unique_lock<std::mutex> lock( handle->mutex );
		handle->condition.wait( lock, [handle] { return handle->remaining == 0; } );
		lock.unlock();
		delete handle;
	}

private:
	void WorkerLoop( int workerIndex )
	{
		for ( ;; )
		{
			Job job;
			{
				std::unique_lock<std::mutex> lock( m_queueMutex );
				m_queueCondition.wait( lock, [this] { return m_stopping || !m_jobs.empty(); } );
				if ( m_stopping && m_jobs.empty() )
				{
					return;
				}
				job = m_jobs.front();
				m_jobs.pop_front();
			}

			job.handle->task( job.startIndex, job.endIndex, static_cast<uint32_t>( workerIndex ),
				job.handle->taskContext );
			{
				std::lock_guard<std::mutex> lock( job.handle->mutex );
				job.handle->remaining -= 1;
				if ( job.handle->remaining == 0 )
				{
					job.handle->condition.notify_one();
				}
			}
		}
	}

	int m_workerCount;
	std::vector<std::thread> m_workers;
	std::deque<Job> m_jobs;
	std::mutex m_queueMutex;
	std::condition_variable m_queueCondition;
	bool m_stopping = false;
};

void* EnqueueTask( b2TaskCallback* task, int itemCount, int minRange, void* taskContext, void* userContext )
{
	return static_cast<TaskPool*>( userContext )->Enqueue( task, itemCount, minRange, taskContext );
}

void FinishTask( void* taskHandle, void* userContext )
{
	static_cast<TaskPool*>( userContext )->Finish( taskHandle );
}
} // namespace

int main()
{
	for ( int workerCount : { 1, 2, 4, 8 } )
	{
		TaskPool taskPool( workerCount );
		b2WorldDef worldDef = b2DefaultWorldDef();
		worldDef.workerCount = workerCount;
		worldDef.enqueueTask = EnqueueTask;
		worldDef.finishTask = FinishTask;
		worldDef.userTaskContext = &taskPool;
		b2WorldId worldId = b2CreateWorld( &worldDef );
		FallingHingeData data = CreateFallingHinges( worldId );

		bool done = false;
		while ( !done )
		{
			b2World_Step( worldId, 1.0f / 60.0f, 4 );
			done = UpdateFallingHinges( worldId, &data );
			if ( data.stepCount > 1000 )
			{
				return 2;
			}
		}

		std::printf( "workers %d sleep %d hash %08x\n", workerCount, data.sleepStep, data.hash );
		DestroyFallingHinges( &data );
		b2DestroyWorld( worldId );
	}
	return 0;
}
