# DAILY BATTLE — SCREEN ARCHITECTURE

**Document:** 03_SCREEN_ARCHITECTURE.md
**Product:** Daily Battle
**Platform:** Android
**Document Status:** SCREEN ARCHITECTURE
**Specification Version:** 1.0
**Last Updated:** 2026-09-30

---

# 1. PURPOSE

This document defines the complete screen and state architecture of Daily
Battle.

It establishes:

- all primary screens
- all secondary states
- screen responsibilities
- screen categories
- entry points
- exit points
- primary navigation relationships
- gameplay screen relationships
- secondary flows
- screen ownership boundaries

This document does NOT define:

- pixel-level layouts
- exact spacing
- colors
- typography
- component styling
- animation implementation
- backend architecture
- detailed gameplay algorithms

Those responsibilities belong to other specification documents.

---

# 2. SCREEN ARCHITECTURE PRINCIPLE

Daily Battle follows:

> One screen = one dominant job.

The screen should have one obvious primary purpose.

The current product responsibilities are:

| Screen | Dominant Job |
|---|---|
| Welcome | Start onboarding |
| Battle Name | Establish player identity |
| Home | Start today's Battle |
| Battle Intro | Prepare for today's Battle |
| Snap | React |
| Shift | Remember / identify change |
| Crowd Call | Predict |
| Results | Understand performance |
| Rival | Beat someone |
| Friends | Manage competition |
| Add Friend | Add a friend |
| Profile | Understand yourself |
| History | See progress |
| Settings | Manage preferences/account |

The gameplay experience must remain focused and must not become a
dashboard during active challenges.

---

# 3. PRIMARY SCREEN COUNT

The current product specification defines **13 primary screens**.

```text
01  Welcome
02  Battle Name
03  Home
04  Battle Intro
05  Snap
06  Shift
07  Crowd Call
08  Results
09  Rival
10  Friends
11  Add Friend
12  Profile
13  History

Settings is currently defined as a secondary flow/state rather than one of
the 13 primary screens.

4. SCREEN CATEGORIES
4.1 Onboarding
Welcome
Battle Name

Purpose:

Establish the player's initial entry into Daily Battle.

4.2 Home
Home

Purpose:

Get the player into today's Official Battle.

Home should remain restrained.

It should not become a comprehensive analytics dashboard.

4.3 Battle
Battle Intro
Snap
Shift
Crowd Call

Purpose:

Deliver the approximately three-minute Official Battle.

Gameplay screens are immersive and focused.

4.4 Results
Results

Purpose:

Reveal and explain the player's completed Battle performance.

4.5 Competition
Rival
Friends
Add Friend

Purpose:

Support personal competition and friend-based rivalry.

This is not a social network.

4.6 Personal Performance
Profile
History

Purpose:

Help the player understand their own performance and progress.

4.7 Settings
Settings

Purpose:

Provide lightweight account, preference, privacy, and legal controls.

Settings is a secondary flow.

5. COMPLETE INFORMATION ARCHITECTURE
DAILY BATTLE
│
├── ONBOARDING
│   ├── Welcome
│   └── Battle Name
│
├── HOME
│   ├── Ready State
│   └── Completed State
│
├── BATTLE
│   ├── Battle Intro
│   ├── Snap
│   │   └── Snap Complete
│   ├── Shift
│   │   └── Shift Complete
│   ├── Crowd Call
│   │   └── Crowd Result
│   └── Battle Completion
│
├── RESULTS
│   └── Today's Result
│
├── RIVAL
│   ├── Rival Overview
│   └── Practice Launch
│
├── FRIENDS
│   ├── Friends List
│   ├── Add Friend
│   └── Incoming Challenge
│
├── PROFILE
│   └── Battle DNA
│
├── HISTORY
│   └── Score History
│
└── SETTINGS

The secondary items above are states or flows and do not automatically
represent separate primary screens.

6. SCREEN REGISTRY

Each screen receives a stable identifier.

These IDs should be used by:

implementation tasks
navigation code
testing
QA
audits
developer handoff
future documentation
SCR-001 — Welcome

Category: Onboarding

Primary job:

Start the onboarding experience.

Entry:

First application entry when onboarding has not been completed.

Exit:

Battle Name.

Primary action:

Continue into onboarding.

Gameplay: No.

Bottom navigation: No.

SCR-002 — Battle Name

Category: Onboarding

Primary job:

Allow the player to establish their Battle name.

Entry:

Welcome.

Exit:

Home.

Gameplay: No.

Bottom navigation: No.

SCR-003 — Home

Category: Home

Primary job:

Get the player into today's Battle.

Entry points:

completed onboarding
returning application launch
navigation from secondary areas where appropriate

Primary content:

greeting
momentum
Today's Battle
Rival

Primary action when Battle is available:

Play Battle.

Completed state:

After today's Official Battle is completed, Home communicates the completed
result and the next Battle tomorrow.

Gameplay: No.

Bottom navigation: Yes.

7. HOME STATES

Home has two important product states.

HOME-READY

Today's Battle has not yet been completed.

Conceptually:

Greeting
Momentum

TODAY'S BATTLE
3 challenges
~3 minutes

[ PLAY BATTLE ]

YOUR RIVAL
...
HOME-COMPLETED

Today's Battle has already been completed.

Conceptually:

TODAY'S BATTLE ✓

[Score]
[Percentile]

NEXT BATTLE
TOMORROW

The Official Battle action must no longer appear as though another official
attempt is available.

8. SCR-004 — Battle Intro

Category: Battle

Primary job:

Prepare the player to start today's Official Battle.

Entry:

Home → Play Battle.

Exit:

Snap.

Primary information:

Today's Battle
3 challenges
approximately 3 minutes
Official Attempt

Primary action:

Start Battle.

Bottom navigation: No.

Gameplay: Not yet active.

The exact challenge types should not be revealed here.

9. SCR-005 — Snap

Category: Gameplay

Primary job:

React.

Entry:

Battle Intro → Start Battle.

Exit:

Shift.

Challenge position:

1 / 3

Gameplay: Yes.

Bottom navigation: No.

Primary gameplay focus:

Fast reaction.

10. SNAP COMPLETE STATE

This is a gameplay state associated with Snap.

It is not necessarily a separate screen.

Conceptually:

SNAP COMPLETE

287 / 300

Purpose:

Confirm completion before automatically transitioning to Shift.

The transition should be short.

No unnecessary Continue button.

11. SCR-006 — Shift

Category: Gameplay

Primary job:

Remember / identify change.

Entry:

Snap completion.

Exit:

Crowd Call.

Challenge position:

2 / 3

Gameplay: Yes.

Bottom navigation: No.

Primary gameplay focus:

Memory and precision.

The grid is the dominant gameplay element.

12. SHIFT COMPLETE STATE

This is a gameplay state associated with Shift.

It is not necessarily a separate screen.

Purpose:

Confirm Shift completion before transitioning to Crowd Call.

The transition should preserve the continuous Battle flow.

13. SCR-007 — Crowd Call

Category: Gameplay

Primary job:

Predict what most people would choose.

Entry:

Shift completion.

Exit:

Crowd Result → Results.

Challenge position:

3 / 3

Gameplay: Yes.

Bottom navigation: No.

Primary gameplay focus:

Social prediction / intuition.

14. CROWD RESULT STATE

Crowd Result is a state associated with Crowd Call.

It is not necessarily a separate primary screen.

Purpose:

Reveal the crowd distribution and communicate whether the player's
prediction matched the crowd.

Conceptually:

THE CROWD SAID

...
 
YOU PREDICTED IT ✓

After the result state, the user proceeds to Results.

15. SCR-008 — Results

Category: Results

Primary job:

Understand today's performance.

Entry:

Completion of the Official Battle.

Exit:

Rival and other secondary navigation destinations.

Primary information hierarchy:

Final score
Percentile
Improvement
Battle breakdown
Personal performance context
Rival comparison

The Result screen should feel like a premium scoreboard rather than a
spreadsheet.

16. RESULT REVEAL STATE

Results have a sequential reveal.

Conceptually:

TODAY'S RESULT
      ↓
Score
      ↓
Percentile
      ↓
Improvement
      ↓
Battle Breakdown
      ↓
Rival

This is a state/animation sequence rather than separate navigation screens.

17. SCR-009 — Rival

Category: Competition

Primary job:

Beat one specific person.

Entry points:

Results
Home
Friends / relevant competition flow

Primary content:

current rival
rival score
player's score
score gap
Beat Rival action
small relevant match history where applicable

Gameplay: No.

Bottom navigation: Yes when reached as a normal application screen.

The Rival screen should focus on one person.

It should not become a Friends directory.

18. RIVAL PRACTICE LAUNCH

Practice is a secondary flow from Rival.

Conceptually:

Rival
 ↓
Practice

Practice does not replace today's Official Battle.

Practice must remain visually distinguishable from Official gameplay.

19. SCR-010 — Friends

Category: Competition

Primary job:

Manage and compare with friends.

Friends is a competition network, not a social feed.

It may contain:

friend list
today's scores
rival relationships
Add Friend entry

It should NOT contain:

posts
likes
comments
follower counts
social feed
chat
20. SCR-011 — Add Friend

Category: Competition

Primary job:

Add a friend.

The current product architecture uses:

Battle Code

as the friend-addition mechanism.

Conceptually:

ADD FRIEND

Battle Code

[ ADD FRIEND ]

YOUR CODE
[ COPY ]

The current specification does not require:

contact permissions
phone-number search
location-based discovery
21. INCOMING CHALLENGE STATE

Incoming Challenge is a secondary Friends flow.

It is not one of the 13 primary screens.

It belongs conceptually under:

Friends

The detailed interaction is not yet fully specified.

Therefore:

Status: PENDING

Do not invent additional challenge mechanics.

22. SCR-012 — Profile

Category: Personal Performance

Primary job:

Understand yourself.

The Profile is referred to as:

Battle DNA

Primary information may include:

player identity
momentum
best score
average score
Battle DNA dimensions
records

The product must not make unsupported IQ or scientific claims.

23. SCR-013 — History

Category: Personal Performance

Primary job:

See progress.

History may contain:

score history
improvement
best score
average
momentum
recent Battles
one appropriate progress visualization

History should not become an excessive analytics dashboard.

24. SETTINGS — SECONDARY FLOW

Settings is part of the application architecture but is not counted among
the 13 primary screens in the current product specification.

Settings provides lightweight:

sound controls
haptic controls
notification controls
Battle name/code controls
privacy
account deletion
terms
about

Detailed Settings screen architecture belongs in:

04_SCREEN_BLUEPRINTS.md

when the Settings screen family is specified.

25. PRIMARY USER FLOW

The first-session primary flow is:

Welcome
   ↓
Battle Name
   ↓
Home
   ↓
Today's Battle
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
Add Friend

The exact first-session navigation after Results may evolve, but this is the
current documented product flow.

26. RETURNING USER FLOW

For a returning player with today's Battle available:

App Launch
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
27. COMPLETED-BATTLE FLOW

If today's Battle has already been completed:

App Launch
   ↓
Home
   ↓
Today's Result
   ↓
Rival

The exact direct navigation from Home to Result is an implementation detail
to be finalized in the screen blueprint/navigation specifications.

The important product rule is:

The player must not be presented with another official attempt.

28. SECONDARY FLOW — PRACTICE
Rival
   ↓
Practice
   ↓
Practice Challenge

Practice is separate from the Official Battle.

It must not modify the official daily result.

29. SECONDARY FLOW — ADD FRIEND
Friends
   ↓
Add Friend
   ↓
Enter Battle Code
   ↓
Add Friend

The detailed success/error states will be defined later.

30. SECONDARY FLOW — INCOMING CHALLENGE

Current architecture:

Friends
   ↓
Incoming Challenge

The exact challenge behavior is not yet sufficiently defined.

Status:

PENDING
31. SECONDARY FLOW — SETTINGS

Settings can be reached from the application shell / profile area according
to the final navigation design.

Current Settings responsibilities:

Preferences
Account
Privacy
Legal
About

The final entry point is to be specified in the navigation blueprint.

32. BOTTOM NAVIGATION OWNERSHIP

The main application navigation contains:

Home
Battle
Friends
Me

The Battle destination is emphasized as the core product action.

Gameplay screens do NOT display the bottom navigation.

Gameplay screens are immersive.

33. BOTTOM NAVIGATION — SCREEN RELATIONSHIP

Conceptually:

Home
 ├── Today's Battle
 └── Rival

Battle
 └── Official Battle

Friends
 ├── Friends
 ├── Add Friend
 └── Incoming Challenge

Me
 ├── Profile
 ├── History
 └── Settings

The exact navigation implementation and destination behavior belong in:

06_NAVIGATION_AND_FLOWS.md

34. GAMEPLAY NAVIGATION RULE

During active gameplay:

Bottom Navigation = Hidden

Do not expose:

Home
Friends
Profile
History
Settings

inside the active challenge UI.

The player should remain focused on the challenge.

35. GAMEPLAY BACK NAVIGATION

Gameplay Back behavior is intentionally not fully defined here.

It must be specified consistently with:

02_GAMEPLAY.md

and

09_OFFLINE_AND_ERROR_HANDLING.md

Current status:

DECISION_REQUIRED

Until defined, implementation must not invent a destructive Back behavior.

36. SCREEN STATE CLASSIFICATION

Every screen/state belongs to one of these categories:

SCREEN
STATE
FLOW
OVERLAY

Current architecture:

Primary screens
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
Secondary states
Home Completed
Snap Complete
Shift Complete
Crowd Result
Secondary flows
Practice
Incoming Challenge
Settings

This distinction prevents unnecessary screen proliferation.

37. SCREEN VS STATE RULE

A visual change does not automatically create a new screen.

For example:

Snap
   ↓
Snap Complete

can remain one screen with different state.

Likewise:

Home Ready
   ↓
Home Completed

can remain one screen with different state.

Create a separate screen only when navigation, responsibility, or interaction
context materially changes.

38. SCREEN OWNERSHIP RULE

Each screen owns its primary responsibility.

Examples:

Home
owns:
Today's Battle entry

Snap
owns:
Reaction gameplay

Shift
owns:
Memory gameplay

Crowd Call
owns:
Prediction gameplay

Results
owns:
Performance result

Rival
owns:
One-person competition

Friends
owns:
Friend competition network

Profile
owns:
Personal Battle identity/performance

History
owns:
Progress over time

Do not duplicate the same major functionality across unrelated screens.

39. SCREEN INFORMATION HIERARCHY

Every screen follows:

Level 1
What should I do?

Level 2
What matters to me?

Level 3
Supporting information

Level 3 information must not overpower Level 1.

This is particularly important on:

Home
Results
Rival
Friends
Profile
History
40. SCREEN DENSITY RULE

Avoid turning screens into collections of:

cards
labels
statistics
badges
charts
secondary actions

The product specification explicitly favors restraint.

The screen should communicate its dominant job immediately.

41. GAMEPLAY SCREEN DENSITY

Gameplay is even more restrictive.

During active gameplay, the hierarchy is:

1. Challenge
2. Required instruction
3. Progress / timer
4. Current score

Everything else is secondary or should be removed.

42. SCREEN ENTRY / EXIT CONTRACT

Every implemented screen must define:

Entry condition
Entry source
Initial state
Primary action
Secondary actions
Exit destinations
Back behavior
Loading state
Error state
Empty state where applicable
Completion state where applicable

This contract is required before implementation.

43. SCREEN ARCHITECTURE REQUIREMENTS
REQ-SCREEN-001
All primary screens must have stable identifiers.

REQ-SCREEN-002
The MVP contains 13 primary screens.

REQ-SCREEN-003
Settings remains a secondary flow unless explicitly promoted.

REQ-SCREEN-004
Gameplay challenges are treated as immersive screens.

REQ-SCREEN-005
Gameplay hides bottom navigation.

REQ-SCREEN-006
Gameplay does not expose unrelated social/navigation controls.

REQ-SCREEN-007
Home owns entry into today's Battle.

REQ-SCREEN-008
Results owns completed-Battle performance presentation.

REQ-SCREEN-009
Rival owns one-person competitive comparison.

REQ-SCREEN-010
Friends owns friend-based competition.

REQ-SCREEN-011
Profile owns Battle DNA / personal identity.

REQ-SCREEN-012
History owns progress over time.

REQ-SCREEN-013
Secondary gameplay states should not automatically become separate screens.

REQ-SCREEN-014
Official and Practice flows remain distinguishable.

REQ-SCREEN-015
Every implemented screen must have defined entry and exit behavior.
44. UNDEFINED ARCHITECTURE ITEMS

The following are not sufficiently specified yet:

[ ] Exact Battle tab destination behavior
[ ] Exact Me tab structure
[ ] Settings entry point
[ ] Incoming Challenge detailed flow
[ ] Practice screen architecture
[ ] Gameplay Back behavior
[ ] Completed Home → Result navigation
[ ] Authentication/account architecture
[ ] Deep-link behavior
[ ] Notification entry points
[ ] External share entry/return behavior

These must be resolved in later documentation.

Antigravity must not invent them.

45. IMPLEMENTATION RULE

If an implementation task references a screen ID from this document,
Antigravity must use the defined responsibility and flow.

Example:

SCR-003 — Home

means:

Home is responsible for getting the user into today's Battle.

It does NOT mean Antigravity may independently add:

a global leaderboard
a news feed
extra analytics
achievements
coins
chat
arbitrary widgets

unless another approved specification explicitly requires them.

46. SCREEN CREATION RULE

Before creating a new screen, Antigravity must determine whether the
requirement can be represented as:

Existing screen
+
state

rather than creating another screen.

If a new screen appears necessary:

STOP
↓
Document proposed screen
↓
Explain why an existing screen/state is insufficient
↓
Create decision/deviation record
↓
Wait for approval
47. SCREEN DELETION RULE

Antigravity must not remove a documented screen because it believes the
screen is unnecessary.

A screen may only be removed after:

Requirement review
↓
Impact analysis
↓
Decision record
↓
Specification update
48. NAVIGATION INTEGRITY

No screen should create a dead end.

Every user-accessible screen must have a defined path to:

continue
complete
go back
exit
recover from an error

where those actions are applicable.

49. PRODUCT LOOP

The complete product architecture should reinforce:

Home
 ↓
Today's Battle
 ↓
Snap
 ↓
Shift
 ↓
Crowd Call
 ↓
Result
 ↓
Rival
 ↓
Done
 ↓
Tomorrow

The intended emotional rhythm is:

Anticipation
↓
Play
↓
Focus
↓
Tension
↓
Score Reveal
↓
Self Comparison
↓
Friend Rivalry
↓
Done
↓
Return Tomorrow
50. ARCHITECTURE DEFINITION OF DONE

The screen architecture is considered implementation-ready when:

[ ] Every primary screen has a stable ID
[ ] Every screen has one dominant responsibility
[ ] Every screen has defined category
[ ] Primary entry points are defined
[ ] Primary exit points are defined
[ ] Gameplay screens are separated from application navigation
[ ] Secondary states are distinguished from primary screens
[ ] Secondary flows are identified
[ ] Bottom navigation ownership is defined
[ ] No unnecessary duplicate screens exist
[ ] Undefined flows are explicitly marked
[ ] No undocumented screen is introduced
[ ] No documented screen is removed without approval
[ ] Navigation dependencies are ready for 06_NAVIGATION_AND_FLOWS.md
51. FINAL ARCHITECTURE RULE

The screen architecture is a contract.

It defines:

WHAT screens and states exist and WHY they exist.

It does not define:

HOW every screen is visually implemented.

Visual implementation belongs to:

04_SCREEN_BLUEPRINTS.md

Technical implementation belongs to:

07_TECHNICAL_ARCHITECTURE.md

Navigation behavior belongs to:

06_NAVIGATION_AND_FLOWS.md

If an implementation requires a screen that is not defined here:

DO NOT INVENT IT.

Create a documented proposal and obtain approval before adding it.