# TypeScript Mastery: Enterprise Task Manager

This project is structured as a **self-guided workshop**. We will build a Task Management System, but the goal isn't the app itself—it's the type system behind it.

## Learning Roadmap

### Phase 1: The Foundation (Interfaces & Types)

**Goal:** Understand the difference between `interface` and `type`, and how to model complex domain objects.

- [ ] implementation: `src/core/models.ts`
- **Concepts**: Interfaces, Enums, Union Types, Optional Properties.

### Phase 2: Generics & Abstraction

**Goal:** Build a reusable storage engine that works for _any_ data type, not just tasks. This is a favorite interview topic.

- [ ] implementation: `src/core/storage.ts`
- **Concepts**: Generic Classes (`class Storage<T>`), Generic Constraints (`<T extends BaseEntity>`).

### Phase 3: Advanced Utility Types

**Goal:** Implement features like "Update Task" where we don't want to pass the whole object.

- [ ] implementation: `src/utils/types.ts`
- **Concepts**: `Partial<T>`, `Pick<T, K>`, `Omit<T, K>`, `Readonly<T>`.

### Phase 4: Type Guards & Narrowing

**Goal:** Handle input that might be messy (e.g., from an API) and safely convert it.

- [ ] implementation: `src/utils/guards.ts`
- **Concepts**: Type Predicates (`arg is Type`), discriminators.

### Phase 5: The Master Class (Mapped & Conditional Types)

**Goal:** Create a powerful event system where types are calculated automatically.

- [ ] implementation: `src/core/events.ts`
- **Concepts**: `keyof`, `typeof`, Conditional Types (`T extends U ? X : Y`).

---

## How to Run

1. `npm install`
2. Write your code in `src/`.
3. Run `npx ts-node src/index.ts` to test.
