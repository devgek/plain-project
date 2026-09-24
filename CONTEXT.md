# Notes

A minimal example domain for the project template: users record, change and discard short free-text notes.

## Language

**Note**:
A titled piece of plain text that a user records, with the times it was created and last changed.
_Avoid_: Memo, entry, item, post

**Title**:
The required short name of a Note, at most 200 characters.
_Avoid_: Subject, heading, name

**Content**:
The optional body of a Note, in plain text of any length (no markup).
_Avoid_: Body, text, description

**Deleting** a Note:
Removing it permanently; there is no archive and no way to restore it.
_Avoid_: Archiving, trashing
