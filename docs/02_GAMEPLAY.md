# DAILY BATTLE — GAMEPLAY SPECIFICATION

**Document:** 02_GAMEPLAY.md  
**Product:** Daily Battle  
**Platform:** Android  
**Document Status:** GAMEPLAY SPECIFICATION  
**Specification Version:** 1.0  
**Last Updated:** 2026-09-30  
**Parent Documents:**
- `00_MASTER_SPEC.md`
- `01_PRODUCT.md`

---

# 1. PURPOSE

This document defines the gameplay rules and gameplay-state requirements
for Daily Battle.

It defines:

- the Official Battle
- the three official challenges
- challenge order
- score structure
- official attempt rules
- Practice separation
- challenge state model
- gameplay progression
- challenge completion
- transition behavior
- gameplay interruption requirements
- gameplay edge cases
- gameplay requirements that are still pending

This document is the authority for gameplay behavior.

UI layout belongs in:

`04_SCREEN_BLUEPRINTS.md`

Visual tokens belong in:

`05_DESIGN_SYSTEM.md`

Technical implementation belongs in:

`07_TECHNICAL_ARCHITECTURE.md`

---

# 2. GAMEPLAY DESIGN PHILOSOPHY

Daily Battle is designed around:

> 3 challenges. ~3 minutes. One score.

The gameplay experience should be:

- short
- focused
- competitive
- understandable
- responsive
- skill-based
- varied
- repeatable
- satisfying

The player should not feel that they are entering a long game session.

The Official Battle should feel like one cohesive daily event containing
three different skill challenges.

---

# 3. OFFICIAL BATTLE DEFINITION

The Official Battle is the primary gameplay experience.

It consists of:

```text
3 official challenges
+
1 official attempt
+
1 combined score

The target experience is approximately:

~3 minutes

The Official Battle is finite.

Once the player completes the Official Battle for the day, the official
attempt is complete.

The player must not be presented with another button that suggests they can
submit another official attempt for the same daily Battle.

4. OFFICIAL BATTLE RULES

The following rules are foundational.

Rule 1 — One Official Battle

There is one Official Battle for the day.

Rule 2 — Three Challenges

The Official Battle contains exactly three challenges:

1. Snap
2. Shift
3. Crowd Call
Rule 3 — One Official Attempt

The player receives one official attempt.

The player cannot restart the Official Battle to replace the official result.

Rule 4 — Same Official Battle

The official challenge set must be the same for everyone.

The official Battle must not be individually personalized in a way that
changes the gameplay challenge for one player versus another.

Rule 5 — One Combined Score

The three challenge results contribute to one combined Official Battle score.

The current maximum score is:

1000
Rule 6 — Practice Is Separate

Practice is not part of the Official Battle.

Practice results must not replace or modify the official daily result.

Rule 7 — Official Completion Is Final

Once the official Battle is successfully completed and submitted, that
official result is final for the day.

5. OFFICIAL BATTLE FLOW

The primary gameplay flow is:

HOME
  ↓
BATTLE INTRO
  ↓
START BATTLE
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

The player should move continuously through the three challenges.

There should not be unnecessary manual continuation screens between
challenges.

6. BATTLE INTRO

The Battle Intro exists immediately before gameplay.

Its purpose is to prepare the player without revealing unnecessary detail.

The Battle Intro communicates:

TODAY'S BATTLE

3 challenges
~3 minutes

OFFICIAL ATTEMPT

[ START BATTLE ]

The Battle Intro should NOT:

reveal the exact challenge types
reveal detailed scoring formulas
show unnecessary statistics
contain social distractions
contain excessive instructions

The player should understand:

This is today's official Battle and I have one attempt.

7. CHALLENGE ORDER

The official challenge order is:

1. SNAP
2. SHIFT
3. CROWD CALL

This order is part of the current product specification.

Any change requires an explicit product decision.

8. CHALLENGE 1 — SNAP
8.1 Identity

Name:

Snap

Primary skill:

Reaction

Visual personality:

Fast + Clean + Focused

Accent:

#4EA8FF
8.2 Gameplay Purpose

Snap tests fast reaction.

The player's primary task is to respond correctly to a visual game event.

The experience should prioritize:

reaction speed
accuracy
focus
rapid feedback
8.3 Gameplay Presentation

The gameplay screen should contain approximately:

Top:
Challenge + progress + timer

Middle:
Game area

Bottom:
Instruction + current score

The game area should occupy approximately:

60–65% of the meaningful viewport

The game area should contain only elements that are relevant to gameplay.

Every visible object should serve one of these roles:

target
distractor
game state

Avoid decorative objects that compete with the target.

8.4 Example Interaction

The existing product specification provides the following conceptual
interaction:

SNAP

1 / 3

        GAME AREA

           BLUE TARGET

TAP BLUE

Score 240 / 300

This example describes the intended interaction direction.

It does NOT define the final algorithm.

8.5 Feedback

Correct interaction:

Small pulse

Incorrect interaction:

Small shake

Do not use a large disruptive error overlay such as:

WRONG!!!

The feedback must preserve gameplay speed.

8.6 Snap Completion

After Snap is completed, a short completion state may communicate:

SNAP COMPLETE

287 / 300

The existing specification indicates approximately:

~700 ms

before transitioning onward.

There should be no unnecessary manual Continue action.

The Battle should flow into Shift automatically.

8.7 Snap Score

Maximum contribution:

300 points

The exact scoring algorithm is:

STATUS: PENDING

The following are NOT yet formally defined:

number of rounds
exact reaction window
target appearance timing
distractor behavior
reaction-time formula
accuracy weighting
minimum score
maximum score calculation
penalties
late input handling
early input handling
missed target handling
tie behavior
randomization
difficulty progression

These must be explicitly defined before final implementation.

9. CHALLENGE 2 — SHIFT
9.1 Identity

Name:

Shift

Primary skills:

Memory + Precision

Visual personality:

Precise

Accent:

#FFB84D
9.2 Gameplay Purpose

Shift is intended to provide a deliberate contrast to Snap.

Snap emphasizes:

Reaction

Shift emphasizes:

Memory and precision

The gameplay should therefore feel more controlled and deliberate.

9.3 Core Interaction

The current product specification defines a grid-based concept.

Conceptual structure:

SHIFT

2 / 3

┌───┬───┬───┐
│   │   │   │
├───┼───┼───┤
│   │   │   │
├───┼───┼───┤
│   │   │   │
└───┴───┴───┘

WHAT MOVED?

[A] [B]
[C] [D]

The grid should dominate the gameplay experience.

9.4 Gameplay Purpose

The player should observe a spatial configuration and then identify a
change.

The exact sequence of:

observe
↓
change
↓
question
↓
answer

must be preserved unless an explicit gameplay decision changes it.

9.5 Shift Score

Maximum contribution:

300 points

The exact scoring algorithm is:

STATUS: PENDING

The following are not yet fully defined:

grid dimensions
number of rounds
observation duration
change duration
number of changed cells
answer format
number of answer choices
time limit
partial scoring
incorrect-answer penalty
difficulty progression
randomization
repeated pattern prevention

These must be explicitly defined before implementation.

10. CHALLENGE 3 — CROWD CALL
10.1 Identity

Name:

Crowd Call

Primary skill:

Social prediction / intuition

Visual personality:

Expressive + Social

Accent:

#39D98A
10.2 Gameplay Purpose

Crowd Call asks the player to predict what most people would choose.

The player is not necessarily selecting their own preferred answer.

The intended gameplay question is:

What would most people choose?

10.3 Core Interaction

The current specification provides this conceptual example:

CROWD CALL

WHAT WOULD MOST PEOPLE CHOOSE?

You suddenly get ₹500 tonight.

[ Movie ]

[ Food + Hangout ]

[ Gaming ]

[ Save it ]

The final content is not fixed by this example.

The example communicates the intended gameplay structure:

Question
+
Multiple choices
+
Prediction of majority choice
10.4 Crowd Result

After the player answers, the experience may reveal the crowd distribution.

Conceptual example:

THE CROWD SAID

Movie             19%
Food + Hangout    36%
Gaming            27%
Save              18%

YOU PREDICTED IT ✓

+120

The reveal is intended to be satisfying and social.

10.5 Crowd Call Score

Maximum contribution:

300 points

The exact scoring algorithm is:

STATUS: PENDING

The following are not yet fully defined:

number of questions
number of choices
source of crowd data
minimum sample size
crowd-data collection method
whether the crowd is historical, live, simulated, or predetermined
prediction scoring
time limit
partial scoring
tie handling
content generation
content validation
regional/cultural variation
answer ordering
repeat prevention

These must be explicitly defined before implementation.

11. CONSISTENCY SCORE

The current Official Battle score model includes:

Snap          300
Shift         300
Crowd Call    300
Consistency   100
----------------
Total        1000

The Consistency component is therefore worth:

100 points

However, the existing product specification does not fully define what
"Consistency" mathematically means.

Possible interpretations must NOT be selected by the implementation agent.

Status:

DECISION_REQUIRED

The final definition must specify:

input values
calculation
normalization
minimum
maximum
rounding
edge cases
whether consistency is calculated across the three challenges
whether it considers accuracy
whether it considers variance
whether it considers timing
whether it considers performance balance

Until defined, Antigravity must not invent a Consistency formula.

12. TOTAL SCORE

The official score is currently defined as:

Snap          0–300
Shift         0–300
Crowd Call    0–300
Consistency   0–100
--------------------
Total         0–1000

Therefore:

Minimum:
0

Maximum:
1000

The exact aggregation formula is conceptually:

Total =
Snap Score
+ Shift Score
+ Crowd Call Score
+ Consistency Score

The component maximums are defined.

The exact algorithms producing each component score remain pending.

13. SCORE INTEGRITY

The client must not allow the player to arbitrarily modify the final score.

Gameplay scores must be generated from gameplay events.

The implementation must eventually distinguish:

raw gameplay events
↓
challenge result
↓
challenge score
↓
Battle score
↓
official result

The authoritative source of final scoring is a technical/data decision that
must be finalized in:

07_TECHNICAL_ARCHITECTURE.md

and

08_DATA_AND_API.md

before production backend implementation.

14. OFFICIAL ATTEMPT STATE

The Official Battle should have a clear state model.

Minimum conceptual states:

NOT_STARTED
IN_PROGRESS
COMPLETED

The detailed application state model may also require:

LOADING
PAUSED
INTERRUPTED
SUBMITTING
SUBMITTED
FAILED

The final technical state machine must be defined before implementation.

15. OFFICIAL BATTLE STATE MACHINE

Conceptual flow:

NOT_STARTED
    │
    │ Start Battle
    ▼
IN_PROGRESS
    │
    ├── Snap
    │
    ├── Shift
    │
    └── Crowd Call
    │
    ▼
COMPLETED
    │
    ▼
RESULT SUBMISSION
    │
    ▼
OFFICIAL RESULT

The user should not be able to transition from:

COMPLETED

back to:

IN_PROGRESS

for another official submission on the same daily Battle.

16. CHALLENGE STATE MODEL

Each challenge should support the following conceptual states:

READY
ACTIVE
CORRECT
INCORRECT
COMPLETE

These states are defined at the product level.

Implementation may require additional internal states, but they must not
change the visible gameplay contract.

17. READY STATE

Purpose:

Prepare the player to interact.

The screen should communicate:

challenge identity
progress
required instruction
readiness

The Ready state should be brief.

18. ACTIVE STATE

The player is currently interacting with the challenge.

During Active:

gameplay input is enabled
irrelevant navigation is disabled
challenge state is authoritative
duplicate input must be handled safely
19. CORRECT STATE

Correct interaction should provide immediate positive feedback.

Feedback may include:

visual pulse
haptic feedback
sound

The feedback should remain concise.

20. INCORRECT STATE

Incorrect interaction should provide clear feedback without disrupting the
overall Battle.

Feedback may include:

subtle shake
error haptic
error sound

Avoid excessive failure animation.

21. COMPLETE STATE

A challenge enters Complete after its required gameplay has ended.

The Complete state may briefly show:

Challenge result

Then the Battle proceeds automatically to the next challenge where the
product flow requires automatic continuation.

22. BETWEEN-CHALLENGE TRANSITIONS

The Battle should feel continuous.

Example:

SNAP COMPLETE
287 / 300

        ↓

SHIFT

There should not be a large navigation interruption between challenges.

The player should not need to manually tap:

CONTINUE

between the official challenges unless a future product decision explicitly
changes this behavior.

23. BATTLE PROGRESS

The Battle contains three challenges.

Progress should communicate the player's position within the Battle.

Conceptual representation:

● ○ ○

Then:

● ● ○

Then:

● ● ●

The exact visual component belongs in:

05_DESIGN_SYSTEM.md

and

04_SCREEN_BLUEPRINTS.md

24. TIMER

The gameplay specification references a timer/progress area.

The timer exists to support the short, focused gameplay experience.

However, the exact timing rules for each challenge are not fully defined
by the current specification.

Status:

DECISION_REQUIRED

The following must be explicitly defined:

whether every challenge has a timer
whether timer is countdown or elapsed time
challenge-specific limits
timer precision
timeout behavior
whether time affects score
whether timeout counts as incorrect
whether timer can be paused
lifecycle behavior during timer operation

No implementation agent may invent these rules.

25. INPUT MODEL

Gameplay input must be:

immediate
deterministic
resistant to accidental duplicate input
visually understandable
accessible where possible

The exact input type varies by challenge.

Current conceptual inputs:

Snap:
Tap

Shift:
Grid / answer selection

Crowd Call:
Answer selection

The final input specifications belong in the individual challenge definitions
and screen blueprints.

26. DUPLICATE INPUT

The game must safely handle:

rapid repeated taps
multiple taps in the same frame/window
taps after the challenge has completed
taps during transition
taps on disabled answers

A completed challenge must ignore gameplay input that could modify the
completed result.

27. BACK BUTTON DURING GAMEPLAY

The Android system Back action requires explicit gameplay handling.

The player must not accidentally lose an Official Battle merely by pressing
Back without a defined behavior.

Current behavior:

STATUS: DECISION_REQUIRED

The final behavior must define whether Back:

confirms exit
pauses
minimizes
returns to Home
resumes the active Battle
discards progress

The behavior must preserve Official Battle integrity.

28. APP BACKGROUNDING

If the application is backgrounded during gameplay, the active Battle state
must be handled deterministically.

Possible causes include:

Home gesture
app switcher
incoming notification
system interruption
screen lock
permission dialog
OS process termination

Exact persistence and resume rules are:

STATUS: PENDING

They must be defined in:

09_OFFLINE_AND_ERROR_HANDLING.md

and

07_TECHNICAL_ARCHITECTURE.md

29. APP TERMINATION DURING BATTLE

If the application is terminated while an Official Battle is in progress,
the product must not create an undefined state.

The final behavior must define whether the Battle can:

resume
restart without score submission
continue from a persisted challenge state
become invalid

Status:

DECISION_REQUIRED

No automatic assumption should be made.

30. NETWORK INTERRUPTION DURING GAMEPLAY

Gameplay should not depend unnecessarily on a continuous network connection
unless a specific challenge requires authoritative remote data.

The exact offline/network behavior is not yet defined.

Status:

PENDING

See:

09_OFFLINE_AND_ERROR_HANDLING.md

31. OFFICIAL RESULT SUBMISSION

After all three challenges are completed:

Snap
+
Shift
+
Crowd Call
+
Consistency
=
Final Battle Score

The result then enters the official result flow.

The system must prevent duplicate official result submissions.

Exact backend submission and idempotency behavior belongs in:

08_DATA_AND_API.md

32. DUPLICATE RESULT SUBMISSION

A single Official Battle must produce one official result.

If the client retries a submission because of:

timeout
connection loss
app restart
duplicate user action

the backend/data layer must not create multiple official results for the
same Battle attempt.

Exact implementation:

PENDING
33. OFFICIAL RESULT LOCK

After successful official submission:

Official Battle:
COMPLETED

Official Score:
LOCKED

The player may still:

view the result
view history
view rival
view friends
practice separately

The player may not replace the official result by replaying the official
Battle.

34. PRACTICE MODE

Practice is a separate gameplay mode.

Purpose:

improve skills
learn mechanics
experiment
repeat challenges
potentially adapt difficulty

Practice is NOT:

an additional official attempt
a way to change today's official score
a replacement for today's Battle
part of the official Battle result
35. PRACTICE SCORE

Practice scores must be explicitly labeled as:

PRACTICE

Practice scores must not be presented as official scores.

Practice must not modify:

Official Battle score
official percentile
official result
official completion state

unless a future product decision explicitly changes the model.

36. PRACTICE PERSONALIZATION

The product direction allows practice to become adaptive.

Conceptually:

Official:
Same Battle for everyone

Practice:
Potentially adaptive

AI may eventually personalize Practice based on player history and
performance.

However, the exact personalization system is:

STATUS: PENDING

No implementation should introduce adaptive Practice until the relevant
rules are specified.

37. OFFICIAL CONTENT CONSISTENCY

Official Battle content must be deterministic at the product level.

All players should receive the same official challenge set for the relevant
daily Battle.

This means the system must eventually define:

Battle identity
daily seed/version
challenge configuration
challenge content
ordering
scoring configuration

The technical mechanism is:

PENDING
38. CONTENT RANDOMIZATION

Randomization may exist inside a challenge where explicitly specified.

However, randomization must not cause different players to receive
meaningfully different Official Battle challenges when the product rule
requires the same official Battle.

Any randomization system must therefore distinguish between:

Official deterministic content

and:

Practice/adaptive content

Exact randomization rules are:

PENDING
39. REPEAT PREVENTION

The system should avoid unintended repetition where the product requires
variety.

This applies to:

challenge content
question content
grid patterns
target sequences
Crowd Call questions

Exact repetition rules are:

PENDING

They must be defined before a content-generation system is considered
complete.

40. CHALLENGE DIFFICULTY

The product contains multiple challenge types.

The exact difficulty system is not currently defined.

Possible future concepts may include:

fixed daily difficulty
deterministic progression
practice-only adaptive difficulty

However, no difficulty model should be selected automatically.

Status:

DECISION_REQUIRED
41. DIFFICULTY RULE FOR OFFICIAL BATTLE

If difficulty changes over time, it must do so consistently for the relevant
Official Battle population.

A player must not receive a secretly easier or harder Official Battle
because of personal AI adaptation unless the product explicitly changes this
rule.

42. CHALLENGE CONTENT VALIDATION

Every official challenge must be valid before being presented to the user.

Validation should eventually cover:

playable state
valid answer
valid score range
valid timing
no impossible state
no missing assets
no ambiguous answer
no duplicate official content where prohibited

Exact validation implementation belongs in the technical/content system.

43. SNAP — OPEN QUESTIONS

The following are intentionally unresolved:

[ ] Number of Snap rounds
[ ] Target types
[ ] Distractor types
[ ] Reaction window
[ ] Round timing
[ ] Total challenge duration
[ ] Accuracy model
[ ] Reaction-time scoring
[ ] Penalty model
[ ] Maximum achievable score
[ ] Minimum achievable score
[ ] Difficulty model
[ ] Randomization model
[ ] Repeat prevention
[ ] Timeout behavior
[ ] Early tap behavior
[ ] Miss behavior

Status:

DECISION_REQUIRED
44. SHIFT — OPEN QUESTIONS
[ ] Grid size
[ ] Number of rounds
[ ] Observation duration
[ ] Transition duration
[ ] Number of changes
[ ] Type of changes
[ ] Answer format
[ ] Number of answer options
[ ] Time limit
[ ] Scoring formula
[ ] Incorrect answer behavior
[ ] Partial scoring
[ ] Difficulty progression
[ ] Randomization
[ ] Repeat prevention
[ ] Timeout behavior

Status:

DECISION_REQUIRED
45. CROWD CALL — OPEN QUESTIONS
[ ] Number of questions
[ ] Number of answer options
[ ] Question categories
[ ] Crowd data source
[ ] Crowd sample size
[ ] Crowd collection timing
[ ] Historical vs live vs simulated crowd
[ ] Majority calculation
[ ] Prediction scoring
[ ] Time limit
[ ] Tie behavior
[ ] Content generation
[ ] Content moderation
[ ] Cultural/regional handling
[ ] Repeat prevention

Status:

DECISION_REQUIRED
46. CONSISTENCY — OPEN QUESTIONS
[ ] Definition of consistency
[ ] Inputs used
[ ] Formula
[ ] Weight
[ ] Normalization
[ ] Minimum score
[ ] Maximum score
[ ] Rounding
[ ] Edge cases

Status:

DECISION_REQUIRED
47. SCORE EDGE CASES

The final scoring system must define behavior for:

No answer
Wrong answer
Late answer
Early input
Multiple input
Timeout
App backgrounded
App terminated
Network lost
Submission failed
Duplicate submission
Invalid challenge state
Invalid challenge content

Until defined, these remain:

PENDING
48. SCORE BOUNDARIES

Every challenge score must remain within its defined range.

Snap:
0–300

Shift:
0–300

Crowd Call:
0–300

Consistency:
0–100

Total:
0–1000

The implementation must prevent:

negative score
score above component maximum
score overflow
duplicate score addition
score mutation after completion
49. SCORE PRECISION

The final player-facing score should be an integer.

Current score examples use whole points.

Exact internal precision, if any, is:

PENDING

The final displayed score must not expose unnecessary internal calculation
precision.

50. SCORE BREAKDOWN

The Results experience should provide the component breakdown:

Snap
Shift
Crowd Call

The Consistency contribution may also be represented where appropriate.

The visual presentation belongs in:

04_SCREEN_BLUEPRINTS.md

51. CHALLENGE RESULT CONTRACT

Each challenge should ultimately produce a structured result containing
conceptually:

Challenge ID
Challenge Type
Attempt ID
Completion State
Raw Performance Data
Score
Maximum Score
Duration
Result Timestamp

The exact data model belongs in:

08_DATA_AND_API.md

This document defines the gameplay meaning, not the final code/data schema.

52. BATTLE RESULT CONTRACT

The completed Official Battle should conceptually produce:

Battle ID
Daily Battle ID
Official Attempt ID

Snap Result
Shift Result
Crowd Call Result

Consistency Score

Total Score
Maximum Score

Completion State
Completion Timestamp

The exact backend/local representation is defined later.

53. BATTLE COMPLETION

A Battle is complete only when all required official challenges have been
completed and the final result has been generated.

Conceptual state:

Snap COMPLETE
      ↓
Shift COMPLETE
      ↓
Crowd Call COMPLETE
      ↓
Consistency calculated
      ↓
Total calculated
      ↓
Battle COMPLETE
54. BATTLE COMPLETION FEEDBACK

The Battle completion should provide a clear completion signal.

The intended experience is:

Challenge completion
↓
Final score reveal
↓
Performance context
↓
Rival comparison
↓
Finished for today

The player should feel that the daily task is complete.

55. NO REPLAY LOOP FOR OFFICIAL BATTLE

After completion:

PLAY BATTLE

must not remain available as though the player can submit another official
attempt.

The completed Home state should communicate:

Today's Battle ✓

[Score]

NEXT BATTLE
TOMORROW

The official Battle is finished.

56. DAILY RESET

A new Official Battle becomes available on the next defined daily cycle.

The exact reset timezone and server-side definition are:

DECISION_REQUIRED

This must be defined in the technical/data specifications.

The client must not independently invent the authoritative daily reset if
the backend is the source of truth.

57. TIMEZONE

Daily Battle is a daily product, therefore timezone handling must eventually
be explicitly defined.

Questions requiring a decision:

[ ] Which timezone determines a user's daily Battle?
[ ] Device timezone or server timezone?
[ ] What happens when timezone changes?
[ ] What happens while traveling?
[ ] Can a user receive two Battles in one calendar day?
[ ] What happens around daylight-saving transitions?

Status:

DECISION_REQUIRED
58. BATTLE VERSIONING

Each Official Battle should eventually have an identifiable version/configuration.

This is necessary to ensure that:

all players receive the intended challenge
results can be reproduced/validated
scoring configuration is known
historical Battles remain understandable

Exact implementation:

PENDING
59. ANTI-CHEAT PRINCIPLE

The gameplay system should protect the integrity of Official Battle scores.

Potential concerns include:

manipulated timers
impossible reaction times
repeated submission
modified local score
replayed attempts
modified challenge state

The detailed anti-cheat strategy belongs in:

07_TECHNICAL_ARCHITECTURE.md

and

08_DATA_AND_API.md

This document only establishes the requirement that Official Battle results
must remain trustworthy.

60. GAMEPLAY SECURITY PRINCIPLE

The client should not be treated as automatically trustworthy for
authoritative scoring decisions.

The exact division between:

Client
Server

is a technical decision.

Status:

PENDING
61. GAMEPLAY ACCESSIBILITY

Gameplay must remain usable without relying solely on:

color

Important states should have additional visual or interaction signals.

Examples:

Correct:
Visual feedback + optional haptic/sound

Incorrect:
Visual feedback + optional haptic/sound

Selected:
Border/background/check/etc.

The full accessibility specification belongs in:

11_ACCESSIBILITY.md

62. GAMEPLAY FEEDBACK

Current intended feedback:

Button:
Light haptic

Correct:
Light success haptic

Incorrect:
Short error haptic

Personal best:
Stronger success feedback

Battle complete:
Medium confirmation

Sound may include:

Tap
Correct
Wrong
Score reveal
Personal best
Battle complete

The detailed timing and implementation belongs in:

10_ANIMATION_HAPTICS.md

63. GAMEPLAY MOTION

Gameplay transitions should be fast enough to preserve momentum.

Current product guidance:

Normal navigation:
200–300 ms

Game transitions:
250–400 ms

Score reveal:
700–1000 ms

Button press:
100–150 ms

These values are implementation targets, not permission to add decorative
animation.

Motion must communicate:

state
feedback
reward
progression
64. NO UNNECESSARY GAMEPLAY UI

During gameplay, do not display:

bottom navigation
profile controls
friend controls
social feed
history
unnecessary statistics
unrelated notifications
dashboard elements

The challenge should dominate the screen.

65. GAMEPLAY LANGUAGE

Official gameplay should use clear language.

Preferred concepts:

TODAY
OFFICIAL
READY
1 / 3
2 / 3
3 / 3

Practice should use:

PRACTICE

Do not use:

LIVE

for the Official Battle because the MVP is not real-time multiplayer.

66. GAMEPLAY CONTENT LANGUAGE

Instructions should be:

short
direct
action-oriented

Examples:

TAP BLUE

WHAT MOVED?

WHAT WOULD MOST PEOPLE CHOOSE?

Avoid long instructional paragraphs during active gameplay.

67. GAMEPLAY INTERRUPTION PRINCIPLE

A gameplay interruption should never leave the Battle in an ambiguous state.

Every interruption must resolve to one of:

Resume
Pause
Abort
Complete
Recover

The exact mapping for each interruption is still pending.

68. GAMEPLAY FAILURE PRINCIPLE

A gameplay failure should not automatically mean application failure.

For example:

Wrong answer

is a valid gameplay result.

It should not cause:

Application error

The UI must distinguish:

Gameplay outcome

from:

System failure
69. SYSTEM FAILURE PRINCIPLE

System failures include:

network error
corrupted challenge
unavailable data
persistence failure
server failure
unexpected application state

These must use the error-handling system defined in:

09_OFFLINE_AND_ERROR_HANDLING.md

They must not be represented as player mistakes.

70. GAMEPLAY LOADING

Loading may occur before:

Battle configuration
challenge content
result submission
synchronized friend/rival data

Loading states must not look like active gameplay.

Exact loading UI belongs in:

04_SCREEN_BLUEPRINTS.md

71. GAMEPLAY EMPTY STATES

Gameplay should avoid empty states wherever possible.

An official challenge should never intentionally appear without playable
content.

If required content is missing, the system should enter an explicit error
state rather than presenting an empty challenge.

72. INVALID CHALLENGE STATE

If a challenge cannot be played because required configuration/content is
invalid:

Do not start the challenge.

The application should fail safely.

The exact recovery behavior is:

PENDING
73. OFFICIAL BATTLE INTEGRITY RULE

The most important gameplay integrity rule is:

One player must receive one valid official result for one official Battle.

The system must prevent:

duplicate official attempts
duplicate score submission
score replacement
client-side score manipulation
accidental second completion
74. PRACTICE INTEGRITY RULE

Practice must remain isolated from Official Battle scoring.

Conceptually:

OFFICIAL
   ↓
Official Result
   ↓
Official History

PRACTICE
   ↓
Practice Result
   ↓
Practice History / Learning

Practice must not silently enter the official result pipeline.

75. GAMEPLAY DATA SEPARATION

The system should distinguish at minimum:

Official Battle
Practice Session

The final database/API model must preserve this distinction.

76. CONTENT SEPARATION

Official content and Practice content should be distinguishable.

Conceptually:

OFFICIAL:
Deterministic daily content

PRACTICE:
Potentially adaptive content

No AI personalization may silently change Official content.

77. DAILY BATTLE EXAMPLE

A conceptual Official Battle may look like:

TODAY'S BATTLE

3 challenges
~3 minutes

OFFICIAL ATTEMPT

        ↓

SNAP
Reaction gameplay
        ↓
287 / 300

        ↓

SHIFT
Memory / precision gameplay
        ↓
252 / 300

        ↓

CROWD CALL
Prediction gameplay
        ↓
271 / 300

        ↓

CONSISTENCY
91 / 100

        ↓

TOTAL
901 / 1000

        ↓

TOP 9%

        ↓

RIVAL
Rahul 914
You 901

        ↓

13 POINTS TO CATCH

        ↓

DONE FOR TODAY

The numerical values above are examples from the product design
specification.

They are not a required fixed daily result.

78. GAMEPLAY EXPERIENCE RHYTHM

The three challenges intentionally have different personalities:

HOME
Dashboard
   ↓
SNAP
Immersion / Reaction
   ↓
SHIFT
Precision / Memory
   ↓
CROWD CALL
Social Prediction
   ↓
RESULT
Reward
   ↓
RIVAL
Competition
   ↓
DONE
Tomorrow

This rhythm is part of the product experience.

79. CHALLENGE CONTRAST

The three challenges should not feel like variations of the same mechanic.

Snap:
Fast

Shift:
Precise

Crowd Call:
Social

The visual system should support these differences without creating three
unrelated applications.

80. GAMEPLAY CONTENT PRINCIPLE

Every challenge must answer:

What skill is this testing?

Current definitions:

Snap:
Reaction

Shift:
Memory / Precision

Crowd Call:
Social Prediction

A future challenge mechanic that does not have a clear skill purpose should
not be introduced without product review.

81. GAMEPLAY COMPLEXITY LIMIT

The Official Battle should remain understandable within the approximately
three-minute experience.

Do not add:

unnecessary tutorials between every challenge
complex inventories
power-ups
currencies
lives
energy
skill trees
unnecessary progression screens

unless explicitly approved.

82. NO COIN-BASED GAMEPLAY

Gameplay does not currently use:

coins
gems
energy
XP currencies

The core reward is:

Score
Improvement
Rivalry
83. NO GLOBAL COMPETITIVE GAMEPLAY

The gameplay system does not require:

global leaderboard
Elo
competitive rating
tiers
global ranking progression

Competitive context is provided by:

personal score
percentile
friends
rival
84. NO REAL-TIME MULTIPLAYER

The Official Battle is not a real-time multiplayer session.

The player does not need to wait for another player to play simultaneously.

Friend competition is primarily asynchronous.

85. NO FAKE LIVE STATE

Gameplay must not display:

LIVE

unless a future feature genuinely implements real-time multiplayer.

The current Official Battle should use:

OFFICIAL
TODAY
READY

as appropriate.

86. GAMEPLAY REQUIREMENT IDs

The following initial requirement IDs are established by this document.

REQ-GAME-001
Official Battle contains three challenges.

REQ-GAME-002
Official challenge order is Snap → Shift → Crowd Call.

REQ-GAME-003
Official Battle targets approximately three minutes.

REQ-GAME-004
Official Battle has one official attempt.

REQ-GAME-005
Official Battle produces one combined score.

REQ-GAME-006
Maximum Official Battle score is 1000.

REQ-GAME-007
Snap contributes up to 300 points.

REQ-GAME-008
Shift contributes up to 300 points.

REQ-GAME-009
Crowd Call contributes up to 300 points.

REQ-GAME-010
Consistency contributes up to 100 points.

REQ-GAME-011
Practice is separate from Official Battle.

REQ-GAME-012
Practice does not replace the Official Battle result.

REQ-GAME-013
Official Battle content is the same for the relevant player population.

REQ-GAME-014
Official Battle completion is final for the daily attempt.

REQ-GAME-015
Gameplay hides unrelated navigation and social controls.

REQ-GAME-016
Challenges provide immediate gameplay feedback.

REQ-GAME-017
Challenge transitions should not require unnecessary manual continuation.

REQ-GAME-018
Official and Practice modes must remain distinguishable.

REQ-GAME-019
Official Battle scores must be protected from duplicate submission.

REQ-GAME-020
Official Battle scores must remain within defined score boundaries.
87. PENDING GAMEPLAY REQUIREMENTS

The following requirements must be created after the unresolved gameplay
decisions are made.

REQ-GAME-SNAP-###
REQ-GAME-SHIFT-###
REQ-GAME-CROWD-###
REQ-GAME-CONSISTENCY-###
REQ-GAME-TIMER-###
REQ-GAME-INTERRUPTION-###
REQ-GAME-RESET-###
REQ-GAME-CONTENT-###
REQ-GAME-ANTI-CHEAT-###

These IDs should only be finalized when the corresponding rules are
explicitly approved.

88. IMPLEMENTATION RULE

Antigravity must not implement an undefined gameplay mechanic simply because
the UI implies that something should happen.

For example:

If the specification says:

Snap:
Reaction game
300 points

but does not define:

exact number of rounds
reaction formula
timing
penalties

Antigravity must NOT invent those rules.

It must report:

Gameplay requirement incomplete.

Missing:
- round count
- timing formula
- scoring formula

and stop the affected implementation task.

89. GAMEPLAY DECISION RULE

When a gameplay rule is still marked:

PENDING

it is not an implementation requirement.

When a gameplay rule is marked:

DECISION_REQUIRED

implementation must stop until the decision is approved and recorded.

When a gameplay rule becomes:

APPROVED

it becomes an implementation requirement.

90. GAMEPLAY DEFINITION OF DONE

A gameplay feature is not complete merely because it can be played.

The feature must satisfy:

[ ] Gameplay rules implemented
[ ] Score calculation implemented
[ ] Score boundaries enforced
[ ] Required states implemented
[ ] Correct feedback implemented
[ ] Incorrect feedback implemented
[ ] Timeout behavior implemented
[ ] Duplicate input handled
[ ] Transition behavior implemented
[ ] Back behavior implemented
[ ] Lifecycle behavior implemented
[ ] Error behavior implemented
[ ] Offline behavior implemented where applicable
[ ] Official/Practice separation verified
[ ] Duplicate official submission prevented
[ ] Score persistence verified
[ ] Accessibility verified
[ ] Tests implemented
[ ] Tests passing
[ ] Build passing
[ ] No unauthorized gameplay changes
[ ] No undocumented gameplay assumptions
91. GAMEPLAY OPEN DECISION REGISTER

Current unresolved gameplay decisions:

DEC-GAME-001 — Snap Algorithm

Status:

DECISION_REQUIRED

Must define:

rounds
timing
targets
distractors
scoring
penalties
difficulty
DEC-GAME-002 — Shift Algorithm

Status:

DECISION_REQUIRED

Must define:

grid
observation
change
answer
timing
scoring
difficulty
DEC-GAME-003 — Crowd Call Algorithm

Status:

DECISION_REQUIRED

Must define:

questions
choices
crowd source
crowd distribution
scoring
timing
content generation
DEC-GAME-004 — Consistency Formula

Status:

DECISION_REQUIRED

Must define:

inputs
formula
normalization
maximum
edge cases
DEC-GAME-005 — Gameplay Timer

Status:

DECISION_REQUIRED

Must define:

whether all challenges are timed
timer type
duration
timeout
scoring relationship
DEC-GAME-006 — Gameplay Interruption

Status:

DECISION_REQUIRED

Must define:

Back
backgrounding
app termination
screen lock
interruption recovery
DEC-GAME-007 — Daily Reset

Status:

DECISION_REQUIRED

Must define:

reset time
timezone authority
travel behavior
date transition behavior
92. CURRENT GAMEPLAY STATUS
Official Battle Concept:
DEFINED

Three Challenge Structure:
DEFINED

Challenge Order:
DEFINED

Official Attempt:
DEFINED

Practice Separation:
DEFINED

Same Official Battle:
DEFINED

Score Maximum:
DEFINED

Snap Role:
DEFINED

Shift Role:
DEFINED

Crowd Call Role:
DEFINED

Snap Exact Algorithm:
PENDING

Shift Exact Algorithm:
PENDING

Crowd Call Exact Algorithm:
PENDING

Consistency Formula:
PENDING

Exact Timer Rules:
PENDING

Difficulty System:
PENDING

Content Generation:
PENDING

Content Repeat Rules:
PENDING

Gameplay Interruption:
PENDING

Daily Reset:
PENDING

Anti-Cheat Implementation:
PENDING

Authoritative Score Architecture:
PENDING
93. FINAL GAMEPLAY PRINCIPLE

Daily Battle gameplay must remain:

SHORT
↓
CLEAR
↓
SKILL-BASED
↓
VARIED
↓
COMPETITIVE
↓
REWARDING
↓
FINITE

The player should never wonder:

What am I supposed to do?

The player should never need to understand:

How does the backend calculate this?

The player should simply:

Play
↓
Perform
↓
Score
↓
Compare
↓
Finish

The system underneath may be sophisticated.

The player experience should remain simple.

94. FINAL RULE FOR UNDEFINED GAMEPLAY

If this document does not define a gameplay rule:

DO NOT INVENT IT.

If the UI suggests a behavior but the gameplay rules do not define it:

DO NOT ASSUME THE RULE.

If two gameplay rules conflict:

STOP IMPLEMENTATION.

If a technical limitation requires changing a gameplay rule:

RECORD THE PROBLEM → PROPOSE OPTIONS → RECEIVE APPROVAL → UPDATE THE SPEC.

The implementation must follow the approved gameplay specification, not
AI assumptions.


### Important: what we have deliberately **not** done

The original source gives us the **gameplay concepts and UX behavior**, but not enough information to honestly define things like:

- whether Snap has 10 rounds or 20
- whether Shift is 3×3 or 4×4
- whether Crowd Call uses historical crowd data or a live/simulated population
- how reaction time converts into points
- how Consistency becomes 0–100
- exact timers
- exact difficulty progression

Those are **real product decisions**, not documentation formatting decisions. The source only establishes the high-level mechanics and examples. 

So this version intentionally creates a **controlled pending-state** for them. That is exactly what we want before Antigravity gets access to the implementation.
