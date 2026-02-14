package repo

import (
    "context"
    "errors"
    "sync"
)

// Item represents a stored value.
type Item struct {
    Key   string
    Value string
}

// Repository describes basic CRUD operations.
type Repository interface {
    Put(ctx context.Context, item Item) error
    Get(ctx context.Context, key string) (Item, error)
    Delete(ctx context.Context, key string) error
    List(ctx context.Context) []Item
}

var ErrNotFound = errors.New("not found")

// InMemoryRepository is a thread-safe map-backed repository.
type InMemoryRepository struct {
    mu   sync.RWMutex
    data map[string]string
}

func NewInMemoryRepository() *InMemoryRepository {
    return &InMemoryRepository{data: make(map[string]string)}
}

func (r *InMemoryRepository) Put(ctx context.Context, item Item) error {
    select {
    case <-ctx.Done():
        return ctx.Err()
    default:
    }
    r.mu.Lock()
    r.data[item.Key] = item.Value
    r.mu.Unlock()
    return nil
}

func (r *InMemoryRepository) Get(ctx context.Context, key string) (Item, error) {
    r.mu.RLock()
    val, ok := r.data[key]
    r.mu.RUnlock()
    if !ok {
        return Item{}, ErrNotFound
    }
    return Item{Key: key, Value: val}, nil
}

func (r *InMemoryRepository) Delete(ctx context.Context, key string) error {
    r.mu.Lock()
    delete(r.data, key)
    r.mu.Unlock()
    return nil
}

func (r *InMemoryRepository) List(ctx context.Context) []Item {
    r.mu.RLock()
    items := make([]Item, 0, len(r.data))
    for k, v := range r.data {
        items = append(items, Item{Key: k, Value: v})
    }
    r.mu.RUnlock()
    return items
}


