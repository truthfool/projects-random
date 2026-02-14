package concurrency

import (
    "context"
    "sync"
)

// Job represents a unit of work to be processed by the worker pool.
type Job func(ctx context.Context)

// WorkerPool is a simple goroutine worker pool using channels and context.
type WorkerPool struct {
    numWorkers int
    jobs       chan Job
    wg         sync.WaitGroup
    cancel     context.CancelFunc
    ctx        context.Context
    onceStart  sync.Once
    onceStop   sync.Once
}

// NewWorkerPool creates a worker pool with N workers and a buffered job queue.
func NewWorkerPool(numWorkers int, queueSize int) *WorkerPool {
    if numWorkers <= 0 {
        numWorkers = 1
    }
    if queueSize < 0 {
        queueSize = 0
    }
    return &WorkerPool{
        numWorkers: numWorkers,
        jobs:       make(chan Job, queueSize),
    }
}

// Start spins up worker goroutines.
func (p *WorkerPool) Start() {
    p.onceStart.Do(func() {
        p.ctx, p.cancel = context.WithCancel(context.Background())
        for i := 0; i < p.numWorkers; i++ {
            p.wg.Add(1)
            go func() {
                defer p.wg.Done()
                for {
                    select {
                    case <-p.ctx.Done():
                        return
                    case job, ok := <-p.jobs:
                        if !ok {
                            return
                        }
                        if job != nil {
                            job(p.ctx)
                        }
                    }
                }
            }()
        }
    })
}

// Submit enqueues a job for processing.
func (p *WorkerPool) Submit(job Job) bool {
    select {
    case p.jobs <- job:
        return true
    default:
        return false
    }
}

// Stop gracefully stops workers after draining the queue.
func (p *WorkerPool) Stop() {
    p.onceStop.Do(func() {
        if p.cancel != nil {
            p.cancel()
        }
        close(p.jobs)
        p.wg.Wait()
    })
}


