# DAILY BATTLE — MASTER SPECIFICATION

**Document:** 00_MASTER_SPEC.md  
**Product:** Daily Battle  
**Platform:** Android  
**Document Status:** MASTER SPECIFICATION  
**Specification Version:** 1.0  
**Last Updated:** 2026-09-30  

---

# 1. DOCUMENT PURPOSE

This document is the highest-level product and implementation authority for
Daily Battle.

It defines:

- what Daily Battle is
- what Daily Battle is not
- the core product promise
- the core experience
- non-negotiable product rules
- official Battle rules
- challenge structure
- social and rivalry principles
- visual/product direction
- MVP boundaries
- documentation authority
- requirement identification
- change control
- unresolved decisions

Detailed implementation specifications must be defined in the appropriate
supporting documents referenced by this document.

This document must remain concise enough to act as a reliable source of truth.

It must NOT become a replacement for the detailed documents in `/docs`.

---

# 2. DOCUMENT AUTHORITY

The Daily Battle repository is the authoritative specification for the
application.

The following authority hierarchy applies:

1. `00_MASTER_SPEC.md`
2. `01_PRODUCT.md`
3. `02_GAMEPLAY.md`
4. `03_SCREEN_ARCHITECTURE.md`
5. `04_SCREEN_BLUEPRINTS.md`
6. `05_DESIGN_SYSTEM.md`
7. `06_NAVIGATION_AND_FLOWS.md`
8. `07_TECHNICAL_ARCHITECTURE.md`
9. `08_DATA_AND_API.md`
10. `09_OFFLINE_AND_ERROR_HANDLING.md`
11. `10_ANIMATION_HAPTICS.md`
12. `11_ACCESSIBILITY.md`
13. `12_TESTING_AND_QA.md`
14. `13_MONETIZATION.md`
15. `14_ANALYTICS.md`
16. `15_ANTIGRAVITY_RULES.md`
17. `16_DEFINITION_OF_DONE.md`

Supporting records:

- `/decisions/` — approved and historical decisions
- `/audits/` — implementation and specification audits

If two documents conflict:

1. Implementation must STOP.
2. The conflict must be identified.
3. The conflict must be recorded in `/decisions/`.
4. The required decision must be explicitly resolved.
5. The affected documents must be updated.
6. Implementation may continue only after the conflict is resolved.

No AI agent, developer, or implementation process may silently resolve
a product or specification conflict.

---

# 3. PRODUCT IDENTITY

## 3.1 Product Name

Daily Battle

## 3.2 Product Type

Daily competitive skill game.

## 3.3 Platform

Android mobile application.

## 3.4 Core Promise

> 3 challenges. ~3 minutes. One score.

## 3.5 Core Concept

Daily Battle is a finite daily competitive experience built around:

- one official daily Battle
- three short challenges
- approximately three minutes of play
- one official score
- personal performance
- friend-vs-friend rivalry
- repeat daily participation
- multiple types of skill

The product should feel like a short daily ritual rather than an
infinite game session.

---

# 4. PRODUCT EXPERIENCE

The intended experience is:

1. Open the application.
2. Understand today's Battle.
3. Start the official Battle.
4. Complete three challenges.
5. Receive one combined score.
6. Understand personal performance.
7. Compare with a rival or friends.
8. Finish the session.
9. Return for the next daily Battle.

The experience should create the following emotional sequence:

> Anticipation → Play → Focus → Tension → Score Reveal → Self Comparison
> → Friend Rivalry → Done → Come Back Tomorrow

The application should feel:

- fast
- competitive
- premium
- intelligent
- playful
- social

The experience should NOT feel:

- childish
- casino-like
- generic esports
- cyberpunk
- like a social network
- like a generic dashboard
- like an infinite feed
- like a scientific brain-training application
- excessively AI-branded

---

# 5. CORE PRODUCT PRINCIPLES

## 5.1 Daily Ritual

Daily Battle is designed around a short daily ritual.

The official Battle is finite.

The product should create anticipation for the next Battle rather than
encourage endless consumption.

---

## 5.2 One Official Battle

The official daily Battle is the central product experience.

There is one official Battle for the day.

The official Battle consists of three challenges.

The official Battle should take approximately three minutes.

The player receives one combined official score.

---

## 5.3 One Official Attempt

The official Battle is designed around one official attempt.

The application must not allow repeated official attempts to manipulate
the official result.

Practice must remain separate from the official Battle.

---

## 5.4 Same Official Battle

The official daily challenge set is the same for everyone.

Personalization may be used in practice or supporting systems where explicitly
specified, but must not silently alter the official Battle for individual
players.

---

## 5.5 Personal Competition

Competition should primarily be personal and social rather than global.

The product emphasizes:

- personal score
- personal improvement
- percentile
- friends
- rival comparison
- momentum
- personal bests

The product does not use a global leaderboard as a core system.

---

## 5.6 Friend Rivalry

Friend competition is an important part of the experience.

The user should be able to understand:

- their own score
- another person's score
- the score gap
- the opportunity to beat that rival

The Rival experience should remain focused on one person and one competitive
goal at a time.

---

## 5.7 Skill Variety

The three official challenges represent different types of skill.

Current challenge structure:

1. Snap — reaction
2. Shift — memory / precision
3. Crowd Call — social prediction

Each challenge should feel distinct.

The three challenges should not be visually or mechanically identical.

---

## 5.8 Score as the Main Reward

The primary reward system is performance.

Important rewards include:

- score
- personal improvement
- personal best
- percentile
- rivalry progress
- momentum
- streak/momentum continuity

The product does not use coins as a primary reward currency.

---

# 6. OFFICIAL BATTLE

## 6.1 Definition

The Official Battle is the central daily gameplay session.

It consists of:

- three official challenges
- approximately three minutes
- one official attempt
- one final combined score

---

## 6.2 Official Battle Flow

The intended flow is:

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
  ↓
Rival
6.3 Official Challenge Order

The current defined order is:

1. Snap
2. Shift
3. Crowd Call

Any change to this order requires an explicit product decision.

6.4 Official Score

The currently defined score model is:

Snap          300 points
Shift         300 points
Crowd Call    300 points
Consistency   100 points
-------------------------
Total        1000 points

The exact algorithms, timing formulas, weighting implementation, and
consistency calculation are NOT considered fully specified by this document.

Those details must be formally defined in:

02_GAMEPLAY.md

Until they are explicitly defined there, implementation must not invent
alternative scoring behavior.

7. CHALLENGE SYSTEM
7.1 Snap
Purpose

Fast reaction.

Defined Characteristics
reaction-focused
fast
clean
focused
minimal visual distraction
Current Score Allocation

Maximum contribution: 300 points.

Detailed Rules

See:

02_GAMEPLAY.md

7.2 Shift
Purpose

Memory and precision.

Defined Characteristics
memory-focused
grid-based
precision-oriented
visually distinct from Snap
Current Score Allocation

Maximum contribution: 300 points.

Detailed Rules

See:

02_GAMEPLAY.md

7.3 Crowd Call
Purpose

Social prediction / intuition.

Defined Characteristics
prediction-focused
social-looking
large question
clear answer choices
crowd distribution can be revealed as part of the result
Current Score Allocation

Maximum contribution: 300 points.

Detailed Rules

See:

02_GAMEPLAY.md

8. PRACTICE MODE

Practice is separate from the Official Battle.

Practice may be used for:

learning
experimentation
improving skills
adaptive practice

Practice must NOT:

modify the official daily score
replace the official attempt
manipulate the official result
be presented as the Official Battle

The official/practice distinction must be visually and behaviorally clear.

Official language may use:

TODAY
READY
OFFICIAL

Practice may use:

PRACTICE

The product must not use fake real-time multiplayer terminology such as
"LIVE" when the experience is not actually live multiplayer.

9. SCORE AND PERFORMANCE

The Results experience is a major reward moment.

The score should be visually dominant.

The Results screen should communicate performance in a clear hierarchy:

Final score
Percentile
Improvement / change
Challenge breakdown
Personal performance context
Rival comparison

The intended result experience should feel closer to a premium sports
scoreboard than an analytical dashboard.

10. PERSONAL PERFORMANCE

Daily Battle should help users understand their own performance over time.

Important concepts include:

current score
personal best
average
improvement
momentum
recent Battles
challenge-specific strengths

The Profile experience uses the concept of:

Battle DNA

Current Battle DNA dimensions include:

Speed
Memory
People

These should be treated as game performance characteristics.

The product must NOT make unsupported scientific, medical, psychological,
or intelligence claims.

The product must NOT present Battle DNA as an IQ measurement.

11. SOCIAL SYSTEM
11.1 Purpose

The social system exists primarily for competition.

It is NOT intended to become a general-purpose social network.

11.2 Friends

Friends may be used to:

compare today's scores
identify rivals
compete
add people using a Battle Code

The Friends experience should focus on competition rather than social posting.

11.3 Explicitly Excluded Social Features

The MVP does not include:

likes
followers
comments
social posts
general social feed
direct chat
social-network style profiles
11.4 Add Friend

The defined MVP friend-addition concept is:

Battle Code
Add Friend
own Battle Code
Copy

The MVP does not require:

contact permission
phone-number search
location-based friend discovery
12. RIVAL SYSTEM

The Rival experience should communicate:

One person. One gap. One goal.

The Rival screen focuses on:

current rival
rival score
user's score
score gap
action to beat the rival
limited match history

The Rival screen must NOT become:

a full friends list
a global leaderboard
an analytical statistics dashboard
a social feed
13. LEADERBOARD PRINCIPLE

Daily Battle does NOT use a global leaderboard as a core product feature.

Instead, competitive context is provided through:

percentile
friends
rival comparison
personal improvement

The product should avoid creating a generic global ranking hierarchy.

14. PRODUCT SCOPE — MVP
14.1 Core MVP Experience

The MVP core experience consists of:

onboarding
Battle name setup
Home
Today's Battle
Battle Intro
Snap
Shift
Crowd Call
Results
Rival
Friends
Add Friend
Profile / Battle DNA
History
Settings
required loading/error/empty/completion states

Settings is treated as a supporting/secondary flow rather than one of the
13 primary product screens.

14.2 Primary Screen Set

The current primary screen architecture contains:

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

Additional supporting flows/states include Settings and other transient
states.

The definitive screen/state registry belongs in:

03_SCREEN_ARCHITECTURE.md

15. EXPLICITLY OUT OF SCOPE

The following are not part of the current MVP product direction unless
explicitly added through a future approved decision.

15.1 Global Leaderboard

Do not build a global leaderboard.

15.2 Coins

Do not build a coin economy or make coins the default reward system.

15.3 Chat

Do not build general-purpose chat.

15.4 Infinite Feed

Do not build an infinite content/social feed.

15.5 Photo Proof as Main Game Mechanic

Photo proof must not become the main gameplay loop.

15.6 Generic Quiz

Daily Battle should not become a generic quiz application.

15.7 IQ Claims

Do not make IQ claims or scientific intelligence claims.

15.8 Camera-First Experience

The camera must not become the primary interaction model of the product.

15.9 Real-Time Multiplayer

The MVP does not use real-time multiplayer as the primary competition model.

Friend competition is based around asynchronous score comparison.

Any future real-time multiplayer feature requires an explicit product decision.

15.10 Excessive AI Branding

AI should operate primarily as infrastructure.

The application should not make AI the dominant visible product identity.

Avoid:

excessive "AI" labels
generic AI marketing language
AI-generated-feeling UI
unnecessary AI branding
15.11 Fake "LIVE"

Do not use "LIVE" terminology when an experience is not actually live.

Use terminology appropriate to the actual state, such as:

READY
TODAY
PRACTICE
OFFICIAL
15.12 Dashboard Clutter

Do not turn the Home screen into an analytics dashboard.

Avoid:

excessive charts
excessive stat cards
unnecessary metrics
competing primary actions
micro-stat overload
15.13 Social Network

Do not expand the Friends system into a general social network.

15.14 Excessive Gamification

Do not introduce game mechanics merely because they are common in other
mobile games.

Any new mechanic must have a documented product purpose.

16. VISUAL PRODUCT DIRECTION

Daily Battle is a premium interactive product with gaming energy.

The target balance is approximately:

75–80% premium product design
20–25% game energy

The product should feel:

premium
focused
competitive
modern
intelligent
energetic

It should NOT feel like:

generic esports
cyberpunk
casino
childish mobile game
generic AI product
generic dashboard
17. VISUAL SYSTEM — HIGH-LEVEL RULES

The application uses a dark-first visual system.

17.1 Core Background
#080B10
17.2 Surfaces
Surface 1: #11161F
Surface 2: #171D28
Elevated: #1C2330
17.3 Text
Primary:   #F5F7FA
Secondary: #A2AAB8
Muted:     #6F7887
Disabled:  #4D5563
17.4 Brand
Primary Violet: #7C5CFC
17.5 Semantic Colors
Success: #39D98A
Warning: #FFB84D
Error:   #FF5D73
Info:    #4EA8FF
18. CHALLENGE ACCENTS

Challenge colors are used to identify challenge types.

They must NOT turn the entire application into three different themes.

Snap:
#4EA8FF

Shift:
#FFB84D

Crowd Call:
#39D98A

Challenge colors should support recognition and feedback while preserving
the overall Daily Battle visual identity.

19. TYPOGRAPHY

The primary typeface is:

Inter

The defined typography system includes:

Display:
56 / 60
Weight: 700

H1:
30 / 36
Weight: 700

H2:
24 / 30
Weight: 700

H3:
18 / 24
Weight: 600

Body Large:
16 / 24
Weight: 500

Body:
14 / 20
Weight: 500

Caption:
12 / 16
Weight: 500

Label:
11–12 / 16
Weight: 600

Important UI text should not be below 12px.

Numeric scores should use tabular numerals where appropriate.

The complete typography implementation specification belongs in:

05_DESIGN_SYSTEM.md

20. LAYOUT PRINCIPLES

The master design canvas is:

390 × 844

The UI must also be validated against:

412 × 915
360 × 800

Primary horizontal screen margin:

20px

Major hero sections may use:

24px

The product follows an 8px-based spacing system.

Defined spacing values:

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

The complete layout system belongs in:

05_DESIGN_SYSTEM.md

21. COMPONENT PRINCIPLES

The UI should use a consistent component system.

Major components include:

buttons
cards
navigation
challenge components
score components
friend components
rival components
feedback components
input components
loading states
empty states
error states

Components should be reused rather than independently recreated for every
screen.

The component system must preserve:

consistent spacing
consistent typography
consistent radius
consistent interaction states
consistent accessibility behavior
22. BUTTON PRINCIPLE

Primary buttons use:

Height: 52px

Primary actions should use direct action language.

Examples:

PLAY BATTLE
BEAT RAHUL
START REMATCH
ADD FRIEND
SHARE RESULT

The exact button component specification belongs in:

05_DESIGN_SYSTEM.md

23. NAVIGATION

The primary bottom navigation consists of:

Home
Battle
Friends
Me

The Battle destination should be visually emphasized as the central product
destination without using a giant floating action button.

The bottom navigation should remain restrained.

During gameplay, the bottom navigation should be hidden.

Gameplay should provide maximum focus.

Complete navigation rules belong in:

06_NAVIGATION_AND_FLOWS.md

24. GAMEPLAY IMMERSION

During gameplay:

bottom navigation is hidden
profile controls are hidden
friend controls are hidden
history controls are hidden
social controls are hidden
gameplay becomes the dominant experience

Gameplay screen hierarchy:

Top:
Challenge / progress / timer

Middle:
Game

Bottom:
Instruction / current score

The gameplay screen must prioritize the actual challenge over decorative UI.

25. SCREEN JOB PRINCIPLE

Every screen must have one dominant job.

Home:
Play today's Battle

Snap:
React

Shift:
Remember

Crowd Call:
Predict

Results:
Understand performance

Rival:
Beat someone

Friends:
Manage rivals

Profile:
Understand yourself

History:
See progress

Settings:
Manage preferences and account

If a proposed UI element does not support the screen's dominant job, it must
be questioned before implementation.

26. HOME PRINCIPLE

The Home screen exists primarily to get the player into today's Battle.

The dominant hierarchy is:

Greeting / identity
Momentum
Today's Battle
Rival

The Today’s Battle area is the primary action.

The Home screen should NOT become:

an analytics dashboard
a social feed
a statistics wall
a leaderboard
a collection of unrelated cards

The exact Home blueprint belongs in:

04_SCREEN_BLUEPRINTS.md

27. RESULT PRINCIPLE

Results are a reward moment.

The hierarchy should prioritize:

Score
Percentile
Improvement
Challenge breakdown
Personal context
Rival comparison

The result experience should communicate achievement without overwhelming the
user with statistics.

28. MOTION PRINCIPLE

Motion exists to communicate:

state change
feedback
reward
progression

Motion must not exist only as decoration.

Defined general timing ranges include:

Normal navigation:
200–300ms

Game transitions:
250–400ms

Score reveal:
700–1000ms

Button press:
100–150ms

The complete motion specification belongs in:

10_ANIMATION_HAPTICS.md

29. SCORE REVEAL

The score reveal is an important emotional moment.

The intended sequence is:

Score:
0 → final score

Then:
Percentile

Then:
Improvement

Then:
Rival comparison

The score reveal should occur over approximately:

700–1000ms

The implementation must preserve the intended hierarchy rather than
introducing unnecessary animation.

30. HAPTICS AND SOUND

Haptics should communicate meaningful feedback.

Current intended mappings include:

Button press:
Light

Correct:
Success feedback

Incorrect:
Short error feedback

Personal best:
Stronger feedback

Battle complete:
Medium feedback

Sound effects may communicate:

tap
correct
wrong
score reveal
personal best
Battle complete

Continuous background music is not required as part of the defined product
direction.

Sound and haptic preferences must be user-controllable.

31. ACCESSIBILITY

Accessibility is a product requirement.

The application must not communicate important states using color alone.

Selected states should use additional visual indicators such as:

border
check
background
shape
typography

Touch targets must meet the defined minimum requirements.

The complete accessibility specification belongs in:

11_ACCESSIBILITY.md

32. AI PRINCIPLE

AI is infrastructure, not the primary product identity.

AI may support:

practice personalization
adaptive practice
backend intelligence
content generation or selection where explicitly approved
supporting product systems

AI must not be unnecessarily exposed as the central selling point.

Official Battle behavior must remain consistent for all players where the
product specification requires the same official challenge.

AI must never silently alter an official Battle for an individual user.

33. REAL-WORLD / WILD CARD PRINCIPLE

Real-world challenges / Wild Card concepts are not part of the current MVP
core experience.

They must not be introduced into the MVP unless explicitly approved.

34. DATA AND ACCOUNT PRINCIPLES

The product currently establishes the concept of:

Battle Name
Battle Code
Friends
Rival
Profile
History
official Battle result

However, the following are NOT fully defined by this Master Specification:

authentication mechanism
account identity model
backend provider
database schema
API contracts
synchronization architecture
account recovery
device migration
server authority
anti-cheat implementation

These must be defined before implementation in:

07_TECHNICAL_ARCHITECTURE.md

and

08_DATA_AND_API.md

No implementation may silently invent these systems.

35. OFFLINE BEHAVIOR

Offline behavior is not fully specified by this Master Specification.

The application must eventually define behavior for:

no internet
connection lost during Battle
connection lost after Battle
connection lost before result submission
retry
duplicate submission
app termination during Battle
backgrounding during Battle
returning to the application
synchronization conflicts

These requirements must be formally documented in:

09_OFFLINE_AND_ERROR_HANDLING.md

before production implementation of affected systems.

36. MONETIZATION

The Master Specification does not currently define a finalized monetization
model.

Do NOT independently introduce:

subscriptions
paid retries
coins
energy
ads
pay-to-win mechanics
cosmetic currency
Battle passes
premium challenges

unless explicitly approved and documented.

The monetization specification belongs in:

13_MONETIZATION.md

Until that document is finalized, monetization behavior is:

STATUS: PENDING
37. ANALYTICS

Analytics requirements are not fully defined by this Master Specification.

No implementation should invent an analytics taxonomy.

The event specification belongs in:

14_ANALYTICS.md

Analytics must eventually define:

event names
event properties
event timing
required events
optional events
privacy considerations
debug behavior
38. TESTING AND QA PRINCIPLE

Compilation is not equivalent to completion.

A feature is complete only when:

required UI exists
required states exist
interactions work
navigation works
back behavior works
loading behavior works
error behavior works
offline behavior works where applicable
lifecycle behavior works
repeated interaction is safe
persistence works where applicable
accessibility requirements are met
tests pass
build succeeds
no unauthorized product changes exist

Complete requirements belong in:

12_TESTING_AND_QA.md

and

16_DEFINITION_OF_DONE.md

39. ANTIGRAVITY GOVERNANCE

Antigravity is an implementation agent.

Antigravity is NOT the product owner.

The product specification determines what should be built.

Antigravity must:

follow the documentation
work in small tasks
reference requirement IDs
report changed files
report tests
report build status
report edge cases
report dependencies
report deviations
stop when requirements are ambiguous
request clarification when specifications conflict

Antigravity must NOT:

invent product requirements
invent gameplay rules
invent scoring
invent screens
redesign the product independently
introduce unauthorized dependencies
silently change UX
silently change architecture
silently change navigation
silently expand scope
declare a task complete without verification

The detailed agent governance rules belong in:

15_ANTIGRAVITY_RULES.md

40. IMPLEMENTATION TASK PRINCIPLE

Daily Battle must be implemented in controlled, independently verifiable
tasks.

The application must NOT be given to an AI agent with a vague instruction
such as:

"Build the entire app."

Each implementation task must define:

Task ID
Objective
Authoritative documents
Requirement IDs
Scope
Non-goals
Files/components affected
Required states
Interactions
Edge cases
Acceptance criteria
Verification requirements

After implementation, the agent must provide an implementation report.

The next task is not automatically authorized by completion of the current
task.

41. CHANGE CONTROL

Any change to the product specification must be explicit.

A change may include:

new feature
removed feature
changed gameplay
changed scoring
changed navigation
changed screen
changed design token
changed architecture
changed API
changed data model
changed monetization
changed analytics
changed platform behavior

Changes must be recorded in:

/decisions/

Each decision should contain:

Decision ID
Date
Status
Problem
Current requirement
Options considered
Approved decision
Reason
Affected documents
Implementation impact

Historical decisions must not be silently erased.

If a decision supersedes an earlier decision, the earlier decision should
remain in the record with status:

SUPERSEDED
42. REQUIREMENT IDENTIFICATION

All meaningful requirements should use stable IDs.

Recommended format:

REQ-PROD-###
REQ-GAME-###
REQ-SCREEN-###
REQ-UI-###
REQ-NAV-###
REQ-TECH-###
REQ-DATA-###
REQ-OFFLINE-###
REQ-MOTION-###
REQ-A11Y-###
REQ-QA-###
REQ-MON-###
REQ-ANALYTICS-###
REQ-AG-###

Examples:

REQ-PROD-001
REQ-GAME-001
REQ-SCREEN-001
REQ-UI-001
REQ-NAV-001
REQ-TECH-001

Requirement IDs must remain stable after creation.

Do not reuse an old ID for a different requirement.

43. REQUIREMENT STATUS

Requirements may use the following status values:

DEFINED
PENDING
DECISION_REQUIRED
APPROVED
IMPLEMENTED
VERIFIED
SUPERSEDED

Definitions:

DEFINED

The requirement is already established by the product specification.

PENDING

The area is known but the exact behavior has not yet been defined.

DECISION_REQUIRED

Multiple legitimate choices exist and explicit product/technical approval
is required.

APPROVED

A decision has been explicitly made and recorded.

IMPLEMENTED

The requirement has been implemented.

VERIFIED

The implementation has been tested/audited and accepted.

SUPERSEDED

The requirement was replaced by a newer approved decision.

44. KNOWN OPEN DECISIONS

The following areas must not be silently invented during implementation.

Product
exact MVP account model
exact Battle Code behavior
exact friend relationship lifecycle
exact rival assignment logic
exact streak/momentum calculation
Gameplay
exact Snap algorithm
exact Snap timing/scoring formula
exact Shift algorithm
exact Shift scoring formula
exact Crowd Call algorithm
exact Crowd Call scoring formula
exact Consistency calculation
exact difficulty progression
exact challenge content generation
exact challenge repetition rules
Technical
Android architecture
package/module structure
dependency policy
persistence technology
backend technology
authentication
networking architecture
synchronization strategy
server authority
anti-cheat strategy
environment configuration
Data
complete domain models
database schema
API endpoints
request/response contracts
Battle result submission model
friend synchronization model
profile/history model
Offline
exact offline support
local Battle state persistence
result queueing
conflict handling
retry behavior
Monetization
monetization model
ads
subscriptions
premium features
purchases
Analytics
analytics provider
event taxonomy
event properties
privacy requirements

Each area must be resolved in its appropriate document before the affected
system is considered production-ready.

45. DOCUMENTATION MAP

The Daily Battle specification is divided into the following documents.

01_PRODUCT.md

Defines:

product vision
product problem
user experience
product principles
core loop
MVP scope
social model
competition model
product boundaries
02_GAMEPLAY.md

Defines:

official Battle rules
Snap
Shift
Crowd Call
scoring
timing
challenge states
difficulty
attempts
practice
completion
gameplay edge cases
03_SCREEN_ARCHITECTURE.md

Defines:

complete screen registry
screen IDs
screen purpose
screen types
screen states
entry points
exit points
relationships
secondary states
transient states
04_SCREEN_BLUEPRINTS.md

Defines:

exact screen layouts
components
spacing
typography
colors
states
interactions
animations
haptics
accessibility
edge cases
acceptance criteria
05_DESIGN_SYSTEM.md

Defines:

colors
typography
spacing
radius
sizing
buttons
inputs
cards
navigation
icons
game components
feedback components
component states
06_NAVIGATION_AND_FLOWS.md

Defines:

navigation graph
primary flow
onboarding flow
Battle flow
result flow
rivalry flow
friend flow
settings flow
back behavior
deep links where applicable
07_TECHNICAL_ARCHITECTURE.md

Defines:

Android architecture
modules
layers
packages
dependencies
state management
networking
persistence
backend integration
security
configuration
environments
08_DATA_AND_API.md

Defines:

domain models
local models
remote models
database schema
API contracts
request/response models
synchronization
validation
error contracts
09_OFFLINE_AND_ERROR_HANDLING.md

Defines:

offline behavior
connection failure
retry
persistence
synchronization
duplicate requests
lifecycle interruption
loading states
empty states
error states
10_ANIMATION_HAPTICS.md

Defines:

transitions
timing
score reveal
challenge feedback
haptics
sound
reduced-motion behavior
11_ACCESSIBILITY.md

Defines:

touch targets
semantics
content descriptions
contrast
text scaling
color independence
screen reader behavior
motion accessibility
12_TESTING_AND_QA.md

Defines:

unit testing
integration testing
UI testing
gameplay testing
navigation testing
lifecycle testing
offline testing
device testing
regression testing
manual QA
13_MONETIZATION.md

Defines monetization only after explicit product approval.

14_ANALYTICS.md

Defines analytics only after explicit approval.

15_ANTIGRAVITY_RULES.md

Defines the rules governing AI-assisted implementation.

16_DEFINITION_OF_DONE.md

Defines the conditions required for a task to be considered complete.

46. DESIGN CONSTITUTION

Every Daily Battle screen should be evaluated against the following questions:

Does this screen have one dominant job?
Is the primary action obvious?
Is the information hierarchy clear?
Does the screen feel like Daily Battle rather than a generic dark app?
Are cards being used because they improve hierarchy rather than because
cards are convenient?
Can the interface be understood without tiny text?
Does the screen strengthen the three-minute daily ritual?
Does the visual design preserve the premium product direction?
Does the screen avoid unnecessary dashboard behavior?
Does the screen preserve the competitive/social identity without becoming
a social network?

If a proposed implementation fails these questions, it must be reviewed
before acceptance.

47. PRODUCT CONSTITUTION

The following rules are considered foundational.

Daily Battle is finite.

Daily Battle is daily.

Daily Battle has three official challenges.

Daily Battle produces one official score.

The official Battle is approximately three minutes.

The official Battle has one official attempt.

The official Battle is the same for everyone.

Practice is separate.

Practice does not alter the official result.

Personal performance matters.

Friend rivalry matters.

Global leaderboard competition is not a core system.

The product is not a social network.

The product is not a generic quiz.

The product is not an IQ test.

The product is not a camera-first experience.

The product is not real-time multiplayer.

The product does not use coins as its core reward system.

The product does not use an infinite feed.

AI is infrastructure rather than the primary identity.

The user should understand what to do without excessive explanation.

Gameplay should receive the user's attention.

Results should feel rewarding.

Rivalry should feel personal.

The user should finish the session and have a reason to return tomorrow.
48. FINAL MVP EXPERIENCE

The intended MVP experience can be summarized as:

INSTALL
   ↓
WELCOME
   ↓
BATTLE NAME
   ↓
HOME
   ↓
TODAY'S BATTLE
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
   ↓
FRIENDS / PROFILE / HISTORY
   ↓
RETURN TOMORROW

The central product ritual is:

OPEN
  ↓
PLAY
  ↓
SCORE
  ↓
COMPARE
  ↓
RETURN
49. IMPLEMENTATION READINESS RULE

The Android implementation must NOT begin simply because the folder exists.

Before the Android project is created, the following documentation areas
must be sufficiently defined:

[ ] 00_MASTER_SPEC.md
[ ] 01_PRODUCT.md
[ ] 02_GAMEPLAY.md
[ ] 03_SCREEN_ARCHITECTURE.md
[ ] 04_SCREEN_BLUEPRINTS.md
[ ] 05_DESIGN_SYSTEM.md
[ ] 06_NAVIGATION_AND_FLOWS.md
[ ] 07_TECHNICAL_ARCHITECTURE.md
[ ] 08_DATA_AND_API.md
[ ] 09_OFFLINE_AND_ERROR_HANDLING.md
[ ] 10_ANIMATION_HAPTICS.md
[ ] 11_ACCESSIBILITY.md
[ ] 12_TESTING_AND_QA.md
[ ] 13_MONETIZATION.md
[ ] 14_ANALYTICS.md
[ ] 15_ANTIGRAVITY_RULES.md
[ ] 16_DEFINITION_OF_DONE.md

[ ] Open decisions reviewed
[ ] Specification conflicts resolved
[ ] Product scope frozen
[ ] Gameplay rules frozen
[ ] Screen architecture frozen
[ ] Design system frozen
[ ] Navigation frozen
[ ] Technical architecture approved
[ ] Data/API contract approved
[ ] QA strategy approved
[ ] Antigravity governance approved

The Android project should be created only after this readiness gate is
passed.

50. MASTER SPECIFICATION STATUS

Current status:

PRODUCT CONCEPT:
DEFINED

CORE PROMISE:
DEFINED

CORE LOOP:
DEFINED

VISUAL DIRECTION:
DEFINED

PRIMARY SCREEN ARCHITECTURE:
DEFINED

OFFICIAL/PRACTICE DISTINCTION:
DEFINED

SOCIAL DIRECTION:
DEFINED

OUT-OF-SCOPE FEATURES:
DEFINED

DETAILED GAMEPLAY ALGORITHMS:
PENDING

DETAILED TECHNICAL ARCHITECTURE:
PENDING

DATA/API CONTRACTS:
PENDING

OFFLINE/SYNC BEHAVIOR:
PENDING

MONETIZATION:
PENDING

ANALYTICS:
PENDING

ANTIGRAVITY GOVERNANCE:
TO BE FORMALIZED IN 15_ANTIGRAVITY_RULES.md

DEFINITION OF DONE:
TO BE FORMALIZED IN 16_DEFINITION_OF_DONE.md
51. MASTER RULE

When in doubt:

Do not invent.

If something is already defined:

Follow the specification.

If something is unclear:

Mark it PENDING.

If two requirements conflict:

STOP and record the conflict.

If implementation requires a deviation:

Report the deviation before implementing it.

If a new feature is proposed:

Treat it as a product decision, not an implementation detail.

If a task is technically complete but has not been verified:

It is not DONE.

Daily Battle should be built from explicit decisions, traceable requirements,
controlled implementation tasks, and verification — not from AI assumptions.


### One important note before you paste it

I intentionally left several things as **PENDING** rather than making them up. That is important because this Master Spec should become the foundation for everything we give Antigravity.

In particular, we still need to formally define the **actual Snap, Shift, and Crowd Call algorithms and scoring formulas** in `02_GAMEPLAY.md`. The existing specification establishes the three challenges and the `300 + 300 + 300 + 100 = 1000` scoring structure, but it does not fully specify the underlying algorithms. :contentReference[oaicite:1]{index=1}

Once you paste this into `00_MASTER_SPEC.md`, **don't start `01_PRODUCT.md` yet**. First tell me when it is pasted, and we'll do a quick **Master Spec audit** against the original specification before moving to `01_PROD