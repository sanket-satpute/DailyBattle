# Daily Battle — Offline & Error Handling Specification

**Document ID:** DB-OFFLINE-ERROR  
**Version:** 1.0  
**Status:** Reliability / Recovery Contract  
**Audience:** Antigravity AI, Android implementation agents, backend developers, QA  
**Product:** Daily Battle  
**Authority:** Master Specification + approved project documentation  
**Last Updated:** 2026-09-30

---

# 1. Purpose

This document defines how Daily Battle behaves when:

- the device is offline
- the network becomes unavailable
- an API request fails
- the backend is unavailable
- the application is backgrounded
- the application process is killed
- the user repeatedly taps an action
- persistence fails
- synchronization fails
- a battle session becomes inconsistent
- stale data is displayed
- a remote operation times out

The goal is to ensure that failures do not create:

- duplicate official attempts
- duplicate submissions
- corrupted scores
- incorrect navigation
- lost battle progress
- fake server data
- misleading UI states

---

# 2. Core Reliability Principle

Daily Battle must distinguish between:

```text
Normal Product State

and:

Technical Failure State

A technical failure must not silently mutate the user's product state.

Example:

NETWORK FAILURE
      ↓
Error / Retry

NOT:

NETWORK FAILURE
      ↓
Start another battle
3. Authority

This document must be interpreted together with:

07_TECHNICAL_ARCHITECTURE.md
08_DATA_AND_API.md
06_NAVIGATION_AND_FLOWS.md
02_GAMEPLAY.md

If a failure behavior is not defined here or in another authoritative document:

Do not invent product behavior.

Mark the behavior as pending.

4. Failure Categories

The application must distinguish at least:

OFFLINE
NETWORK_UNAVAILABLE
REQUEST_TIMEOUT
SERVER_ERROR
AUTHENTICATION_ERROR
AUTHORIZATION_ERROR
VALIDATION_ERROR
PERSISTENCE_ERROR
SYNC_ERROR
INVALID_STATE
UNKNOWN_ERROR

These technical categories may map to fewer user-facing messages.

5. User-Facing Principle

Technical errors should be translated into understandable product language.

Do not expose raw technical errors such as:

SocketTimeoutException
HTTP 500
NullPointerException
SQLiteException

to normal users.

6. Error Severity

Use three conceptual severity levels.

Level 1 — Recoverable

The current task can continue or retry.

Examples:

temporary network failure
request timeout
temporary synchronization failure

UI behavior:

Current state preserved
+
Retry option
Level 2 — Recoverable With Navigation

The current operation cannot continue immediately, but existing data is safe.

Examples:

backend unavailable before battle start
stale remote data
friend request failure

UI behavior:

Preserve known state
+
Explain issue
+
Allow retry / return
Level 3 — Critical

Application state may be inconsistent or cannot safely continue.

Examples:

corrupted battle session
invalid official session
unrecoverable persistence failure

UI behavior:

Do not guess
Do not create replacement state
Preserve diagnostics
Provide safe recovery path

Exact recovery UX must be approved for each critical state.

7. Offline Definition

Offline means the application cannot currently establish the required network connection.

Offline does NOT automatically mean:

No local data

The application may still have valid local state.

Therefore:

Offline
+
Valid Local State

is different from:

Offline
+
No Local State
8. Connectivity State

Conceptually:

ONLINE
OFFLINE
UNKNOWN

Connectivity information is advisory.

A device reported as ONLINE may still experience a failed request.

Therefore:

A successful connectivity check must never be treated as proof that the next API request will succeed.

9. General Offline Behavior

When offline:

preserve local state
do not clear existing content
do not create replacement official data
do not fabricate server values
do not repeatedly retry without user/system control
provide an appropriate recovery action
10. App Launch While Offline
Case A — Cached/Local State Available

Example:

User opens app
     ↓
No network
     ↓
Cached Home/Battle state exists

Behavior:

show valid cached information where allowed
indicate stale/offline state when relevant
do not fabricate freshness
allow locally supported actions
Case B — Required Remote Data Missing

Example:

User opens app
     ↓
Offline
     ↓
Today's Battle never downloaded

Behavior:

do not invent a Daily Battle
do not generate an unofficial replacement
explain that the required battle data is unavailable
provide retry/reconnect behavior

Exact copy is a UI specification concern.

11. Today's Battle Availability

The official battle is authoritative.

If the client cannot retrieve today's official Battle and does not have valid cached data:

Do NOT create a local replacement Battle.

This is critical.

The application must never silently turn Practice content into the official battle.

12. Cached Today's Battle

If a valid official Battle definition has already been cached:

Cached Battle
      ↓
Validate identity/version
      ↓
Use according to approved offline rules

However, cached battle availability does not automatically grant permission to start an official attempt offline.

Whether an Official Battle can be started or completed without server connectivity is:

PENDING

until explicitly defined.

13. Battle Start Failure

User:

PLAY BATTLE

Request fails.

The system must not:

create a second local official session
assume the battle started remotely
navigate to Snap without valid session state
mark the battle completed

Instead:

Battle remains Not Started
+
Error / Retry

unless the backend has already confirmed the session.

14. Duplicate Start Protection

Scenario:

User taps PLAY
↓
Request sent
↓
Network becomes slow
↓
User taps PLAY again

The application must not create:

Session A
Session B

for the same Official Battle.

Use:

clientRequestId
+
idempotent backend operation
+
local state protection

where supported by the architecture.

15. Start Request Timeout

If StartOfficialBattle times out:

The client does NOT know whether the server received the request.

Therefore:

UNKNOWN RESULT

must not be treated as:

FAILED

or:

SUCCESS

without confirmation.

Preferred conceptual flow:

Start Request
      ↓
Timeout
      ↓
Check existing official session
      ↓
If found → continue
If not found → retry safely

Exact backend implementation is pending.

16. Official Battle Session Recovery

If a user already has an official session:

Battle Session = IN_PROGRESS

the application should restore that session rather than create another one.

Conceptually:

App Launch
    ↓
Check Active Official Session
    ↓
Found?
 ┌──Yes──────→ Restore
 │
 No
 ↓
Normal Home State
17. Process Death

Scenario:

User playing Shift
      ↓
Android kills process
      ↓
User opens app again

Required principle:

Process death must not create a new official attempt.

The app should recover from persisted battle/session state.

Exact recovery point depends on the finalized gameplay interruption rules.

18. App Backgrounding

Scenario:

Snap active
↓
User receives notification
↓
App goes background
↓
User returns

The implementation must preserve enough state to recover safely.

Pending product decisions include:

whether timers continue
whether timers pause
whether a challenge expires
whether the current challenge resumes

Antigravity MUST NOT invent these rules.

19. Screen Recreation

Screen recreation must not:

restart a challenge
reset a score
create a new session
submit an answer twice
navigate to the wrong screen

UI state should be reconstructed from the authoritative application/session state.

20. Gameplay State Recovery

The implementation should distinguish:

Challenge not started
Challenge active
Challenge completed
Battle completed

For example:

SNAP = COMPLETE
SHIFT = ACTIVE
CROWD CALL = NOT_STARTED

means the application must not return the user to Snap.

21. Challenge Submission Failure

Scenario:

User answers Snap
↓
Submit
↓
Network failure

The system must preserve enough information to determine whether the submission:

reached the server
failed before transmission
succeeded but response was lost

Do not blindly submit the answer repeatedly.

Use request identity/idempotency where supported.

22. Duplicate Challenge Submission

The same challenge submission must not produce:

Result A
Result B

for one official challenge.

Conceptually:

(sessionId, challengeId, submissionIdentity)

must be uniquely identifiable.

Exact server-side constraint is pending.

23. Challenge Result Unknown State

If submission response is lost:

Client does not know final server state

The client must enter a recoverable synchronization state.

It must not:

show a fabricated score
mark the battle complete without confirmation
create a replacement challenge
24. Battle Completion Failure

Scenario:

Crowd Call complete
↓
Complete Battle request
↓
Timeout

The client must not assume:

Battle failed

because the server may already have completed it.

Recovery:

Check Battle Result
        ↓
Found?
 ├── Yes → display result
 └── No  → safe retry
25. Final Result Integrity

Once an Official Battle Result is confirmed:

resultId
battleId
sessionId
userId
totalScore

must remain associated.

Refreshing the result must not generate a new result.

26. Result Screen Offline

If the user has already completed today's Battle and the result is cached:

Offline
+
Cached Result

the result may be displayed.

However, values known to require fresh server data must not be presented as current if freshness is unknown.

Examples:

latest rival score
latest percentile
newly updated friend scores
27. Result Screen Without Cached Result

If the result is required but unavailable locally and the network is unavailable:

No fabricated result
+
Retry / recovery state

Do not display placeholder scores as if they were real.

28. Percentile Failure

If the official score exists but percentile cannot be retrieved:

Score = available
Percentile = unavailable

Do not estimate percentile locally unless the calculation is explicitly defined and authoritative.

The UI should use the approved partial-data state.

29. Rival Data Failure

If today's rival data cannot be loaded:

Existing cached rival information may be displayed where valid.

But stale data must not be presented as today's confirmed result.

Possible conceptual state:

Rival known
Rival score unavailable

The screen should degrade gracefully.

30. Friends Failure

If Friends cannot be loaded:

Existing cached friends → display if valid
No cached friends → empty/error state

Do not interpret network failure as:

User has zero friends

This distinction is mandatory.

31. Empty vs Error

These are different states.

Empty

Means:

The request succeeded and there is genuinely no data.

Example:

No friends yet.
Error

Means:

The system could not determine the current data.

Example:

Couldn't load friends.

Never convert an API error into an empty state.

32. Loading vs Empty

Loading means:

Data request is still in progress.

Empty means:

Request completed successfully
+
No data exists.

Never show:

No friends yet

while the friends request is still loading.

33. Retry Policy

Retries must be intentional.

Retry is appropriate for:

transient network failure
timeout
temporary server failure

Retry is NOT appropriate for:

invalid user input
unauthorized request
forbidden request
malformed data
permanently invalid battle state
34. Automatic Retry

Automatic retry should be conservative.

Do not create:

infinite retry loop

Rules:

bounded retry count
increasing delay where appropriate
cancellation support
no repeated user-visible navigation
no duplicate business mutations

Exact retry counts remain implementation-level decisions unless specified elsewhere.

35. User-Initiated Retry

Where useful, expose:

Retry

as the primary recovery action.

Retry must repeat the appropriate operation safely.

It must not restart the entire application flow.

36. Server Error

HTTP/server failure should be mapped into an appropriate application state.

Example:

Server unavailable
↓
Preserve local state
↓
Retry

Do not clear:

battle session
cached profile
friend list
history

merely because one request failed.

37. Authentication Failure

If authentication becomes part of the final architecture:

Unauthorized

must be distinguished from:

Offline

Do not repeatedly retry an expired credential forever.

Exact authentication recovery is pending.

38. Authorization Failure

A forbidden operation should not be retried indefinitely.

Example:

User attempts operation
↓
Backend says forbidden
↓
Show appropriate state

Do not interpret authorization failure as network failure.

39. Validation Failure

If the server rejects an invalid request:

Do not retry unchanged request automatically.

Instead:

Identify invalid state/input
↓
Preserve valid local state
↓
Present corrective UI if possible
40. Persistence Failure

Local storage failure is critical because it may affect battle integrity.

If the application cannot safely persist an official battle state:

Do not proceed as though the state was safely saved.

Potential behavior:

Persistence Failure
       ↓
Protect current state
       ↓
Prevent unsafe transition
       ↓
Show recovery state

Exact UX is pending.

41. Corrupted Battle State

If local state is inconsistent, examples include:

Crowd Call complete
but Snap missing

or:

Battle completed
but no result exists

The app must not guess.

It should:

identify inconsistency
preserve diagnostics
attempt safe server reconciliation where possible
avoid creating a second official attempt
expose a recovery state if reconciliation fails
42. Reconciliation

When local and remote state disagree:

Local State
     +
Remote State
     ↓
Reconciliation

The server should generally be authoritative for server-owned official data.

Examples:

Official result
Official battle identity
Friend relationship
Percentile

Local preferences remain locally authoritative where defined.

43. Stale Data

Cached data should be treated as:

KNOWN BUT POSSIBLY STALE

not:

CURRENT

unless freshness has been established.

Stale data should be visually or contextually distinguished only where the product design calls for it.

Do not add intrusive banners everywhere.

44. Offline Banner

A global permanent offline banner should NOT be added automatically.

Offline indication should appear where it helps the user understand why an operation cannot continue.

Avoid turning the entire app into a technical-status dashboard.

45. Battle-Specific Failure Priority

During an official battle, preserving battle integrity has higher priority than convenience.

Priority order:

1. Preserve official attempt
2. Preserve current progress
3. Avoid duplicate submission
4. Recover authoritative state
5. Continue battle
6. Present error messaging

Do not sacrifice state integrity for a smoother-looking transition.

46. Practice Offline Behavior

Practice is separate from Official.

If Practice is eventually allowed offline:

Practice
   ↓
Local execution
   ↓
Practice result

must not alter:

Official Battle
Official Score
Official Percentile
Official History

Exact offline Practice behavior is pending.

47. Network Recovery

When connectivity returns:

OFFLINE
   ↓
ONLINE

the application must not automatically perform arbitrary mutations.

It should only synchronize operations that are:

explicitly supported
safely retryable
idempotent
still valid
48. Synchronization Queue

If an offline mutation queue is approved, each queued operation should contain conceptually:

PendingOperation
├── operationId
├── operationType
├── payload
├── createdAt
├── retryCount
├── status
└── lastError

Possible states:

PENDING
SYNCING
SUCCESS
FAILED
CANCELLED

The exact queue implementation is pending.

Do not implement a generic queue without approved offline mutation requirements.

49. Sync Ordering

Operations with dependencies must preserve logical order.

Example:

Start Battle
    ↓
Submit Snap
    ↓
Submit Shift
    ↓
Submit Crowd Call
    ↓
Complete Battle

The system must not submit:

Complete Battle

before required challenge submissions have been accepted.

50. Synchronization Failure

If synchronization fails:

Local state remains intact
+
Operation remains recoverable
+
User is not given false success

Do not silently delete failed operations.

51. App Restart During Sync

If synchronization is interrupted:

App killed
↓
Restart

the pending operation must either:

remain recoverable
be safely retried
be reconciled against server state

depending on the finalized synchronization architecture.

It must not be duplicated.

52. Network Request Cancellation

Requests must support cancellation when appropriate.

Examples:

user leaves a screen
screen is destroyed
operation becomes irrelevant

However:

A cancellation must not accidentally cancel a server-side mutation that has already been accepted.

This is especially important for battle result submission.

53. Error Recovery Must Be Localized

An error in one feature must not unnecessarily destroy unrelated application state.

Example:

Friends API fails

must not clear:

today's Battle
Profile
History
Settings

Similarly:

History API fails

must not invalidate the active battle.

54. Screen-Level Failure Matrix
Screen / Feature	Offline	Remote Failure	Cached Data
Welcome	Continue	N/A	N/A
Battle Name	Local if supported	Save failure state	Local value
Home	Cached where valid	Preserve content	Yes
Battle Intro	Depends on battle availability	Retry	Yes if valid
Snap	Preserve active session	Recover/retry	Session state
Shift	Preserve active session	Recover/retry	Session state
Crowd Call	Preserve active session	Recover/retry	Session state
Results	Cached result if available	Retry/reconcile	Yes
Rival	Cached data if valid	Partial/error	Yes
Friends	Cached data if valid	Error/retry	Yes
Add Friend	Requires approved network behavior	Error/retry	No
Profile	Cached profile	Partial/error	Yes
History	Cached history	Error/retry	Yes
Settings	Local	Mostly unaffected	Local

This table represents the architectural reliability model.

Exact screen copy belongs to the screen specifications.

55. Battle State Failure Matrix
Situation	Required Principle
Battle not started	Do not create session on failed request
Start timeout	Reconcile before retry
Battle active	Preserve session
Challenge submission timeout	Reconcile before duplicate submit
App killed	Restore session
Network lost	Preserve progress
Battle completion timeout	Check result before retry
Result confirmed	Never create duplicate result
Local state corrupted	Reconcile, do not guess
Server unavailable	Preserve known local state
56. Error Messaging Principles

User-facing messages should:

explain what happened at an appropriate level
avoid technical jargon
provide a clear next action
avoid blame
avoid alarming language
avoid false certainty

Examples of conceptual messaging:

Couldn't load today's battle.
Try again.
Your battle is saved.
We'll reconnect when possible.
We couldn't confirm your result yet.
Check again.

Final copy is owned by the UX/content specification.

57. Do Not Use False Success

Never display:

Battle Complete

unless completion has been confirmed by the authoritative state.

Never display:

Friend Added

unless the relationship is confirmed.

Never display:

Score Submitted

unless submission is confirmed or the product explicitly defines a local-pending state.

58. Do Not Use False Empty States

Never show:

No Friends

when the actual state is:

Friends request failed.

Never show:

No History

when history could not be loaded.

Never show:

No Rival

when rival data simply failed to load.

59. Do Not Clear Data on Error

An API failure must not automatically cause:

clear database
reset session
logout user
delete cache
reset battle

Such behavior requires explicit authorization.

60. Recovery Hierarchy

When an operation fails:

1. Preserve state
2. Determine whether result is known
3. Reconcile if necessary
4. Retry safely
5. Restore UI
6. Inform user

Not:

1. Reset
2. Start over
61. Developer Diagnostics

Development builds should expose enough information to diagnose:

request ID
operation
battle ID
session ID
challenge ID
local state
remote state
error category
retry count

Sensitive values must not be logged.

62. Error Logging

Production logging must avoid:

passwords
authentication tokens
private credentials
unnecessary personal data
raw sensitive request payloads

Logs should remain useful for diagnosing:

session corruption
duplicate submission
synchronization failure
API errors
persistence errors
63. Crash vs Recoverable Error

A crash is not an acceptable substitute for error handling.

Critical expected failures must be represented as controlled states.

Examples:

Network failure
Database failure
Invalid session
Server rejection

must not cause avoidable application crashes.

64. Unknown Errors

For unexpected failures:

UNKNOWN_ERROR

The app should:

preserve known safe state
log diagnostic information
avoid destructive recovery
provide a safe user recovery path

Do not silently continue with potentially invalid state.

65. Retry Safety Rules

A retry is safe only when:

operation is idempotent
OR
operation has a stable request identity
OR
server state has been reconciled first

This rule is especially important for:

StartOfficialBattle
SubmitChallengeResult
CompleteBattle
AddFriend
66. Rate Limiting

If the backend reports rate limiting:

RATE_LIMITED

the client should not immediately retry repeatedly.

Respect server-provided retry information where available.

Exact rate-limit UX is pending.

67. Version Mismatch

If the client receives unsupported battle/configuration data:

Unknown Battle Version

the client must not guess how to render or score it.

Possible safe behavior:

Reject unsupported configuration
+
Report compatibility issue

Exact fallback behavior is pending.

68. Malformed Remote Data

If the backend returns structurally invalid data:

Parse
  ↓
Validation
  ↓
Invalid

the client must not blindly render it.

Use:

Controlled Error

rather than:

Crash
69. Security-Related Failures

Security failures must fail closed.

Examples:

Invalid authentication
Invalid authorization
Invalid session
Tampered request

The client must not bypass validation to keep the UI moving.

70. Recovery After Successful Reconnection

After connectivity returns:

Reconnect
   ↓
Refresh/reconcile relevant state
   ↓
Update UI

Do not refresh every feature indiscriminately.

Refresh only the state that requires reconciliation.

71. Background Sync

Background synchronization is optional and pending.

Do not add continuous background polling.

If implemented later, it must have:

clear purpose
battery constraints
network constraints
retry limits
cancellation behavior
72. No Infinite Polling

The application must never enter an uncontrolled loop such as:

request
↓
failure
↓
request
↓
failure
↓
request
...

All retries must be bounded and controlled.

73. Error State UI Rules

Error states should remain visually consistent with the Daily Battle design system.

Avoid:

giant red warning screens
excessive technical information
dramatic error animations
unnecessary modal interruptions

The app should remain calm and recoverable.

74. Gameplay Error UI

Gameplay errors should not destroy concentration.

Avoid:

FULL SCREEN ERROR

unless continuing is genuinely impossible.

Prefer:

small contextual feedback
+
preserved game state
+
clear recovery

where appropriate.

75. Animation During Error

Error animation must never imply a false gameplay result.

For example:

Do not play:

Battle Complete animation

while completion is still unconfirmed.

76. Haptics During Error

Error haptics may be used for genuine incorrect challenge input where specified.

Network/server errors should not reuse gameplay "wrong answer" feedback.

The two meanings must remain distinct.

77. Sound During Error

Do not use the gameplay incorrect-answer sound for:

network failure
server failure
offline state

unless explicitly approved.

78. Offline and Official Battle — Critical Rule

The most important reliability rule in Daily Battle is:

Never trade official-attempt integrity for convenience.

If the system does not know whether an official action succeeded:

Do not assume failure.
Do not assume success.
Reconcile.

This rule applies to:

starting the battle
submitting challenge results
completing the battle
saving the final result
79. Offline and Practice — Critical Rule

Practice must never become a fallback implementation of Official Battle.

Invalid pattern:

Official Battle unavailable
       ↓
Start Practice
       ↓
Call it today's Battle

This is prohibited.

80. Pending Reliability Decisions
ID	Decision	Status
ERROR-PENDING-001	Can Official Battle start offline?	Pending
ERROR-PENDING-002	Can Official Battle continue fully offline?	Pending
ERROR-PENDING-003	Can Official results be submitted offline?	Pending
ERROR-PENDING-004	Exact timer behavior during backgrounding	Pending
ERROR-PENDING-005	Exact timer behavior after process death	Pending
ERROR-PENDING-006	Gameplay Back behavior	Pending
ERROR-PENDING-007	Challenge interruption behavior	Pending
ERROR-PENDING-008	Offline Practice behavior	Pending
ERROR-PENDING-009	Offline mutation queue	Pending
ERROR-PENDING-010	Synchronization conflict resolution	Pending
ERROR-PENDING-011	Maximum automatic retry count	Pending
ERROR-PENDING-012	Retry backoff strategy	Pending
ERROR-PENDING-013	Cached Battle freshness policy	Pending
ERROR-PENDING-014	Result cache freshness	Pending
ERROR-PENDING-015	Multi-device reconciliation	Pending
ERROR-PENDING-016	Authentication recovery	Pending
ERROR-PENDING-017	Critical persistence failure UX	Pending
ERROR-PENDING-018	Background synchronization	Pending
81. QA Scenarios

The following scenarios MUST eventually be tested.

Network
launch offline
launch online
lose network while Home is visible
lose network before Battle starts
lose network during Snap
lose network during Shift
lose network during Crowd Call
lose network during result submission
network returns during active battle
server returns 500
request timeout
malformed response
Lifecycle
background during Snap
background during Shift
background during Crowd Call
process death during Snap
process death during Shift
process death during Crowd Call
process death after battle completion
screen recreation during battle
Duplicate Actions
double-tap PLAY
double-tap START
double-submit answer
double-tap Complete
repeat Add Friend
retry after timeout
Data
cached battle available
cached battle unavailable
cached result available
cached result unavailable
stale friend data
stale rival data
empty friends
failed friends request
empty history
failed history request
Recovery
recover active official session
recover after timeout
reconcile successful server mutation
recover after failed synchronization
recover after app restart
recover after network restoration
82. Critical QA Invariants

QA must verify:

Invariant 1

No duplicate official battle.

Invariant 2

No duplicate challenge result.

Invariant 3

No duplicate final result.

Invariant 4

Practice never modifies Official.

Invariant 5

Network failure never becomes an empty state.

Invariant 6

Empty data never becomes an error state.

Invariant 7

Unknown server result is reconciled before retrying a mutation.

Invariant 8

Process death never creates a new official attempt.

Invariant 9

Offline mode never fabricates authoritative server data.

Invariant 10

A failed request never silently deletes valid local state.

83. Antigravity Implementation Rules

Antigravity MUST:

preserve known state during errors
model failure states explicitly
distinguish empty from error
distinguish offline from unauthorized
protect mutation operations
use idempotency where required
reconcile unknown mutation results
persist active official battle state
prevent duplicate official attempts
prevent duplicate challenge submissions
prevent duplicate final results
test lifecycle interruptions
test network interruptions
document deviations

Antigravity MUST NOT:

reset a battle because a request failed
create a second official session after timeout
fabricate scores
fabricate percentile
treat API failure as empty data
silently switch Official to Practice
clear caches on ordinary errors
add infinite retry loops
invent offline behavior
invent timer behavior
invent recovery rules that are marked Pending
84. Technical Definition of Done

Offline/error handling is complete only when:

Network
offline state handled
timeout handled
server error handled
malformed response handled
retry behavior verified
Battle
official session survives interruption
duplicate start prevented
duplicate submission prevented
completion reconciliation implemented
result integrity verified
Lifecycle
backgrounding tested
process recreation tested
process death tested
navigation interruption tested
Data
cached state handled
stale state handled
empty/error distinction verified
synchronization verified
UX
errors are understandable
no false success
no false empty state
no destructive recovery
design-system compliant
QA
critical failure scenarios tested
critical invariants verified
implementation report completed
all deviations documented
85. Final Reliability Rule

Daily Battle must behave according to:

KNOWN SUCCESS
      ↓
Continue

KNOWN FAILURE
      ↓
Recover / Retry

UNKNOWN RESULT
      ↓
Reconcile

INVALID STATE
      ↓
Do Not Guess

OFFLINE
      ↓
Preserve State

PROCESS DEATH
      ↓
Restore State

Never:

UNKNOWN
  ↓
GUESS

Never:

FAILURE
  ↓
RESET EVERYTHING

Never:

OFFLINE
  ↓
FABRICATE DATA

The application should fail safely, transparently, and recoverably.

