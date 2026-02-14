import { BaseEntity, ID } from "./models";

// PHASE 2: Generics & Abstraction
// LEARNING GOAL: Writing reusable code that maintains type safety.

// 1. Generic Result Pattern
// Instead of throwing errors, return a Success or Failure object.
// This is very common in strict TypeScript changes.
type Success<T> = { success: true; data: T };
type Failure = { success: false; error: string };
export type Result<T> = Success<T> | Failure;

// 2. Generic Interface
// Defines a contract for ANY entity T.
export interface Repository<T extends BaseEntity> {
    findById(id: ID): Result<T>;
    save(item: T): Result<T>;
    delete(id: ID): Result<boolean>;
    findAll(): T[];
}

// 3. Generic Class with Constraint
// <T extends BaseEntity> ensures T has an 'id' property.
// Without 'extends BaseEntity', typescript would complain when we access item.id.
export class InMemoryStorage<T extends BaseEntity> implements Repository<T> {
    private data = new Map<ID, T>();

    save(item: T): Result<T> {
        this.data.set(item.id, item);
        return { success: true, data: item };
    }

    findById(id: ID): Result<T> {
        const item = this.data.get(id);
        if (!item) {
            return { success: false, error: `Item with id ${id} not found` };
        }
        return { success: true, data: item };
    }

    delete(id: ID): Result<boolean> {
        if (!this.data.has(id)) {
            return { success: false, error: "Not found" };
        }
        this.data.delete(id);
        return { success: true, data: true };
    }

    findAll(): T[] {
        return Array.from(this.data.values());
    }
}
