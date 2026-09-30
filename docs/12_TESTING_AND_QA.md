# Daily Battle — Testing & QA Specification

**Document:** `12_TESTING_AND_QA.md`  
**Status:** LOCKED as QA governance; implementation details marked PROPOSED/PENDING  
**Product:** Daily Battle  
**Platform:** Android  
**Primary implementation agent:** Antigravity  
**Product authority:** User / Product Owner  
**Architecture authority:** `07_TECHNICAL_ARCHITECTURE.md`  
**Data/API authority:** `08_DATA_AND_API.md`  
**Failure-handling authority:** `09_OFFLINE_AND_ERROR_HANDLING.md`  
**Motion authority:** `10_ANIMATION_HAPTICS.md`  
**Accessibility authority:** `11_ACCESSIBILITY.md`  
**Design authority:** `05_DESIGN_SYSTEM.md`  
**Navigation authority:** `06_NAVIGATION_AND_FLOWS.md`

---

# 1. Purpose

This document defines the complete testing and quality-assurance contract for Daily Battle.

The purpose of QA is not merely to determine whether the application builds.

QA must verify that:

1. The implemented product matches the approved product specification.
2. The UI matches the approved screen architecture and design system.
3. Gameplay behaves according to the approved gameplay rules.
4. Official Battle integrity is preserved.
5. Practice cannot modify official Battle results.
6. Navigation follows the approved flow.
7. Loading, error, offline, retry, lifecycle, and recovery states are handled correctly.
8. Accessibility requirements are implemented.
9. Repeated taps, duplicate submissions, process death, and network uncertainty do not corrupt state.
10. No unauthorized product behavior has been introduced.
11. The implementation remains traceable to documented requirements.
12. Each implementation task has verifiable evidence.

QA is therefore a **product correctness process**, not only a software-testing process.

---

# 2. QA Authority

Testing must follow the documentation hierarchy.

## 2.1 Authority order

When testing behavior, use this order:

1. `00_MASTER_SPEC.md`
2. Product and gameplay requirements
3. Screen architecture and screen blueprints
4. Design system
5. Navigation and flows
6. Technical architecture
7. Data/API specification
8. Offline/error specification
9. Animation/haptics specification
10. Accessibility specification
11. Testing/QA specification
12. Implementation code

If implementation behavior conflicts with an authoritative specification, the implementation does not automatically become the new expected behavior.

The conflict must be reported.

---

# 3. QA Status Vocabulary

Every requirement/test should use explicit status language.

| Status | Meaning |
|---|---|
| `PASS` | Requirement verified successfully |
| `FAIL` | Requirement is not satisfied |
| `BLOCKED` | Cannot be verified because of an external blocker |
| `NOT TESTED` | Verification has not yet occurred |
| `PENDING` | Requirement itself is not finalized |
| `WAIVED` | Explicitly approved exception |
| `REGRESSION` | Previously passing behavior has broken |
| `DEVIATION` | Implementation differs from approved specification |

Antigravity must not convert `PENDING` requirements into assumed behavior.

---

# 4. Testing Philosophy

Daily Battle must be tested from four perspectives:

## 4.1 Product correctness

Does the application do what Daily Battle is supposed to do?

## 4.2 UX correctness

Does the user experience follow the intended hierarchy, flow, states, feedback, and interaction model?

## 4.3 Technical correctness

Does the implementation preserve state, data integrity, lifecycle behavior, and architectural boundaries?

## 4.4 Resilience

Does the application remain safe and understandable when:

- network fails,
- requests time out,
- the user taps repeatedly,
- the app backgrounds,
- the process is killed,
- data is stale,
- a submission result is uncertain,
- APIs return errors,
- local persistence fails?

A feature is not considered complete merely because its happy path works.

---

# 5. Requirement Traceability

Every implemented requirement must be traceable to a test.

The minimum traceability chain is:

```text
Requirement
    ↓
Implementation
    ↓
Test Case
    ↓
Execution
    ↓
Evidence
    ↓
Result

Example:

REQ-NAV-004
    ↓
Official Battle navigation
    ↓
QA-NAV-004
    ↓
Manual + automated navigation test
    ↓
Screenshot / test output
    ↓
PASS
6. Requirement IDs

Existing requirement IDs from other documents must be preserved.

Examples:

REQ-SCREEN-001
REQ-GAME-001
REQ-NAV-001
REQ-DATA-001
REQ-ERROR-001
REQ-MOTION-001
REQ-ACCESS-001

Testing IDs should use a separate namespace.

Examples:

QA-PRODUCT-001
QA-GAME-001
QA-NAV-001
QA-DATA-001
QA-ERROR-001
QA-ACCESS-001
QA-UI-001
QA-LIFECYCLE-001

Do not reuse requirement IDs as test IDs.

7. Test Pyramid

Testing should exist at multiple levels.

                 E2E / Manual QA
              ─────────────────────
             UI / Integration Tests
          ───────────────────────────
          Repository / API / State
       ────────────────────────────────
          Domain / Gameplay Tests
    ─────────────────────────────────────
              Unit Tests

The majority of deterministic business logic should be tested below the UI layer.

UI tests should verify integration and user-visible behavior rather than duplicating every internal implementation detail.

8. Unit Testing

Unit tests should cover deterministic logic that can be tested without the Android UI.

Examples include:

score calculations once formulas are finalized,
challenge state transitions,
validation,
battle state transitions,
retry decisions,
result mapping,
data transformations,
repository result mapping,
percentile formatting,
momentum calculations,
Battle DNA calculations,
input validation,
duplicate-submission protection logic.

Exact formulas must come from the relevant authoritative specification.

If a formula is still pending, the test must not invent one.

9. Gameplay Testing

Gameplay is one of the highest-priority QA areas.

Each challenge must be tested independently.

9.1 Snap

Verify:

challenge enters Ready state correctly,
gameplay begins only at the appropriate state,
target/distractor behavior follows specification,
user input is accepted only when appropriate,
correct interaction produces the intended feedback,
incorrect interaction produces the intended feedback,
score is calculated through the approved scoring logic,
completion occurs exactly once,
completion state cannot be accidentally replayed,
transition to Shift occurs correctly,
repeated taps do not create duplicate results.

Test:

Ready
→ Active
→ Correct
→ Complete

and:

Ready
→ Active
→ Incorrect
→ Complete

where applicable.

10. Shift Testing

Verify:

grid renders correctly,
initial state is correct,
movement/change state is correct,
question/instruction is correct,
answer options are rendered correctly,
only valid selections are accepted,
selected state is visually and semantically clear,
result is calculated through approved scoring logic,
completion occurs once,
transition to Crowd Call is correct.

Test invalid interaction paths as well.

11. Crowd Call Testing

Verify:

question is displayed correctly,
answer choices are available,
user prediction is accepted correctly,
selected state is correct,
crowd result is displayed only at the appropriate stage,
prediction result is calculated using approved logic,
completion occurs once,
transition to Results is correct.

Crowd data must not be fabricated as real server data during production testing.

Fake/test data must be explicitly isolated.

12. Scoring Tests

Scoring is a critical domain boundary.

Current approved score structure:

Snap        300
Shift       300
Crowd Call  300
Consistency 100
----------------
Total      1000

This defines the current intended allocation.

However, the exact formulas and algorithms for:

Snap score,
Shift score,
Crowd Call score,
Consistency,
timing normalization,
penalties,
rounding,
percentile,

must not be invented during implementation if they are not finalized in the authoritative specification.

12.1 Required tests once formulas are finalized

For every scoring component test:

Boundary values
minimum valid input
maximum valid input
exact boundary
just below boundary
just above boundary
Determinism

Same inputs must produce the same result.

Invalid input

Invalid values must not produce undefined or corrupted scores.

Rounding

Rounding behavior must be explicitly verified.

Total score

Verify:

Snap + Shift + Crowd Call + Consistency = Total

according to the finalized scoring contract.

Overflow

Inputs must not cause:

negative unexpected scores,
integer overflow,
floating-point corruption,
impossible totals.
13. Official Battle Integrity Tests

Official Battle is a protected transaction.

Tests must verify:

A user can start the official Battle only when eligible.
The official Battle uses the correct official challenge set.
Official challenge order is preserved.
Official attempt state is persisted appropriately.
Reopening the app does not accidentally create a second attempt.
Repeated taps do not create multiple attempts.
Repeated submissions do not create duplicate results.
Process death does not silently reset or duplicate the attempt.
Network uncertainty does not cause duplicate official submission.
Official results cannot be replaced by Practice results.
Practice cannot alter official score.
Completed official Battle cannot be replayed as another official attempt unless the product specification explicitly allows it.
14. Practice Isolation Tests

Practice must remain separate from Official Battle.

Verify:

Practice score
≠
Official Battle score

Practice must not modify:

official score,
official completion state,
official streak/momentum,
official ranking/percentile,
official Battle result,

unless the authoritative product specification explicitly defines an interaction.

Test:

Official not started
→ Practice
→ exit
→ Official still not started

and:

Official completed
→ Practice
→ exit
→ Official result unchanged
15. State-Machine Testing

Every stateful feature should be tested as a state machine.

15.1 Battle state

At minimum:

NOT_STARTED
IN_PROGRESS
COMPLETED

Verify all valid transitions.

Verify invalid transitions are rejected or safely handled.

Example:

COMPLETED → IN_PROGRESS

must not happen accidentally.

16. Challenge State Testing

Challenge states defined by the specification must be tested.

Example:

READY
ACTIVE
CORRECT
INCORRECT
COMPLETE

Tests must verify:

valid transitions,
invalid transitions,
transition timing,
repeated event handling,
lifecycle interruption,
cancellation,
completion idempotency.
17. Navigation Testing

Navigation must follow 06_NAVIGATION_AND_FLOWS.md.

17.1 Primary navigation

Verify:

Home
Battle
Friends
Me

Each destination must resolve correctly.

17.2 Onboarding

Verify:

Welcome
→ Battle Name
→ Home

Verify back behavior at every stage.

18. Official Battle E2E Test

Canonical test:

Launch
→ Welcome
→ Battle Name
→ Home
→ Battle Intro
→ Snap
→ Shift
→ Crowd Call
→ Results
→ Rival

Verify:

no unexpected screen,
no duplicated screen,
no accidental bottom navigation during gameplay,
no skipped challenge,
no duplicated challenge,
correct back behavior,
correct completion state,
correct result.
19. Gameplay Navigation Restrictions

During official gameplay:

bottom navigation must remain hidden,
unrelated social/profile controls must not appear,
user must not accidentally leave the battle through ordinary navigation,
system back behavior must follow the approved battle interruption/recovery rules.

Exact interruption behavior remains governed by the navigation and error specifications.

If that behavior is unresolved, mark the test PENDING.

20. UI Testing

UI tests should verify:

layout,
hierarchy,
component states,
text,
spacing,
alignment,
visibility,
interaction,
accessibility semantics.

UI tests must not rely only on screenshots.

A visually similar screen can still contain incorrect behavior.

21. Visual Regression Testing

Visual verification should be performed against the approved design specification.

Primary master size:

390 × 844

Validation sizes:

412 × 915
360 × 800

Verify:

margins,
safe areas,
card sizes,
typography,
button sizes,
navigation,
spacing,
icon placement,
score presentation,
challenge accent usage,
state appearance.

Visual QA should specifically detect:

text clipping,
unexpected wrapping,
overlapping components,
incorrect padding,
incorrect bottom inset,
keyboard overlap,
navigation overlap,
inconsistent component sizing.
22. Screen-by-Screen QA

Every primary screen must have a dedicated QA pass.

SCR-001 — Welcome

Verify:

visual hierarchy,
CTA,
onboarding state,
accessibility,
navigation.
SCR-002 — Battle Name

Verify:

input,
validation,
keyboard behavior,
empty state,
invalid state,
continue behavior,
persistence.
SCR-003 — Home

Verify:

greeting,
momentum,
Today’s Battle hero,
play CTA,
rival card,
completed state,
loading/error states.
SCR-004 — Battle Intro

Verify:

3 challenges,
~3 minutes,
official attempt messaging,
start behavior,
no unnecessary challenge details.
SCR-005 — Snap

Verify:

gameplay area,
timer/progress,
interaction,
feedback,
score,
completion.
SCR-006 — Shift

Verify:

grid,
question,
answer interaction,
state transitions,
completion.
SCR-007 — Crowd Call

Verify:

question,
answer cards,
prediction interaction,
crowd result,
completion.
SCR-008 — Results

Verify:

score,
percentile,
improvement,
breakdown,
rival comparison,
next action,
score reveal sequence.
SCR-009 — Rival

Verify:

single rival,
score,
gap,
beat CTA,
match history.
SCR-010 — Friends

Verify:

friend list,
today's scores,
add friend,
empty state,
loading/error.
SCR-011 — Add Friend

Verify:

Battle Code,
own code,
copy behavior,
invalid code,
duplicate friend handling.
SCR-012 — Profile

Verify:

Battle DNA,
momentum,
best score,
average,
records,
accessibility,
no unsupported IQ/scientific claims.
SCR-013 — History

Verify:

graph,
best,
average,
momentum,
recent battles,
empty state,
loading/error.
23. Secondary Flow QA

Test:

Home Completed,
Snap Complete,
Shift Complete,
Crowd Result,
Practice,
Incoming Challenge,
Settings.

These flows must not be treated as optional simply because they are not primary screens.

24. Lifecycle Testing

Android lifecycle behavior is critical.

Test:

Background

During:

Home
Battle Intro
Snap
Shift
Crowd Call
Results

send app to background and return.

Verify state is preserved correctly.

Rotation / configuration changes

If orientation/configuration changes are supported, verify state preservation.

If orientation is locked, verify that behavior matches the approved product decision.

Process death

Force-kill the app during:

Home,
Battle Intro,
Snap,
Shift,
Crowd Call,
Results.

Reopen.

Verify that state is recovered according to the approved lifecycle contract.

25. Network Testing

Test at minimum:

Online
Offline
Slow network
Request timeout
Server error
Authentication error
Authorization error
Validation error
Unknown result
Connection restored

Network tests must verify that the application does not falsely report a successful mutation when the result is unknown.

26. Offline Testing

Test:

Launch offline

Verify:

cached data behavior,
appropriate unavailable states,
no misleading success.
Start Battle offline

Verify behavior according to the offline specification.

Submit while offline

Verify:

no duplicate submission,
no false completion,
appropriate recovery state.
Reconnect

Verify reconciliation.

27. Duplicate Interaction Testing

Repeated interaction is a mandatory QA category.

Test rapid repeated taps on:

Play Battle,
Start Battle,
answer options,
Submit,
Add Friend,
Copy,
Retry,
Share,
navigation controls.

Expected behavior:

one logical action,
no duplicate requests,
no duplicate navigation,
no duplicate result,
no corrupted state.
28. Repository and Data Testing

Repositories must be tested independently from UI.

Verify:

correct data mapping,
null handling,
stale data handling,
error mapping,
retry behavior,
persistence,
cache behavior,
synchronization behavior.

Domain models must not unexpectedly depend on transport-specific DTO behavior.

29. API Contract Testing

Where APIs exist, verify:

request structure,
response structure,
required fields,
optional fields,
error structure,
status handling,
idempotency behavior,
version compatibility.

Exact API endpoints remain governed by 08_DATA_AND_API.md.

Do not create undocumented endpoints merely to make tests pass.

30. Persistence Testing

Test persistence of all data that the specification requires to survive app restarts.

Examples:

battle state,
official attempt state,
profile data,
Battle Name,
settings,
friend state,
cached battle data.

Test:

write
→ close
→ reopen
→ read

Also test:

existing data
→ migration
→ read

Migration strategy/tooling remains subject to the technical architecture decision.

31. Migration Testing

If persistent schemas change:

Create representative old data.
Upgrade application.
Run migration.
Open application.
Verify data integrity.
Verify no silent data loss.

Migration failures must fail safely.

32. Error-State Testing

Every network/data-driven screen must have appropriate:

Loading
Success
Empty
Error
Retry

states where applicable.

Do not use an empty state to hide an actual error.

Do not use an error state when valid data simply does not exist.

33. Accessibility Testing

Follow 11_ACCESSIBILITY.md.

Verify:

minimum 44px touch target,
preferred 48px where possible,
semantic labels,
focus order,
screen reader behavior,
text scaling,
state announcements,
color-independent status,
disabled state semantics,
error semantics,
loading semantics,
score semantics,
challenge instruction semantics,
bottom navigation semantics.

Test with accessibility services enabled.

34. Color Independence

Do not test only by visual color matching.

Verify:

Correct vs incorrect is distinguishable without color,
selected vs unselected is distinguishable without color,
disabled vs enabled is distinguishable without color,
warning/error/success states have additional visual or semantic indicators.
35. Motion Testing

Verify:

transition duration,
sequencing,
cancellation,
no stuck animation,
no duplicate animation,
no animation after screen disposal,
reduced-motion behavior.

Important timing categories:

Normal navigation: 200–300ms
Game transitions: 250–400ms
Score reveal: 700–1000ms
Button press: 100–150ms

Exact values must follow 10_ANIMATION_HAPTICS.md.

36. Haptic Testing

Verify appropriate haptic behavior for:

button interaction,
correct answer,
incorrect answer,
personal best,
battle completion.

Verify:

no accidental repeated vibration,
no vibration after disabled interaction,
no vibration during unrelated navigation,
settings toggle works where applicable.
37. Sound Testing

Verify:

tap sound,
correct sound,
incorrect sound,
score reveal,
personal best,
battle complete.

Verify:

mute behavior,
settings behavior,
no continuous background music unless explicitly approved,
no duplicated sound events.
38. Device and Screen Matrix

At minimum validate:

Category	Target
Master	390×844
Large	412×915
Small	360×800
Orientation	Approved orientation only
Accessibility	Text scaling enabled
Network	Online/offline
Lifecycle	Background/process death
Performance	Low/mid/high device class

Exact Android OS/device matrix is PENDING until the supported-device policy is finalized.

Antigravity must not invent a minimum Android version.

39. Responsive Layout Testing

Verify the application does not depend on hardcoded assumptions tied only to 390×844.

Check:

text wrapping,
hero card height,
bottom navigation,
keyboard,
score layouts,
challenge grids,
answer cards,
long names,
large scores,
localization expansion if localization is later introduced.
40. Long-Content Testing

Test:

long Battle Name,
long friend name,
long error message,
long server response,
long accessibility label,
large score,
large percentile,
empty history,
many history entries.

No important content may be silently clipped.

41. Security and Privacy QA

Verify:

sensitive data is not logged unnecessarily,
authentication credentials are not exposed,
API secrets are not embedded in client code,
private friend/account data is not shown to unauthorized users,
account deletion follows the approved data policy,
Battle Code behavior follows the data specification.

Do not place production secrets inside the repository.

42. Analytics Testing

Analytics events should be verified only after the analytics specification is finalized.

Where events exist, verify:

event name,
event trigger,
event payload,
event frequency,
duplicate prevention,
no sensitive data leakage.

Analytics must never alter gameplay behavior.

43. Test Data

Testing must use explicit test data.

Test data categories:

TEST_USER
TEST_FRIEND
TEST_RIVAL
TEST_BATTLE
TEST_CHALLENGE
TEST_RESULT

Production user data must not be casually used for testing.

Fake data must be clearly separated from production data.

44. Deterministic Test Data

Gameplay tests should prefer deterministic fixtures.

Example:

Battle Fixture A
Challenge Fixture A1
Challenge Fixture A2
Challenge Fixture A3

The same fixture should produce predictable behavior.

Randomness may be used only where the specification requires it.

Randomized tests must have reproducible seeds or equivalent reproducibility mechanisms if supported.

45. End-to-End Battle Test Matrix
E2E-001 — New user
Install
→ Welcome
→ Battle Name
→ Home
→ Battle Intro
→ Snap
→ Shift
→ Crowd Call
→ Results

Expected:

complete official Battle exactly once.
E2E-002 — Returning user
Launch
→ Home
→ Today’s Battle

Expected:

correct current state.
E2E-003 — Completed Battle
Complete official Battle
→ return Home

Expected:

completed Home state,
result visible,
next battle messaging.
E2E-004 — Practice
Home
→ Practice
→ Play
→ Complete
→ Home

Expected:

official Battle unchanged.
E2E-005 — Network failure
Start/submit
→ network failure
→ recover

Expected:

no duplicate official result,
appropriate recovery state.
E2E-006 — Process death
Start Battle
→ kill process
→ reopen

Expected:

approved recovery behavior.
E2E-007 — Repeated tap
Tap Play rapidly

Expected:

one Battle session.
E2E-008 — Friend flow
Friends
→ Add Friend
→ enter Battle Code
→ Add

Expected:

correct friend state,
no duplicate friend.
46. Regression Testing

Every significant implementation task must trigger regression testing of affected areas.

Regression scope:

Small UI change

Run:

affected screen,
navigation to/from screen,
accessibility,
visual regression.
Gameplay change

Run:

affected challenge,
official Battle E2E,
scoring,
persistence,
lifecycle,
transition to next challenge.
Data/API change

Run:

repository tests,
error handling,
offline behavior,
affected UI,
E2E Battle if relevant.
Navigation change

Run:

complete navigation map,
back behavior,
deep-link/entry behavior if applicable,
official Battle flow.
47. Bug Classification

Bugs must be classified by impact.

P0 — Critical

Examples:

official score corruption,
duplicate official result,
data loss,
app cannot launch,
security-critical exposure,
unrecoverable battle state.

Release blocker.

P1 — High

Examples:

major screen unusable,
official Battle cannot complete,
major navigation failure,
persistent crash,
major accessibility failure.

Normally release blocker.

P2 — Medium

Examples:

incorrect secondary state,
recoverable UI issue,
non-critical data display problem,
minor navigation issue.

Release decision requires review.

P3 — Low

Examples:

minor visual spacing,
minor animation mismatch,
cosmetic issue without functional impact.

May be deferred with explicit approval.

48. Bug Report Requirements

Every bug report should include:

Bug ID:
Title:
Severity:
Environment:
App Version:
Device:
Screen:
Requirement ID:
Steps to Reproduce:
Expected:
Actual:
Frequency:
Evidence:
Logs:
Screenshot/Video:
Workaround:
Regression:
49. Acceptance Criteria

A task passes QA only when:

requirements are implemented,
happy path works,
relevant edge cases work,
error states work,
loading states work,
empty states work,
navigation works,
lifecycle behavior works,
accessibility requirements are satisfied,
visual requirements are satisfied,
repeated interactions are safe,
tests pass,
build succeeds,
no unauthorized product changes exist.
50. Rejection Criteria

A task must not be accepted if:

a critical requirement fails,
official Battle integrity is compromised,
implementation invents undefined product behavior,
unauthorized screens/features are introduced,
Practice affects Official Battle,
duplicate submissions can occur,
major lifecycle corruption exists,
critical accessibility requirements fail,
documented error states are missing,
implementation contains required unresolved TODOs,
build/test verification is absent.
51. Antigravity QA Report

After every implementation task, Antigravity must produce:

TASK:
STATUS:

IMPLEMENTED:
- ...

REQUIREMENTS VERIFIED:
- REQ-XXX
- REQ-XXX

TESTS RUN:
- ...

TEST RESULTS:
- PASS
- PASS
- FAIL

FILES CHANGED:
- ...

BUILD:
- PASS / FAIL

EDGE CASES TESTED:
- ...

ACCESSIBILITY:
- ...

VISUAL VERIFICATION:
- ...

LIFECYCLE:
- ...

NETWORK/OFFLINE:
- ...

NEW DEPENDENCIES:
- None / list

DEVIATIONS:
- None / DEV-XXX

KNOWN ISSUES:
- ...

EVIDENCE:
- screenshots
- recordings
- logs
- test output

NEXT RECOMMENDED TASK:
- ...

AUTHORIZATION:
Do not continue automatically.

The final line is important.

A recommended next task is not permission to execute it.

52. Four Required Audits

Daily Battle uses four independent audit categories.

52.1 Product Audit

Verify:

correct product behavior,
correct gameplay,
correct scoring,
correct official/practice separation,
correct social behavior,
no unauthorized features.
52.2 UI/UX Audit

Verify:

design system,
hierarchy,
spacing,
typography,
states,
navigation,
accessibility,
responsive behavior,
motion.
52.3 Code/Architecture Audit

Verify:

layer boundaries,
state management,
repository separation,
domain logic,
persistence,
API handling,
dependency discipline,
testability.
52.4 Edge-Case Audit

Verify:

offline,
timeout,
duplicate taps,
duplicate submissions,
process death,
backgrounding,
stale data,
empty data,
malformed data,
invalid input,
retry,
recovery.
53. Audit Timing

Audits should occur:

After foundational implementation
Product Audit
UI/UX Audit
Code/Architecture Audit
Before gameplay completion
Product
Gameplay
Edge Cases
Before release
Product
UI/UX
Code/Architecture
Edge Cases
Full Regression
54. Build Verification

Every meaningful implementation task must verify that the project builds successfully.

Build verification must include, where applicable:

Compile
→ Test
→ Package
→ Install
→ Launch

A successful compile alone is not sufficient.

Exact build tooling and CI provider remain subject to the technical architecture and development environment decisions.

55. CI/CD

Automated CI should eventually execute relevant checks such as:

Static analysis
Unit tests
Domain tests
Repository tests
UI tests
Build verification

Exact CI provider/tooling is PENDING.

Antigravity must not introduce a CI provider without approval if it creates project infrastructure or external dependencies.

56. Performance Testing

Verify:

launch responsiveness,
navigation responsiveness,
gameplay responsiveness,
animation smoothness,
no obvious frame drops,
no unnecessary network requests,
no excessive memory usage,
no repeated expensive computation during gameplay.

Gameplay must remain responsive under normal supported device conditions.

Exact quantitative performance thresholds are PENDING unless defined elsewhere.

57. Memory / Resource Testing

Test for:

memory leaks,
unnecessary retained screens,
retained listeners,
repeated animation resources,
duplicated network requests,
large image/resource misuse.

Especially test:

Home
→ Battle
→ Results
→ Home

repeatedly.

58. Crash Testing

Verify:

normal launch,
cold start,
warm start,
background/foreground,
process recreation,
gameplay,
network transitions,
malformed responses,
invalid input.

Any reproducible crash in the official Battle path is at least a high-priority defect and may be critical depending on impact.

59. Manual QA Checklist

Before accepting a release candidate:

[ ] App installs
[ ] App launches
[ ] Onboarding works
[ ] Battle Name works
[ ] Home works
[ ] Today’s Battle works
[ ] Battle Intro works
[ ] Snap works
[ ] Shift works
[ ] Crowd Call works
[ ] Results works
[ ] Rival works
[ ] Friends works
[ ] Add Friend works
[ ] Profile works
[ ] History works
[ ] Settings works
[ ] Official Battle completes
[ ] Practice is isolated
[ ] Back behavior works
[ ] Offline behavior works
[ ] Retry works
[ ] Process death recovery works
[ ] Duplicate taps are safe
[ ] Accessibility verified
[ ] Visual QA verified
[ ] Motion verified
[ ] Haptics verified
[ ] Sound verified
[ ] No critical crashes
[ ] No P0 defects
[ ] No unauthorized features
[ ] Build verified
60. Release Gate

A release candidate must satisfy:

Product QA             PASS
Gameplay QA            PASS
Navigation QA          PASS
UI/UX QA               PASS
Accessibility QA       PASS
Lifecycle QA           PASS
Offline/Error QA       PASS
Data/API QA            PASS
Regression QA          PASS
Build Verification     PASS
Critical Audit         PASS

No P0 defect may remain.

P1 defects require explicit review before release.

P2/P3 defects may only remain when explicitly accepted and documented.

61. Evidence Requirements

QA claims should be backed by evidence where practical.

Evidence may include:

screenshots,
screen recordings,
automated test output,
build output,
logs,
API responses,
test reports,
accessibility inspection results.

Evidence should identify:

Task
Requirement
Test
Environment
Result

Do not claim PASS based only on visual assumption.

62. Test Environment Separation

At minimum distinguish:

Development
Testing
Production

Exact environment architecture is PENDING.

Production data must not be casually mutated during QA.

63. Unauthorized Product Changes

QA must explicitly check for scope creep.

Reject or report:

new screens not specified,
new game modes,
new currencies,
new leaderboard systems,
chat,
infinite feeds,
unnecessary AI branding,
new monetization mechanics,
new social mechanics,
new permissions,
new notifications,
new data collection.

The absence of a requirement is not permission to invent a feature.

64. Testing Undefined Requirements

When a test encounters an undefined requirement:

STOP
↓
Mark test PENDING
↓
Record the ambiguity
↓
Ask for product/architecture decision
↓
Update authoritative specification
↓
Implement/update test
↓
Resume QA

Do not silently choose a behavior.

65. QA Decision Log

Unresolved QA questions should be recorded.

Suggested IDs:

QA-PENDING-001
QA-PENDING-002
QA-PENDING-003
...

Each entry should contain:

Question:
Affected Requirement:
Affected Screen:
Affected Test:
Why Decision Is Required:
Possible Behaviors:
Decision:
Decision Owner:
Date:
66. Current QA Pending Decisions

The following items should remain pending until explicitly decided:

QA-PENDING-001

Exact supported Android OS/device matrix.

QA-PENDING-002

Exact automated UI-testing framework/tooling.

QA-PENDING-003

Exact CI provider and pipeline architecture.

QA-PENDING-004

Exact performance thresholds.

QA-PENDING-005

Exact production/staging environment structure.

QA-PENDING-006

Exact API integration test strategy.

QA-PENDING-007

Exact persistence migration tooling.

QA-PENDING-008

Exact analytics testing implementation.

QA-PENDING-009

Exact crash reporting tooling.

QA-PENDING-010

Exact screenshot/visual regression tooling.

QA-PENDING-011

Final scoring formulas required for deterministic scoring tests.

QA-PENDING-012

Final percentile calculation required for result verification.

QA-PENDING-013

Final lifecycle behavior for an interrupted active challenge where the specification remains unresolved.

QA-PENDING-014

Final behavior for unsupported/edge device configurations.

67. QA Task Workflow

Every implementation task follows:

1. Read authoritative requirements
        ↓
2. Identify affected tests
        ↓
3. Implement
        ↓
4. Build
        ↓
5. Run automated tests
        ↓
6. Run manual verification
        ↓
7. Verify edge cases
        ↓
8. Verify accessibility
        ↓
9. Verify visual behavior
        ↓
10. Record evidence
        ↓
11. Report deviations
        ↓
12. Product/QA acceptance
        ↓
13. Git checkpoint
        ↓
14. Next authorized task
68. Git / Checkpoint Policy

Before significant implementation tasks:

checkpoint

After successful acceptance:

verified checkpoint

If a task fails:

do not proceed to unrelated feature work

Fix or explicitly record the blocker first.

69. Regression Baseline

Once a feature has passed QA, its verified behavior becomes part of the regression baseline.

Future changes must not break previously accepted behavior without an explicit approved requirement change.

If behavior intentionally changes:

Old requirement
→ Decision
→ Updated specification
→ Updated tests
→ New implementation
→ Regression verification
70. Definition of Done — Testing

A feature is DONE only when:

Product
 Approved requirement implemented
 No unauthorized behavior
 Product flow verified
Gameplay
 Correct state transitions
 Correct interaction
 Correct completion
 Correct scoring where finalized
 Official/Practice isolation verified
UI
 Correct layout
 Correct typography
 Correct spacing
 Correct states
 Responsive validation completed
Navigation
 Entry works
 Exit works
 Back behavior works
 No duplicate navigation
Resilience
 Loading tested
 Empty tested
 Error tested
 Offline tested
 Retry tested
 Lifecycle tested
 Duplicate interaction tested
Accessibility
 Touch targets verified
 Semantics verified
 Focus order verified
 Color independence verified
 Dynamic text behavior verified
Feedback
 Motion verified
 Haptics verified
 Sound verified
 Reduced-motion behavior verified
Technical
 Unit tests pass
 Relevant integration tests pass
 Relevant UI tests pass
 Build passes
 No critical crashes
 No unauthorized dependencies
QA
 Evidence collected
 Implementation report completed
 Deviations recorded
 Four relevant audits completed
 No unresolved release-blocking issue
 Acceptance explicitly recorded
71. Antigravity Non-Negotiable Rules

Antigravity must:

Treat this document as the QA contract.
Never mark an untested requirement as PASS.
Never invent missing scoring formulas.
Never invent missing product behavior.
Never hide test failures.
Never ignore lifecycle failures.
Never ignore duplicate-submission risks.
Never treat compile success as complete QA.
Never skip accessibility verification for user-facing screens.
Never skip error/offline testing for network-dependent behavior.
Never introduce unauthorized product features.
Report all deviations explicitly.
Report all new dependencies explicitly.
Preserve requirement traceability.
Provide evidence for significant verification.
Stop when an important requirement is ambiguous.
Ask for a decision rather than silently selecting one.
Never continue into the next feature merely because the previous task generated a recommendation.
72. Final QA Principle

Daily Battle is considered high quality only when:

It looks correct
        AND
It feels correct
        AND
It plays correctly
        AND
It preserves state correctly
        AND
It survives failure correctly
        AND
It remains accessible
        AND
It matches the documented product
        AND
It contains no unauthorized behavior

The final QA objective is therefore:

Verify the product that was specified — not the product the implementation happened to become.