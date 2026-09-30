# Daily Battle — Analytics Specification

**Document:** `14_ANALYTICS.md`  
**Status:** LOCKED as analytics governance; exact event implementation remains partially PENDING  
**Product:** Daily Battle  
**Platform:** Android  
**Product authority:** User / Product Owner  
**Implementation agent:** Antigravity  

**Related documents:**
- `00_MASTER_SPEC.md`
- `01_PRODUCT.md`
- `02_GAMEPLAY.md`
- `03_SCREEN_ARCHITECTURE.md`
- `04_SCREEN_BLUEPRINTS.md`
- `05_DESIGN_SYSTEM.md`
- `06_NAVIGATION_AND_FLOWS.md`
- `07_TECHNICAL_ARCHITECTURE.md`
- `08_DATA_AND_API.md`
- `09_OFFLINE_AND_ERROR_HANDLING.md`
- `10_ANIMATION_HAPTICS.md`
- `11_ACCESSIBILITY.md`
- `12_TESTING_AND_QA.md`
- `13_MONETIZATION.md`

---

# 1. Purpose

This document defines the analytics contract for Daily Battle.

Analytics should answer:

1. Are users reaching the core Daily Battle experience?
2. Are users completing the official Battle?
3. Where do users drop out?
4. Are the three challenges functioning as intended?
5. Do users return for the next Daily Battle?
6. Is rivalry creating meaningful engagement?
7. Are Friends and Battle Code functionality being used?
8. Is Practice being used without contaminating Official Battle metrics?
9. Are errors and offline conditions affecting completion?
10. If monetization is introduced, does it provide value without damaging the core loop?

Analytics must measure the product.

Analytics must **not define the product**.

---

# 2. Analytics Philosophy

The primary analytics objective is understanding the user's interaction with:

```text
Open
 ↓
Today's Battle
 ↓
Play
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
Return tomorrow

The analytics system should therefore prioritize:

Battle participation,
Battle completion,
challenge completion,
score outcomes,
retention,
rivalry,
friend competition,
practice separation,
reliability.

Avoid building an analytics system around vanity metrics.

3. Analytics Authority

Analytics requirements must not override:

product requirements,
gameplay rules,
scoring rules,
navigation,
accessibility,
privacy,
monetization boundaries.

If analytics requirements conflict with product behavior, the product behavior remains authoritative unless the product specification is intentionally changed.

4. Analytics Must Not Change Gameplay

Analytics must be observational.

Analytics must not:

change challenge difficulty,
alter scoring,
determine official outcomes,
modify rival selection,
modify percentile,
unlock gameplay,
delay gameplay,
block a Battle,
require a network request before gameplay can continue.

A failed analytics event must not cause the game to fail.

5. Core Analytics Questions

The initial analytics system should answer these questions.

Acquisition / Entry
Did the user open the application?
Did they reach Home?
Did they complete onboarding?
Battle
Did the user see Today's Battle?
Did they start it?
Did they complete it?
Which challenge caused abandonment?
How long did the Battle take?
Performance
What score did the user achieve?
How did the score change over time?
Which challenge was completed?
Which challenge was abandoned?
Social
Did the user view their rival?
Did they add friends?
Did they compare scores?
Retention
Did the user return for the next Daily Battle?
Did they maintain momentum/streak behavior?
Reliability
Did network failure occur?
Did an official submission require reconciliation?
Did the application recover from interruption?
6. Analytics Event Naming

Event names should use a consistent convention.

Recommended format:

<domain>_<action>

Examples:

app_opened
onboarding_completed
battle_viewed
battle_started
challenge_started
challenge_completed
battle_completed
results_viewed
rival_viewed
friend_added

Exact production event names are PENDING until the final analytics schema is approved.

Antigravity must not introduce multiple naming conventions.

7. Event Naming Rules

Event names must:

be lowercase,
use one consistent separator,
describe an observable event,
avoid ambiguous names,
avoid UI implementation details,
avoid user-facing copy.

Prefer:

battle_started

over:

play_button_clicked

because the first represents the product event rather than a particular UI implementation.

8. Event Categories

Analytics events should be grouped conceptually into:

Application
Onboarding
Navigation
Battle
Challenge
Results
Rival
Friends
Profile
History
Practice
Errors
Lifecycle
Performance
Monetization

Not every category needs implementation in the first release.

9. Application Events

Potential core events:

app_opened
app_backgrounded
app_foregrounded

These are candidates and remain subject to final implementation.

10. Onboarding Events

Potential events:

welcome_viewed
battle_name_started
battle_name_completed
onboarding_completed

The analytics system should distinguish:

Onboarding started
Onboarding completed

This allows onboarding abandonment to be identified.

11. Home Events

Potential events:

home_viewed
today_battle_viewed
rival_card_viewed

Do not generate excessive analytics events for every visual interaction.

Analytics should focus on meaningful product actions.

12. Battle Events

The central Battle events are conceptually:

battle_viewed
battle_started
battle_completed

Where relevant, metadata should distinguish:

official
practice

This distinction is mandatory for analysis.

13. Official vs Practice Analytics

Official and Practice data must remain analytically separated.

Example conceptual property:

battle_mode:
  official
  practice

Do not allow Practice events to be counted as Official Battle completion.

Example:

official_battle_completed

must represent an actual Official Battle completion.

A generic:

battle_completed

must not be ambiguous between modes.

14. Battle Session Identifier

A Battle session should have a unique analytics correlation identifier where appropriate.

Conceptually:

battle_session_id

This allows events such as:

battle_started
challenge_started
challenge_completed
battle_completed

to be associated with the same session.

The exact ID-generation mechanism is governed by the technical architecture.

15. Challenge Events

Each challenge should produce meaningful lifecycle events.

Conceptually:

challenge_started
challenge_completed

with properties identifying:

challenge_type
battle_mode
battle_session_id
challenge_index

Possible challenge types:

snap
shift
crowd_call
16. Challenge Completion

A challenge completion event should occur exactly once per completed challenge.

Repeated UI events must not produce duplicate logical completion events.

For example:

user taps rapidly

must not produce:

challenge_completed × 5

when only one challenge actually completed.

17. Challenge Abandonment

The product should be able to understand where users leave the Battle.

A potential event:

challenge_abandoned

may include:

challenge_type
challenge_index
battle_mode
reason

However, "reason" should only be recorded when the reason is actually known.

Do not infer why a user abandoned a Battle.

18. Battle Abandonment

A Battle may be interrupted because of:

user leaving,
app backgrounding,
process death,
network failure,
crash,
unknown state.

These should not automatically be treated as the same event.

Where possible, analytics should distinguish known causes from unknown causes.

19. No Speculative User Intent

Analytics must not infer:

User disliked challenge
User found challenge too difficult
User was bored
User intended to quit
User was frustrated

unless the product explicitly collects information supporting such conclusions.

Analytics should record observable behavior.

20. Results Events

Potential events:

results_viewed
score_revealed
rival_comparison_viewed
share_started

Exact event list remains PENDING.

21. Score Analytics

Scores may be recorded as event properties where appropriate.

Potential properties:

total_score
snap_score
shift_score
crowd_call_score
consistency_score

However, analytics must use the authoritative scoring model.

Do not create analytics-only scoring formulas.

22. Score Privacy and Data Minimization

Only collect scores necessary for approved product analytics.

Do not collect unnecessary raw gameplay information.

If detailed interaction telemetry is proposed, it requires separate product/privacy review.

23. Percentile Analytics

Percentile may be recorded where necessary.

Potential property:

percentile

However, percentile must come from the authoritative product/data system.

Analytics must not independently calculate a competing percentile.

24. Rival Analytics

Potential events:

rival_viewed
rival_comparison_viewed
rival_battle_started

Potential properties:

score_gap

Only properties required for approved analysis should be collected.

Do not expose private rival information through analytics.

25. Friends Analytics

Potential events:

friends_viewed
add_friend_started
friend_added
friend_request_failed

Potential properties:

friend_action_result

Do not collect unnecessary personal information.

26. Battle Code Analytics

The Battle Code itself should not normally be recorded as an analytics event property.

Analytics should record the action:

add_friend_started
friend_added

rather than storing the actual code.

This follows data minimization.

27. Profile Analytics

Potential events:

profile_viewed
battle_dna_viewed

Analytics should not require collecting unnecessary profile content.

28. History Analytics

Potential events:

history_viewed

The fact that a user viewed History may be useful.

However, recording every graph interaction is not automatically necessary.

Only meaningful interactions should be instrumented.

29. Practice Analytics

Practice analytics should remain separate.

Potential events:

practice_viewed
practice_started
practice_challenge_started
practice_challenge_completed
practice_completed

Practice events should include:

battle_mode = practice

or an equivalent explicit mode identifier.

30. Retention Analytics

Retention is a major product metric.

Relevant concepts include:

Day 1 return
Day 7 return
Day 14 return
Day 30 return

Exact retention definitions are PENDING.

The important principle is:

Returning to the Daily Battle is more meaningful than simply opening a screen.

31. Battle Completion Rate

A core metric should conceptually measure:

Official Battle completions
──────────────────────────
Official Battle starts

Exact metric definition should be finalized before dashboard implementation.

32. Challenge Drop-Off

The product should be able to identify completion/drop-off by challenge.

Conceptually:

Snap started
Snap completed

Shift started
Shift completed

Crowd Call started
Crowd Call completed

This allows the product team to identify where users stop.

Do not automatically interpret a lower completion rate as a design failure without further investigation.

33. Battle Duration

Battle duration may be measured from:

battle_started

to:

battle_completed

Potential metric:

battle_duration_ms

or an equivalent server/client-derived duration.

Exact duration source is PENDING.

34. Challenge Duration

Challenge duration may be recorded if needed.

Potential property:

challenge_duration_ms

However, timing information should only be collected when it serves a documented product or gameplay purpose.

35. Timing Integrity

Analytics timestamps must not be used as the authoritative source for gameplay scoring.

Gameplay timing belongs to gameplay/domain logic.

Analytics timing is for observation and analysis.

36. Momentum / Streak Analytics

If momentum/streak exists in the product, analytics may measure:

current momentum,
momentum changes,
Battle return behavior.

However, analytics must not independently calculate or redefine momentum.

The authoritative value comes from the product/data layer.

37. Personal Best Analytics

Potential events:

personal_best_achieved

Potential properties:

score
previous_best
improvement

Only approved fields should be transmitted.

38. Error Analytics

Errors are important analytics signals.

Potential events:

battle_start_failed
challenge_load_failed
battle_submission_failed
friend_action_failed
data_load_failed

The exact event set should correspond to documented error states.

39. Error Properties

Where appropriate, errors may include:

error_category
error_code
screen
operation
retryable

Do not send:

passwords,
authentication tokens,
private messages,
raw personal information,
unnecessary API responses.
40. Network Analytics

Potential observable events:

network_unavailable
request_timeout
request_failed
request_recovered

These should be used carefully.

A temporary network problem should not generate excessive duplicate events.

41. Offline Analytics

Offline behavior should not make analytics blocking.

If an event cannot be transmitted immediately:

Gameplay continues

The application should follow the approved analytics delivery strategy.

Exact offline analytics queue behavior is PENDING.

42. Analytics Delivery

The application must not make core gameplay dependent on analytics delivery.

Bad architecture:

send analytics
 ↓
wait for server
 ↓
start challenge

Preferred conceptual behavior:

game event
 ↓
record analytics event
 ↓
continue gameplay
43. Analytics Failure

If analytics fails:

DO NOT
- crash
- block gameplay
- block navigation
- change score
- change Battle state

Analytics failure is non-critical to the core game loop.

44. Duplicate Analytics Events

Analytics should be idempotent where duplicate logical events are possible.

Particularly important for:

Battle completion,
Challenge completion,
Friend added,
Purchase completed,
Personal best.

A UI event may fire multiple times while the product event should occur once.

45. Event Properties

Event properties should be:

documented,
typed,
minimal,
stable,
useful.

Example conceptual schema:

Event:
battle_completed

Properties:
battle_mode
battle_session_id
total_score
duration_ms

Exact schema remains PENDING.

46. User Identity

Analytics may require a user identifier.

If used, the identifier should be:

stable,
non-sensitive,
documented,
appropriate for analytics.

Do not send unnecessary personally identifiable information.

Exact identity strategy is PENDING.

47. Anonymous Users

If Daily Battle supports anonymous use, analytics must distinguish anonymous sessions from authenticated accounts where necessary.

Exact authentication architecture remains governed by 07_TECHNICAL_ARCHITECTURE.md.

48. Device Information

Only useful device information should be collected.

Potential technical metadata:

app_version
os_version
device_class
screen_size

Exact fields are PENDING.

Do not collect unnecessary hardware identifiers.

49. App Version

Analytics events should be attributable to an app version.

Conceptual property:

app_version

This is important for identifying regressions after releases.

50. Build Type

Where appropriate, analytics must distinguish:

development
testing
production

Development/test events must not contaminate production analytics.

51. Environment Isolation

Never allow test data to appear as production user analytics.

Recommended conceptual property:

environment:
  development
  staging
  production

Exact environment strategy remains PENDING.

52. Analytics Consent and Privacy

The final analytics implementation must comply with applicable privacy requirements and the approved product privacy policy.

Before production implementation, define:

what data is collected,
why it is collected,
retention,
deletion,
consent requirements where applicable,
third-party processors/SDKs,
user controls where required.

Exact legal/privacy requirements are PENDING.

53. Data Minimization

The analytics system should collect the minimum information necessary to answer approved product questions.

Do not collect information simply because the analytics SDK makes it technically possible.

54. Sensitive Data

Do not place sensitive information into analytics properties.

Examples of data that should not be casually included:

Passwords
Authentication tokens
Payment credentials
Private messages
Raw contact lists
Exact location
Unnecessary personal identifiers
55. Battle Code Privacy

Do not log the actual Battle Code unless a specific documented requirement requires it.

Prefer:

friend_added = true

over:

battle_code = "ABC123"
56. Friend Privacy

Analytics should measure friend-related actions without creating a shadow copy of the user's social graph.

Avoid collecting unnecessary:

friend names,
phone numbers,
contact information,
private identifiers.
57. Analytics Architecture

Analytics should be abstracted from UI and gameplay code.

Conceptual architecture:

UI / Domain Event
       ↓
Analytics Abstraction
       ↓
Analytics Repository
       ↓
Analytics Provider

The application should not scatter provider-specific analytics calls throughout every screen.

58. Conceptual Analytics Interface

A possible abstraction:

AnalyticsTracker

track(
    event,
    properties
)

This is a conceptual architectural proposal.

Exact implementation is PENDING.

59. Domain Events vs Analytics Events

Do not tightly couple product state transitions to a third-party analytics SDK.

Prefer:

Domain event
    ↓
Analytics mapper
    ↓
Analytics event

This keeps the product logic independent from the analytics provider.

60. Analytics Provider

No analytics provider is currently locked.

Possible provider/tooling is therefore:

PENDING

Antigravity must not install an analytics SDK merely because it is common.

Before selecting one, evaluate:

privacy,
cost,
Android support,
offline behavior,
event limits,
dashboard capabilities,
export capabilities,
data ownership,
regional requirements,
integration complexity.
61. Analytics SDK Rules

If an SDK is eventually selected, document:

SDK name
Version
Purpose
Data collected
Permissions
Network behavior
Privacy implications
Cost
Event limits
Initialization behavior
Offline behavior

No SDK may be added without approval.

62. Initialization

Analytics initialization must not block the application from becoming usable.

Avoid:

Analytics initialization
        ↓
wait
        ↓
Home

unless there is a specific approved technical reason.

Preferred conceptual flow:

App launch
 ↓
App usable
 ↓
Analytics initializes independently
63. Lifecycle Analytics

Lifecycle events may include:

app_opened
app_backgrounded
app_foregrounded

However, lifecycle instrumentation should not create excessive noise.

Exact lifecycle event requirements remain PENDING.

64. Navigation Analytics

Analytics may capture meaningful screen views.

Potential events:

home_viewed
battle_intro_viewed
results_viewed
rival_viewed
friends_viewed
profile_viewed
history_viewed

Do not automatically track every internal composable/view/component as a screen.

65. Screen View Definition

A screen-view event should represent meaningful user exposure to a product screen.

It should not fire because:

a component recomposed,
a view was recreated,
a configuration change occurred,
the same screen rendered internally again.
66. Battle Funnel

The primary Battle funnel should conceptually be:

Home Viewed
     ↓
Today’s Battle Viewed
     ↓
Battle Started
     ↓
Snap Started
     ↓
Snap Completed
     ↓
Shift Started
     ↓
Shift Completed
     ↓
Crowd Call Started
     ↓
Crowd Call Completed
     ↓
Battle Completed
     ↓
Results Viewed
     ↓
Rival Viewed

This is one of the most important analytics structures in the product.

67. Funnel Integrity

The funnel must distinguish:

Started
Completed
Abandoned
Failed

Do not interpret missing events as a specific reason.

For example:

No battle_completed

does not automatically mean:

User intentionally abandoned Battle

It could also indicate:

crash,
network failure,
process death,
instrumentation failure.
68. Retention Funnel

A conceptual retention sequence:

First successful Battle
        ↓
Next-day return
        ↓
Next Battle start
        ↓
Next Battle completion

This is more useful than measuring application opens alone.

69. Rival Engagement Funnel

Potential funnel:

Results
 ↓
Rival Viewed
 ↓
Rival Comparison
 ↓
Next Battle

This can help evaluate whether rivalry is connected to continued engagement.

It must not be interpreted as causal without appropriate analysis.

70. Friends Funnel

Potential funnel:

Friends Viewed
 ↓
Add Friend Started
 ↓
Friend Added
 ↓
Friend Score Viewed

Exact funnel definitions remain PENDING.

71. Practice Funnel

Potential funnel:

Practice Viewed
 ↓
Practice Started
 ↓
Challenge Completed
 ↓
Practice Completed

Practice must remain analytically separate from Official Battle.

72. Monetization Analytics

Monetization analytics are governed by 13_MONETIZATION.md.

If monetization is not implemented:

No monetization events

should be added merely for future use.

If monetization is later approved, possible events include:

paywall_viewed
purchase_started
purchase_completed
purchase_failed
purchase_restored
subscription_started
subscription_cancelled

These are examples only.

Exact event schema is PENDING.

73. Monetization Separation

Monetization analytics must never modify:

score,
Battle state,
challenge difficulty,
rival state,
percentile.

Analytics observes monetization.

It does not control gameplay.

74. Performance Analytics

Performance telemetry may eventually track:

app startup,
screen load,
Battle load,
challenge transition,
API latency,
crash rate.

Exact telemetry requirements are PENDING.

Do not add expensive telemetry that damages gameplay performance.

75. Crash Analytics

Crash reporting may be useful for production reliability.

If introduced, it must:

avoid sensitive data,
distinguish app version,
provide useful stack traces,
respect privacy requirements.

The crash-reporting provider is PENDING.

76. Analytics and Offline Mode

If offline:

Gameplay continues according to offline rules.

Analytics should follow its own delivery strategy.

Potential future architecture:

Event
 ↓
Local queue
 ↓
Connectivity available
 ↓
Upload

This is PROPOSED, not yet locked.

77. Event Queue Requirements

If an offline queue is implemented, it must define:

maximum queue size,
retention period,
retry policy,
ordering,
duplicate prevention,
corruption handling,
user deletion behavior.

All are currently PENDING.

78. Analytics Deletion

If user account deletion exists, the analytics architecture must define how user-associated analytics data is treated.

Exact retention/deletion policy is PENDING and must be aligned with the privacy specification.

79. Analytics Testing

Every analytics implementation must be testable without relying exclusively on a production dashboard.

Tests should verify:

Correct event
Correct trigger
Correct properties
Correct mode
Correct frequency
No duplicates
No sensitive data
Failure does not block gameplay
80. Analytics Unit Tests

Test:

event mapping,
property mapping,
mode mapping,
null handling,
event naming,
duplicate prevention logic.

Example:

Official Battle completed
→ battle_completed
→ battle_mode = official

Practice:

Practice completed
→ battle_completed
→ battle_mode = practice

or the final approved event structure.

81. Analytics Integration Tests

Verify:

UI action
 ↓
domain event
 ↓
analytics mapper
 ↓
analytics tracker

No unintended duplicate event should be generated.

82. Analytics Failure Test

Simulate analytics provider failure.

Expected:

Analytics failure
        ↓
No crash
No score change
No navigation failure
No Battle corruption
83. Analytics Privacy Test

QA must inspect analytics payloads for accidental inclusion of:

passwords,
tokens,
personal contact data,
unnecessary identifiers,
private content,
Battle Codes.
84. Analytics Environment Test

Verify:

Development
→ development analytics

Testing
→ test/staging analytics

Production
→ production analytics

No environment should contaminate another.

85. Analytics Debugging

During development, analytics events should be inspectable through an approved debugging mechanism.

Exact debugging tool is PENDING.

The debugging system must not expose sensitive production data unnecessarily.

86. Analytics Versioning

Analytics schemas may evolve.

If an event changes meaning or property structure:

Old event
→ documented change
→ schema version / migration strategy
→ updated dashboards
→ updated tests

Do not silently reuse an event name for a materially different meaning.

87. Event Documentation

Every production event should eventually have a registry entry:

Event:
Description:
Trigger:
Screen:
Domain:
Required properties:
Optional properties:
Mode:
Frequency:
Privacy classification:
Owner:
Version:
88. Analytics Registry

A future registry may look like:

Event	Trigger	Mode	Key Properties	Status
app_opened	App becomes active	All	app_version	PENDING
onboarding_completed	Onboarding completes	All	—	PENDING
battle_started	Battle begins	Official/Practice	session_id, mode	PENDING
challenge_started	Challenge begins	Official/Practice	type, index	PENDING
challenge_completed	Challenge completes	Official/Practice	type, index	PENDING
battle_completed	Battle completes	Official/Practice	score, mode	PENDING
results_viewed	Results shown	Official	session_id	PENDING
rival_viewed	Rival viewed	All	—	PENDING
friend_added	Friend accepted/added	All	result status	PENDING
practice_started	Practice starts	Practice	session_id	PENDING

This table is a planning registry, not a final implementation contract.

89. Analytics Naming Anti-Patterns

Avoid:

button_clicked
screen_clicked
thing_happened
user_action
event1
battle_event
misc_event

These names lose product meaning.

Prefer semantic product events.

90. Analytics Over-Instrumentation

Do not track every:

tap,
animation,
recomposition,
scroll,
icon press,
internal state change.

Track meaningful product behavior.

Over-instrumentation creates:

noisy data,
larger payloads,
higher costs,
harder analysis,
privacy risk.
91. Analytics and Product Decisions

Analytics should inform product decisions.

It must not automatically make them.

Example:

Metric:
Crowd Call completion lower than Snap

This is a factual observation.

It does not automatically mean:

Crowd Call should be removed.

The product team must investigate context before changing the product.

92. Analytics Interpretation Rules

Analytics reports should distinguish:

Observed data
↓
Analysis
↓
Hypothesis
↓
Product decision

Do not present hypotheses as facts.

Example:

Observed:
Users frequently leave after Shift.

Possible hypotheses:
- challenge difficulty,
- timing,
- UI issue,
- technical issue,
- user interruption.

Next step:
Investigate evidence.
93. No User-Level Manipulation

Analytics must not be used to secretly alter a user's gameplay experience based on inferred characteristics unless a separate, approved personalization system exists.

Examples of prohibited implicit behavior:

User struggling
→ secretly easier Official Battle

User likely to pay
→ harder challenge

User inactive
→ secretly change score

Official Battle integrity must remain intact.

94. AI and Analytics

AI may eventually use analytics-derived signals for approved personalization.

However:

AI must not silently alter Official Battle fairness,
analytics must remain privacy-compliant,
personalization must have explicit product requirements.

AI analytics usage is PENDING.

95. Analytics Cost Control

If an external provider charges based on event volume, the system should avoid unnecessary high-frequency events.

Particularly avoid sending an event for every:

frame,
timer tick,
animation frame,
game-loop iteration.

Gameplay timing should remain local.

96. Analytics Performance

Analytics must have negligible impact on:

launch,
navigation,
challenge responsiveness,
animation,
battery,
memory,
network usage.

Exact performance thresholds are PENDING.

97. Analytics Definition of Done

Analytics implementation is DONE only when:

Product
 Approved analytics questions are documented
 Event meanings are unambiguous
 Official and Practice are separated
Technical
 Analytics abstraction exists where required
 Provider integration is isolated
 Event mapping is deterministic
 Failures do not block gameplay
 Duplicate events are controlled
Privacy
 Data minimization applied
 No sensitive information in payloads
 Privacy requirements reviewed
 User deletion behavior defined
QA
 Event triggers tested
 Properties tested
 Duplicate behavior tested
 Offline behavior tested
 Provider failure tested
 Environment separation tested
 Production payload reviewed
Governance
 Event registry updated
 Requirement IDs assigned
 Pending decisions resolved where necessary
 Deviations recorded
98. Analytics Requirements
REQ-AN-001

Analytics must not alter core gameplay behavior.

REQ-AN-002

Official and Practice analytics must remain distinguishable.

REQ-AN-003

Analytics failures must not block gameplay.

REQ-AN-004

Analytics events must represent meaningful product behavior.

REQ-AN-005

Analytics must not collect unnecessary sensitive or personal data.

REQ-AN-006

Official Battle completion must be measurable.

REQ-AN-007

Challenge-level completion must be measurable.

REQ-AN-008

Battle abandonment/drop-off must be observable without unsupported assumptions about user intent.

REQ-AN-009

Analytics must support app-version/environment attribution.

REQ-AN-010

Analytics must be independently testable.

REQ-AN-011

Analytics provider-specific implementation must remain isolated from core gameplay logic.

REQ-AN-012

Analytics must not independently redefine authoritative gameplay values such as score, percentile, or momentum.

REQ-AN-013

Analytics must not expose sensitive friend or Battle Code information unnecessarily.

REQ-AN-014

Development/test analytics must not contaminate production analytics.

REQ-AN-015

Analytics instrumentation must not become a dependency for successful gameplay.

99. Analytics Pending Decisions
AN-PENDING-001

Which analytics provider, if any, will be used?

AN-PENDING-002

What is the final production event registry?

AN-PENDING-003

What user identifier strategy is used?

AN-PENDING-004

What anonymous-user analytics behavior is required?

AN-PENDING-005

What consent/privacy model applies?

AN-PENDING-006

What analytics data retention period applies?

AN-PENDING-007

What deletion behavior is required?

AN-PENDING-008

What offline analytics queue strategy is used?

AN-PENDING-009

What event deduplication mechanism is used?

AN-PENDING-010

What production dashboard/reporting system is used?

AN-PENDING-011

What retention definitions are used?

AN-PENDING-012

What Battle completion metric definition is used?

AN-PENDING-013

What challenge abandonment definition is used?

AN-PENDING-014

What performance telemetry is required?

AN-PENDING-015

What crash-reporting provider is used?

AN-PENDING-016

What monetization events are required after monetization is finalized?

AN-PENDING-017

What analytics SDK, if any, is permitted?

AN-PENDING-018

What AI/personalization analytics usage, if any, is approved?

100. Antigravity Analytics Rules

Antigravity must:

Treat analytics as an observational subsystem.
Never let analytics control gameplay.
Never invent production event names without documenting them.
Never invent analytics properties merely because they are convenient.
Never collect unnecessary personal information.
Never log authentication secrets.
Never log private Battle Codes without explicit approval.
Never mix Official and Practice analytics.
Never treat missing analytics events as proof of user intent.
Never block gameplay while waiting for analytics.
Never add an analytics SDK without approval.
Never add analytics solely because a provider makes it easy.
Test duplicate event behavior.
Test provider failure.
Test offline behavior.
Test development/production separation.
Report all analytics dependencies.
Preserve event traceability.
Stop when privacy or event semantics are undefined.
Ask for a decision rather than guessing.
101. Final Analytics Principle

Daily Battle analytics should answer:

What happened?

before attempting to answer:

Why did it happen?

And product decisions should remain a separate step:

OBSERVE
   ↓
MEASURE
   ↓
ANALYZE
   ↓
FORM HYPOTHESIS
   ↓
DECIDE
   ↓
IMPLEMENT
   ↓
MEASURE AGAIN

Analytics is the measurement layer.

It is not the product authority.