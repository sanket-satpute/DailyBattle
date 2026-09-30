# Daily Battle — Accessibility Specification

**Document ID:** DB-ACCESSIBILITY  
**Version:** 1.0  
**Status:** Accessibility & Inclusive Interaction Contract  
**Audience:** Antigravity AI, Android implementation agents, UI/UX, QA  
**Product:** Daily Battle  
**Authority:** Master Specification + approved project documentation  
**Last Updated:** 2026-09-30

---

# 1. Purpose

This document defines the accessibility requirements for Daily Battle.

Accessibility must be considered across:

- onboarding
- navigation
- Home
- Battle Intro
- Snap
- Shift
- Crowd Call
- Results
- Rival
- Friends
- Add Friend
- Profile
- History
- Settings
- loading states
- empty states
- error states
- haptics
- sound
- animation
- gameplay feedback

Accessibility is a product requirement.

It must not be treated as a final QA-only activity.

---

# 2. Accessibility Principle

The user must be able to understand and operate the product without relying on a single sensory channel.

In particular:

> Color, animation, sound, and haptics must not be the sole source of important information.

---

# 3. Accessibility Goals

Daily Battle should support:

1. clear visual hierarchy
2. readable text
3. sufficient contrast
4. large enough touch targets
5. meaningful semantics
6. screen-reader navigation
7. scalable text where technically supported
8. non-color-only state communication
9. reduced-motion behavior
10. optional sound/haptic feedback
11. predictable focus order
12. accessible error recovery
13. accessible gameplay feedback

---

# 4. Accessibility Authority

Accessibility implementation must follow:

1. `05_DESIGN_SYSTEM.md`
2. `10_ANIMATION_HAPTICS.md`
3. this document
4. screen-specific specifications

If a screen specification conflicts with an accessibility requirement:

> Do not silently remove accessibility behavior.

Report the conflict and propose a resolution.

---

# 5. Accessibility Is Not a Separate UI

Accessibility must use the same product hierarchy as the visual design.

The accessible experience should preserve:

```text
What should I do?
        ↓
What matters?
        ↓
Supporting information

Do not create an entirely different information architecture for accessibility.

6. Touch Target Requirements

The existing design system defines:

Minimum touch target: 44 px
Preferred touch target: 48 px

Interactive controls should therefore provide at least the minimum required touch area.

Where practical, use the preferred 48 px target.

7. Touch Target vs Visual Size

The visible icon does not need to be 44–48 px.

For example:

Visual icon: 20–24 px
Touch target: ≥44 px

The touch area may include appropriate invisible padding.

Do not enlarge visual icons unnecessarily just to satisfy touch-target requirements.

8. Touch Target Spacing

Adjacent interactive elements must have sufficient separation to reduce accidental activation.

Avoid:

[Button][Button]

with insufficient spacing.

Especially important for:

answer options
navigation items
settings rows
friend actions
result CTAs
9. Primary CTA Accessibility

Primary actions such as:

PLAY BATTLE
START
BEAT RIVAL
ADD FRIEND

must:

be clearly identifiable
have sufficient touch area
have an accessible semantic label
provide pressed-state feedback
remain usable without haptics or sound
10. Icon-Only Controls

Icon-only controls must have accessible labels.

Example:

[copy icon]

must expose a meaningful semantic description such as:

Copy battle code

Do not expose:

Icon
Button
Image

as the only accessibility information.

11. Decorative Images

Decorative images that provide no information should not create unnecessary screen-reader noise.

They should be treated as decorative where supported.

The screen reader should focus on meaningful content.

12. Informational Images

If an image communicates information required to understand the screen, its semantic description must communicate that information.

Do not rely on:

Image

alone.

13. Text Hierarchy

Typography follows the established design system.

Important UI must remain readable.

The design system establishes:

Display
H1
H2
H3
Body Large
Body
Caption
Label

Do not introduce extremely small text solely to fit more content.

14. Minimum Important Text Size

The design system states:

No important UI should be below 12 px.

Do not create critical controls, instructions, score information, or error messages below the established minimum.

15. Dynamic Text / Font Scaling

The application should support Android text scaling where practical.

When text size increases:

content must remain understandable
important text must not be clipped
buttons must remain usable
labels must not overlap
score values must remain visible
navigation must remain understandable
16. Fixed-Height Text Containers

Avoid rigid fixed-height containers for content that may scale.

Especially avoid:

fixed height
+
single-line required
+
large text scaling

when it can cause clipping.

17. Score Accessibility

Scores are high-priority information.

Example:

901

must be understandable by assistive technologies as a score.

Do not expose the score only as:

9 0 1

or as an unintelligible collection of animated digits.

The final score must have a stable semantic representation.

18. Animated Score Accessibility

During score reveal:

0
10
100
500
901

is visual animation.

Assistive technologies should not necessarily announce every intermediate value.

The accessible representation should resolve to:

Score: 901

once the final result is available.

19. Score Reveal and Reduced Motion

If reduced motion is enabled:

skip or shorten the counter animation
show the final score promptly
preserve the emotional hierarchy through typography and layout
do not hide result information
20. Color Independence

Color must never be the only indicator of state.

Examples:

Correct = green
Incorrect = red

is insufficient by itself.

Use additional signals such as:

icon
text
shape
position
animation
haptic
sound

where appropriate.

21. Challenge Accent Colors

Current challenge accents:

Snap       #4EA8FF
Shift      #FFB84D
Crowd Call #39D98A

These colors identify challenge context.

They must not be the only way to distinguish challenges.

The challenge name/context must also be communicated.

22. Correct / Incorrect States

Correct and incorrect states must have non-color indicators.

For example:

Correct
✓

and:

Incorrect
×

or an equivalent approved visual/semantic treatment.

Do not rely solely on:

green
red
23. Focus / Selection State

Selected controls must have a clear state that does not depend only on color.

Examples:

border
shape
checkmark
filled state
semantic selected state

The exact visual treatment follows the design system.

24. Disabled State

Disabled controls must communicate:

This action is currently unavailable.

Do not communicate disabled state through color alone.

The control should also be semantically exposed as disabled where supported.

25. Loading State

Loading must be understandable without relying exclusively on animation.

For example:

Loading...

may accompany a spinner where appropriate.

Do not make a continuously rotating icon the only indication that something is loading.

26. Error State

Errors must be understandable without:

red color alone
vibration alone
sound alone
animation alone

The error should have an appropriate textual or semantic representation.

Example:

Couldn't load today's battle.
Retry
27. Empty State

An empty state must clearly communicate that:

Data exists successfully, but there is currently nothing to show.

This is different from an error.

For example:

No friends yet.
Add a friend to start competing.

should be distinguishable from:

Couldn't load friends.
Retry
28. Screen Reader Support

The application should expose meaningful semantics to Android screen readers.

Important elements include:

screen title
primary CTA
navigation destinations
challenge instructions
score
selected answer
result state
error message
retry action
friend code
settings controls
29. Screen-Level Semantics

Each screen should expose a clear semantic hierarchy.

Example:

Screen: Today's Battle

Today's Battle
3 challenges
About 3 minutes
Official attempt
Play Battle

The screen reader should not encounter a random implementation order unrelated to the visual hierarchy.

30. Focus Order

Accessibility focus should generally follow the visual and interaction hierarchy.

For example Home:

Greeting
↓
Momentum
↓
Today's Battle
↓
Play Battle
↓
Rival
↓
Bottom navigation

Do not make the focus jump unpredictably between distant elements.

31. Bottom Navigation Accessibility

Bottom navigation contains:

Home
Battle
Friends
Me

Each destination must have:

accessible label
selected state
predictable order
sufficient touch area

The selected destination must be exposed semantically, not only visually.

32. Gameplay Navigation

During gameplay, the bottom navigation is hidden.

The accessibility hierarchy must also reflect that.

Do not expose hidden navigation controls to the screen reader.

33. Gameplay Instructions

Every challenge must provide accessible instructions.

For example:

Snap
React as quickly as possible.

The exact copy belongs to gameplay/screen specifications.

The instruction must be available as text/semantics rather than only visual animation.

34. Snap Accessibility

Snap is reaction-focused.

The challenge must expose:

current instruction
current state
target/distractor information where appropriate
result state
completion state

The exact accessibility interaction model for Snap is PENDING if the challenge mechanics ultimately require timing-dependent interaction that cannot be equivalently performed through assistive input.

Do not invent an alternative scoring system.

35. Snap Timing Accessibility

Snap may depend on reaction time.

Accessibility cannot simply disable timing and preserve the same score unless the gameplay specification explicitly supports that behavior.

Therefore the following are pending:

accessibility timing adjustment
extended-time mode
alternative interaction
whether accessibility mode changes scoring

Antigravity MUST NOT silently alter scoring for accessibility.

36. Shift Accessibility

Shift must expose:

instruction
grid state
available answers
selected answer
correct/incorrect state

The grid must have a meaningful semantic representation where possible.

Avoid exposing every decorative grid cell as unnecessary accessibility noise.

37. Shift Grid Semantics

If the grid itself is interactive:

Each interactive element must have a meaningful semantic label.

Example conceptual labels:

Row 1, Column 1
Row 1, Column 2
...

However, if the interaction model is answer-selection rather than direct grid manipulation, only the actual interactive answer controls should be exposed.

Do not expose non-interactive visual elements as buttons.

38. Crowd Call Accessibility

Crowd Call must expose:

question
answer options
selected answer
crowd result
prediction result

The answer options must have meaningful labels.

Selection must be exposed semantically.

39. Crowd Distribution Accessibility

A visual distribution such as:

A — 62%
B — 24%
C — 14%

must not be communicated only through bar lengths or colors.

Assistive technology should receive meaningful text/value information.

Example conceptual semantic representation:

Option A: 62 percent
Option B: 24 percent
Option C: 14 percent
40. Prediction Result Accessibility

The result should expose a clear statement.

Example:

Your prediction matched the crowd's top choice.

or the appropriate approved result.

Do not require the user to infer the result from color or animation.

41. Challenge Progress

Gameplay progress should be understandable without relying solely on:

dots
progress color
animation

The user should be able to understand the current position in the battle.

Conceptually:

Challenge 1 of 3

is accessible information.

42. Timer Accessibility

If a gameplay timer is visible, it should have an accessible representation.

Do not repeatedly announce every timer update to a screen reader.

The accessibility implementation should avoid excessive announcements.

Exact timer announcement behavior is pending.

43. Rapidly Changing Content

Do not automatically announce every visual change.

Potentially problematic:

Score:
0
1
2
3
...
901

or:

Timer:
3.0
2.9
2.8
2.7
...

Assistive technology should receive meaningful state changes rather than every frame/update.

44. Live Regions / Announcements

Use announcements only for meaningful events.

Potential examples:

Challenge complete
Score: 287
Battle complete
Personal best

Avoid unnecessary announcements for:

decorative animation
every score increment
every timer tick
every recomposition

Exact announcement implementation is technical.

45. Haptic Independence

Haptics are optional feedback.

If haptics are disabled or unavailable:

Gameplay remains understandable.

Do not make haptics the only indication of:

correct
incorrect
battle complete
Personal Best
46. Sound Independence

Sound is optional feedback.

If sound is disabled or unavailable:

Gameplay remains understandable.

Do not make sound the only indication of an important state.

47. Visual Feedback Independence

Visual feedback remains primary.

However, important information should still be semantically available to assistive technologies.

48. Combined Feedback Rule

For important states, use multiple channels where appropriate:

Correct
├── Visual
├── Semantic
├── Optional Haptic
└── Optional Sound

Incorrect:

Correct
└── Green color only
49. Reduced Motion

Daily Battle should respect platform/user reduced-motion preferences where supported.

When reduced motion is enabled:

remove unnecessary decorative animation
shorten non-essential transitions
avoid continuous motion
skip non-essential score-counting animation
preserve state feedback
preserve gameplay functionality
50. Reduced Motion Must Not Remove Meaning

Do not implement reduced motion as:

No animation
+
No feedback

Instead:

Reduced motion
↓
Less movement
+
Same information
51. Accessibility and Score Reveal

Normal:

0
 ↓
901
 ↓
Percentile
 ↓
Improvement
 ↓
Rival

Reduced motion:

901
 ↓
Percentile
 ↓
Improvement
 ↓
Rival

The hierarchy remains intact.

Only unnecessary animation is reduced.

52. Typography Accessibility

The established type hierarchy should remain consistent.

Do not solve accessibility by randomly increasing:

font weight
letter spacing
font size

on individual screens.

Use the design system as the source of truth.

53. Contrast

Text and interactive elements must have sufficient contrast against their backgrounds.

The established color system includes:

Background
#080B10

Surface 1
#11161F

Surface 2
#171D28

Elevated
#1C2330

Primary Text
#F5F7FA

Secondary Text
#A2AAB8

Muted Text
#6F7887

Disabled Text
#4D5563

These colors must be validated for their intended usage.

Do not use muted/disabled colors for important information if contrast becomes insufficient.

54. Disabled Text

Disabled content may use the disabled color only when it is genuinely non-interactive/unavailable.

Do not use disabled styling for:

secondary but important information
legal text that must be readable
critical instructions
score information
55. Brand and Challenge Colors

Brand:

#7C5CFC

Challenge accents:

Snap       #4EA8FF
Shift      #FFB84D
Crowd Call #39D98A

These must not be used as the sole indicator of state.

56. Focus Visibility

Interactive elements must have a visible focus/selection state where keyboard, accessibility, or other non-touch navigation can produce focus.

Focus must not disappear merely because the visual design was optimized for touch.

57. Keyboard / External Input
Status

PENDING

The product is designed primarily for touch interaction.

Support for:

hardware keyboard
external accessibility switch
external controller

has not been formally specified.

Do not invent gameplay behavior for these input methods.

However, standard Android accessibility semantics should not be intentionally broken.

58. Orientation

The master design targets portrait Android UI.

Exact orientation policy is a technical/product decision.

If portrait-only is approved:

layout must remain stable
no landscape-specific accidental UI should appear

Do not add landscape gameplay layouts without approval.

59. Small Screen Accessibility

Master validation includes:

390 × 844
412 × 915
360 × 800

The smallest supported validation target must be tested for:

text clipping
CTA visibility
touch target overlap
score clipping
bottom navigation
gameplay controls
error messages
60. Large Text on Small Screens

When text scaling causes insufficient space:

Priority should be:

1. Critical content
2. Primary action
3. User input
4. Supporting content
5. Decorative content

Do not hide critical controls to preserve decorative layout.

61. Safe Area

The interface must respect system UI and safe areas.

Accessibility content must not be:

hidden behind status bars
hidden behind navigation/gesture areas
clipped by bottom navigation
62. Scroll Behavior

When content becomes larger than the available viewport:

allow appropriate scrolling
preserve logical focus order
ensure focused elements can be brought into view
do not trap focus
63. Settings Accessibility

Settings controls must expose:

setting name
current state
action/state change

Example:

Haptics
On

The semantic representation must communicate both:

What setting?
Current value?

not only:

Switch
64. Battle Name Accessibility

The Battle Name input must provide:

accessible label
input purpose
current value
validation state
error message if applicable

Placeholder text must not be the only label.

65. Battle Code Accessibility

The Battle Code screen must support:

reading the code
copying the code
entering a code
validation feedback

The copy action must have an accessible semantic label.

66. Error Message Association

Validation errors should be associated with the relevant control where technically possible.

Example:

Battle Code
[ ABC123 ]

Invalid battle code.

The user should not need to guess which input caused the error.

67. Friends Accessibility

Friends rows must expose:

friend's name
relevant score/status
available action

Do not make an entire complex row an ambiguous single accessibility node if multiple actions exist.

68. Rival Accessibility

The Rival screen should clearly expose:

Rival name
Rival score
Your score
Score gap
Available action

Do not require visual comparison alone.

69. Profile Accessibility

Profile information should be readable in logical order.

Battle DNA dimensions such as:

Speed
Memory
People

must have accessible labels and values.

Do not expose a visual chart without an equivalent textual/value representation.

70. History Accessibility

History charts must have an accessible alternative.

If a graph visually shows score progression, the underlying data should remain accessible as text/value information.

Do not make:

graph shape

the only way to understand historical performance.

71. Decorative Motion

Continuous decorative motion should be avoided because it:

adds cognitive noise
may distract users
increases battery use
creates reduced-motion complexity

This reinforces the motion specification.

72. Accessibility and Cognitive Load

Daily Battle is intentionally a short, focused experience.

Avoid unnecessary:

instructions
repeated explanations
modal interruptions
confirmation dialogs
animations
competing CTAs

Accessibility should improve clarity, not increase cognitive load.

73. One Dominant Job

The existing product architecture defines:

Home       → Play
Snap       → React
Shift      → Remember
Crowd Call → Predict
Results    → Understand performance
Rival      → Beat someone
Friends    → Manage rivals
Profile    → Understand yourself
History    → See progress

Accessibility must preserve this single dominant job.

Do not add accessibility UI that obscures the primary task.

74. Gameplay Cognitive Clarity

Gameplay instructions should be:

concise
directly actionable
visible/available when needed
semantically accessible

Avoid forcing the user to remember long instructions while playing.

75. No Accessibility Feature Should Change Product Meaning

Accessibility implementation must not silently change:

official score
challenge order
official attempt count
Battle result
rival result
percentile
gameplay rules

unless a formally approved accessibility gameplay mode defines such a change.

76. Accessibility Gameplay Modes
Status

PENDING

The product has not yet defined dedicated accessibility gameplay modes.

Potential future considerations:

extended timing
alternative input
simplified interaction
visual timing assistance

These MUST NOT be implemented automatically.

If introduced, each mode requires:

product definition
scoring definition
eligibility definition
UI definition
backend implications
analytics implications
QA coverage
77. No Hidden Accessibility Shortcuts

Do not add undocumented gestures or alternate interactions such as:

double tap
long press
swipe
multi-touch

unless they are part of the approved interaction model.

Unexpected gestures can create accessibility conflicts.

78. Screen Reader Testing

QA should test major screens with Android screen-reader functionality enabled.

Verify:

screen title
focus order
button labels
selected states
disabled states
error messages
score
gameplay instructions
navigation
settings
79. Accessibility Testing Matrix
Area	Test
Text	Large text
Contrast	Light/dark surface combinations
Touch	Target size
Screen reader	Labels/order
Focus	Logical traversal
Color	Color-independent states
Motion	Reduced motion
Sound	Sound disabled
Haptics	Haptics disabled
Gameplay	Feedback remains understandable
Errors	Error text accessible
Loading	Loading state understandable
Charts	Text alternative
Scores	Final score accessible
Navigation	Selected destination exposed
80. Screen-by-Screen QA
Welcome

Verify:

title readable
CTA accessible
focus order logical
no decorative element receives focus
Battle Name

Verify:

input label
keyboard behavior
validation
error association
CTA semantics
Home

Verify:

Today’s Battle is understandable
PLAY BATTLE accessible
Momentum readable
Rival information readable
bottom navigation accessible
Battle Intro

Verify:

battle title
challenge count
duration
official attempt
start CTA
Snap

Verify:

instructions
challenge state
result feedback
progress
timing semantics where supported
Shift

Verify:

grid/interaction semantics
answer options
result state
progress
Crowd Call

Verify:

question
answer options
selection
crowd distribution
prediction result
Results

Verify:

final score
percentile
improvement
breakdown
rival comparison
CTA labels
Rival

Verify:

rival identity
scores
gap
action
Friends

Verify:

friend identity
score
status
actions
add friend
Add Friend

Verify:

Battle Code label
input
copy action
validation
success/error state
Profile

Verify:

Battle DNA values
records
score values
chart alternatives
History

Verify:

graph alternative
score values
date information
ordering
Settings

Verify:

setting labels
current states
switches
legal/about rows
81. Accessibility and Offline/Error States

Accessibility applies to failure states too.

Example:

Couldn't load friends.
Retry

must expose both:

error message
retry action

to assistive technologies.

Do not make error recovery visual-only.

82. Accessibility and One-Shot Feedback

Important one-shot events should have accessible equivalents.

Examples:

Battle complete
Personal best
Challenge complete

The implementation must avoid requiring animation to finish before the information becomes available.

83. Accessibility and Haptic Failure

If the device does not support a particular haptic:

The product must continue normally.

Never block interaction because:

haptic unavailable
84. Accessibility and Sound Failure

If sound cannot play:

The product must continue normally.

Never make audio playback a prerequisite for:

challenge completion
battle completion
score display
85. Accessibility and Network Failure

Network failures must remain understandable without:

vibration
sound
animation

The user must still see or otherwise receive a meaningful error/recovery state.

86. Accessibility and Animations

Animations must not:

hide important information
delay access to important information
require precise visual tracking when avoidable
repeatedly move focus
create unnecessary announcements
87. Accessibility Performance

Accessibility features must not significantly degrade:

gameplay responsiveness
navigation
animation performance
input handling

However:

Accessibility takes priority over decorative animation.

If there is a conflict, remove unnecessary visual complexity rather than degrading accessibility.

88. Developer Implementation Rules

Antigravity MUST:

use semantic labels
expose meaningful selected/disabled states
preserve logical focus order
support appropriate touch targets
avoid color-only communication
support reduced-motion behavior
provide textual/value alternatives for charts
provide accessible score values
keep error recovery accessible
keep haptic/sound optional
test accessibility during implementation

Antigravity MUST NOT:

use color as the sole state indicator
make icons unlabeled
expose decorative images as interactive
expose hidden controls to accessibility services
hide critical information behind animation
rely solely on haptics
rely solely on sound
create inaccessible custom controls
add undocumented accessibility gestures
silently change gameplay scoring
89. Accessibility Review Checklist

Before a screen is accepted:

Content
 Screen purpose is clear
 Important text is readable
 Labels are meaningful
 No critical information exists only visually
Interaction
 Touch targets meet minimum
 Controls have semantic roles
 Selected state is exposed
 Disabled state is exposed
 Focus order is logical
Visual
 Contrast is acceptable
 Color is not the sole state indicator
 Text does not clip
 Important information remains visible at larger text sizes
Motion
 Reduced motion considered
 No critical information depends on animation
 Focus is not disrupted
Audio/Haptics
 Sound is optional
 Haptics are optional
 Visual/semantic fallback exists
Errors
 Error is understandable
 Error is accessible
 Recovery action is accessible
90. Accessibility Definition of Done

Accessibility implementation is complete only when:

Visual
sufficient contrast verified
color-independent states verified
typography verified
large-text behavior verified
small-screen behavior verified
Interaction
touch targets verified
focus order verified
selected/disabled states verified
semantic labels verified
Screen Reader
major screens tested
gameplay instructions tested
score tested
result tested
error states tested
Gameplay
Snap accessibility reviewed
Shift accessibility reviewed
Crowd Call accessibility reviewed
progress accessible
feedback accessible
timer behavior reviewed
Motion
reduced motion supported
critical information available without animation
Feedback
sound can be disabled
haptics can be disabled
visual/semantic fallback exists
QA
accessibility test matrix completed
issues documented
deviations documented
implementation report completed
91. Pending Accessibility Decisions
ID	Decision	Status
ACCESS-PENDING-001	Exact accessibility timing behavior for Snap	Pending
ACCESS-PENDING-002	Whether accessibility timing changes scoring	Pending
ACCESS-PENDING-003	Exact timer screen-reader announcement behavior	Pending
ACCESS-PENDING-004	Dedicated accessibility gameplay mode	Pending
ACCESS-PENDING-005	Extended-time gameplay mode	Pending
ACCESS-PENDING-006	Alternative gameplay input methods	Pending
ACCESS-PENDING-007	Exact reduced-motion implementation	Pending
ACCESS-PENDING-008	Hardware keyboard support	Pending
ACCESS-PENDING-009	External controller support	Pending
ACCESS-PENDING-010	Exact portrait/orientation policy	Pending
ACCESS-PENDING-011	Chart semantic representation implementation	Pending
92. Final Accessibility Rule

Daily Battle must follow:

VISUAL INFORMATION
        +
SEMANTIC INFORMATION
        +
OPTIONAL HAPTIC
        +
OPTIONAL SOUND

Important information must never depend on only one of these channels.

The user must be able to:

Understand
   ↓
Interact
   ↓
Receive feedback
   ↓
Recover from errors

without requiring a specific sensory feedback channel.

Accessibility is part of the product quality bar, not a post-development patch.