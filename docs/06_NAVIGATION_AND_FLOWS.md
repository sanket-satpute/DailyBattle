# DAILY BATTLE — NAVIGATION & FLOWS

Version: 1.0
Status: Product Navigation Contract
Platform: Android
Master Frame: 390 × 844 px

---

# 0. PURPOSE

This document defines how users move through Daily Battle.

It establishes:

- primary navigation
- bottom navigation
- onboarding flow
- Home flow
- official Battle flow
- challenge-to-challenge transitions
- Results flow
- Rival flow
- Friends flow
- Add Friend flow
- Profile flow
- History flow
- Settings flow
- Practice flow
- Incoming Challenge flow
- Back behavior
- completed-state navigation
- loading/error navigation
- navigation guards
- lifecycle behavior
- deep-link expectations
- navigation rules
- prohibited navigation patterns

This document defines navigation behavior.

It does NOT redefine:

- gameplay rules
- scoring formulas
- visual design tokens
- backend implementation
- authentication architecture
- API contracts

Those belong to their respective documents.

---

# 1. NAVIGATION PRINCIPLES

Daily Battle navigation follows five principles.

## 1.1 One dominant destination

Every navigation action should have an obvious destination.

---

## 1.2 The Battle is a focused flow

Once the user enters an official Battle:

```text
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

The user should not feel like they are navigating a normal app during gameplay.

1.3 Gameplay hides app navigation

During:

Snap
Shift
Crowd Call

hide:

bottom navigation
profile controls
friend controls
history controls
social navigation

Gameplay owns the screen.

1.4 Navigation must preserve state

Returning to a previous screen must not accidentally:

reset completed gameplay
create a second official attempt
duplicate a friend
discard valid user input
create duplicate navigation destinations
1.5 Navigation must never invent product behavior

If the desired behavior is not defined in this document:

STOP
DO NOT INVENT

Report the ambiguity before implementation.

2. PRIMARY NAVIGATION MODEL

Daily Battle has four primary navigation destinations:

HOME
BATTLE
FRIENDS
ME

Bottom navigation contains exactly:

┌────────┬────────┬────────┬────────┐
│  Home  │ Battle │Friends │   Me   │
└────────┴────────┴────────┴────────┘
3. PRIMARY NAVIGATION DESTINATIONS
Home

Purpose:

Start today's Battle.

Home is the default post-onboarding destination.

Battle

Purpose:

Access the Battle destination.

The exact long-term structure of the Battle tab is not fully defined in the current product specification.

Therefore:

STATUS: PENDING

Do not invent additional Battle-tab features.

Friends

Purpose:

Manage the user's competition network.

Includes:

friends
today's scores
current rivalry
Add Friend
Me

Purpose:

Access the user's personal area.

The current product specification defines:

Profile / Battle DNA
History
Settings

The exact internal Me-tab navigation structure is:

STATUS: PENDING

Do not invent additional sections.

4. PRIMARY NAVIGATION MAP
                    ┌─────────────┐
                    │    HOME     │
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
      BATTLE            FRIENDS             ME
          │                │                │
          │                │         ┌──────┼──────┐
          │                │         ▼      ▼      ▼
          │                │      PROFILE HISTORY SETTINGS
          │                │
          │                ▼
          │           ADD FRIEND
          │
          ▼
      BATTLE FLOW

This represents the known product architecture.

5. FIRST-TIME USER FLOW

Primary onboarding:

Welcome
   ↓
Battle Name
   ↓
Home

Stable screen IDs:

SCR-001 Welcome
SCR-002 Battle Name
SCR-003 Home
6. WELCOME FLOW
Entry

First app launch.

APP
 ↓
WELCOME
Primary action
WELCOME
 ↓
[ GET STARTED ]
 ↓
BATTLE NAME

Exact button copy should follow the approved screen blueprint.

Back behavior

Welcome is the root of the first-time onboarding flow.

There is no previous Daily Battle screen.

Do not navigate backwards into an undefined screen.

7. BATTLE NAME FLOW
WELCOME
 ↓
BATTLE NAME
 ↓
[ CONTINUE ]
 ↓
HOME

The Battle Name becomes the user's in-product identity.

Validation

If the name is invalid:

BATTLE NAME
 ↓
validation feedback
 ↓
remain on BATTLE NAME

Do not navigate forward until the required input is valid.

Back
BATTLE NAME
 ↓
BACK
 ↓
WELCOME

Entered text should not be discarded unnecessarily.

8. HOME AS NAVIGATION HUB

Home is the primary navigation hub after onboarding.

Primary purpose:

Get the user into Today's Battle.

Home contains:

Greeting
Momentum

TODAY'S BATTLE
[ PLAY BATTLE ]

YOUR RIVAL
[ BEAT RIVAL ]
9. HOME → BATTLE FLOW

Primary flow:

HOME
 ↓
[ PLAY BATTLE ]
 ↓
BATTLE INTRO

Screen IDs:

SCR-003 Home
SCR-004 Battle Intro
10. BATTLE INTRO

Purpose:

Confirm that the user is about to begin the official Battle.

Structure:

TODAY'S BATTLE

3 challenges
~3 minutes

● ○ ○

OFFICIAL ATTEMPT

[ START BATTLE ]
Primary transition
BATTLE INTRO
 ↓
START BATTLE
 ↓
SNAP
11. OFFICIAL BATTLE FLOW

The official Battle is a linear flow.

BATTLE INTRO
      ↓
     SNAP
      ↓
  SNAP COMPLETE
      ↓
     SHIFT
      ↓
 SHIFT COMPLETE
      ↓
  CROWD CALL
      ↓
 CROWD RESULT
      ↓
    RESULTS

Stable screen IDs:

SCR-004 Battle Intro
SCR-005 Snap
SCR-006 Shift
SCR-007 Crowd Call
SCR-008 Results

Secondary states:

Snap Complete
Shift Complete
Crowd Result
12. OFFICIAL BATTLE RULE

The official Battle is:

ONE DAILY BATTLE
+
ONE OFFICIAL ATTEMPT

After completion:

OFFICIAL BATTLE
=
COMPLETED

The user must not be offered another official submission for the same Battle.

13. OFFICIAL BATTLE NAVIGATION LOCK

Once the user starts the official Battle, bottom navigation is hidden.

The user cannot use:

Home
Battle
Friends
Me

to leave gameplay through the standard bottom navigation.

Gameplay is treated as a focused flow.

14. SNAP FLOW
BATTLE INTRO
 ↓
SNAP
 ↓
SNAP COMPLETE
 ↓
SHIFT

Snap is the first challenge.

Purpose:

React.

Snap completion

The completion state briefly displays the challenge score.

Example:

SNAP COMPLETE

287 / 300

Then automatically transitions to Shift.

No unnecessary Continue action.

15. SHIFT FLOW
SNAP COMPLETE
 ↓
SHIFT
 ↓
SHIFT COMPLETE
 ↓
CROWD CALL

Shift is the second challenge.

Purpose:

Remember / identify what changed.

Shift completion

Display a short completion state.

Then automatically transition to Crowd Call.

16. CROWD CALL FLOW
SHIFT COMPLETE
 ↓
CROWD CALL
 ↓
CROWD RESULT
 ↓
RESULTS

Crowd Call is the third challenge.

Purpose:

Predict.

Crowd result

Crowd Call may show:

prediction result
crowd distribution
challenge result

Then transition to Results.

17. GAMEPLAY TRANSITION RULE

Challenge transitions should feel continuous.

The intended sequence is:

PLAY
 ↓
FEEDBACK
 ↓
NEXT CHALLENGE

Not:

PLAY
 ↓
MENU
 ↓
CONTINUE
 ↓
NEXT CHALLENGE

Avoid unnecessary interruption.

18. GAMEPLAY BACK BEHAVIOR

The exact Back behavior while inside:

Snap
Shift
Crowd Call

is not fully specified by the current product specification.

Therefore:

STATUS: PENDING PRODUCT DECISION

Antigravity must NOT invent behavior such as:

automatically submitting
restarting
abandoning
returning to Home
showing a confirmation dialog

until this is explicitly defined.

19. ANDROID SYSTEM BACK

Android system Back and in-app Back must follow the same navigation contract.

When behavior is undefined:

DO NOT GUESS.

Implementation should report:

NAV-PENDING-001

or the project's equivalent pending-navigation record.

20. RESULTS FLOW

After Crowd Call:

CROWD RESULT
 ↓
RESULTS

Results is the emotional end of the daily Battle.

Purpose:

Understand performance.

21. RESULTS CONTENT ORDER

Primary order:

FINAL SCORE
 ↓
PERCENTILE
 ↓
IMPROVEMENT
 ↓
BREAKDOWN
 ↓
RIVAL

Example:

901 / 1000

TOP 9%

+37 FROM YESTERDAY

SNAP       287
SHIFT      252
CROWD      271

RIVAL
Rahul      914
You        901

13 points to catch

The exact values are examples, not hardcoded data.

22. RESULTS → RIVAL

Primary next action:

RESULTS
 ↓
RIVAL

The Rival screen provides the personal competition context.

23. RESULTS → HOME

The exact post-results navigation behavior is not fully defined.

Possible destinations may include:

Home
Rival

The current product specification establishes Rival as part of the primary flow:

Results
 ↓
Rival

Therefore the primary documented flow is:

RESULTS
 ↓
RIVAL

After Rival, navigation may return to Home.

Exact implementation behavior:

STATUS: PENDING FINAL NAVIGATION DECISION
24. COMPLETED HOME FLOW

After completing Today's Battle:

HOME

changes state.

Instead of:

PLAY BATTLE

show:

TODAY'S BATTLE ✓

901
TOP 9%

NEXT BATTLE
TOMORROW

The official Battle CTA disappears.

25. COMPLETED HOME RULE

Completed Home must never imply:

PLAY AGAIN
SUBMIT AGAIN
NEW OFFICIAL ATTEMPT

for the same daily Battle.

Practice may exist separately.

26. HOME → RIVAL

Home may navigate directly to Rival through:

YOUR RIVAL
[ BEAT RAHUL ]

Flow:

HOME
 ↓
RIVAL
27. RIVAL SCREEN

Purpose:

Beat someone.

Concept:

ONE PERSON
ONE GAP
ONE GOAL

Structure:

YOUR RIVAL

Rahul
914

VS

You
901

13 POINTS TO CATCH

[ BEAT RAHUL ]
28. RIVAL → PRACTICE

Practice is a secondary flow.

Known architecture:

RIVAL
 ↓
PRACTICE

Practice is NOT the official Battle.

29. PRACTICE RULE

Practice:

DOES NOT CHANGE
OFFICIAL SCORE

Practice should remain visually distinct from:

OFFICIAL BATTLE
30. PRACTICE FLOW

Known high-level flow:

RIVAL
 ↓
PRACTICE LAUNCH
 ↓
PRACTICE GAME
 ↓
PRACTICE RESULT
 ↓
RETURN

The exact Practice screen architecture and challenge selection flow are:

STATUS: PENDING

Do not invent additional screens.

31. FRIENDS FLOW

Primary:

HOME
 ↓
FRIENDS TAB
 ↓
FRIENDS

Friends is a competition network.

It is NOT a social feed.

32. FRIENDS SCREEN

Purpose:

Manage rivals.

Structure:

FRIENDS

[ + ADD FRIEND ]

TODAY

Rahul       914
Sanket      901
Neha        817
Aman        761
33. FRIENDS → ADD FRIEND
FRIENDS
 ↓
[ ADD FRIEND ]
 ↓
ADD FRIEND
34. ADD FRIEND FLOW

Add Friend uses Battle Code.

ADD A FRIEND

Enter Battle Code

[ B7K9P2 ]

[ ADD FRIEND ]

Your Battle Code

K4X8M9

[ COPY ]
Explicitly excluded

Do not navigate to:

Contacts
Phone-number search
Location search
Social media search

unless a future product decision explicitly adds them.

35. ADD FRIEND RESULT STATES

Possible states:

Default
Loading
Success
Invalid Code
Already Added
Error

Exact backend validation behavior belongs to the Data/API specification.

Navigation rule:

If adding fails:

remain on ADD FRIEND

If successful:

ADD FRIEND
 ↓
FRIENDS

unless a future product decision defines another destination.

36. INCOMING CHALLENGE

Incoming Challenge exists as a secondary flow.

Known architecture:

FRIENDS
 ↓
INCOMING CHALLENGE

However, exact:

entry point
screen structure
accept behavior
reject behavior
expiration
notification behavior

are not fully defined.

Therefore:

STATUS: PENDING

Do not invent them.

37. FRIEND STATES

Friend state model:

Pending
Accepted
Blocked

Navigation should respect the state.

For example:

Pending

must not be presented as an active competition relationship until accepted.

38. FRIEND PRIVACY

Friend-facing navigation must not expose:

location
phone number
email
private information

unless explicitly required.

Competition-relevant information only.

39. PROFILE FLOW

Profile belongs to the personal area.

Known purpose:

Understand yourself.

Profile is also called:

Battle DNA
40. PROFILE CONTENT

Known information:

Player Name

Momentum

Best Score

Average

Battle DNA

Speed
Memory
People

Personal Records

No IQ or scientific intelligence claims.

41. PROFILE → HISTORY

The exact internal navigation structure of the Me tab is not fully locked.

However, History is a defined product destination.

Potential documented relationship:

PROFILE
 ↓
HISTORY

This relationship should be treated as:

KNOWN PRODUCT RELATIONSHIP

while the exact Me-tab navigation presentation remains pending.

42. HISTORY FLOW

Purpose:

See progress.

History contains:

Score graph
Best
Average
Momentum
Recent battles
43. HISTORY NAVIGATION

History should provide a clear route back to its parent personal area.

Do not create a new bottom navigation destination for History.

History is not one of:

Home
Battle
Friends
Me
44. SETTINGS FLOW

Settings is a secondary destination.

Known content:

GAME
Sound
Haptics
Notifications

ACCOUNT
Battle Name
Battle Code

PRIVACY
Privacy Policy
Delete Account

ABOUT
Terms
About Daily Battle
45. SETTINGS ENTRY POINT

The exact entry point into Settings is not fully specified.

Possible location:

Me
 ↓
Settings

but this should be treated as:

STATUS: PENDING CONFIRMATION

Do not create a random Settings icon on multiple screens.

46. SETTINGS NAVIGATION RULE

Settings should not become part of the primary bottom navigation.

Primary navigation remains:

Home
Battle
Friends
Me
47. SETTINGS BACK

Expected hierarchy:

SETTINGS
 ↓
BACK
 ↓
PARENT PERSONAL AREA

Exact parent destination is dependent on the final Me-tab architecture.

48. BOTTOM NAVIGATION RULES

Bottom navigation is available on normal app-level screens.

It is hidden during:

Battle Intro
Snap
Shift
Crowd Call

and associated focused gameplay transitions.

49. BOTTOM NAV ACTIVE STATE

The selected destination must be visually identifiable.

Example:

HOME
Battle
Friends
Me

when Home is active.

Battle should be visually emphasized when it is the active destination.

50. BOTTOM NAVIGATION DURING RESULTS

Results is not gameplay.

Therefore the exact bottom-navigation visibility during Results must follow the screen blueprint/navigation implementation.

Current source defines Results as a separate result screen but does not explicitly lock its bottom-navigation state.

Therefore:

STATUS: PENDING

Do not infer from gameplay alone.

51. NAVIGATION STACK MODEL

At the conceptual level:

Onboarding Stack
       ↓
Main App Stack
       ↓
Focused Battle Flow
Onboarding
Welcome
Battle Name
Main app
Home
Friends
Me
...
Focused Battle
Battle Intro
Snap
Shift
Crowd Call
Results

The implementation architecture may use the Android navigation framework appropriate to the project.

The product behavior must remain consistent with this model.

52. BATTLE FLOW AS A TRANSACTION

The official Battle should be treated as one logical user journey:

Battle Session
│
├── Snap
├── Shift
├── Crowd Call
└── Results

Navigation must preserve the session state across these transitions.

53. NO DUPLICATE OFFICIAL ATTEMPT

Navigation must prevent:

Double tap
Back
Forward
Re-entry
Lifecycle recreation

from creating a second official attempt.

54. RAPID TAP PROTECTION

Primary navigation actions should prevent accidental duplicate navigation.

Example:

PLAY BATTLE

rapidly tapped multiple times must not create:

Battle Intro
Battle Intro
Battle Intro

Only one destination should be created.

55. SCREEN RECREATION

Android configuration changes or process recreation must not silently:

create a new Battle
reset the official attempt
duplicate a result
lose a completed challenge

State persistence requirements belong to the technical architecture/data documents.

Navigation must consume that persisted state correctly.

56. APP BACKGROUNDING DURING BATTLE

If the app leaves foreground during:

Snap
Shift
Crowd Call

the navigation layer must restore the appropriate Battle state rather than assuming the user wants a new Battle.

Exact timer/pause behavior is a gameplay/technical decision.

If not defined:

STATUS: PENDING
57. APP RE-ENTRY

If the user opens Daily Battle again:

If today's Battle is not started:
HOME
If today's Battle is in progress:
RESUME APPROPRIATE BATTLE STATE
If today's Battle is completed:
HOME COMPLETED STATE

The exact persistence mechanism is defined elsewhere.

58. NAVIGATION GUARD — COMPLETED BATTLE

If the user has already completed today's official Battle:

HOME

must show:

TODAY'S BATTLE ✓

and not:

PLAY BATTLE

for another official attempt.

59. ERROR NAVIGATION

If a screen cannot load its required data:

SCREEN
 ↓
ERROR STATE
 ↓
[ RETRY ]

Do not automatically navigate to:

LOGIN
WELCOME
HOME

unless authentication or product architecture explicitly requires it.

60. LOADING NAVIGATION

When loading:

CURRENT SCREEN
 ↓
LOADING STATE
 ↓
CONTENT

The navigation destination should not change simply because data is loading.

61. ERROR → RETRY

Retry should normally preserve the current destination.

Example:

HOME
 ↓
LOAD ERROR
 ↓
RETRY
 ↓
HOME CONTENT

not:

LOAD ERROR
 ↓
WELCOME
62. EMPTY STATE NAVIGATION

An empty state should provide the most relevant next action.

Example:

NO RIVALS YET

[ ADD FRIEND ]

Flow:

FRIENDS
 ↓
ADD FRIEND
63. DESTRUCTIVE NAVIGATION

Destructive actions such as:

Delete Account

must not execute through a simple accidental tap.

The exact confirmation flow belongs to the product/account specification.

At minimum:

User intent
 ↓
Confirmation
 ↓
Destructive action
64. SHARE FLOW

Results contains:

SHARE RESULT

The share action leaves Daily Battle through the Android sharing mechanism.

Conceptually:

RESULTS
 ↓
SHARE RESULT
 ↓
ANDROID SHARE SHEET
 ↓
External App

Returning should restore the Results screen.

Exact share implementation belongs to technical architecture.

65. NOTIFICATION ENTRY POINT

Notifications are supported as a Settings preference.

However, the exact notification navigation destinations are not currently fully specified.

Therefore:

STATUS: PENDING

Do not invent notification deep links.

66. DEEP LINKING

Deep-link behavior is currently:

STATUS: PENDING

Potential future destinations may include:

Battle
Rival
Friend invitation
Result

But these must not be implemented without explicit product requirements.

67. AUTHENTICATION NAVIGATION

Authentication/account architecture is currently not fully defined.

Therefore this document does not define:

Login
Signup
OTP
Google Sign-In
Apple Sign-In
Password Reset

Do not introduce an authentication flow based only on assumptions.

68. NAVIGATION LANGUAGE

Navigation labels should remain concise.

Use:

Home
Battle
Friends
Me

Do not rename these destinations without a product decision.

69. PRIMARY FLOW

The complete known primary journey:

WELCOME
   ↓
BATTLE NAME
   ↓
HOME
   ↓
BATTLE INTRO
   ↓
SNAP
   ↓
SHIFT
   ↓
CROWD CALL
   ↓
RESULTS
   ↓
RIVAL

This is the canonical MVP journey.

70. SECONDARY FLOW — RIVAL
HOME
   ↓
RIVAL
   ↓
PRACTICE

Practice remains separate from Official Battle.

71. SECONDARY FLOW — FRIENDS
FRIENDS
   ↓
ADD FRIEND
   ↓
FRIENDS

Additional incoming challenge flow:

FRIENDS
   ↓
INCOMING CHALLENGE

Exact incoming challenge behavior remains pending.

72. PERSONAL AREA FLOW

Known conceptual structure:

ME
│
├── PROFILE
│
├── HISTORY
│
└── SETTINGS

The exact implementation/navigation presentation is still pending.

73. FULL MVP NAVIGATION MAP
                              ┌──────────────┐
                              │   WELCOME    │
                              │   SCR-001    │
                              └──────┬───────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │ BATTLE NAME  │
                              │   SCR-002    │
                              └──────┬───────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │     HOME     │
                              │   SCR-003    │
                              └──────┬───────┘
                                     │
                    ┌────────────────┼─────────────────┐
                    │                │                 │
                    │                │                 │
                    ▼                ▼                 ▼
             BATTLE FLOW          RIVAL            FRIENDS
                    │                │                 │
                    │                ▼                 ▼
                    │             PRACTICE         ADD FRIEND
                    │
                    ▼
             ┌──────────────┐
             │ BATTLE INTRO │
             │   SCR-004    │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │     SNAP     │
             │   SCR-005    │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │     SHIFT    │
             │   SCR-006    │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │ CROWD CALL   │
             │   SCR-007    │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │   RESULTS    │
             │   SCR-008    │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │    RIVAL     │
             │   SCR-009    │
             └──────────────┘


BOTTOM NAVIGATION

┌─────────┬─────────┬─────────┬─────────┐
│  HOME   │ BATTLE  │ FRIENDS │   ME    │
└─────────┴─────────┴─────────┴─────────┘

FRIENDS
   │
   ├── ADD FRIEND
   │
   └── INCOMING CHALLENGE [PENDING]

ME
   │
   ├── PROFILE
   │
   ├── HISTORY
   │
   └── SETTINGS
74. SCREEN OWNERSHIP

Every screen has one navigation responsibility.

Screen	ID	Navigation responsibility
Welcome	SCR-001	Start onboarding
Battle Name	SCR-002	Complete identity setup
Home	SCR-003	Start today's Battle
Battle Intro	SCR-004	Confirm official Battle start
Snap	SCR-005	Execute challenge 1
Shift	SCR-006	Execute challenge 2
Crowd Call	SCR-007	Execute challenge 3
Results	SCR-008	Present completed Battle
Rival	SCR-009	Present personal competition
Friends	SCR-010	Manage competition network
Add Friend	SCR-011	Add competition connection
Profile	SCR-012	Understand player identity
History	SCR-013	See progress

Secondary:

Home Completed
Snap Complete
Shift Complete
Crowd Result
Practice
Incoming Challenge
Settings
75. NAVIGATION ANTI-PATTERNS

Do NOT implement:

Bottom navigation inside gameplay
WRONG
Snap + Home/Battle/Friends/Me
Nested bottom navigation
WRONG
Friends → another bottom navigation
Duplicate screens

Do not create multiple versions of:

Home
Friends
Profile

for minor states.

Use state-driven UI where appropriate.

Hidden destinations

Do not make important destinations reachable only through undocumented gestures.

Random back behavior

Every navigation path must have a defined destination.

Infinite navigation

Do not introduce:

Feed
Feed
Feed
Feed

Daily Battle is not an infinite-feed product.

76. NAVIGATION STATE MACHINE

Conceptual application state:

ONBOARDING
    │
    ▼
READY
    │
    ▼
BATTLE_INTRO
    │
    ▼
BATTLE_IN_PROGRESS
    │
    ├── SNAP
    ├── SHIFT
    └── CROWD
    │
    ▼
BATTLE_COMPLETED
    │
    ▼
RESULTS
    │
    ▼
RIVAL
    │
    ▼
HOME_COMPLETED

Separate app-level destinations:

FRIENDS
PROFILE
HISTORY
SETTINGS
77. NAVIGATION INVARIANTS

These rules must always remain true.

Invariant 1

One official Battle per day.

Invariant 2

Official Battle cannot be accidentally duplicated through navigation.

Invariant 3

Practice cannot modify official Battle score.

Invariant 4

Gameplay hides bottom navigation.

Invariant 5

Completed Battle cannot become an unfinished Battle through Back navigation.

Invariant 6

Rapid taps cannot create duplicate destinations.

Invariant 7

Errors do not unexpectedly reset onboarding.

Invariant 8

Navigation does not invent unsupported features.

78. NAVIGATION REQUIREMENTS
REQ-NAV-001

The app shall provide four primary navigation destinations:

Home
Battle
Friends
Me
REQ-NAV-002

The app shall use Home as the primary post-onboarding destination.

REQ-NAV-003

The official Battle shall follow:

Battle Intro
→ Snap
→ Shift
→ Crowd Call
→ Results
REQ-NAV-004

Gameplay screens shall hide bottom navigation.

REQ-NAV-005

Challenge completion shall transition to the next challenge without unnecessary manual navigation.

REQ-NAV-006

The app shall prevent duplicate official Battle attempts caused by repeated navigation.

REQ-NAV-007

Completed Home shall not present another official-play CTA for the same daily Battle.

REQ-NAV-008

Practice shall remain distinct from the official Battle.

REQ-NAV-009

Practice shall not modify the official score.

REQ-NAV-010

Friends shall provide access to Add Friend.

REQ-NAV-011

Add Friend shall use Battle Code as the defined MVP friend-add mechanism.

REQ-NAV-012

The navigation system shall preserve Battle state across normal Android lifecycle events.

REQ-NAV-013

Loading and error states shall preserve the user's current navigation context.

REQ-NAV-014

The app shall not introduce undocumented destinations or navigation patterns.

REQ-NAV-015

Undefined navigation behavior shall be treated as pending product decisions rather than implementation opportunities.

79. PENDING NAVIGATION DECISIONS

The following items remain intentionally unresolved.

NAV-PENDING-001
Exact Back behavior during Snap

NAV-PENDING-002
Exact Back behavior during Shift

NAV-PENDING-003
Exact Back behavior during Crowd Call

NAV-PENDING-004
Exact Battle tab destination structure

NAV-PENDING-005
Exact Me tab internal navigation

NAV-PENDING-006
Exact Settings entry point

NAV-PENDING-007
Incoming Challenge complete flow

NAV-PENDING-008
Practice screen architecture

NAV-PENDING-009
Results bottom navigation visibility

NAV-PENDING-010
Completed Results → Rival → Home behavior

NAV-PENDING-011
Authentication/account navigation

NAV-PENDING-012
Deep-link destinations

NAV-PENDING-013
Notification destination behavior

NAV-PENDING-014
Share flow return behavior

These are not bugs.

They are intentionally unresolved product decisions.

80. ANTIGRAVITY RULE

When implementing navigation:

Read this document.
Read 03_SCREEN_ARCHITECTURE.md.
Read the relevant screen blueprint.
Read the relevant gameplay specification.
Implement only documented behavior.
If behavior conflicts, STOP.
Report the conflict.
Do not silently choose a behavior.
81. NAVIGATION VERIFICATION

Every navigation implementation must be tested for:

Forward navigation
 Welcome → Battle Name
 Battle Name → Home
 Home → Battle Intro
 Battle Intro → Snap
 Snap → Shift
 Shift → Crowd Call
 Crowd Call → Results
 Results → Rival
Secondary navigation
 Home → Rival
 Rival → Practice
 Friends → Add Friend
 Add Friend → Friends
 Me → Profile
 Me → History
 Me → Settings

Only the documented relationships should be tested as mandatory.

82. BACK NAVIGATION QA

Test:

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

For every screen verify:

 Back destination is defined
 No duplicate screen is created
 User state is preserved
 Completed Battle is not reset
 No accidental second official attempt occurs

For undefined Battle Back behavior:

DO NOT ASSUME.

Mark the test as pending until the product decision is locked.

83. LIFECYCLE QA

Test:

Open app
Background app
Return
Rotate/configuration recreation if applicable
Process recreation
Return from external app

Verify:

 correct destination restored
 Battle state preserved
 no duplicate Battle attempt
 no duplicate navigation
 completed state preserved
84. RAPID-TAP QA

For every major CTA:

Tap once
Tap twice rapidly
Tap repeatedly

Verify:

ONE INTENDED NAVIGATION

not:

multiple destinations
multiple Battle sessions
multiple friend requests
85. NAVIGATION DEFINITION OF DONE

Navigation is complete when:

 Primary navigation works
 Onboarding flow works
 Official Battle flow works
 Challenge transitions work
 Results flow works
 Rival flow works
 Friends flow works
 Add Friend flow works
 Profile flow works
 History flow works
 Settings flow works
 Practice entry is correctly separated
 Gameplay hides bottom navigation
 Duplicate navigation is prevented
 Official Battle cannot be duplicated
 Completed Battle state is preserved
 Loading states preserve context
 Error states preserve context
 Android lifecycle behavior is tested
 Rapid taps are tested
 Undefined behaviors are documented rather than invented
 No unauthorized navigation destinations exist
86. FINAL NAVIGATION PRINCIPLE

Daily Battle should feel like:
OPEN
 ↓
TODAY
 ↓
PLAY
 ↓
FOCUS
 ↓
SCORE
 ↓
RIVAL
 ↓
DONE
 ↓
TOMORROW

Not:

OPEN
 ↓
DASHBOARD
 ↓
FEED
 ↓
PROFILE
 ↓
STORE
 ↓
LEADERBOARD
 ↓
CHAT
 ↓
GAME
 ↓
...

The navigation system exists to reinforce the daily ritual.

The product should always make the next meaningful action obvious.

END OF NAVIGATION & FLOWS