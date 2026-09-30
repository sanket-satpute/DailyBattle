# Daily Battle — Complete Development Roadmap

**Document:** `17_DEVELOPMENT_ROADMAP.md`  
**Product:** Daily Battle  
**Status:** AUTHORITATIVE DEVELOPMENT ROADMAP  
**Scope:** Software development only  
**Purpose:** Define the exact engineering sequence for implementing, integrating, testing, hardening, and preparing the Daily Battle Android application for release.

---

# 1. DOCUMENT PURPOSE

This document defines the complete engineering roadmap for Daily Battle.

It answers:

> What do we implement first, what comes next, what depends on what, where do edge cases get handled, when do we integrate each subsystem, and when is the software technically complete?

This document covers:

- Android application development
- application architecture
- UI architecture
- design-system implementation
- navigation
- state management
- gameplay architecture
- challenge implementation
- scoring
- results
- local persistence
- remote APIs
- synchronization
- offline behavior
- lifecycle recovery
- error handling
- accessibility implementation
- animations
- haptics
- sound
- analytics
- monetization implementation
- security
- performance
- automated testing
- integration testing
- UI testing
- end-to-end testing
- edge-case testing
- release engineering

This document does NOT define:

- product strategy
- market strategy
- marketing
- business strategy
- growth strategy
- monetization strategy itself
- visual design decisions
- gameplay rules themselves
- API contracts themselves
- architectural rules themselves

Those remain defined in the appropriate authoritative documents.

---

# 2. AUTHORITATIVE DOCUMENTS

Development must follow:

```text
00_MASTER_SPEC.md
01_PRODUCT.md
02_GAMEPLAY.md
03_SCREEN_ARCHITECTURE.md
04_SCREEN_BLUEPRINTS.md
05_DESIGN_SYSTEM.md
06_NAVIGATION_AND_FLOWS.md
07_TECHNICAL_ARCHITECTURE.md
08_DATA_AND_API.md
09_OFFLINE_AND_ERROR_HANDLING.md
10_ANIMATION_HAPTICS.md
11_ACCESSIBILITY.md
12_TESTING_AND_QA.md
13_MONETIZATION.md
14_ANALYTICS.md
15_ANTIGRAVITY_RULES.md
16_DEFINITION_OF_DONE.md
17_DEVELOPMENT_ROADMAP.md

This roadmap determines implementation order.

The other documents determine implementation requirements.

3. CORE DEVELOPMENT PRINCIPLE

Daily Battle must be developed incrementally.

Never attempt:

"Build the entire application."

Instead:

ARCHITECTURE
    ↓
FOUNDATION
    ↓
APP SHELL
    ↓
CORE DOMAIN
    ↓
UI
    ↓
GAMEPLAY
    ↓
DATA
    ↓
INTEGRATION
    ↓
FAILURE HANDLING
    ↓
TESTING
    ↓
HARDENING

Every sprint must produce a testable software increment.

4. DEVELOPMENT PHASE MODEL

The complete development sequence is:

PHASE 0
Development Environment

        ↓

PHASE 1
Architecture Foundation

        ↓

PHASE 2
Android Application Foundation

        ↓

PHASE 3
Design System Implementation

        ↓

PHASE 4
Application Shell + Navigation

        ↓

PHASE 5
Core Domain + State Architecture

        ↓

PHASE 6
Onboarding + Home

        ↓

PHASE 7
Official Battle Engine

        ↓

PHASE 8
Snap

        ↓

PHASE 9
Shift

        ↓

PHASE 10
Crowd Call

        ↓

PHASE 11
Scoring Engine

        ↓

PHASE 12
Results

        ↓

PHASE 13
Rival + Friends

        ↓

PHASE 14
Profile + History + Settings

        ↓

PHASE 15
Persistence

        ↓

PHASE 16
Backend + API Integration

        ↓

PHASE 17
Synchronization + Offline

        ↓

PHASE 18
Lifecycle + Recovery

        ↓

PHASE 19
Practice Mode

        ↓

PHASE 20
Analytics

        ↓

PHASE 21
Monetization Infrastructure

        ↓

PHASE 22
Accessibility + Interaction Hardening

        ↓

PHASE 23
Comprehensive Testing

        ↓

PHASE 24
Performance + Security + Stability

        ↓

PHASE 25
Release Hardening

        ↓

PHASE 26
Release Build
5. SPRINT EXECUTION MODEL

Every sprint follows:

READ
 ↓
PLAN
 ↓
IMPLEMENT
 ↓
BUILD
 ↓
UNIT TEST
 ↓
INTEGRATION TEST
 ↓
MANUAL TEST
 ↓
EDGE-CASE TEST
 ↓
FIX
 ↓
AUDIT
 ↓
CHECKPOINT

A sprint is not complete merely because the application compiles.

PHASE 0 — DEVELOPMENT ENVIRONMENT
Objective

Prepare the engineering environment required to develop Daily Battle reliably.

Sprint 0.1 — Repository Development Structure

Implement the development repository structure:

DailyBattle/
├── docs/
├── decisions/
├── audits/
└── android/

The Android project will live under:

android/
Sprint 0.2 — Android Development Environment

Verify:

Android SDK
Gradle
JDK
Android Studio compatibility
emulator/device connectivity
debug build
test execution
Sprint 0.3 — Git Development Baseline

Create the initial development checkpoint.

Required baseline:

repository exists
documentation exists
Android environment verified
Git baseline created
Edge Cases

Verify:

clean clone
fresh environment
missing SDK
incorrect JDK
Gradle cache unavailable
emulator unavailable
physical device unavailable
offline Gradle build where dependencies are cached
PHASE 1 — ARCHITECTURE FOUNDATION
Objective

Implement the structural architecture before feature development.

The existing architecture specification establishes layered responsibilities and emphasizes preventing architectural drift.

Sprint 1.1 — Application Module Structure

Implement approved package/module boundaries.

Conceptually:

UI
 ↓
Presentation
 ↓
Domain
 ↓
Repository
 ↓
Data
Sprint 1.2 — Domain Boundary

Create domain models and use-case boundaries required by the MVP.

Examples:

User
Battle
BattleSession
Challenge
ChallengeResult
BattleResult
Friend
Rival
Profile
History
Settings

Do not prematurely implement unused abstractions.

Sprint 1.3 — Presentation State Architecture

Establish:

UI State
UI Events
Domain Results
One-shot Events
Loading
Error
Empty
Success

Avoid scattered mutable state.

Sprint 1.4 — Error Architecture

Create a consistent error model.

Conceptually:

Validation
Domain
Network
Authentication
Authorization
Persistence
Server
Timeout
Unknown

Errors must not silently disappear.

Sprint 1.5 — Dependency Injection / Composition

Implement the approved dependency boundary.

Verify:

testability
fake repositories
mock services
environment configuration
no UI-created network clients
no domain dependency on Android UI
Edge Cases

Test:

invalid state
null/missing data
repository failure
malformed response
dependency unavailable
initialization failure
repeated initialization
lifecycle recreation
PHASE 2 — ANDROID APPLICATION FOUNDATION
Objective

Create a clean, buildable Daily Battle Android application.

Sprint 2.1 — Android Project

Implement:

application module
manifest
Gradle configuration
application entry point
resources
themes
approved SDK versions
Sprint 2.2 — App Startup

Implement:

Application startup
 ↓
Dependency initialization
 ↓
Navigation initialization
 ↓
Initial state
Sprint 2.3 — Environment Configuration

Separate:

Development
Testing
Production

Do not hardcode:

API URLs
secrets
credentials
production keys
Sprint 2.4 — Base Test Infrastructure

Establish:

unit tests
integration tests
UI tests
test fixtures
fake repositories
test data factories
Edge Cases

Test:

cold start
warm start
process restart
first launch
corrupted local configuration
missing configuration
debug vs release configuration
PHASE 3 — DESIGN SYSTEM IMPLEMENTATION
Objective

Turn the approved design system into reusable Android components.

Sprint 3.1 — Color Tokens

Implement the approved color system.

Background
Surface
Elevated
Text
Brand
Semantic
Challenge accents
Sprint 3.2 — Typography

Implement:

Inter
display
headings
body
caption
labels
tabular numerals
Sprint 3.3 — Spacing / Radius / Sizing

Implement:

4
8
12
16
20
24
32
40
48
56
64

and approved:

screen margins
touch targets
button height
input height
radius system
Sprint 3.4 — Buttons

Implement:

Default
Pressed
Disabled
Loading
Success
Sprint 3.5 — Inputs

Implement:

text input
battle code input
validation
focused state
error state
disabled state
Sprint 3.6 — Cards / Surfaces

Implement reusable:

standard card
elevated card
challenge card
result card
rival card
friend row
Sprint 3.7 — Navigation Components

Implement:

top bar
bottom navigation
navigation states
selected/unselected states
Sprint 3.8 — Feedback Components

Implement:

loading
empty
error
retry
success
inline validation
Edge Cases

Test:

long text
small screen
large text
disabled interaction
rapid tapping
accessibility semantics
content overflow
component recreation
PHASE 4 — APPLICATION SHELL + NAVIGATION
Objective

Create the navigational structure of Daily Battle.

Sprint 4.1 — Root Navigation

Implement:

Home
Battle
Friends
Me
Sprint 4.2 — Screen Routes

Register all approved screens.

SCR-001 Welcome
SCR-002 Battle Name
SCR-003 Home
SCR-004 Battle Intro
SCR-005 Snap
SCR-006 Shift
SCR-007 Crowd Call
SCR-008 Results
SCR-009 Rival
SCR-010 Friends
SCR-011 Add Friend
SCR-012 Profile
SCR-013 History
Sprint 4.3 — Navigation State

Handle:

selected tab
nested navigation
back stack
return navigation
screen restoration
Sprint 4.4 — Gameplay Navigation Boundary

Implement:

Normal app
→ bottom navigation visible

Gameplay
→ bottom navigation hidden
Edge Cases

Test:

repeated navigation
rapid navigation
Back
system Back
process recreation
deep navigation stack
invalid route
returning from gameplay
returning from Results
navigating to a screen twice
PHASE 5 — CORE DOMAIN + STATE ARCHITECTURE
Objective

Implement the domain logic before deeply coupling it to UI.

Sprint 5.1 — User State

Implement:

user identity
battle name
settings state
account state
Sprint 5.2 — Battle Domain

Implement:

Battle
Battle Session
Challenge
Challenge Result
Battle Result
Sprint 5.3 — Battle State Machine

Implement:

NOT_STARTED
 ↓
READY
 ↓
ACTIVE
 ↓
CHALLENGE_COMPLETE
 ↓
NEXT_CHALLENGE
 ↓
BATTLE_COMPLETE
 ↓
RESULTS
Sprint 5.4 — Challenge State Machine

Implement:

READY
ACTIVE
CORRECT
INCORRECT
COMPLETE
Sprint 5.5 — Attempt Integrity

Implement protections against:

duplicate official attempts
duplicate challenge submissions
illegal challenge progression
skipped challenge
replaying completed official challenge
modifying completed results
Edge Cases

Test:

duplicate event
event arrives out of order
state restored from disk
process killed during state transition
invalid state transition
repeated completion
completion after timeout
stale state
simultaneous state events
PHASE 6 — ONBOARDING + HOME
Objective

Build the first complete user-facing product flow.

Sprint 6.1 — Welcome

Implement SCR-001.

Sprint 6.2 — Battle Name

Implement SCR-002.

Handle:

empty
too short
too long
invalid characters
whitespace
keyboard
submission
persistence
Sprint 6.3 — Home

Implement SCR-003.

Primary hierarchy:

Greeting
Momentum
Today's Battle
Rival
Sprint 6.4 — Home Completed State

Implement:

Today's Result
Score
Percentile
Next Battle
Edge Cases

Home:

no battle available
battle loading
stale battle
battle already completed
no rival
no friends
missing profile
offline
server error
first-time user
returning user
corrupted local state
PHASE 7 — OFFICIAL BATTLE ENGINE
Objective

Create the production battle runtime before implementing individual games.

Sprint 7.1 — Battle Initialization

Implement:

Home
 ↓
Battle Intro
 ↓
Initialize Official Battle
Sprint 7.2 — Battle Session Lifecycle

Handle:

Created
Ready
Started
Challenge Active
Challenge Complete
Battle Complete
Submitted
Sprint 7.3 — Timer Infrastructure

Implement timer abstraction.

Must handle:

pause
resume
background
process recreation
timeout
system clock changes where relevant
Sprint 7.4 — Challenge Sequencing

Enforce:

Snap
 ↓
Shift
 ↓
Crowd Call

No arbitrary ordering.

Sprint 7.5 — Battle Completion

Implement:

Final challenge complete
 ↓
Aggregate results
 ↓
Finalize battle
 ↓
Navigate to Results
Edge Cases

Critical:

User presses Start twice
User presses answer twice
User backgrounds app
User locks phone
Process killed
Network disappears
Timer reaches zero
Submission times out
Submission succeeds but response is lost
User presses Back
User opens another screen

Every case must have deterministic behavior.

PHASE 8 — SNAP
Objective

Implement the first official challenge.

Sprint 8.1 — Snap Game Domain

Implement:

challenge generation/input
reaction state
result state
completion rules
Sprint 8.2 — Snap UI

Implement:

game area
target
distractor
instruction
progress
timer where applicable
Sprint 8.3 — Snap Feedback

Implement:

correct pulse
incorrect shake
haptic
sound
transition
Sprint 8.4 — Snap Scoring Input

Produce a raw challenge result.

Do not place authoritative scoring calculations inside the UI.

Snap Edge Cases

Test:

Target appears
Target does not appear
Very fast tap
Very slow tap
Tap before target
Multiple taps
Tap outside target
Incorrect target
Timeout
Background during active round
Screen recreation
Double completion
PHASE 9 — SHIFT
Objective

Implement the second official challenge.

Sprint 9.1 — Shift Domain

Implement:

grid state
movement/change state
answer state
completion rules
Sprint 9.2 — Shift UI

Implement:

grid
instruction
answer options
progress
timer if applicable
Sprint 9.3 — Shift Feedback

Implement:

correct
incorrect
selection
completion
haptic
sound
Sprint 9.4 — Shift Result

Produce the challenge result for the scoring layer.

Shift Edge Cases

Test:

No movement
Multiple movement
Ambiguous movement
Rapid answer
Repeated answer
Wrong answer
Timeout
Background
Process death
State restoration
Invalid challenge data
PHASE 10 — CROWD CALL
Objective

Implement the third official challenge.

Sprint 10.1 — Crowd Domain

Implement:

question
choices
user prediction
crowd distribution
prediction result
Sprint 10.2 — Crowd UI

Implement:

question
answer cards
selection state
result distribution
prediction outcome
Sprint 10.3 — Crowd Result

Generate challenge result.

Sprint 10.4 — Crowd Feedback

Implement:

selection feedback
result animation
haptics
sound
completion transition
Crowd Edge Cases

Test:

No answer selected
Multiple answers
Repeated tap
Network unavailable
Crowd data unavailable
Malformed distribution
Zero distribution
Invalid distribution
Timeout
Background
Process death
Result already submitted
PHASE 11 — SCORING ENGINE
Objective

Create a centralized authoritative scoring system.

Sprint 11.1 — Challenge Score Contracts

Create score outputs for:

Snap
Shift
Crowd Call
Sprint 11.2 — Challenge Scoring

Implement only approved formulas.

Current structure:

Snap        300
Shift       300
Crowd Call  300
Consistency 100
Total      1000

Undefined formulas must not be invented.

Sprint 11.3 — Consistency

Implement the approved Consistency calculation once its formula is defined.

Sprint 11.4 — Battle Score Aggregation

Implement:

Challenge scores
 ↓
Consistency
 ↓
Total
Sprint 11.5 — Derived Metrics

Implement approved calculations for:

percentile
personal best
average
momentum
Battle DNA
rival gap

Only after their definitions are finalized.

Scoring Edge Cases

Test:

0 score
Maximum score
Negative input
Missing challenge score
Duplicate challenge score
Invalid challenge score
Score overflow
Floating point issue
Rounding
Tie
Personal best
Non-personal best
Incomplete battle
Practice battle
Official battle
PHASE 12 — RESULTS
Objective

Implement the complete result experience.

Sprint 12.1 — Result Model

Implement:

final score
breakdown
percentile
improvement
personal best
rival comparison
Sprint 12.2 — Results UI

Implement SCR-008.

Sprint 12.3 — Score Reveal

Implement:

Score
 ↓
Percentile
 ↓
Improvement
 ↓
Rival
Sprint 12.4 — Result Persistence

Persist official result safely.

Sprint 12.5 — Result Recovery

If result submission is interrupted:

Unknown
 ↓
Recover
 ↓
Reconcile
 ↓
Show final authoritative result

Do not create a second official result.

Results Edge Cases

Test:

zero score
maximum score
no percentile
no rival
personal best
tied score
stale rival score
offline result
duplicate result
result submission timeout
process death during reveal
reopening Results
completed battle reopened
incomplete result data
PHASE 13 — RIVAL + FRIENDS
Objective

Implement the competitive network.

Sprint 13.1 — Rival Domain

Implement:

current rival
scores
gap
match history
Sprint 13.2 — Rival UI

Implement SCR-009.

Sprint 13.3 — Friends Domain

Implement:

friend identity
relationship state
friend scores
pending state
accepted state
blocked state
Sprint 13.4 — Friends UI

Implement SCR-010.

Sprint 13.5 — Add Friend

Implement SCR-011.

Sprint 13.6 — Friend Code

Implement:

generate/display code
copy
enter code
validation
submission
success
failure
Competition Edge Cases
No friends
Friend already exists
Friend request pending
Friend request rejected
Invalid code
Expired code
Own code
Duplicate request
Blocked user
Friend removed
Rival removed
Rival unavailable
Rival score unavailable
Stale friend score
Offline
Server timeout
PHASE 14 — PROFILE + HISTORY + SETTINGS
Sprint 14.1 — Profile

Implement SCR-012.

Sprint 14.2 — Battle DNA

Implement:

Speed
Memory
People

from authoritative result data.

Sprint 14.3 — History

Implement SCR-013.

Sprint 14.4 — History Data

Implement:

best
average
momentum
recent battles
graph data
Sprint 14.5 — Settings

Implement:

sound
haptics
notifications
battle name
battle code
privacy
account deletion
terms
about
Edge Cases

Profile:

no results
incomplete profile
missing metrics
zero battles
corrupt historical data

History:

no history
one battle
many battles
missing dates
duplicate records
out-of-order records

Settings:

persistence failure
invalid update
repeated update
account deletion failure
account deletion retry
notification permission unavailable
PHASE 15 — LOCAL PERSISTENCE
Objective

Introduce durable local storage.

Sprint 15.1 — Persistence Models

Implement local representations for required entities.

Sprint 15.2 — Database / Storage Layer

Implement approved storage technology.

Sprint 15.3 — Repository Integration

Connect domain repositories to local storage.

Sprint 15.4 — Migrations

Implement schema migration strategy.

Sprint 15.5 — Corruption Handling

Handle:

malformed records
missing fields
incompatible schema
failed migration
partial write
database failure
Persistence Edge Cases
First install
Upgrade
Downgrade if supported
Migration failure
Partial data
Duplicate data
Corrupt record
Storage unavailable
Low storage
Process killed during write
Concurrent writes
Repeated writes
PHASE 16 — BACKEND + API INTEGRATION
Objective

Connect the Android application to approved remote services.

Sprint 16.1 — Network Client

Implement:

HTTP client
serialization
timeout
headers
authentication integration
error mapping
Sprint 16.2 — Battle APIs

Implement approved operations for:

retrieving today's battle
starting official battle
submitting challenge results
completing battle
retrieving result
Sprint 16.3 — Social APIs

Implement:

friends
add friend
rival
friend scores
Sprint 16.4 — Profile APIs

Implement:

profile
history
settings where remote persistence is required
Sprint 16.5 — API Validation

Validate:

response schema
missing fields
unexpected fields
invalid enum
malformed payload
incorrect HTTP status
server error payload
API Edge Cases
200 valid
200 malformed
204
400
401
403
404
409
422
429
500
502
503
504
timeout
DNS failure
connection reset
slow connection
partial response
malformed JSON
unexpected schema
PHASE 17 — SYNCHRONIZATION + OFFLINE
Objective

Make local and remote state coexist safely.

Sprint 17.1 — Source of Truth

Define per-domain:

Local
Remote
Cached
Derived

authority.

Sprint 17.2 — Cache

Implement:

cache read
cache write
expiration/staleness rules where approved
refresh
Sprint 17.3 — Synchronization

Implement:

Local change
 ↓
Pending operation
 ↓
Network available
 ↓
Sync
 ↓
Success
Sprint 17.4 — Retry

Implement controlled retry.

No infinite retry loops.

Sprint 17.5 — Conflict Handling

Handle:

stale records
duplicate mutations
conflicting updates
remote changes
local changes
Offline Edge Cases
Offline at launch
Offline before battle
Offline during battle
Offline during submission
Offline after submission
Network restored during submission
Network changes repeatedly
Slow network
Intermittent network
Server returns stale data
Sync fails
Sync succeeds after retry
Duplicate sync
App killed during sync
PHASE 18 — LIFECYCLE + RECOVERY
Objective

Make the application safe under Android lifecycle events.

Sprint 18.1 — Background / Foreground

Handle:

background
foreground
screen lock
notification interruption
app switching
Sprint 18.2 — Configuration / Recreation

Handle:

screen recreation
process recreation
state restoration
Sprint 18.3 — Process Death

Test:

Battle active
 ↓
Process killed
 ↓
App reopened
 ↓
Recover state
Sprint 18.4 — Interrupted Submission

Handle:

Submit
 ↓
Process killed
 ↓
Unknown submission status

without duplicating the mutation.

Sprint 18.5 — Recovery Testing

Test every primary screen after:

background
foreground
process death
recreation
network change
PHASE 19 — PRACTICE MODE
Objective

Implement Practice after Official Battle is stable.

Sprint 19.1 — Practice Architecture

Create a separate practice session type.

Sprint 19.2 — Practice Gameplay

Reuse challenge infrastructure safely.

Sprint 19.3 — Practice Results

Practice results remain isolated.

Sprint 19.4 — Adaptive Practice

Only implement approved adaptive/AI behavior.

Practice Edge Cases
Practice started
Practice interrupted
Practice completed
Practice repeated
Official battle active
Official battle completed
Offline
No practice data
Invalid practice configuration

Critical invariant:

Practice must not mutate official battle results.
PHASE 20 — ANALYTICS
Objective

Add analytics without coupling analytics to product behavior.

Sprint 20.1 — Analytics Abstraction

Create:

Analytics interface
 ↓
Provider implementation
Sprint 20.2 — Core Events

Implement approved events for:

app open
onboarding
battle
challenges
results
rival
friends
practice
errors
network
performance
Sprint 20.3 — Event Reliability

Handle:

duplicate event
missing event
offline event
delayed event
app killed before event delivery
provider unavailable
Sprint 20.4 — Privacy

Verify:

data minimization
no unnecessary personal data
no sensitive data leakage
no credentials/tokens in analytics

The underlying architecture should treat important errors as combinations of state, logging, retry/fallback and telemetry rather than silently dropping them.

PHASE 21 — MONETIZATION INFRASTRUCTURE
Objective

Implement only explicitly approved monetization functionality.

Sprint 21.1 — Entitlement Architecture

If monetization is approved, implement:

entitlement state
purchase state
restore state
expiration where applicable
Sprint 21.2 — Purchase Integration

Implement approved provider integration.

Sprint 21.3 — Failure Handling

Handle:

Purchase success
Purchase cancelled
Purchase failed
Network failure
Duplicate purchase
Restore
Expired entitlement
Unknown entitlement
Sprint 21.4 — Gameplay Protection

Verify monetization cannot:

alter official scoring unfairly
interrupt gameplay unexpectedly
corrupt battle state
PHASE 22 — ACCESSIBILITY + INTERACTION HARDENING
Sprint 22.1 — Semantics

Implement semantic labels for all meaningful controls.

Sprint 22.2 — Focus Order

Verify:

logical focus
forms
navigation
gameplay controls
dialogs
Sprint 22.3 — State Accessibility

Correct/incorrect/selected/disabled/loading/error states must not rely on color alone.

Sprint 22.4 — Text Scaling

Test:

larger font
long labels
score values
buttons
cards
graph labels
Sprint 22.5 — Reduced Motion

Implement and test reduced-motion behavior.

Accessibility Edge Cases
Large text
Screen reader
TalkBack
Reduced motion
Low contrast environment
Color-blind interpretation
Long name
Long error
Long friend name
Large score
Empty state
Error state
Loading state
PHASE 23 — COMPREHENSIVE TESTING
Objective

Test the complete application as an integrated system.

Sprint 23.1 — Unit Tests

Test:

domain rules
scoring
validation
state machines
utilities
derived metrics
Sprint 23.2 — Repository Tests

Test:

local storage
API mapping
serialization
synchronization
duplicate handling
migration
Sprint 23.3 — Integration Tests

Test:

UI
 ↓
ViewModel
 ↓
Use Case
 ↓
Repository
 ↓
Data source
Sprint 23.4 — UI Tests

Every primary screen:

Welcome
Battle Name
Home
Battle Intro
Snap
Shift
Crowd Call
Results
Rival
Friends
Add Friend
Profile
History
Settings
Sprint 23.5 — Official Battle E2E

Run the complete flow:

Welcome
 ↓
Battle Name
 ↓
Home
 ↓
Battle Intro
 ↓
Snap
 ↓
Shift
 ↓
Crowd Call
 ↓
Results
 ↓
Rival
Sprint 23.6 — Failure E2E

Break every major boundary.

Test:

Network
Persistence
Lifecycle
Navigation
Timers
Scoring
Submission
Authentication
Authorization
Sprint 23.7 — Duplicate Action Testing

Systematically test:

double tap
triple tap
rapid tap
back spam
retry spam
start spam
submit spam
navigation spam
Sprint 23.8 — State Corruption Testing

Inject:

invalid local state
invalid API state
incomplete battle
missing challenge
duplicate result
invalid score
stale result
inconsistent friend state
Sprint 23.9 — Regression

Run the complete test suite after fixes.

PHASE 24 — PERFORMANCE + SECURITY + STABILITY
Sprint 24.1 — Startup Performance

Measure:

cold start
warm start
first screen rendering
Sprint 24.2 — Gameplay Performance

Measure:

frame rendering
input latency
animation performance
timer accuracy
memory
Sprint 24.3 — Long Session Stability

Test:

Repeated battles
Repeated navigation
Long app session
Background/foreground cycles
Repeated practice
Repeated results viewing
Sprint 24.4 — Memory

Check:

leaks
retained screens
large image/resource retention
unnecessary caches
Sprint 24.5 — Network Efficiency

Check:

duplicate requests
unnecessary polling
request cancellation
retry storms
oversized payloads
Sprint 24.6 — Security

Verify:

authentication
authorization
secure storage
API validation
score authority
battle ownership
secrets
debug endpoints
logging

Never trust the Android client as the sole authority for competitive score integrity.

PHASE 25 — RELEASE HARDENING
Objective

Turn the feature-complete application into a release-ready application.

Sprint 25.1 — Code Cleanup

Remove:

dead code
debug code
temporary mocks
development endpoints
debug logging
unused dependencies
temporary UI
TODOs required for release
Sprint 25.2 — Architecture Audit

Check:

UI
Presentation
Domain
Repository
Data

for boundary violations.

Sprint 25.3 — Dependency Audit

Check:

dependency versions
unnecessary libraries
permissions
SDK integrations
release dependencies
Sprint 25.4 — Build Configuration

Verify:

release build
signing
production endpoint
production configuration
no debug flags
no test data
PHASE 26 — RELEASE BUILD
Sprint 26.1 — Release Candidate Build

Generate the first release candidate.

Sprint 26.2 — Fresh Install Test

Test:

Install
 ↓
First launch
 ↓
Onboarding
 ↓
Battle

on a clean environment.

Sprint 26.3 — Upgrade Test

Test:

Old version
 ↓
New version
 ↓
Data migration
 ↓
App launch
Sprint 26.4 — Final Regression

Run the complete test suite.

Sprint 26.5 — Final Device Matrix

Validate the approved Android device matrix and the required layout sizes:

360 × 800
390 × 844
412 × 915
6. GLOBAL EDGE-CASE MATRIX

Every development phase must consider the following categories.

Input
empty
null
invalid
too short
too long
unexpected characters
rapid input
duplicate input
Navigation
Back
system Back
rapid navigation
duplicate navigation
invalid route
process recreation
deep stack
Network
offline
slow
timeout
disconnect
reconnect
server error
malformed response
authentication failure
authorization failure
rate limiting
Persistence
missing data
corrupt data
migration failure
partial write
duplicate write
concurrent write
storage failure
Lifecycle
background
foreground
screen lock
process death
recreation
notification interruption
Concurrency
double tap
duplicate request
parallel request
retry while request active
response after timeout
late response
out-of-order response
Gameplay
timeout
incorrect answer
correct answer
repeated answer
no answer
challenge interruption
battle interruption
battle completion duplication
Scoring
zero
maximum
invalid
missing
duplicate
overflow
rounding
tie
incomplete
practice
official
Data Synchronization
stale cache
fresh remote
local newer
remote newer
conflict
duplicate operation
failed sync
partial sync
Accessibility
large text
TalkBack
reduced motion
color independence
focus
semantic labels
long content
7. CROSS-CUTTING RULES

The following are not separate late-stage features.

They must be implemented progressively.

Error Handling

Error handling begins in Phase 1.

It must not be postponed to Phase 23.

Accessibility

Accessibility begins when components are created.

The final accessibility phase is an audit and hardening pass, not the first time accessibility is considered.

Testing

Tests must be written alongside implementation.

Do not leave all tests until the end.

Offline

Offline behavior must be designed alongside data flows.

Do not bolt offline support onto a network-only architecture later.

Lifecycle

Lifecycle safety must be considered for every stateful screen.

Observability

Important asynchronous operations should be traceable without logging sensitive information.

8. SPRINT ACCEPTANCE RULE

A sprint is complete only when:

[ ] Implementation complete
[ ] Requirements traced
[ ] Build passes
[ ] Unit tests pass
[ ] Required integration tests pass
[ ] UI tests pass where applicable
[ ] Edge cases tested
[ ] Error states tested
[ ] Lifecycle tested where applicable
[ ] Offline tested where applicable
[ ] Accessibility tested where applicable
[ ] No unauthorized feature added
[ ] No unapproved dependency added
[ ] No silent deviation
[ ] Documentation updated
[ ] Implementation report produced
[ ] Audit completed
[ ] Git checkpoint created
9. PHASE ACCEPTANCE RULE

A phase is complete only when:

Every sprint
    ↓
passes
    ↓
Phase integration test
    ↓
Phase edge-case test
    ↓
Phase architecture review
    ↓
Phase acceptance
10. DEVELOPMENT CHECKPOINTS

Recommended checkpoints:

DEV-000
Environment Ready

DEV-001
Architecture Ready

DEV-002
Android Foundation Ready

DEV-003
Design System Ready

DEV-004
Navigation Ready

DEV-005
Core Domain Ready

DEV-006
Onboarding/Home Ready

DEV-007
Battle Engine Ready

DEV-008
Snap Ready

DEV-009
Shift Ready

DEV-010
Crowd Call Ready

DEV-011
Scoring Ready

DEV-012
Results Ready

DEV-013
Competition Ready

DEV-014
Profile/History/Settings Ready

DEV-015
Persistence Ready

DEV-016
API Integration Ready

DEV-017
Offline/Sync Ready

DEV-018
Lifecycle Recovery Ready

DEV-019
Practice Ready

DEV-020
Analytics Ready

DEV-021
Monetization Ready

DEV-022
Accessibility Ready

DEV-023
System QA Ready

DEV-024
Hardening Ready

DEV-025
Release Candidate Ready
11. FINAL ENGINEERING DEFINITION

Daily Battle development is complete only when:

Architecture
      +
Android implementation
      +
UI
      +
Navigation
      +
Gameplay
      +
Scoring
      +
Results
      +
Competition
      +
Persistence
      +
Backend integration
      +
Synchronization
      +
Offline handling
      +
Lifecycle recovery
      +
Practice
      +
Analytics
      +
Approved monetization
      +
Accessibility
      +
Automated tests
      +
Integration tests
      +
E2E tests
      +
Edge-case testing
      +
Performance
      +
Security
      +
Stability
      +
Release build

all satisfy their respective authoritative specifications.

12. FINAL DEVELOPMENT SEQUENCE

The complete engineering order is:

0. DEVELOPMENT ENVIRONMENT
        ↓
1. ARCHITECTURE FOUNDATION
        ↓
2. ANDROID FOUNDATION
        ↓
3. DESIGN SYSTEM
        ↓
4. APP SHELL
        ↓
5. CORE DOMAIN
        ↓
6. ONBOARDING + HOME
        ↓
7. BATTLE ENGINE
        ↓
8. SNAP
        ↓
9. SHIFT
        ↓
10. CROWD CALL
        ↓
11. SCORING
        ↓
12. RESULTS
        ↓
13. RIVAL + FRIENDS
        ↓
14. PROFILE + HISTORY + SETTINGS
        ↓
15. PERSISTENCE
        ↓
16. BACKEND/API
        ↓
17. SYNC + OFFLINE
        ↓
18. LIFECYCLE + RECOVERY
        ↓
19. PRACTICE
        ↓
20. ANALYTICS
        ↓
21. MONETIZATION INFRASTRUCTURE
        ↓
22. ACCESSIBILITY + INTERACTION HARDENING
        ↓
23. COMPREHENSIVE TESTING
        ↓
24. PERFORMANCE + SECURITY + STABILITY
        ↓
25. RELEASE HARDENING
        ↓
26. RELEASE BUILD
13. FINAL PRINCIPLE

The development process follows:

Implement → verify → break → fix → verify again → checkpoint → continue.

Never:

Implement everything → test at the end.

Every important failure mode must be designed before it becomes a production bug.

Every stateful operation must have:

Normal
Loading
Success
Failure
Retry
Interrupted
Recovered

behavior where applicable.

Every important asynchronous operation must consider:

duplicate
timeout
late response
process death
offline
retry
recovery

Every competitive operation must consider:

authority
integrity
idempotency
duplicate submission
reconciliation

Every screen must consider:

normal
loading
empty
error
offline
lifecycle
accessibility

And every completed phase must leave the application in a known, buildable, testable state.

The objective is not merely to make Daily Battle work.
The objective is to make every important path through Daily Battle deterministic, testable, recoverable, and maintainable.


### One correction I strongly recommend

I would **rename the previous `17_IMPLEMENTATION_ROADMAP.md` to `17_DEVELOPMENT_ROADMAP.md`** rather than maintaining two roadmap files.

Your documentation hierarchy then becomes:

```text
00–16 = WHAT the system/product must be
17     = IN WHAT ENGINEERING ORDER we build it

And then there is a very clean separation:

                 DAILY BATTLE
                      │
        ┌─────────────┴─────────────┐
        │                           │
   SPECIFICATION              DEVELOPMENT
        │                           │
     00–16                    17_DEVELOPMENT
        │                       _ROADMAP
        │                           │
        └──────────────┬────────────┘
                       ↓
                ANTIGRAVITY TASKS
                       ↓
                  IMPLEMENTATION
                       ↓
                 TEST + AUDIT
                       ↓
                  CHECKPOINT

The important improvement here is that edge cases are no longer one final “QA phase.” They are attached to the engineering stage where the failure can actually originate—battle-state failures during the battle engine, timer/lifecycle failures during gameplay, duplicate submissions during scoring/API work, corrupted state during persistence, sync conflicts during offline work, etc. That is much safer than discovering all of them at the end.

Also, the architecture source explicitly supports this development philosophy: testing is intended to exist across unit/domain/data/integration/system/UI boundaries, while persistence, synchronization, error handling, and failure recovery are architectural concerns rather than purely final QA concerns.