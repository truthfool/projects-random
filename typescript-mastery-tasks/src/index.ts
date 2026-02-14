import { InMemoryStorage } from "./core/storage";
import { Task, TaskPriority, TaskStatus } from "./core/models";
import { EventEmitter } from "./core/events";
import { getAttachmentSummary } from "./utils/guards";

// Main Entry Point
// Run this with: npx ts-node src/index.ts

const taskStorage = new InMemoryStorage<Task>();
const events = new EventEmitter();

// Listen to events
events.on("task:created", (task) => {
    console.log(`[EVENT] New Task Created: ${task.title}`);
});

// Create a task
const newTask: Task = {
    id: 1,
    title: "Master TypeScript",
    description: "Build a project using advanced types",
    status: TaskStatus.IN_PROGRESS,
    priority: TaskPriority.HIGH,
    createdAt: new Date(),
    updatedAt: new Date(),
    assigneeId: "user-123",
    tags: ["learning", "coding"],
};

// Save it
const result = taskStorage.save(newTask);
if (result.success) {
    console.log("Task Saved Successfully!");
    events.emit("task:created", result.data);
} else {
    console.error("Error Saving:", result.error);
}

// Demonstrate Discriminated Unions
const attachment = { kind: "file", fileUrl: "http://example.com/spec.pdf", size: 10 } as const;
console.log(getAttachmentSummary(attachment));

// Demonstrate Generics Fetch
const fetched = taskStorage.findById(1);
if (fetched.success) {
    console.log("Fetched Task:", fetched.data.title);
}
