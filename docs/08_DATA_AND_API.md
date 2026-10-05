# Daily Battle — Data & API Specification

**Document ID:** DB-DATA-API  
**Version:** 1.0  
**Status:** Data Contract / API Architecture Specification  
**Audience:** Antigravity AI, Android implementation agents, backend developers, QA  
**Product:** Daily Battle  
**Authority:** Master Specification + approved project documentation  
**Last Updated:** 2026-09-30

---

# 1. Purpose

This document defines the data model, data ownership, persistence boundaries, API contract principles, synchronization behavior, and data integrity requirements for Daily Battle.

The goal is to establish a stable contract between:

- Android application
- local persistence
- remote backend
- battle engine
- social/friend system
- profile/history system

This document does NOT select a backend provider unless explicitly approved elsewhere.

---

# 2. Authority

Data implementation must follow:

1. `00_MASTER_SPEC.md`
2. `01_PRODUCT.md`
3. `02_GAMEPLAY.md`
4. `03_SCREEN_ARCHITECTURE.md`
5. `04_SCREEN_BLUEPRINTS.md`
6. `05_DESIGN_SYSTEM.md`
7. `06_NAVIGATION_AND_FLOWS.md`
8. `07_TECHNICAL_ARCHITECTURE.md`
9. this document
10. approved decision records

If a data requirement conflicts with a higher-level product requirement:

**STOP and report the conflict.**

Do not silently modify the product behavior.

---

# 3. Data Architecture Principles

The data architecture must follow these principles:

1. One authoritative owner for persistent data.
2. Official Battle and Practice data must remain separate.
3. UI models must not automatically become persistence models.
4. Remote DTOs must not automatically become domain models.
5. Critical operations must be idempotent.
6. Client must not fabricate authoritative server data.
7. Local persistence must protect active battle state.
8. Data synchronization must be explicit.
9. Temporary/mock data must be clearly isolated.
10. Schema changes must be documented.
11. Sensitive data must be minimized.
12. No unnecessary personal data collection.

---

# 4. Data Ownership Model

Conceptually:

```text
                     ┌────────────────────┐
                     │     UI State       │
                     └─────────┬──────────┘
                               │
                               ↓
                     ┌────────────────────┐
                     │   Domain Models    │
                     └─────────┬──────────┘
                               │
                 ┌─────────────┴─────────────┐
                 ↓                           ↓
        ┌──────────────────┐       ┌──────────────────┐
        │  Local Storage   │       │ Remote Backend   │
        └──────────────────┘       └──────────────────┘

The UI must not directly own authoritative business data.

5. Core Data Domains

The MVP data model contains these conceptual domains:

User
Battle
Challenge
Battle Session
Challenge Result
Battle Result
Friend
Rival
Profile
History
Settings

Some domains may be represented by multiple persistence models.

6. User
Purpose

Represents the application user.

Conceptual fields
User
├── userId
├── battleName
├── createdAt
├── updatedAt
└── accountStatus
Field definitions
Field	Type	Required	Authority
userId	ID	Yes	Remote/auth system
battleName	String	Yes	User/profile
createdAt	Timestamp	Yes	Backend
updatedAt	Timestamp	Yes	Backend
accountStatus	Enum	Pending	Backend

Exact authentication/account fields are pending.

Do not add:

phone number
contacts
location
email

unless an approved feature requires them.

7. Battle

A Battle represents the official daily challenge definition.

Conceptually:

Battle
├── battleId
├── battleDate
├── status
├── version
├── challenges
└── metadata
Fields
Field	Type	Required	Notes
battleId	ID	Yes	Unique battle identifier
battleDate	Date	Yes	Official battle date
status	Enum	Yes	Battle availability
version	Integer	Yes	Configuration version
challenges	List	Yes	Three official challenges
metadata	Object	Optional	Non-game-critical metadata
8. Battle Status

Conceptual values:

NOT_AVAILABLE
AVAILABLE
ACTIVE
COMPLETED

The exact backend status model is pending.

Client must not invent additional product states without requirement support.

9. Official Battle Composition

The official Battle contains exactly three challenge slots:

1. Snap
2. Shift
3. Crowd Call

The official challenge order is:

Snap
  ↓
Shift
  ↓
Crowd Call

The implementation must not randomly reorder the official challenge sequence.

10. Challenge

A Challenge represents one game inside a Battle.

Conceptually:

Challenge
├── challengeId
├── battleId
├── type
├── order
├── configuration
└── version
Fields
Field	Type	Required
challengeId	ID	Yes
battleId	ID	Yes
type	Enum	Yes
order	Integer	Yes
configuration	Object	Yes
version	Integer	Yes
11. Challenge Types

MVP challenge types:

SNAP
SHIFT
CROWD_CALL

No additional challenge type should be added without an approved product requirement.

12. Challenge Configuration

Challenge configuration represents the information required to run a challenge.

Conceptually:

ChallengeConfiguration
├── challengeType
├── parameters
├── content
└── scoringConfiguration

The exact fields are challenge-specific.

The configuration must NOT expose implementation details that are unnecessary to the client.

13. Snap Configuration

Conceptually:

SnapConfiguration
├── target
├── distractors
├── timing
└── rules

Exact fields and timing/scoring parameters remain pending until the gameplay specification defines them.

Antigravity MUST NOT invent the final Snap scoring formula.

14. Shift Configuration

Conceptually:

ShiftConfiguration
├── grid
├── initialState
├── changedState
├── answerOptions
└── rules

The exact grid dimensions, movement pattern, timing and scoring parameters are pending where not explicitly defined.

15. Crowd Call Configuration

Conceptually:

CrowdCallConfiguration
├── question
├── answerOptions
├── crowdDistribution
└── rules

The exact source and generation method for crowd-distribution data is pending.

The client must not fabricate crowd statistics and present them as real population data.

16. Battle Session

A Battle Session represents the user's current attempt.

This is distinct from the Battle definition.

Battle
     ↓
Battle Session
     ↓
Challenge Sessions
     ↓
Results

Conceptually:

BattleSession
├── sessionId
├── userId
├── battleId
├── mode
├── status
├── currentChallenge
├── startedAt
├── completedAt
└── challengeSessions
17. Battle Mode

The system must explicitly distinguish:

OFFICIAL
PRACTICE

These values must not be inferred from screen navigation.

18. Official Session Rules

An Official Battle Session:

belongs to one user
belongs to one official Battle
may have one official attempt
produces the official score
is persisted
must be recoverable
must not be duplicated

Conceptual uniqueness constraint:

(userId, battleId, OFFICIAL)

must identify one official attempt.

19. Practice Session Rules

A Practice Session:

is separate from Official
may be repeated according to approved product rules
does not modify the official result
does not replace the official Battle
does not modify official ranking/percentile

Practice data must never overwrite:

OfficialBattleResult
20. Battle Session Status

Conceptual values:

NOT_STARTED
IN_PROGRESS
COMPLETED

Potential additional technical states may exist internally, but they must not change product behavior without approval.

21. Challenge Session

A Challenge Session represents the user's active/completed interaction with one challenge.

ChallengeSession
├── challengeId
├── sessionId
├── status
├── startedAt
├── completedAt
├── input
└── result
22. Challenge Session Status

Conceptual values:

READY
ACTIVE
CORRECT
INCORRECT
COMPLETE

These states align with the existing gameplay state model.

23. User Input

User input must be represented separately from the challenge configuration.

Conceptually:

ChallengeInput
├── challengeId
├── inputValue
├── submittedAt
└── attemptMetadata

The exact structure is challenge-specific.

Do not store unnecessary raw input.

24. Challenge Result

A challenge result represents the outcome of one completed challenge.

ChallengeResult
├── challengeId
├── sessionId
├── score
├── maxScore
├── completedAt
└── metadata

Current maximum scores:

Snap        300
Shift       300
Crowd Call  300

The exact scoring algorithm is pending.

25. Battle Result

The final official result aggregates the three challenge results.

BattleResult
├── resultId
├── userId
├── battleId
├── sessionId
├── snapScore
├── shiftScore
├── crowdCallScore
├── consistencyScore
├── totalScore
├── percentile
├── completedAt
└── metadata
26. Total Score

Current product structure:

Snap          /300
Shift         /300
Crowd Call    /300
Consistency   /100
------------------
Total        /1000

Therefore:

maxTotalScore = 1000

The exact Consistency calculation is PENDING.

The client must not invent it.

27. Score Integrity

The client must not allow arbitrary modification of:

totalScore
percentile
consistencyScore

after official completion.

If the backend is authoritative for scoring, the client must treat server-confirmed values as authoritative.

28. Result Versioning

Official results should support versioning where necessary.

Conceptually:

resultId
resultVersion
createdAt
updatedAt

If the backend corrects a result, the client must not overwrite newer data with stale data.

Exact correction policy is pending.

29. Percentile

Percentile is derived from population data.

Conceptually:

Battle Result
     ↓
Population comparison
     ↓
Percentile

The client should receive percentile data from an authoritative source.

It must not estimate or fabricate percentile values.

30. Personal Best

Personal best is derived from historical official battle results.

Conceptually:

History
   ↓
Completed Official Results
   ↓
Maximum Score
   ↓
Personal Best

The same official result must not be counted multiple times.

31. Average Score

Average score is derived from eligible historical results.

Exact eligibility rules are pending.

Examples of pending questions:

Does Practice count?
Do incomplete battles count?
Does a corrected result replace the original?
Does a missing day count?

Do not invent these rules.

32. Momentum / Streak

Momentum is a product concept.

The exact persistence and calculation model is pending.

The data model should allow:

currentMomentum
previousMomentum
lastCompletedBattleDate

if required by the finalized gameplay/product specification.

Do not implement an arbitrary streak formula.

33. Friend

A Friend represents a competition relationship.

Conceptually:

Friend
├── friendshipId
├── userId
├── friendUserId
├── status
├── createdAt
└── updatedAt
34. Friend Status

Current conceptual states:

PENDING
ACCEPTED
BLOCKED

The exact friend-request direction model is pending.

35. Friend Code

The Add Friend flow uses a Battle Code.

Conceptually:

BattleCode
├── code
├── userId
└── status

The code must be:

unique within its defined scope
safe to share
validated server-side
resistant to accidental malformed input

The exact code format is pending.

36. Rival

Rival is a relationship/selection derived from the user's competition network.

Conceptually:

Rival
├── userId
├── rivalUserId
├── selectedAt
└── status

The current product requires a single current rival.

The exact rival-selection algorithm is pending.

37. Rival Score Comparison

The Rival experience may require:

RivalScoreComparison
├── userScore
├── rivalScore
├── scoreGap
├── battleId
└── direction

Example:

Rival: Rahul
You: 901
Rival: 914
Gap: 13

These numbers are illustrative UI examples and must not be treated as seed production data.

38. Profile

The Profile represents the user's personal performance identity.

Conceptually:

Profile
├── userId
├── battleName
├── momentum
├── bestScore
├── averageScore
├── battleDNA
└── records
39. Battle DNA

Battle DNA contains product-level performance descriptors.

Current conceptual dimensions:

Speed
Memory
People

These are gameplay descriptors.

They must NOT be stored or represented as:

IQ
medical metrics
psychological diagnoses
scientific intelligence measurements

Exact calculation formulas:
Speed = average of all authoritative official Snap scores (sum(SnapScore) / N)
Memory = average of all authoritative official Shift scores (sum(ShiftScore) / N)
People = average of all authoritative official Crowd Call scores (sum(CrowdCallScore) / N)

Only completed official battles are included. Exclude practice, incomplete, and abandoned battles. Ensure dimensions are calculated independently with equal weight. Initial state: unavailable.

40. History

History contains the user's past official battles.

Conceptually:

BattleHistoryItem
├── battleId
├── date
├── score
├── percentile
├── personalBest
└── status

Practice results should not be mixed into official history unless explicitly approved.

41. Settings

Settings are primarily local preferences.

Conceptually:

Settings
├── soundEnabled
├── hapticsEnabled
├── notificationsEnabled
└── otherApprovedPreferences

Exact settings list follows the approved Settings specification.

Do not add theme switching in MVP if the product specification excludes it.

42. Timestamps

All persisted timestamps should use an unambiguous representation.

Conceptually:

createdAt
updatedAt
startedAt
completedAt

Remote timestamps should have a clearly defined timezone/format.

The final server time strategy remains pending.

43. IDs

Every persistent entity requiring independent identity should have a stable ID.

Examples:

userId
battleId
challengeId
sessionId
resultId
friendshipId

IDs must not depend on UI position.

For example:

"challenge_1"

should not be the sole identity of a challenge merely because it appears first.

44. Local Entity vs Domain Model

The implementation should conceptually separate:

Remote DTO
Local Entity
Domain Model
UI Model

Example:

BattleResponseDto
      ↓
BattleEntity
      ↓
Battle
      ↓
BattleUiState

This prevents backend/schema changes from directly leaking into UI.

45. API Architecture
Status

PENDING

The final API style/provider is not yet locked.

Possible implementations include:

REST
GraphQL
another approved API architecture

This document defines the required logical operations independent of transport.

46. Logical API Operations

The system will require operations conceptually equivalent to:

Battle
Get Today's Battle
Get Battle
Start Official Battle
Get Active Battle Session
Submit Challenge Result
Complete Battle
Get Battle Result
47. Battle API Contract

Conceptually:

GET /battle/today

Response:

{
  battleId,
  battleDate,
  status,
  version,
  challenges[]
}

This is a logical contract example.

The final endpoint path and HTTP implementation are pending.

48. Start Official Battle

Logical operation:

StartOfficialBattle

Input:

userId
battleId
clientRequestId

Output:

sessionId
battleId
mode
status
currentChallenge
startedAt

The operation must be idempotent.

Repeated requests for the same official battle must not create duplicate official sessions.

49. Submit Challenge Result

Logical operation:

SubmitChallengeResult

Input concept:

sessionId
challengeId
answer/input
clientRequestId
completedAt

Output concept:

challengeResult
nextChallenge
sessionStatus

The exact request payload depends on the finalized challenge implementation.

50. Complete Battle

Logical operation:

CompleteBattle

Input:

sessionId
clientRequestId

Output:

battleResult

The backend must verify that:

session exists
session belongs to user
correct battle
required challenges completed
session has not already been finalized
51. Get Result

Logical operation:

GetBattleResult

Input:

battleId
userId

Output concept:

BattleResult
52. Friends API

Logical operations:

GetFriends
GetFriend
SendFriendRequest
AcceptFriendRequest
RejectFriendRequest
BlockFriend

Exact API implementation is pending.

53. Add Friend

Logical operation:

AddFriendByBattleCode

Input:

battleCode
clientRequestId

Output:

friendship
status

The backend must validate the Battle Code.

54. Rival API

Logical operations:

GetCurrentRival
SetRival
GetRivalComparison

The exact selection algorithm is pending.

55. Profile API

Logical operations:

GetProfile
GetBattleDNA
GetPersonalRecords

Exact calculation ownership is pending.

56. History API

Logical operations:

GetBattleHistory
GetBattleHistoryItem

The server should return authoritative historical result data where applicable.

57. Request Idempotency

Operations that can create or mutate data should support a request identity.

Conceptually:

clientRequestId

This is especially important for:

StartOfficialBattle
SubmitChallengeResult
CompleteBattle
SendFriendRequest
AddFriend

A retry with the same request identity must not create duplicate data.

58. API Error Contract

The backend should return structured errors.

Conceptually:

{
  code,
  message,
  details,
  requestId
}

Example error codes:

BATTLE_NOT_AVAILABLE
BATTLE_ALREADY_STARTED
BATTLE_ALREADY_COMPLETED
SESSION_NOT_FOUND
CHALLENGE_NOT_FOUND
INVALID_CHALLENGE_STATE
RESULT_ALREADY_SUBMITTED
FRIEND_CODE_INVALID
FRIEND_ALREADY_EXISTS
RATE_LIMITED
UNAUTHORIZED
FORBIDDEN
SERVER_ERROR

Exact error vocabulary is pending backend design.

59. Client Error Mapping

The Android client must map backend errors into product-level states.

Example:

BATTLE_ALREADY_COMPLETED
        ↓
Completed Battle State

rather than:

Unknown Error

when a meaningful product state is known.

60. Synchronization Strategy

The architecture must distinguish:

Local-only data
Server-authoritative data
Cached server data
Pending synchronization

Example:

Data	Expected authority
Sound preference	Local
Haptic preference	Local
Active official session	Shared / authoritative
Official result	Server
Friend relationship	Server
Rival	Server
Cached history	Server
Temporary UI state	Local/UI

Final authority must be confirmed for each domain during backend design.

61. Offline Queue

If offline mutation support is approved, mutations may require a local queue.

Conceptually:

PendingOperation
├── operationId
├── operationType
├── payload
├── createdAt
├── retryCount
└── status

However, offline mutation behavior for official score submission is currently pending.

Do not implement a generic offline queue merely because it appears architecturally useful.

62. Cache Policy

Caching must have an explicit reason.

Potential cacheable data:

today's Battle
completed result
profile
friends
history

The cache must have:

freshness rules
invalidation rules
synchronization behavior

These rules are pending where not defined.

63. Data Validation

Validation must occur at multiple boundaries.

UI

Basic input validation.

Domain

Business-rule validation.

Backend

Authoritative validation.

Never assume UI validation alone is sufficient.

64. Battle Validation

Before starting or continuing an official battle, validate:

battle exists
battle is available
battle belongs to expected date/version
session state is valid
challenge sequence is valid

Exact date/version validation is subject to final backend rules.

65. Result Validation

Before accepting a final result:

all required challenges complete
scores within valid range
session belongs to user
session belongs to battle
session not already finalized

The server must be treated as authoritative for final official results.

66. Score Range Validation

Current maximum challenge scores:

Snap        300
Shift       300
Crowd Call  300

Consistency:

100

Total:

1000

Therefore the system must reject impossible values outside the finalized scoring model.

Exact server-side anti-cheat validation remains pending.

67. Anti-Cheat Boundary

The client must not be treated as inherently trustworthy for authoritative scores.

Potential server validation may include:

valid session
valid challenge
valid sequence
valid submission
valid score range
duplicate submission protection

The exact anti-cheat strategy is pending.

Do not invent an invasive anti-cheat system.

68. Data Privacy

Only data necessary for approved product functionality should be collected.

Avoid collecting:

contacts
precise location
unnecessary device identifiers
unrelated personal information

unless a documented feature requires it.

69. Account Deletion

The product includes account/privacy controls.

The final account deletion architecture is pending.

Where supported, deletion must address:

account identity
profile data
friend relationships
battle history
server data
locally cached data

The exact retention requirements must be defined by the backend/privacy specification.

70. Schema Versioning

Persistent schemas must be versioned.

A schema change must include:

version
migration
compatibility consideration
test
rollback consideration

Do not modify production schema assumptions without migration planning.

71. API Versioning

If the backend uses versioned APIs, the version must be explicit.

Example:

/api/v1/...

The actual API versioning strategy is pending.

72. Pagination

History and Friends may eventually contain many records.

If backend responses can grow beyond practical limits, pagination should be supported.

Conceptually:

page
pageSize
cursor
nextCursor

The exact pagination mechanism is pending.

The client must not assume the entire history is always returned in one request.

73. Ordering

Where ordering matters, it must be explicit.

Examples:

Challenge order
History chronological order
Friend list order

Do not rely on backend/database incidental ordering.

Official challenge order is fixed:

Snap
Shift
Crowd Call
74. Concurrency

The system must handle cases where the same user/session is modified from:

app retry
another device
duplicate network request
stale client state

The backend must be authoritative for conflict resolution.

The exact multi-device policy is pending.

75. Data Freshness

The UI should know whether data is:

Fresh
Cached
Refreshing
Unavailable

The product should not display stale information as unquestionably current where freshness materially matters.

This is especially relevant to:

today's battle
rival score
friends' today's scores
percentile
76. Development Data

Development data must be isolated from production data.

Examples:

DEV
STAGING
PRODUCTION

The final environment architecture is pending.

No development user or fake score should be shipped unintentionally.

77. Example Domain Object

Illustrative only:

BattleResult(
    resultId = "...",
    userId = "...",
    battleId = "...",
    sessionId = "...",
    snapScore = 287,
    shiftScore = 252,
    crowdCallScore = 271,
    consistencyScore = 91,
    totalScore = 901,
    percentile = 9,
    completedAt = "..."
)

The values above are examples for structure only.

They are NOT production seed data.

78. Data Contract Rules for Antigravity

Antigravity MUST:

use stable IDs
preserve official/practice separation
preserve challenge ordering
preserve score boundaries
avoid duplicate official sessions
use repository boundaries
separate DTO/domain/entity models where appropriate
handle loading/error states
handle offline conditions
validate inputs
protect mutation operations from duplicate requests
document schema changes

Antigravity MUST NOT:

invent backend endpoints as final contracts
invent scoring formulas
invent percentile calculations
invent rival algorithms
invent authentication
invent database technology
invent server-side anti-cheat
invent new entities without requirement support
79. Pending Data Decisions
ID	Decision	Status
DATA-PENDING-001	Backend provider	Pending
DATA-PENDING-002	API style	Pending
DATA-PENDING-003	Authentication model	Pending
DATA-PENDING-004	User/account schema	Pending
DATA-PENDING-005	Battle date/time authority	Pending
DATA-PENDING-006	Battle configuration source	Pending
DATA-PENDING-007	Exact challenge configuration schemas	Pending
DATA-PENDING-008	Exact scoring formulas	Pending
DATA-PENDING-009	Consistency calculation	Pending
DATA-PENDING-010	Percentile calculation/source	Pending
DATA-PENDING-011	Momentum/streak calculation	Pending
DATA-PENDING-012	Battle DNA calculation	Resolved
DATA-PENDING-013	Rival selection algorithm	Pending
DATA-PENDING-014	Friend-code format	Pending
DATA-PENDING-015	Offline result submission	Pending
DATA-PENDING-016	Synchronization conflict strategy	Pending
DATA-PENDING-017	Multi-device behavior	Pending
DATA-PENDING-018	Account deletion/retention	Pending
DATA-PENDING-019	API versioning	Pending
DATA-PENDING-020	Pagination strategy	Pending
DATA-PENDING-021	Anti-cheat strategy	Pending
80. Relationship Diagram

Conceptually:

USER
 │
 ├───────────────┐
 │               │
 ↓               ↓
PROFILE        FRIENDSHIPS
 │               │
 │               ↓
 │             RIVAL
 │
 ↓
BATTLE HISTORY
 │
 ↓
BATTLE RESULT
 │
 └────── BATTLE SESSION
              │
              ├── SNAP
              ├── SHIFT
              └── CROWD CALL

Battle definition:

BATTLE
 ├── SNAP
 ├── SHIFT
 └── CROWD CALL

User attempt:

USER
 ↓
BATTLE SESSION
 ↓
CHALLENGE SESSIONS
 ↓
CHALLENGE RESULTS
 ↓
BATTLE RESULT
81. Data Integrity Rules

The following invariants must always hold:

Invariant 1

One user cannot have multiple official attempts for the same Battle.

Invariant 2

Practice cannot overwrite Official.

Invariant 3

A challenge result must belong to a valid session.

Invariant 4

A session must belong to a valid Battle.

Invariant 5

A completed Battle must contain all required challenge results.

Invariant 6

A final result must belong to the correct user.

Invariant 7

Challenge ordering must remain:

Snap → Shift → Crowd Call
Invariant 8

Official total score cannot exceed 1000.

Invariant 9

Duplicate requests must not create duplicate business records.

Invariant 10

Client-generated data must not override authoritative server data.

82. Data Definition of Done

The data implementation is complete only when:

all required entities are defined
ownership is clear
IDs are stable
official/practice separation is enforced
repository boundaries are respected
local persistence is tested
remote contracts are tested where implemented
loading/error/offline behavior exists
duplicate mutation protection exists
official attempt integrity is verified
schema migrations are tested
mock data is isolated
no production code depends on placeholder data
no unapproved data collection exists
all pending decisions are documented
implementation report is complete
83. Final Data Rule

The data layer must preserve this principle:

Product Requirement
       ↓
Data Contract
       ↓
Domain Rule
       ↓
Persistence / API
       ↓
UI

Never reverse this relationship:

Backend convenience
       ↓
Invented product behavior

The backend exists to support the product.

The data model must not silently redefine the product.


### Important architectural boundary

At this point, we have a clean separation:

| Document | Main responsibility |
|---|---|
| `02_GAMEPLAY.md` | **What the game does** |
| `03_SCREEN_ARCHITECTURE.md` | **What screens exist** |
| `04_SCREEN_BLUEPRINTS.md` | **What each screen contains** |
| `05_DESIGN_SYSTEM.md` | **How it looks** |
| `06_NAVIGATION_AND_FLOWS.md` | **How users move through it** |
| `07_TECHNICAL_ARCHITECTURE.md` | **How Android should be structured** |
| `08_DATA_AND_API.md` | **What data exists and how it moves** |

The next document should therefore be **`09_OFFLINE_AND_ERROR_HANDLING.md`**. That one should go much deeper