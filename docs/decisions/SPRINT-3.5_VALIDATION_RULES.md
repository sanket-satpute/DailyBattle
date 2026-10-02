# DECISION RECORD: SPRINT 3.5 INPUT VALIDATION RULES

## Status
LOCKED

## Context
Sprint 3.5 requires implementing text inputs and a specific Battle Code input.
`04_SCREEN_BLUEPRINTS.md` (lines 2166–2167) explicitly listed "Battle Name validation rules" and "Battle Code validation rules" as PENDING decisions that must not be silently invented.

## Resolution
The following validation rules are approved and must be enforced globally:

### Battle Code
- Exactly 6 characters
- Allowed characters: A–Z and 0–9 only
- Input is automatically normalized to uppercase
- No spaces
- No special characters
- No shorter or longer values are valid
- Canonical validation: `^[A-Z0-9]{6}$`

**Examples:**
- `K4X8M9` → valid
- `ABC123` → valid
- `abc123` → normalize to `ABC123`
- `ABC12` → invalid
- `ABC1234` → invalid
- `AB-C12` → invalid

### Battle Name
- Minimum 1 character
- Maximum 20 characters
- Letters, numbers, spaces, and normal Unicode characters are allowed
- Leading/trailing whitespace should be trimmed before validation/storage
- Consecutive internal spaces may remain as entered
- Empty/whitespace-only input → invalid
- Values exceeding 20 characters → invalid
- Do not silently truncate user input

## Implementation Requirements
- Implement these rules exactly; do not add additional restrictions.
- Keep validation logic separate from UI so it can be unit tested.
- Validation must be deterministic and shared wherever these inputs are used.
- Provide the documented validation states: default, focused, error, and valid/success where applicable.
- Do not invent additional Battle Code semantics (prefixes, reserved codes, expiry, etc.).
