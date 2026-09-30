# DAILY BATTLE — DESIGN SYSTEM

Version: 1.0
Status: Product Design Contract
Platform: Android
Master Frame: 390 × 844 px
Primary Typeface: Inter
Spacing Base: 8 px
Design Mode: Dark-first MVP

---

# 0. PURPOSE

This document defines the visual and interaction system used throughout Daily Battle.

It converts the product's visual direction into reusable rules for:

- colors
- typography
- spacing
- sizing
- radius
- borders
- elevation
- buttons
- inputs
- navigation
- cards
- game components
- score components
- social components
- feedback components
- states
- accessibility
- responsive behavior
- motion
- haptics
- sound
- implementation naming

The purpose is consistency.

A screen must not invent its own:

- color
- font size
- spacing
- radius
- button height
- icon family
- state behavior
- interaction language

unless the deviation is explicitly approved.

---

# 1. DESIGN NORTH STAR

Daily Battle is:

> Premium interactive product with gaming energy.

Target relationship:

```text
75–80%
Premium product design

20–25%
Game energy

The UI must NOT become:

generic esports
cyberpunk
casino
childish cartoon UI
AI-purple-gradient UI
excessive glassmorphism
giant glowing borders
dashboard-heavy analytics UI
social-media feed UI
fake scientific brain-training UI

The game itself may be expressive.

The surrounding product must remain restrained.

2. DESIGN PERSONALITY

The visual system should communicate:

Fast

The user understands the interface immediately.

Competitive

Scores and rivalry feel meaningful.

Premium

No cheap gradients, clutter, or excessive neon.

Intelligent

Typography and information structure feel deliberate.

Playful

Gameplay may use expressive motion.

Social

Friends and rivals feel personal rather than like a generic leaderboard.

3. MASTER DEVICE SYSTEM
3.1 Primary frame
390 × 844 px

This is the master design frame.

3.2 Validation frames
412 × 915 px
360 × 800 px

The design must work on both without requiring a separate layout.

4. SAFE AREA SYSTEM
4.1 Horizontal margin

Default:

20px

Major hero sections may use:

24px
4.2 Top safe area

Approximately:

24px + system status area

Important content must not collide with system UI.

4.3 Bottom navigation

When visible:

72–80px

Reserve sufficient content space above it.

4.4 Gameplay

Gameplay screens:

NO BOTTOM NAVIGATION

Also remove:

profile controls
friend scores
recent history
social controls

Gameplay gets the entire visual field.

5. GRID / SPACING SYSTEM

Daily Battle uses an:

8px spacing system

Approved spacing values:

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
5.1 Primary spacing

Most UI should primarily use:

16
20
24
32
5.2 Avoid arbitrary values

Do not introduce random spacing such as:

17
19
23
27
31

unless technically required and approved.

The purpose of the spacing system is visual discipline.

6. COLOR SYSTEM
6.1 Background
Background Primary
#080B10

Usage:

app background
gameplay background
primary screen canvas
6.2 Surfaces
Surface 1
#11161F

Usage:

standard cards
navigation surfaces
grouped content
Surface 2
#171D28

Usage:

elevated interactive surfaces
secondary cards
selected/interactive containers where appropriate
Surface Elevated
#1C2330

Usage:

elevated elements
overlays
high-priority interactive surfaces
7. TEXT COLOR SYSTEM
7.1 Primary text
#F5F7FA

Use for:

headings
primary labels
important scores
primary actions
7.2 Secondary text
#A2AAB8

Use for:

supporting information
descriptions
secondary labels
7.3 Tertiary / muted text
#6F7887

Use for:

low-priority metadata
supporting context

Do not use for important information.

7.4 Disabled text
#4D5563

Use only for genuinely disabled content.

8. BRAND COLOR
Primary Brand Violet
#7C5CFC

This is the primary interactive brand color.

Use for:

primary buttons
active navigation
selected states
progress
score emphasis
focus indicators
interactive accents
8.1 Purple restraint rule

Purple must NOT dominate every screen.

The app should remain:

Dark
+
restrained
+
premium

not:

Purple everywhere
+
glow everywhere
9. SEMANTIC COLORS
Success
#39D98A

Use for:

correct
success
personal-best confirmation
successful completion
Warning
#FFB84D

Use for:

warning
attention
Shift challenge accent
Error
#FF5D73

Use for:

incorrect
errors
destructive feedback
Info
#4EA8FF

Use for:

informational states
Snap challenge accent
10. CHALLENGE COLOR SYSTEM

Each challenge has a subtle visual identity.

Snap
#4EA8FF

Character:

Fast
Reactive
Focused
Shift
#FFB84D

Character:

Precise
Structured
Memory
Crowd Call
#39D98A

Character:

Social
Predictive
Expressive
Challenge color rule

Challenge colors should support recognition.

They must NOT:

overpower the screen
become full-screen neon
replace accessibility indicators
be used as arbitrary decoration elsewhere
11. BORDER SYSTEM

Default border:

#252D39

Use subtle borders when required for:

cards
inputs
selected states
separators
navigation

Avoid heavy borders.

Avoid glowing borders.

12. GRADIENT RULE

Gradients are not a primary design mechanism.

Use:

Minimal gradients

Only when explicitly justified by the screen design.

Do not create:

large purple gradients
AI-style gradient backgrounds
rainbow gradients
glowing gradient borders
13. SHADOW / ELEVATION

The visual system favors minimal shadows.

Do not create heavy floating-card shadows.

Elevation should primarily come from:

surface color
contrast
subtle border
limited shadow where necessary
14. TYPOGRAPHY

Primary typeface:

Inter

Use one primary type family.

Do not introduce additional typefaces without approval.

15. TYPE SCALE
Display
56px / 60px
Weight: 700

Use for:

very large score / hero moments
major visual emphasis

Only use when the screen can support the size without clutter.

H1
30px / 36px
Weight: 700

Use for:

major screen headings
important titles
H2
24px / 30px
Weight: 700

Use for:

section headings
major content blocks
H3
18px / 24px
Weight: 600

Use for:

card headings
subsections
important row titles
Body Large
16px / 24px
Weight: 500

Use for:

important supporting copy
large instructions
Body
14px / 20px
Weight: 500

Use for:

standard supporting text
list content
secondary information
Caption
12px / 16px
Weight: 500

Use for:

metadata
supporting labels
Label
11–12px / 16px
Weight: 600

Use carefully.

Important UI must not depend on very small labels.

16. MINIMUM TEXT SIZE

Minimum important UI text:

12px

Do not create dense interfaces with tiny labels.

If removing a label makes the interface understandable:

REMOVE THE LABEL.
17. NUMERIC TYPOGRAPHY

Scores should use:

Tabular numerals

This ensures values align cleanly.

Important numeric information includes:

total score
challenge score
rival score
percentile
average
personal best
momentum counts
18. BUTTON SYSTEM

Primary button height:

52px

Horizontal padding:

20–24px

Text:

15px
Weight: 700
19. BUTTON TYPES
Primary

Purpose:

Main action.

Examples:

PLAY BATTLE
START BATTLE
BEAT RAHUL
ADD FRIEND
SHARE RESULT

Visual:

brand violet background
primary text
strong contrast
52px height
Secondary

Purpose:

Important but subordinate action.

Use when there are two meaningful actions.

Ghost

Purpose:

Low-emphasis action.

Examples:

COPY
secondary navigation
non-primary utility actions
Icon button

Purpose:

Compact utility action.

Examples:

Back
Close
Copy
Settings
More

Minimum touch target:

44 × 44px
20. BUTTON COPY

Use specific action verbs.

Good:

PLAY BATTLE
BEAT RAHUL
START REMATCH
ADD FRIEND
SHARE RESULT

Avoid vague actions:

Continue
Proceed
Click here
Submit

unless the context genuinely requires them.

21. BUTTON STATES

Every reusable button supports:

Default
Pressed
Disabled
Loading
Success
Default

Normal interactive state.

Pressed

Use short visual feedback.

Target motion:

100–150ms
Disabled

Clearly distinguish from enabled.

Do not rely only on opacity.

Loading

Preserve button dimensions.

Do not cause layout jumping.

Success

Use only where success is meaningful.

Do not keep success state indefinitely.

22. INPUT SYSTEM

Standard input height:

52px

Recommended states:

Default
Focused
Valid
Invalid
Disabled

Input should include:

clear label
clear focus state
readable text
validation feedback where required

Do not communicate validity only through color.

23. CARD SYSTEM

Major cards:

16px radius

Smaller cards:

12px radius

Cards should exist because they improve information grouping.

Do not turn every piece of content into a card.

24. BATTLE CARD

Used primarily on Home.

Structure:

TODAY'S BATTLE

3 challenges
~3 minutes

● ● ●

[ PLAY BATTLE ]

The Battle Card is the dominant Home component.

Approximate meaningful viewport occupation:

50–55%
25. RIVAL CARD

Purpose:

Show the user's current personal competition.

Example:

YOUR RIVAL

Rahul       914
You         901

13 points to catch

[ BEAT RAHUL ]

Rival card must remain secondary to Today's Battle on Home.

26. RESULT CARD

Purpose:

Present performance information.

Possible content:

SNAP
287 / 300

SHIFT
252 / 300

CROWD CALL
271 / 300

Do not overload it with unnecessary metadata.

27. STAT COMPONENT

A stat must have context.

Bad:

927

Better:

BEST SCORE
927

or:

927 / 1000

Never create unexplained numeric blocks.

28. FRIEND ROW

Used in:

Friends
Rival
competition contexts

States:

Normal
You
Rival
Pending

Example:

Rahul                  914
Sanket                 901   YOU

The user's row must remain identifiable.

29. NAVIGATION SYSTEM

Bottom navigation contains exactly four destinations:

Home
Battle
Friends
Me
30. NAVIGATION ICONS

Recommended icon family:

Lucide

or:

Material Symbols Rounded

Use one consistent family.

Do not mix multiple unrelated icon families.

Suggested icon concepts

Home:

House

Battle:

Lightning / Target

Friends:

Users

Me:

Profile

Exact icon selection can be finalized during component implementation.

31. ICON SIZES

Standard:

20px
24px

Large game icons may exceed this where gameplay requires it.

Do not arbitrarily scale standard UI icons to large sizes.

32. BOTTOM NAVIGATION

Approximate height:

72–80px

Background:

#080B10

or a slightly elevated surface where necessary.

Battle tab should be active/emphasized when appropriate.

Explicitly prohibited

Do not use:

Giant glowing circular Battle button

Do not make Battle look like a disconnected floating action button.

33. TOP BAR

Top bar may contain:

title
back action
progress
timer
contextual action

Do not place unnecessary controls into the top bar.

Gameplay top area should remain focused.

34. BACK BUTTON

Standard touch target:

≥44 × 44px

Recommended icon size:

24px

Back behavior must follow the navigation specification.

Do not invent destructive gameplay behavior.

35. GAMEPLAY HEADER

Gameplay screens use:

Challenge
+
Progress
+
Timer

Example:

SNAP                 1 / 3

18

Exact timer placement and timer value belong to gameplay specification.

36. PROGRESS DOTS

Use progress dots to communicate:

1 / 3
2 / 3
3 / 3

Example:

● ○ ○

or equivalent accessible representation.

Progress must not depend solely on color.

37. GAMEPLAY CONTENT STRUCTURE

Gameplay screen hierarchy:

TOP
Challenge + progress + timer

MIDDLE
Game

BOTTOM
Instruction + current score

This is the standard gameplay composition.

38. TIMER

Timer is a gameplay component.

It should be:

immediately readable
visually secondary to the actual challenge
consistent between game screens

Do not create oversized decorative timers.

Exact timer behavior is defined by gameplay requirements.

39. SCORE COMPONENT

Score must always have context.

Examples:

240 / 300
901 / 1000

For gameplay:

Score
240 / 300

For result:

901 / 1000

Use tabular numerals.

40. SCORE REVEAL

Result score reveal duration:

700–1000ms

Sequence:

Today's Result
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

Motion should be sequential and controlled.

41. PERSONAL BEST COMPONENT

When the user sets a new record:

NEW PERSONAL BEST

927

+23

Visual treatment:

subtle glow
small particles
success feedback
stronger success haptic

Avoid:

full-screen fireworks
excessive confetti
giant celebration overlays
42. ANSWER CARD

Used by:

Shift
Crowd Call

Variants:

Default
Pressed
Selected
Correct
Incorrect
Answer card rules

Must:

be comfortably tappable
clearly communicate state
preserve readability
support accessibility

Target touch height:

52–56px

where applicable.

43. ANSWER CARD — ACCESSIBILITY

Selected state must not rely only on color.

Possible combination:

Border
+
Check indicator
+
Background change

Correct:

Success indicator
+
visual state

Incorrect:

Error indicator
+
visual state
44. GRID CELL

Used by Shift.

Grid cells must:

remain comfortably tappable
maintain visual consistency
preserve clear spacing
avoid unnecessary decoration

The grid is the visual center of Shift.

Exact grid dimensions belong to gameplay requirements.

45. FEEDBACK SYSTEM

Feedback communicates:

Change
Feedback
Reward

not decoration.

Supported feedback:

Success
Error
Loading
Empty
Toast
46. SUCCESS FEEDBACK

Use:

success semantic color
subtle motion
appropriate iconography
appropriate haptic

Do not use large intrusive overlays.

47. ERROR FEEDBACK

Use:

error semantic color
clear text
appropriate iconography
short error haptic where gameplay requires it

Do not rely solely on red color.

48. TOAST

Toast should be:

short
contextual
non-blocking
easy to understand

Do not use Toast for information that requires prolonged user attention.

49. LOADING SYSTEM

Preferred:

Subtle skeleton

or:

Small contextual loading indicator

Avoid:

Loading...
Loading...
Loading...

Do not use a giant spinner unless technically necessary.

50. EMPTY STATE

Never show a blank screen.

Structure:

WHAT IS MISSING?

WHY DOES IT MATTER?

WHAT CAN I DO?

Example:

NO RIVALS YET

Add a friend and start
your first Battle rivalry.

[ ADD FRIEND ]
51. ERROR STATE

Standard structure:

COULDN'T LOAD [CONTENT]

Check your connection
and try again.

[ RETRY ]

Do not automatically throw the user back to onboarding.

52. OFFICIAL / PRACTICE BADGES

Official:

OFFICIAL BATTLE

Practice:

PRACTICE

These states must be visually distinguishable.

Practice must never appear to affect the official score.

53. STATUS LANGUAGE

Allowed MVP status language:

READY
TODAY
PRACTICE
OFFICIAL

Do not use:

LIVE

because Daily Battle MVP does not contain real-time multiplayer.

54. SOCIAL COMPONENTS

Daily Battle social UI is competition-focused.

Supported concepts:

Friend Row
Rival Row
Challenge Card
Share Card

Not supported in MVP:

Likes
Followers
Comments
Posts
Chat
Infinite Feed
55. SHARE CARD

Visual structure:

DAILY BATTLE

901

TOP 9%

Can you beat me?

[ PLAY ]

Keep it simple.

Do not create a giant screenshot of the application UI.

56. PROFILE / BATTLE DNA COMPONENTS

Profile may contain:

Player Name
Momentum
Best Score
Average
Battle DNA
Personal Records

Battle DNA categories currently defined:

Speed
Memory
People

Do not add IQ or scientific intelligence claims.

57. HISTORY COMPONENTS

History should contain:

Score graph
Best
Average
Momentum
Recent battles

One graph is sufficient.

Do not turn History into an analytics dashboard.

58. SETTINGS COMPONENTS

Settings uses grouped rows.

Groups:

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

Settings should be the calmest screen in the application.

59. SETTINGS VISUAL RULE

Do not use:

large promotional cards
giant hero areas
excessive game decoration
score visualizations
glowing effects

Prefer:

Grouped rows
+
simple separators
+
clear hierarchy
60. RADIUS SYSTEM

Approved radius values:

8px
12px
16px
24px
999px
Usage
Small controls
8–12px
Inputs
12px
Small cards
12px
Major cards
16px
Major visual containers
24px
Pills / chips
999px
61. CARD PADDING

Default card padding should use the spacing system.

Preferred:

16px
20px
24px

Do not create arbitrary padding values.

62. DIVIDERS

Use dividers sparingly.

Default divider:

#252D39

Dividers should support grouping rather than visually fragmenting the screen.

63. TOUCH TARGETS

Minimum:

44 × 44px

Preferred:

48–52px

Primary controls:

52px height

Fast gameplay must receive special attention to touch sizing.

64. RESPONSIVE SYSTEM
64.1 360px

Do NOT drastically shrink typography.

Instead:

reduce horizontal gaps
reduce card padding
allow text wrapping
stack content where necessary
64.2 390px

Master design.

390 × 844
64.3 412px

Use:

increased breathing room
preserved hierarchy
preserved component sizes where possible

Do not simply scale everything upward.

65. GAME RESPONSIVENESS
Snap

Game area scales proportionally.

Shift

Grid cells remain comfortably tappable.

Crowd Call

Answer cards remain approximately:

52–56px

touch height where applicable.

66. ACCESSIBILITY SYSTEM

Never communicate state through color alone.

Use combinations of:

Color
+
Shape
+
Icon
+
Text
+
Motion

where appropriate.

67. CONTRAST

Primary text:

#F5F7FA

must remain strongly readable against:

#080B10
#11161F
#171D28
#1C2330

Secondary and muted text must only be used for information that can safely be less prominent.

68. ACCESSIBLE STATE EXAMPLES
Selected
Purple border
+
Check icon
+
Background change
Correct
Success color
+
Correct indicator
+
Optional motion
Incorrect
Error color
+
Incorrect indicator
+
Short feedback

Never:

Color only
69. MOTION SYSTEM

Motion exists to communicate:

Change
Feedback
Reward

Not decoration.

70. MOTION DURATIONS
Button press
100–150ms
Normal navigation
200–300ms
Game transition
250–400ms
Score reveal
700–1000ms
71. HOME → BATTLE MOTION

The Today's Battle card may:

Slightly expand / zoom
        ↓
Fade into gameplay

The transition should make entering the Battle feel distinct.

Do not make it cinematic or slow.

72. BETWEEN-CHALLENGE MOTION

After Snap:

SNAP COMPLETE
287 / 300

Then automatically transition to:

SHIFT

No manual Continue button.

Same principle applies to:

SHIFT → CROWD CALL
CROWD CALL → RESULTS

where gameplay requirements permit.

73. HAPTIC SYSTEM
Button
Light
Correct
Light success
Wrong
Short error
Personal Best
Stronger success
Battle Complete
Medium confirmation

Haptics should never be excessive.

74. SOUND SYSTEM

No continuous background music in MVP.

Supported sound events:

Tap
Correct
Wrong
Score Reveal
Personal Best
Battle Complete

User controls:

Sound ON / OFF
Haptics ON / OFF
75. ICONOGRAPHY RULES

Use one consistent icon family.

Approved direction:

Lucide

or:

Material Symbols Rounded

Do not mix unrelated icon families.

76. ICON WEIGHTS

Icons should visually match the weight of surrounding typography.

Avoid:

overly heavy icons
childish icons
detailed illustrations inside standard controls
mismatched stroke styles
77. ILLUSTRATION RULE

Illustration is optional.

When used:

keep it subtle
support the task
do not dominate the screen
do not turn empty states into giant illustrations
78. VISUAL DENSITY

Daily Battle must avoid information overload.

The design should aggressively remove:

unnecessary badges
redundant metadata
tiny labels
excessive cards
repeated descriptions
unnecessary charts

Ask:

Can the user understand this screen without this element?

If yes:

REMOVE IT.
79. CARD USAGE RULE

Cards are not the default container for everything.

Use a card when it:

groups related information
creates hierarchy
separates interactive content
establishes a meaningful surface

Do not create nested cards simply because content needs structure.

80. SCREEN-SPECIFIC VISUAL CHARACTER
Home
Restrained dashboard
Snap
Fast
Clean
Focused
Shift
Precise
Structured
Grid-dominant
Crowd Call
Social
Expressive
Predictive
Results
Emotional
Score-led
Rewarding
Rival
Personal
Competitive
Focused
Friends
Competition network
Profile
Personal identity
History
Progress
Settings
Calm
Functional
Minimal
81. INFORMATION HIERARCHY

Every screen follows:

Level 1

What should I do?

Level 2

What matters to me?

Level 3

Supporting information.

Example Home:

Level 1
Today's Battle

Level 2
Rival

Level 3
Momentum / supporting statistics

Never make every element visually equal.

82. COMPONENT NAMING

Implementation components should use predictable names.

Recommended:

DBButton
DBTextField
DBTopBar
DBBottomNav
DBBattleCard
DBRivalCard
DBResultCard
DBFriendRow
DBAnswerCard
DBProgressDots
DBTimer
DBScore
DBFeedback
DBToast
DBEmptyState
DBErrorState
DBLoadingState

Gameplay:

DBSnapGame
DBShiftGame
DBCrowdCallGame

This naming convention is intended to make implementation traceable.

83. DESIGN TOKEN NAMING

Recommended structure:

Color/
  background/primary
  surface/1
  surface/2
  surface/elevated
  text/primary
  text/secondary
  text/muted
  text/disabled
  brand/primary
  semantic/success
  semantic/warning
  semantic/error
  semantic/info
  challenge/snap
  challenge/shift
  challenge/crowd
  border/default
84. SPACING TOKENS
Spacing/
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
85. RADIUS TOKENS
Radius/
  8
  12
  16
  24
  999
86. SIZING TOKENS

Recommended:

Touch/
  minimum = 44
  preferred = 48

Button/
  height = 52

Input/
  height = 52

Screen/
  margin = 20
87. COMPONENT VARIANT MATRIX
Button
Type:
Primary
Secondary
Ghost
Icon

State:
Default
Pressed
Disabled
Loading
Success
Answer Card
State:
Default
Pressed
Selected
Correct
Incorrect
Friend Row
State:
Normal
You
Rival
Pending
Battle Card
State:
Ready
Completed
Loading
Error
Input
State:
Default
Focused
Valid
Invalid
Disabled
88. FIGMA ORGANIZATION

The design system should be organized into:

00 — Cover

01 — Foundations

02 — Components

03 — Onboarding

04 — Home

05 — Battle

06 — Result

07 — Rival

08 — Friends

09 — Profile

10 — History

11 — Settings

12 — Prototype

13 — Developer Handoff
89. FOUNDATIONS PAGE

Must contain:

Colors
Typography
Spacing
Grid
Radius
Sizing
Elevation
Iconography
90. COMPONENT PAGE

Must contain reusable:

Buttons
Inputs
Navigation
Cards
Rows
Score
Timer
Progress
Answer Cards
Grid Cells
Feedback
States
91. DESIGN SYSTEM SOURCE OF TRUTH

The following values are locked:

Background:
#080B10

Surface 1:
#11161F

Surface 2:
#171D28

Elevated:
#1C2330

Primary Text:
#F5F7FA

Secondary Text:
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

Border:
#252D39
92. CHALLENGE COLORS — SOURCE OF TRUTH
Snap:
#4EA8FF

Shift:
#FFB84D

Crowd Call:
#39D98A
93. TYPOGRAPHY — SOURCE OF TRUTH
Font:
Inter

Display:
56 / 60 / 700

H1:
30 / 36 / 700

H2:
24 / 30 / 700

H3:
18 / 24 / 600

Body Large:
16 / 24 / 500

Body:
14 / 20 / 500

Caption:
12 / 16 / 500

Label:
11–12 / 16 / 600
94. CORE DIMENSIONS — SOURCE OF TRUTH
Master frame:
390 × 844

Page margin:
20

Major hero margin:
24

Primary button:
52

Input:
52

Touch minimum:
44

Preferred touch:
48–52

Bottom navigation:
72–80

Major card radius:
16

Small card radius:
12

Input radius:
12

Button radius:
See approved component implementation token.

Pill radius:
999
95. BUTTON RADIUS DECISION

The Master UI/UX specification defines:

Buttons:
14px radius

The broader radius system contains:

8
12
16
24
999

Therefore the button radius must remain an explicit implementation token rather than being silently rounded to another value.

Until formally reconciled:

button.radius = 14px

is the source specification value.

Do not silently replace it with 12px or 16px.

96. TYPOGRAPHY DECISION

The Master UI/UX specification defines:

Display:
56 / 60

Any earlier experimental foundation value that differs from this is not authoritative.

For this design-system document:

Display = 56 / 60 / 700

is the source-of-truth value.

97. VISUAL EFFECT RULES

Use restrained:

shadows
gradients
glow
particles
motion

Avoid:

giant glow
neon outlines
excessive blur
glassmorphism everywhere
animated decorative noise

The UI should remain premium.

98. GAME ENERGY RULE

Game energy comes primarily from:

Gameplay
Motion
Feedback
Score reveal
Rivalry
Challenge accents

It does NOT come from:

Neon everywhere
Glow everywhere
Gaming badges everywhere
Decorative explosions
99. NO GLOBAL ECONOMY UI

Do not introduce:

Coins
XP
Gems
Energy
Lives

unless a future product decision explicitly introduces an economy.

MVP rewards are:

Score
Improvement
Rivalry
100. NO GLOBAL RANKING UI

Do not create components for:

Global Rank
Elo
Tier
League
Global Leaderboard

MVP competitive language is:

Percentile
Friends
Rival
Score gap
Personal best
101. NO FAKE LIVE UI

Do not create:

LIVE
LIVE NOW
REAL-TIME

for MVP.

The product is not real-time multiplayer.

Use:

READY
TODAY
OFFICIAL
PRACTICE

depending on context.

102. OFFICIAL BATTLE DESIGN RULE

Official Battle:

Same challenge
+
Same day
+
One official attempt

Visual identity:

OFFICIAL BATTLE

Practice is visually distinct.

103. AI DESIGN RULE

AI should remain infrastructure.

Do not add persistent UI such as:

AI GENERATED
POWERED BY AI
AI CHALLENGE

unless explicitly required by the product.

The user's experience should remain focused on playing.

104. REAL-WORLD MODE DESIGN RULE

Wild Card / real-world mode is not part of MVP.

Do not create its components now.

Future implementation must reuse this design system rather than creating an independent visual language.

105. COMPONENT QUALITY CHECKLIST

Every component must answer:

 What problem does it solve?
 Is it reusable?
 Does it use design tokens?
 Does it have defined states?
 Does it meet touch requirements?
 Does it support accessibility?
 Does it work at 360px?
 Does it work at 412px?
 Does it avoid unnecessary decoration?
 Does it preserve Daily Battle visual identity?
106. SCREEN QUALITY CHECKLIST

Every screen must pass:

Visual
 Dark-first
 Inter typography
 Correct colors
 Correct spacing
 Correct radius
 Minimal shadows
 Minimal gradients
UX
 One dominant job
 Clear hierarchy
 Clear primary action
 No micro-text overload
 Correct navigation
 Correct state behavior
Accessibility
 No color-only state
 Strong text contrast
 ≥44px touch targets
 Important UI ≥12px
 Gameplay controls comfortably tappable
Product
 No global leaderboard
 No coins
 No chat
 No infinite feed
 No IQ claims
 No fake LIVE
 No excessive AI branding
 No unauthorized features
107. DESIGN TOKEN IMPLEMENTATION RULE

In Android implementation:

Do not hardcode repeated visual values throughout the application.

Instead establish centralized tokens for:

Colors
Typography
Spacing
Radius
Sizing
Motion

The implementation layer must map back to this document.

108. TOKEN TRACEABILITY

Recommended mapping:

DBColor.BackgroundPrimary
DBColor.Surface1
DBColor.Surface2
DBColor.SurfaceElevated

DBColor.TextPrimary
DBColor.TextSecondary
DBColor.TextMuted
DBColor.TextDisabled

DBColor.BrandPrimary

DBColor.Success
DBColor.Warning
DBColor.Error
DBColor.Info

DBColor.Snap
DBColor.Shift
DBColor.Crowd

DBSpacing.XS
DBSpacing.SM
DBSpacing.MD
DBSpacing.LG
DBSpacing.XL

DBRadius.Small
DBRadius.Medium
DBRadius.Large
DBRadius.XLarge
DBRadius.Pill

Exact Android naming may be adapted to the selected architecture, but the semantic mapping must remain.

109. DO NOT DUPLICATE DESIGN TOKENS

Do not create screen-specific copies such as:

HomePurple
BattlePurple
ResultPurple
RivalPurple

when they represent the same semantic token.

Use:

BrandPrimary

instead.

110. SEMANTIC OVER RAW VALUES

Prefer:

BrandPrimary

over:

#7C5CFC

inside component definitions.

Prefer:

Spacing20

over:

20

inside layout definitions.

The raw value belongs to the token layer.

111. DESIGN SYSTEM CHANGE CONTROL

Any change to a locked token must be documented.

Example:

DS-001

Token:
BrandPrimary

Current:
#7C5CFC

Proposed:
#XXXXXX

Reason:
...

Affected components:
...

Affected screens:
...

Approval:
Pending / Approved / Rejected

Do not silently change tokens because a screen "looks better."

112. DESIGN SYSTEM DEFINITION OF DONE

The Design System is complete when:

 Colors are tokenized
 Typography is tokenized
 Spacing is tokenized
 Radius is tokenized
 Sizing is tokenized
 Buttons exist in all required states
 Inputs exist in all required states
 Navigation components exist
 Cards exist
 Friend/Rival rows exist
 Score component exists
 Timer exists
 Progress exists
 Answer Card exists
 Grid Cell exists
 Feedback components exist
 Empty/Error/Loading states exist
 Accessibility rules are represented
 Responsive rules are represented
 No unauthorized product components exist
113. ANTIGRAVITY IMPLEMENTATION CONTRACT

Antigravity must use this design system as the visual source of truth.

It may:

create reusable components
map tokens into Android
implement component states
optimize rendering
implement accessibility
implement responsive behavior
create previews/tests

It must NOT:

invent new colors
invent new typography scales
invent arbitrary spacing
invent new navigation
create new visual themes
add unapproved components
introduce a light theme in MVP
replace the icon family without approval
introduce excessive gradients
introduce gaming/cyberpunk styling

If a technical limitation prevents exact implementation:

STOP
REPORT
PROPOSE
WAIT FOR APPROVAL
114. DESIGN SYSTEM AUDIT

Before any screen is considered visually complete, audit:

COLOR
TYPOGRAPHY
SPACING
RADIUS
COMPONENT
STATE
ACCESSIBILITY
RESPONSIVENESS
MOTION
PRODUCT RULES

A screen that looks good but violates the design system is not complete.

115. FINAL VISUAL CONSTITUTION

Daily Battle should consistently feel like:

DARK
+
PREMIUM
+
FAST
+
COMPETITIVE
+
RESTRAINED
+
PLAYFUL DURING GAMEPLAY
+
PERSONAL AFTER THE SCORE

The visual rhythm is:

DASHBOARD
Home
   ↓
IMMERSION
Snap
   ↓
PRECISION
Shift
   ↓
SOCIAL
Crowd Call
   ↓
REWARD
Result
   ↓
COMPETITION
Rival
   ↓
EXIT
Tomorrow

This rhythm is part of the product identity.

END OF DESIGN SYSTEM