# DAILY BATTLE — SCREEN BLUEPRINTS

Version: 1.0
Status: Product Specification
Authority: Product + UX Blueprint
Master Canvas: 390 × 844 px
Primary Platform: Android
Design Language: Premium dark-first interactive product
Typography: Inter
Spacing System: 8pt
Primary Content Margin: 20px

---

# 0. PURPOSE

This document defines the implementation-level blueprint for every Daily Battle screen.

It sits between:

- `03_SCREEN_ARCHITECTURE.md`
- `05_DESIGN_SYSTEM.md`

The purpose of this document is to answer:

- What exists on each screen?
- Where does it exist?
- What is visually dominant?
- What can the user interact with?
- What states must exist?
- What happens after each interaction?
- What happens when data is unavailable?
- What happens when the user returns to the screen?
- What changes between Ready / Active / Completed states?
- What must NOT be added?

This document does NOT define:

- Android implementation details
- database schemas
- API contracts
- exact gameplay algorithms
- backend architecture
- authentication architecture
- monetization implementation
- analytics implementation

Those belong to later documents.

---

# 1. BLUEPRINT AUTHORITY

The following documents are authoritative in this order:

1. `00_MASTER_SPEC.md`
2. `01_PRODUCT.md`
3. `02_GAMEPLAY.md`
4. `03_SCREEN_ARCHITECTURE.md`
5. `04_SCREEN_BLUEPRINTS.md`
6. `05_DESIGN_SYSTEM.md`
7. `06_NAVIGATION_AND_FLOWS.md`
8. `07_TECHNICAL_ARCHITECTURE.md`

If a later implementation document conflicts with this blueprint, the conflict must be reported.

Do not silently modify product behavior.

---

# 2. MASTER SCREEN RULES

## 2.1 One screen = one dominant job

Every screen must have one obvious purpose.

| Screen | Dominant Job |
|---|---|
| Welcome | Enter Daily Battle |
| Battle Name | Establish player identity |
| Home | Play today's Battle |
| Battle Intro | Confirm and start official Battle |
| Snap | React |
| Shift | Remember |
| Crowd Call | Predict |
| Results | Understand performance |
| Rival | Beat someone |
| Friends | Manage competition network |
| Add Friend | Connect through Battle Code |
| Profile | Understand yourself |
| History | See progress |
| Settings | Manage preferences and account |

---

# 3. GLOBAL CANVAS

## 3.1 Master frame

```text
390 × 844 px
3.2 Validation frames
412 × 915
360 × 800

The 390 × 844 frame is the master.

The UI must adapt without requiring a separate design.

4. GLOBAL LAYOUT RULES
4.1 Horizontal content margin

Primary:

20px

Major hero sections may use:

24px
4.2 Top safe area

Reserve approximately:

24px + system status area

Do not place important content underneath the system status area.

4.3 Bottom navigation

When bottom navigation exists:

Home
Battle
Friends
Me

Reserve sufficient bottom space.

Approximate navigation height:

72–80px
4.4 Gameplay

Gameplay screens:

hide bottom navigation
hide social controls
hide profile controls
hide friend scores
hide unnecessary history
prioritize the challenge itself

Gameplay should feel immersive.

5. GLOBAL INFORMATION HIERARCHY

Every screen follows three information levels.

Level 1

What should I do?

Level 2

What matters to me?

Level 3

Supporting information.

Never give every element equal visual weight.

6. GLOBAL COMPONENT RULES

Primary reusable components:

Top Bar
Back Button
Primary Button
Secondary Button
Ghost Button
Icon Button
Battle Card
Rival Card
Result Card
Stat Row
Friend Row
Answer Card
Progress Dots
Timer
Score
Feedback Indicator
Toast
Empty State
Error State
Loading State
Bottom Navigation

Component states must follow the global state system.

7. GLOBAL STATE SYSTEM
7.1 Button
Default
Pressed
Disabled
Loading
Success
7.2 Game
Ready
Active
Correct
Incorrect
Complete
7.3 Battle
Not Started
In Progress
Completed
7.4 Friend
Pending
Accepted
Blocked
8. SCREEN BLUEPRINT FORMAT

Every screen below follows this structure:

Purpose
Entry
Exit
Layout
Content
Components
States
Interactions
Navigation
Edge cases
Responsive rules
Acceptance criteria
Explicit non-goals
9. SCR-001 — WELCOME
Purpose

Introduce Daily Battle and allow the user to begin onboarding.

Dominant job:

Enter Daily Battle.

Entry

First launch.

Layout
┌─────────────────────────────┐
│                             │
│                             │
│        DAILY BATTLE         │
│                             │
│    3 challenges.            │
│    ~3 minutes.              │
│    One score.               │
│                             │
│                             │
│                             │
│       [ GET STARTED ]       │
│                             │
│                             │
└─────────────────────────────┘

Exact decorative treatment remains subject to the final design system.

Content hierarchy
Level 1

Daily Battle identity.

Level 2

Core promise.

Level 3

Get Started action.

Primary interaction
GET STARTED

Navigates to:

SCR-002 Battle Name
States
Default

Normal onboarding state.

Pressed

Primary button pressed feedback.

Loading

Only required if onboarding initialization requires asynchronous work.

Do not introduce a fake loading state.

Edge cases

If local initialization fails:

Couldn’t start Daily Battle.
Try again.

Provide retry.

Non-goals

Do not add:

login form unless authentication is separately approved
social login
long product explanation
feature carousel
leaderboard preview
AI explanation
gameplay tutorial carousel
Acceptance criteria
Daily Battle identity is immediately visible.
Core promise is understandable.
One obvious primary action exists.
No dashboard elements are present.
Screen works on 360px width.
10. SCR-002 — BATTLE NAME
Purpose

Allow the user to establish their in-game identity.

Dominant job:

Choose Battle Name.

Layout
┌─────────────────────────────┐
│                             │
│         YOUR BATTLE         │
│            NAME             │
│                             │
│  Choose the name your       │
│  friends will see.          │
│                             │
│  ┌───────────────────────┐  │
│  │ Battle Name           │  │
│  └───────────────────────┘  │
│                             │
│                             │
│       [ CONTINUE ]          │
│                             │
└─────────────────────────────┘

The exact explanatory copy can be refined later.

Components
Screen title
Text input
Primary button
Validation message
Input states
Empty
Focused
Valid
Invalid
Disabled
Validation

The exact:

minimum length
maximum length
allowed characters
uniqueness requirements

are not defined in the current product specification.

These must be defined before implementation.

Do not invent them.

Interaction

User enters Battle Name.

If valid:

CONTINUE

→ SCR-003 Home

If invalid:

Show inline validation.

Do not navigate.

Edge cases
Empty input
Invalid input
Keyboard overlap
Very long name
Existing saved name on app reopen
Acceptance criteria
User can clearly identify the required input.
Validation is visible without relying only on color.
Continue is disabled or blocked when the input is invalid.
Keyboard does not obscure the primary action.
11. SCR-003 — HOME
Purpose

Get the user into today's Battle.

Dominant job:

Play today's Battle.

11.1 READY STATE
Layout
┌─────────────────────────────┐
│ Good evening, Sanket.       │
│                             │
│ Momentum                   │
│ 🔥 7 days                  │
│                             │
│ ┌─────────────────────────┐ │
│ │ TODAY'S BATTLE           │ │
│ │                          │ │
│ │ 3 challenges             │ │
│ │ ~3 minutes               │ │
│ │                          │ │
│ │ ● ● ●                    │ │
│ │                          │ │
│ │ [ PLAY BATTLE ]          │ │
│ └─────────────────────────┘ │
│                             │
│ YOUR RIVAL                  │
│ ┌─────────────────────────┐ │
│ │ Rahul       914          │ │
│ │ You         901          │ │
│ │                          │ │
│ │ 13 points to catch       │ │
│ │                          │ │
│ │ [ BEAT RAHUL ]           │ │
│ └─────────────────────────┘ │
│                             │
│ Home Battle Friends Me      │
└─────────────────────────────┘
Hero card

Approximate meaningful viewport occupation:

50–55%

Content:

TODAY'S BATTLE

3 challenges
~3 minutes

● ● ●

[ PLAY BATTLE ]

Do NOT reveal:

Snap
Shift
Crowd Call

before the Battle starts.

Momentum

Momentum may appear as a supporting element.

It must not compete with Today's Battle.

Rival card

Example:

YOUR RIVAL

Rahul       914
You         901

13 points to catch

[ BEAT RAHUL ]

Rival is secondary to Today's Battle.

11.2 COMPLETED STATE

After official Battle completion:

TODAY'S BATTLE ✓

901
TOP 9%

NEXT BATTLE
TOMORROW

Then rival information.

The official Play Battle button disappears.

Important rule

Never make the user believe another official attempt is available.

Interactions
PLAY BATTLE

→ SCR-004 Battle Intro

BEAT RAHUL

→ SCR-009 Rival

Bottom nav

Home:

stay on Home.

Battle:

destination behavior must be finalized.

Friends:

→ SCR-010 Friends

Me:

Profile destination behavior must be finalized.

Loading state

Use subtle skeletons for:

Today's Battle data
rival score
momentum

Do not show large spinner unless necessary.

Error state
COULDN'T LOAD TODAY'S BATTLE

Check your connection
and try again.

[ RETRY ]

Do not throw the user back to onboarding/login.

Empty rival state
NO RIVAL YET

Add a friend and start
your first Battle rivalry.

[ ADD FRIEND ]
Acceptance criteria
Today's Battle is visually dominant.
Rival is secondary.
No dashboard clutter.
Completed state cannot accidentally restart official Battle.
Loading and error states exist.
Bottom navigation remains visible.
12. SCR-004 — BATTLE INTRO
Purpose

Confirm the user is entering today's official Battle.

Dominant job:

Start official Battle.

Layout
┌─────────────────────────────┐
│                             │
│        TODAY'S BATTLE       │
│                             │
│        3 challenges         │
│        ~3 minutes           │
│                             │
│        ● ○ ○                │
│                             │
│       OFFICIAL ATTEMPT      │
│                             │
│     [ START BATTLE ]        │
│                             │
└─────────────────────────────┘
Rules

Do not show exact challenge names.

Do not add unnecessary information.

Primary interaction
START BATTLE

→ SCR-005 Snap

Back behavior

Exact Gameplay/Intro Back behavior is not fully defined.

Implementation must not invent destructive or restart behavior.

This must be resolved before final implementation.

Acceptance criteria
Official attempt is obvious.
User understands there are 3 challenges.
Start action is unmistakable.
No social content appears.
13. SCR-005 — SNAP
Purpose

Test fast reaction.

Dominant job:

React.

Gameplay mode

Bottom navigation hidden.

Social controls hidden.

Gameplay occupies the screen.

Layout
┌─────────────────────────────┐
│ SNAP                    1/3 │
│                             │
│         TIMER               │
│                             │
│                             │
│       GAME AREA             │
│                             │
│       TARGET /              │
│       DISTRACTOR            │
│                             │
│                             │
│                             │
│       Instruction           │
│       Current Score         │
└─────────────────────────────┘

Game area should occupy approximately:

60–65%
Gameplay states
Ready
Active
Correct
Incorrect
Complete
Ready

Prepare user for the challenge.

No unnecessary explanatory text.

Active

Only the information required to play remains visible.

Correct

Use:

small pulse
success feedback
appropriate haptic
score update if applicable

Avoid large celebratory overlays.

Incorrect

Use:

small shake
short error feedback
appropriate haptic

Do not display:

WRONG!!!

as a giant overlay.

Complete

Example:

SNAP COMPLETE

287 / 300

Then automatically transition to Shift.

No manual Continue button.

Transition

Game transition:

250–400ms
Non-goals

Do not add:

leaderboard
rival score
friend information
profile
social controls
unnecessary stats
persistent bottom navigation
Acceptance criteria
Game area dominates.
Controls are comfortably tappable.
Feedback does not interrupt flow.
Completion automatically advances.
Bottom navigation remains hidden.
14. SCR-006 — SHIFT
Purpose

Test memory and precision.

Dominant job:

Remember.

Layout
┌─────────────────────────────┐
│ SHIFT                   2/3 │
│                             │
│          TIMER              │
│                             │
│      ┌──┬──┬──┬──┐          │
│      │  │  │  │  │          │
│      ├──┼──┼──┼──┤          │
│      │  │  │  │  │          │
│      ├──┼──┼──┼──┤          │
│      │  │  │  │  │          │
│      └──┴──┴──┴──┘          │
│                             │
│        WHAT MOVED?          │
│                             │
│     [ ANSWER OPTION ]       │
│     [ ANSWER OPTION ]       │
└─────────────────────────────┘

The exact grid dimensions are defined by gameplay requirements and must not be invented here.

Design character

Shift should visually contrast with Snap.

Snap:

Fast
Reactive
Minimal

Shift:

Precise
Structured
Grid-based
States
Ready
Active
Selected
Correct
Incorrect
Complete
Answer interaction

Answer cards must remain comfortably tappable.

Target:

approximately 52–56px touch height
Feedback

Selected state must not rely only on color.

Use combinations such as:

border
background
check / indicator
motion
Completion

Example:

SHIFT COMPLETE

252 / 300

Then automatically proceed to Crowd Call.

Acceptance criteria
Grid is visually dominant.
Answer options are easily tappable.
Selection is accessible without color alone.
No bottom navigation.
Automatic transition after completion.
15. SCR-007 — CROWD CALL
Purpose

Test social prediction / intuition.

Dominant job:

Predict.

Layout
┌─────────────────────────────┐
│ CROWD CALL              3/3 │
│                             │
│                             │
│        QUESTION             │
│                             │
│  ┌───────────────────────┐  │
│  │ Answer A              │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ Answer B              │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ Answer C              │  │
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
Design character

This should be the most socially expressive gameplay screen.

Still:

premium
focused
dark-first
restrained

Do not turn it into a social feed.

Answer cards

Target minimum touch height:

52–56px
Result state

After selection:

Show crowd distribution and prediction result.

Example structure:

THE CROWD

A  ███████████  62%
B  █████        28%
C  ██           10%

YOUR PICK
A

CORRECT

Exact visualization and scoring behavior remain subject to gameplay specification.

Completion

After result:

transition to:

SCR-008 Results
Acceptance criteria
Question is dominant.
Answer cards are obvious.
Selection state is accessible.
Crowd result is understandable.
No social feed behavior.
No manual continuation unless specifically required by gameplay.
16. SCR-008 — RESULTS
Purpose

Communicate completion and performance.

Dominant job:

Understand performance.

16.1 Core hierarchy

The result sequence is:

TODAY'S RESULT
       ↓
Score
       ↓
Percentile
       ↓
Improvement
       ↓
Breakdown
       ↓
Rival
       ↓
Tomorrow
Layout
┌─────────────────────────────┐
│       TODAY'S RESULT        │
│                             │
│           901               │
│         / 1000              │
│                             │
│          TOP 9%             │
│                             │
│        +37 YESTERDAY        │
│                             │
│ ┌─────────────────────────┐ │
│ │ SNAP          287       │ │
│ │ SHIFT         252       │ │
│ │ CROWD CALL    271       │ │
│ └─────────────────────────┘ │
│                             │
│ PERSONAL BEST     927       │
│ AVERAGE           806       │
│                             │
│ RAHUL             914       │
│ YOU               901       │
│                             │
│ 13 points to catch          │
│                             │
│ [ BEAT RAHUL ]              │
│                             │
│ Tomorrow's Battle awaits.   │
└─────────────────────────────┘

The exact values above are illustrative values from the design specification, not hardcoded product data.

Score reveal

Animation duration:

700–1000ms

Sequence:

Score count
Percentile
Improvement
Breakdown
Rival
Score language

Never show an unexplained number.

Preferred:

901 / 1000

or:

901
TOP 9%
16.2 PERSONAL BEST STATE

When a new record is achieved:

NEW PERSONAL BEST

927

+23

Use subtle celebration:

small particles
score glow
stronger success haptic

Do not use full-screen fireworks.

Actions
BEAT RAHUL

→ SCR-009 Rival

Share Result

Share functionality is specified conceptually, but exact Android share flow is not defined here.

Do not invent final implementation.

Completion message

The result screen should leave the user with:

score
rival
tomorrow

This closes the daily loop.

Acceptance criteria
Score is immediately visible.
Score context is clear.
Percentile is secondary.
Breakdown is understandable.
Rival is personal, not global.
Completion feels final.
User understands official Battle is finished.
17. SCR-009 — RIVAL
Purpose

Create a direct one-person competitive goal.

Dominant job:

Beat someone.

Core principle
ONE PERSON.
ONE GAP.
ONE GOAL.
Layout
┌─────────────────────────────┐
│          YOUR RIVAL         │
│                             │
│           Rahul             │
│            914              │
│                             │
│             VS              │
│                             │
│           Sanket            │
│            901              │
│                             │
│      13 POINTS TO CATCH     │
│                             │
│       [ BEAT RAHUL ]        │
│                             │
│       MATCH HISTORY         │
│                             │
│       small + clean        │
└─────────────────────────────┘
Rules

Do not show:

entire friends list
global leaderboard
huge analytics
unnecessary charts
Elo
competitive rating
tiers
Practice

Rival can lead to Practice.

Practice must be clearly labeled:

PRACTICE

Practice must never look like the official Battle.

Acceptance criteria
One rival is dominant.
Score gap is immediately understandable.
Primary CTA is obvious.
Match history remains secondary.
No global ranking language.
18. SCR-010 — FRIENDS
Purpose

Manage the competition network.

Dominant job:

Manage rivals.

Layout
┌─────────────────────────────┐
│ FRIENDS                     │
│                             │
│ [ + ADD FRIEND ]            │
│                             │
│ TODAY                       │
│                             │
│ Rahul       914             │
│ Sanket      901      YOU    │
│ Neha        817             │
│ Aman        761             │
│                             │
└─────────────────────────────┘
Rules

Friends are a competition network.

This is NOT a social network.

Do not include:

likes
followers
comments
posts
chat
infinite feed
Sorting

Default:

Highest today's score first

The user must always be visually identifiable as:

YOU

Even when not first.

Friend row states
Normal
You
Rival
Pending
Empty state
NO RIVALS YET

Add a friend and start
your first Battle rivalry.

[ ADD FRIEND ]
Privacy

Do not reveal:

location
phone number
email
private stats

Only expose:

Battle name
competition-relevant game statistics
Acceptance criteria
Friends screen is score-focused.
User is easy to locate.
Add Friend is obvious.
No social-feed behavior.
Empty state exists.
Privacy boundaries are respected.
19. SCR-011 — ADD FRIEND
Purpose

Connect two players through Battle Code.

Dominant job:

Add a friend.

Layout
┌─────────────────────────────┐
│                             │
│       ADD A FRIEND          │
│                             │
│       Battle Code           │
│                             │
│  ┌───────────────────────┐  │
│  │ B7K9P2                │  │
│  └───────────────────────┘  │
│                             │
│       [ ADD FRIEND ]        │
│                             │
│                             │
│       YOUR BATTLE CODE      │
│                             │
│          K4X8M9             │
│          [ COPY ]           │
│                             │
└─────────────────────────────┘
Explicitly excluded

Do not implement:

contact permission
phone-number search
location search
States
Empty

No code entered.

Valid

Code accepted for submission.

Invalid

Code format or lookup failed.

Loading

Friend request is being submitted.

Success

Friend request successfully sent.

Error

Request could not be completed.

Acceptance criteria
Battle Code is the central interaction.
User can copy their own code.
No contact access is required.
Errors are understandable.
Success is clearly communicated.
20. SCR-012 — PROFILE / BATTLE DNA
Purpose

Communicate:

Who am I as a player?

Dominant job:

Understand yourself.

Layout
┌─────────────────────────────┐
│ PROFILE                     │
│                             │
│ Sanket                      │
│                             │
│ 🔥 7 DAY MOMENTUM           │
│                             │
│ BEST SCORE                  │
│ 927                         │
│                             │
│ AVERAGE                     │
│ 806                         │
│                             │
│ BATTLE DNA                  │
│                             │
│ SPEED        91             │
│ MEMORY       78             │
│ PEOPLE       94             │
│                             │
│ PERSONAL RECORDS            │
│                             │
│ Best Score                  │
│ Best Momentum               │
│ Battles Played              │
└─────────────────────────────┘
Rules

Battle DNA is a game profile.

It is NOT a scientific assessment.

Do not use:

IQ
intelligence claims
“smarter than X%”
scientific personality claims
Stats

The current specification uses:

Speed
Memory
People

The exact calculation formulas are not defined in this blueprint.

Do not invent formulas.

Acceptance criteria
Player identity is obvious.
Momentum is visible.
Best and average scores are understandable.
Battle DNA feels like game feedback.
No scientific claims.
21. SCR-013 — HISTORY
Purpose

Show improvement over time.

Dominant job:

See progress.

Layout
┌─────────────────────────────┐
│ HISTORY                     │
│                             │
│ YOUR SCORE                  │
│                             │
│ 950 ┤         ●             │
│ 900 ┤     ●   │             │
│ 850 ┤  ●      ●             │
│ 800 ┤●                        │
│     └────────────             │
│                             │
│ BEST       927               │
│ AVERAGE    806               │
│ MOMENTUM   7 DAYS            │
│                             │
│ RECENT BATTLES              │
│                             │
│ Today       901              │
│ Yesterday   864              │
│ ...                         │
└─────────────────────────────┘
Rules

One graph is sufficient.

Do not create an analytics dashboard.

Do not add:

multiple charts
dozens of filters
excessive statistics
complicated performance analysis
Empty state

If no historical battles exist:

YOUR HISTORY STARTS TODAY

Complete your first Battle
to see your progress.
Acceptance criteria
One graph communicates progress.
Best / Average / Momentum are secondary.
Recent Battles are readable.
Screen does not feel like an analytics dashboard.
22. SETTINGS — SECONDARY FLOW

Settings is a secondary flow rather than one of the 13 primary screens.

Purpose

Provide calm account, preference, privacy, and legal controls.

Dominant job:

Manage preferences and account.

Visual character

Settings is the calmest screen in the app.

Do not use:

game-like cards
large hero sections
promotional panels
decorative score treatments
excessive visual effects

Prefer grouped rows.

Layout
┌─────────────────────────────┐
│ SETTINGS                    │
│                             │
│ GAME                        │
│ Sound                  ON   │
│ Haptics                ON   │
│ Notifications          ON   │
│                             │
│ ACCOUNT                     │
│ Battle Name                >│
│ Battle Code                >│
│                             │
│ PRIVACY                     │
│ Privacy Policy             >│
│ Delete Account             >│
│                             │
│ ABOUT                       │
│ Terms                      >│
│ About Daily Battle         >│
└─────────────────────────────┘
Rules

No Light / Dark / System theme selection in V1.

Daily Battle is dark-first in MVP.

Destructive action

Delete Account must not be triggered accidentally.

A confirmation flow is required.

Exact confirmation wording is not defined here.

Acceptance criteria
Settings uses grouped rows.
No unnecessary cards.
Sound and Haptics are easy to control.
Privacy and legal items are clearly separated.
Delete Account requires deliberate confirmation.
23. SECONDARY STATE — HOME COMPLETED

State ID:

STATE-HOME-COMPLETED

Purpose:

Communicate that today's official Battle is finished.

Required information
Today's Battle ✓
Score
Percentile
Next Battle Tomorrow
Rival
Forbidden

Do not show:

PLAY BATTLE

as an official replay action.

24. SECONDARY STATE — SNAP COMPLETE

State ID:

STATE-SNAP-COMPLETE

Example:

SNAP COMPLETE

287 / 300

Duration should be short.

Automatically transition to Shift.

No manual Continue button.

25. SECONDARY STATE — SHIFT COMPLETE

State ID:

STATE-SHIFT-COMPLETE

Example:

SHIFT COMPLETE

252 / 300

Automatically transition to Crowd Call.

26. SECONDARY STATE — CROWD RESULT

State ID:

STATE-CROWD-RESULT

Required information:

user's selected answer
crowd distribution
prediction result
relevant score/result feedback

Do not turn this into a social feed.

27. SECONDARY FLOW — PRACTICE

Practice is separate from Official Battle.

Official
OFFICIAL BATTLE

Rules:

same official challenge for everyone
one official attempt
Practice
PRACTICE

Rules:

separate from official score
must not accidentally appear to change rankings
may eventually use adaptive difficulty

The current specification allows practice personalization conceptually, but exact Practice screen architecture is not fully defined.

Do not invent the final Practice UI.

28. SECONDARY FLOW — INCOMING CHALLENGE

Incoming Challenge exists in the information architecture.

Exact UI behavior is not sufficiently defined.

Before implementation, define:

where the challenge appears
what a challenge contains
who can send one
whether it expires
accept behavior
decline behavior
notification behavior
resulting navigation

Until defined:

DO NOT INVENT.
29. NAVIGATION BEHAVIOR
Primary flow
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
Secondary
Rival
  ↓
Practice
Friends
  ↓
Add Friend
Bottom navigation
Home
Battle
Friends
Me

Exact Battle tab destination behavior must be finalized before implementation.

Exact Me tab architecture must be finalized before implementation.

30. GAMEPLAY NAVIGATION RULE

Once the official Battle starts:

Snap
 ↓
Shift
 ↓
Crowd Call
 ↓
Results

The challenge sequence should feel continuous.

Avoid introducing unnecessary navigation controls between challenges.

31. BACK BEHAVIOR

Back behavior must be explicitly defined before implementation for:

Battle Intro
Snap
Shift
Crowd Call
Results
Rival
Practice
Add Friend
Settings

Especially during an official Battle, Back must not accidentally:

restart a challenge
create a second attempt
discard an official result
duplicate a submission

If behavior is undefined:

STOP IMPLEMENTATION
ASK FOR PRODUCT DECISION
32. RESPONSIVE RULES
32.1 360px width

Do not drastically shrink typography.

Instead:

reduce horizontal gaps
reduce card padding
allow text wrapping
stack content where required
preserve touch targets
32.2 412px width

Use additional breathing room.

Do not simply scale every element up.

32.3 Gameplay
Snap

Scale game area proportionally.

Shift

Maintain comfortably tappable grid cells.

Crowd Call

Maintain answer card touch height around:

52–56px
33. TOUCH TARGET RULES

General minimum:

44 × 44px

Preferred for primary controls:

48–52px

Fast gameplay requires particularly careful touch sizing.

34. ACCESSIBILITY

Never communicate state using color alone.

Example selected state:

purple border
+
check indicator
+
background change

not merely:

purple = selected

Snap should also use shapes / visual feedback in addition to color.

Text contrast must remain strong.

35. MOTION

Motion must communicate:

Change
Feedback
Reward

It must not exist merely for decoration.

Timing

Normal navigation:

200–300ms

Game transitions:

250–400ms

Score reveal:

700–1000ms

Button press:

100–150ms
36. HOME → BATTLE TRANSITION

The Battle card may:

slightly expand / zoom
        ↓
fade into gameplay

Purpose:

Create a distinct entrance into the Battle.

Do not make it excessively cinematic.

37. HAPTICS
Button

Light.

Correct

Light success.

Wrong

Short error.

Personal Best

Stronger success.

Battle Complete

Medium confirmation.

Haptics must never become excessive.

38. SOUND

No continuous background music in MVP.

Supported feedback:

tap
correct
wrong
score reveal
personal best
battle complete

Settings must provide:

Sound ON / OFF
Haptics ON / OFF
39. LOADING STATES

Use subtle loading / skeleton states.

Avoid repeated text such as:

Loading...
Loading...
Loading...

Avoid giant spinners unless technically necessary.

40. ERROR STATES

General pattern:

COULDN'T LOAD [CONTENT]

Check your connection
and try again.

[ RETRY ]

Do not unnecessarily send the user back to onboarding.

41. EMPTY STATES

Never use a blank screen.

Every empty state must communicate:

What is missing?
Why does it matter?
What can the user do next?

Example:

NO RIVALS YET

Add a friend and start
your first Battle rivalry.

[ ADD FRIEND ]
42. SCORE VISUAL LANGUAGE

Every score must have context.

Bad:

901

Preferred:

901 / 1000

or:

901

TOP 9%

Score must never appear ambiguous.

Use tabular numerals for score values.

43. PERSONAL BEST MICRO-STATE

When a new record is achieved:

NEW PERSONAL BEST

927

+23

Visual treatment:

subtle score glow
small particles
success haptic

Avoid:

full-screen fireworks
excessive confetti
blocking animations
44. RIVAL GAP VISUALIZATION

Keep the comparison simple.

Example:

Rahul     914
──────────────
You       901

13 points

Do not build a large analytical chart for a small score gap.

45. FRIENDS SORTING

Default:

Highest today's score first

The current player must remain visually identifiable.

Use:

YOU

even when the user is not first.

46. FRIEND PRIVACY

Do not expose:

location
phone number
email
private statistics

unless explicitly required by an approved product requirement.

Default visible competitive identity:

Battle Name
+
competition-relevant game statistics
47. SHARE RESULT

Conceptual share card:

DAILY BATTLE

901

TOP 9%

Can you beat me?

[ PLAY ]

Brand identity should remain strong.

Intended surfaces include:

WhatsApp
Instagram
Telegram
direct messages

Exact Android share implementation belongs to the technical specification.

48. COPY PRINCIPLES

Tone:

Confident
Concise
Playful
Competitive

Avoid:

Corporate
Childish
Motivational-speaker style
Fake scientific
Copy examples

Home:

Good evening, Sanket.

Battle:

3 challenges. ~3 minutes.

Rival:

13 points to catch.

Result:

New personal best.

Practice:

Official score stays locked.

Tomorrow:

Tomorrow's Battle awaits.

Do not use unnecessarily long descriptions.

49. OFFICIAL VS PRACTICE

This distinction must remain visually and semantically strong.

Official:

OFFICIAL BATTLE

Practice:

PRACTICE

Practice must never look as though it changes official ranking or score.

50. SOCIAL LANGUAGE

Prefer personal competition language.

Good:

Rahul is 13 points ahead.

Beat Rahul.

New personal best.

You're 37 points ahead of yesterday.

Avoid MVP-stage language such as:

Global Rank
Competitive Rating
Elo
Tier 4
51. GLOBAL LEADERBOARD

Not part of MVP.

Use:

Percentile

and:

Friends

instead.

Example:

TOP 9%
52. PRODUCT RESTRICTIONS

The following must not appear in these screens unless separately approved:

Global leaderboard
Coins
Chat
Infinite feed
IQ claims
Scientific intelligence claims
Camera-first flows
Real-time multiplayer
Excessive AI branding
Fake LIVE state
Dashboard clutter
53. AI VISUALIZATION RULE

AI is infrastructure.

Do not prominently advertise:

AI-powered challenge generation

throughout the product.

The product is about:

playing

not:

using AI
54. REAL-WORLD MODE

Real-world / Wild Card is not MVP.

Do not add Wild Card screens to the current MVP blueprint.

Future versions may introduce it while preserving the existing visual system.

55. SCREEN-BY-SCREEN QA CHECKLIST

For every implemented screen, verify:

Layout
 390 × 844 master layout works
 360 × 800 works
 412 × 915 works
 Safe areas respected
 20px content margins respected
 Bottom navigation space reserved where required
Hierarchy
 One dominant job
 Primary action obvious
 Secondary information visually subordinate
 No unnecessary micro-text
Interaction
 Touch targets ≥44px
 Primary controls approximately 48–52px
 Pressed state exists
 Disabled state exists where relevant
 Loading state exists where relevant
 Success/error feedback exists where relevant
Accessibility
 State is not communicated by color alone
 Text contrast is sufficient
 Important text is not below 12px
 Game controls are comfortably tappable
Navigation
 Entry is correct
 Exit is correct
 Back behavior is defined
 No duplicate navigation paths
 Gameplay hides bottom navigation
Product
 No unauthorized feature
 No global leaderboard
 No coins
 No chat
 No infinite feed
 No fake LIVE state
 No unauthorized AI branding
56. SCREEN ACCEPTANCE MATRIX
Screen	Primary Job	Primary Action	Key State(s)	Bottom Nav
SCR-001 Welcome	Enter app	Get Started	Default	No
SCR-002 Battle Name	Set identity	Continue	Input states	No
SCR-003 Home	Start today's Battle	Play Battle	Ready / Completed / Loading / Error	Yes
SCR-004 Battle Intro	Start official Battle	Start Battle	Ready	No
SCR-005 Snap	React	Gameplay input	Ready / Active / Correct / Incorrect / Complete	No
SCR-006 Shift	Remember	Answer	Ready / Active / Selected / Correct / Incorrect / Complete	No
SCR-007 Crowd Call	Predict	Answer	Ready / Active / Selected / Result	No
SCR-008 Results	Understand performance	Beat Rival / Share	Reveal / Personal Best / Complete	No
SCR-009 Rival	Beat someone	Beat Rival / Practice	Normal / No Rival	No
SCR-010 Friends	Manage rivals	Add Friend	Normal / Empty / Pending	Yes
SCR-011 Add Friend	Connect	Add Friend	Empty / Valid / Loading / Success / Error	No
SCR-012 Profile	Understand self	Navigate records	Normal / Empty	Yes
SCR-013 History	See progress	Navigate history	Normal / Empty	Yes
Settings	Manage preferences	Row actions	Normal / Loading / Error / Confirmation	No
57. IMPLEMENTATION PRIORITY

Screen implementation should follow this order:

Phase 1 — App Entry
SCR-001 Welcome
SCR-002 Battle Name
SCR-003 Home
Phase 2 — Official Battle Entry
SCR-004 Battle Intro
Phase 3 — Gameplay
SCR-005 Snap
SCR-006 Shift
SCR-007 Crowd Call
Phase 4 — Completion
SCR-008 Results
SCR-009 Rival
Phase 5 — Social Competition
SCR-010 Friends
SCR-011 Add Friend
Phase 6 — Personal Performance
SCR-012 Profile
SCR-013 History
Phase 7 — Secondary Flows
Settings
Practice
Incoming Challenge
Share
58. UNRESOLVED ITEMS

The following MUST NOT be silently invented by implementation agents.

UX / Navigation
Exact Battle tab destination
Exact Me tab structure
Settings entry point
Completed Home → Result behavior
Gameplay Back behavior
Practice screen architecture
Incoming Challenge architecture
Notification entry behavior
Share return behavior
Deep-link behavior
Product
Authentication/account architecture
Battle Name validation rules
Battle Code validation rules
Friend request lifecycle
Challenge lifecycle
Practice challenge rules
Gameplay
Exact Snap scoring algorithm
Exact Shift scoring algorithm
Exact Crowd Call scoring algorithm
Exact Consistency calculation
Exact timer values
Exact challenge generation rules
Data
Exact percentile calculation
Exact rival selection rules
Exact momentum calculation
Exact Battle DNA calculation
Exact history retention
Exact offline synchronization behavior
59. BLUEPRINT CHANGE CONTROL

If implementation reveals a requirement that cannot technically be satisfied:

DO NOT silently change the blueprint.

Create:

DEV-XXX

with:

Requirement:
Problem:
Proposed change:
Reason:
UX impact:
Technical impact:
Product impact:
Approval status:

Only approved deviations may become implementation behavior.

60. ANTIGRAVITY IMPLEMENTATION RULE

Antigravity must treat this file as an implementation blueprint, not inspiration.

It may:

implement specified layouts
implement specified states
implement specified interactions
improve code structure
optimize rendering
implement responsive behavior
implement accessibility requirements

It may NOT:

invent screens
invent product features
invent scoring
invent monetization
invent navigation
invent backend behavior
invent challenge rules
invent new social features
redesign the product without approval

If required information is missing:

STOP
REPORT THE GAP
ASK FOR A DECISION
61. DEFINITION OF BLUEPRINT COMPLETION

This document is considered complete when every approved screen has:

stable screen ID
clear purpose
dominant job
layout structure
content hierarchy
component list
interaction definition
state definition
navigation behavior
loading behavior
error behavior
empty behavior where relevant
responsive behavior
accessibility requirements
acceptance criteria
explicit non-goals

Undefined product behavior must remain explicitly marked as undefined.

62. FINAL DESIGN CONSTITUTION

Every implementation must pass these questions:

1.

What is the one thing this screen wants the user to do or feel?

2.

Is there one obvious visual hierarchy?

3.

Does the screen feel like Daily Battle rather than a generic dark app?

4.

Are cards being used because they are useful?

5.

Can the user understand the screen without reading tiny text?

6.

Does the screen preserve the premium-product / game-energy balance?

7.

Does it preserve the daily competitive ritual?

8.

Does it avoid unnecessary dashboard behavior?

9.

Does it preserve the distinction between Official and Practice?

10.

Does it preserve the core loop?

OPEN
 ↓
TODAY'S BATTLE
 ↓
PLAY
 ↓
SCORE
 ↓
RIVAL
 ↓
DONE
 ↓
TOMORROW

This is the fundamental Daily Battle experience.

END OF SCREEN BLUEPRINTS