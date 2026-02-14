import { Task, Attachment, BaseEntity } from "../core/models";

// PHASE 4: Type Guards & Narrowing
// LEARNING GOAL: Safely handling unknown data at runtime.

// 1. User-Defined Type Guard
// Syntax: parameter is Type
// Returns boolean, but tells compiler "If true, treat variable as Type".
export function isTask(entity: BaseEntity): entity is Task {
    return (entity as Task).title !== undefined && (entity as Task).status !== undefined;
}

// 2. Discriminated Union Switch (The "safe" way)
// TS can narrow the type automatically inside 'if' or 'switch' checks on the discriminator ('kind').
export function getAttachmentSummary(att: Attachment): string {
    switch (att.kind) {
        case "file":
            // TS knows 'att' is FileAttachment here
            return `File: ${att.fileUrl} (${att.size}mb)`;
        case "note":
            // TS knows 'att' is NoteAttachment here
            return `Note: ${att.content.substring(0, 20)}...`;
        case "link":
             // TS knows 'att' is LinkAttachment here
            return `Link: ${att.url}`;
        default:
            // This ensures we handled all cases. If we add a new kind and forget a case,
            // this line will likely error (or we can use 'never' type).
            const _exhaustiveCheck: never = att;
            return _exhaustiveCheck;
    }
}
