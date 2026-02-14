package util

// Map applies a function to each element of a slice and returns a new slice.
func Map[T any, R any](in []T, fn func(T) R) []R {
    out := make([]R, len(in))
    for i, v := range in {
        out[i] = fn(v)
    }
    return out
}

// Keys returns the keys of a map as a slice.
func Keys[K comparable, V any](m map[K]V) []K {
    keys := make([]K, 0, len(m))
    for k := range m {
        keys = append(keys, k)
    }
    return keys
}


