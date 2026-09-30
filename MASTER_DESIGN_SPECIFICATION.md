Yes. Ab jo screenshots tumne diye hain, unko dekhne ke baad main **Stitch-generated design ko final reference nahi maanta**. Usne functional structure samajh liya hai, lekin visual system abhi too generic, too dashboard-heavy, aur inconsistent hai.

Isliye pehle **complete master design specification** lock karte hain. Uske baad hum screen-by-screen manually design karenge — Figma mein — aur Stitch ko sirf helper/reference ki tarah use karenge.

# DAILY BATTLE — MASTER UI/UX DESIGN SPECIFICATION

## 0. Product Design North Star

### Product

**Daily Battle**

### Core promise

> **3 challenges. ~3 minutes. One score.**

### Emotional sequence

```text
ANTICIPATION
     ↓
PLAY
     ↓
FOCUS
     ↓
TENSION
     ↓
SCORE REVEAL
     ↓
SELF COMPARISON
     ↓
FRIEND RIVALRY
     ↓
DONE
     ↓
COME BACK TOMORROW
```

The product should feel like a **daily competitive ritual**, not like a game store, social network, or brain-training dashboard.

---

# 1. THE MOST IMPORTANT VISUAL DECISION

## We are NOT making a "gaming dashboard."

Current Stitch output looks like:

> dark dashboard + purple glow + lots of cards + tiny labels

We need:

# **Premium interactive product with gaming energy**

Think of the visual relationship as:

**75–80% premium product**
+
**20–25% game energy**

Not:

**50% gaming + 50% analytics.**

---

# 2. DESIGN PERSONALITY

The product should communicate these six qualities:

### 1. Fast

The user understands the screen immediately.

### 2. Competitive

Scores and rivalry feel meaningful.

### 3. Premium

No cheap gradients, no clutter, no excessive neon.

### 4. Intelligent

Typography and information structure feel deliberate.

### 5. Playful

Games themselves can have expressive motion.

### 6. Social

Friends/rivals should feel personal, not like a generic leaderboard.

---

# 3. WHAT THE PRODUCT SHOULD NOT LOOK LIKE

Absolutely avoid:

- generic esports UI
- cyberpunk UI
- casino UI
- childish cartoon UI
- "AI app" purple-gradient aesthetic
- excessive glassmorphism
- giant glowing borders
- 8–10 tiny stat cards
- dashboard everywhere
- dense information
- social-media feed design
- fake scientific brain-training aesthetic

---

# 4. CANVAS / DEVICE SPECIFICATION

For Figma, use:

# **390 × 844 px**

Primary mobile design frame.

Why 390 × 844?

It gives us a compact modern phone reference while keeping designs adaptable to larger Android devices.

Then validate against:

### Secondary

**412 × 915**

### Small Android

**360 × 800**

The UI must work without redesign.

---

# 5. SAFE AREA

Use:

### Left/right content margin

**20 px**

### Major hero sections

**24 px**

### Top content safe area

Approximately:

**24 px + system status area**

### Bottom

Reserve space for bottom navigation when present.

During gameplay:

**no bottom navigation.**

---

# 6. GRID SYSTEM

Use an:

# **8 px spacing system**

Allowed values:

```text
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
```

Primary spacing:

**16 / 20 / 24 / 32**

Don't randomly use:

17, 19, 23, 27 etc.

This will make the UI visually disciplined.

---

# 7. COLOR SYSTEM

I would slightly refine the current Stitch palette.

## Base

### Background

`#080B10`

Very dark ink.

### Surface 1

`#11161F`

### Surface 2

`#171D28`

### Elevated

`#1C2330`

---

## Typography

### Primary

`#F5F7FA`

### Secondary

`#A2AAB8`

### Tertiary

`#6F7887`

### Disabled

`#4D5563`

---

# 8. Primary brand color

## Electric Violet

`#7C5CFC`

This remains the primary action color.

Use for:

- primary button
- active navigation
- selected state
- progress
- score emphasis
- interactive focus

But:

## Purple must NOT dominate every screen.

---

# 9. Semantic colors

### Success

`#39D98A`

### Warning

`#FFB84D`

### Error

`#FF5D73`

### Info

`#4EA8FF`

These are semantic colors.

Do not use them randomly for decoration.

---

# 10. Challenge accent system

Each game gets a subtle identity.

### Snap

**Electric Blue**

`#4EA8FF`

### Shift

**Amber**

`#FFB84D`

### Crowd Call

**Mint / Teal**

`#39D98A`

Important:

These colors should identify the **challenge**, not turn the whole UI into three different themes.

---

# 11. BORDER SYSTEM

Use:

### Default

`#252D39`

1 px

### Active

Primary purple at controlled opacity.

### Success

Success green at controlled opacity.

No bright 2–3px borders everywhere.

---

# 12. GRADIENT RULE

This is extremely important.

### Allowed

Only subtle gradients in:

- hero Battle card
- score reveal
- major CTA glow
- exceptional state like personal best

### Not allowed

Every card.

Every button.

Every background.

Every heading.

The current Stitch output is too close to the generic "purple AI SaaS" visual language in some places.

---

# 13. TYPOGRAPHY

## Font

# **Inter**

Use one font family throughout.

---

## Scale

### Display

**56 px / 60 px**
Weight 700

For:

> 901

### H1

**30 / 36**
700

### H2

**24 / 30**
700

### H3

**18 / 24**
600

### Body large

**16 / 24**
500

### Body

**14 / 20**
500

### Caption

**12 / 16**
500

### Labels

**11–12 / 16**
600

Do not allow important UI labels below 12 px.

---

# 14. NUMERIC STYLE

Scores are extremely important.

Use:

### Tabular numerals

So:

```text
901
902
903
```

don't visually jump when animated.

Score digits should feel like **game score**, not finance dashboard.

---

# 15. CORNER RADIUS

### Major cards

**16 px**

### Small cards

**12 px**

### Buttons

**14 px**

### Inputs

**12 px**

### Chips

**999 px**

Don't make every element a pill.

---

# 16. ELEVATION

Use depth mostly through:

- surface contrast
- borders
- very subtle shadows

Not giant shadows.

The UI should feel like:

> dark layers

rather than floating neon cards.

---

# 17. CARD PHILOSOPHY

This is where current Stitch needs major improvement.

## Rule:

### A card should contain a meaningful group.

Example:

✅ Today's Battle

✅ Rival

✅ Personal stats

Not:

```text
Card
  └ Card
      └ Card
          └ Card
```

### Maximum:

One primary card can contain grouped rows, but avoid excessive nesting.

---

# 18. BUTTON SYSTEM

## Primary button

Solid purple.

Height:

### 52 px

Horizontal padding:

**20–24 px**

Text:

15 px / 700

Example:

> PLAY BATTLE

---

## Secondary

Dark surface + border.

Example:

> VIEW RESULTS

---

## Tertiary

Text only.

Example:

> BACK

---

# 19. BUTTON COPY

Use verbs.

### Good

**PLAY BATTLE**

**BEAT RAHUL**

**START REMATCH**

**ADD FRIEND**

**SHARE RESULT**

### Bad

**Continue**

**Proceed**

**Click here**

**Submit**

Use specific action language.

---

# 20. ICON SYSTEM

Use a consistent icon family.

Recommended:

### Lucide

or

### Material Symbols Rounded

Don't mix five icon families.

Icon sizes:

**20 px**

**24 px**

Large game icons can go larger.

---

# 21. NAVIGATION SYSTEM

Bottom navigation:

```text
Home
Battle
Friends
Me
```

Exactly four.

### Home

House

### Battle

Lightning / target

### Friends

Users

### Me

Profile

---

# 22. BOTTOM NAV DESIGN

Height:

approximately **72–80 px**

Background:

same background or slightly elevated surface.

No giant glowing circular central button.

Current Stitch's giant center Battle button feels a little like a floating action button.

Instead:

### Battle tab is active and visually emphasized.

---

# 23. GAMEPLAY MODE

When entering Battle:

### Hide bottom navigation.

Also hide:

- profile
- friend score
- recent history
- social controls

Gameplay should occupy the screen.

---

# 24. INFORMATION HIERARCHY

Every screen must follow:

## Level 1

What should I do?

## Level 2

What matters to me?

## Level 3

Supporting information.

Example Home:

### Level 1

Today's Battle

### Level 2

Rival

### Level 3

Momentum / recent stats

Not everything equal.

---

# 25. MASTER INFORMATION ARCHITECTURE

The complete MVP:

```text
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
│   ├── Snap Complete
│   ├── Shift
│   ├── Shift Complete
│   ├── Crowd Call
│   └── Crowd Result
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
```

---

# 26. SCREEN COUNT

There are **13 primary screens**.

### 01

Welcome

### 02

Battle Name

### 03

Home

### 04

Battle Intro

### 05

Snap

### 06

Shift

### 07

Crowd Call

### 08

Results

### 09

Rival

### 10

Friends

### 11

Add Friend

### 12

Profile

### 13

History

Then secondary states:

- Snap complete
- Shift complete
- Crowd result
- Home completed
- Practice launch
- Incoming challenge
- Settings

These are **states/flows**, not necessarily completely separate Figma pages.

---

# 27. USER FLOW

## First session

```text
Install
 ↓
Welcome
 ↓
Choose Battle Name
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
```

---

# 28. HOME — DESIGN SPEC

Home's one purpose:

# **GET THE USER INTO TODAY'S BATTLE**

Structure:

```text
Greeting
Momentum

TODAY'S BATTLE
[PLAY]

YOUR RIVAL
[BEAT RAHUL]
```

That's it.

### Remove from Home:

- detailed charts
- full performance history
- huge leaderboard
- too many statistics

Those belong elsewhere.

---

# 29. HOME CARD

Hero card occupies approximately:

### 50–55% of meaningful viewport height.

Inside:

```text
TODAY'S BATTLE

3 challenges
~3 minutes

● ● ●

[ PLAY BATTLE ]
```

Do not reveal exact challenge types.

---

# 30. HOME RIVAL CARD

Below:

```text
YOUR RIVAL

Rahul      914
You        901

13 points to catch

[ BEAT RAHUL ]
```

The rival should be visually important but clearly secondary to Today's Battle.

---

# 31. COMPLETED HOME

After completing:

```text
TODAY'S BATTLE ✓

901
TOP 9%

NEXT BATTLE
TOMORROW
```

Then rival.

Official Battle button disappears.

Never confuse users into thinking they can submit another official attempt.

---

# 32. BATTLE INTRO

Very minimal.

```text
TODAY'S BATTLE

3 challenges
~3 minutes

● ○ ○

OFFICIAL ATTEMPT

[ START BATTLE ]
```

No exact challenge names.

No clutter.

---

# 33. GAME SCREEN PHILOSOPHY

Gameplay screens should have:

### Top

Challenge + progress + timer

### Middle

Game

### Bottom

Instruction + current score

That's almost everything.

---

# 34. SNAP — FINAL DESIGN DIRECTION

Current Stitch screen is **too decorative**.

Snap should feel:

# FAST + CLEAN + FOCUSED

Structure:

```text
SNAP                 18
1 / 3

        GAME AREA

           🔵

TAP BLUE

Score       240/300
```

---

# 35. SNAP GAME AREA

The game area should take:

### ~60–65% of viewport.

No random decorative icons.

Every object must be:

- target
- distractor
- game state

Nothing else.

---

# 36. SNAP VISUAL FEEDBACK

Correct:

**small pulse**

Incorrect:

**small shake**

No giant:

> WRONG!!

overlay.

We need speed.

---

# 37. SNAP COMPLETION

Short result:

```text
SNAP COMPLETE

287 / 300
```

~700 ms.

Then automatic transition.

---

# 38. SHIFT — DESIGN PERSONALITY

Shift should contrast Snap.

Snap:

**dynamic**

Shift:

**precise**

Visual center:

# GRID

Not cards everywhere.

Structure:

```text
SHIFT                 24
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
```

Grid should dominate the screen.

---

# 39. CROWD CALL — DESIGN PERSONALITY

This should be the most social-looking game.

Not more colorful.

More **expressive**.

Question:

```text
CROWD CALL             18

WHAT WOULD MOST PEOPLE
CHOOSE?

You suddenly get ₹500 tonight.

[ Movie ]

[ Food + Hangout ]

[ Gaming ]

[ Save it ]
```

Large question.

Large answer cards.

Minimal additional UI.

---

# 40. CROWD RESULT

Then transform:

```text
THE CROWD SAID

Movie          19%
████████

Food + Hangout 36%
██████████████

Gaming         27%
██████████

Save           18%

YOU PREDICTED IT ✓

+120
```

This should be a satisfying reveal.

---

# 41. RESULTS — DESIGN PERSONALITY

Result screen should feel like:

# **SPORTS SCOREBOARD**

but premium.

Not a spreadsheet.

First visual:

# 901

Second:

**TOP 9%**

Third:

**+37 FROM YESTERDAY**

Fourth:

Battle Breakdown.

Fifth:

Rival.

---

# 42. RESULT SCREEN STRUCTURE

```text
TODAY'S RESULT

             901

           TOP 9%

      +37 FROM YESTERDAY

────────────────────────

BATTLE BREAKDOWN

⚡ Snap          287
🧠 Shift         252
🎯 Crowd Call    271

────────────────────────

PERSONAL BEST    927
AVERAGE          806

────────────────────────

YOUR RIVAL

Rahul            914
You              901

13 points to catch

[ BEAT RAHUL ]

[ SHARE RESULT ]
```

---

# 43. SCORE REVEAL ANIMATION

The score should count up:

```text
0
173
412
682
824
901
```

Duration:

**700–1000 ms**

Then percentile appears.

Then improvement.

Then rival.

This sequence is important.

Don't show everything simultaneously.

---

# 44. RIVAL SCREEN

This should be one of the strongest pages.

Concept:

# ONE PERSON. ONE GAP. ONE GOAL.

Structure:

```text
YOUR RIVAL

        Rahul
         914

          VS

        Sanket
         901

    13 POINTS TO CATCH

      [ BEAT RAHUL ]
```

Then:

### Match history

small and clean.

No friends list here.

---

# 45. FRIENDS SCREEN

Friends = competition network.

Not social network.

```text
FRIENDS

[ + ADD FRIEND ]

TODAY

Rahul       914
Sanket      901
Neha        817
Aman        761
```

No:

- likes
- followers
- comments
- posts
- chat

---

# 46. ADD FRIEND

Simple:

```text
ADD A FRIEND

Enter Battle Code

[ B7K9P2 ]

[ ADD FRIEND ]

Your Battle Code

K4X8M9
[ COPY ]
```

No contact permission.

No phone-number search.

No location.

---

# 47. PROFILE — BATTLE DNA

Profile should communicate:

> Who am I as a player?

Structure:

```text
Sanket

🔥 7 DAY MOMENTUM

BEST SCORE
927

AVERAGE
806

BATTLE DNA

⚡ SPEED       91
🧠 MEMORY      78
🎯 PEOPLE      94

PERSONAL RECORDS

Best Score
Best Momentum
Battles Played
```

No IQ.

No “You are smarter than X%.”

This is a game profile, not a scientific assessment.

---

# 48. HISTORY

Purpose:

# **Show improvement**

Not analytics overload.

One graph:

```text
YOUR SCORE

950 ┤         ●
900 ┤     ●   │
850 ┤  ●      ●
800 ┤●
    └────────────
```

Then:

Best

Average

Momentum

Recent battles.

That's enough.

---

# 49. SETTINGS

Very lightweight.

```text
SETTINGS

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
```

No fancy cards.

---

# 50. PLAYER STATES

We need a complete UI state system.

## Button

```text
Default
Pressed
Disabled
Loading
Success
```

## Game

```text
Ready
Active
Correct
Incorrect
Complete
```

## Battle

```text
Not Started
In Progress
Completed
```

## Friend

```text
Pending
Accepted
Blocked
```

---

# 51. EMPTY STATES

Never use blank screens.

Example Friends:

```text
NO RIVALS YET

Add a friend and start
your first Battle rivalry.

[ ADD FRIEND ]
```

Simple illustration/graphic can be used.

Not giant illustrations.

---

# 52. ERROR STATES

Example:

```text
COULDN'T LOAD TODAY'S BATTLE

Check your connection
and try again.

[ RETRY ]
```

Never throw user back to login.

---

# 53. LOADING STATES

Use subtle skeleton/loading indicators.

Not:

> Loading... Loading... Loading...

No giant spinning loader unless necessary.

---

# 54. MOTION DESIGN

Motion should communicate:

### change

### feedback

### reward

Not decoration.

---

## Transition speed

### Normal navigation

**200–300 ms**

### Game transitions

**250–400 ms**

### Score reveal

**700–1000 ms**

### Button press

**100–150 ms**

---

# 55. HOME → BATTLE

The Battle card can slightly expand/zoom.

Then fade into gameplay.

This gives the Battle a distinct entrance.

---

# 56. BETWEEN CHALLENGES

After Snap:

```text
SNAP COMPLETE
287 / 300
```

Then:

**SHIFT**

No manual Continue button.

The Battle should flow continuously.

---

# 57. RESULT ANIMATION

Sequence:

```text
TODAY'S RESULT
       ↓
Score count
       ↓
Percentile
       ↓
Improvement
       ↓
Breakdown
       ↓
Rival
```

Each stage enters quickly.

This creates the emotional rhythm.

---

# 58. HAPTICS

### Button

Light.

### Correct

Light success.

### Wrong

Short error.

### Personal best

Stronger success.

### Battle complete

Medium confirmation.

Haptics should never be excessive.

---

# 59. SOUND

No continuous background music in MVP.

Use:

- tap
- correct
- wrong
- score reveal
- personal best
- battle complete

Provide:

```text
Sound ON
Haptics ON
```

---

# 60. ACCESSIBILITY

Never communicate state only with color.

For example:

Selected option:

```text
purple border
+
check
+
background change
```

not just:

> purple = selected

Snap:

Shapes should complement colors.

Text contrast must remain strong.

---

# 61. CONTENT DENSITY RULE

This should become one of our strictest design rules:

# **One screen = one dominant job.**

Home:

**Play**

Snap:

**React**

Shift:

**Remember**

Crowd:

**Predict**

Result:

**Understand performance**

Rival:

**Beat someone**

Friends:

**Manage rivals**

Profile:

**Understand yourself**

History:

**See progress**

---

# 62. NO MICRO-TEXT OVERLOAD

Current Stitch uses many tiny labels:

- score metadata
- badges
- little category labels
- mini descriptions
- status chips

We should aggressively remove anything nonessential.

Ask:

> Can the user understand this screen without this label?

If yes:

**delete it.**

---

# 63. NO "LIVE"

Important.

We don't have live multiplayer in MVP.

So remove:

> LIVE

Use:

**READY**

**TODAY**

**PRACTICE**

**OFFICIAL**

depending on context.

---

# 64. OFFICIAL vs PRACTICE

This needs strong visual distinction.

### Official:

```text
OFFICIAL BATTLE
```

### Practice:

```text
PRACTICE
```

Practice should never accidentally look like it changes rankings.

---

# 65. SOCIAL LANGUAGE

Use personal language.

### Good

> Rahul is 13 points ahead.

> Beat Rahul.

> New personal best.

> You're 37 points ahead of yesterday.

### Avoid

> Global Rank

> Competitive Rating

> Elo

> Tier 4

at MVP stage.

---

# 66. NO GLOBAL LEADERBOARD

Use:

### Percentile

> TOP 9%

and:

### Friends

> Rahul 914  
> You 901

That's enough.

Global leaderboard can come much later, if user research shows demand.

---

# 67. NO COINS

Don't have:

> 500 coins

> 320 XP

> 2 gems

unless we discover a real economy use case later.

The game reward is:

### score

### improvement

### rivalry

That's cleaner.

---

# 68. PERSONALIZATION

MVP:

**same official challenge**

for everyone.

Later:

practice can adapt.

Example:

```text
Official
Everyone gets same Battle

Practice
AI adapts difficulty to user
```

This distinction should remain in the product architecture.

---

# 69. AI VISUALIZATION

Do NOT prominently advertise:

> AI-powered challenge generation

on every screen.

User doesn't care.

AI is infrastructure.

Eventually:

```text
User history
 ↓
performance
 ↓
challenge selection
 ↓
practice personalization
```

The app remains about:

> **playing**

not:

> “using AI.”

---

# 70. REAL-WORLD MODE

Not in MVP.

Future:

### Wild Card

Could appear as one optional challenge.

But it must use exactly the same visual system.

Don't make the entire app camera-first.

---

# 71. DESIGN COMPONENT LIBRARY

Figma should contain:

## Foundations

Colors

Typography

Spacing

Grid

Elevation

---

## Buttons

Primary

Secondary

Ghost

Icon

---

## Navigation

Bottom Nav

Top Bar

Back Button

---

## Cards

Battle Card

Rival Card

Stat Card

Result Card

Friend Row

---

## Game

Timer

Progress Dots

Instruction

Answer Card

Grid Cell

Score

Feedback

---

## Social

Friend Row

Rival Row

Challenge Card

Share Card

---

## Feedback

Toast

Success

Error

Loading

Empty

---

# 72. Figma VARIABLES

Use variables rather than manually entering every value.

Example:

```text
Color/
  background/primary
  surface/1
  surface/2
  text/primary
  text/secondary
  accent/primary
  semantic/success
  semantic/error
  semantic/warning
```

And:

```text
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
  64
```

This will massively help consistency.

---

# 73. Figma COMPONENT VARIANTS

For example:

### Button

```text
Type:
Primary
Secondary
Ghost

State:
Default
Pressed
Disabled
Loading
```

### Answer Card

```text
Default
Pressed
Selected
Correct
Incorrect
```

### Friend Row

```text
Normal
You
Rival
Pending
```

---

# 74. SCREEN ORGANIZATION IN FIGMA

Create exactly:

```text
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
```

---

# 75. PROTOTYPE FLOW

Primary:

```text
Welcome
 ↓
Name
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
```

Secondary:

```text
Rival
 ↓
Practice
```

Third:

```text
Friends
 ↓
Add Friend
 ↓
Incoming Challenge
```

---

# 76. RESPONSIVENESS

The 390×844 design is the master.

At 360 width:

### Never shrink typography drastically.

Instead:

- reduce horizontal gaps
- reduce card padding
- allow text wrap
- stack elements when necessary

At 412 width:

- increase breathing room
- don't simply make everything larger

---

# 77. GAME AREA RESPONSIVENESS

Snap:

Game area should scale proportionally.

Shift:

Grid cells remain comfortably tappable.

Crowd:

Answer cards should remain at least approximately:

### 52–56 px touch height

---

# 78. TOUCH TARGETS

Interactive elements should generally be:

### ≥44 × 44 px

Prefer:

### 48–52 px

for primary controls.

This is especially important because the game is fast.

---

# 79. SCORE SYSTEM VISUAL LANGUAGE

Every score should have a context.

Bad:

> 901

Good:

> **901 / 1000**

or:

> **901**

with:

> TOP 9%

Score must never look ambiguous.

---

# 80. PERSONAL BEST

This deserves its own micro-state.

When new record:

```text
NEW PERSONAL BEST

927

+23
```

Use subtle celebration:

- small particles
- haptic
- score glow

Not fireworks covering the entire screen.

---

# 81. RIVAL GAP

Use a very simple visual:

```text
Rahul     914
──────────────
You       901

13 points
```

Don't build huge charts for a 13-point difference.

---

# 82. FRIENDS SORTING

Default:

### Highest today's score first.

But user always has a special:

> **YOU**

position.

If user is not first:

still visually locate them.

---

# 83. FRIENDS PRIVACY

Don't reveal:

- location
- phone number
- email
- private stats

unless explicitly required.

Only:

**Battle name + game stats relevant to competition.**

---

# 84. SHARE CARD DESIGN

Share card should be very simple:

```text
DAILY BATTLE

901

TOP 9%

Can you beat me?

[ PLAY ]
```

Brand identity strong.

No massive UI screenshot.

This is meant for:

- WhatsApp
- Instagram
- Telegram
- direct messages

---

# 85. BRAND LANGUAGE

Tone:

### Confident

### concise

### playful

### competitive

Not:

- corporate
- childish
- motivational-speaker style
- fake scientific

---

# 86. Copy examples

### Home

> Good evening, Sanket.

### Battle

> 3 challenges. ~3 minutes.

### Rival

> 13 points to catch.

### Result

> New personal best.

### Practice

> Official score stays locked.

### Tomorrow

> Tomorrow's Battle awaits.

No overlong descriptions.

---

# 87. KEY DESIGN PRINCIPLE FOR DAILY RETURN

We don't want:

> “Keep scrolling.”

We want:

> **“I'm done today.”**

That means the app should have a satisfying ending.

The result screen should communicate completion.

The user leaves with:

**score**

**rival**

**tomorrow**

That is the product loop.

---

# 88. CURRENT STITCH DESIGN — WHAT WE ARE SPECIFICALLY ABANDONING

Based on your screenshots, I would remove or heavily reduce:

### ❌ Tiny status labels everywhere

### ❌ "LIVE"

### ❌ excessive purple glow

### ❌ oversized central Snap decoration

### ❌ repeated nested cards

### ❌ huge analytical Rival page

### ❌ Friends Standing inside Rival

### ❌ charts on screens where they aren't necessary

### ❌ excessive micro-statistics

### ❌ floating Battle button that feels disconnected

### ❌ too many elements competing for attention

---

# 89. WHAT WE ARE KEEPING FROM STITCH

There are good things in the current work.

### ✅ Dark background

### ✅ Purple brand accent

### ✅ Strong score typography

### ✅ Rival concept

### ✅ Today's Battle hero

### ✅ Bottom navigation concept

### ✅ Battle breakdown

### ✅ Friend comparison

### ✅ Overall premium direction

We don't throw everything away.

We **reconstruct it properly**.

---

# 90. THE FINAL VISUAL RHYTHM

This is perhaps the most important thing in the entire specification:

```text
               DASHBOARD
                  HOME
                    ↓
                 IMMERSION
                 SNAP
                    ↓
                 PRECISION
                 SHIFT
                    ↓
                  SOCIAL
               CROWD CALL
                    ↓
                 REWARD
                 RESULT
                    ↓
                COMPETITION
                  RIVAL
                    ↓
                  EXIT
             "SEE YOU TOMORROW"
```

Every stage has a different emotional purpose.

That is what will make the app feel designed rather than assembled.

---

# 91. FINAL MASTER RULES — LOCK THESE

## VISUAL

**Dark ink background**

**Electric violet primary**

**Muted semantic colors**

**Inter**

**8pt spacing**

**16px major cards**

**52px primary buttons**

**20px page margins**

**12px minimum text**

**Minimal shadows**

**Minimal gradients**

---

## UX

**One screen = one dominant job**

**Gameplay = fullscreen focus**

**Dashboard = restrained**

**Result = emotional**

**Rival = personal**

**Friends = competition, not social feed**

---

## PRODUCT

**3 official challenges**

**~3 minutes**

**same official Battle for everybody**

**one official attempt**

**practice separate**

**personal score**

**percentile**

**friend rivalry**

**momentum**

**tomorrow's unknown Battle**

---

## DON'T

**No global leaderboard**

**No coins**

**No chat**

**No infinite feed**

**No IQ claims**

**No camera-first experience**

**No real-time multiplayer**

**No excessive AI branding**

**No fake LIVE state**

**No dashboard clutter**

---

# 92. Most important: this becomes our design constitution

From now on, every screen we design should be checked against these questions:

### 1.
**What is the one thing this screen wants the user to do/feel?**

### 2.
**Is there one obvious visual hierarchy?**

### 3.
**Does the screen look like Daily Battle, or like a generic dark app?**

### 4.
**Are we using cards because they're useful, or because the designer doesn't know how else to structure information?**

### 5.
**Can the user understand the screen without reading tiny text?**

### 6.
**Does this screen strengthen the 3-minute daily ritual?**

---

# 🔒 FINAL DESIGN DIRECTION

I would now **stop trying to make Stitch generate the complete UI**.

Use Stitch for rough exploration only.

Build the final design system manually in Figma using this specification.

The current Stitch screenshots are useful because they gave us the **information architecture and rough component ideas**. But the final product needs to be rebuilt around:

> **Premium dark product design → immersive gameplay → dramatic result → personal rivalry.**

That is the actual design identity of Daily Battle.

## Next step

Now we should design the **Figma Foundations + Components first**, before touching individual screens:

**Color variables → Typography → spacing → buttons → cards → navigation → score component → timer → progress → answer cards → friend/rival rows.**

Once those are locked, we can design **Home screen pixel-by-pixel**, then Snap, Shift, Crowd Call, Result, and Rival. This will prevent exactly the inconsistency you're seeing in the Stitch output.