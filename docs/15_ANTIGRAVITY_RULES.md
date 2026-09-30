# Daily Battle — Antigravity Rules

**Document:** `15_ANTIGRAVITY_RULES.md`  
**Status:** LOCKED  
**Product:** Daily Battle  
**Platform:** Android  
**Primary implementation agent:** Antigravity  
**Product authority:** User / Product Owner  

**Purpose:**  
This document defines the operating contract that Antigravity must follow while implementing Daily Battle.

Antigravity is the implementation agent.

It is **not** the product owner.

It must implement the documented product rather than independently redesigning, expanding, simplifying, or interpreting it.

---

# 1. Core Principle

The implementation process is:

```text
PRODUCT DECISION
      ↓
SPECIFICATION
      ↓
ARCHITECTURE
      ↓
IMPLEMENTATION
      ↓
TESTING
      ↓
AUDIT
      ↓
ACCEPTANCE
      ↓
CHECKPOINT
      ↓
NEXT TASK

Antigravity operates primarily in the implementation stage.

It must not silently replace earlier stages with its own assumptions.

2. Authority Model

Daily Battle has three distinct authorities.

2.1 Product Authority

User / Product Owner

Responsible for:

product decisions,
feature approval,
UX direction,
gameplay rules,
monetization decisions,
scope decisions,
final acceptance.
2.2 Architecture / Specification Authority

The project documentation defines:

technical architecture,
data contracts,
screen architecture,
navigation,
design system,
gameplay rules,
error handling,
accessibility,
testing requirements.
2.3 Implementation Authority

Antigravity

Responsible for:

writing implementation code,
integrating approved systems,
running builds,
running tests,
fixing implementation defects,
reporting deviations,
providing evidence.

Antigravity does not gain product authority merely because it can technically implement something.

3. Documentation Hierarchy

When deciding how to implement a feature, Antigravity must consult the documentation in this order:

00_MASTER_SPEC.md
        ↓
01_PRODUCT.md
        ↓
02_GAMEPLAY.md
        ↓
03_SCREEN_ARCHITECTURE.md
        ↓
04_SCREEN_BLUEPRINTS.md
        ↓
05_DESIGN_SYSTEM.md
        ↓
06_NAVIGATION_AND_FLOWS.md
        ↓
07_TECHNICAL_ARCHITECTURE.md
        ↓
08_DATA_AND_API.md
        ↓
09_OFFLINE_AND_ERROR_HANDLING.md
        ↓
10_ANIMATION_HAPTICS.md
        ↓
11_ACCESSIBILITY.md
        ↓
12_TESTING_AND_QA.md
        ↓
13_MONETIZATION.md
        ↓
14_ANALYTICS.md
        ↓
15_ANTIGRAVITY_RULES.md

If two documents appear to conflict:

Do not silently choose one.

Record the conflict and request a decision.

4. Source-of-Truth Rule

The documentation repository is the implementation source of truth.

The Android code is not the source of truth for product requirements.

Therefore:

Code says X
Spec says Y
        ↓
Do not assume X is correct
        ↓
Report discrepancy
5. No Assumption Rule

Antigravity must not invent missing requirements.

Examples:

No scoring formula
→ Do not invent one.

No authentication decision
→ Do not choose one silently.

No backend architecture
→ Do not create an arbitrary backend.

No monetization model
→ Do not add billing.

No ad decision
→ Do not add ads.

No exact API endpoint
→ Do not fabricate an endpoint.

No screen architecture
→ Do not create a new screen automatically.
6. STOP Rule

When a missing decision materially affects implementation:

STOP
↓
Identify ambiguity
↓
Document it
↓
Explain implementation impact
↓
Ask for decision

Do not proceed by guessing.

7. What Counts as a Material Ambiguity

An ambiguity is material if different interpretations could change:

user experience,
gameplay,
score,
data model,
API contract,
navigation,
security,
privacy,
monetization,
architecture,
persistence,
testing,
release behavior.

Minor implementation details may be chosen by Antigravity when they do not alter approved behavior.

8. Reasonable Implementation Freedom

Antigravity may make implementation-level decisions when:

The product behavior is already defined.
The architecture allows multiple equivalent implementations.
The decision does not affect user-visible behavior.
The decision does not introduce unnecessary dependencies.
The decision does not create a future architectural constraint.

Examples:

private helper naming,
internal utility organization,
local variable names,
equivalent implementation details.

These decisions must remain subordinate to the specification.

9. No Product Invention

Antigravity must not introduce:

new screens,
new game modes,
new currencies,
new social systems,
new AI features,
new dashboards,
new leaderboards,
new monetization,
new permissions,
new notifications,

unless explicitly approved.

10. Explicit Do-Not-Build List

The following are outside the current product unless separately approved:

Global leaderboard
Coins
Gems
Energy
Lives
Chat
Social feed
Likes
Followers
Generic quiz feed
Infinite content feed
Photo proof as the main game
IQ claims
Scientific brain-training claims
Real-time multiplayer
Fake LIVE indicators
Excessive AI branding
AI-generated game rules
Pay-to-win mechanics
Paid Official Battle retries
Casino-like reward mechanics
11. Core Product Loop Must Remain Intact

Antigravity must preserve:

Open App
    ↓
Today's Battle
    ↓
3 Challenges
    ↓
Score
    ↓
Compare
    ↓
Return Tomorrow

Any implementation that makes this loop harder to access without an approved reason must be reported.

12. One Screen = One Dominant Job

The screen architecture defines a dominant job for each screen.

Examples:

Home       → Play
Snap       → React
Shift      → Remember
Crowd Call → Predict
Results    → Understand performance
Rival      → Beat someone
Friends    → Manage rivals
Profile    → Understand yourself
History    → See progress

Antigravity must not overload screens with unrelated features.

13. Screen Creation Rule

Before creating a new screen, Antigravity must verify:

Does an existing screen already own this responsibility?
Is the screen documented?
Is there a requirement ID?
Is the navigation path documented?
Is the screen included in the screen architecture?

If not:

STOP
→ request approval
14. Screen ID Rule

Primary screens must retain their stable screen IDs.

Current IDs:

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

Do not renumber existing screens without explicit approval.

15. Requirement Traceability

Every meaningful implementation task must reference requirement IDs.

Example:

Task:
Implement Home Today’s Battle card.

Requirements:
REQ-SCREEN-003
REQ-NAV-003
REQ-DESIGN-XXX

The exact applicable IDs must come from the authoritative documents.

16. Task Scope Rule

Every Antigravity task should have:

Task ID
Objective
Authoritative references
Scope
Non-goals
Requirements
States
Interactions
Edge cases
Acceptance criteria
Verification

If a task lacks enough information to implement safely, stop and ask.

17. Small Task Rule

Do not ask Antigravity to build the entire application in one uncontrolled task.

Implementation should proceed through small, verifiable slices.

Preferred:

App Shell
→ Home
→ Battle Intro
→ Snap
→ Shift
→ Crowd Call
→ Results
→ Rival
→ Friends
...

rather than:

"Build the complete Daily Battle app."
18. Vertical Slice Rule

Where practical, each implementation task should produce a complete vertical slice.

Example:

UI
 ↓
State
 ↓
Domain
 ↓
Repository
 ↓
Persistence/API
 ↓
Tests

Do not create large amounts of disconnected UI that cannot be verified.

19. Do Not Build Future Features Prematurely

Do not create architecture for hypothetical features simply because they may exist later.

Examples:

No premium system
→ no billing framework.

No ads
→ no ad SDK.

No chat
→ no chat infrastructure.

No authentication decision
→ no arbitrary authentication provider.

No backend decision
→ no arbitrary backend.

Future architecture should be introduced when the requirement becomes real.

20. Dependency Rule

Before adding any dependency, Antigravity must determine:

why it is needed,
whether existing platform functionality can solve the problem,
whether the architecture permits it,
whether it introduces privacy/security implications,
whether it creates unnecessary maintenance.

Every new dependency must be reported.

21. Dependency Report

For each new dependency:

Dependency:
Version:
Purpose:
Why needed:
Alternative considered:
Permissions:
Network behavior:
License:
Security implications:
Privacy implications:
Build impact:
Approved:

Do not silently add dependencies.

22. Architecture Rule

The implementation should respect the approved layered architecture.

Conceptually:

UI
 ↓
Presentation
 ↓
Domain
 ↓
Repository Interfaces
 ↓
Local / Remote Data

Do not bypass layers merely because a shortcut is easier.

23. Business Logic Boundary

Gameplay/business rules belong in the appropriate domain layer.

UI code must not become the authoritative source of:

scoring,
official Battle integrity,
rival calculations,
Battle state,
persistence rules.
24. Scoring Rule

Scoring must remain deterministic and centralized.

Antigravity must not duplicate score calculations across:

UI,
ViewModel,
repository,
API mapper,
analytics.

There must be a clear authoritative scoring boundary.

If scoring formulas are not finalized:

DO NOT INVENT FORMULA
25. Official Battle Integrity Rule

Official Battle must be treated as a protected transaction.

Antigravity must protect against:

duplicate starts,
duplicate submissions,
duplicate completion,
score replacement,
accidental replay,
Practice contamination.
26. Practice Isolation Rule

Practice must never silently alter Official Battle state.

Verify:

Practice
≠
Official

for:

score,
completion,
official result,
official ranking/percentile,
official Battle state.
27. Navigation Rule

Navigation must follow:

06_NAVIGATION_AND_FLOWS.md.

Antigravity must not invent navigation shortcuts that bypass documented product states.

28. Back Behavior

Back behavior must be explicitly defined.

If a screen's back behavior is not defined:

Do not invent product behavior.

Ask or document the ambiguity.

This is especially important during:

active gameplay,
incomplete official Battle,
submission,
result processing.
29. Bottom Navigation Rule

Primary bottom navigation:

Home
Battle
Friends
Me

Gameplay hides the bottom navigation.

Do not introduce a floating central Battle button unless explicitly approved.

30. Gameplay Isolation Rule

During gameplay, avoid unrelated:

profile controls,
friend controls,
social controls,
history controls,
bottom navigation.

The gameplay screen should maintain focus.

31. UI Design Rule

Antigravity must implement the approved design system.

Primary visual direction:

Premium interactive product with gaming energy.

Do not drift toward:

generic dashboard,
childish game UI,
cyberpunk,
casino,
excessive neon,
excessive glassmorphism,
giant glows,
AI-purple-gradient aesthetics.
32. Design Token Rule

Use the documented design tokens rather than arbitrary values.

Primary colors include:

Background: #080B10
Surface 1: #11161F
Surface 2: #171D28
Elevated: #1C2330

Primary text: #F5F7FA
Secondary text: #A2AAB8
Muted text: #6F7887
Disabled: #4D5563

Brand violet: #7C5CFC

Success: #39D98A
Warning: #FFB84D
Error: #FF5D73
Info: #4EA8FF

Challenge accents:

Snap: #4EA8FF
Shift: #FFB84D
Crowd Call: #39D98A

Do not introduce arbitrary colors for convenience.

33. Typography Rule

Primary typography uses Inter.

Important UI must not arbitrarily introduce unrelated fonts.

Current documented typography includes:

Display: 56 / 60 / 700
H1: 30 / 36 / 700
H2: 24 / 30 / 700
H3: 18 / 24 / 600
Body Large: 16 / 24 / 500
Body: 14 / 20 / 500
Caption: 12 / 16 / 500
Labels: 11–12 / 16 / 600

Important UI should not use text below 12px.

34. Spacing Rule

Use the documented 8px spacing system:

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

Exceptions require a design reason and should be reported when they affect visible layout.

35. Screen Size Rule

Master design size:

390 × 844

Validation sizes:

412 × 915
360 × 800

Do not assume every device is exactly 390×844.

36. Touch Target Rule

Interactive targets must respect:

Minimum: 44px
Preferred: 48px
37. State Rule

Every important interactive component must implement its required states.

Button states:

Default
Pressed
Disabled
Loading
Success

Game states:

Ready
Active
Correct
Incorrect
Complete

Do not implement only the default state.

38. Loading Rule

Loading must be an explicit state.

Do not:

freeze the UI,
silently ignore input,
display stale content as if it were current,
create indefinite spinners.

Loading behavior must follow the offline/error specification.

39. Error Rule

Errors must be:

understandable,
recoverable where possible,
appropriately scoped,
visually consistent,
accessible.

Do not expose raw technical exceptions as the user-facing message.

40. Empty State Rule

An empty state is not automatically an error.

Examples:

No friends yet
No history yet
No rival yet

must remain distinct from:

Failed to load friends
Failed to load history
41. Offline Rule

Offline behavior must follow:

09_OFFLINE_AND_ERROR_HANDLING.md.

Antigravity must not assume:

offline = error

without checking the documented behavior.

42. Retry Rule

Retry must be safe.

Repeated Retry actions must not create:

duplicate requests,
duplicate Battle sessions,
duplicate submissions,
duplicate friend actions.
43. Lifecycle Rule

The application must handle:

backgrounding,
foregrounding,
process recreation,
process death,
screen recreation,

according to the documented state model.

Never assume the Activity/process will remain alive throughout a Battle.

44. Persistence Rule

State that must survive lifecycle events must be stored through the approved persistence architecture.

Do not rely only on:

in-memory variables

for critical Battle state.

45. Network Rule

Network operations must:

handle timeout,
handle failure,
handle unknown result,
avoid duplicate mutations,
map errors consistently.

Do not assume:

=
server did not receive request

This is especially important for official submissions.

46. Idempotency Rule

Mutating operations must be designed/tested for duplicate execution where required.

Examples:

Start Battle
Submit Challenge
Complete Battle
Add Friend
Purchase

Repeated execution must not corrupt product state.

47. Analytics Rule

Analytics must remain observational.

Antigravity must not:

block gameplay waiting for analytics,
modify score based on analytics,
add unsupported user profiling,
log sensitive information.

See 14_ANALYTICS.md.

48. Monetization Rule

Until explicitly approved:

No billing
No ads
No subscriptions
No premium
No coins
No pay-to-win

See 13_MONETIZATION.md.

49. Security Rule

Antigravity must not commit:

API secrets,
private keys,
production credentials,
passwords,
signing secrets,
tokens.

Secrets must use the approved secure configuration mechanism.

50. Privacy Rule

Do not collect or persist data simply because it may be useful later.

Any new personal-data collection requires explicit review.

Examples requiring caution:

contacts,
precise location,
camera,
microphone,
identifiers,
friend data.
51. Permission Rule

Do not request permissions unless an approved feature requires them.

Do not request permissions:

"just in case,"
for future features,
for analytics convenience,
because a library requests them unnecessarily.
52. Accessibility Rule

Accessibility is not a final polish step.

It must be considered during implementation.

At minimum verify:

touch targets,
semantics,
focus order,
screen-reader labels,
state announcements,
color independence,
dynamic text,
reduced motion.

See 11_ACCESSIBILITY.md.

53. Motion Rule

Motion must communicate:

state change,
feedback,
reward,
transition.

Do not animate everything.

Documented timing categories include:

Normal navigation: 200–300ms
Game transitions: 250–400ms
Score reveal: 700–1000ms
Button press: 100–150ms
54. Haptics Rule

Haptics must be intentional.

Examples:

Light → button
Success → correct
Short error → incorrect
Stronger → personal best
Medium → battle complete

Do not use haptics continuously or excessively.

55. Sound Rule

Sound effects should correspond to meaningful feedback.

No continuous background music is part of the current product direction.

Sound settings must be respected.

56. Testing Rule

No feature is complete merely because it compiles.

Every task must verify relevant:

Functional behavior
UI behavior
Navigation
States
Edge cases
Accessibility
Lifecycle
Offline behavior
Regression

See 12_TESTING_AND_QA.md.

57. Build Rule

After meaningful implementation:

Build
→ Test
→ Verify

A successful build does not equal acceptance.

58. Screenshot Rule

For UI tasks, Antigravity should provide screenshots or equivalent visual evidence where practical.

Evidence should demonstrate:

correct screen,
correct state,
correct dimensions,
relevant interaction result.
59. No False Verification

Antigravity must never report:

PASS

for something it did not actually verify.

If unable to test:

NOT TESTED

or:

BLOCKED

must be reported.

60. Test Evidence

When reporting successful verification, include appropriate evidence:

Test name
Environment
Expected
Actual
Result
Evidence
61. Implementation Report

After every task, Antigravity must produce:

TASK:
STATUS:

OBJECTIVE:
...

IMPLEMENTED:
...

REQUIREMENTS:
- REQ-XXX
- REQ-XXX

FILES CHANGED:
- ...

TESTS:
- ...

TEST RESULTS:
- ...

BUILD:
PASS / FAIL

VISUAL VERIFICATION:
PASS / FAIL / NOT TESTED

ACCESSIBILITY:
PASS / FAIL / NOT TESTED

LIFECYCLE:
PASS / FAIL / NOT TESTED

OFFLINE / ERROR:
PASS / FAIL / NOT TESTED

DEPENDENCIES:
None / list

DEVIATIONS:
None / DEV-XXX

KNOWN ISSUES:
...

EVIDENCE:
...

NEXT RECOMMENDED TASK:
...
62. Recommendation ≠ Authorization

The final line:

NEXT RECOMMENDED TASK

is informational only.

Antigravity must not automatically begin the next task.

The user must authorize the next implementation task.

63. Deviation Rule

If implementation cannot exactly follow the specification:

STOP
↓
Create DEV-XXX
↓
Explain reason
↓
Describe impact
↓
Propose alternative
↓
Request approval

Do not silently substitute behavior.

64. Deviation Record

Each deviation should contain:

DEV-XXX

Requirement:
Affected area:
Problem:
Why exact implementation is not possible:
Proposed deviation:
User impact:
Technical impact:
Testing impact:
Dependencies:
Approval:
Status:

Possible status:

PROPOSED
APPROVED
REJECTED
IMPLEMENTED
REVERTED
65. Architecture Deviation Rule

Technical limitations do not automatically authorize product changes.

Example:

Technical limitation
→ proposed architecture change

not:

Technical limitation
→ silently change UX
66. Product Deviation Rule

If Antigravity believes the documented product is technically problematic, it may report the issue.

It must not independently redesign the product.

Correct flow:

Problem
↓
Evidence
↓
Recommendation
↓
Product decision
↓
Updated specification
↓
Implementation
67. Refactoring Rule

Refactoring is allowed when it:

improves maintainability,
preserves behavior,
does not alter product requirements,
does not expand scope unnecessarily.

Large architectural refactors require reporting.

68. Scope Creep Rule

During implementation, Antigravity may discover adjacent improvements.

Examples:

"While implementing Friends, we should add chat."
"While implementing Results, we should add a global leaderboard."
"While implementing Profile, we should add achievements."

These are not automatic tasks.

Record them as suggestions only.

69. TODO Rule

Do not leave required production behavior as:

TODO
FIXME
placeholder
fake implementation
temporary hardcoded result

unless explicitly approved as temporary development scaffolding.

Any remaining TODO affecting Definition of Done must be reported.

70. Mock Data Rule

Mock/fake data is permitted for development when required.

But:

it must be clearly isolated,
it must not accidentally ship as production behavior,
it must not be mistaken for real backend behavior.
71. Hardcoded Data Rule

Hardcoded values are acceptable only when they represent approved static product configuration.

Do not hardcode:

production API results,
user-specific results,
scores,
friend relationships,
authentication state,
premium entitlements,

as a substitute for real architecture.

72. Environment Rule

Development/testing/production configurations must remain distinguishable.

Do not accidentally connect development builds to production destructive systems.

73. Logging Rule

Logs should help debugging without exposing sensitive information.

Never log:

passwords,
authentication tokens,
payment credentials,
private keys,
unnecessary personal data.
74. Error Logging Rule

Errors should preserve useful technical information for developers while presenting appropriate user-facing messages.

Conceptually:

Developer:
technical error + context

User:
clear actionable message

Do not expose raw stack traces to users.

75. API Rule

Do not invent API endpoints.

If API behavior is not documented:

STOP
→ report required API contract
76. Data Model Rule

Do not create domain fields simply because the UI currently wants them.

Every important persistent field should have a documented purpose.

77. Database Rule

Do not choose database technology solely because it is familiar.

The selected persistence technology must follow the approved architecture.

Current options may remain pending where documented.

78. Backend Rule

Backend implementation is subject to the approved technical/data architecture.

Antigravity must not silently introduce:

arbitrary backend providers,
arbitrary databases,
cloud services,
authentication providers,

without approval.

79. Authentication Rule

Authentication/account architecture remains subject to its pending decision.

Do not assume:

Google login
Firebase Auth
email/password
phone OTP

unless explicitly approved.

80. Notifications Rule

Notifications are not automatically enabled merely because they could improve retention.

Before implementation, define:

trigger,
permission,
frequency,
user controls,
deep-link destination,
privacy behavior.
81. Deep-Link Rule

Do not invent deep-link routes.

Every deep link should map to an approved navigation destination.

82. Share Rule

Sharing must use the approved share flow.

Do not add custom social integrations without approval.

83. Figma / Design Reference Rule

If a Figma screen or approved visual reference exists, it is a design reference.

Antigravity should reproduce the approved design intent rather than creating a visually unrelated interpretation.

If a design conflict exists between:

source specification,
Figma,
implementation,

report the conflict.

Do not silently pick one.

84. Design Drift Rule

Implementation should not gradually drift toward generic components.

Examples of drift:

Custom premium card
→ generic Material card

Custom CTA
→ generic default button

Premium dark layout
→ generic dashboard

Purposeful game feedback
→ generic snackbar

If the design specification calls for a custom treatment, preserve it.

85. Component Reuse Rule

Reusable components should be created where repetition is real.

Examples:

buttons,
cards,
score displays,
navigation items,
challenge headers,
result rows.

Do not create a component abstraction solely for theoretical reuse.

86. Component Consistency Rule

If two UI elements represent the same semantic component, they should share:

dimensions,
typography,
state behavior,
accessibility,
interaction model,

unless the specification explicitly distinguishes them.

87. Game Challenge Rule

Snap, Shift, and Crowd Call should feel like different challenge types.

Do not implement all three as visually identical quiz screens.

Their interaction models are intentionally different.

88. Snap Rule

Snap should prioritize:

reaction,
speed,
focus,
clean visual field.

Avoid clutter.

89. Shift Rule

Shift should prioritize:

memory,
precision,
grid interaction.

It should contrast with Snap.

90. Crowd Call Rule

Crowd Call should prioritize:

prediction,
social intuition,
answer selection,
crowd distribution/result.

It should be the most social-looking challenge while remaining part of the finite Battle.

91. Results Rule

Results should prioritize:

Score
↓
Percentile
↓
Improvement
↓
Breakdown
↓
Rival

Do not turn Results into a dashboard containing unrelated information.

92. Rival Rule

Rival should communicate:

One person
One score
One gap
One goal

Do not turn Rival into a global leaderboard.

93. Friends Rule

Friends is a competition network.

It is not:

a social feed,
chat,
likes,
comments,
followers.
94. Profile Rule

Profile represents the user's performance identity.

Battle DNA must remain grounded in gameplay data.

Do not make unsupported psychological/scientific claims.

95. History Rule

History should communicate improvement over time.

Do not create excessive analytical dashboards unless approved.

96. Settings Rule

Settings should remain calm and utility-focused.

Do not turn Settings into a game screen or monetization dashboard.

97. Performance Rule

Do not sacrifice gameplay responsiveness for architectural complexity.

At the same time, do not use performance as justification for breaking documented product behavior without approval.

98. Memory Rule

Avoid unnecessary retention of:

Activity references,
Context references,
listeners,
timers,
network callbacks,
large image/data objects.

Lifecycle correctness must be verified.

99. Timer Rule

Gameplay timers must belong to gameplay state rather than merely UI rendering.

A recomposition/re-render must not accidentally restart a challenge timer.

100. Randomness Rule

Where gameplay requires randomness:

define where randomness belongs,
ensure official Battle determinism/fairness,
separate Practice randomness where required,
ensure testability.

Do not introduce random behavior into Official Battle merely to make the game "more interesting."

101. Official Challenge Set Rule

Official Battle challenge content must follow the approved challenge-generation/distribution system.

Antigravity must not secretly adapt official challenges based on:

payment status,
user performance,
previous failures,
inferred ability,

unless explicitly specified.

102. AI Rule

AI is infrastructure, not the product's primary visible identity.

Do not add:

AI assistant screens,
AI chat,
excessive "AI-powered" labels,
AI-generated scores,
arbitrary AI personalization,

unless explicitly approved.

103. Real-World Challenge Rule

Real-world/Wild Card concepts are outside the current MVP unless explicitly promoted into scope.

Do not implement them as hidden future functionality.

104. No Fake LIVE Rule

Daily Battle does not have real-time multiplayer in the current model.

Do not label screens:

LIVE

unless there is actual live functionality and the specification is updated.

Prefer approved concepts such as:

READY
TODAY
PRACTICE
OFFICIAL
105. Product Language Rule

Use approved product terminology consistently.

Examples:

Daily Battle
Official Battle
Practice
Snap
Shift
Crowd Call
Results
Rival
Friends
Battle DNA
Battle Code

Do not rename concepts casually.

106. Copy Rule

Do not invent marketing/product copy for important screens if approved copy exists.

If copy is unspecified and materially affects UX:

Ask / flag for approval.

Minor implementation copy may be proposed but should be clearly identified.

107. Localization Rule

Localization is not currently assumed unless explicitly specified.

Do not introduce a localization architecture that materially changes scope without approval.

If localization is later required, UI must support text expansion appropriately.

108. Testing Before Completion

Before reporting completion, Antigravity should verify:

Build
Tests
UI
Navigation
States
Lifecycle
Offline/Error
Accessibility

Relevant categories only need to be tested for the affected scope, but critical Battle-path changes require regression.

109. Four-Audit Rule

Major milestones must undergo:

Product Audit
UI/UX Audit
Code/Architecture Audit
Edge-Case Audit

A task can pass implementation testing while still failing a product audit.

110. Audit Findings

Audit findings must be classified:

Critical
High
Medium
Low
Observation

Do not hide findings merely because the app technically works.

111. Release Rule

A release candidate must not contain unresolved critical defects.

Release requirements are governed by 12_TESTING_AND_QA.md.

112. Git Rule

Use version control checkpoints.

Recommended:

Before major task
→ checkpoint

After successful task
→ verified checkpoint

If a task introduces a regression:

stop
→ inspect
→ fix/revert
→ retest

Do not continue piling features onto an unstable baseline.

113. Commit Rule

Commits should be logically grouped.

Prefer:

Implement Home screen
Implement Snap challenge
Add Battle state handling
Add Friends flow

rather than one giant ambiguous commit containing unrelated changes.

Exact commit naming is implementation-level and may be chosen by Antigravity.

114. Change Control

Any change to a locked requirement requires:

Change request
↓
Reason
↓
Impact analysis
↓
Decision
↓
Specification update
↓
Implementation
↓
Regression test

Code changes alone do not redefine the product.

115. Specification Update Rule

If a product decision changes, update the relevant documentation before relying on the new behavior as the new source of truth.

Avoid:

Code changed
but docs unchanged

for finalized product decisions.

116. No Silent Reconciliation

If two documents contain inconsistent values:

Do not average them.
Do not choose whichever is easier.
Do not silently normalize them.

Create a decision item.

Example:

Source A:
Button radius = 14

Source B:
Button radius = 16

→ Record conflict
→ Request decision
117. Pending Status Rule

PENDING means:

Not yet authorized for implementation.

It does not mean:

Use your best judgment.

This distinction is mandatory.

118. Proposed Status Rule

PROPOSED means:

A possible solution has been identified but has not yet become authoritative.

Antigravity may implement it only after approval when it affects product/architecture.

119. Locked Status Rule

LOCKED means:

Implement according to this specification unless a formal change is approved.

120. User Testing Rule

When the user manually tests the app and reports behavior:

Treat the report as valuable evidence.

Do not dismiss it because automated tests passed.

Investigate:

Reported behavior
↓
Reproduce
↓
Identify requirement
↓
Fix
↓
Retest
121. Screenshot Review Rule

When screenshots are provided for review, compare them against:

screen blueprint,
design system,
spacing,
typography,
hierarchy,
states.

Do not only check whether the screen "looks good."

122. Visual QA Rule

Visual quality is judged against the approved product direction.

Questions include:

Does the hierarchy match?
Does the screen have one dominant job?
Are margins correct?
Are components aligned?
Are states clear?
Does it feel premium?
Is the gameplay focus preserved?
123. No Generic UI Substitution

Do not replace a specified component with a generic framework component merely because it is faster.

Example:

Specified custom score reveal
≠
generic snackbar

unless the specification permits the substitution.

124. Error Message Rule

User-facing error messages should describe:

What happened.
What the user can do next.

Avoid exposing implementation jargon.

125. Accessibility Failure Rule

Accessibility defects must not be dismissed as cosmetic.

If a user cannot:

identify a control,
activate a control,
understand a state,
distinguish important states,

the feature is not complete.

126. Analytics Failure Rule

Analytics failure is not a reason to fail the core Battle.

If the analytics provider is unavailable:

Battle continues.
127. Monetization Failure Rule

If monetization is later introduced, billing failure must not corrupt core gameplay.

A failed purchase must not:

alter score,
alter Battle state,
create fake entitlement,
prevent normal free gameplay.
128. Security Failure Rule

Any discovered security issue involving:

credentials,
authentication,
payment,
unauthorized data access,

must be reported immediately.

Do not defer critical security findings to cosmetic cleanup.

129. Data Integrity Rule

User data must remain internally consistent.

Examples:

Completed Battle
→ must have a valid result.

Official result
→ must belong to the correct official session.

Practice result
→ must remain practice.

Friend
→ must not become duplicate due to repeated action.
130. Result Integrity Rule

Results must correspond to the actual completed Battle.

Do not:

fabricate scores,
overwrite results with placeholder values,
display stale results as current,
calculate a second competing result in UI.
131. Persistence Integrity Rule

Persistence must not create:

duplicate Battles,
duplicate friends,
duplicate results,
impossible states.
132. Recovery Rule

When an operation has an unknown outcome:

Do not guess success.
Do not guess failure.

Use the documented reconciliation mechanism.

133. No Infinite Retry Rule

Retries must be controlled.

Do not create:

request fails
→ retry
→ retry
→ retry
→ retry forever

Use the documented retry strategy.

134. No Infinite Loading Rule

Every loading operation must have an eventual:

success
error
timeout/recovery

path where applicable.

135. No Hidden Network Dependency

A screen should not unexpectedly require network connectivity when the specification permits offline use.

136. Developer Experience Rule

The implementation should remain understandable to future developers.

Avoid unnecessary:

abstractions,
magic values,
duplicated logic,
giant files,
hidden side effects.
137. Code Readability Rule

Prefer code that clearly communicates:

What
Why
State
Boundary

Do not optimize for minimum line count.

138. Comments Rule

Comments should explain why, not merely repeat what the code does.

Important architecture/product constraints may be documented in comments where useful.

139. No Dead Architecture

Do not create unused:

repositories,
interfaces,
SDK wrappers,
feature flags,
screens,
database tables,

merely for hypothetical future features.

140. Final Implementation Contract

Antigravity's job is:

READ
UNDERSTAND
IMPLEMENT
TEST
REPORT
STOP

Not:

READ
GUESS
EXPAND
REDESIGN
IMPLEMENT EVERYTHING
141. Definition of Done — Antigravity Task

A task is complete only when:

[ ] Requirements identified
[ ] Scope defined
[ ] Implementation completed
[ ] Relevant tests passed
[ ] Build passed
[ ] UI verified
[ ] States verified
[ ] Navigation verified
[ ] Accessibility verified
[ ] Lifecycle verified where relevant
[ ] Offline/error behavior verified where relevant
[ ] No unauthorized features
[ ] No unexplained deviations
[ ] Dependencies reported
[ ] Evidence collected
[ ] Implementation report generated
[ ] Git checkpoint created
[ ] User/product acceptance pending or received
142. Final Non-Negotiable Rules

Antigravity must never:

Invent product requirements.
Invent scoring formulas.
Invent monetization.
Invent APIs.
Invent authentication.
Invent backend architecture.
Invent navigation.
Invent screens.
Invent permissions.
Invent analytics collection.
Add dependencies without reporting them.
Hide deviations.
Claim tests passed when they were not run.
Treat PENDING as permission to guess.
Treat technical convenience as product authority.
Let Practice modify Official Battle.
Allow monetization to create pay-to-win behavior.
Allow analytics to block gameplay.
Ignore accessibility.
Continue automatically into the next task.
143. Final Operating Principle

The implementation agent should behave according to this rule:

If it is specified, implement it.
If it is tested, report the result.
If it is ambiguous, stop and ask.
If it is not approved, do not build it.
If it deviates, document it.
If it fails, fix it before moving forward.