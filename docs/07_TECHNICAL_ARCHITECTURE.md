# Daily Battle — Technical Architecture Specification

**Document ID:** DB-TECH-ARCH  
**Version:** 1.0  
**Status:** Implementation Architecture Contract  
**Audience:** Antigravity AI, Android implementation agents, developer reviewers, QA  
**Product:** Daily Battle  
**Primary implementation target:** Android  
**Authority:** Master Specification + approved project documentation  
**Last Updated:** 2026-09-30

---

# 1. Purpose

This document defines the technical architecture and implementation boundaries for the Daily Battle Android application.

Its purpose is to ensure that implementation remains:

- predictable
- maintainable
- testable
- traceable to product requirements
- safe against accidental feature invention
- resilient to lifecycle changes
- compatible with offline/error conditions
- suitable for incremental Antigravity implementation

This document is an implementation contract.

Antigravity MUST NOT treat this document as permission to invent product requirements.

If a technical decision required for implementation is not defined here and is not defined by another authoritative project document, it MUST be treated as a pending decision.

---

# 2. Architecture Authority

Technical implementation decisions must follow this authority order:

1. `00_MASTER_SPEC.md`
2. `01_PRODUCT.md`
3. `02_GAMEPLAY.md`
4. `03_SCREEN_ARCHITECTURE.md`
5. `04_SCREEN_BLUEPRINTS.md`
6. `05_DESIGN_SYSTEM.md`
7. `06_NAVIGATION_AND_FLOWS.md`
8. `07_TECHNICAL_ARCHITECTURE.md`
9. approved decision records in `/decisions`
10. individual implementation task specifications

Lower-level implementation instructions MUST NOT override a higher-level product requirement.

If two documents conflict:

1. stop implementation of the affected area
2. identify the conflict
3. report the conflicting requirement IDs
4. propose a resolution
5. wait for approval

Do not silently choose one.

---

# 3. Product Constraints That Architecture Must Preserve

The architecture exists to support the existing product model.

The following constraints are architectural requirements, not optional UI details.

## 3.1 Core product loop

The application must support:

```text
Open App
    ↓
Today's Battle
    ↓
3 Challenges
    ↓
Score
    ↓
Personal Result
    ↓
Rival / Friend Comparison
    ↓
Return Tomorrow

The core promise is:

3 challenges. ~3 minutes. One score.

3.2 Official Battle

The official Daily Battle is:

finite
one official attempt
composed of three challenges
shared as the official challenge set
score-producing
persistent
distinct from Practice

The architecture MUST prevent accidental creation of multiple official attempts for the same daily battle.

3.3 Practice

Practice is a separate mode.

Practice:

does not replace the official attempt
does not modify the official score
does not modify official ranking/percentile
must be distinguishable from Official Battle at the data/model level

Practice architecture must not reuse official-attempt persistence in a way that can accidentally overwrite official results.

3.4 No global leaderboard

The architecture must not introduce a global leaderboard system.

The current social competition model is centered around:

friends
rivals
personal performance
percentile
improvement

A global leaderboard must not be added as a technical convenience.

3.5 No coins/economy

The architecture must not introduce:

coins
gems
energy
lives
loot
reward currency

unless separately approved in product documentation.

3.6 No chat/social feed

The initial architecture must not require:

messaging infrastructure
real-time chat
posts
comments
likes
follower systems
infinite social feed

Friends are a competition network, not a social-media system.

3.7 AI

AI may exist as infrastructure for approved personalization or future capabilities.

AI MUST NOT become a prominent product dependency unless explicitly specified.

The client architecture must not assume that every battle requires an AI request.

The official battle must remain deterministic from the user's perspective.

4. Architectural Goals

The architecture must optimize for:

predictable behavior
clear state ownership
offline resilience
lifecycle safety
testability
incremental implementation
minimal accidental coupling
easy debugging
clear backend boundaries
future extensibility without premature complexity
5. Architecture Status

Some technical decisions are not currently locked by the product documentation.

Each decision therefore has one of these statuses:

Status	Meaning
LOCKED	Must be implemented exactly
PROPOSED	Recommended technical direction
PENDING	Requires explicit project decision
IMPLEMENTED	Already approved and implemented
DEPRECATED	Must not be used

Antigravity MUST NOT convert a PROPOSED or PENDING decision into a permanent architecture without approval.

6. Recommended Application Architecture
Status

PROPOSED

The recommended architecture is:

UI
 ↓
Presentation / Screen State
 ↓
Domain / Use Cases
 ↓
Repository Interfaces
 ↓
Data Layer
 ├── Local Data Source
 ├── Remote Data Source
 └── Cache / Synchronization

Conceptually:

┌─────────────────────────────┐
│            UI               │
│  Screens / Components       │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│      Presentation           │
│ Screen State / Events       │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│          Domain             │
│ Use Cases / Business Rules  │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│       Repository API        │
└──────────────┬──────────────┘
               ↓
       ┌───────┴────────┐
       ↓                ↓
┌─────────────┐  ┌──────────────┐
│   Local     │  │    Remote    │
│ Data Source │  │ Data Source  │
└─────────────┘  └──────────────┘

The UI must not directly access:

database APIs
network clients
remote APIs
authentication providers
raw persistence models
7. Android UI Technology
Status

PENDING

The existing product/design documents define visual behavior and screen architecture but do not establish the final Android UI framework.

Possible implementation:

Jetpack Compose
Android Views/XML

Antigravity MUST NOT treat either as locked until the project decision is recorded.

Recommended direction

Jetpack Compose is the proposed implementation direction because the product requires:

many controlled visual states
reusable components
animated game states
responsive layouts
rapid iteration

This recommendation is technical, not a product requirement.

If Compose is approved, all new UI should use Compose unless a documented exception exists.

8. Application Layer Boundaries

The application should be separated into the following logical areas.

app
├── presentation
├── domain
├── data
├── navigation
├── design system
├── core
└── testing

These are logical boundaries.

They do not automatically require separate Gradle modules.

9. Proposed Package Structure
Status

PROPOSED

If a single Android application module is used for MVP:

com.dailybattle.app

├── core
│   ├── common
│   ├── result
│   ├── time
│   ├── logging
│   └── platform
│
├── design
│   ├── theme
│   ├── components
│   ├── typography
│   ├── colors
│   ├── spacing
│   └── icons
│
├── navigation
│
├── presentation
│   ├── onboarding
│   ├── home
│   ├── battle
│   ├── snap
│   ├── shift
│   ├── crowdcall
│   ├── results
│   ├── rival
│   ├── friends
│   ├── addfriend
│   ├── profile
│   ├── history
│   └── settings
│
├── domain
│   ├── battle
│   ├── scoring
│   ├── friends
│   ├── rival
│   ├── profile
│   └── history
│
├── data
│   ├── local
│   ├── remote
│   ├── repository
│   └── mapper
│
└── testing

This structure is a proposal.

Antigravity may refine package names for technical reasons, but MUST preserve the architectural boundaries.

10. Presentation Layer

The presentation layer owns:

screen state
user events
UI rendering
transient UI effects
loading state
error presentation
navigation requests

It does NOT own core business rules.

For example:

User taps PLAY BATTLE
        ↓
Presentation event
        ↓
Domain use case
        ↓
Battle state/result
        ↓
Presentation state update
        ↓
UI

The UI must not calculate official scoring directly.

11. Screen State

Every major screen must have an explicit state model.

A conceptual state model:

Loading
Content
Empty
Error

Gameplay requires additional states:

Ready
Active
Correct
Incorrect
Complete

These states are already part of the product state system.

The implementation must represent them explicitly rather than relying on scattered booleans.

Avoid:

isLoading
isError
isComplete
isCorrect
isStarted
isFinished

when these flags can create contradictory combinations.

Prefer a state machine or sealed state model where appropriate.

12. One Source of Truth

For each piece of application state there must be one authoritative owner.

Examples:

Data	Owner
Current official battle	Battle domain/data layer
Current challenge	Battle session
Official attempt status	Battle persistence
Final score	Battle result
Friend list	Friends repository
Rival	Rival domain/data
Profile	Profile repository
History	History repository
Settings	Settings/preferences storage

The UI must not maintain an independent competing copy of persistent business state.

13. Domain Layer

The domain layer contains product rules that must remain independent from UI and storage implementation.

Examples:

StartOfficialBattle
StartPractice
SubmitSnapAnswer
SubmitShiftAnswer
SubmitCrowdPrediction
CompleteBattle
CalculateBattleResult
GetCurrentRival
GetTodayBattle
GetBattleHistory
AddFriend

Exact use-case naming may evolve.

The important requirement is that business logic does not live directly inside screen components.

14. Challenge Architecture

The three MVP challenges are:

Snap
Shift
Crowd Call

Each challenge must have:

unique challenge identity
challenge configuration
current state
answer/input state
scoring result
completion state

Conceptually:

Challenge
├── identity
├── mode
├── configuration
├── state
├── user input
├── result
└── completion

The architecture should allow challenge implementations to remain isolated.

A Snap implementation must not depend directly on Shift internals.

15. Challenge Interface
Status

PROPOSED

A common conceptual challenge contract:

Challenge
 ├── prepare()
 ├── start()
 ├── handleInput()
 ├── evaluate()
 └── complete()

The exact Android/Kotlin API must be determined during implementation.

The common contract exists to prevent each game from creating unrelated lifecycle behavior.

16. Scoring Architecture

Scoring is domain logic.

It MUST NOT be calculated inside:

Composables
Activities
Fragments
UI event handlers
animation callbacks

Conceptually:

User Input
    ↓
Challenge Evaluation
    ↓
Challenge Score
    ↓
Battle Score Aggregation
    ↓
Final Battle Result

Current score structure:

Snap          300
Shift         300
Crowd Call    300
Consistency   100
------------------
Total        1000

The exact algorithms for:

Snap score
Shift score
Crowd Call score
Consistency
final aggregation behavior

must be treated as PENDING until formally specified.

Antigravity MUST NOT invent formulas.

17. Official Attempt Integrity

The official battle requires strong state integrity.

The architecture must prevent:

Official Battle
      ↓
Start
      ↓
App killed
      ↓
Restart
      ↓
Second official attempt accidentally created

The battle session must be recoverable.

The application should persist enough information to determine:

whether today's official battle has started
whether it is in progress
which challenge is active
whether a challenge has completed
whether the battle has completed
whether the final result has already been submitted

Exact persistence implementation remains a technical decision.

18. Idempotency

Operations that may be repeated because of:

retry
lifecycle recreation
network failure
process restart
duplicate tap
duplicate callback

must be safe.

Examples:

Submit battle result
Complete official battle
Add friend
Save profile change

A repeated request must not unintentionally:

create duplicate results
create duplicate friends
consume another official attempt
overwrite a newer result with an older result
19. Data Layer

The data layer is responsible for:

local persistence
remote communication
caching
mapping
synchronization
repository implementations

The UI must never directly access the database or network layer.

20. Local Data
Status

PENDING

The application requires local persistence, but the exact persistence technology is not yet locked.

Possible implementation:

Room
DataStore
another approved Android persistence mechanism

The final choice must be recorded before implementation of persistent production data.

Conceptual local data categories
User Preferences
Battle Session
Battle Result
Challenge Result
Friends
Rival
Profile
History
Cached Remote Data

Do not create database tables/entities merely because they appear conceptually useful.

Each persisted entity must have an actual product requirement.

21. Remote Data
Status

PENDING

The current product documentation does not establish the final backend provider.

Potential backend concerns include:

user/account data
daily battle configuration
battle results
friends
rival relationships
percentile
history synchronization

Antigravity MUST NOT independently select Firebase, Supabase, custom Spring Boot, AWS, or another backend and treat that decision as approved.

The backend decision must be explicitly recorded.

22. Repository Pattern

Repositories provide the domain layer with stable interfaces.

Conceptually:

BattleRepository
FriendsRepository
ProfileRepository
HistoryRepository
SettingsRepository

Example:

Presentation
      ↓
Use Case
      ↓
BattleRepository
      ↓
Local / Remote implementation

The domain layer must not depend on:

Room
Retrofit
Firebase
Supabase
HTTP clients
Android Context

unless explicitly justified by architecture.

23. DTO / Domain / Persistence Separation

Remote API models, domain models, and persistence models should not automatically be the same class.

Conceptually:

Remote DTO
    ↓ mapper
Domain Model
    ↓ mapper
Local Entity

This prevents backend schema changes from directly changing the product/domain model.

For simple data where duplication creates no meaningful value, exceptions may be documented.

24. Networking
Status

PENDING

The network client is not currently locked.

Regardless of implementation technology, networking must support:

timeout handling
cancellation
retry where safe
structured errors
authentication where approved
response validation
offline detection
duplicate request protection

Retries MUST NOT be applied blindly to non-idempotent operations.

25. Offline Architecture

Offline behavior is a product requirement.

The application must distinguish:

No Internet
Server Error
Request Timeout
Invalid Response
Authentication Failure
Unknown Error

These must not all become:

Something went wrong.

The app should continue to function locally where the product allows it.

At minimum, the architecture must protect:

active battle session
already-completed challenge state
local result display
navigation state
user preferences

Exact server synchronization rules are pending.

26. Battle Synchronization

The official battle is conceptually:

Server / authoritative battle
        ↓
Client loads battle
        ↓
Client plays battle
        ↓
Client records result
        ↓
Result synchronization

The client must not silently generate a different official battle when remote data is unavailable unless an approved fallback specification explicitly allows it.

Official challenge identity and configuration must remain consistent.

27. Time and Daily Battle Identity

Daily Battle is date-sensitive.

The architecture must not use the device's local date as the sole source of truth for official battle identity unless explicitly approved.

Potential concerns include:

timezone changes
manual clock changes
travel
daylight saving changes
server/client disagreement

The final authoritative daily-battle date/time model is PENDING.

Until defined, Antigravity must not invent anti-cheat or timezone behavior.

28. Lifecycle Handling

Android lifecycle events must not destroy official battle state.

Relevant events include:

app background
app foreground
screen recreation
rotation where applicable
process recreation
low-memory process death
notification interruption
permission dialogs
external app launch

The user should be able to return to an interrupted battle without accidentally restarting it.

29. Backgrounding During Gameplay

The behavior must distinguish:

Temporary backgrounding
Permanent exit
Process death

The current product specification requires lifecycle-safe behavior but does not fully define timer treatment or challenge-specific resume rules.

Therefore:

Pending
whether gameplay timer pauses
whether timer continues
whether challenge expires
whether an interrupted challenge can resume
whether a challenge can be abandoned

Antigravity MUST NOT invent these rules.

30. Navigation Architecture

Navigation must follow:

06_NAVIGATION_AND_FLOWS.md

The primary navigation structure is:

Home
Battle
Friends
Me

Gameplay hides the bottom navigation.

The official battle flow is:

Battle Intro
    ↓
Snap
    ↓
Shift
    ↓
Crowd Call
    ↓
Results

Navigation implementation must not introduce alternative flows that bypass official battle rules.

31. Navigation and Business Logic Separation

Navigation should respond to domain/application state.

Do not make navigation decisions solely from visual state.

Bad:

if button clicked:
    navigate to results

Better conceptual model:

Challenge completes
        ↓
Domain confirms completion
        ↓
Presentation receives state
        ↓
Navigation event emitted
        ↓
Next screen

This prevents navigation from getting ahead of actual battle state.

32. Back Navigation

Gameplay Back behavior is currently a pending product decision.

Therefore:

do not invent a universal back behavior
do not automatically cancel official battles
do not automatically submit incomplete scores
do not automatically restart challenges

until the behavior is formally approved.

33. Dependency Injection
Status

PENDING

A dependency injection framework is not currently locked.

Possible options include:

Hilt
manual dependency injection
another approved solution

The architecture should nevertheless maintain dependency inversion regardless of framework.

Production code should not construct concrete networking/database dependencies deep inside UI classes.

34. Configuration Management

Environment-specific configuration must not be hardcoded into UI or domain code.

Examples:

API base URL
environment
feature flags
analytics configuration
logging configuration

Secrets MUST NOT be embedded in the Android application.

An Android client must be treated as inspectable.

35. Security

The application must not store:

API secrets
private backend credentials
service-account keys
signing secrets

inside source code or APK resources.

Sensitive local data should use appropriate platform security mechanisms when required.

Authentication architecture is currently pending.

36. Authentication
Status

PENDING

The current product specification does not fully define authentication.

Pending questions include:

anonymous user or account-first
sign-in providers
account creation
account recovery
device migration
logout
account deletion implementation
guest-to-account conversion

Antigravity MUST NOT invent authentication screens or providers.

37. Friends and Rival Data

The architecture should keep:

Friend relationship

separate from:

Rival selection

A friend may become a rival, but the concepts should not be technically conflated.

Current product behavior requires:

friends
today's friend scores
a current rival
score comparison
Beat Rival action

Exact rival selection algorithm is pending.

38. Profile / Battle DNA

The profile concept is:

Battle DNA

It represents personal gameplay characteristics such as:

Speed
Memory
People

The architecture must not expose these as:

IQ
psychological diagnosis
scientific measurement
medical assessment
validated intelligence score

They are product-level gameplay descriptors.

Exact calculation rules remain pending where not formally specified.

39. Percentile

Percentile is part of the product result experience.

However, the architecture must distinguish:

Personal score

from:

Percentile calculated from population/server data

The client must not fabricate percentile values.

If percentile data is unavailable, the UI should follow the approved loading/error state rather than displaying invented data.

40. Analytics
Status

PROPOSED / PENDING

Analytics should be implemented behind an abstraction.

Conceptually:

Analytics
    ↓
AnalyticsProvider

UI/domain code should emit semantic events rather than depend directly on a vendor SDK.

Example conceptual events:

battle_viewed
battle_started
challenge_started
challenge_completed
battle_completed
result_viewed
rival_viewed
friend_added
practice_started

Exact event taxonomy must be defined in:

14_ANALYTICS.md

Do not invent the final analytics schema here.

41. Logging

Logging must support debugging without leaking sensitive information.

Development builds may use more verbose logs.

Production builds should avoid:

user-sensitive information
authentication tokens
raw backend credentials
unnecessary personal data

Logs should contain enough context to diagnose:

battle state failures
navigation failures
synchronization failures
persistence failures
42. Error Model

The application should use structured application errors.

Conceptually:

AppError
├── Network
├── Timeout
├── Offline
├── Authentication
├── Authorization
├── Validation
├── Persistence
├── BattleState
├── Server
└── Unknown

The UI should map technical errors to user-appropriate messages.

Do not expose raw exception messages to users.

43. Loading Model

Loading states must be explicit.

Avoid indefinite loading.

Every remote operation should have a defined:

Loading
Success
Failure

behavior.

For screens with cached content:

Cached Content
+
Refreshing

may be preferable to blank-screen loading.

The exact UX belongs to screen specifications.

44. Duplicate Input Protection

The app must protect against repeated taps.

Examples:

PLAY BATTLE
START
SUBMIT ANSWER
BEAT RIVAL
ADD FRIEND
SHARE RESULT

A rapid repeated tap must not produce:

duplicate navigation
duplicate requests
duplicate friend relationships
duplicate score submissions
multiple battle starts

Buttons should enter appropriate loading/disabled states where required.

45. Threading / Concurrency

Long-running work must not block the main UI thread.

Potential asynchronous operations include:

network calls
database operations
synchronization
analytics delivery
expensive calculations

Gameplay interactions must remain responsive.

Challenge timing must not depend on UI rendering callbacks in a way that introduces inconsistent scoring.

46. Timer Architecture

Gameplay timers are product-critical.

Timers must use a timing mechanism appropriate for accurate elapsed-time measurement.

Do not derive elapsed time solely from:

number of UI recompositions
frame count
animation progress

The exact challenge timer/scoring rules remain pending.

47. State Restoration

Important screen and battle state should survive appropriate lifecycle events.

At minimum, implementation must consider:

Activity recreation
background/foreground
process recreation
navigation away and return
network interruption

State restoration must not create duplicate official attempts.

48. Performance Requirements

The application should maintain:

responsive navigation
smooth gameplay
stable animation
low unnecessary recomposition/re-rendering
efficient list rendering
minimal network requests
minimal battery impact

Gameplay must prioritize responsiveness over unnecessary visual effects.

Avoid heavy:

blur
continuous shaders
particle effects
large animated backgrounds
unnecessary network polling

unless explicitly approved.

49. Design System Integration

The technical implementation must consume the approved design system.

Centralize:

Colors
Typography
Spacing
Radii
Dimensions
Button styles
Input styles
Card styles
Navigation styles
Game feedback

Do not hardcode the same design token in multiple unrelated screens.

For example, this should not happen repeatedly:

Color(0xFF080B10)

inside many independent files.

Use centralized design tokens.

50. Responsive Layout

Master design size:

390 × 844

Validation targets include:

412 × 915
360 × 800

Layouts must adapt without changing the intended hierarchy.

Do not:

crop critical CTA text
clip score values
overlap system UI
hide bottom navigation incorrectly
break challenge interaction areas
51. Accessibility

Architecture must support:

semantic labels
adequate touch targets
screen-reader compatibility
readable text
non-color-only state communication
sufficient contrast
scalable content where supported

Accessibility must not be added only at the final QA phase.

It should be part of component implementation.

52. Sound and Haptics

Sound and haptic feedback should be abstracted.

Conceptually:

FeedbackController
├── playSound()
└── haptic()

The product specification defines feedback categories such as:

button tap
correct
incorrect
score reveal
personal best
battle complete

Settings must be able to control the appropriate feedback channels.

No continuous background music should be introduced unless separately approved.

53. Permissions

The app should request only permissions required by approved product functionality.

Do not add permissions for speculative features.

In particular, the current product does not require:

contacts access
location access
camera permission for the core battle loop

unless an approved future feature requires them.

54. Sharing

Result sharing exists conceptually in the product experience.

However, the exact:

Android share implementation
generated share content
image/card generation
return behavior

is pending where not defined elsewhere.

Do not create an external social integration merely because a Share button exists.

55. Notifications

Notifications are a secondary product capability.

The architecture should allow future notification integration without making the core battle dependent on notifications.

Pending:

notification permission behavior
reminder schedule
notification copy
deep-link destination
notification frequency

Do not implement speculative notification campaigns.

56. Deep Links

Deep-link behavior is currently pending.

Potential future targets include:

friend invite
battle result
incoming challenge

Until approved, do not create a public deep-link contract.

57. Feature Flags

Feature flags may be useful for controlled rollout.

However, feature flags must not become a substitute for product decisions.

Each flag must have:

owner
purpose
default value
rollout behavior
removal plan

Do not create flags for every UI property.

58. Dependency Rules

New dependencies require justification.

Before adding a dependency, Antigravity must report:

Dependency name
Version
Purpose
Why existing platform capability is insufficient
APK/build impact
Security considerations
Maintenance considerations

No dependency may be added solely because it makes implementation faster.

59. Build Configuration
Status

PENDING

The following values must be explicitly finalized:

application ID
package name
minimum SDK
target SDK
compile SDK
Kotlin version
Android Gradle Plugin version
Gradle version
build variants
signing configuration
release configuration

Antigravity must inspect the existing project before creating or changing these values.

Do not overwrite a working environment blindly.

60. Environment Structure

The recommended conceptual environments are:

Development
Testing
Production

Exact environment setup is pending backend decisions.

Development configuration must not accidentally point to production services.

61. Testing Architecture

Testing is mandatory.

The architecture should support:

Unit Tests

For:

scoring
battle state transitions
challenge evaluation
repository logic
validation
mapping
business rules
UI Tests

For:

navigation
button states
major screen states
gameplay transitions
completed battle flow
Integration Tests

For:

persistence
synchronization
repository behavior
remote/local interaction
62. Critical Business Logic Test Requirement

The following must have automated tests before being considered complete:

Official battle can start
Official battle cannot be duplicated
Challenge completion is recorded
Final score is calculated correctly
Practice does not modify official result
Repeated submit does not duplicate result
App interruption does not create duplicate attempt
Battle completion persists

Exact scoring expectations depend on the finalized gameplay specification.

63. Deterministic Testing

Gameplay logic should be testable deterministically.

Avoid business logic that depends directly on:

System.currentTimeMillis()
Random
network state
device state
UI animation timing

without an abstraction that allows controlled testing.

Random challenge generation, if used, must support deterministic test inputs.

64. Mock / Fake Data

During early implementation, fake repositories may be used.

Example:

FakeBattleRepository
FakeFriendsRepository
FakeProfileRepository

This allows the UI and domain layers to be developed before the final backend is available.

Fake data MUST be clearly marked as development/test data.

It must never silently ship as production data.

65. Seed / Demo Data

Any temporary demo content must be isolated.

Example:

DEBUG ONLY

Production builds must not depend on:

Rahul
901
914
927

or any other example values from the design specification.

Those values are illustrative UI examples unless explicitly converted into product data.

66. Architecture for Incremental Antigravity Implementation

Antigravity must implement the application in vertical slices.

Recommended order:

1. Project foundation
2. Design system
3. App shell
4. Navigation
5. Onboarding
6. Home
7. Battle session foundation
8. Snap
9. Shift
10. Crowd Call
11. Results
12. Rival
13. Friends
14. Add Friend
15. Profile
16. History
17. Settings
18. Persistence
19. Remote synchronization
20. Error/offline hardening
21. Analytics
22. Accessibility
23. QA

This is an implementation sequence proposal.

It is not permission to execute every step automatically.

Each step requires its own task specification and verification.

67. Antigravity Task Boundary

Every task must contain:

Task ID
Objective
Authoritative Documents
Requirements
Scope
Non-Goals
Files Allowed to Change
States
Interactions
Acceptance Criteria
Verification

Example:

TASK-ANDROID-001

Objective:
Create the Android project shell.

Authoritative:
07_TECHNICAL_ARCHITECTURE.md
05_DESIGN_SYSTEM.md

Scope:
- project initialization
- application entry point
- theme foundation
- basic build verification

Non-Goals:
- gameplay
- backend
- authentication
- analytics

Acceptance:
- project builds
- app launches
- no gameplay implemented
- no unauthorized dependencies
68. Files Allowed to Change

Antigravity should modify only files required for the assigned task.

If implementation requires unrelated changes:

stop
report the reason
request approval if the change affects architecture or product behavior

Do not perform opportunistic refactors during unrelated tasks.

69. Requirement Traceability

Every significant implementation must reference requirement IDs.

Example:

REQ-GAME-001
REQ-SCREEN-003
REQ-NAV-004
TECH-ARCH-012

Implementation comments should not become a substitute for documentation.

Traceability should be maintained through task reports and decision records.

70. Architecture Change Control

Any change to:

architecture
data model
navigation
persistence
backend contract
dependency strategy
scoring implementation
authentication
official battle state handling

requires a documented decision.

Use:

DEC-XXX

for approved architectural decisions.

71. Technical Deviation Log

If Antigravity cannot implement the specification exactly, create:

DEV-XXX

with:

Requirement:
Problem:
Technical reason:
Proposed deviation:
Impact:
Alternative considered:
Approval:
Status:

No silent deviations.

72. Anti-Patterns

The following are prohibited without explicit approval.

Architecture
giant single Activity containing all business logic
UI directly accessing database
UI directly calling APIs
global mutable state
hidden singleton business state
duplicated business rules
Gameplay
score calculation inside UI
timer based solely on animation frames
challenge state stored only in screen memory
duplicate official attempts
Product
global leaderboard
coins
chat
infinite feed
unapproved monetization
unapproved AI features
unapproved challenges
Implementation
giant one-shot generated application
uncontrolled dependency additions
replacing working project configuration without inspection
silently changing product requirements
73. Observability

Development builds should provide enough diagnostic information to determine:

Current screen
Current battle state
Current challenge
Official/practice mode
Persistence state
Synchronization state

This should be available through appropriate developer tooling/logging rather than visible to normal users.

74. Release Safety

Before release, verify:

no debug data
no test accounts
no development endpoints
no verbose sensitive logs
no mock repositories accidentally active
no development feature flags enabled
no placeholder UI
no unapproved permissions
no unapproved dependencies
no unresolved critical TODOs
75. Technical Definition of Done

A technical feature is complete only when:

Architecture
correct layer
correct dependency direction
no unauthorized coupling
no architecture violations
State
all required states represented
lifecycle-safe
duplicate actions protected
persistence behavior verified
UI
design-system compliant
responsive
accessible
all required states implemented
Data
local/remote behavior defined
mapping verified
errors handled
synchronization behavior tested
Testing
unit tests for business-critical logic
UI tests where required
integration tests where required
regression checks completed
Build
clean build succeeds
debug build launches
no new critical warnings
dependencies documented
Product
no invented features
no requirement drift
no silent behavior changes
Documentation
implementation report completed
deviations recorded
architecture decisions recorded
screenshots/evidence attached where appropriate
76. Implementation Report Requirement

After every implementation task, Antigravity MUST return:

Implementation Report

Task ID:
Status:

1. What was implemented
2. Files changed
3. Requirements satisfied
4. Tests added/updated
5. Build result
6. Manual verification
7. Edge cases checked
8. Dependencies added
9. Deviations
10. Known issues
11. Screenshots/evidence
12. Recommended next task

The recommended next task is informational only.

It is NOT permission to continue.

77. Pending Technical Decisions

The following decisions must be explicitly resolved before their dependent implementation begins.

ID	Decision	Status
TECH-PENDING-001	Compose vs Views	Pending
TECH-PENDING-002	Minimum Android SDK	Pending
TECH-PENDING-003	Target/Compile SDK	Pending
TECH-PENDING-004	Kotlin/AGP/Gradle versions	Pending
TECH-PENDING-005	Application ID/package	Pending
TECH-PENDING-006	Local database technology	Pending
TECH-PENDING-007	Preferences storage	Pending
TECH-PENDING-008	Backend provider/architecture	Pending
TECH-PENDING-009	Network client	Pending
TECH-PENDING-010	Dependency injection	Pending
TECH-PENDING-011	Authentication	Pending
TECH-PENDING-012	Daily Battle authoritative time/date	Pending
TECH-PENDING-013	Gameplay interruption behavior	Pending
TECH-PENDING-014	Gameplay Back behavior	Pending
TECH-PENDING-015	Exact scoring algorithms	Pending
TECH-PENDING-016	Consistency formula	Pending
TECH-PENDING-017	Percentile source/calculation	Pending
TECH-PENDING-018	Rival selection algorithm	Pending
TECH-PENDING-019	Analytics provider	Pending
TECH-PENDING-020	Notification architecture	Pending
TECH-PENDING-021	Deep-link architecture	Pending
TECH-PENDING-022	Share implementation	Pending
TECH-PENDING-023	Release/signing configuration	Pending
78. Explicit Non-Goals

This document does NOT define:

final visual screen layouts
exact gameplay algorithms
exact scoring formulas
monetization pricing
final backend API contracts
authentication UX
notification copy
deep-link UX
final analytics taxonomy
future game modes
global leaderboard
chat
social feed

Those belong to their respective specifications or decision records.

79. Architecture Principle

The Daily Battle architecture should make the following relationship explicit:

PRODUCT DECISION
       ↓
SPECIFICATION
       ↓
IMPLEMENTATION
       ↓
TEST
       ↓
VERIFICATION
       ↓
APPROVAL

Never:

AI GUESS
   ↓
IMPLEMENTATION
   ↓
PRODUCT BECOMES WHATEVER THE CODE DOES

Antigravity is an implementation system.

It is not the product decision-maker.

80. Final Architecture Rule

When requirements are clear:

Implement exactly.

When requirements are incomplete:

Stop and identify the missing decision.

When implementation is technically difficult:

Report the constraint and propose alternatives.

When a deviation is required:

Document it before implementation.

When implementation is complete:

Build, test, verify, report, and wait.

No silent invention.
No silent simplification.
No silent product changes.

81. Document Completion Criteria

07_TECHNICAL_ARCHITECTURE.md is considered complete when:

architecture boundaries are documented
product/domain/data responsibilities are separated
official/practice separation is defined
lifecycle behavior requirements are documented
offline/error requirements are documented
persistence requirements are documented
navigation architecture is referenced
testing architecture is documented
dependency rules are documented
security rules are documented
Antigravity task boundaries are documented
deviation/change-control rules are documented
unresolved technical decisions are explicitly listed
no pending technical decision has been silently converted into a product requirement

## Important point

I deliberately **did not lock** things such as Firebase, Supabase, Spring Boot, Room, Hilt, Retrofit, Compose, SDK versions, authentication, etc. into this document as facts.

Those are **technical decisions we haven't formally established yet**. The architecture document therefore marks them `PROPOSED` or `PENDING`. That prevents Antigravity from taking a reasonable-sounding assumption and turning it into permanent architecture.

The next document after this should be:

**`08_DATA_AND_API.md`**

That is where we should define the actual **Battle, Challenge, Result, User, Friend, Rival, Profile, History and API data contracts**—again separating **locked data requirements** from backend decisions that still need approval.