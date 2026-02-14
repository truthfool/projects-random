// PHASE 1: Domain Modeling
// LEARNING GOAL: Understanding Interfaces, Types, Enums, and Discriminated Unions.

// 1. Enums help fix a set of values.
// In interviews, know the difference between numeric (default) and string enums.
export enum TaskStatus {
    TODO = "TODO",
    IN_PROGRESS = "IN_PROGRESS",
    DONE = "DONE",
    ARCHIVED = "ARCHIVED"
}

export enum TaskPriority {
    LOW = 1,
    MEDIUM = 2,
    HIGH = 3,
    CRITICAL = 4
}

// 2. Type Aliases are great for Unions and Primitives.
// PRO TIP: Use 'type' for things that WON'T be extended (like IDs or Unions).
export type ID = string | number; 

// 3. Interfaces are for shapes that might be extended or implemented by classes.
export interface BaseEntity {
    id: ID;
    createdAt: Date;
    updatedAt: Date;
}

// 4. Inheritance: Extending interfaces.
export interface User extends BaseEntity {
    name: string;
    email: string;
    role: "admin" | "user" | "guest"; // Literal Type Union (very powerful!)
}

export interface Task extends BaseEntity {
    title: string;
    description?: string; // Optional property
    status: TaskStatus;
    priority: TaskPriority;
    assigneeId: ID | null; // Nullable type
    tags: string[];
}

// 5. Discriminated Unions (Advanced)
// This is often asked in interviews. A type that can be one of several shapes,
// distinguished by a common property ('kind').

interface NoteAttachment {
    kind: 'note';
    content: string;
}

interface FileAttachment {
    kind: 'file';
    fileUrl: string;
    size: number;
}

interface LinkAttachment {
    kind: 'link';
    url: string;
}

export type Attachment = NoteAttachment | FileAttachment | LinkAttachment;

export interface TaskWithAttachments extends Task {
    attachments: Attachment[];
}
