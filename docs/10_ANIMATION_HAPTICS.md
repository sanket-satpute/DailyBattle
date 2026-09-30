# Daily Battle — Animation, Haptics & Audio Specification

**Document ID:** DB-ANIMATION-HAPTICS  
**Version:** 1.0  
**Status:** Interaction Feedback Contract  
**Audience:** Antigravity AI, Android implementation agents, UI/UX, QA  
**Product:** Daily Battle  
**Authority:** Master Specification + approved project documentation  
**Last Updated:** 2026-09-30

---

# 1. Purpose

This document defines the motion, animation, haptic, and sound behavior for Daily Battle.

Its purpose is to ensure that:

- animations communicate meaningful state changes
- gameplay feels responsive
- score feedback feels rewarding
- navigation remains fast
- haptics reinforce interaction
- sound reinforces important events
- motion remains consistent across screens
- accessibility is respected
- Antigravity does not invent unnecessary animation

Motion is part of the product system.

It is not decoration.

---

# 2. Core Motion Principle

Daily Battle uses:

> Motion to communicate change, feedback, and reward.

Motion must NOT exist merely because an element can be animated.

Every animation should answer at least one of:

1. What changed?
2. What should the user focus on?
3. Was the action successful?
4. Was the action incorrect?
5. Did the battle progress?
6. Was something earned or achieved?

If an animation does none of these:

> Do not add it.

---

# 3. Visual Motion Direction

Daily Battle should feel:

- premium
- responsive
- focused
- controlled
- competitive
- modern

It should NOT feel:

- childish
- arcade-heavy
- casino-like
- chaotic
- excessively futuristic
- neon
- cartoonish
- over-animated

Motion should support the product-design direction rather than turning the app into an animation showcase.

---

# 4. Motion Hierarchy

Motion intensity follows:

```text
Level 0 — Static
    ↓
Level 1 — Micro Interaction
    ↓
Level 2 — State Transition
    ↓
Level 3 — Reward / Score Reveal

Most UI interactions should use Level 0–2.

Level 3 should be reserved for important moments.

5. Global Duration Tokens

The existing product specification defines these timing ranges:

Motion	Duration
Normal navigation	200–300 ms
Game transitions	250–400 ms
Score reveal	700–1000 ms
Button press feedback	100–150 ms

These ranges are the authoritative motion direction.

Do not randomly introduce unrelated durations throughout the application.

6. Proposed Motion Tokens
Status

PROPOSED

Use centralized motion tokens rather than hardcoding durations throughout screens.

Conceptually:

motion.instant      = 0 ms
motion.micro        = 100–150 ms
motion.fast         = 150–200 ms
motion.standard     = 200–300 ms
motion.game         = 250–400 ms
motion.reward       = 700–1000 ms

Exact token names may vary by implementation.

The timing ranges must remain consistent with the approved design specification.

7. Easing
Status

PROPOSED

Animations should use predictable easing appropriate to the interaction.

Recommended conceptual mapping:

Motion	Easing
Button press	Ease-out
Small feedback	Ease-out
Screen entrance	Ease-out
Screen exit	Ease-in
Score reveal	Controlled ease-out
Spring-like feedback	Limited / subtle

Avoid exaggerated bounce or elastic easing.

The app should feel precise rather than playful.

8. No Default Animation Everywhere

Antigravity MUST NOT automatically animate:

every card
every text block
every icon
every list item
every screen section
every number
every navigation destination

Animation must be explicitly justified.

9. Screen Transition Philosophy

Screen transitions should communicate:

"The user moved to a new state."

They should not communicate:

"The application is showing off its animation."

Transitions must remain fast enough that they do not make the ~3-minute Battle feel slow.

10. Standard Navigation Transition

For ordinary navigation:

Duration: 200–300 ms

Recommended behavior:

Current Screen
    ↓
Subtle exit
    +
Next Screen enters

Avoid:

dramatic zoom
3D rotation
long crossfade
page curl
cinematic transitions
11. Onboarding Transitions

Welcome → Battle Name should feel lightweight.

Use:

subtle content transition

Do not introduce:

long logo animation
elaborate particle effects
animated backgrounds
unnecessary onboarding carousel

The user should reach the product quickly.

12. Home Screen Motion

Home is the primary launch point.

Motion should reinforce:

Today's Battle

Allowed:

subtle hero entrance
small state transition when Battle becomes completed
controlled CTA press feedback
score/result reveal where applicable

Avoid:

continuously animated hero card
pulsing CTA forever
moving background elements
auto-playing decorative effects
13. Today's Battle CTA

The primary PLAY BATTLE button uses micro feedback.

Tap
 ↓
100–150 ms feedback
 ↓
Navigation

Feedback may include:

slight scale response
surface/opacity response
subtle haptic

Do not make the button continuously pulse to attract attention.

14. Battle Intro

Battle Intro should be calm and focused.

Allowed:

content entrance
CTA press feedback
subtle challenge-progress indicator

Avoid:

revealing all challenge details through dramatic animation
animated countdown unless explicitly part of gameplay
unnecessary background effects
15. Gameplay Motion Philosophy

Gameplay is different from normal product UI.

Gameplay motion must prioritize:

responsiveness
readability
reaction speed
feedback
state clarity

Animation must never make a user wait unnecessarily before the next interaction.

16. Gameplay Transition Timing

Game transitions:

250–400 ms

This applies conceptually to:

Battle Intro → Snap
Snap → Shift
Shift → Crowd Call
Crowd Call → Results

The transition should feel continuous but fast.

17. Snap Motion

Snap is the fastest-feeling challenge.

Motion should be:

minimal
immediate
precise

The game area should dominate.

Avoid:

large entrance animations
distracting background motion
decorative particles
oversized success animations
18. Snap Correct Feedback

Correct interaction may use:

small visual pulse
+
light/success haptic
+
short success sound

The visual response must remain compact.

Do NOT display:

CORRECT!!!

as a large full-screen celebration.

19. Snap Incorrect Feedback

Incorrect interaction may use:

small shake
+
short error haptic
+
short incorrect sound

The shake should be:

short
localized
subtle

Do not shake the entire screen.

Do not use large red overlays.

20. Snap Completion

Example conceptual flow:

Challenge Complete
      ↓
287 / 300
      ↓
Short completion feedback
      ↓
Transition to Shift

The score feedback should be visible without creating a long pause.

Exact score-reveal timing follows the finalized gameplay implementation.

21. Shift Motion

Shift is a memory/precision challenge.

Its visual language should contrast Snap.

Motion should emphasize:

state change
grid transformation
selection feedback

Avoid unnecessary movement around the entire screen.

22. Shift Grid Animation

If the gameplay design requires a visual transformation:

Grid State A
     ↓
Controlled transition
     ↓
Grid State B

The transition must remain readable.

Do not animate every cell independently unless required by the finalized challenge design.

23. Shift Correct Feedback

Use:

small local confirmation
+
success haptic
+
success sound where enabled

Avoid:

giant green overlays
full-screen celebration
excessive particles
24. Shift Incorrect Feedback

Use:

localized shake
+
short error haptic
+
incorrect sound

The user must immediately understand that the answer was incorrect.

Do not interrupt the flow with a long animation.

25. Crowd Call Motion

Crowd Call is the most social-looking challenge.

Motion may be slightly richer than Snap/Shift, but must remain controlled.

Allowed:

answer-card selection
distribution reveal
prediction result reveal
subtle movement of distribution bars
26. Crowd Distribution Animation

When crowd distribution is revealed:

0%
 ↓
Final distribution

The animation should be:

short
readable
controlled

The animation must not imply that the distribution is being calculated live unless the product/backend actually provides live data.

Do not use fake "LIVE" indicators.

27. Crowd Call Prediction Result

The result should communicate:

Your prediction
+
Crowd outcome
+
Whether prediction matched

Use sequential but fast feedback.

Do not turn the result into a large social-media-style celebration.

28. Challenge-to-Challenge Transition

Canonical sequence:

Snap
 ↓
Shift
 ↓
Crowd Call

The transition should communicate progression.

Recommended conceptual structure:

Complete current challenge
        ↓
brief completion feedback
        ↓
250–400 ms transition
        ↓
next challenge ready

Avoid inserting unnecessary loading-like animations between locally available challenges.

29. Automatic Transition vs User Tap

The existing gameplay direction supports automatic progression after a completed challenge.

Therefore:

Complete
 ↓
Feedback
 ↓
Next Challenge

should not require an unnecessary extra:

CONTINUE

button unless explicitly specified.

30. Results Screen Entry

Results is a high-emotion transition.

It should feel more significant than ordinary navigation.

However:

Significance comes from sequencing and score reveal, not excessive visual effects.

31. Score Reveal

The product specification defines:

Score reveal:
700–1000 ms

Conceptual sequence:

0
 ↓
Final Score
 ↓
Percentile
 ↓
Improvement
 ↓
Rival

The sequence should feel deliberate.

32. Score Animation

The score may animate from a low/start value toward the final value.

Requirements:

final value must be exact
animation must not alter the actual score
score must be accessible even if animation is reduced
animation must not delay the user excessively

The animation is presentation only.

The domain result already exists independently.

33. Score Reveal Integrity

The displayed animated number must eventually resolve to:

authoritative final score

Never calculate the final score from animation progress.

Incorrect architecture:

Animation Value
      ↓
Battle Score

Correct:

Battle Result
      ↓
Animation
      ↓
Displayed Score
34. Percentile Reveal

After the score:

Final Score
    ↓
Percentile

Use a subtle entrance.

Avoid:

giant percentile animation
confetti
fireworks
casino-like effects

The user should remain focused on performance.

35. Improvement Reveal

If an improvement value exists:

+37 from yesterday

may enter after percentile.

The plus value should have a clear positive visual treatment consistent with the design system.

Do not imply improvement when data is unavailable.

36. Rival Reveal

The rival section may enter after the user's own result.

Conceptually:

Your Score
    ↓
Percentile
    ↓
Improvement
    ↓
Rival

The rival reveal should create tension without becoming theatrical.

37. Personal Best

If a Personal Best is achieved:

Personal Best

may receive stronger feedback.

The existing product direction allows stronger haptics for a Personal Best.

However:

no excessive animation
no full-screen takeover
no confetti by default
no loud continuous sound

unless explicitly approved.

38. Battle Complete Feedback

Battle completion may use:

medium haptic
+
battle-complete sound
+
controlled visual transition

The feedback should feel rewarding.

It must still remain premium and restrained.

39. Button States

Buttons should provide immediate press feedback.

Timing:

100–150 ms

Possible visual behavior:

Default
   ↓
Pressed
   ↓
Released

Do not create exaggerated button movement.

40. Disabled Buttons

Disabled buttons should not perform:

press animation
haptic
success sound

unless required by accessibility behavior.

Disabled means:

No action occurred.

41. Loading Buttons

Loading buttons should communicate:

The requested operation is being processed.

They must prevent duplicate taps where required.

Avoid overly elaborate spinners.

Use the simplest appropriate loading indicator.

42. Success States

Success feedback should be proportional to the action.

Examples:

Friend added

Small success feedback.

Battle completed

Stronger feedback.

Personal best

Strongest achievement feedback.

Do not use the same animation intensity for all successes.

43. Error Feedback

Error feedback should be distinct from gameplay incorrect feedback.

For example:

Incorrect Answer

and:

Network Failure

must not use identical:

haptic
sound
animation

because they communicate different meanings.

44. Haptic System

Haptics are part of the interaction system.

Conceptual haptic categories:

HAPTIC_LIGHT
HAPTIC_SUCCESS
HAPTIC_ERROR
HAPTIC_STRONG_SUCCESS
HAPTIC_BATTLE_COMPLETE

Exact Android API implementation is a technical decision.

45. Haptic Mapping

The product specification defines the following conceptual mapping:

Event	Haptic
Button tap	Light
Correct answer	Success
Incorrect answer	Short error
Personal Best	Stronger
Battle complete	Medium

Do not introduce additional haptic patterns without justification.

46. Button Tap Haptic

Use a light haptic for meaningful interactive controls.

Do not trigger haptics for:

every scroll
every animation frame
decorative UI
passive screen entry
47. Correct Answer Haptic

Correct gameplay input may use a short success haptic.

It should be:

immediate
brief
clearly distinguishable from an error
48. Incorrect Answer Haptic

Incorrect input may use a short error haptic.

It should not be:

painful
long
repeated
overly strong
49. Personal Best Haptic

Personal Best is a stronger achievement moment.

Use stronger feedback than ordinary correct input.

Conceptually:

Personal Best
    ↓
stronger haptic
+
score/result feedback

Do not repeatedly trigger it.

It should happen only when a genuine Personal Best is confirmed.

50. Battle Complete Haptic

Battle completion uses a medium-strength haptic.

It should communicate:

The complete Daily Battle is finished.

It should not feel like an alarm.

51. Haptic Frequency

Avoid haptic fatigue.

Do not trigger multiple overlapping haptic patterns for one event.

For example:

Correct Answer

should not cause:

button haptic
+
correct haptic
+
screen transition haptic

unless explicitly designed.

Prefer one meaningful haptic event.

52. Haptic Settings

The user may disable haptics through Settings.

If haptics are disabled:

Visual feedback remains
Sound follows its own setting
Haptics do not fire

Disabling haptics must not disable gameplay.

53. Sound System

Sound should reinforce important interaction states.

Conceptual sound categories:

UI_TAP
CORRECT
INCORRECT
SCORE_REVEAL
PERSONAL_BEST
BATTLE_COMPLETE
54. Sound Mapping
Event	Sound
Button tap	UI tap
Correct answer	Correct
Incorrect answer	Incorrect
Score reveal	Score reveal
Personal Best	Personal best
Battle complete	Battle complete
55. No Continuous Background Music

The product direction explicitly avoids continuous background music in the MVP.

Do not add:

looping music
ambient tracks
automatic music playback

unless a future approved requirement changes this.

56. Sound Settings

Sound must be independently controllable from haptics.

Conceptually:

Sound ON/OFF
Haptics ON/OFF

Turning sound off must not disable:

visual feedback
haptics

Turning haptics off must not disable:

visual feedback
sound
57. System Volume

The application must respect Android audio behavior.

Do not attempt to override system volume.

Do not repeatedly force audio playback when the user has disabled or muted sound.

58. Audio Focus

If audio is implemented, it should respect Android audio-focus behavior.

Do not aggressively interrupt other applications' audio for minor UI sounds.

Exact audio-focus implementation is technical and pending.

59. Animation and Accessibility

Motion must never be the only way to communicate meaning.

For example:

Correct

must not be represented only by:

green animation

It should also have another meaningful indicator such as:

visual state
text/label where appropriate
shape/icon
haptic/sound where enabled

This follows the product accessibility requirement.

60. Reduced Motion
Status

REQUIRED BEHAVIOR / IMPLEMENTATION DETAIL PENDING

The application should respect platform/user reduced-motion preferences where supported.

When reduced motion is active:

remove unnecessary decorative movement
shorten non-essential transitions
preserve essential state feedback
keep score/result information visible
do not remove critical interaction feedback

The product must remain understandable without animation.

61. Animation Cancellation

Animations must be cancellable or safely finish when:

user navigates away
screen leaves composition/lifecycle
process state changes
a new state supersedes the old state

Do not leave stale animation callbacks modifying a new screen state.

62. Animation and Business Logic Separation

Animations must never own business state.

Incorrect:

Score animation finishes
      ↓
Set battle completed

Correct:

Battle completed
      ↓
Result state exists
      ↓
Animation presents result

The animation is presentation only.

63. Timer and Animation Separation

Gameplay timers must not depend on animation progress.

Incorrect:

Animation duration
      ↓
Gameplay timer

Correct:

Authoritative elapsed time
      ↓
Gameplay state

Gameplay state
      ↓
Visual timer animation

This prevents frame-rate or rendering differences from changing gameplay behavior.

64. Lifecycle Safety

If an animation is interrupted by:

backgrounding
process recreation
navigation
configuration change

the underlying product state must remain authoritative.

Do not use animation completion callbacks as the only trigger for critical state changes.

65. Navigation Animation Rules

Navigation animation must not delay important actions.

For example:

PLAY BATTLE

should not wait for an unnecessarily long exit animation before entering Battle Intro.

The user should perceive the app as responsive.

66. Modal / Dialog Motion

Dialogs should use subtle entrance/exit transitions.

Avoid:

bouncing dialogs
spinning dialogs
dramatic scale effects
full-screen theatrical transitions

Dialogs are utility UI, not gameplay rewards.

67. Bottom Navigation Motion

Bottom navigation may use subtle active-state transitions.

Allowed:

icon state change
small indicator transition
subtle label emphasis

Avoid:

large icon jumps
expanding navigation bars
floating animated Battle button

The product explicitly avoids a giant central floating Battle button.

68. Friend / Rival Motion

Social competition UI may use subtle feedback:

Friend added
Rival selected
Score updated

But Friends is not a social feed.

Avoid:

reaction animations
likes/comments animations
chat-style typing indicators
social-media interaction patterns
69. Profile Motion

Profile/Battle DNA should remain calm.

Allowed:

subtle chart/metric entrance
record update feedback
small value transitions

Avoid:

continuously animated statistics
rotating profile graphics
gamified XP bars unless explicitly approved
70. History Motion

History should prioritize data readability.

A graph may animate into view subtly.

Do not:

animate every data point dramatically
continuously animate the graph
use motion to imply trends that are not in the data
71. Settings Motion

Settings is intentionally the calmest part of the app.

Use minimal motion.

Allowed:

toggle transition
navigation transition
small feedback on changed preference

Avoid:

game-like celebrations
animated cards
decorative motion
achievement feedback
72. Loading Animation

Loading indicators should communicate progress without becoming distracting.

Avoid:

large full-screen animated loaders for small operations
excessive pulsing
decorative spinners

Prefer:

small
clear
temporary
73. Skeleton Loading

Skeleton loading may be used where appropriate.

However:

do not animate skeletons continuously if not needed
do not skeletonize gameplay
do not use skeletons when content appears immediately

The implementation should avoid unnecessary loading theatrics.

74. Empty State Motion

Empty states should generally be static or use very subtle entrance motion.

Do not animate empty-state illustrations continuously.

75. Error State Motion

Error states should generally be static.

A subtle entrance is acceptable.

Do not use:

flashing red
repeated shaking
warning pulses
dramatic alarms
76. Achievement Intensity Hierarchy

The product should have a clear hierarchy:

Normal interaction
        ↓
Correct answer
        ↓
Challenge complete
        ↓
Battle complete
        ↓
Personal Best

Feedback intensity should generally increase with significance.

However, all levels remain visually restrained.

77. No Casino Mechanics

Do not use:

slot-machine effects
jackpot animations
confetti explosions
coin showers
roulette-like reveals
flashing reward sequences

Daily Battle is a skill/competition product, not a gambling-style experience.

78. No Arcade Overload

Do not add:

screen shake on every event
particle bursts on every correct answer
constant glow
bouncing cards
animated gradients
flashing borders
aggressive countdown effects

Motion must remain premium.

79. No Continuous Decorative Motion

Avoid:

floating particles
moving gradients
animated background shapes
continuous card breathing
permanent CTA pulsing

unless a future approved design explicitly requires them.

80. Motion Performance

Animations must remain smooth on supported Android devices.

Avoid unnecessarily expensive:

blur
shadows
shader effects
particle systems
large bitmap animations
continuous recomposition caused by animation

Gameplay responsiveness takes priority over decorative fidelity.

81. Animation Resource Management

Animations must not:

continue after a screen is destroyed
retain references to obsolete UI
create unbounded coroutines
create repeated timers
leak contexts

Lifecycle-aware implementation is required.

82. Testing Motion

Motion testing should verify:

correct duration range
correct trigger
correct state
no duplicate animation
no animation on disabled controls
animation cancellation
lifecycle safety
reduced-motion behavior

Exact frame-by-frame pixel validation is not required for every animation.

Behavior and timing are the priority.

83. Haptic Testing

QA should verify:

haptic fires for intended event
haptic does not fire for disabled interaction
haptic respects Settings
duplicate taps do not produce duplicate haptics
incorrect and correct feedback remain distinguishable
Personal Best feedback only occurs for genuine Personal Best
Battle Complete feedback occurs once
84. Sound Testing

QA should verify:

sound respects sound setting
sound does not play when disabled
correct sound maps to correct event
incorrect sound maps to incorrect event
Battle Complete sound occurs once
Personal Best sound occurs only when applicable
no continuous background music plays
sound does not repeatedly trigger after lifecycle recreation
85. Motion State Matrix
Event	Visual	Haptic	Sound
Button tap	Micro press	Light	Tap
Correct answer	Small pulse	Success	Correct
Incorrect answer	Small shake	Error	Incorrect
Challenge complete	Compact transition	Appropriate success	Optional completion
Battle complete	Result transition	Medium	Battle complete
Personal Best	Stronger result feedback	Stronger	Personal Best
Network error	Contextual error	None/minimal	Usually none
Empty state	Static/subtle	None	None
Loading	Minimal indicator	None	None
86. Accessibility Matrix
Event	Must work without motion?
Correct answer	Yes
Incorrect answer	Yes
Battle complete	Yes
Personal Best	Yes
Score reveal	Yes
Loading	Yes
Error	Yes
Navigation	Yes

The final information must always remain available without animation.

87. Animation Naming

Implementation should use semantic animation names.

Preferred:

buttonPress
challengeCorrect
challengeIncorrect
challengeComplete
battleTransition
scoreReveal
personalBest
battleComplete

Avoid vague names such as:

animation1
coolAnimation
effect2
newThing
88. Haptic Naming

Preferred:

HapticEvent.ButtonTap
HapticEvent.Correct
HapticEvent.Incorrect
HapticEvent.PersonalBest
HapticEvent.BattleComplete

Exact implementation language may differ.

89. Sound Naming

Preferred:

SoundEvent.UiTap
SoundEvent.Correct
SoundEvent.Incorrect
SoundEvent.ScoreReveal
SoundEvent.PersonalBest
SoundEvent.BattleComplete

Sound files should use semantic names rather than unexplained filenames.

90. Centralized Feedback System
Status

PROPOSED

Motion, haptic, and sound should be centrally coordinated where practical.

Conceptually:

FeedbackController
├── visual feedback
├── haptic feedback
└── sound feedback

This prevents each screen from implementing unrelated feedback rules.

91. Feedback Independence

Visual, haptic, and sound channels should remain independently controllable.

Example:

Visual = ON
Haptic = OFF
Sound = ON

must be valid.

92. Critical Rule: Domain vs Feedback

Business events must exist independently of feedback.

Example:

PersonalBestAchieved
       ↓
Visual Feedback
Haptic Feedback
Sound Feedback
Analytics

Not:

Haptic finished
       ↓
PersonalBestAchieved
93. Feedback Duplication Prevention

One business event should not accidentally trigger feedback multiple times.

For example:

BattleComplete

must not cause:

3 × haptic
3 × sound
3 × animation

because of recomposition, lifecycle recreation, or repeated state observation.

One-shot effects must be modeled separately from persistent state where appropriate.

94. One-Shot Event Handling

Transient effects such as:

navigation
toast/snackbar
haptic
sound
achievement animation

should not repeatedly execute merely because a screen recomposes or is recreated.

The implementation must distinguish:

Persistent State

from:

One-Shot Event
95. Example Feedback Flow

Correct Snap answer:

User Input
    ↓
Domain evaluates answer
    ↓
Correct result
    ↓
Challenge state = CORRECT
    ↓
Presentation emits feedback event
    ├── Visual pulse
    ├── Success haptic
    └── Correct sound
    ↓
Challenge completes
    ↓
Next challenge transition

The domain result exists independently of all three feedback channels.

96. Example Personal Best Flow
Battle Result confirmed
        ↓
Personal Best = true
        ↓
Presentation
   ├── Score/result feedback
   ├── Stronger haptic
   └── Personal Best sound

No feedback should trigger before Personal Best is actually confirmed.

97. Example Error Flow
API Request
    ↓
Timeout
    ↓
Error state
    ↓
Preserve battle state
    ↓
Show retry

Do NOT:

Timeout
 ↓
Incorrect haptic
 ↓
Incorrect sound

A network failure is not a gameplay mistake.

98. Implementation Constraints for Antigravity

Antigravity MUST:

use centralized motion values
follow approved duration ranges
preserve gameplay responsiveness
use semantic feedback events
respect sound/haptic settings
handle reduced motion
prevent duplicate one-shot effects
separate feedback from business logic
make lifecycle-safe animations
test important feedback states

Antigravity MUST NOT:

add decorative animations without specification
add continuous background motion
add confetti by default
add screen shake globally
add casino-style effects
add fake LIVE animation
animate business state directly
use animation completion as business confirmation
trigger repeated haptics because of recomposition
introduce background music without approval
99. Pending Motion Decisions
ID	Decision	Status
MOTION-PENDING-001	Exact easing curves	Pending
MOTION-PENDING-002	Exact score counter animation	Pending
MOTION-PENDING-003	Exact reduced-motion implementation	Pending
MOTION-PENDING-004	Exact sound asset set	Pending
MOTION-PENDING-005	Exact haptic API/pattern mapping	Pending
MOTION-PENDING-006	Audio focus implementation	Pending
MOTION-PENDING-007	Exact Crowd Call distribution animation	Pending
MOTION-PENDING-008	Exact challenge transition choreography	Pending
MOTION-PENDING-009	Gameplay interruption animation behavior	Pending
100. Animation & Feedback Definition of Done

Motion/feedback implementation is complete only when:

Motion
durations follow approved ranges
transitions are consistent
gameplay remains responsive
no unauthorized decorative motion exists
lifecycle behavior is safe
reduced-motion behavior is supported
Haptics
mappings are implemented
settings are respected
duplicate feedback is prevented
intensity hierarchy is preserved
Sound
mappings are implemented
settings are respected
no background music exists unless approved
duplicate sounds are prevented
lifecycle behavior is safe
Architecture
feedback is separated from domain logic
one-shot events are handled correctly
animation does not control business state
feedback is centrally managed where appropriate
QA
motion states tested
haptics tested
sound tested
interruption tested
accessibility tested
implementation report completed
101. Final Motion Rule

Daily Battle motion follows:

STATE CHANGE
    ↓
MEANINGFUL FEEDBACK
    ↓
FAST TRANSITION
    ↓
NEXT STATE

Not:

EVERYTHING
    ↓
ANIMATE EVERYTHING
    ↓
WAIT
    ↓
ANIMATE AGAIN

The goal is not maximum animation.

The goal is maximum clarity and responsiveness with minimum unnecessary motion.