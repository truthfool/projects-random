import { Task } from "./models";

// PHASE 5: Advanced Event System
// LEARNING GOAL: Mapped Types and Conditional Types.

// Let's say we have an event map:
export interface EventMap {
    "task:created": Task;
    "task:updated": Task;
    "task:deleted": { id: string };
    "user:login": { userId: string; timestamp: number };
}

// 1. keyof: Extracting keys
type EventName = keyof EventMap; // "task:created" | "task:updated" | ...

// 2. Generic Handler
type Handler<T> = (data: T) => void;

export class EventEmitter {
    private listeners: Partial<Record<EventName, Handler<any>[]>> = {};

    // K extends keyof EventMap ensures 'event' is a valid event name.
    // data: EventMap[K] ensures 'data' matches the specific event type.
    on<K extends keyof EventMap>(event: K, handler: Handler<EventMap[K]>) {
        if (!this.listeners[event]) {
            this.listeners[event] = [];
        }
        this.listeners[event]!.push(handler);
    }

    emit<K extends keyof EventMap>(event: K, data: EventMap[K]) {
        const handlers = this.listeners[event];
        if (handlers) {
            handlers.forEach(h => h(data));
        }
    }
}
