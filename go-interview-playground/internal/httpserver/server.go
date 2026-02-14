package httpserver

import (
    "context"
    "encoding/json"
    "log"
    "net/http"
    "time"

    "github.com/ishanranasingh/go-interview-playground/internal/concurrency"
    "github.com/ishanranasingh/go-interview-playground/internal/middleware"
    "github.com/ishanranasingh/go-interview-playground/internal/repo"
)

type Server struct {
    http *http.Server
    repo repo.Repository
    pool *concurrency.WorkerPool
}

func NewServer(port int, r repo.Repository, p *concurrency.WorkerPool) *Server {
    mux := http.NewServeMux()
    s := &Server{repo: r, pool: p}
    mux.HandleFunc("/health", s.handleHealth)
    mux.HandleFunc("/items", s.handleItems)
    mux.HandleFunc("/work", s.handleWork)

    bucket := middleware.NewTokenBucket(10, 5) // burst 10, 5 req/s
    handler := middleware.RateLimit(mux, bucket)

    s.http = &http.Server{
        Addr:              ":" + itoa(port),
        Handler:           handler,
        ReadHeaderTimeout: 3 * time.Second,
    }
    return s
}

func (s *Server) Start() error {
    log.Printf("listening on %s", s.http.Addr)
    return s.http.ListenAndServe()
}

func (s *Server) Shutdown(ctx context.Context) error {
    return s.http.Shutdown(ctx)
}

func (s *Server) handleHealth(w http.ResponseWriter, r *http.Request) {
    w.WriteHeader(http.StatusOK)
    _, _ = w.Write([]byte("ok"))
}

func (s *Server) handleItems(w http.ResponseWriter, r *http.Request) {
    switch r.Method {
    case http.MethodGet:
        items := s.repo.List(r.Context())
        writeJSON(w, items, http.StatusOK)
    case http.MethodPost:
        var in struct{
            Key string `json:"key"`
            Value string `json:"value"`
        }
        if err := json.NewDecoder(r.Body).Decode(&in); err != nil {
            http.Error(w, err.Error(), http.StatusBadRequest)
            return
        }
        if err := s.repo.Put(r.Context(), repo.Item{Key: in.Key, Value: in.Value}); err != nil {
            http.Error(w, err.Error(), http.StatusInternalServerError)
            return
        }
        writeJSON(w, map[string]string{"status":"stored"}, http.StatusCreated)
    default:
        w.WriteHeader(http.StatusMethodNotAllowed)
    }
}

func (s *Server) handleWork(w http.ResponseWriter, r *http.Request) {
    var in struct{ Payload string `json:"payload"` }
    _ = json.NewDecoder(r.Body).Decode(&in)
    ok := s.pool.Submit(func(ctx context.Context){
        select {
        case <-ctx.Done():
            return
        case <-time.After(500 * time.Millisecond):
        }
        _ = s.repo.Put(context.Background(), repo.Item{Key: time.Now().Format(time.RFC3339Nano), Value: in.Payload})
    })
    if !ok {
        http.Error(w, "queue full", http.StatusServiceUnavailable)
        return
    }
    writeJSON(w, map[string]string{"status":"enqueued"}, http.StatusAccepted)
}

func writeJSON(w http.ResponseWriter, v any, status int) {
    w.Header().Set("Content-Type", "application/json")
    w.WriteHeader(status)
    _ = json.NewEncoder(w).Encode(v)
}

// itoa avoids importing strconv for a single use.
func itoa(i int) string {
    if i == 0 { return "0" }
    neg := false
    if i < 0 { neg = true; i = -i }
    var b [20]byte
    bp := len(b)
    for i > 0 {
        bp--
        b[bp] = byte('0' + (i % 10))
        i /= 10
    }
    if neg {
        bp--
        b[bp] = '-'
    }
    return string(b[bp:])
}


