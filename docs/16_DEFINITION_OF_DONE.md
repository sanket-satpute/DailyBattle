# Daily Battle — Definition of Done

**Document:** `16_DEFINITION_OF_DONE.md`  
**Status:** LOCKED  
**Product:** Daily Battle  
**Platform:** Android  
**Product authority:** User / Product Owner  
**Implementation agent:** Antigravity  

**Purpose:**  
This document defines the final acceptance contract for Daily Battle.

A feature, screen, gameplay system, flow, milestone, or release is **DONE** only when the applicable requirements in this document have been satisfied and verified.

---

# 1. Purpose

"Implemented" and "Done" are not the same thing.

A feature may be:

- coded,
- compiled,
- visually present,
- partially functional,

and still not be DONE.

Daily Battle uses the following standard:

```text
SPECIFIED
    ↓
IMPLEMENTED
    ↓
TESTED
    ↓
VERIFIED
    ↓
AUDITED
    ↓
ACCEPTED
    ↓
DONE

A task must not be marked complete merely because the code exists.

2. Definition of Done Authority

This document works together with:

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

No Definition of Done item overrides a higher-level product requirement.

3. Core Definition of Done

A feature is DONE only when:

Correct product behavior
        AND
Correct UI/UX
        AND
Correct navigation
        AND
Correct states
        AND
Correct technical implementation
        AND
Correct error handling
        AND
Correct lifecycle behavior
        AND
Correct accessibility
        AND
Relevant tests pass
        AND
Build passes
        AND
No unauthorized behavior
        AND
No unresolved blocking deviation
        AND
Evidence exists
4. Scope of "Done"

The Definition of Done applies at multiple levels:

Component
Screen
User flow
Gameplay challenge
Feature
Vertical slice
Milestone
Release candidate
Production release

Not every checklist item applies identically to every level.

Only relevant items must be satisfied, but critical product requirements must never be skipped.

5. Requirement Traceability

Every completed task must be traceable to requirements.

Minimum chain:

Requirement
    ↓
Task
    ↓
Implementation
    ↓
Test
    ↓
Evidence
    ↓
Acceptance

Example:

REQ-SCREEN-003
      ↓
TASK-HOME-001
      ↓
Home implementation
      ↓
QA-HOME-001
      ↓
Screenshot + test output
      ↓
PASS
6. Requirement Status

Every requirement relevant to the task must be classified as:

PASS
FAIL
BLOCKED
NOT TESTED
PENDING
WAIVED

A PENDING requirement cannot be silently treated as PASS.

7. Product DoD

The feature must match the approved product specification.

Verify:

[ ] User goal is correct
[ ] Feature purpose is correct
[ ] Product behavior matches specification
[ ] User flow matches specification
[ ] No unauthorized behavior exists
[ ] No unnecessary feature has been introduced
[ ] Product terminology is correct
[ ] Core Daily Battle loop remains intact
8. Scope DoD

Verify:

[ ] Task scope was explicitly defined
[ ] Non-goals were respected
[ ] No scope creep was introduced
[ ] Future features were not prematurely implemented
[ ] No hidden feature flags were added unnecessarily
[ ] No speculative architecture was introduced
9. Screen DoD

For every implemented screen:

[ ] Screen has an approved screen ID
[ ] Screen purpose is documented
[ ] Screen has one dominant job
[ ] Correct entry path exists
[ ] Correct exit path exists
[ ] Correct navigation exists
[ ] Correct back behavior exists
[ ] Correct UI hierarchy exists
[ ] Correct content exists
[ ] Correct states exist
[ ] Loading state handled where relevant
[ ] Empty state handled where relevant
[ ] Error state handled where relevant
[ ] Accessibility verified
[ ] Visual QA completed
10. Screen IDs

Primary screens currently include:

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

Existing IDs must not be changed casually.

11. Design System DoD

Every user-facing feature must follow the approved design system.

Verify:

[ ] Background colors correct
[ ] Surface colors correct
[ ] Text colors correct
[ ] Challenge accent colors correct
[ ] Typography correct
[ ] Spacing correct
[ ] Radius correct
[ ] Button dimensions correct
[ ] Input dimensions correct
[ ] Icon sizing correct
[ ] Touch targets correct
[ ] Component states correct
12. Color DoD

Primary colors must follow the documented tokens:

Background:
#080B10

Surface 1:
#11161F

Surface 2:
#171D28

Elevated:
#1C2330

Primary text:
#F5F7FA

Secondary text:
#A2AAB8

Muted:
#6F7887

Disabled:
#4D5563

Brand:
#7C5CFC

Success:
#39D98A

Warning:
#FFB84D

Error:
#FF5D73

Info:
#4EA8FF

Challenge accents:

Snap:
#4EA8FF

Shift:
#FFB84D

Crowd Call:
#39D98A

No arbitrary visible colors should be introduced without a design reason and approval where required.

13. Typography DoD

Verify:

[ ] Inter is used
[ ] Heading hierarchy is correct
[ ] Body hierarchy is correct
[ ] Numeric score typography is appropriate
[ ] Tabular numerals are used where required
[ ] Important UI does not fall below the documented minimum
[ ] Text does not clip
[ ] Text does not unexpectedly overlap
14. Spacing DoD

The documented spacing system must be respected:

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

Verify:

[ ] Screen margins correct
[ ] Card padding correct
[ ] Section spacing correct
[ ] Button spacing correct
[ ] Bottom navigation spacing correct
[ ] Gameplay spacing correct
15. Responsive Layout DoD

Validate against:

390 × 844
412 × 915
360 × 800

Verify:

[ ] No clipping
[ ] No overlapping
[ ] No broken hierarchy
[ ] No keyboard overlap
[ ] No bottom navigation overlap
[ ] Long text handled
[ ] Scores remain readable
[ ] Gameplay remains usable
16. Component DoD

Every reusable component must:

[ ] Have a defined purpose
[ ] Follow design tokens
[ ] Have documented states
[ ] Have correct interaction behavior
[ ] Have accessibility semantics
[ ] Support required content variations
[ ] Avoid unnecessary coupling
17. Button DoD

For applicable buttons:

[ ] Default
[ ] Pressed
[ ] Disabled
[ ] Loading
[ ] Success

Verify:

[ ] Correct height
[ ] Correct typography
[ ] Correct radius
[ ] Correct padding
[ ] Correct icon behavior
[ ] Correct touch target
[ ] Correct accessibility state
18. Input DoD

For applicable inputs:

[ ] Empty
[ ] Focused
[ ] Filled
[ ] Invalid
[ ] Disabled
[ ] Loading where relevant

Verify:

[ ] Keyboard behavior
[ ] Validation
[ ] Error message
[ ] Focus behavior
[ ] Text accessibility
[ ] Long input behavior
19. Navigation DoD

Verify:

[ ] Correct destination
[ ] Correct back behavior
[ ] Correct stack behavior
[ ] No duplicate navigation
[ ] No unexpected screen
[ ] No dead-end screen
[ ] Deep-link behavior where applicable
[ ] Bottom navigation behavior correct
20. Primary Navigation DoD

The primary navigation structure is:

Home
Battle
Friends
Me

Verify:

[ ] Home opens Home
[ ] Battle opens approved Battle destination
[ ] Friends opens Friends
[ ] Me opens approved personal destination
[ ] Active state is correct
[ ] Gameplay hides bottom navigation
21. Onboarding DoD

The onboarding flow must work:

Welcome
   ↓
Battle Name
   ↓
Home

Verify:

[ ] New user can enter
[ ] Battle Name works
[ ] Validation works
[ ] Continue works
[ ] Persistence works where required
[ ] Returning user does not unnecessarily repeat onboarding
22. Official Battle DoD

The canonical Battle flow must work:

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

Verify:

[ ] Correct order
[ ] No skipped challenge
[ ] No duplicate challenge
[ ] Correct state transitions
[ ] Correct completion
[ ] Correct score
[ ] Correct result
[ ] Correct persistence
[ ] Correct recovery
23. Official Battle Integrity DoD

Verify:

[ ] One official attempt is respected
[ ] Duplicate start prevented
[ ] Duplicate submission prevented
[ ] Duplicate completion prevented
[ ] Official result cannot be accidentally replaced
[ ] Practice cannot modify official result
[ ] Process death does not corrupt Battle state
[ ] Network uncertainty is safely handled
24. Snap DoD

Verify:

[ ] Ready state
[ ] Active state
[ ] Correct interaction
[ ] Incorrect interaction
[ ] Feedback
[ ] Score
[ ] Completion
[ ] Transition to Shift
[ ] Repeated tap protection
[ ] Accessibility
[ ] Timing behavior
25. Shift DoD

Verify:

[ ] Grid renders correctly
[ ] Initial state correct
[ ] Movement/change behavior correct
[ ] Question correct
[ ] Answer selection works
[ ] Selected state correct
[ ] Correct/incorrect state correct
[ ] Score correct
[ ] Completion correct
[ ] Transition to Crowd Call correct
[ ] Accessibility
26. Crowd Call DoD

Verify:

[ ] Question renders correctly
[ ] Answer choices render correctly
[ ] Selection works
[ ] Result state works
[ ] Crowd distribution/result works where specified
[ ] Score correct
[ ] Completion correct
[ ] Transition to Results correct
[ ] Accessibility
27. Scoring DoD

Scoring must use the authoritative scoring specification.

Current score allocation:

Snap        300
Shift       300
Crowd Call  300
Consistency 100
Total      1000

Verify only finalized formulas.

[ ] Snap scoring verified
[ ] Shift scoring verified
[ ] Crowd Call scoring verified
[ ] Consistency scoring verified
[ ] Total score verified
[ ] Boundary values tested
[ ] Invalid inputs tested
[ ] Rounding tested
[ ] Determinism tested

If exact formulas remain pending:

[ ] Mark scoring verification PENDING
[ ] Do not invent formula
28. Results DoD

Verify:

[ ] Score shown correctly
[ ] Score reveal works
[ ] Percentile shown where available
[ ] Improvement shown where available
[ ] Challenge breakdown correct
[ ] Rival comparison correct
[ ] Personal best correct
[ ] Next action correct
[ ] No stale result
[ ] No duplicate result
29. Rival DoD

Verify:

[ ] Correct rival
[ ] Correct rival score
[ ] Correct user score
[ ] Correct score gap
[ ] Beat CTA correct
[ ] Match history correct where available
[ ] No global leaderboard
[ ] No unrelated social functionality
30. Friends DoD

Verify:

[ ] Friends load
[ ] Today's scores load
[ ] Add Friend works
[ ] Empty state works
[ ] Error state works
[ ] Loading state works
[ ] Duplicate friend action handled
[ ] No chat
[ ] No social feed
[ ] No unnecessary contact permission
31. Add Friend DoD

Verify:

[ ] Battle Code input
[ ] Validation
[ ] Add action
[ ] Success state
[ ] Failure state
[ ] Duplicate friend handling
[ ] Own-code behavior
[ ] Copy behavior
[ ] Accessibility
32. Profile DoD

Verify:

[ ] Battle DNA
[ ] Momentum
[ ] Best score
[ ] Average
[ ] Records
[ ] Correct data
[ ] Loading
[ ] Empty/error behavior where relevant
[ ] No unsupported IQ claims
[ ] No unsupported scientific claims
33. History DoD

Verify:

[ ] History data loads
[ ] Graph displays correctly
[ ] Best score correct
[ ] Average correct
[ ] Momentum correct
[ ] Recent Battles correct
[ ] Empty state
[ ] Error state
[ ] Loading state
[ ] Responsive layout
34. Settings DoD

Verify:

[ ] Sound setting
[ ] Haptic setting
[ ] Notification setting where applicable
[ ] Battle Name/account controls
[ ] Privacy controls
[ ] Delete account where applicable
[ ] Terms
[ ] About

Only approved settings should exist.

Do not add Light/Dark/System theme selection if it is not part of the current V1 specification.

35. State DoD

Every relevant feature must support the states defined by its specification.

General states:

Loading
Success
Empty
Error
Disabled

Gameplay:

Ready
Active
Correct
Incorrect
Complete

Battle:

Not Started
In Progress
Completed

Friend:

Pending
Accepted
Blocked

Only applicable states need to exist on each feature.

36. Loading DoD

Verify:

[ ] Loading is visible when required
[ ] User understands that work is occurring
[ ] Repeated action is controlled
[ ] Loading eventually resolves
[ ] No indefinite loading
[ ] Loading is accessible
37. Error DoD

Verify:

[ ] Error is understandable
[ ] Error is scoped correctly
[ ] Retry exists when appropriate
[ ] Retry is safe
[ ] Technical details are not exposed unnecessarily
[ ] Accessibility semantics exist
38. Empty-State DoD

Verify:

[ ] Empty state is distinct from error
[ ] Correct explanation
[ ] Appropriate next action
[ ] No misleading success state
39. Offline DoD

Where offline behavior applies:

[ ] Offline state detected
[ ] Correct cached data behavior
[ ] Correct unavailable behavior
[ ] No false success
[ ] No duplicate mutation
[ ] Reconnection handled
[ ] Reconciliation handled where required
40. Lifecycle DoD

Verify relevant states across:

[ ] Background
[ ] Foreground
[ ] Screen recreation
[ ] Process death
[ ] App restart

Critical Battle state must not be accidentally lost or duplicated.

41. Duplicate Interaction DoD

Test rapid repeated interaction for:

[ ] Play Battle
[ ] Start Battle
[ ] Challenge answer
[ ] Submit
[ ] Retry
[ ] Add Friend
[ ] Copy
[ ] Share
[ ] Purchase where applicable

Expected:

One logical action
42. Data DoD

Verify:

[ ] Domain models correct
[ ] DTO mapping correct
[ ] Persistence mapping correct
[ ] Null handling correct
[ ] Error mapping correct
[ ] IDs correct
[ ] Timestamps correct
[ ] Data invariants preserved
43. API DoD

Where API functionality exists:

[ ] Request correct
[ ] Response mapping correct
[ ] Error mapping correct
[ ] Timeout handled
[ ] Unknown result handled
[ ] Idempotency handled
[ ] Authentication handled where applicable
[ ] Authorization handled where applicable

Do not invent undocumented API behavior.

44. Persistence DoD

Verify:

[ ] Required data survives restart
[ ] Required data survives lifecycle recreation
[ ] Required Battle state survives interruption
[ ] No duplicate records
[ ] No accidental data loss
[ ] Migration tested where applicable
45. Accessibility DoD

Follow 11_ACCESSIBILITY.md.

Verify:

[ ] Minimum touch targets
[ ] Semantic labels
[ ] Screen-level semantics
[ ] Focus order
[ ] Screen reader behavior
[ ] Dynamic text
[ ] Color independence
[ ] Correct/incorrect state semantics
[ ] Loading semantics
[ ] Error semantics
[ ] Reduced motion
46. Motion DoD

Verify:

[ ] Navigation motion correct
[ ] Game transitions correct
[ ] Score reveal correct
[ ] Button feedback correct
[ ] Animations cancel safely
[ ] No stuck animation
[ ] No duplicate animation
[ ] Reduced-motion behavior
47. Haptic DoD

Verify:

[ ] Button haptic
[ ] Correct-answer haptic
[ ] Incorrect-answer haptic
[ ] Personal-best haptic
[ ] Battle-complete haptic
[ ] Toggle behavior
[ ] No duplicate vibration
48. Sound DoD

Verify:

[ ] Tap sound
[ ] Correct sound
[ ] Incorrect sound
[ ] Score reveal
[ ] Personal best
[ ] Battle complete
[ ] Sound toggle
[ ] No unintended continuous music
49. Analytics DoD

Where analytics is implemented:

[ ] Correct event
[ ] Correct trigger
[ ] Correct properties
[ ] Official/Practice distinction
[ ] No duplicate event
[ ] No sensitive data
[ ] Analytics failure does not block gameplay
[ ] Test environment separated from production
50. Monetization DoD

Only applicable after monetization is explicitly approved.

Verify:

[ ] Approved monetization model
[ ] Clear pricing
[ ] Clear entitlement
[ ] Purchase flow
[ ] Restore flow
[ ] Failure handling
[ ] Offline behavior
[ ] Duplicate purchase protection
[ ] Accessibility
[ ] No pay-to-win
[ ] No Official Battle manipulation

If monetization is not approved:

[ ] No billing system
[ ] No ad SDK
[ ] No premium system
[ ] No coins
51. Security DoD

Verify:

[ ] No secrets in source
[ ] No credentials in logs
[ ] No unnecessary sensitive data
[ ] Correct authentication boundaries
[ ] Correct authorization boundaries
[ ] Appropriate entitlement validation
[ ] Secure configuration
52. Privacy DoD

Verify:

[ ] Only approved data collected
[ ] No unnecessary permissions
[ ] No unnecessary personal data
[ ] Analytics payload reviewed
[ ] Friend data protected
[ ] Account deletion behavior defined where applicable
53. Performance DoD

Verify:

[ ] App launches correctly
[ ] Navigation remains responsive
[ ] Gameplay remains responsive
[ ] Animations remain smooth
[ ] No obvious memory leaks
[ ] No excessive network calls
[ ] No obvious resource abuse

Exact numerical thresholds remain governed by the pending performance decisions.

54. Crash DoD

Verify:

[ ] Cold start
[ ] Warm start
[ ] Background/foreground
[ ] Battle flow
[ ] Network failure
[ ] Invalid input
[ ] Process recreation
[ ] Process death

No reproducible critical crash may remain.

55. Visual QA DoD

For UI tasks, verify:

[ ] 390×844
[ ] 412×915
[ ] 360×800
[ ] Typography
[ ] Spacing
[ ] Alignment
[ ] Cards
[ ] Buttons
[ ] Navigation
[ ] Icons
[ ] States
[ ] Safe areas

Provide visual evidence where practical.

56. Test DoD

Relevant automated tests must pass.

Potential categories:

[ ] Unit
[ ] Domain
[ ] Gameplay
[ ] State machine
[ ] Repository
[ ] API
[ ] Persistence
[ ] UI
[ ] Navigation
[ ] Lifecycle
[ ] Offline
[ ] Accessibility

Only applicable categories are required for a given task.

57. Regression DoD

After a change:

[ ] Affected feature retested
[ ] Affected navigation retested
[ ] Relevant E2E flow retested
[ ] Related states retested
[ ] Existing accepted behavior remains intact

Major gameplay changes require broader regression.

58. Build DoD

The application must:

[ ] Compile
[ ] Build successfully
[ ] Install
[ ] Launch
[ ] Reach relevant feature

A compile-only result is insufficient.

59. Dependency DoD

For every newly introduced dependency:

[ ] Purpose documented
[ ] Version documented
[ ] Reason documented
[ ] Security reviewed
[ ] Privacy reviewed
[ ] Permissions reviewed
[ ] Build impact reviewed
[ ] Approval obtained where required
60. Code Quality DoD

Verify:

[ ] Appropriate architecture
[ ] Clear state ownership
[ ] No unnecessary duplication
[ ] No unnecessary abstractions
[ ] No obvious dead code
[ ] No required TODOs
[ ] No debug-only behavior in production path
[ ] No hidden product logic in UI
61. Architecture DoD

Verify:

[ ] UI boundaries respected
[ ] Presentation boundaries respected
[ ] Domain boundaries respected
[ ] Repository boundaries respected
[ ] Data boundaries respected
[ ] Business logic not duplicated
[ ] Provider-specific code isolated where appropriate
62. Documentation DoD

For a completed feature:

[ ] Requirement documented
[ ] Relevant architecture documented
[ ] Relevant data contract documented
[ ] Relevant error behavior documented
[ ] Relevant QA documented
[ ] Deviations documented
[ ] Pending decisions documented
63. Deviation DoD

If implementation differs from specification:

[ ] DEV-XXX created
[ ] Reason documented
[ ] Impact documented
[ ] Proposed solution documented
[ ] Approval status documented
[ ] Tests updated

No undocumented deviation is considered acceptable.

64. Pending Decision DoD

If implementation depends on an unresolved decision:

[ ] PENDING item identified
[ ] Affected implementation documented
[ ] Affected tests documented
[ ] Work stopped if necessary
[ ] Decision requested

Do not mark the dependent feature fully DONE.

65. Evidence DoD

A completed task should have appropriate evidence.

Examples:

[ ] Screenshot
[ ] Screen recording
[ ] Test output
[ ] Build output
[ ] Logs
[ ] Accessibility verification
[ ] API verification

Evidence must correspond to the actual implementation version being accepted.

66. Antigravity Implementation Report DoD

Every completed implementation task must produce:

TASK:
STATUS:

OBJECTIVE:

IMPLEMENTED:

REQUIREMENTS VERIFIED:

FILES CHANGED:

TESTS RUN:

TEST RESULTS:

BUILD:

UI/UX:

ACCESSIBILITY:

LIFECYCLE:

OFFLINE/ERROR:

DEPENDENCIES:

DEVIATIONS:

KNOWN ISSUES:

EVIDENCE:

NEXT RECOMMENDED TASK:
67. Recommendation Rule

The implementation report may include:

NEXT RECOMMENDED TASK

but this does not mean Antigravity may automatically execute it.

The next task requires explicit authorization.

68. Four-Audit DoD

For major milestones, complete:

[ ] Product Audit
[ ] UI/UX Audit
[ ] Code/Architecture Audit
[ ] Edge-Case Audit

Each audit must produce findings.

69. Product Audit DoD

Verify:

[ ] Product intent preserved
[ ] Core loop preserved
[ ] Gameplay correct
[ ] Social model correct
[ ] Official/Practice separation correct
[ ] No unauthorized features
[ ] No scope creep
70. UI/UX Audit DoD

Verify:

[ ] Visual hierarchy
[ ] Design system
[ ] Spacing
[ ] Typography
[ ] Interaction states
[ ] Navigation
[ ] Accessibility
[ ] Motion
[ ] Responsive behavior
71. Code/Architecture Audit DoD

Verify:

[ ] Layer boundaries
[ ] State ownership
[ ] Repository boundaries
[ ] Data separation
[ ] Error handling
[ ] Dependency discipline
[ ] Testability
[ ] Maintainability
72. Edge-Case Audit DoD

Verify:

[ ] Offline
[ ] Timeout
[ ] Retry
[ ] Duplicate tap
[ ] Duplicate submission
[ ] Process death
[ ] Backgrounding
[ ] Empty data
[ ] Malformed data
[ ] Invalid input
[ ] Recovery
73. Milestone DoD

A milestone is DONE only when:

[ ] All milestone requirements implemented
[ ] All applicable feature DoDs passed
[ ] Regression passed
[ ] Four audits completed
[ ] No critical defects
[ ] Blocking deviations resolved
[ ] Documentation updated
[ ] Evidence collected
[ ] Git checkpoint created
74. Release Candidate DoD

A release candidate must satisfy:

[ ] Product QA PASS
[ ] Gameplay QA PASS
[ ] Navigation QA PASS
[ ] UI/UX QA PASS
[ ] Accessibility QA PASS
[ ] Lifecycle QA PASS
[ ] Offline/Error QA PASS
[ ] Data/API QA PASS
[ ] Regression QA PASS
[ ] Build verification PASS
[ ] Security review PASS
[ ] Privacy review PASS where applicable
[ ] Four audits PASS
75. Release Blockers

The following are release blockers unless explicitly waived:

[ ] P0 defect
[ ] Official Battle score corruption
[ ] Duplicate official result
[ ] Unrecoverable Battle state
[ ] Critical security issue
[ ] Critical data loss
[ ] Application cannot launch
[ ] Critical gameplay path broken
[ ] Major accessibility failure
[ ] Unauthorized production behavior
76. P1 Defects

P1 defects require explicit review before release.

A release must not silently ignore a P1 issue.

Document:

Issue
Impact
Workaround
Risk
Decision
Owner
77. P2 / P3 Defects

P2/P3 issues may be deferred when:

[ ] Documented
[ ] Understood
[ ] Non-blocking
[ ] Product/QA acceptance exists
[ ] Follow-up is tracked
78. Git Checkpoint DoD

After successful acceptance:

[ ] Changes committed
[ ] Working tree checked
[ ] Build verified
[ ] Tests verified
[ ] Checkpoint identified

A known-good state should be recoverable.

79. No Partial Completion Rule

Do not call a feature:

DONE

when it is actually:

UI complete
but
logic incomplete

or:

happy path complete
but
error path missing

or:

works locally
but
not tested

Use accurate status:

PARTIAL
BLOCKED
NOT TESTED
PENDING
80. Temporary Implementation Rule

Temporary scaffolding may exist during development only if:

[ ] Clearly identified
[ ] Isolated
[ ] Not mistaken for production behavior
[ ] Has replacement plan
[ ] Does not compromise product correctness

It must not be presented as final.

81. Mock Backend DoD

If a fake/mock backend is used:

[ ] Clearly separated
[ ] Deterministic where needed
[ ] Production integration boundary preserved
[ ] No fake data presented as real production data
[ ] Replacement path documented
82. Test Data DoD

Test data must be:

[ ] Explicit
[ ] Reproducible
[ ] Isolated
[ ] Non-sensitive
[ ] Suitable for automated testing
83. Production Data DoD

Production data must not be used casually for testing.

If production-like behavior is required, use controlled fixtures or approved test environments.

84. Security Secret DoD

Before acceptance:

[ ] No API secrets in source
[ ] No private keys in repository
[ ] No passwords in repository
[ ] No production tokens in logs
[ ] No accidental credentials in screenshots/evidence
85. Permission DoD

Before release:

[ ] Every permission has an approved purpose
[ ] No unnecessary permission
[ ] Runtime request occurs only when needed
[ ] Permission denial handled
[ ] Permission state handled
86. Network DoD

Where network is required:

[ ] Online success
[ ] Offline
[ ] Slow network
[ ] Timeout
[ ] Server error
[ ] Unknown result
[ ] Retry
[ ] Reconnection
87. Analytics DoD for Release

If analytics is enabled:

[ ] Production events verified
[ ] Test events isolated
[ ] No sensitive payloads
[ ] Official/Practice separation verified
[ ] Provider failure safe
88. Monetization DoD for Release

If monetization is enabled:

[ ] Approved model
[ ] Billing tested
[ ] Restore tested
[ ] Entitlement tested
[ ] Pricing verified
[ ] Purchase failure tested
[ ] Privacy reviewed
[ ] No pay-to-win
[ ] No Official Battle manipulation

If monetization is not approved:

[ ] No monetization infrastructure shipped
89. Final Manual QA

Before release, manually execute the primary path:

Fresh Install
    ↓
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
    ↓
Home

Then verify:

[ ] App restart
[ ] Returning user
[ ] Completed Battle state
[ ] Practice
[ ] Friends
[ ] Profile
[ ] History
[ ] Settings
90. Final Device QA

At minimum:

[ ] 390×844
[ ] 412×915
[ ] 360×800

Additional device/OS coverage remains governed by the supported-device decision.

91. Final Accessibility QA

Before release:

[ ] Screen reader
[ ] Large text
[ ] Touch target inspection
[ ] Color independence
[ ] Focus order
[ ] Reduced motion
[ ] Error announcements
[ ] Loading announcements
[ ] Gameplay instructions
92. Final Edge-Case QA

Before release:

[ ] Airplane mode
[ ] Network loss during Battle
[ ] Network loss during submission
[ ] Rapid taps
[ ] Background during Battle
[ ] Process death during Battle
[ ] App restart after completion
[ ] Empty Friends
[ ] Empty History
[ ] Invalid Battle Code
[ ] Duplicate Friend
93. Final Product Audit

The final product audit must answer:

Does the application still represent Daily Battle as specified?

Verify:

[ ] 3 challenges
[ ] ~3 minutes
[ ] One official score
[ ] Friend rivalry
[ ] Personal improvement
[ ] Premium product feel
[ ] No generic dashboard drift
[ ] No global leaderboard
[ ] No coins
[ ] No chat
[ ] No infinite feed
[ ] No pay-to-win
94. Final Architecture Audit

Verify:

[ ] No accidental architecture shortcuts
[ ] No duplicated business logic
[ ] No hidden dependencies
[ ] No provider lock-in without approval
[ ] State ownership is clear
[ ] Persistence boundaries are clear
[ ] API boundaries are clear
[ ] Gameplay remains testable
95. Final Documentation Audit

Before release:

[ ] Product docs current
[ ] Gameplay docs current
[ ] Screen docs current
[ ] Design system current
[ ] Navigation current
[ ] Architecture current
[ ] Data/API current
[ ] Error handling current
[ ] Accessibility current
[ ] QA current
[ ] Monetization current
[ ] Analytics current
[ ] Antigravity rules current
[ ] Deviations recorded
[ ] Pending decisions recorded
96. Final Acceptance Record

A final release acceptance should contain:

Release:
Version:
Date:

Product QA:
PASS / FAIL

UI/UX QA:
PASS / FAIL

Gameplay QA:
PASS / FAIL

Navigation QA:
PASS / FAIL

Accessibility QA:
PASS / FAIL

Lifecycle QA:
PASS / FAIL

Offline/Error QA:
PASS / FAIL

Data/API QA:
PASS / FAIL

Security:
PASS / FAIL

Privacy:
PASS / FAIL

Analytics:
PASS / FAIL / N/A

Monetization:
PASS / FAIL / N/A

Regression:
PASS / FAIL

Build:
PASS / FAIL

P0:
Count

P1:
Count

P2:
Count

P3:
Count

Deviations:
List

Known Issues:
List

Final Decision:
ACCEPTED / NOT ACCEPTED

Decision Owner:
Date:
97. Final Acceptance Rule

The final decision must be explicit.

Valid:

ACCEPTED

or:

NOT ACCEPTED

Do not use ambiguous status such as:

Looks good
Mostly done
Probably ready
Should work
98. Definition of Done — Complete Checklist
Product
[ ] Product requirements implemented
[ ] Core loop preserved
[ ] No unauthorized features
[ ] No scope creep
Design
[ ] Design system followed
[ ] Visual hierarchy correct
[ ] Responsive behavior correct
[ ] States correct
Gameplay
[ ] Snap complete
[ ] Shift complete
[ ] Crowd Call complete
[ ] Scoring verified where finalized
[ ] Official Battle integrity verified
[ ] Practice isolated
Navigation
[ ] Onboarding
[ ] Home
[ ] Battle
[ ] Results
[ ] Rival
[ ] Friends
[ ] Profile
[ ] History
[ ] Settings
Technical
[ ] Architecture
[ ] Data
[ ] Persistence
[ ] API
[ ] Lifecycle
[ ] Error handling
[ ] Offline handling
Quality
[ ] Unit tests
[ ] Integration tests
[ ] UI tests
[ ] E2E tests
[ ] Regression
[ ] Manual QA
Accessibility
[ ] Semantics
[ ] Touch targets
[ ] Screen reader
[ ] Dynamic text
[ ] Color independence
[ ] Reduced motion
Reliability
[ ] Duplicate tap
[ ] Duplicate submission
[ ] Timeout
[ ] Network failure
[ ] Process death
[ ] Recovery
Security / Privacy
[ ] Secrets protected
[ ] Permissions reviewed
[ ] Data minimization
[ ] Sensitive data protected
Governance
[ ] Requirements traced
[ ] Deviations recorded
[ ] Pending decisions recorded
[ ] Evidence collected
[ ] Implementation report completed
[ ] Four audits completed
[ ] Git checkpoint created
99. What "DONE" Means

For Daily Battle:

DONE means the approved product behavior has been implemented, verified, tested, audited, documented, and accepted — with no unresolved blocking issue or unauthorized product deviation.

It does not mean:

"The code was written."

It does not mean:

"The app builds."

It does not mean:

"The happy path works."

It means the complete applicable acceptance contract has been satisfied.

100. Antigravity Final Rule

Before declaring any task DONE, Antigravity must ask:

1. Did I implement exactly what was specified?
2. Did I avoid inventing anything?
3. Did I test the relevant behavior?
4. Did I test the relevant failure states?
5. Did I verify the UI?
6. Did I verify accessibility?
7. Did I verify lifecycle behavior where relevant?
8. Did I verify offline/error behavior where relevant?
9. Did I check for duplicate actions?
10. Did I check for unauthorized changes?
11. Did I report all dependencies?
12. Did I report all deviations?
13. Did I provide evidence?
14. Did I update the relevant documentation?
15. Did I produce the implementation report?
16. Did I stop rather than automatically continue?

If any applicable answer is NO, the task is not yet DONE.

101. Final Project Principle

The Daily Battle implementation is successful when:

SPECIFICATION
      ↓
IMPLEMENTATION
      ↓
VERIFICATION
      ↓
AUDIT
      ↓
ACCEPTANCE

produces the same intended product at the end as was defined at the beginning.

Build only what is approved.
Verify what is built.
Document what changed.
Fix what fails.
Stop when complete.